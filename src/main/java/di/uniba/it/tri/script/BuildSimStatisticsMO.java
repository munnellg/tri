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
package di.uniba.it.tri.script;

import di.uniba.it.tri.space.TemporalSpaceUtils;
import di.uniba.it.tri.api.Tri;
import di.uniba.it.tri.api.TriResultObject;
import di.uniba.it.tri.vectors.Vector;
import di.uniba.it.tri.vectors.VectorFactory;
import di.uniba.it.tri.vectors.VectorReader;
import di.uniba.it.tri.vectors.VectorType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 *
 * @author pierpaolo
 */
public class BuildSimStatisticsMO {

    static Options options;

    static CommandLineParser cmdParser = new BasicParser();

    static {
        options = new Options();
        options.addOption("i", true, "Input directory")
                .addOption("o", true, "Output file")
                .addOption("f", true, "Output format: plain or csv (default=plain)")
                .addOption("m", true, "Mode: pointwise (point) or cumulative (cum) (default=cum)");
    }

    private static final Logger LOG = Logger.getLogger(BuildSimStatisticsMO.class.getName());

    /**
     *
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("i") && cmd.hasOption("o")) {
                String format = cmd.getOptionValue("f", "plain");
                String mode = cmd.getOptionValue("m", "cum");
                if (!(format.equals("plain") || format.equals("csv"))) {
                    throw new IllegalArgumentException("No valid format");
                }
                if (!(mode.equals("point") || mode.equals("cum"))) {
                    throw new IllegalArgumentException("No valid mode");
                }
                Tri api = new Tri();
                api.setMaindir(cmd.getOptionValue("i"));
                //load elemental vector
                api.load("file", null, "-1");
                char sep = '\t';
                List<String> years = api.year(0, Integer.MAX_VALUE);
                BufferedWriter writer = new BufferedWriter(new FileWriter(cmd.getOptionValue("o")));
                if (format.equals("csv")) {
                    writer.append(",word");
                    for (String year : years) {
                        writer.append(",");
                        writer.append(year);
                    }
                    writer.newLine();
                    sep = ',';
                }
                VectorReader evr = api.getStores().get(Tri.ELEMENTAL_NAME);
                int dimension = evr.getDimension();
                LOG.info("Vector dimension: " + dimension);
                List<String> keys = new ArrayList<>();
                Iterator<String> it = evr.getKeys();
                while (it.hasNext()) {
                    keys.add(it.next());
                }
                evr.close();
                LOG.info("Words: " + keys.size());
                Collections.sort(years);
                Map<String, double[]> values = new HashMap<>();
                Map<String, Vector> pre_map = new HashMap<>();
                for (String key : keys) {
                    pre_map.put(key, VectorFactory.createZeroVector(VectorType.REAL, dimension));
                    values.put(key, new double[years.size()]);
                }
                for (int i = 0; i < years.size(); i++) {
                    int c = 0;
                    String year = years.get(i);
                    LOG.info(year);
                    VectorReader vr2 = TemporalSpaceUtils.getVectorReader(new File(cmd.getOptionValue("i")), year, true);
                    vr2.init();
                    LOG.info("Computing...");
                    for (String key : keys) {
                        Vector copy = null;
                        Vector v2 = vr2.getVector(key);
                        if (v2 != null) {
                            copy = v2.copy();
                        }
                        Vector v1 = pre_map.get(key);
                        double[] a = values.get(key);
                        if (copy != null) {
                            if (mode.equals("cum")) {
                                copy.superpose(v1, 1, null);
                                copy.normalize();
                                a[i] = copy.measureOverlap(v1);
                                pre_map.put(key, copy);
                            } else {
                                a[i] = copy.measureOverlap(v1);
                                pre_map.put(key, copy);
                            }
                        } else {
                            a[i] = 0;
                        }
                        c++;
                        if (c % 1000 == 0) {
                            System.out.print(".");
                            if (c % 100000 == 0) {
                                System.out.println(c);
                            }
                        }
                    }
                }
                int id = 0;
                for (String key : keys) {
                    if (format.equals("csv")) {
                        writer.append(String.valueOf(id));
                        writer.append(sep);
                    }
                    writer.append(key);
                    double[] a = values.get(key);
                    for (double v : a) {
                        if (v >= 0) {
                            writer.append(sep).append(String.valueOf(v));
                        } else {
                            writer.append(sep).append(String.valueOf(0d));
                        }
                    }
                    writer.newLine();
                    id++;
                }
                writer.close();
            } else {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("Build sim matrix (memory optimized)", options, true);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }

}