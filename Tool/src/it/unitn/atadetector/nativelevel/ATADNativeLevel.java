package it.unitn.atadetector.nativelevel;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import it.unitn.atadetector.MainAPKLooper;
import it.unitn.atadetector.util.ATADLogger;
import it.unitn.atadetector.util.FileUtil;
import org.json.*;

/**
 * This class is responsible for detecting anti-tampering (AT) and anti-debugging (AD) defences at native layer
 * It will statically extract imported symbols and strings from the given .so files
 * Then, it will use such information to perform a search with the protection patterns
 * Eventually, it will return the likelihood related to whether there are AT/AD protections or not in the .so files
 */
public class ATADNativeLevel {

    // variables for logging
    private static final String className = "ATADNativeLevel";
    private static final String analyzeMethodName = "analyzeSoLibraries";
    private static final String extractSymbolsName = "extractSymbols";
    private static final String extractStringsName = "extractStrings";

    // the directory containing the .so files
    private File soLibrariesDirectory = null;


    /**
     * Constructor
     * @param soLibrariesDirectory the directory containing the .so files
     */
    public ATADNativeLevel(File soLibrariesDirectory) {

        // if it was actually given a file and not a null
        if (soLibrariesDirectory != null) {

            // if the directory really exists
            if (soLibrariesDirectory.exists()) {

                // assigning the value
                this.soLibrariesDirectory = soLibrariesDirectory;
            }
        }
    }


    /**
     * This method loops over the .so files in the 'lib' folder of the given APK
     * after some checks, for each file it searches for given symbols/strings in the library
     * exploiting the 'nm' command
     * @return a JSON object summarizing the results of the analysis
     */
    public boolean analyzeSoLibraries(JSONObject reportForThisAPK) {

        // ===== ANALYZE SO LIBRARIES IN APK =====
        // - step 1 -> get all .so files in the given directory
        // - step 2 -> get all the symbols from each of the .so files
        // - step 3 -> compare the symbols to the JSON patterns


        // if either the pointer to the folder or the JSONObject are null
        if (this.soLibrariesDirectory == null || reportForThisAPK == null) {

            // log error
            ATADLogger.logError(className, analyzeMethodName, "given null or not existing directory for .so " +
                    "files OR null JSON object for reporting the outcome", null);

            // notify the error to the looper class
            MainAPKLooper.notifyError(soLibrariesDirectory.getAbsolutePath(), "given null or not existing directory for .so" +
                    "files OR null JSON object for reporting the outcome");

            // return false to indicate the error
            return false;
        }
        // otherwise, proceed with the analysis
        else {

            // ===== step 1 -> get all .so files in the given directory =====

            // log info
            ATADLogger.logInfo(className, analyzeMethodName, "starting analysis (Native level) in folder " + soLibrariesDirectory.getAbsolutePath() + "...");

            // the set that will hold the files
            HashSet<File> soLibraries = new HashSet<>();

            // get all the .so files
            FileUtil.getAllFilesWithGivenExtension(soLibrariesDirectory, soLibraries, "so");

            // if there are no libraries
            // (it is possible, thus it can also NOT be an error)
            if (soLibraries.isEmpty()) {

                // log warning
                ATADLogger.logWarning(className, analyzeMethodName, "no libraries in in folder " + soLibrariesDirectory.getAbsolutePath());

            }


            // the set that will hold the symbols and the strings
            HashMap<String, ArrayList<String>> symbolsForLibraries= new HashMap<>();
            HashMap<String, ArrayList<String>> stringsForLibraries= new HashMap<>();


            // ===== step 2 -> get all the symbols and strings from each of the .so files =====
            // now, for all .so files
            for (File library : soLibraries) {

                // extract the symbols for all the libraries
                symbolsForLibraries.put(library.getName(), extractSymbols(library));

                if (symbolsForLibraries.get(library.getName()) == null) {

                    // return false to indicate that an error occurred. The "extractSymbols" function will send the error to the looper
                    return false;
                }

                // extract the strings for all the libraries
                stringsForLibraries.put(library.getName(), extractStrings(library));

                if (stringsForLibraries.get(library.getName()) == null) {

                    // return false to indicate that an error occurred. The "extractStrings" function will send the error to the looper
                    return false;
                }
            }

            // ===== step 3 -> compare the symbols to the JSON patterns =====
            // ===== parse the JSON to get the patterns:
            // ===== for each protection category -> for each protection -> for each .so library -> compare symbols with patterns (symbols/strings)

            // get the array of categories
            JSONArray categories = reportForThisAPK.getJSONArray(FileUtil.protectionCategory);

            // now, for each protection category (e.g. anti-tampering, anti-debugging, ...)
            for (int i = 0; i < categories.length(); i++) {

                // get the protection category object to later retrieve the values of the
                // protection category (name, list of protections, ...)
                JSONObject protectionCategory = categories.getJSONObject(i);

                // log info
                ATADLogger.logVerboseInfo(className, analyzeMethodName, "parsing protection patterns " +
                        "of category " + protectionCategory.getString(FileUtil.categoryName) + "...");

                // get the list of protections
                JSONArray protections = protectionCategory.getJSONArray(FileUtil.protection);

                // now, for each different kind of protection
                for (int j = 0; j < protections.length(); j++) {

                    // get the protection pattern object to later retrieve the values
                    // (name, java/native level protection patterns, ...)
                    JSONObject protection = protections.getJSONObject(j);

                    // log info
                    ATADLogger.logVerboseInfo(className, analyzeMethodName, "parsing protection patterns " +
                            "of protection " + protection.getJSONArray(FileUtil.nameOfTheProtection).getString(0) + "...");

                    // get the native level patterns
                    JSONObject nativeLevelPatterns = protection.getJSONArray(FileUtil.nativeLevelPatterns).getJSONObject(0);

                    // get the patterns for the imported symbols
                    JSONArray importedSymbols = nativeLevelPatterns.getJSONArray(FileUtil.importedSymbols);

                    // get the patterns for the strings
                    JSONArray strings = nativeLevelPatterns.getJSONArray(FileUtil.strings);

                    // now, for each library
                    for (File library : soLibraries) {

                        // now, for each symbol retrieved from the library
                        for (String symbol : symbolsForLibraries.get(library.getName())) {

                            // for each symbol in the pattern list
                            for (int k = 0; k < importedSymbols.length(); k++) {

                                // if there is a match
                                if (importedSymbols.getJSONArray(k).getString(0).equals(symbol)) {

                                    // increment the matches found
                                    int matchesFound = importedSymbols.getJSONArray(k).getInt(1);
                                    matchesFound++;
                                    importedSymbols.getJSONArray(k).put(1, matchesFound);
                                }
                            }
                        }

                        // now, for each string retrieved from the library
                        for (String string : stringsForLibraries.get(library.getName())) {

                            // for each string in the pattern list
                            for (int k = 0; k < strings.length(); k++) {

                                // if there is a match
                                if (strings.getJSONArray(k).getString(0).equals(string)) {

                                    // increment the matches found
                                    int matchesFound = strings.getJSONArray(k).getInt(1);
                                    matchesFound++;
                                    strings.getJSONArray(k).put(1, matchesFound);
                                }
                            }
                        }
                    }
                }
            }
        }

        // if we arrived here, return true to indicate that everything went fine
        return true;
    }


