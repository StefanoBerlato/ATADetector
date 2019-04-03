package it.unitn.atadetector;

import it.unitn.atadetector.util.ATADLogger;
import it.unitn.atadetector.util.FileUtil;
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.System.exit;


/**
 * Entry point of the application;
 * exit codes:
 *      - 0 success
 *      - 1 error in argument parsing
 *      - 2 cannot load json file
 *      - 3 cannot get apk files (given path is wrong)
 *      - 4 zero apk files found
 */
public class MainAPKLooper {

    // the json patterns and final report
    static JSONObject protectionATADPatterns;
    private static JSONObject finalReport;

    // where to save results
    private static final String resultsFolderPath = System.getProperty("user.dir") + "/results";
    static File resultsFolder = new File(resultsFolderPath);

    // do we have to search for JAVA patterns only in the main package (the one specified in
    // the manifest file)? (used for testing)
    public static boolean onlyMainPackage = false;

    // string to save at the end in an file listing all the apk not
    // analyzed because of errors, along with a brief description of the error
    private static String errors = "";

    // variables for logging
    private static final String className = "MainAPKLooper";
    private static final String sumLongVersionOfJSONReports = "sumLongVersionOfJSONReports";
    private static final String mainMethodName = "main";
    private static final String ataDetectorLogo = "\n" +
            "        _______       _____       _            _             \n" +
            "     /\\|__   __|/\\   |  __ \\     | |          | |            \n" +
            "    /  \\  | |  /  \\  | |  | | ___| |_ ___  ___| |_ ___  _ __ \n" +
            "   / /\\ \\ | | / /\\ \\ | |  | |/ _ \\ __/ _ \\/ __| __/ _ \\| '__|\n" +
            "  / ____ \\| |/ ____ \\| |__| |  __/ ||  __/ (__| || (_) | |   \n" +
            " /_/    \\_\\_/_/    \\_\\_____/ \\___|\\__\\___|\\___|\\__\\___/|_|   \n" +
            "                                                             \n" +
            "                                                            \n";

    // the number of threads that finished (successfully and not)
    private static int successfulFinishedJobs;
    private static int errorFinishedJobs;
    private static int numberOfFoundAPKs;

