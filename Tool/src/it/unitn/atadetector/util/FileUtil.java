package it.unitn.atadetector.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Just an utility class for file handling
 */
public class FileUtil {

    // variables for logging
    private static final String className = "FileUtil";
    private static final String getFileNameAndExtensionMethodName = "getFileNameAndExtension";
    private static final String getAllFilesWithGivenExtensionMethodName = "getAllFilesWithGivenExtension";
    private static final String extractClassFilesFromJARMethodName = "extractClassFilesFromJAR";
    private static final String loadJSONFileMethodName = "loadJSONFile";
    private static final String extractSOLibrariesFromAPKMethodName = "extractSOLibrariesFromAPK";
    private static final String saveFileOnFileSystem = "saveFileOnFileSystem";
    private static final String getApplicationPackageFromManifestName = "getApplicationPackageFromManifest";
    private static final String substitutePackageNameInJSONName = "substitutePackageNameInJSON";


    // variables for the JSON file. Simply a list of the nodes in the JSON file
    public static final String protectionCategory = "protectionCategory";
    public static final String categoryName = "categoryName";
    public static final String protection = "protection";
    public static final String nameOfTheProtection = "_nameOfTheProtection";
    public static final String javaLevelPatterns = "javaLevelPatterns";
    public static final String classes = "classes";
    public static final String methods = "methods";
    public static final String attributes = "attributes";
    public static final String strings = "strings";
    public static final String nativeLevelPatterns = "nativeLevelPatterns";
    public static final String importedSymbols = "importedSymbols";
    public static final String substituteWithTheApplicationPackage = "substituteWithTheApplicationPackage";

    public static final String signatureChecking = "SignatureChecking";
    public static final String codeIntegrityChecking = "CodeIntegrityChecking";
    public static final String installerVerification = "InstallerVerification";
    public static final String safetyNetAttestation = "SafetyNetAttestation";
    public static final String emulatorDetection = "EmulatorDetection";
    public static final String dynamicAnalysisFrameworkDetection = "DynamicAnalysisFrameworkDetection";
    public static final String debuggerDetection = "DebuggerDetection";
    public static final String debuggableStatusDetection = "DebuggableStatusDetection";
    public static final String timeChecks = "TimeChecks";
    public static final String alteringDebuggerMemoryStructure = "AlteringDebuggerMemoryStructure";


    /**
     * Utility method to split a file name into name (i.e. path) and extension
     * @param filename the file name
     * @return a String array: 0 is name/path, 1 is extension. null if error
     */
    public static String[] getFileNameAndExtension(String filename) {

        // log info
        ATADLogger.logVerboseInfo(className, getFileNameAndExtensionMethodName, "retrieving file name and extension for file " + filename);

        // get the index of the last occurrence of the file separator (the dot)
        int lastIndexOfDot = filename.lastIndexOf(".");

        // create the array to be returned
        String [] nameAndExtension = {null, null};

        // if the last occurrence of the file separator is found and it is not the first char
        if ( lastIndexOfDot != -1 && lastIndexOfDot != 0) {

            // take the substring from the beginning to the file separator (excluded) as name/path
            nameAndExtension[0] = filename.substring(0, lastIndexOfDot );

            // take the substring from the file separator (excluded) as extension
            nameAndExtension[1] = filename.substring(lastIndexOfDot + 1);
        }
        // if that is not the case
        else {

            // log info
            ATADLogger.logVerboseInfo(className, getFileNameAndExtensionMethodName,
                    "error while splitting file name: " + filename + ", skipping it.");

            // set the returning value to null
            nameAndExtension = null;
        }

        // finally return
        return nameAndExtension;

    }


