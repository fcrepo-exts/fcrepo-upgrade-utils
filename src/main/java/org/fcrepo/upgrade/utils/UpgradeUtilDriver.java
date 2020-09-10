/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.upgrade.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author dbernstein
 * @since 2019-08-05
 */
public class UpgradeUtilDriver {

    private static final org.slf4j.Logger logger = getLogger(UpgradeUtilDriver.class);

    public static final Map<FedoraVersion, Set<FedoraVersion>> VALID_MIGRATION_PATHS = new HashMap<>();

    static {
        VALID_MIGRATION_PATHS.put(FedoraVersion.V_4_7_5, Set.of(FedoraVersion.V_5));
        VALID_MIGRATION_PATHS.put(FedoraVersion.V_5, Set.of(FedoraVersion.V_6));
    }

    private UpgradeUtilDriver() {
        // Prevent public instantiation
    }

    /**
     * The main entry point
     *
     * @param args from the command line
     */
    public static void main(final String[] args) {
        final UpgradeUtilDriver driver = new UpgradeUtilDriver();

        try {
            driver.run(args);

        } catch (final Exception e) {
            logger.error("Error performing upgrade: {}", e.getMessage());
            logger.debug("Stacktrace: ", e);
        }
    }

    private void run(final String[] args) {

        // Help option
        final Options configOptions = new org.apache.commons.cli.Options();

        configOptions.addOption(Option.builder("h")
                                      .longOpt("help")
                                      .hasArg(false)
                                      .desc("Print these options")
                                      .required(false)
                                      .build());

        configOptions.addOption(Option.builder("i")
                                      .longOpt("input-dir")
                                      .hasArg(true)
                                      .desc("The path to the directory containing a Fedora 4.7.x or Fedora 5.x export")
                                      .required(true)
                                      .build());

        configOptions.addOption(Option.builder("o")
                                      .longOpt("output-dir")
                                      .hasArg(true)
                                      .desc("The path to the directory where upgraded resources will be written. " +
                                            "Default value: output_<yyyyMMdd-HHmmss>. " +
                                            "For example: output_20200101-075901")
                                      .required(false)
                                      .build());

        configOptions.addOption(Option.builder("s")
                                      .longOpt("source-version")
                                      .hasArg(true)
                                      .desc(format(
                                          "The version of Fedora that was the source of the export. Valid values: %s",
                                          join(VALID_MIGRATION_PATHS.keySet())))
                                      .required(true)
                                      .build());

        configOptions.addOption(Option.builder("t")
                .longOpt("target-version")
                .hasArg(true)
                .desc(format("The version of Fedora to which you are upgrading. Valid values: %s",
                        join(VALID_MIGRATION_PATHS.values().stream()
                                .flatMap(x -> x.stream()).collect(Collectors.toSet()))))
                .required(true)
                .build());

        // first see if they've specified a config file
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(configOptions, args);
        } catch (final ParseException e) {
            printHelpAndExit(e.getMessage(), configOptions);
            return;
        }

        final File inputDir = new File(cmd.getOptionValue("i"));

        if (!inputDir.exists()) {
            printHelpAndExit(format("input directory %s does not exist.", inputDir.getAbsolutePath()), configOptions);
        }

        final String outputDirStr = cmd.getOptionValue("o");

        final File outputDir;

        if (outputDirStr == null) {
            final var date = Calendar.getInstance().getTime();
            final var dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            final var strCurrentTime = dateFormat.format(date);
            outputDir = new File("output_" + strCurrentTime);
            outputDir.mkdirs();
        } else {
            outputDir = new File(outputDirStr);
            if (!outputDir.exists()) {
                printHelpAndExit(format("output directory %s does not exist.", outputDir.getAbsolutePath()),
                        configOptions);
            }
        }

        try {
            final var config = new Config();
            config.setSourceVersion(FedoraVersion.fromString(cmd.getOptionValue("s")));
            config.setTargetVersion(FedoraVersion.fromString(cmd.getOptionValue("t")));
            config.setInputDir(inputDir);
            config.setOutputDir(outputDir);
            //start the upgrade
            final var manager = UpgradeManagerFactory.create(config);
            manager.start();
        } catch (final Exception e) {
            logger.error("Upgrade failed.", e);
            printHelpAndExit(format("Upgrade failed: %s  -> See log for details.", e.getMessage()), configOptions);
        }
    }

    private Object join(Collection<FedoraVersion> versions) {
        return versions.stream().map(x -> x.getStringValue()).collect(Collectors.joining(","));
    }

    private static void printHelpAndExit(final String errorMessage, final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        System.err.println(errorMessage);
        formatter.printHelp("fcepo-upgrade-util", options);
        System.exit(1);
    }
}