    /**
     * given a .so file, this method extracts imported and exported symbols
     * @param sharedObjectFile the .so file
     * @return an array containing the list of extracted symbols from the library
     */
    private ArrayList<String> extractSymbols (File sharedObjectFile) {

        // log info
        ATADLogger.logInfo(className, extractSymbolsName, "extracting symbols for .so library " + sharedObjectFile.getName() + "...");

        // the array that will contain the symbols
        ArrayList<String> symbols;

        // try to get the list of symbols
        try {

            // the array that will contain the symbols
            symbols = new ArrayList<>();

            // create a process builder to run nm
            ProcessBuilder pb = new ProcessBuilder("nm", "-D", "-C", "-P", sharedObjectFile.getName());

            pb.redirectErrorStream(true);

            // set the working directory to be the libraries one
            pb.directory(soLibrariesDirectory);

            // start the process
            Process p = pb.start();

            // here we must NOT wait for the process to finish through "p.waitFor();"
            // because the output of the process ("nm") could be too big for the buffer size
            // provided by the underlying platform. So we "drain" the buffer instead, that in
            // the end has the same effect of p.waitFor();

            // get a reader for the inputStream
            BufferedReader nmLogReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // the string that will contain the temporary line logs
            String output;

            // while there is output to parse
            while ( ( output = nmLogReader.readLine()) != null) {

                // add the symbol to the list
                // in posix2 format, the first element of the line is the symbol
                symbols.add(output.split(" ")[0]);
            }
        }
        // catch IOException (thrown by pb.start)
        catch (IOException e) {

            // log error
            ATADLogger.logError(className, extractSymbolsName, "error while starting nm command", e);

            // notify the error to the looper class
            MainAPKLooper.notifyError(soLibrariesDirectory.getAbsolutePath(), "error while starting nm command");

            // return null to indicate the error
            return null;
        }

        // return the list of symbols
        return symbols;
    }



    /**
     * given a .so file, this method extracts the strings in it
     * @param sharedObjectFile the .so file
     * @return an array containing the list of extracted strings from the library
     */
    private ArrayList<String> extractStrings (File sharedObjectFile) {

        // log info
        ATADLogger.logInfo(className, extractStringsName, "extracting Strings for .so library " + sharedObjectFile.getName() + "...");

        // the array that will contain the strings
        ArrayList<String> strings;

        // try to get the list of strings
        try {

            // the array that will contain the symbols
            strings = new ArrayList<>();

            // create a process builder to run nm
            // why to call "strings" with "-f? Because we noticed that, when finding a null string
            // or one made only of whitespaces, the "stringsLogReader.readLine()" invocation goes in deadlock
            ProcessBuilder pb = new ProcessBuilder("strings", "-f", sharedObjectFile.getName());

            pb.redirectErrorStream(true);

            // set the working directory to be the libraries one
            pb.directory(soLibrariesDirectory);

            // start the process
            Process p = pb.start();

            // here we must NOT wait for the process to finish through "p.waitFor();"
            // because the output of the process ("strings") could be too big for the buffer size
            // provided by the underlying platform. So we "drain" the buffer instead, that in
            // the end has the same effect of p.waitFor();

            // get a reader for the inputStream
            BufferedReader stringsLogReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // the string that will contain the temporary line logs
            String output;

            // while there is output to parse
            while (( output = stringsLogReader.readLine()) != null) {

                // add the string to the list
                // the output is like: "libName: stringFound". so we split on the first occurrence of " "
                // and then take the second element (the actual string)
                // why to call
                strings.add(output.split(" ", 2)[1]);
            }
        }

        // catch IOException (thrown by pb.start)
        catch (IOException e) {

            // log error
            ATADLogger.logError(className, extractStringsName, "error while starting string command", e);

            // notify the error to the looper class
            MainAPKLooper.notifyError(soLibrariesDirectory.getAbsolutePath(), "error while starting string command");

            // return null to indicate the error
            return null;
        }

        // return the list of strings
        return strings;
    }
}