    /**
     * Utility method to recursively get APK files inside a given directory
     * @param apkDirectory the folder where to search for the files
     * @param apkFileSet the set in which all APK files will be inserted
     * @param extension the extension the file has to end with (e.g. "apk", "class", ...)
     */
    public static void getAllFilesWithGivenExtension(final File apkDirectory, HashSet<File> apkFileSet, String extension) {

        // log info
        ATADLogger.logVerboseInfo(className, getAllFilesWithGivenExtensionMethodName, "retrieving all files in directory " + apkDirectory.getName() + " with extension " + extension);

        // if the set is not initialized yet
        if (apkFileSet == null) {

            // initialize it
            apkFileSet = new HashSet<>();
        }

        // if the directory exists
        if (apkDirectory.exists()) {

            // try to list the files in the directory
            try {

                // for each file
                for (final File possibleAPKFile : Objects.requireNonNull(apkDirectory.listFiles())) {

                    // if it is a directory
                    if (possibleAPKFile.isDirectory()) {

                        // recursively call this function
                        getAllFilesWithGivenExtension(possibleAPKFile, apkFileSet, extension);
                    }
                    // else
                    else {

                        // if the extension of the file is "apk"
                        if (getFileNameAndExtension(possibleAPKFile.getName())[1].endsWith(extension)) {

                            // add it into the set
                            apkFileSet.add(possibleAPKFile);
                        }
                    }
                }
            }
            // catch an eventual exception
            catch (NullPointerException e) {

                // log error
                ATADLogger.logError(className, getAllFilesWithGivenExtensionMethodName, "the directory " +
                        apkDirectory + " could not be accessed (probable too low permissions)", e);
            }
        }
        // otherwise, if it is null
        else {

            // log error
            ATADLogger.logError(className, getAllFilesWithGivenExtensionMethodName, "the directory " +
                    apkDirectory + " does not exist", null);

        }
    }


    /**
     * extract Jar files from jarFilePath to classFilesDirectory
     * @param jarFilePath the JAR
     * @param classFilesDirectory the destination directory
     * @param filters optional array of filters (e.g. to not extract java classes of standard Android libraries)
     * @return true if everything was successful, false otherwise
     */
    public static boolean extractClassFilesFromJAR(String jarFilePath, String classFilesDirectory, String[] filters) {

        // log info
        ATADLogger.logInfo(className, extractClassFilesFromJARMethodName, "extracting .class files from JAR "
                + jarFilePath + " to directory " + classFilesDirectory);

        // log verbose info
        ATADLogger.logVerboseInfo(className, extractClassFilesFromJARMethodName, "filters are" + Arrays.toString(filters));

        try {

            // create a new jar file
            JarFile jar = new JarFile(jarFilePath);

            // enumeration object for iterating over the entries (both .class files and subdirectories)
            Enumeration enumEntries = jar.entries();

            // until we have elements to iterate over
            while (enumEntries.hasMoreElements()) {

                // get the entry (either directory or file) and its name
                JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
                String jarEntryName = jarEntry.getName();

                // boolean for filters
                boolean isToBeFiltered = false;

                // for each filter
                for (String filter : filters) {

                    // if the entry is to be filtered
                    if (jarEntryName.startsWith(filter)) {

                        // set the flag (and break)
                        isToBeFiltered = true;
                        break;
                    }
                }


                // if the jar entry is not to be filtered
                if (!isToBeFiltered) {

                    // create the reference to the class file
                    File classFile = new File(classFilesDirectory + File.separator + jarEntryName);

                    // if the file is actually a directory, just create it
                    if (jarEntry.isDirectory()) {

                        // if the directory doesn't exists already
                        if (!classFile.exists()) {

                            // if we didn't manage to create the directory
                            if (!classFile.mkdir()) {

                                // log info
                                ATADLogger.logError(className, extractClassFilesFromJARMethodName, "was not able" +
                                        " to create directory " + classFile + " while extracting jar files", null);

                                // return false
                                return false;

                            }
                        }
                    }
                    // else, if it is a file, transcribe the content of the jar entry to the file
                    else {

                        // get input stream for the jar entry to get its content
                        InputStream is = jar.getInputStream(jarEntry);

                        // the output stream to write the .class file
                        FileOutputStream fos = new FileOutputStream(classFile);

                        // write contents of 'is' to 'fos'
                        while (is.available() > 0) {

                            // actually write
                            fos.write(is.read());
                        }

                        // at the end, close the files
                        fos.close();
                        is.close();
                    }
                }
            }
            // at the end, close also the jar
            jar.close();
        }
        // if there was an exception
        catch ( IOException e) {

            // log info
            ATADLogger.logError(className, extractClassFilesFromJARMethodName, "exception while extracting jar files", e);

            // return false
            return false;
        }

        // if everything was fine
        return true;
    }


