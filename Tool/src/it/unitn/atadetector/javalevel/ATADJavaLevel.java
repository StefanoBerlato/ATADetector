package it.unitn.atadetector.javalevel;

import it.unitn.atadetector.MainAPKLooper;
import it.unitn.atadetector.javalevel.asm.ClassFileASMVisitor;
import it.unitn.atadetector.util.ATADLogger;
import it.unitn.atadetector.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is responsible for detecting anti-tampering (AT) and anti-debugging (AD) defences at java layer
 * It will exploit the ASM library to navigate through the code and check the presence of the protection patterns
 * Eventually, it will insert into the given json the counter of the matches found
 */
public class ATADJavaLevel {

    // variables for logging
    private static final String className = "ATADJavaLevel";
    private static final String analyzeJavaClassesName = "analyzeJavaClasses";

    // the directory containing the .class files
    private File classFilesDirectory = null;

    // the name of the package of the application, retrieved from the manifest. It is used
    // to filter the java class to analyze (if filled)
    public String packageName =  "";



    /**
     * Constructor
     * @param classFilesDirectory the directory containing the .class files
     */
    public ATADJavaLevel(File classFilesDirectory) {

        // if it was actually given a file and not a null
        if (classFilesDirectory != null) {

            // if the directory really exists
            if (classFilesDirectory.exists()) {

                // assigning the value
                this.classFilesDirectory = classFilesDirectory;
            }
        }

    }

