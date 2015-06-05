/**
 * Copyright (c) 2014, the Temporal Random Indexing AUTHORS.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the University of Bari nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * GNU GENERAL PUBLIC LICENSE - Version 3, 29 June 2007
 *
 */
package di.uniba.it.tri.gbooks;

import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.ReverseObjectVectorComparator;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;

/**
 *
 * @author pierpaolo
 */
public class GbooksInMemoryReader {

    private static final Logger LOG = Logger.getLogger(GbooksInMemoryReader.class.getName());

    private int dimension = 1000;

    private int seed = 10;

    private DB db;

    private HTreeMap<String, Integer> dict;

    private NavigableSet<Fun.Tuple2<Integer, CountEntry>> occSet;

    private Map<Integer, Vector> ri;

    private Map<Integer, String> inverseDict;

    private Random random;

    public void init(String storageDirname) throws IOException {
        //load DB
        File dbfile = new File(storageDirname + "/dbmap/gbmap");
        db = DBMaker.newFileDB(dbfile).cacheHardRefEnable().mmapFileEnableIfSupported().transactionDisable().closeOnJvmShutdown().make();
        //dictionary
        dict = db.get("dict");
        LOG.info("Init inverse map...");
        inverseDict = new HashMap<>();
        Iterator<String> iterator = dict.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            inverseDict.put(dict.get(key), key);
        }
        //co-occur info
        occSet = db.get("occ");
        ri = new HashMap<>();
        random = new Random();
    }

    public Vector getVector(String word, int startYear, int endYear) throws IOException {
        Integer wordId = dict.get(word);
        if (wordId != null) {
            Vector vector = VectorFactory.createZeroVector(VectorType.REAL, dimension);
            Iterable<CountEntry> occit = Fun.filter(occSet, wordId);
            for (CountEntry entry : occit) {
                if (entry.getYear() >= startYear && entry.getYear() <= endYear) {
                    Vector riv = ri.get(entry.getWordId());
                    if (riv == null) {
                        riv = VectorFactory.generateRandomVector(VectorType.REAL, dimension, seed, random);
                        ri.put(entry.getWordId(), riv);
                    }
                    vector.superpose(riv, entry.getCount(), null);
                }
            }
            if (!vector.isZeroVector()) {
                vector.normalize();
            }
            return vector;
        } else {
            return null;
        }
    }

    public List<ObjectVector> getNearestVectors(Vector v, int startYear, int endYear, int n) throws IOException {
        PriorityQueue<ObjectVector> queue = new PriorityQueue<>();
        Iterator<String> it = dict.keySet().iterator();
        int c = 0;
        System.out.println();
        while (it.hasNext()) {
            String word = it.next();
            Vector wordv = getVector(word, startYear, endYear);
            if (wordv != null) {
                double sim = wordv.measureOverlap(v);
                ObjectVector ov = new ObjectVector(word, sim);
                if (queue.size() <= n) {
                    queue.offer(ov);
                } else {
                    queue.poll();
                    queue.offer(ov);
                }
            }
            c++;
            if (c % 1000 == 0) {
                System.out.print(".");
                if (c % 100000 == 0) {
                    System.out.println("." + c);
                }
            }
        }
        System.out.println();
        queue.poll();
        List<ObjectVector> list = new ArrayList<>(queue);
        Collections.sort(list, new ReverseObjectVectorComparator());
        return list;
    }

    public List<ObjectVector> getNearestWords(String word, int startYear, int endYear, int n) throws IOException {
        PriorityQueue<ObjectVector> queue = new PriorityQueue<>();
        Integer wordId = dict.get(word);
        int t = 0;
        if (wordId != null) {
            Iterable<CountEntry> occit = Fun.filter(occSet, wordId);
            for (CountEntry entry : occit) {
                if (entry.getYear() >= startYear && entry.getYear() <= endYear) {
                    ObjectVector ov = new ObjectVector(inverseDict.get(entry.getWordId()), entry.getCount());
                    if (queue.size() <= n) {
                        queue.offer(ov);
                    } else {
                        queue.poll();
                        queue.offer(ov);
                    }
                }
                t++;
            }
        }
        queue.poll();
        List<ObjectVector> list = new ArrayList<>(queue);
        Collections.sort(list, new ReverseObjectVectorComparator());
        System.out.println(list.size() + "/" + t);
        return list;
    }

    public void close() throws IOException {
        db.close();
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input directory")
                .addOption("s", true, "Seed (optional, default 10)")
                .addOption("d", true, "Vector dimensions (optional, default 1000)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i")) {
                GbooksInMemoryReader gtri = new GbooksInMemoryReader();
                gtri.setDimension(Integer.parseInt(cmd.getOptionValue("d", "1000")));
                gtri.setSeed(Integer.parseInt(cmd.getOptionValue("s", "10")));
                try {
                    gtri.init(cmd.getOptionValue("i"));
                    Map<String, Vector> memory = new HashMap<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    boolean read = true;
                    String s;
                    while (read) {
                        System.out.print("> ");
                        s = reader.readLine();
                        if (s.matches("(^lv$)|(^lv\\s+.*$)")) {
                            try {
                                String[] split = s.split("\\s+");
                                if (split.length > 4) {
                                    Vector vector = gtri.getVector(split[2], Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                                    if (vector != null) {
                                        memory.put(split[1], vector);
                                    } else {
                                        System.out.println("Vector not found for: " + split[1]);
                                    }
                                } else {
                                    System.err.println("No valid arguments");
                                }
                            } catch (Exception ex) {
                                System.err.println("Error to execute lv command: " + ex.getMessage());
                            }
                        } else if (s.matches("(^sim$)|(^sim\\s+.*$)")) {
                            try {
                                String[] split = s.split("\\s+");
                                if (split.length > 2) {
                                    Vector v1 = memory.get(split[1]);
                                    Vector v2 = memory.get(split[2]);
                                    if (v1 != null && v2 != null) {
                                        System.out.println("sim(" + split[1] + ", " + split[2] + "): " + v1.measureOverlap(v2));
                                    } else {
                                        System.out.println("Vectors not found");
                                    }
                                } else {
                                    System.err.println("No valid arguments");
                                }
                            } catch (Exception ex) {
                                System.err.println("Error to execute sim command: " + ex.getMessage());
                            }
                        } else if (s.matches("(^nh$)|(^nh\\s+.*$)")) {
                            try {
                                String[] split = s.split("\\s+");
                                if (split.length > 4) {
                                    Vector v = memory.get(split[1]);
                                    if (v != null) {
                                        List<ObjectVector> nv = gtri.getNearestVectors(v, Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                                        for (ObjectVector ov : nv) {
                                            System.out.println(ov.getKey() + "\t" + ov.getScore());
                                        }
                                    } else {
                                        System.out.println("Vector not found");
                                    }
                                } else {
                                    System.err.println("No valid arguments");
                                }
                            } catch (Exception ex) {
                                System.err.println("Error to execute sim command: " + ex.getMessage());
                            }
                        } else if (s.matches("(^nhw$)|(^nhw\\s+.*$)")) {
                            try {
                                String[] split = s.split("\\s+");
                                if (split.length > 4) {
                                    List<ObjectVector> nv = gtri.getNearestWords(split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                                    for (ObjectVector ov : nv) {
                                        System.out.println(ov.getKey() + "\t" + ov.getScore());
                                    }
                                } else {
                                    System.err.println("No valid arguments");
                                }
                            } catch (Exception ex) {
                                System.err.println("Error to execute sim command: " + ex.getMessage());
                            }
                        } else if (s.equals("!q")) {
                            System.out.println("Goodbye");
                            read = false;
                        } else {
                            System.out.println("Command not valid: " + s);
                        }
                    }
                    reader.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        gtri.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GbooksInMemoryReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("TRI Gbooks reader", options, true);
            }
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

}