    /**
     * This methods reads a JSON file and store it into a JSONObject
     * @param pathOfJsonFile the path on the filesystem of the json file
     */
    public static JSONObject loadJSONFile (String pathOfJsonFile) {

        // log info
        ATADLogger.logInfo(className, loadJSONFileMethodName, "reading json file " + pathOfJsonFile + "...");

        // first of all, acquire and load the file
        File jsonProtectionPatternsFile = new File(pathOfJsonFile);

        // the JSON object containing the protection patterns
        JSONObject jsonFile = null;

        // if the file exists and it is really a file
        if (jsonProtectionPatternsFile.exists() && jsonProtectionPatternsFile.isFile()) {

            // try to read the content
            try {

                // get the bytes from the file
                byte[] jsonProtectionPatternsFileInBytes= Files.readAllBytes(Paths.get(pathOfJsonFile));

                // get the string from the bytes and build the JSON object
                jsonFile = new JSONObject(new String(jsonProtectionPatternsFileInBytes));

            }
            // if an exception was thrown
            catch (IOException e) {

                // log info
                ATADLogger.logError(className, loadJSONFileMethodName, "FATAL: cannot load the JSON file: ", e);

                // exit with status 2
                System.exit(2);
            }
        }
        // else, if the file doesn't exist or it is a directory
        else {

            // log info
            ATADLogger.logError(className, loadJSONFileMethodName, "FATAL: cannot load the JSON file, either it doesn't exist or it is a directory", null);

            // exit with status 2
            System.exit(2);
        }

        // return the json file
        return jsonFile;

    }


    /**
     * save on file system content as byte array in a file
     * @param fullPathOfFile directory where to save the file
     * @param contentToSave the byte array to save
     * @param behaviourFlag flag for behaviour if file already exists:
     *                      - 0 overwrite the previous file
     *                      - 1 create file appending a number at the end of the name
     *                      - 2 do not write if file already exists
     * @return the file path of the saved file if everything went well, null otherwise
     */
    public static boolean saveFileOnFileSystem(String fullPathOfFile, byte[] contentToSave, int behaviourFlag) {

        // log info
        ATADLogger.logVerboseInfo(className, saveFileOnFileSystem, "saving file with name " +
                fullPathOfFile);

        // calculate file name and extension
        String[] nameAndExtension = FileUtil.getFileNameAndExtension(fullPathOfFile);
        String fullPathOfFileWithoutExtension = nameAndExtension[0];
        String fileExtension = nameAndExtension[1];

        // the returning value (default value is the given path)
        String newFilePath = fullPathOfFile;

        // create the file
        File newFile = new File(newFilePath);

        // the output stream
        FileOutputStream outputStreamForFile;

        // if the file exists already and the flag is 0
        if (newFile.exists()) {

            // switch depending on the behaviour flag
            switch (behaviourFlag) {

                // overwrite the previous file, thus delete it
                case 0:

                    // if we were NOT able to delete it
                    if (!newFile.delete()) {

                        newFilePath = null;
                    }
                    else {

                        // log info
                        ATADLogger.logVerboseInfo(className, saveFileOnFileSystem, "file " + newFilePath
                                + "  already existed. Behaviour is to overwrite, so the file was deleted" );
                    }
                    break;


                // append a number at the end of the file name
                case 1:

                    // this is for appending a number at the end of the file name if it already exists
                    for (int i = 0; newFile.exists(); i++) {

                        newFilePath = fullPathOfFileWithoutExtension + " (" + i + ")." + fileExtension;
                        newFile = new File(newFilePath);
                    }

                    // log info
                    ATADLogger.logVerboseInfo(className, saveFileOnFileSystem, "file " + newFilePath
                            + "  already existed. Behaviour is to append number, so the file name was modified. Now" +
                            " the file name if " +  newFilePath);
                    break;


                // do not write
                case 2:

                    // just set to null the file path
                    newFilePath = null;
                    break;


                // for any other case
                default:

                    // set to null the file path
                    newFilePath = null;
                    // log info
                    ATADLogger.logWarning(className, saveFileOnFileSystem, "given wrong flag " + behaviourFlag
                            + ". It must be 0, 1 or 2. The file was not saved");
                    return false;
            }
        }

        try {

            // if we have a file path
            if (newFilePath != null) {

                // if we were able to create it
                if (newFile.createNewFile()) {

                    // create an output stream
                    outputStreamForFile = new FileOutputStream(newFile);

                    // save the content
                    outputStreamForFile.write(contentToSave);

                    // flush and close the stream
                    outputStreamForFile.flush();
                    outputStreamForFile.close();
                } else {

                    // log error and return false
                    ATADLogger.logError(className, saveFileOnFileSystem, ": not able to create new file " + newFilePath, null);
                    return false;
                }
            }
        }
        catch (IOException e) {

            ATADLogger.logError(className, saveFileOnFileSystem, ": exception while saving file " + newFilePath, e);

            return false;

        }

        // return
        return true;
    }




