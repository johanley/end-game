package endgame.output.stats.yearly.csv;

import java.io.File;
import java.util.List;

import endgame.util.Log;
import endgame.util.Util;

/** 
 Helper class for outputting .csv files. 
 <P>The output .csv file is intended for import into a charting tool, such as MS Excel or Open Office.
 
 <P>The data saved is determined by the caller.
 
 <P>Some reports are only appropriate when the number of iterations is 1; that is, when the scenario is not 
 run multiple times because of randomly generated security prices.
*/
final class CsvFile {
  
  CsvFile(String conventionalFileName, File scenarioFile){
    this.conventionalFileName = conventionalFileName;
    this.scenarioFile = scenarioFile;
  }

  /** Field separator: {@value}. */
  static final String SEP = ",";

  /** Return a line of comma-separated items. * */
  String lineFrom(Object... items) {
    String result = "";
    for(Object item : items) {
      result = result + item.toString() + CsvFile.SEP;
    }
    return result.substring(0, result.length()-1); //chop off final sep char
  }
  
  /** Return a line of comma-separated items. Nulls are rendered as empty. */
  String lineFrom(List<Object> items) {
    String result = "";
    for(Object item : items) {
      String dataPoint = item != null ? item.toString() : "";
      result = result + dataPoint + CsvFile.SEP;
    }
    return result.substring(0, result.length()-1); //chop off final sep char
  }

  /** 
   Save the lines to the .csv file. 
   The output .csv file is placed in the same directory as the input scenario file.
  <P>The various data series are in columns.
  */
  void save(List<String> lines) {
    String file = fileBesideThe(scenarioFile); 
    Log.log("Writing report to " + file);
    Util.saveLinesToFile(file, lines);    
  }
  
  private String conventionalFileName = "";
  private File scenarioFile;
  
  /** Return the full name of the file, with the complete path. */
  private String fileBesideThe(File scenarioFile) {
    String shortScenarioFileName = scenarioFile.getName();
    int firstDash = shortScenarioFileName.indexOf("-");
    String shortLogFileName = shortScenarioFileName.substring(0, firstDash) + "-" + conventionalFileName + ".csv";
    String result = scenarioFile.getParent() + File.separator + shortLogFileName;
    return result;
  }
}