    /**
     * The main function. First it will parse the JSON pattern file (in the default path inside the project or
     * in the one specified by the user, if any. Then, given the path of directory containing the APK files,
     * it will first acquire a reference to all of these. Then, it will loop for each application and:
     *  - create a temp directory with the same name of the APK file
     *  - invoke (apkTool, dex2jar, ...?) to unpack the APK file and obtain the (jar, folder with all *.class, ...?)
     *    and the "lib" folder with the ".so" binaries
     *  - instantiate an ATADetector, passing as parameter
     * @param args arguments
     */
    public static void main (String[] args) {

        // get the time
        // init and finishing time
        long initTime = new Date().getTime();

        // log the logo
        ATADLogger.directlyLog(ataDetectorLogo);

        // ===== LOCAL VARIABLES =====
        // the path to the directory containing the APKs to analyzeSoLibraries
        String pathOfAPKS;

        // the default verbosity level (0 only errors, 1 errors and warnings, 2 log, warnings and errors)
        int verbosityLevel = 1;

        // thread number
        int maxNumberOfThread = 1;

        // the path to the default json file containing the protection patterns
        String pathOfProtectionPatternsJSON = "/json/protectionATADPatterns.json";

        // ===== INPUT ARGUMENTS PARSING =====

        // the list of options/parameters this program can take
        Options options = new Options();

        Option apks = new Option("p", "pathOfAPKs", true, "path to the directory containing the APK files");
        apks.setRequired(true);
        options.addOption(apks);

        Option patterns = new Option("j", "pathOfJSON", true, "path of JSON file containing " +
                "the protection patterns to detect. Don't specify if you want to use the default one");
        patterns.setRequired(false);
        options.addOption(patterns);

        Option dex2jarPath = new Option("d", "pathOfD2J", true, "path of the .sh dex2jar file. " +
                "Don't specify if you want to use the default one");
        dex2jarPath.setRequired(false);
        dex2jarPath.setRequired(false);
        options.addOption(dex2jarPath);

        Option verbosity = new Option("v", "verbose", true, "verbose level:\n" +
                "    - 0: output only errors\n" +
                "    - 1  (default): output errors and warnings\n" +
                "    - 2  output log, warnings and errors\n" +
                "    - 3  output verbose log, warnings and errors");
        verbosity.setRequired(false);
        options.addOption(verbosity);

        Option threadsOption = new Option("t", "threadsNumber", true, "Maximum number of threads " +
                "ATADetector can spawn. Default is 8");
        threadsOption.setRequired(false);
        options.addOption(threadsOption);

        Option onlyMainPackageOption = new Option("o", "onlyMainPackage", true, "Set to \"true\" if have to search" +
                "for JAVA patterns only in the application main package (retrieved from manifest). Not a good idea except when" +
                " doing training and tuning");
        onlyMainPackageOption.setRequired(false);
        options.addOption(onlyMainPackageOption);

        // get the parser
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        // try to parse the arguments
        try {
            cmd = parser.parse(options, args);
        }
        // if the user omitted the path of the apk files or provided a wrong parameter
        catch (ParseException e) {

            // log error
            ATADLogger.logError(className, mainMethodName, "wrong usage or arguments: " + options, null);

            // exit with status 1
            exit(1);
        }

        // get the mandatory parameters
        // get the path of the apks directory
        pathOfAPKS = cmd.getOptionValue("pathOfAPKs");

        // get the optional parameters
        // get the custom json
        String tempJson = cmd.getOptionValue("pathOfJSON");

        // if the user provided a custom json
        if (tempJson != null) {

            // assign the value to the proper variable
            pathOfProtectionPatternsJSON = tempJson;
        }

        // get the custom dex2jar
        String tempd2j = cmd.getOptionValue("pathOfD2J");

        // if the user provided a custom json
        if (tempd2j != null) {

            // assign the value to the proper variable
            ATADetector.dex2jarPath = tempd2j;
        }

        // get the verbosity level
        String tempVerbosity = cmd.getOptionValue("verbose");

        // if the user provided a custom json
        if (tempVerbosity != null) {

            // try to cast the value to an int
            try {

                // cast from string to int
                verbosityLevel = Integer.valueOf(tempVerbosity);

            }
            // catch an eventual exception
            catch (NumberFormatException e) {

                // log error
                ATADLogger.logError(className, mainMethodName, "error in parsing argument verbosity", e);

                // exit with status 1
                exit(1);
            }

        }

        // assign the verbosity level to the logger
        ATADLogger.verbosityLevel = verbosityLevel;

        // get the threads number level
        String tempThreadNumber = cmd.getOptionValue("threadsNumber");

        // if the user wants to set a specific thread number
        if (tempThreadNumber != null) {

            // try to cast the value to an int
            try {

                // cast from string to int
                maxNumberOfThread = Integer.valueOf(tempThreadNumber);

                // if the number is less than 0
                if (maxNumberOfThread < 1) {

                    // set it to one
                    maxNumberOfThread = 1;
                }

            }
            // catch an eventual exception
            catch (NumberFormatException e) {

                // log error
                ATADLogger.logError(className, mainMethodName, "error in parsing argument thread number", e);

                // exit with status 1
                exit(1);
            }
        }

        // get the only main package option
        String onlyMainPackageString = cmd.getOptionValue("onlyMainPackage");

        // if the option was given
        if ("true".equals(onlyMainPackageString)) {

            // set the value to true
            onlyMainPackage = true;

            // log info
            ATADLogger.logInfo(className, mainMethodName, "will search only in main package (testing mode)");
        }


        // last thing to setup: create the results folder
        // delete it if there is an old one
        /*if (resultsFolder.exists()) {

            if (!FileUtil.deleteDirectory(resultsFolder)) {

                // log the error and exit
                ATADLogger.logError(className, mainMethodName, "error while deleting old results" +
                        " folder =>" + resultsFolder.getAbsolutePath() + ". Exiting...", null);
                exit(1);
            }
        }*/

        if (!resultsFolder.exists()) {

            // create it. If we were not able to create it
            if (!resultsFolder.mkdir()) {

                // log the error and exit
                ATADLogger.logError(className, mainMethodName, "error while creating the results" +
                        " folder =>" + resultsFolder.getAbsolutePath() + ". Exiting...", null);
                exit(1);
            }
        }





        // ===== READ JSON FILE OF PROTECTION PATTERNS =====
        // assign the json object
        protectionATADPatterns = new JSONObject(
                new Scanner(MainAPKLooper.class.getResourceAsStream(pathOfProtectionPatternsJSON),
                "UTF-8").useDelimiter("\\A").next()
        );

        // copy the original JSON file for the final report
        finalReport = new JSONObject(protectionATADPatterns.toString());

        // log info
        ATADLogger.logInfo(className, mainMethodName, "json file successfully loaded");


        // ===== READ FILES IN APK DIRECTORY =====
        // instantiate the file (actually the folder) given the path
        final File apkDirectoryPath = new File(pathOfAPKS);

        // the set that will contain the apk files
        HashSet<File> apkFiles = new HashSet<>();

        // if the given path reflects to an existing directory
        if (apkDirectoryPath.exists() && apkDirectoryPath.isDirectory()) {

            // log info
            ATADLogger.logInfo(className, mainMethodName, "reading apk files from " + pathOfAPKS + "...");

            // get the apk files
            FileUtil.getAllFilesWithGivenExtension(apkDirectoryPath, apkFiles, "apk");
        }
        else {

            // log error
            ATADLogger.logError(className, mainMethodName, "given apk directory either doesn't exists or is not a directory", null);

            // exit with status 3
            exit(3);

        }

        // if the search returned zero files
        if (apkFiles.size() == 0) {

            // log error
            ATADLogger.logError(className, mainMethodName, "didn't found any APK file in " + pathOfAPKS + ". Exiting...", null);

            // exit with status 4
            exit(4);

        }

        // get the number of apks found
        numberOfFoundAPKs = apkFiles.size();

        // log info
        ATADLogger.logInfo(className, mainMethodName, "apk files successfully loaded, " +
                "there " + ( (apkFiles.size() == 1) ? "is " : "are " ) + numberOfFoundAPKs + " apk files to analyze");


        // the number of threads that finished
        successfulFinishedJobs = 0;
        errorFinishedJobs = 0;


        // ===== FOR EACH APK, START THE ANALYSIS =====
        // creating a threadPool executor, fixing the maximum amount of threads (maxNumberOfThread)
        ThreadPoolExecutor apkAnalyzerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxNumberOfThread);