    /**
     * Method used for extracting .so libraries from the apk file
     * @param apkFile the apk file
     * @param destinationDirectory the destination directory
     * @return true if everything was successful, false otherwise
     */
    public static boolean extractSOLibrariesFromAPK (File apkFile, File destinationDirectory) {

        // returning value
        boolean outcome = true;

        // log info
        ATADLogger.logInfo(className, extractSOLibrariesFromAPKMethodName, "extracting .so libraries from APK "
                + apkFile.getName() + " to directory " + destinationDirectory.getName());

        try {

            // buffer that will be used for the stream
            byte[] buffer = new byte[1024];

            // the zip input stream from the apk file
            ZipInputStream zis = new ZipInputStream(new FileInputStream(apkFile));

            // to loop over the entries in the zip file
            ZipEntry zipEntry;

            // acquire the next entry
            zipEntry = zis.getNextEntry();

            // while there are entries
            while (zipEntry != null) {

                // get the filename and the extension
                String [] zipEntryFileNameAndExtension = getFileNameAndExtension(zipEntry.getName());

                // if there were no errors in the splitting of the name
                if (zipEntryFileNameAndExtension != null) {

                    // if the file is a .so library
                    if (zipEntryFileNameAndExtension[1].equals("so")) {

                        // get the name of the library (getting rid of directories in path)
                        String[] soLibraryNameSplit = zipEntry.getName().split("/");
                        String soLibraryName = soLibraryNameSplit[soLibraryNameSplit.length - 1];

                        // create a file from it
                        File newFile = new File(destinationDirectory + "/" + soLibraryName);

                        // TODO when there are multiple versions of the same library (armv7, armv8, x86, ...), the latest version overwrites the others

                        // the output stream for the uncompressed files
                        FileOutputStream fos = new FileOutputStream(newFile);

                        // while there are bytes to decompress
                        int len;
                        while ((len = zis.read(buffer)) > 0) {

                            // write the bytes in the new file
                            fos.write(buffer, 0, len);
                        }

                        // when finished, close the output stream
                        fos.close();
                    }
                }

                // acquire the next entry
                zipEntry = zis.getNextEntry();
            }

            // close the zip file and stream
            zis.closeEntry();
            zis.close();

        }
        // catch an eventual exception
        catch (Exception e) {

            // log info
            ATADLogger.logError(className, extractSOLibrariesFromAPKMethodName, "exception while extracting .so libraries from APK "
                    + apkFile.getName() + " to directory " + destinationDirectory.getName(), e);

            // set the outcome to false
            outcome = false;
        }

        // finally, return
        return outcome;
    }


