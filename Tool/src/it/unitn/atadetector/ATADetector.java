package it.unitn.atadetector;


import it.unitn.atadetector.javalevel.ATADJavaLevel;
import it.unitn.atadetector.nativelevel.ATADNativeLevel;
import it.unitn.atadetector.util.ATADLogger;
import it.unitn.atadetector.util.FileUtil;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static it.unitn.atadetector.MainAPKLooper.resultsFolder;
import static it.unitn.atadetector.util.JSONSolver.applyFormulasOnJSON;


/**
 * This class, given an APK file, will analyze its AT-AD defences by invoking the java and native level ATADs
 * The main purpose of this class is to performs checks on the apk file and to extract both the .clas files from the dex
 * archive and the "lib" folder containing the .so binaries
 */
public class ATADetector extends Thread {

    // variables for logging
    private static final String className = "ATADetector";
    private static final String analyzeApplicationMethodName = "analyzeApplication";
    private static final String aTADetectorMethodName = "ATADetector";
    private static final String runMethodName = "run";

    // dex2jar variables
    static  String dex2jarPath = "resources/dex2jar-2.0/d2j-dex2jar.sh";
    //static  String dex2jarPath = "/kore-ns-groups/st/sberlato/ATADetector//resources/lib/dex2jar-nightly-2.1/d2j-dex2jar.sh";
    private static String dex2jarAbsPath = null;

    // filters for libraries to avoid during the extraction of JAVA classes from the JAR
    private static String[] javaClassFiltersForLibraries = new String[] {};

    // the apk file this class has to analyzeSoLibraries
    private File apkFile = null;


    /**
     * The constructor performs some checks on the validity of the given file
     * Then, it assigns the apk file variable
     * @param apkFile the apk file
     */
    public ATADetector(File apkFile){

        // if the file is not null
        if (apkFile != null) {

            // if the file exists and it is not a directory
            if (apkFile.exists() && apkFile.isFile()) {

                // if the file extension is "apk"
                if (FileUtil.getFileNameAndExtension(apkFile.getName())[1].endsWith("apk")) {

                    // assign the file
                    this.apkFile = apkFile;

                    // log info
                    ATADLogger.logInfo(className, aTADetectorMethodName, "created a new ATADetector for " + this.apkFile.getName() + " file\"");
                }
            }
        }

        // if some of the checks above failed
        if (this.apkFile == null) {

            // log info
            ATADLogger.logInfo(className, aTADetectorMethodName, "given invalid apk file in constructor: \" +\n" +
                    "                                 ((apkFile == null) ? \"file is null\" : apkFile.getPath())");
        }
    }