        // for each apk file
        for (File apkFile : apkFiles) {

            // create the thread
            ATADetector ataDetector = new ATADetector(apkFile);

            // submit a thread to the pool
            apkAnalyzerExecutor.submit(ataDetector);
        }

        // until all threads haven't finished
        while ((successfulFinishedJobs + errorFinishedJobs) < numberOfFoundAPKs) {

            try {

                // wait, polling each 5 seconds
                Thread.sleep(5000);
            }

            // if there was an interrupt
            catch (InterruptedException e) {

                // log error
                ATADLogger.logError(className, mainMethodName, "exception while waiting for threads to finish...", null);
            }
        }


        // save the final report
        boolean saveJSONFileResult = false;
        try {
            saveJSONFileResult = FileUtil.saveFileOnFileSystem(
                    resultsFolder.getAbsolutePath() + "/finalReport.json", finalReport.toString(2).getBytes(), 1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // if there was an error while saving the JSON
        if (!saveJSONFileResult) {

            // log error
            ATADLogger.logError(className, mainMethodName, "error while saving final report JSON file...", null);
        }


        // last, create the short versions of the JSON reports
        JSONObject shortVersionReport = createShortVersionsOfJSONReports(finalReport);

        // save the short final report
        boolean saveJSONShortFileResult = false;
        try {
            saveJSONShortFileResult = FileUtil.saveFileOnFileSystem(
                    resultsFolder.getAbsolutePath() + "/finalReport_short.json", shortVersionReport.toString(2).getBytes(), 1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // if there was an error while saving the JSON
        if (!saveJSONShortFileResult) {

            // log error
            ATADLogger.logError(className, mainMethodName, "error while saving final short report JSON file...", null);
        }


        // save the errors file
        boolean saveErrorsFileResult = false;
        try {
            saveErrorsFileResult = FileUtil.saveFileOnFileSystem(
                    resultsFolder.getAbsolutePath() + "/errors.txt", errors.getBytes(), 1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        // if there was an error while saving the errors file
        if (!saveErrorsFileResult) {

            // log error
            ATADLogger.logError(className, mainMethodName, "error while saving errors file...", null);
        }


        // get the time
        long endTime = new Date().getTime();

        // log info
        ATADLogger.directlyLog("End of execution. There " + ( (apkFiles.size() == 1) ? "was" : "were" ) + ": \n" +
                "- " + numberOfFoundAPKs + " apks to analyze\n" +
                "- " + successfulFinishedJobs + " apks successfully analyzed\n" +
                "- " + errorFinishedJobs + " apks not analyzed because of errors\n\n" +
                "The analysis started at: " + new Date(initTime) +"\n" +
                "and finished  at time  : " + new Date(endTime) + "\n" +
                "(time elapsed in milliseconds = " + (endTime - initTime) + ")\n\n" +
                "You can find in the apks folder the final json report.\n" +
                "Thank you!");

        // exit with status 0
        exit(0);
    }


    /**
     * extrapolate the protection occurrences from the long version of the final report
     * @param finalReport  the long version of the report
     * @return the short version of the report
     */
    private static JSONObject createShortVersionsOfJSONReports(JSONObject finalReport) {

        // the short version
        JSONObject shortVersionOfReport = new JSONObject();

        // get the array of categories
        JSONArray categories = finalReport.getJSONArray(FileUtil.protectionCategory);

        // now, for each protection category (e.g. anti-tampering, anti-debugging, ...)
        for (int i = 0; i < categories.length(); i++) {

            // get the protection category object to later retrieve the values of the
            // protection category occurrences
            JSONObject apkReportCategoriesProtectionCategory = categories.getJSONObject(i);

            // get the list of protections
            JSONArray apkReportProtections = apkReportCategoriesProtectionCategory.getJSONArray(FileUtil.protection);

            // now, for each different kind of protection
            for (int j = 0; j < apkReportProtections.length(); j++) {

                // get the protection pattern object to later retrieve the java and native protection occurrences
                JSONObject apkReportProtection = apkReportProtections.getJSONObject(j);

                // get the protection name
                String protectionName = apkReportProtection.getJSONArray(FileUtil.nameOfTheProtection).getString(0);

                // get the java and native level protection occurrences
                int javaProtectionOccurrencesWithLibraries = apkReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getInt(1);
                int javaProtectionOccurrencesWithoutLibraries = apkReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getInt(2);
                int nativeProtectionOccurrences     = apkReportProtection.getJSONArray(FileUtil.nativeLevelPatterns).getInt(1);

                // add the information about the protection and its occurrences
                shortVersionOfReport.put(protectionName + "_JAVA_WITH_LIBRARIES", javaProtectionOccurrencesWithLibraries);
                shortVersionOfReport.put(protectionName + "_JAVA_WITHOUT_LIBRARIES", javaProtectionOccurrencesWithoutLibraries);
                shortVersionOfReport.put(protectionName + "_NATIVE", nativeProtectionOccurrences);
            }
        }

        // return
        return shortVersionOfReport;
    }


    /**
     * THIS is the function invoked by the instance of ATADetector when an error occurred
     * @param apkName the name of the APK the ATADetector instance was working on
     * @param errorDescription a description of the error
     */
    public static synchronized void notifyError(String apkName, String errorDescription) {

        // add the error to the string
        errors = errors + apkName + " : " + errorDescription + "\n";

        // increment the number of jobs finished with errors
        errorFinishedJobs++;

        // log the number of applications analyzed
        ATADLogger.logError(className, sumLongVersionOfJSONReports, "Analysis on app " + apkName + " exited because" +
                "on an error: " + errorDescription + "\n" +
                "apps analyzed: " + (successfulFinishedJobs + errorFinishedJobs) + "/" + numberOfFoundAPKs, null);
    }

    /**
     * THIS is the function invoked by the instance of ATADetector when everything was fine
     * It takes the json report and just sums all the values (occurrences and protections found)
     */
    static synchronized void sumLongVersionOfJSONReports(JSONObject report) {

        // get the array of categories
        JSONArray apkReportCategories   = report.getJSONArray(FileUtil.protectionCategory);
        JSONArray finalReportCategories = finalReport.getJSONArray(FileUtil.protectionCategory);

        // now, for each protection category (e.g. anti-tampering, anti-debugging, ...)
        for (int i = 0; i < finalReportCategories.length(); i++) {

            // get the protection category object to later retrieve the values of the
            // protection category (name, list of protections, ...)
            JSONObject apkReportCategoriesProtectionCategory    = apkReportCategories.getJSONObject(i);
            JSONObject finalReportCategoriesProtectionCategory  = finalReportCategories.getJSONObject(i);

            // get the list of protections
            JSONArray apkReportProtections   = apkReportCategoriesProtectionCategory.getJSONArray(FileUtil.protection);
            JSONArray finalReportProtections = finalReportCategoriesProtectionCategory.getJSONArray(FileUtil.protection);

            // now, for each different kind of protection
            for (int j = 0; j < finalReportProtections.length(); j++) {

                // get the protection pattern object to later retrieve the values
                // (name, java/native level protection patterns, ...)
                JSONObject apkReportProtection              = apkReportProtections.getJSONObject(j);
                JSONObject finalReportProtection            = finalReportProtections.getJSONObject(j);

                // get the java and native level patterns
                JSONObject apkReportJavaLevelPatterns       = apkReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getJSONObject(0);
                JSONObject apkReportNativeLevelPatterns     = apkReportProtection.getJSONArray(FileUtil.nativeLevelPatterns).getJSONObject(0);
                JSONObject finalReportJavaLevelPatterns     = finalReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getJSONObject(0);
                JSONObject finalReportNativeLevelPatterns   = finalReportProtection.getJSONArray(FileUtil.nativeLevelPatterns).getJSONObject(0);

                // sum the protection occurrences
                finalReportProtection.getJSONArray(FileUtil.javaLevelPatterns).put(1,
                        finalReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getInt(1) +
                              apkReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getInt(1));

                finalReportProtection.getJSONArray(FileUtil.javaLevelPatterns).put(2,
                        finalReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getInt(2) +
                                apkReportProtection.getJSONArray(FileUtil.javaLevelPatterns).getInt(2));

                finalReportProtection.getJSONArray(FileUtil.nativeLevelPatterns).put(1,
                        finalReportProtection.getJSONArray(FileUtil.nativeLevelPatterns).getInt(1) +
                                apkReportProtection.getJSONArray(FileUtil.nativeLevelPatterns).getInt(1));

                // get the patterns for the classes, methods, attributes and strings
                JSONArray apkReportPatternClasses           = apkReportJavaLevelPatterns.getJSONArray(FileUtil.classes);
                JSONArray apkReportPatternMethods           = apkReportJavaLevelPatterns.getJSONArray(FileUtil.methods);
                JSONArray apkReportPatternAttributes        = apkReportJavaLevelPatterns.getJSONArray(FileUtil.attributes);
                JSONArray apkReportPatternStrings           = apkReportJavaLevelPatterns.getJSONArray(FileUtil.strings);
                JSONArray finalReportPatternClasses         = finalReportJavaLevelPatterns.getJSONArray(FileUtil.classes);
                JSONArray finalReportPatternMethods         = finalReportJavaLevelPatterns.getJSONArray(FileUtil.methods);
                JSONArray finalReportPatternAttributes      = finalReportJavaLevelPatterns.getJSONArray(FileUtil.attributes);
                JSONArray finalReportPatternStrings         = finalReportJavaLevelPatterns.getJSONArray(FileUtil.strings);

                // get the patterns for the imported symbols and strings
                JSONArray apkReportPatternNativeSymbols     = apkReportNativeLevelPatterns.getJSONArray(FileUtil.importedSymbols);
                JSONArray apkReportPatternNativeStrings     = apkReportNativeLevelPatterns.getJSONArray(FileUtil.strings);
                JSONArray finalReportPatternNativeSymbols   = finalReportNativeLevelPatterns.getJSONArray(FileUtil.importedSymbols);
                JSONArray finalReportPatternNativeStrings   = finalReportNativeLevelPatterns.getJSONArray(FileUtil.strings);


                // sum the count
                finalReportProtection.getJSONArray(FileUtil.nameOfTheProtection).put(1,
                        finalReportProtection.getJSONArray(FileUtil.nameOfTheProtection).getInt(1) +
                                apkReportProtection.getJSONArray(FileUtil.nameOfTheProtection).getInt(1));

                // sum the count
                finalReportProtection.getJSONArray(FileUtil.nameOfTheProtection).put(2,
                        finalReportProtection.getJSONArray(FileUtil.nameOfTheProtection).getInt(2) +
                                apkReportProtection.getJSONArray(FileUtil.nameOfTheProtection).getInt(2));

                // for each class in the pattern
                for (int k = 0; k < finalReportPatternClasses.length(); k++) {

                    // the current pattern (<string for the pattern, number of occurrences found>)
                    JSONArray currentPattern = finalReportPatternClasses.getJSONArray(k);

                    // sum the found values
                    currentPattern.put(1, currentPattern.getInt(1) + apkReportPatternClasses.getJSONArray(k).getInt(1));
                    currentPattern.put(2, currentPattern.getInt(2) + apkReportPatternClasses.getJSONArray(k).getInt(2));
                }

                // for each method in the pattern
                for (int k = 0; k < finalReportPatternMethods.length(); k++) {

                    // the current pattern (<string for the pattern, number of occurrences found>)
                    JSONArray currentPattern = finalReportPatternMethods.getJSONArray(k);

                    // sum the found values
                    currentPattern.put(1, currentPattern.getInt(1) + apkReportPatternMethods.getJSONArray(k).getInt(1));
                    currentPattern.put(2, currentPattern.getInt(2) + apkReportPatternMethods.getJSONArray(k).getInt(2));
                }

                // for each attribute in the pattern
                for (int k = 0; k < finalReportPatternAttributes.length(); k++) {

                    // the current pattern (<string for the pattern, number of occurrences found>)
                    JSONArray currentPattern = finalReportPatternAttributes.getJSONArray(k);

                    // sum the found values
                    currentPattern.put(1, currentPattern.getInt(1) + apkReportPatternAttributes.getJSONArray(k).getInt(1));
                    currentPattern.put(2, currentPattern.getInt(2) + apkReportPatternAttributes.getJSONArray(k).getInt(2));
                }

                // for each string in the pattern
                for (int k = 0; k < finalReportPatternStrings.length(); k++) {

                    // the current pattern (<string for the pattern, number of occurrences found>)
                    JSONArray currentPattern = finalReportPatternStrings.getJSONArray(k);

                    // sum the found values
                    currentPattern.put(1, currentPattern.getInt(1) + apkReportPatternStrings.getJSONArray(k).getInt(1));
                    currentPattern.put(2, currentPattern.getInt(2) + apkReportPatternStrings.getJSONArray(k).getInt(2));
                }

                // for each symbol in the pattern
                for (int k = 0; k < finalReportPatternNativeSymbols.length(); k++) {

                    // the current pattern (<string for the pattern, number of occurrences found>)
                    JSONArray currentPattern = finalReportPatternNativeSymbols.getJSONArray(k);

                    // sum the found values
                    currentPattern.put(1, currentPattern.getInt(1) + apkReportPatternNativeSymbols.getJSONArray(k).getInt(1));
                }

                // for each native string in the pattern
                for (int k = 0; k < finalReportPatternNativeStrings.length(); k++) {

                    // the current pattern (<string for the pattern, number of occurrences found>)
                    JSONArray currentPattern = finalReportPatternNativeStrings.getJSONArray(k);

                    // the currentValue
                    int currentValue = currentPattern.getInt(1);

                    // sum the found values
                    currentPattern.put(1, currentValue + apkReportPatternNativeStrings.getJSONArray(k).getInt(1));
                }
            }
        }

        // increment the number of finished jobs
        successfulFinishedJobs++;

        // log the number of applications analyzed
        ATADLogger.directlyLog("Successfully completed another analysis." +
                "apps analyzed: " + (successfulFinishedJobs + errorFinishedJobs) + "/" + numberOfFoundAPKs);
    }
}