    /**
     * Get package name from compiled manifest through command "aapt"
     * @param apkFile the apk file
     * @return the package name as string, or null if there was an error
     */
    public static String getApplicationPackageFromManifest (File apkFile) {

        // the package name
        String packageName = null;

        // log error
        ATADLogger.logInfo(className, getApplicationPackageFromManifestName, "getting package name for apk " + apkFile.getName());

        try {

            // create a process builder to run the dex2jar program
            ProcessBuilder pb = new ProcessBuilder("aapt", "dump", "badging", apkFile.getAbsolutePath(),
                    "|", "grep", "package:\\", "name");

            // redirect output to log file
            pb.redirectErrorStream(true);

            // start the process
            Process p = pb.start();

            // here we must NOT wait for the process to finish through "p.waitFor();"
            // because the output of the process ("aapt") could be too big for the buffer size
            // provided by the underlying platform. So we "drain" the buffer instead, that in
            // the end has the same effect of p.waitFor();

            // get a reader for the inputStream
            BufferedReader aaptLogReader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // the string that will contain the temporary line logs
            String output;

            // this is an example of what the output should be:
            // "package: name='glumo.com.glumo' versionCode='1' versionName='1.0' platformBuildVersionName='7.1.1'"

            // while there is output to parse
            while ((output = aaptLogReader.readLine()) != null) {

                // split by char '
                String[] splitOutput = output.split("'");

                // the the length is greater than one and the first part is = "package: name="
                if (splitOutput.length > 1 && splitOutput[0].equals("package: name=")) {

                    // the package name is the second element of the array (e.g. "glumo.com.glumo")
                    packageName = splitOutput[1];

                    // substitute point with slash -> "glumo.com.glumo" -> "glumo/com/glumo"
                    packageName = packageName.replace(".", "/");
                }
            }
        }
        // catch any exception
        catch (IOException e) {

            // log error
            ATADLogger.logError(className, getApplicationPackageFromManifestName, "error in invoking aapt for " + apkFile.getName(), e);

            // return null to indicate error
            packageName = null;

        }

        // return
        return packageName;
    }


