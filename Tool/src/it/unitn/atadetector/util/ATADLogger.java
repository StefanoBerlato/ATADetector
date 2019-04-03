package it.unitn.atadetector.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * utility class for logging info, warnings and errors
 */
public final class ATADLogger {

    // the verbosity level: the higher the value the more will be displayed
    // 0: output only errors
    // 1  (default):  output errors and warnings
    // 2:  output log, warnings and errors
    // 3: log literally everything
    public static int verbosityLevel = 1;

    // colors for logging
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private static File logFile = new File(System.getProperty("user.dir") + "/log.txt");


    /**
     * log an error with an exception by invoking the log inner function
     * @param classInvokingLogger the class invoking the logger
     * @param methodInvokingLogger the method of the class invoking the logger
     * @param logErrorMessage the error message to log
     * @param e the exception to log
     */
    public static void logError (String classInvokingLogger, String methodInvokingLogger, String logErrorMessage, Exception e) {

        // composing the message that will be logged
        String messageToLog = ANSI_RED + "[ERROR - " + classInvokingLogger + " (" + methodInvokingLogger + ")]: " + ANSI_RESET +  logErrorMessage;

        // if the exception is not null
        if (e != null) {

            // append the exception
            messageToLog = messageToLog + " -> " + getStringFromException(e);
        }

        // finally log
        log(messageToLog);
    }

    /**
     * log an warning by invoking the log inner function
     * @param classInvokingLogger the class invoking the logger
     * @param methodInvokingLogger the method of the class invoking the logger
     * @param logWarningMessage the warning message to log
     */
    public static void logWarning (String classInvokingLogger, String methodInvokingLogger, String logWarningMessage) {

        // if the verbosity level is 1 ore more
        if (verbosityLevel > 0) {

            // composing the message that will be logged
            String messageToLog = ANSI_YELLOW + "[WARNING - " + classInvokingLogger + " (" + methodInvokingLogger + ")]: " + ANSI_RESET + logWarningMessage;

            // finally log
            log(messageToLog);
        }
    }

    /**
     * log an info by invoking the log inner function
     * @param classInvokingLogger the class invoking the logger
     * @param methodInvokingLogger the method of the class invoking the logger
     * @param logInfoMessage the info message to log
     */
    public static void logInfo (String classInvokingLogger, String methodInvokingLogger, String logInfoMessage) {

        // if the verbosity level is 2 ore more
        if (verbosityLevel > 1) {

            // composing the message that will be logged
            String messageToLog = ANSI_CYAN + "[INFO - " + classInvokingLogger + " (" + methodInvokingLogger + ")]: " + ANSI_RESET + logInfoMessage;

            // finally log
            log(messageToLog);
        }
    }


    /**
     * log verbose by invoking the log inner function
     * @param classInvokingLogger the class invoking the logger
     * @param methodInvokingLogger the method of the class invoking the logger
     * @param logInfoMessage the verbose info message to log
     */
    public static void logVerboseInfo (String classInvokingLogger, String methodInvokingLogger, String logInfoMessage) {

        // if the verbosity level is 3 ore more
        if (verbosityLevel > 2) {

            // composing the message that will be logged
            String messageToLog = ANSI_CYAN + "[INFO VERBOSE - " + classInvokingLogger + " (" + methodInvokingLogger + ")]: " + ANSI_RESET + logInfoMessage;

            // finally log
            log(messageToLog);
        }
    }


    /**
     * this method offers the possibility to directly log a message
     * @param messageToLog the message to log
     */
    public static void directlyLog(String messageToLog) {

        // log the message
        log(messageToLog);
    }

    /**
     * Utility method to get the exception as a string
     * @param e the exception
     * @return the string representing the exception
     */
    private static String getStringFromException (Exception e) {

        // the string to return
        String exceptionMessage;

        // if the exception is not null
        if (e != null) {

            // create a string writer
            StringWriter sw = new StringWriter();

            // create a print writer given the string writer
            PrintWriter pw = new PrintWriter(sw);

            // print the exception message inside the print writer
            e.printStackTrace(pw);

            // get the string from the string writer
            exceptionMessage = sw.toString();
        }
        // otherwise
        else {
            // set the proper message
            exceptionMessage = "exception is null!";
        }

        // finally return
        return exceptionMessage;
    }

    /**
     * method that collects all the log requests, abstracting from the actual log implementation
     * (through standard output, file, ...)
     * @param finalMessageToLog the string message to log
     */
    private static void log (String finalMessageToLog) {

        /*if (!logFile.exists()) {

            try {
                boolean logFileNewFile = logFile.createNewFile();
                if (!logFileNewFile) {
                    System.out.println("error while creating log file");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            Files.write(Paths.get(logFile.getAbsolutePath()), finalMessageToLog.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        // also for now log here
        System.out.println(finalMessageToLog);

    }
}

