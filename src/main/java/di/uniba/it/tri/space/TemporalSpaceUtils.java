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
package di.uniba.it.tri.space;

import di.uniba.it.tri.space.clustering.Clusters;
import di.uniba.it.tri.shell.TriShell;
import di.uniba.it.tri.vectors.FileVectorReader;
import di.uniba.it.tri.vectors.MapVectorReader;
import di.uniba.it.tri.vectors.MemoryVectorReader;
import di.uniba.it.tri.vectors.ObjectVector;
import di.uniba.it.tri.vectors.RealVector;
import di.uniba.it.tri.vectors.ReverseObjectVectorComparator;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorStoreUtils;
import di.uniba.it.tri.vectors.VectorType;
import static di.uniba.it.tri.vectors.VectorUtils.getNearestVector;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * Utils for managing WordSpaces
 *
 * @author pierpaolo
 */
public class TemporalSpaceUtils {

    /**
     * Combine two or more WordSpaces using vectors sum
     *
     * @param spaces WordSpaces
     * @return The WordSpace as a Map that is the combination of given
     * WordSpaces
     */
    public static Map<String, Vector> combineSpaces(Map<String, Vector>... spaces) {
        Map<String, Vector> newSpace = new HashMap<>();
        for (Map<String, Vector> space : spaces) {
            Iterator<String> iterator = space.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Vector v = space.get(key);
                Vector nw = newSpace.get(key);
                if (nw != null) {
                    nw.superpose(v, 1, null);
                } else {
                    newSpace.put(key, v);
                }
            }
        }
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().normalize();
        }
        return newSpace;
    }

    /**
     * Combine two or more VectorReaders using vectors sum
     *
     * @param readers VectorReaders
     * @return The WordSpace as a Map that is the combination of given
     * VectorReaders
     * @throws IOException
     */
    public static Map<String, Vector> combineVectorReader(VectorReader... readers) throws IOException {
        Map<String, Vector> newSpace = new HashMap<>();
        for (VectorReader reader : readers) {
            Iterator<ObjectVector> allVectors = reader.getAllVectors();
            while (allVectors.hasNext()) {
                ObjectVector ov = allVectors.next();
                Vector nw = newSpace.get(ov.getKey());
                if (nw != null) {
                    nw.superpose(ov.getVector(), 1, null);
                } else {
                    newSpace.put(ov.getKey(), ov.getVector());
                }
            }
        }
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().normalize();
        }
        return newSpace;
    }

    /**
     * Combine two or more VectorReaders using vectors sum
     *
     * @param readers VectorReaders
     * @return The VectorReader that is the combination of given VectorReaders
     * @throws IOException
     */
    public static VectorReader combineAndBuildVectorReader(VectorReader... readers) throws IOException {
        Map<String, Vector> newSpace = new HashMap<>();
        System.out.println();
        for (VectorReader reader : readers) {
            System.out.print(".");
            Iterator<ObjectVector> allVectors = reader.getAllVectors();
            while (allVectors.hasNext()) {
                ObjectVector ov = allVectors.next();
                Vector nw = newSpace.get(ov.getKey());
                if (nw != null) {
                    nw.superpose(ov.getVector(), 1, null);
                } else {
                    newSpace.put(ov.getKey(), ov.getVector());
                }
            }
        }
        System.out.println();
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().normalize();
        }
        return new MapVectorReader(newSpace);
    }

    /**
     * Combine two or more VectorReaders using vectors sum and save the result
     * in a File
     *
     * @param outputFile The File
     * @param readers VectorReaders
     * @throws IOException
     */
    public static void combineAndSaveVectorReader(File outputFile, VectorReader... readers) throws IOException {
        Map<String, Vector> newSpace = new HashMap<>();
        System.out.println();
        for (VectorReader reader : readers) {
            System.out.print(".");
            Iterator<ObjectVector> allVectors = reader.getAllVectors();
            while (allVectors.hasNext()) {
                ObjectVector ov = allVectors.next();
                Vector nw = newSpace.get(ov.getKey());
                if (nw != null) {
                    nw.superpose(ov.getVector(), 1, null);
                } else {
                    newSpace.put(ov.getKey(), ov.getVector());
                }
            }
        }
        System.out.println();
        int dimension = 0;
        Iterator<Vector> iterator = newSpace.values().iterator();
        while (iterator.hasNext()) {
            if (dimension == 0) {
                Vector v = iterator.next();
                dimension = v.getDimension();
                v.normalize();
            } else {
                iterator.next().normalize();
            }
        }
        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        String header = VectorStoreUtils.createHeader(VectorType.REAL, dimension, -1);
        outputStream.writeUTF(header);
        for (Entry<String, Vector> entry : newSpace.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            entry.getValue().writeToStream(outputStream);
        }
        outputStream.close();
        newSpace.clear();
        newSpace = null;
    }

    /**
     * Return the list of the n nearest vectors given a word
     *
     * @param store The VectorReader that contains vectors
     * @param word The word
     * @param n The number of nearest vectors
     * @return The list of the n nearest vectors
     * @throws IOException
     */
    public static List<ObjectVector> getNearestVectors(VectorReader store, String word, int n) throws IOException {
        Vector vector = store.getVector(word);
        if (vector != null) {
            return getNearestVectors(store, vector, n);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Return the list of the n nearest vectors given a vector
     *
     * @param store The VectorReader that contains vectors
     * @param vector The vector
     * @param n The number of nearest vectors
     * @return The list of the n nearest vectors
     * @throws IOException
     */
    public static List<ObjectVector> getNearestVectors(VectorReader store, Vector vector, int n) throws IOException {
        PriorityQueue<ObjectVector> queue = new PriorityQueue<>();
        Iterator<ObjectVector> allVectors = store.getAllVectors();
        while (allVectors.hasNext()) {
            ObjectVector ov = allVectors.next();
            double overlap = ov.getVector().measureOverlap(vector);
            ov.setScore(overlap);
            if (queue.size() <= n) {
                queue.offer(ov);
            } else {
                queue.poll();
                queue.offer(ov);
            }
        }
        queue.poll();
        List<ObjectVector> list = new ArrayList<>(queue);
        Collections.sort(list, new ReverseObjectVectorComparator());
        return list;
    }

    /**
     * Given a directory of stored vector file readers returns the list of files
     * belonging to a specified time period
     *
     * @param startDir The directory
     * @param start The begin of the time period
     * @param end The end of the time period
     * @return The list of files
     */
    public static List<File> getFileTemporalRange(File startDir, int start, int end) {
        List<File> list = new ArrayList<>();
        File[] listFiles = startDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(".vectors")) {
                String name = file.getName().replaceAll(".vectors", "");
                String[] split = name.split("_");
                String stringYear = split[split.length - 1];
                int year = Integer.parseInt(stringYear);
                if (year >= start && year <= end) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    private static Map<String, File> mapSpaceFile = new HashMap<>();

    /**
     * Return a list of available years given the directory where file readers
     * are stored and the time period
     *
     * @param startDir The directory
     * @param start The begin of the time period
     * @param end The end of the time period
     * @return The list of available years
     */
    public static List<String> getAvailableYears(File startDir, int start, int end) {
        mapSpaceFile.clear();
        File[] listFiles = startDir.listFiles();
        Pattern pattern = Pattern.compile("[0-9]+");
        for (File file : listFiles) {
            if (file.getName().endsWith(".vectors")) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.find()) {
                    String stringYear = matcher.group();
                    int year = Integer.parseInt(stringYear);
                    if (year >= start && year <= end) {
                        mapSpaceFile.put(stringYear, file);
                    }
                }
            }
        }
        return new ArrayList(mapSpaceFile.keySet());
    }

    /**
     * Index the file of elemental vectors
     *
     * @param stardDir The directory containing the WordSpaces
     * @return The index of elemental vectors
     * @throws IOException
     */
    public static IndexReader indexElemental(File stardDir) throws IOException {
        File elementalFile = getElementalFile(stardDir);
        return index(elementalFile);
    }

    /**
     * Index a VectorReader in order to search words
     *
     * @param vreader The VectorReader
     * @return The index
     * @throws IOException
     */
    public static IndexReader index(VectorReader vreader) throws IOException {
        Iterator<String> keys = vreader.getKeys();
        RAMDirectory ramDir = new RAMDirectory();
        IndexWriterConfig iwconfig = new IndexWriterConfig(Version.LATEST, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        IndexWriter writer = new IndexWriter(ramDir, iwconfig);
        while (keys.hasNext()) {
            String word = keys.next();
            Document doc = new Document();
            doc.add(new StringField("word", word, Field.Store.YES));
            writer.addDocument(doc);
        }
        writer.close();
        return DirectoryReader.open(ramDir);
    }

    /**
     * Index a file in order to search words
     *
     * @param file The file
     * @return The index
     * @throws IOException
     */
    public static IndexReader index(File file) throws IOException {
        FileVectorReader vreader = new FileVectorReader(file);
        vreader.init();
        IndexReader index = index(vreader);
        vreader.close();
        return index;
    }

    /**
     * Get the elemental vectors file
     *
     * @param startDir The directory containing the WordSpaces
     * @return
     */
    public static File getElementalFile(File startDir) {
        return new File(startDir.getAbsolutePath() + "/vectors.elemental");
    }

    /**
     * Get the file in which vectors of a specified year are stored
     *
     * @param startDir The directory containing the WordSpaces
     * @param year The year
     * @return The file
     */
    public static File getVectorFile(File startDir, int year) {
        return getVectorFile(startDir, String.valueOf(year));
    }

    /**
     * Get the file in which vectors of a specified year are stored
     *
     * @param startDir The directory containing the WordSpaces
     * @param year The year
     * @return The file
     */
    public static File getVectorFile(File startDir, String year) {
        //return new File(startDir.getAbsolutePath() + "/count_" + year + ".vectors");
        return (mapSpaceFile.get(year));
    }

    /**
     * Get the VectorReader in which vectors of a specified year are stored
     *
     * @param startDir The directory containing the WordSpaces
     * @param year The year
     * @param mem
     * @return The file
     * @throws java.io.IOException
     */
    public static VectorReader getVectorReader(File startDir, String year, boolean mem) throws IOException {
        File file = getVectorFile(startDir, year);
        VectorReader vr;
        if (mem) {
            vr = new MemoryVectorReader(file);
            vr.init();
        } else {
            vr = new FileVectorReader(file);
            vr.init();
        }
        return vr;
    }

    /**
     * Get the VectorReader in which vectors of a specified year are stored
     *
     * @param startDir The directory containing the WordSpaces
     * @param year The year
     * @param mem
     * @return The file
     * @throws java.io.IOException
     */
    public static VectorReader getVectorReader(File startDir, int year, boolean mem) throws IOException {
        return getVectorReader(startDir, String.valueOf(year), mem);
    }

    /**
     * Return the less n similar vectors in two WordSpaces
     *
     * @param store1 The first Vector Reader
     * @param store2 The second Vector Reader
     * @param n The number of vectors
     * @param min min threshold
     * @param max max threshold
     * @return The list of less n similar vectors
     * @throws IOException
     */
    public static List<ObjectVector> sims(VectorReader store1, VectorReader store2, int n, double min, double max) throws IOException {
        PriorityQueue<ObjectVector> queue = new PriorityQueue<>();
        Iterator<ObjectVector> allVectors = store1.getAllVectors();
        int c = 0;
        TriShell.println("");
        while (allVectors.hasNext()) {
            ObjectVector ov = allVectors.next();
            Vector vector = store2.getVector(ov.getKey());
            if (vector != null) {
                double overlap = 1 - ov.getVector().measureOverlap(vector);
                if (overlap >= min && overlap <= max) {
                    ov.setScore(overlap);
                    if (queue.size() <= n) {
                        queue.offer(ov);
                    } else {
                        queue.poll();
                        queue.offer(ov);
                    }
                }
            }
            c++;
            if (c % 1000 == 0) {
                TriShell.print(".");
            }
        }
        TriShell.println("");
        queue.poll();
        List<ObjectVector> list = new ArrayList<>(queue);
        Collections.sort(list, new ReverseObjectVectorComparator());
        return list;
    }

    /**
     * Count the number of vectors in a VectorReader
     *
     * @param reader The VectorReader
     * @return The number of vectors
     * @throws IOException
     */
    public static int countVectors(VectorReader reader) throws IOException {
        Iterator<ObjectVector> allVectors = reader.getAllVectors();
        int counter = 0;
        while (allVectors.hasNext()) {
            allVectors.next();
            counter++;
        }
        return counter;
    }

    /**
     * Load stop words from a file (one stop word per line)
     *
     * @param filename The file name
     * @return The set of stop words
     * @throws IOException
     */
    public static Set<String> loadStopWord(String filename) throws IOException {
        return loadStopWord(new File(filename));
    }

    /**
     * Load stop words from a file (one stop word per line)
     *
     * @param file The file
     * @return The set of stop words
     * @throws IOException
     */
    public static Set<String> loadStopWord(File file) throws IOException {
        Set<String> set = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (reader.ready()) {
            set.add(reader.readLine().toLowerCase().trim());
        }
        reader.close();
        return set;
    }

    public static Clusters kMeansCluster(VectorReader vr, List<ObjectVector> objectVectors, int k) throws IOException {
        Clusters clusters = new Clusters(new int[objectVectors.size()], new Vector[k]);
        Random rand = new Random();

        // Initialize cluster mappings randomly.
        for (int i = 0; i < objectVectors.size(); ++i) {
            int randInt = rand.nextInt(Integer.MAX_VALUE);
            clusters.getClusterMappings()[i] = randInt % k;
        }

        // Loop that computes centroids and reassigns members.
        boolean clustering = true;
        while (clustering) {
            // Clear centroid register.
            for (int i = 0; i < clusters.getCentroids().length; ++i) {
                clusters.getCentroids()[i] = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
            }
            // Generate new cluster centroids.
            for (int i = 0; i < objectVectors.size(); ++i) {
                clusters.getCentroids()[clusters.getClusterMappings()[i]].superpose(objectVectors.get(i).getVector(), 1, null);
            }
            for (int i = 0; i < k; ++i) {
                clusters.getCentroids()[i].normalize();
            }

            boolean changeFlag = false;
            // Map items to clusters.
            for (int i = 0; i < objectVectors.size(); i++) {
                int j = getNearestVector(objectVectors.get(i).getVector(), clusters.getCentroids());
                if (j != clusters.getClusterMappings()[i]) {
                    changeFlag = true;
                    clusters.getClusterMappings()[i] = j;
                }
            }
            if (changeFlag == false) {
                clustering = false;
            }
        }

        return clusters;
    }

    public static Vector computeMeanVector(VectorReader vr) throws IOException {
        Iterator<ObjectVector> allVectors = vr.getAllVectors();
        Vector mean = VectorFactory.createZeroVector(VectorType.REAL, vr.getDimension());
        float n = 0;
        while (allVectors.hasNext()) {
            ObjectVector next = allVectors.next();
            mean.superpose(next.getVector(), 1, null);
            n++;
        }
        float[] mv = ((RealVector) mean).getCoordinates();
        for (int i = 0; i < mv.length; i++) {
            mv[i] /= n;
        }
        mean = new RealVector(mv);
        return mean;
    }

}