    /**
     * This method will analyze the Java classes of the given APK, collecting imported classes, methods, attributes and strings
     * Then it will compare them to the patterns in the JSON file
     * @param reportForThisAPK the JSON report of this APK (clone of the original JSON report)
     */
    public boolean analyzeJavaClasses(JSONObject reportForThisAPK) {

        // ===== ANALYZE CLASS FILES IN APK =====
        // - step 1 -> get all .class files in the given directory
        // - step 2 -> parse each .class file with ASM, getting invoked methods, fields and strings
        // - step 3 -> compare these with the ones in the JSON patterns

        // if either the pointer to the folder or the JSONObject are null
        if (this.classFilesDirectory == null || reportForThisAPK == null) {

            // log error
            ATADLogger.logError(className, analyzeJavaClassesName, "given null or not existing directory for .class " +
                    "files OR null JSON object for reporting the outcome", null);

            // notify the error to the looper class
            MainAPKLooper.notifyError(classFilesDirectory.getAbsolutePath(), "given null or not existing directory for .class " +
                    "files OR null JSON object for reporting the outcome");

            // return false to indicate the error
            return false;
        }
        // otherwise, proceed with the analysis
        else {

            // ===== step 1 -> get all .class files in the given directory =====

            // the file containing the java class files to analyze. If packageName was given, the
            // research is automatically narrowed to the main package only
            File directoryOfClassesFile = new File(classFilesDirectory.getAbsolutePath() +
                    (("".equals(packageName)) ? "" : "/" + packageName));

            // log info
            ATADLogger.logInfo(className, analyzeJavaClassesName, "starting analysis (Java level) in folder "
                    + directoryOfClassesFile.getAbsolutePath() + "...");

            // the set that will hold the files
            HashSet<File> classFiles = new HashSet<>();

            // get all the .class files
            FileUtil.getAllFilesWithGivenExtension(directoryOfClassesFile , classFiles, "class");

            // if there are no class files (surely an error)
            if (classFiles.isEmpty()) {

                // log error
                ATADLogger.logError(className, analyzeJavaClassesName, "no .class files in in folder " +
                        classFilesDirectory.getAbsolutePath() + " (probably there was an error while retrieving the files)", null);

                // notify the error to the looper class
                MainAPKLooper.notifyError(classFilesDirectory.getAbsolutePath(), "no .class files in in folder");

                // return false to indicate the error
                return false;
            }
            // otherwise
            else {

                // list of libraries. The purpose is to collect separately the class-method-variable-string occurrences
                // wrt the libraries, so to produce two different final reports: one including the libraries, one with
                // just the developers' code
                String[] libraries = new String [] {
                        "android.",
                        "androidx.",
                        "butterknife.",
                        "com.android.",
                        "com.adcolony.",
                        "com.adjust.",
                        "com.crittercism.",
                        "com.readystatesoftware.",
                        "com.appsflyer.",
                        "com.networkbench.",
                        "com.dropbox.",
                        "com.braintreepayments.",
                        "com.airbnb.lottie.",
                        "com.jakewharton.",
                        "com.rateus.",
                        "com.twitter.",
                        "com.comscore.",
                        "com.my.target.",
                        "com.startapp.",
                        "com.mobvista.",
                        "com.facebook.",
                        "com.monet.",
                        "com.samsung.",
                        "com.kochava.",
                        "com.baidu.",
                        "com.tune.",
                        "com.amazon.",
                        "com.moat.",
                        "com.inmobi.",
                        "com.flurry.",
                        "com.tencent.",
                        "com.paypal.",
                        "com.distil.",
                        "com.google.",
                        "com.zendesk.",
                        "com.bugsnag.",
                        "org.chromium.",
                        "org.conscrypt.",
                        "com.applovin.",
                        "com.squareup.",
                        "com.foursquare.",
                        "com.mixpanel.",
                        "com.getkeepsafe.",
                        "com.qihoo360.",
                        "com.anjlab.",
                        "com.scottyab.",
                        "com.unity3d.",
                        "com.zopim.",
                        "com.learnium.",
                        "com.crashlytics.",
                        "com.stripe.",
                        "com.umeng.",
                        "cn.jiguang.",
                        "dalvik.",
                        "dagger.",
                        "de.blinkt.openvpn.",
                        "java.",
                        "javax.",
                        "io.fabric.",
                        "io.agora.",
                        "io.sentry.",
                        "io.intercom.",
                        "io.branch.",
                        "io.reactivex.",
                        "io.realm.",
                        "net.hockeyapp.",
                        "net.openid.",
                        "org.acra.",
                        "org.spongycastle.",
                        "org.xbill.",
                        "okio.gzipsing.",
                        "org.apache.",
                        "org.mozilla.",
                        "org.sufficientlysecure.",
                        "org.godotengine.",
                        "org.webrtc.",
                        "okhttp3.",
                        "org.greenrobot.",
                        "org.robolectric.",
                        "org.parceler.",
                        "retrofit2.",
                        "kotlin.",
                        "kotlinx."

                };

                // ===== step 2: parse each .class file with ASM, getting invoked methods, fields and strings  =====

                // this is the object that collects all the elements (strings, methods, fields, ...) found in a class.
                // The key is class name. Then the array list has classes in [0], methods in [1], attributes in [2], strings in [3].
                // The key of the second map is the name of the found occurrence, the value an array that has in 0 the total number of
                // occurrences, in 1 the number of occurrences if the class does NOT belong to a library
                HashMap<String, ArrayList<HashMap<String, int[]>>> classesProperties = new HashMap<>();

                // now, for all .class files
                for (File classFile : classFiles) {

                    try {

                        // "0" if the file is NOT a library, "1" if the file is a library
                        // (thus to be filtered)
                        String isLibraryFlag = "0";

                        // name (and package) of class file
                        String classFileName = classFile.getAbsolutePath().split("/classFiles/")[1].replace("/", ".");

                        // for each filter
                        for (String filter : libraries) {

                            // if the entry is to be filtered (thus, it is a library)
                            if (classFileName.startsWith(filter)) {

                                // set the flag
                                isLibraryFlag = "1";
                                break;
                            }
                        }

                        // instantiate an array list
                        ArrayList<HashMap<String, int[]>> tempArrayList = new ArrayList<>();

                        // add the four HashMap (classes in [0], methods in [1], attributes in [2], strings in [3])
                        tempArrayList.add(0, new HashMap<>());
                        tempArrayList.add(1, new HashMap<>());
                        tempArrayList.add(2, new HashMap<>());
                        tempArrayList.add(3, new HashMap<>());

                        // put the new list inside the map
                        classesProperties.put(classFileName, tempArrayList);

                        // read the file through a stream
                        FileInputStream classFileInputStream = new FileInputStream(classFile);

                        // create a visitor for retrieving all the symbols from the class
                        ClassFileASMVisitor tempVisitor = new ClassFileASMVisitor(classesProperties.get(classFileName), isLibraryFlag);

                        // the reader ASM class that will generate the events for the given class
                        ClassReader tempReader = new ClassReader(classFileInputStream);

                        // combine all together and start the analysis to extract the symbols
                        tempReader.accept(tempVisitor, 0);

                        // close the stream
                        classFileInputStream.close();
                    }
                    // if the file was not found
                    catch (FileNotFoundException e) {

                        // log error
                        ATADLogger.logError(className, analyzeJavaClassesName, ".class file " + classFile.getAbsolutePath() + " not found in folder " +
                                classFilesDirectory.getAbsolutePath() + " (no read permission? was deleted? other IO errors?)", e);

                        // notify the error to the looper class
                        MainAPKLooper.notifyError(classFilesDirectory.getAbsolutePath(), ".class file " + classFile.getAbsolutePath() + " not found in folder");

                        // return false to indicate the error
                        return false;
                    }
                    // exception while instantiating reader
                    catch (IOException e) {

                        // log error
                        ATADLogger.logError(className, analyzeJavaClassesName, "error while creating ASM reader for " +
                                ".class file " + classFile.getAbsolutePath(), e);

                        // notify the error to the looper class
                        MainAPKLooper.notifyError(classFilesDirectory.getAbsolutePath(), "error while creating ASM reader");

                        // return false to indicate the error
                        return false;
                    }
                }



                // ===== step 3 -> compare the symbols to the JSON patterns =====
                // ===== parse the JSON to get the patterns:
                // ===== for each protection category -> for each protection -> for each .class file-> compare symbols with patterns (classes/methods/attributes/strings)

                // get the array of categories
                JSONArray categories = reportForThisAPK.getJSONArray(FileUtil.protectionCategory);

                // now, for each protection category (e.g. anti-tampering, anti-debugging, ...)
                for (int i = 0; i < categories.length(); i++) {

                    // get the protection category object to later retrieve the values of the
                    // protection category (name, list of protections, ...)
                    JSONObject protectionCategory = categories.getJSONObject(i);

                    // log info
                    ATADLogger.logVerboseInfo(className, analyzeJavaClassesName, "parsing protection patterns " +
                            "of category " + protectionCategory.getString(FileUtil.categoryName) + "...");

                    // get the list of protections
                    JSONArray protections = protectionCategory.getJSONArray(FileUtil.protection);

                    // now, for each different kind of protection
                    for (int j = 0; j < protections.length(); j++) {

                        // get the protection pattern object to later retrieve the values
                        // (name, java/native level protection patterns, ...)
                        JSONObject protection = protections.getJSONObject(j);

                        // log info
                        ATADLogger.logVerboseInfo(className, analyzeJavaClassesName, "parsing protection patterns " +
                                "of protection " + protection.getJSONArray(FileUtil.nameOfTheProtection).getString(0) + "...");

                        // get the java level patterns
                        JSONObject javaLevelPatterns = protection.getJSONArray(FileUtil.javaLevelPatterns).getJSONObject(0);

                        // get the patterns for the classes, methods, attributes and strings
                        JSONArray patternClasses = javaLevelPatterns.getJSONArray(FileUtil.classes);
                        JSONArray patternMethods = javaLevelPatterns.getJSONArray(FileUtil.methods);
                        JSONArray patternAttributes = javaLevelPatterns.getJSONArray(FileUtil.attributes);
                        JSONArray patternStrings = javaLevelPatterns.getJSONArray(FileUtil.strings);

                        // get the length of the patterns. Remember that the "Strings" pattern contains also the string
                        // representation of the other patterns "Classes", "Methods" and "Attributes" (this is to try to
                        // catch reflection calls). Thus, the length of only strings is the overall length minus the
                        // length of the other patterns
                        int patternClassesLength = patternClasses.length();
                        int patternMethodsLength = patternMethods.length();
                        int patternAttributesLength = patternAttributes.length();
                        int patternStringsWithStringsForReflection = patternStrings.length();
                        int patternStringsLength = patternStringsWithStringsForReflection - patternAttributesLength - patternMethodsLength - patternClassesLength;

                        int attributesOffset =  (patternStringsLength + patternClassesLength + patternMethodsLength);
                        int methodsOffset    =  (patternStringsLength + patternClassesLength);


                        // the variable to collect the strings related to reflection. The first key is the
                        // identifier for the pattern level (class, methods or attribute). The second is the set
                        // of the found strings. Unfortunately, this way we are not able to count occurrences (so even if
                        // we find 100+ occurrences for a pair class-attribute, we count it only once). This is a conservative
                        // approach. because we can not do the other way around
                        HashMap<String, HashMap<String, int[]>> reflectionStrings = new HashMap<>();
                        reflectionStrings.put(FileUtil.classes, new HashMap<>());
                        reflectionStrings.put(FileUtil.methods, new HashMap<>());
                        reflectionStrings.put(FileUtil.attributes, new HashMap<>());

                        // now, for each classFile
                        for (File classFile : classFiles) {

                            // name (and package) of class file
                            String classFileName = classFile.getAbsolutePath().split("/classFiles/")[1].replace("/", ".");

                            // get the list of retrieved properties of the class
                            ArrayList<HashMap<String, int[]>> currentClassProperties = classesProperties.get(classFileName);

                            // for each String in the pattern
                            for (int k = 0; k < patternStrings.length(); k++) {

                                // for each string found in the current .class file we are analyzing
                                for (String foundString : currentClassProperties.get(3).keySet()) {

                                    // get reference to the variable to make code more readable
                                    JSONArray currentStringPattern = patternStrings.getJSONArray(k);

                                    // if there is a match
                                    if (currentStringPattern.getString(0).equals(foundString)) {

                                        // if the index of the strings is not related to the "string" but to other
                                        // patterns ("classes", "attributes" or "methods"). This is for reflection:
                                        if (k > patternStringsLength) {

                                            // if the pattern refers to an attribute
                                            if (k >= attributesOffset) {

                                                // add it to the set of the name of the attributes found as strings
                                                reflectionStrings.get(FileUtil.attributes).put(foundString, currentClassProperties.get(3).get(foundString));
                                            }
                                            // else if the pattern refers to a method
                                            else if (k >= methodsOffset) {

                                                // add it to the set of the name of the methods found as strings
                                                reflectionStrings.get(FileUtil.methods).put(foundString, currentClassProperties.get(3).get(foundString));
                                            }
                                            // else, the pattern refers to a class
                                            else {

                                                // add it to the set of the name of the classes found as strings
                                                reflectionStrings.get(FileUtil.classes).put(foundString, currentClassProperties.get(3).get(foundString));
                                            }
                                        }

                                        // increment the matches found and insert the update number in the pattern
                                        currentStringPattern.put(1, currentStringPattern.getInt(1) +
                                                currentClassProperties.get(3).get(foundString)[0]);
                                        currentStringPattern.put(2, currentStringPattern.getInt(2) +
                                                currentClassProperties.get(3).get(foundString)[1]);
                                    }
                                }
                            }


                            // for each class in the pattern
                            for (int k = 0; k < patternClasses.length(); k++) {

                                // get reference to the variable to make code more readable
                                JSONArray currentClassPattern = patternClasses.getJSONArray(k);

                                // for each class imported in the current .class file we are analyzing
                                for (String foundClass : currentClassProperties.get(0).keySet()) {

                                    // if there is a match
                                    if (currentClassPattern.getString(0).equals(foundClass)) {

                                        // increment the matches found and insert the update number in the pattern
                                        currentClassPattern.put(1, currentClassPattern.getInt(1) +
                                                currentClassProperties.get(0).get(foundClass)[0]);
                                        currentClassPattern.put(2, currentClassPattern.getInt(2) +
                                                currentClassProperties.get(0).get(foundClass)[1]);

                                        // DO NOT also add it to the set of the name of the classes found as strings
                                        // BECAUSE semantically may not be correct, but it is a great optimization
                                        // Substitute the "/" with the "." because this is what is required to load a
                                        // class through reflection
                                        //reflectionStrings.get(FileUtil.classes).add(foundClass.replace("/", "."));
                                    }
                                }
                            }


                            // for each method in the pattern
                            for (int k = 0; k < patternMethods.length(); k++) {

                                // get reference to the variable to make code more readable
                                JSONArray currentMethodPattern = patternMethods.getJSONArray(k);

                                // for each method invoked in the current .class file we are analyzing
                                for (String foundMethod : currentClassProperties.get(1).keySet()) {

                                    // if there is a match
                                    if (currentMethodPattern.getString(0).equals(foundMethod)) {

                                        // increment the matches found and insert the update number in the pattern
                                        currentMethodPattern.put(1, currentMethodPattern.getInt(1) +
                                                currentClassProperties.get(1).get(foundMethod)[0]);
                                        currentMethodPattern.put(2, currentMethodPattern.getInt(2) +
                                                currentClassProperties.get(1).get(foundMethod)[1]);
                                    }
                                }

                                // We try to see if we got something through reflection. This is the criteria: for each method,
                                // if we found the string of the method AND (the string of the class OR the class itself),
                                // we increment the occurrences for that method by 1

                                // get the method and split it into the class and the name of the method
                                String[] currentMethodSplit = currentMethodPattern.getString(0).split("\\.");
                                String currentClassAsString = currentMethodSplit[0].replace("/", ".");
                                String currentMethodAsString = currentMethodSplit[1];

                                // did we find the class and method as strings? and in which part of the code?
                                boolean foundClassInLibrary = false;
                                boolean foundMethodInLibrary = false;
                                boolean foundClassNotInLibrary = false;
                                boolean foundMethodNotInLibrary = false;

                                // for each class found as a string
                                for (String foundClassesAsStrings : reflectionStrings.get(FileUtil.classes).keySet()) {

                                    // if there is a match in the patterns
                                    if (foundClassesAsStrings.equals(currentClassAsString)) {

                                        if (reflectionStrings.get(FileUtil.classes).get(foundClassesAsStrings)[0] > 0) {
                                            foundClassInLibrary = true;
                                        }
                                        if (reflectionStrings.get(FileUtil.classes).get(foundClassesAsStrings)[1] > 0) {
                                            foundClassNotInLibrary = true;
                                        }

                                    }
                                }

                                // for each method found as a string
                                for (String foundMethodsAsStrings : reflectionStrings.get(FileUtil.methods).keySet()) {

                                    // if there is a match in the patterns
                                    if (foundMethodsAsStrings.equals(currentMethodAsString)) {

                                        if (reflectionStrings.get(FileUtil.methods).get(foundMethodsAsStrings)[0] > 0) {
                                            foundMethodInLibrary = true;
                                        }
                                        if (reflectionStrings.get(FileUtil.methods).get(foundMethodsAsStrings)[1] > 0) {
                                            foundMethodNotInLibrary = true;
                                        }
                                    }
                                }


                                // now, if we found both the class and the method as strings in a library
                                if (foundClassInLibrary && foundMethodInLibrary) {

                                    // increment the matches found and insert the update number in the pattern
                                    currentMethodPattern.put(1, currentMethodPattern.getInt(1) + 1);

                                    //// remove the occurrence of the method from the set (not the class, it may be useful when detecting attributes)
                                    ////reflectionStrings.get(FileUtil.methods).remove(currentMethodAsString);
                                }
                                // now, if we found both the class and the method as strings NOT in a library
                                if (foundClassNotInLibrary && foundMethodNotInLibrary) {

                                    // increment the matches found and insert the update number in the pattern
                                    currentMethodPattern.put(2, currentMethodPattern.getInt(2) + 1);

                                    //// remove the occurrence of the method from the set (not the class, it may be useful when detecting attributes)
                                    ////reflectionStrings.get(FileUtil.methods).remove(currentMethodAsString);
                                }
                            }


                            // for each attribute in the pattern
                            for (int k = 0; k < patternAttributes.length(); k++) {

                                // get reference to the variable to make code more readable
                                JSONArray currentAttributePattern = patternAttributes.getJSONArray(k);

                                // for each attribute invoked in the current .class file we are analyzing
                                for (String foundAttribute : currentClassProperties.get(2).keySet()) {

                                    // if there is a match
                                    if (currentAttributePattern.getString(0).equals(foundAttribute)) {

                                        // increment the matches found and insert the update number in the pattern
                                        currentAttributePattern.put(1, currentAttributePattern.getInt(1) +
                                                currentClassProperties.get(2).get(foundAttribute)[0]);
                                        currentAttributePattern.put(2, currentAttributePattern.getInt(2) +
                                                currentClassProperties.get(2).get(foundAttribute)[1]);
                                    }
                                }

                                // We try to see if we got something through reflection. This is the criteria: for each attribute,
                                // if we found the string of the attribute AND (the string of the class OR the class itself),
                                // we increment the occurrences for that attribute by 1

                                // get the method and split it into the class and the name of the attribute
                                String[] currentPatternSplit = currentAttributePattern.getString(0).split("\\.");
                                String currentClassAsString = currentPatternSplit[0].replace("/", ".");
                                String currentAttributeAsString = currentPatternSplit[1];

                                // did we find the class and attribute as strings? and in which part of the code?
                                boolean foundClassInLibrary = false;
                                boolean foundAttributeInLibrary = false;
                                boolean foundClassNotInLibrary = false;
                                boolean foundAttributeNotInLibrary = false;

                                // for each class found as a string
                                for (String foundClassesAsStrings : reflectionStrings.get(FileUtil.classes).keySet()) {

                                    // if there is a match in the patterns
                                    if (foundClassesAsStrings.equals(currentClassAsString)) {

                                        if (reflectionStrings.get(FileUtil.classes).get(foundClassesAsStrings)[0] > 0) {
                                            foundClassInLibrary = true;
                                        }
                                        if (reflectionStrings.get(FileUtil.classes).get(foundClassesAsStrings)[1] > 0) {
                                            foundClassNotInLibrary = true;
                                        }

                                    }
                                }

                                // for each method found as a string
                                for (String foundMethodsAsStrings : reflectionStrings.get(FileUtil.attributes).keySet()) {

                                    // if there is a match in the patterns
                                    if (foundMethodsAsStrings.equals(currentAttributeAsString)) {

                                        if (reflectionStrings.get(FileUtil.attributes).get(foundMethodsAsStrings)[0] > 0) {
                                            foundAttributeInLibrary = true;
                                        }
                                        if (reflectionStrings.get(FileUtil.attributes).get(foundMethodsAsStrings)[1] > 0) {
                                            foundAttributeNotInLibrary = true;
                                        }
                                    }
                                }


                                // now, if we found both the class and the attribute as strings in a library
                                if (foundClassInLibrary && foundAttributeInLibrary) {

                                    // increment the matches found and insert the update number in the pattern
                                    currentAttributePattern.put(1, currentAttributePattern.getInt(1) + 1);

                                    //// remove the occurrence of the attribute from the set (not the class, it may be useful when detecting attributes)
                                    ////reflectionStrings.get(FileUtil.attributes).remove(currentMethodAsString);
                                }
                                // now, if we found both the class and the attribute as strings NOT in a library
                                if (foundClassNotInLibrary && foundAttributeNotInLibrary) {

                                    // increment the matches found and insert the update number in the pattern
                                    currentAttributePattern.put(2, currentAttributePattern.getInt(2) + 1);

                                    //// remove the occurrence of the attribute from the set (not the class, it may be useful when detecting attributes)
                                    ////reflectionStrings.get(FileUtil.attributes).remove(currentMethodAsString);
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
}