    /**
     * This method is responsible for initializing the analysis: its main task is to extract the
     * java code and the "lib" folder (containing the ".so" files) and to instantiate the Java and
     * Native level analyzers. Eventually it will collect the response and return it to the Main
     */
    private void analyzeApplication() {

        // ===== EXTRACT FILES FROM APK AND THEN INVOKE THE JAVA/NATIVE LEVEL ANALYZERS=====
        // extract java code
        // - step 1 -> dex2jar on apk file
        // - step 2 -> put jar in temporary directory
        // - step 3 -> extract class files from java
        //
        // extract native code
        // - step 1 -> unzip apk using ZipFile
        // - step 2 -> put lib folder in temporary directory
        //
        // invoke ATADJavaLevel and ATADNativeLevel


        // get the path of the apk and the name
        String apkNameWithoutExtension = FileUtil.getFileNameAndExtension(apkFile.getName())[0];

        // the directory where to store temporary files
        File temporaryDirectory = new File(resultsFolder.getAbsolutePath() + "/" + apkNameWithoutExtension);

        // if the directory exists already, clean it
        if (temporaryDirectory.exists()) {

            if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                // log error
                ATADLogger.logError(className, analyzeApplicationMethodName, "was not able to " +
                        "delete old temporary directory for " + apkFile.getName() +  "... Exiting", null);

                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "was not able to " +
                        "delete old temporary directory for " + apkFile.getName() +  "... Exiting");

                // return to exit the thread
                return;

            }
        }

        // if we didn't manage to create the directory
        if (!temporaryDirectory.mkdir()) {

            // log error
            ATADLogger.logError(className, analyzeApplicationMethodName, "error in creating temporary directory for storing JAR file" +
                    " for " + apkFile.getName() +  "... Exiting", null);

            // notify the error to the looper class
            MainAPKLooper.notifyError(apkFile.getName(), "error in creating temporary directory for storing JAR file");

            // return to exit the thread
            return;

        }


        // ===== this code was added just for launching the algorithm in a HPC. Thus you may want to delete it ====
        // log info
        ATADLogger.logInfo(className, analyzeApplicationMethodName, "copying the apk from " +
                apkFile.getAbsolutePath() +  " to temporary folder");
        // try to copy the apk to local folder
        try {

            // create a process builder to run the dex2jar program
            ProcessBuilder pb = new ProcessBuilder("cp",
                    apkFile.getAbsolutePath(), temporaryDirectory.getAbsolutePath() + "/" + apkFile.getName());

            // set the working directory to be the temporary one
            // pb.directory(temporaryDirectory);

            // redirect output to log file
            pb.redirectErrorStream(true);

            // start the process
            Process p = pb.start();

            p.waitFor();

            apkFile = new File(temporaryDirectory.getAbsolutePath() + "/" + apkFile.getName());
        }
        // catch
        catch (IOException | InterruptedException e) {

            // notify error and return
            MainAPKLooper.notifyError(apkFile.getName(), "not able to copy to temporary folder: " +
                    FileUtil.getStringFromStackTrace(e));
            return;
        }
        // catch

        // ===== end of code =====

        // log info
        ATADLogger.logInfo(className, analyzeApplicationMethodName, "start extracting java code from " + apkFile.getName() +  "...");

        // the apk absolute path
        String apkAbsolutePath = apkFile.getAbsolutePath();


        // the directory where to store class files
        File classFilesDirectory = new File(temporaryDirectory.getAbsolutePath() + "/classFiles/");

        // the directory where to store .so libraries
        File soLibrariesDirectory = new File(temporaryDirectory.getAbsolutePath() + "/soLibraries/");

        // the path to the jar file that dex2jar will create
        String jarFilePath = temporaryDirectory.getAbsolutePath() + "/" + apkNameWithoutExtension + "-dex2jar.jar";


        // ===== EXTRACT JAR FROM APK =====

        // ===== step 1 -> dex2jar on apk file
        try {

            // log info
            ATADLogger.logInfo(className, analyzeApplicationMethodName, "invoking dex2jar on " + apkFile.getName() +  "...");

            // if not already done by another thread, get the absolute path of the .sh dex2jar file
            if (dex2jarAbsPath == null) {

                // create the file
                File dex2jarFile = new File(dex2jarPath);

                // get the absolute path
                dex2jarAbsPath = dex2jarFile.getAbsolutePath();
            }

            // create a process builder to run the dex2jar program
            ProcessBuilder pb = new ProcessBuilder(dex2jarAbsPath, apkAbsolutePath);

            // set the working directory to be the temporary one
            pb.directory(temporaryDirectory);

            // redirect output to log file
            pb.redirectErrorStream(true);

            // start the process
            Process p = pb.start();

            // here we must NOT wait for the process to finish through "p.waitFor();"
            // because the output of the process ("dex2jar") could be too big for the buffer size
            // provided by the underlying platform. So we "drain" the buffer instead, that in
            // the end has the same effect of p.waitFor();

            // get a reader for the inputStream
            BufferedReader d2jLogReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // the string that will contain Dex2Jar log and a temporary string
            StringBuilder output = new StringBuilder();
            String temp;

            // while there is output to parse
            while ( ( temp = d2jLogReader.readLine()) != null) {

                // add it to the output
                output.append(" - ").append(temp);
            }

            // if the output contains the word "Exception", it means that something went wrong
            if (output.toString().contains("Exception")) {

                // log error
                ATADLogger.logError(className, analyzeApplicationMethodName, "dex2jar returned an exception. " +
                        "It is not possible to proceed with " + apkFile.getName() + ". Exiting...", null);

                if (!FileUtil.deleteDirectory(temporaryDirectory)) {
                    // notify the error to the looper class
                    MainAPKLooper.notifyError(apkFile.getName(), "dex2jar returned an exception: " + output
                    + ". Moreover, we were not able to clean the temporary directory");
                }
                else {
                    // notify the error to the looper class
                    MainAPKLooper.notifyError(apkFile.getName(), "dex2jar returned an exception: " + output);
                }

                // return to exit the thread
                return;
            }

            // now look for an eventual error.zip file
            File errorFile = new File(temporaryDirectory + "/" + apkNameWithoutExtension + "-error.zip");

            // if the file exists
            if (errorFile.exists()) {

                ATADLogger.logWarning(className, analyzeApplicationMethodName, "dex2jar had some errors " +
                        "while working on " + apkFile.getName() + ". Anyway we can try to proceed and go on with the analysis");
            }
        }
        // catch any exception
        catch (IOException e) {

            // log error
            ATADLogger.logError(className, analyzeApplicationMethodName, "error in invoking dex2jar for " + apkFile.getName(), e);

            if (!FileUtil.deleteDirectory(temporaryDirectory)) {
                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error in invoking dex2jar"
                        + ". Moreover, we were not able to clean the temporary directory");
            }
            else {
                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error in invoking dex2jar");
            }

            // return to exit the thread
            return;

        }

        // ===== step 2 -> put jar in temporary directory
        // now we want to extract the .class files from the jar
        // if the directory doesn't exists already
        if (!classFilesDirectory.exists()) {

            // if we didn't manage to create the directory
            if (!classFilesDirectory.mkdir()) {

                // log info
                ATADLogger.logError(className, analyzeApplicationMethodName, "error in creating temporary directory for storing .class files " +
                        "for " + apkFile.getName() +  "... Exiting", null);


                if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                    // notify the error to the looper class
                    MainAPKLooper.notifyError(apkFile.getName(), "error in creating temporary directory " +
                            "for storing .class files" + ". Moreover, we were not able to clean the temporary directory");
                }
                else {
                    // notify the error to the looper class
                    MainAPKLooper.notifyError(apkFile.getName(), "error in creating temporary directory " +
                            "for storing .class files");
                }

                // return to exit the thread
                return;
            }
        }

        // ===== step 3 -> extract class files from java
        // extract the class files. If there was an error
        if (!FileUtil.extractClassFilesFromJAR(jarFilePath, classFilesDirectory.getAbsolutePath(), javaClassFiltersForLibraries)) {

            // log info
            ATADLogger.logError(className, analyzeApplicationMethodName, "error while extracting class files from JAR " +
                    "for " + apkFile.getName() +  "... Exiting", null);

            if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while extracting class files from JAR" +
                        ". Moreover, we were not able to clean the temporary directory");
            }
            else {
                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while extracting class files from JAR");
            }

            // return to exit the thread
            return;

        }
        // if we are here, it means that everything is fine and we can move on to the lib extraction




        // ===== EXTRACT LIB FOLDER =====

        // now we want to extract the .so libraries from the APK
        // if the directory doesn't exists already
        if (!soLibrariesDirectory.exists()) {

            // if we didn't manage to create the directory
            if (!soLibrariesDirectory.mkdir()) {

                // log info
                ATADLogger.logError(className, analyzeApplicationMethodName, "error in creating temporary directory for storing .so libraries " +
                        "for " + apkFile.getName() +  "... Exiting", null);


                if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                    // notify the error to the looper class
                    MainAPKLooper.notifyError(apkFile.getName(), "error in creating temporary directory for storing .so libraries" +
                    ". Moreover, we were not able to clean the temporary directory");
                }
                else {
                    // notify the error to the looper class
                    MainAPKLooper.notifyError(apkFile.getName(), "error in creating temporary directory for storing .so libraries");

                }

                // return to exit the thread
                return;
            }
        }

        // if the operation of extraction was not successful
        if (!FileUtil.extractSOLibrariesFromAPK(apkFile, soLibrariesDirectory)) {

            // log error
            ATADLogger.logError(className, analyzeApplicationMethodName, "error while extracting .so libraries from APK " +
                    apkFile.getName() +  "... Exiting", null);

            if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while extracting .so libraries from APK" +
                        ". Moreover, we were not able to clean the temporary directory");
            }
            else {
                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while extracting .so libraries from APK");
            }

            // return to exit the thread
            return;
        }



        // ===== INVOKING ATADJavaLevel and ATADNativeLevel =====

        // copy the original JSON file
        // the json report for the APK analyzed by this ATADetector instance
        JSONObject reportForThisAPK = new JSONObject(MainAPKLooper.protectionATADPatterns.toString());

        // substitute the string "substituteWithTheApplicationPackage" in the JSON with the application package name
        String apkPackageName = apkNameWithoutExtension; // cannot use because cluster does not have aapt FileUtil.getApplicationPackageFromManifest(apkFile);

        // if everything was ok
        if (apkPackageName != null) {

            // invoke the function to substitute the strings
            FileUtil.substitutePackageNameInJSON(reportForThisAPK, apkPackageName);

        }
        else {

            // log warning
            ATADLogger.logWarning(className, analyzeApplicationMethodName, "Not able get package name " +
                    "from manifest. Some detections may be imprecise (the ones regarding the BuildConfig class");

        }

        // log info
        ATADLogger.logInfo(className, analyzeApplicationMethodName, "Instantiating ATADJavaLevel and ATADNativeLevel");

        // instantiating ATADJavaLevel
        ATADJavaLevel atadJavaLevel = new ATADJavaLevel(classFilesDirectory);

        // if the research is to be narrowed to the main package only
        if (MainAPKLooper.onlyMainPackage) {

            // set the package name to the application
            atadJavaLevel.packageName = apkPackageName;

        }

        // start the analysis and get the JSON report for Java level.
        boolean everythingOKJava = atadJavaLevel.analyzeJavaClasses(reportForThisAPK);

        // if there was an error during the java analysis
        if (!everythingOKJava) {

            if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while analyzeJavaClasses" +
                        ". Moreover, we were not able to clean the temporary directory");
            }
            else {
                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while analyzeJavaClasses");
            }

            // just return. The ATADJavaLevel class is responsible for sending the error message to the looper
            return;
        }

        // instantiating ATADNativeLevel
        ATADNativeLevel atadNativeLevel = new ATADNativeLevel(soLibrariesDirectory);

        // start the analysis and get the JSON report for Native level
        boolean everythingOKNative = atadNativeLevel.analyzeSoLibraries(reportForThisAPK);

        // if there was an error during the native analysis
        if (!everythingOKNative) {

            if (!FileUtil.deleteDirectory(temporaryDirectory)) {

                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while analyzeSoLibraries" +
                        ". Moreover, we were not able to clean the temporary directory");
            }
            else {
                // notify the error to the looper class
                MainAPKLooper.notifyError(apkFile.getName(), "error while analyzeSoLibraries");
            }

            // just return. The ATADNativeLevel class is responsible for sending the error message to the looper
            return;
        }

        // based on the thresholds, decide whether a protection is present or not
        JSONObject shortJSONReportVersion = applyFormulasOnJSON(reportForThisAPK);

        // save the JSON file for this report
        boolean saveResultLongVersion = FileUtil.saveFileOnFileSystem(
                resultsFolder.getAbsolutePath() + "/" + apkNameWithoutExtension + ".json",
                reportForThisAPK.toString(2).getBytes(), 1);
        boolean saveResultShortVersion = FileUtil.saveFileOnFileSystem(
                resultsFolder.getAbsolutePath() + "/" + apkNameWithoutExtension + "_short.json",
                shortJSONReportVersion.toString(2).getBytes(), 1);

        // if there was an error while saving the JSONs
        if (!saveResultLongVersion || !saveResultShortVersion) {

            // log error
            ATADLogger.logError(className, analyzeApplicationMethodName, "error while saving JSON file", null);

            // don't return error, because info in JSON are correct anyway
        }

        // give the report of this apk to the looper class
        MainAPKLooper.sumLongVersionOfJSONReports(reportForThisAPK);

        if (!FileUtil.deleteDirectory(temporaryDirectory)) {

            ATADLogger.logError(className, analyzeApplicationMethodName,
                    "not able to clean temporary directory", null);
        }
    }


    /**
     *  Start this instance of ATADetector as a new thread
     *  First, check if the apkFile is valid or not
     */
    @Override
    public void run() {

        // if some of the checks in the constructor failed
        if (this.apkFile == null) {

            // log error
            ATADLogger.logError(className, runMethodName, "running analysis on null apk file (error during initialization)", null);

            // notify the error to the looper class
            MainAPKLooper.notifyError(apkFile.getName(), "running analysis on null apk file (error during initialization)");

        }
        // otherwise
        else  {

            // log directly, too important
            ATADLogger.directlyLog("starting analysis on file " + apkFile.getName() +  "...");

            try {
                // start the analysis
                analyzeApplication();
            }
            // catch any exception (otherwise the thread will go in deadlock)
            catch (Exception e) {

                // log the error
                ATADLogger.logError(className, runMethodName, "exception was thrown", e);

                // notify the error to the main
                MainAPKLooper.notifyError(apkFile.getName(), "an exception was thrown during the execution " +
                        "of the analysis (check logs for more details): " + e.getMessage());
            }
            // catch any error
            catch (Error e) {

                // log the error
                ATADLogger.logError(className, runMethodName, "error was thrown: " + e.getMessage(), null);

                // notify the error to the main
                MainAPKLooper.notifyError(apkFile.getName(), "an error was thrown during the execution " +
                        "of the analysis (check logs for more details): " + e.getMessage());
            }
            // catch any throwable
            catch (Throwable e) {

                // log the error
                ATADLogger.logError(className, runMethodName, "throwable was thrown: " + e.getMessage(), null);

                // notify the error to the main
                MainAPKLooper.notifyError(apkFile.getName(), "a throwable was thrown during the execution " +
                        "of the analysis (check logs for more details): " + e.getMessage());
            }
        }
    }
}