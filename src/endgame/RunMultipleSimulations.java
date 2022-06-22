package endgame;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import endgame.input.syntax.ParseException;
import endgame.util.Log;

/** 
 Run a number of simulations, one after the other.
 This class is especially useful when the scenarios are closely related to each other, as a family or group. 
*/
public final class RunMultipleSimulations implements Runnable {
  
  /** 
   Run the simulations.
   The directory from which to start the search for scenario files (.ini) is passed as the first and only argument.
   The directory must exist, and should contain .ini scenario files, either at or below the root directory.
  */
  public static void main(String... args) throws ParseException, IOException {
    /*
    If you prefer, you can also run this by simply hard-coding the folder location
    String folder = "C:\\myworkspace\\end-game\\scenario";
     */
    String folder = args[0];
    RunMultipleSimulations runMultipleSims = new RunMultipleSimulations(folder);
    runMultipleSims.run();
  }
  
  /** Pass the root directory, where to start the search for scenario files ending with .ini. */
  public RunMultipleSimulations(String root) {
    this.root = root;
  }

  /**
   Starting with a directory, search that directory and all its sub-directories (recursively) for files that end with '.ini'. 
   Those are treated as scenario files.
   Run each scenario file in sequence, as it is found.
  */
  @Override public void run() {
    double start = System.currentTimeMillis();
    Log.forceConsole("Searching for multiple scenarios under " + root);
    Log.enableLoggingToConsole(false);
    File rootDir = new File(root);
    if (!rootDir.exists()) {
      Log.log("The directory doesn't exist:" + rootDir);
    }
    else if (!rootDir.canRead()) {
      Log.log("Directory cannot be be read: " + rootDir);
    }
    else if (!rootDir.isDirectory()) {
      Log.log("Not a directory: " + rootDir);
    }
    else {
      try {
        //process the directory tree
        FileVisitor<Path> fileProcessor = new FileWalker();
        Files.walkFileTree(Paths.get(root), fileProcessor);      
      }
      catch(Throwable ex) {
        Log.error(ex.toString(), ex);
      }
    }
    double executionTime = System.currentTimeMillis() - start;
    Log.enableLoggingToConsole(true);
    Log.forceConsole("Execution time: " + executionTime/1000.0 + "s");
    Log.forceConsole("Done.");
  }
  
  //PRIVATE
    
  private String root = "";
  
  private static final class FileWalker extends SimpleFileVisitor<Path> {
    @Override public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
      if (path.toString().endsWith(".ini")) {
        try {
          RunSimulation runScenario = new RunSimulation(path.toString());
          Log.forceConsole("Scenario: " + path.toString());
          runScenario.run();
        } 
        catch (ParseException e) {
          e.printStackTrace();
        }
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
