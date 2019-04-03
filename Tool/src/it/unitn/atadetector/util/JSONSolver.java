package it.unitn.atadetector.util;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * This class is responsible for applying the boolean formulas
 * in order to decide whether a protection was detected or not
 * Obviously, the static initialization of this class heavily
 * depends on which JSON you are using. In fact, this is built to
 * parse the default JSON this program is shipped with
 */
public class JSONSolver {


    // variables for logging
    private static final String className = "JSONSolver";
    private static final String applyFormulasOnJSON = "applyFormulasOnJSON";


    /**
     * This methods receives the JSON report, parses it and applies the proper formula
     * This implementation is the default one, relying in the formula referenced in the paper
     * @param report the JSON report filled with occurrences found by the detector
     * @return the short version of the report (with only the occurrences of the protections)
     */
    public static JSONObject applyFormulasOnJSON(JSONObject report) {

        // the short versions of the report
        JSONObject shortReportVersion = new JSONObject();

        // get the array of categories
        JSONArray categories = report.getJSONArray(FileUtil.protectionCategory);

        // now, for each protection category (e.g. anti-tampering, anti-debugging, ...)
        for (int i = 0; i < categories.length(); i++) {

            // get the protection category object to later retrieve the values of the
            // protection category (name, list of protections, ...)
            JSONObject protectionCategory = categories.getJSONObject(i);

            // log info
            ATADLogger.logVerboseInfo(className, applyFormulasOnJSON, "parsing protection patterns " +
                    "of category " + protectionCategory.getString(FileUtil.categoryName) + "...");

            // get the list of protections
            JSONArray protections = protectionCategory.getJSONArray(FileUtil.protection);

            // now, for each different kind of protection
            for (int j = 0; j < protections.length(); j++) {

                // get the protection pattern object to later retrieve the values
                // (name, java/native level protection patterns, ...)
                JSONObject protection = protections.getJSONObject(j);

                // the name of the protection
                JSONArray protectionJSONArray = protection.getJSONArray(FileUtil.nameOfTheProtection);

                // log info
                ATADLogger.logVerboseInfo(className, applyFormulasOnJSON, "applying formula on " +
                        "protection " + protectionJSONArray.getString(0));

                // get the java and native level patterns
                JSONObject javaLevelPatterns = protection.getJSONArray(FileUtil.javaLevelPatterns).getJSONObject(0);
                JSONObject nativeLevelPatterns = protection.getJSONArray(FileUtil.nativeLevelPatterns).getJSONObject(0);

                // get the patterns for the classes, methods, attributes and strings
                JSONArray patternClasses = javaLevelPatterns.getJSONArray(FileUtil.classes);
                JSONArray patternMethods = javaLevelPatterns.getJSONArray(FileUtil.methods);
                JSONArray patternAttributes = javaLevelPatterns.getJSONArray(FileUtil.attributes);
                JSONArray patternStrings = javaLevelPatterns.getJSONArray(FileUtil.strings);

                // get the patterns for the imported symbols and the strings
                JSONArray importedSymbols = nativeLevelPatterns.getJSONArray(FileUtil.importedSymbols);
                JSONArray strings = nativeLevelPatterns.getJSONArray(FileUtil.strings);

                // 1 is with libraries, 2 is without (JAVA)
                for (int l = 1; l <= 2; l++) {

                    // now switch formula depending on the protection being analyzed
                    switch (protectionJSONArray.getString(0)) {

                        case FileUtil.signatureChecking:

                            // if the formula is satisfied (JAVA)
                            if (
                                    ((patternStrings.getJSONArray(0).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(1).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(2).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(3).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(4).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(5).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(6).getInt(l) > 0 ||
                                     patternStrings.getJSONArray(7).getInt(l) > 0)
                                                &&
                                    (patternMethods.getJSONArray(0).getInt(l) > 0 ||
                                     patternMethods.getJSONArray(1).getInt(l) > 0 ||
                                     patternMethods.getJSONArray(2).getInt(l) > 0)
                                                &&
                                   ((patternMethods.getJSONArray(7).getInt(l) > 0 ||
                                     patternMethods.getJSONArray(8).getInt(l) > 0)
                                            ||
                                    (patternAttributes.getJSONArray(0).getInt(l) > 0 ||
                                     patternAttributes.getJSONArray(1).getInt(l) > 0 ||
                                    ((patternAttributes.getJSONArray(2).getInt(l) -
                                    ((patternStrings.getJSONArray(8).getInt(l) == 0 ||
                                      patternStrings.getJSONArray(9).getInt(l) == 0 ||
                                      patternStrings.getJSONArray(10).getInt(l) == 0) ? 0 : 1)
                                    ) > 0)
                                    )))
                            )
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.signatureChecking + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.signatureChecking + "_JAVA", 0);
                            }

                            // if the formula is satisfied (NATIVE)
                            if (
                                    (strings.getJSONArray(0).getInt(1) > 0 &&
                                     strings.getJSONArray(1).getInt(1) > 0 &&
                                     strings.getJSONArray(2).getInt(1) > 0 &&
                                     strings.getJSONArray(3).getInt(1) > 0 &&
                                     strings.getJSONArray(4).getInt(1) > 0 &&
                                     strings.getJSONArray(5).getInt(1) > 0)
                            ) {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.nativeLevelPatterns).put(1, 1);
                                shortReportVersion.put(FileUtil.signatureChecking + "_NATIVE", 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.signatureChecking + "_NATIVE", 0);
                            }
                            break;


                        case FileUtil.codeIntegrityChecking:

                            // if the formula is satisfied (JAVA)
                            if
                            (
                                 (((((patternStrings.getJSONArray(0).getInt(l) -
                                    ((patternStrings.getJSONArray(5).getInt(l) == 0) ? 0 : 1)
                                      ) > 0) ||
                                      patternStrings.getJSONArray(1).getInt(l) > 0 ||
                                      patternStrings.getJSONArray(2).getInt(l) > 0 ||
                                      patternStrings.getJSONArray(3).getInt(l) > 0 ||
                                      patternStrings.getJSONArray(4).getInt(l) > 0))
                                            &&
                                    ((patternMethods.getJSONArray(0).getInt(l) > 0)
                                     ||
                                     (((patternAttributes.getJSONArray(0).getInt(l) -
                                     ((patternStrings.getJSONArray(5).getInt(l) == 0) ? 0 : 1)
                                     ) > 0)))
                                            &&
                                    ((patternMethods.getJSONArray(1).getInt(l) > 0 ||
                                      patternMethods.getJSONArray(2).getInt(l) > 0)
                                     ||
                                     (patternMethods.getJSONArray(3).getInt(l) > 0 ||
                                      patternMethods.getJSONArray(4).getInt(l) > 0 ||
                                      patternMethods.getJSONArray(5).getInt(l) > 0 ||
                                      patternMethods.getJSONArray(6).getInt(l) > 0)))
                                            &&
                                     (patternStrings.getJSONArray(5).getInt(l) == 0)
                            )
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.codeIntegrityChecking + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.codeIntegrityChecking + "_JAVA", 0);
                            }
                            break;