    /**
     * This method substitute the string "substituteWithTheApplicationPackage" in the JSON with the package name of the application
     * @param reportForThisAPK the JSON object in which we have to substitute the string
     * @param apkPackageName the package name
     */
    public static void substitutePackageNameInJSON(JSONObject reportForThisAPK, String apkPackageName) {

        // log info
        ATADLogger.logInfo(classes, substitutePackageNameInJSONName, "substituting package name in JSON: " + apkPackageName);

        // parse the JSON to substitute each occurrence of the string "substituteWithTheApplicationPackage"
        // get the array of categories
        JSONArray categories = reportForThisAPK.getJSONArray(FileUtil.protectionCategory);

        // now, for each protection category (e.g. anti-tampering, anti-debugging, ...)
        for (int i = 0; i < categories.length(); i++) {

            // get the protection category object
            JSONObject protectionCategory = categories.getJSONObject(i);

            // get the list of protections
            JSONArray protections = protectionCategory.getJSONArray(FileUtil.protection);

            // now, for each different kind of protection
            for (int j = 0; j < protections.length(); j++) {

                // get the protection pattern object
                JSONObject protection = protections.getJSONObject(j);

                // get the java level patterns
                JSONObject javaLevelPatterns = protection.getJSONArray(FileUtil.javaLevelPatterns).getJSONObject(0);

                // get the patterns for the classes, methods, attributes and strings
                JSONArray patternClasses = javaLevelPatterns.getJSONArray(FileUtil.classes);
                JSONArray patternMethods = javaLevelPatterns.getJSONArray(FileUtil.methods);
                JSONArray patternAttributes = javaLevelPatterns.getJSONArray(FileUtil.attributes);
                JSONArray patternStrings = javaLevelPatterns.getJSONArray(FileUtil.strings);


                // for each class in the pattern
                for (int k = 0; k < patternClasses.length(); k++) {

                    // get the pattern for the class
                    String patternClass = patternClasses.getJSONArray(k).getString(0);

                    // if there is a match with the string to replace
                    if (patternClass.contains(substituteWithTheApplicationPackage)) {

                        // get the actual pattern for the class
                        String newPattern = patternClass.split("/")[1];

                        // create and substitute the new array
                        JSONArray newArray = new JSONArray();

                        // the first element is the new pattern
                        // (e.g. "substituteWithTheApplicationPackage/BuildConfig.DEBUG" -> "glumo/com/glumo/BuildConfig")
                        newArray.put(0, apkPackageName + "/" + newPattern);

                        // the second and third elements are the number of occurrences found (now obviously zero)
                        newArray.put(1, 0);
                        newArray.put(2, 0);

                        // replace the array
                        patternClasses.put(k, newArray);
                    }
                }

                // for each method in the pattern
                for (int k = 0; k < patternMethods.length(); k++) {

                    // get the pattern for the method
                    String patternMethod = patternMethods.getJSONArray(k).getString(0);

                    // if there is a match with the string to replace
                    if (patternMethod.contains(substituteWithTheApplicationPackage)) {

                        // get the actual pattern for the class
                        String newPattern = patternMethod.split("/")[1];

                        // create and substitute the new array
                        JSONArray newArray = new JSONArray();

                        // the first element is the new pattern
                        // (e.g. "substituteWithTheApplicationPackage/BuildConfig.doStuff" -> "glumo/com/glumo/BuildConfig.doStuff")
                        newArray.put(0, apkPackageName + "/" + newPattern);

                        // the second and third elements are the number of occurrences found (now obviously zero)
                        newArray.put(1, 0);
                        newArray.put(2, 0);

                        // replace the array
                        patternMethods.put(k, newArray);
                    }
                }

                // for each attribute in the pattern
                for (int k = 0; k < patternAttributes.length(); k++) {

                    // get the pattern for the attribute
                    String patternAttribute = patternAttributes.getJSONArray(k).getString(0);

                    // if there is a match with the string to replace
                    if (patternAttribute.contains(substituteWithTheApplicationPackage)) {

                        // get the actual pattern for the class
                        String newPattern = patternAttribute.split("/")[1];

                        // create and substitute the new array
                        JSONArray newArray = new JSONArray();

                        // the first element is the new pattern
                        // (e.g. "substituteWithTheApplicationPackage/BuildConfig.DEBUG" -> "glumo/com/glumo/BuildConfig.DEBUG")
                        newArray.put(0, apkPackageName + "/" + newPattern);

                        // the second and third elements are the number of occurrences found (now obviously zero)
                        newArray.put(1, 0);
                        newArray.put(2, 0);

                        // replace the array
                        patternAttributes.put(k, newArray);
                    }
                }

                // for each string in the pattern
                for (int k = 0; k < patternStrings.length(); k++) {

                    // get the pattern for the string
                    String patternString = patternStrings.getJSONArray(k).getString(0);

                    // if there is a match with the string to replace
                    if (patternString.contains(substituteWithTheApplicationPackage)) {

                        // get the actual pattern for the class
                        String newPattern = patternString.split("\\.")[1];

                        // create and substitute the new array
                        JSONArray newArray = new JSONArray();

                        // the first element is the new pattern
                        // (e.g. "substituteWithTheApplicationPackage/BuildConfig.DEBUG" -> "glumo.com.glumo.BuildConfig.DEBUG")
                        // Note that here we replace "/" with ".". This is because this is the way to spell names of classes,
                        // methods and strings for reflection
                        newArray.put(0, apkPackageName.replace("/", ".") + "." + newPattern);

                        // the second and third elements are the number of occurrences found (now obviously zero)
                        newArray.put(1, 0);
                        newArray.put(2, 0);

                        // replace the array
                        patternStrings.put(k, newArray);
                    }
                }
            }
        }
    }


    /**
     * this method recursively deletes a directory and all its content
     * @param directoryToBeDeleted entry point
     * @return true if operation was successfull
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }


    public static String getStringFromStackTrace (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        return sStackTrace;
    }

}