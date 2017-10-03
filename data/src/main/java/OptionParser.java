import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionParser {

    private static final Logger log = LoggerFactory.getLogger(OptionParser.class);
    private String[] args = null;
    private Options options = new Options();


    public OptionParser(String[] args) {
        this.args = args;
        options.addRequiredOption("s", "src", true, "Specifies Music Brainz dump directory.");
        options.addRequiredOption("d", "dst", true, "Specifies Dataset output directory.");
        options.addOption("n", "num", true, "Specifies number of artist files. Default value is " + DumpConverter.DEFAULT_NUMBER_OF_ARTIST_DOCS);
        options.addOption("u", "users", true, "Specifies number of users, which will be used for rating generation. Default value is " + DumpConverter.DEFAULT_NUMBER_OF_USERS);
        options.addOption("c", "chosen", false, "When specified only data with images will be converted. Execution time will be raised.");
        options.addOption("h", "help", false, "Prints usage information.");
    }

    public void parseOpts() {

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                help();
            }

            DumpConverter dumpConverter = new DumpConverter(cmd.getOptionValue("s"), cmd.getOptionValue("d"));
            if (cmd.hasOption("c")) {
                dumpConverter.setConvertOnlyWithImages(true);
            }

            if (cmd.hasOption("n")) {
                Long numberOfArtistRecords = null;
                try {
                    numberOfArtistRecords = Long.parseLong(cmd.getOptionValue("n"));
                } catch (NumberFormatException e) {
                    log.error("Failed to parse option as number");
                    help();
                }

                if (numberOfArtistRecords != null && numberOfArtistRecords <= 0) {
                    log.error("Number of Artist documents must be greater than zero");
                    help();
                }

                dumpConverter.setNumberOfArtists(numberOfArtistRecords);
            }

            if (cmd.hasOption("u")) {
                Long numberOfUsers = null;
                try {
                    numberOfUsers = Long.parseLong(cmd.getOptionValue("u"));
                } catch (NumberFormatException e) {
                    log.error("Failed to parse option as number");
                    help();
                }

                if (numberOfUsers != null && numberOfUsers <= 0) {
                    log.error("Number of users must be greater than zero");
                    help();
                }

                dumpConverter.setNumberOfUsers(numberOfUsers);
            }

            dumpConverter.convert();
        } catch (ParseException e) {
            log.error("Failed to parse command line properties. {}", e.getMessage());
            help();
        }

    }

    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("mb-dump-converter-1.0-SNAPSHOT.jar", options);
        System.exit(0);
    }


}
