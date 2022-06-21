package endgame.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import endgame.Scenario;

/** 
 Centralized logging policies.
 This implementation is likely not appropriate for a servlet environment.
*/
public final class Log {
  
  /*
   * This implementation might be changed to use the JDK's logging packages in the future, but 
   * for the moment it seems fine.
   */
  
  /** Log a message to the enabled outputs. */
  public static void log(Object thing) {
    logit(thing.toString());
  }
  
  /** 
   Toggle the logging to the console (standard out).
   True by default. 
   You may wish to disable logging to the console if:
   <ul> 
    <li>you are running multiple scenarios
    <li>the scenario has a large number of histories to run 
    <li>you wish to run the scenario more quickly
   </ul>
  */
  public static void enableLoggingToConsole(Boolean toggle) {
    toConsole = toggle;
  }

  /** Called only at the end of running a scenario. Uses UTF-8 encoding to write to a file. */
  public static void flushLogBufferFor(Scenario scenario, File scenarioFile) {
    String logFile = logFileNameFrom(scenarioFile);
    log("Writing to log file " + logFile);
    Util.saveLinesToFile(logFile, lineBuffer);
  }

  /**
   Log to the console, regardless of any other settings. 
   Rarely called. You should almost always prefer the {@link #log(Object)} method instead.
   As a side-effect, this method will also save the log message to the log-file buffer.
  */
  public static void forceConsole(Object thing) {
    System.out.println(thing.toString());
    addToBuffer(thing);
  }

  /** The user should always see a stack trace if one is created. */
  public static void error(Object thing, Throwable ex) {
    forceConsole(thing);
    forceConsole(Log.theStackTrace(ex));
  }
  
  // PRIVATE 

  /** 
   Return the full path of a conventional log file, using a hard-coded policy.
   
   <P>Example: '101.6-blah-blah.ini' becomes '101.6.log' (in the same directory as the scenario file).
   This is a very specific policy for the name of the log file.
   It depends on the location of the first '-' character.
   If it displeases you, just change it to match your own style. 
  */
  private static String logFileNameFrom(File scenarioFile) {
    String shortScenarioFileName = scenarioFile.getName();
    int firstDash = shortScenarioFileName.indexOf("-");
    String shortLogFileName = shortScenarioFileName.substring(0, firstDash) + ".log";
    return scenarioFile.getParent() + File.separator + shortLogFileName;
  }
  
  /** Enabled by default. */
  private static Boolean toConsole = Boolean.TRUE;
  
  /** 
   Records the logging in a buffer, which may or may not be later saved in a file.
   If the caller elects to not use a logging file, then the content is simply abandoned 
   at the end of execution. 
   The full logging output of a scenario will be sent to this buffer.
   The buffer is cleared each time it is written to a file. 
  */
  private static List<String> lineBuffer = new ArrayList<>();
  
  private static void logit(Object thing) {
    if (toConsole) {
      forceConsole(thing);
    }
    addToBuffer(thing);
  }
  
  private static void addToBuffer(Object thing) {
    lineBuffer.add(thing.toString());
  }

  private static String theStackTrace(Throwable throwable) {
    Writer result = new StringWriter();
    PrintWriter printWriter = new PrintWriter(result);
    throwable.printStackTrace(printWriter);
    return result.toString();
  }
}