                        case FileUtil.installerVerification:

                            // if the formula is satisfied (JAVA)
                            if ((patternStrings.getJSONArray(0).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(1).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(2).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(3).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(4).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(5).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(6).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(7).getInt(l) > 0)
                                    &&
                                (patternMethods.getJSONArray(0).getInt(l) > 0))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.installerVerification + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.installerVerification + "_JAVA", 0);
                            }
                            break;


                        case FileUtil.safetyNetAttestation:

                            // if the formula is satisfied (JAVA)
                            if ((patternMethods.getJSONArray(0).getInt(l) > 0 ||
                                 patternMethods.getJSONArray(1).getInt(l) > 0 ||
                                 patternMethods.getJSONArray(2).getInt(l) > 0)
                                    ||
                                (patternClasses.getJSONArray(0).getInt(l) > 0 ||
                                 patternClasses.getJSONArray(1).getInt(l) > 0 ||
                                 patternClasses.getJSONArray(2).getInt(l) > 0 ||
                                 patternClasses.getJSONArray(3).getInt(l) > 0))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.safetyNetAttestation + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.safetyNetAttestation + "_JAVA", 0);
                            }
                            break;


                        case FileUtil.emulatorDetection:

                            // if the formula is satisfied (JAVA)
                            if ((((( patternStrings.getJSONArray(0).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(1).getInt(l) > 0)
                                    &&
                                   (patternStrings.getJSONArray(2).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(3).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(4).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(5).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(6).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(7).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(8).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(9).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(10).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(11).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(12).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(13).getInt(l) > 0))
                                        ||
                                   (patternAttributes.getJSONArray(0).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(1).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(2).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(3).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(4).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(5).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(6).getInt(l) > 0 ||
                                    patternAttributes.getJSONArray(7).getInt(l) > 0))
                                            &&
                                   (patternStrings.getJSONArray(14).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(15).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(16).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(17).getInt(l) > 0 ||
                                    //patternStrings.getJSONArray(18).getInt(l) > 0 ||
                                    //patternStrings.getJSONArray(19).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(20).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(21).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(22).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(23).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(24).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(25).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(26).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(27).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(28).getInt(l) > 0))
                                                ||
                                   (patternStrings.getJSONArray(29).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(30).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(31).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(32).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(33).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(34).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(35).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(36).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(37).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(38).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(39).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(40).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(41).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(42).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(43).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(44).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(45).getInt(l) > 0 ||
                                    patternStrings.getJSONArray(46).getInt(l) > 0)
                                                ||
                                    (patternMethods.getJSONArray(0).getInt(l) > 0 ))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.emulatorDetection + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.emulatorDetection + "_JAVA", 0);
                            }

                            // if the formula is satisfied (NATIVE)
                            if (((  strings.getJSONArray(0).getInt(1) > 0 ||
                                    strings.getJSONArray(1).getInt(1) > 0 ||
                                    strings.getJSONArray(2).getInt(1) > 0 ||
                                    strings.getJSONArray(3).getInt(1) > 0 ||
                                    strings.getJSONArray(4).getInt(1) > 0 ||
                                    strings.getJSONArray(5).getInt(1) > 0 ||
                                    strings.getJSONArray(6).getInt(1) > 0 ||
                                    strings.getJSONArray(7).getInt(1) > 0 ||
                                    strings.getJSONArray(8).getInt(1) > 0 ||
                                    strings.getJSONArray(9).getInt(1) > 0 ||
                                    strings.getJSONArray(10).getInt(1) > 0 ||
                                    strings.getJSONArray(11).getInt(1) > 0)
                                    &&
                                   (strings.getJSONArray(12).getInt(1) > 0 ||
                                    strings.getJSONArray(13).getInt(1) > 0 ||
                                    strings.getJSONArray(14).getInt(1) > 0 ||
                                    strings.getJSONArray(15).getInt(1) > 0 ||
                                    //strings.getJSONArray(16).getInt(1) > 0 ||
                                    //strings.getJSONArray(17).getInt(1) > 0 ||
                                    strings.getJSONArray(18).getInt(1) > 0 ||
                                    strings.getJSONArray(19).getInt(1) > 0 ||
                                    strings.getJSONArray(20).getInt(1) > 0 ||
                                    strings.getJSONArray(21).getInt(1) > 0 ||
                                    strings.getJSONArray(22).getInt(1) > 0 ||
                                    strings.getJSONArray(23).getInt(1) > 0 ||
                                    strings.getJSONArray(24).getInt(1) > 0 ||
                                    strings.getJSONArray(25).getInt(1) > 0 ||
                                    strings.getJSONArray(26).getInt(1) > 0))
                                        ||
                                   (strings.getJSONArray(27).getInt(1) > 0 ||
                                    strings.getJSONArray(28).getInt(1) > 0 ||
                                    strings.getJSONArray(29).getInt(1) > 0 ||
                                    strings.getJSONArray(30).getInt(1) > 0 ||
                                    strings.getJSONArray(31).getInt(1) > 0 ||
                                    strings.getJSONArray(32).getInt(1) > 0 ||
                                    strings.getJSONArray(33).getInt(1) > 0 ||
                                    strings.getJSONArray(34).getInt(1) > 0 ||
                                    strings.getJSONArray(35).getInt(1) > 0 ||
                                    strings.getJSONArray(36).getInt(1) > 0 ||
                                    strings.getJSONArray(37).getInt(1) > 0 ||
                                    strings.getJSONArray(38).getInt(1) > 0 ||
                                    strings.getJSONArray(39).getInt(1) > 0 ||
                                    strings.getJSONArray(40).getInt(1) > 0 ||
                                    strings.getJSONArray(41).getInt(1) > 0 ||
                                    strings.getJSONArray(42).getInt(1) > 0 ||
                                    strings.getJSONArray(43).getInt(1) > 0 ||
                                    strings.getJSONArray(44).getInt(1) > 0)) {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.nativeLevelPatterns).put(1, 1);
                                shortReportVersion.put(FileUtil.emulatorDetection + "_NATIVE", 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.emulatorDetection + "_NATIVE", 0);
                            }
                            break;


                        case FileUtil.dynamicAnalysisFrameworkDetection:

                            // if the formula is satisfied (JAVA)
                            if ((patternStrings.getJSONArray(0).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(1).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(2).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(3).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(4).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(5).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(6).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(7).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(8).getInt(l) > 0 ||
                                 patternStrings.getJSONArray(9).getInt(l) > 0)
                                            ||
                                (((patternAttributes.getJSONArray(0).getInt(l) > 0)
                                    ||
                                (patternMethods.getJSONArray(3).getInt(l) > 0))
                                        &&
                                (patternMethods.getJSONArray(4).getInt(l) > 0)
                                        &&
                                (patternStrings.getJSONArray(11).getInt(l) > 0 ||
                                patternStrings.getJSONArray(12).getInt(l) > 0 ||
                                patternStrings.getJSONArray(13).getInt(l) > 0 ||
                                patternStrings.getJSONArray(14).getInt(l) > 0 ||
                                patternStrings.getJSONArray(15).getInt(l) > 0)))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.dynamicAnalysisFrameworkDetection + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.dynamicAnalysisFrameworkDetection + "_JAVA", 0);
                            }

                            // if the formula is satisfied (NATIVE)
                            if ((strings.getJSONArray(0).getInt(1) > 0 ||
                                 strings.getJSONArray(1).getInt(1) > 0 ||
                                 strings.getJSONArray(2).getInt(1) > 0 ||
                                 strings.getJSONArray(3).getInt(1) > 0 ||
                                 strings.getJSONArray(4).getInt(1) > 0 ||
                                 strings.getJSONArray(5).getInt(1) > 0 ||
                                 strings.getJSONArray(6).getInt(1) > 0 ||
                                 strings.getJSONArray(7).getInt(1) > 0 ||
                                 strings.getJSONArray(8).getInt(1) > 0 ||
                                 strings.getJSONArray(9).getInt(1) > 0)) {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.nativeLevelPatterns).put(1, 1);
                                shortReportVersion.put(FileUtil.dynamicAnalysisFrameworkDetection + "_NATIVE", 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.dynamicAnalysisFrameworkDetection + "_NATIVE", 0);
                            }
                            break;


                        case FileUtil.debuggerDetection:

                            // if the formula is satisfied (JAVA)
                            if ((patternMethods.getJSONArray(0).getInt(l) > 0 ||
                                 patternMethods.getJSONArray(1).getInt(l) > 0 ||
                                 patternMethods.getJSONArray(2).getInt(l) > 0)
                                    ||
                                ((patternStrings.getJSONArray(0).getInt(l) > 0)
                                        &&
                                ((patternStrings.getJSONArray(1).getInt(l) > 0)
                                            ||
                                 (patternStrings.getJSONArray(2).getInt(l) > 0 &&
                                  patternStrings.getJSONArray(3).getInt(l) > 0))))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.debuggerDetection + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.debuggerDetection + "_JAVA", 0);
                            }

                            // if the formula is satisfied (NATIVE)
                            if ((importedSymbols.getJSONArray(0).getInt(1) > 0 &&
                                 importedSymbols.getJSONArray(1).getInt(1) > 0 &&
                                 importedSymbols.getJSONArray(2).getInt(1) > 0 &&
                                 importedSymbols.getJSONArray(3).getInt(1) > 0)
                                    ||
                                ((strings.getJSONArray(0).getInt(1) > 0)
                                    &&
                                ((strings.getJSONArray(1).getInt(1) > 0)
                                    ||
                                 (strings.getJSONArray(2).getInt(1) > 0 &&
                                  strings.getJSONArray(3).getInt(1) > 0)))) {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.nativeLevelPatterns).put(1, 1);
                                shortReportVersion.put(FileUtil.debuggerDetection + "_NATIVE", 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.debuggerDetection + "_NATIVE", 0);
                            }
                            break;


                        case FileUtil.debuggableStatusDetection:

                            // if the formula is satisfied (JAVA)
                            if (((patternStrings.getJSONArray(0).getInt(l) > 0)
                                    &&
                                 (patternStrings.getJSONArray(1).getInt(l) > 0 ||
                                  patternStrings.getJSONArray(2).getInt(l) > 0))
                                        ||
                                 (patternAttributes.getJSONArray(1).getInt(l) > 0 ||
                                  patternAttributes.getJSONArray(2).getInt(l) > 0))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.debuggableStatusDetection + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.debuggableStatusDetection + "_JAVA", 0);
                            }


                            // if the formula is satisfied (NATIVE)
                            if (strings.getJSONArray(0).getInt(1) > 0) {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.nativeLevelPatterns).put(1, 1);
                                shortReportVersion.put(FileUtil.debuggableStatusDetection + "_NATIVE", 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.debuggableStatusDetection + "_NATIVE", 0);
                            }
                            break;


                        case FileUtil.timeChecks:

                            // if the formula is satisfied (JAVA)
                            if (patternMethods.getJSONArray(0).getInt(l) > 0 ||
                                patternMethods.getJSONArray(1).getInt(l) > 0 ||
                                patternMethods.getJSONArray(2).getInt(l) > 0 ||
                                patternMethods.getJSONArray(3).getInt(l) > 0 ||
                                patternMethods.getJSONArray(4).getInt(l) > 0 ||
                                patternMethods.getJSONArray(5).getInt(l) > 0 ||
                                patternMethods.getJSONArray(6).getInt(l) > 0)
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.javaLevelPatterns).put(l, 1);
                                shortReportVersion.put(FileUtil.timeChecks + "_JAVA_" + l, 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.timeChecks + "_JAVA", 0);
                            }
                            break;


                        case FileUtil.alteringDebuggerMemoryStructure:

                            // if the formula is satisfied (NATIVE)
                            if ((importedSymbols.getJSONArray(0).getInt(1) > 0)
                                    ||
                                (strings.getJSONArray(0).getInt(1) > 0 &&
                                 strings.getJSONArray(1).getInt(1) > 0))
                            {

                                // set the protection count to 1
                                protection.getJSONArray(FileUtil.nativeLevelPatterns).put(1, 1);
                                shortReportVersion.put(FileUtil.alteringDebuggerMemoryStructure + "_NATIVE", 1);
                            }
                            else {

                                shortReportVersion.put(FileUtil.alteringDebuggerMemoryStructure + "_NATIVE", 0);
                            }
                            break;

                        default:
                    }
                }
            }
        }

        // finally, return
        return shortReportVersion;
    }
}
