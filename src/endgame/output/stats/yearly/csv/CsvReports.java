package endgame.output.stats.yearly.csv;

import java.io.File;
import java.util.Map;

import endgame.Scenario;
import endgame.output.stats.yearly.History;
import endgame.util.Log;

public final class CsvReports {
  
  /** In the case of a single iteration, only 1 history will be present. */
  public void save(Scenario sim, Map<Integer, History> histories, String configFile) {
    int numHistories = histories.keySet().size();
    Log.log("Saving reports. Number of histories: "  + numHistories + ".");
    if (numHistories == 1) {
      saveReportsForSingleHistory(sim, configFile, histories.values().stream().findFirst().get());
    }
    else {
      saveReportsForMultipleHistories(sim, configFile, histories);
    }
  }
  
  /** 
   Compact summary reports for important data.
   Intended only for the case in which only a single history has been generated. 
  */
  private void saveReportsForSingleHistory(Scenario sim, String configFile, History history) {
    File scenarioFile = new File(configFile);
    
    CsvCashAndTaxSummary cashTaxEtc = new CsvCashAndTaxSummary();
    cashTaxEtc.saveToCsv(history, scenarioFile, sim);
    
    CsvCashFlows cashFlows = new CsvCashFlows();
    cashFlows.saveToCsv(history, scenarioFile, sim);
  }
  
  /**
   Details of each data point of interest, across all histories.
   Intended only for the case in which only a multiple histories have been generated, AND randomness has been used in the model. 
   The idea is that the end user can import this data into a spreadsheet, and generate graphs.
   For a given year, the the graphs (usually just using points, not lines) will have a a lot of spread for a given year. 
  */
  private void saveReportsForMultipleHistories(Scenario sim, String configFile, Map<Integer, History> histories) {
    CsvMultipleHistories csv = new CsvMultipleHistories();
    csv.save("histories-net-cash", histories, new File(configFile), sim, CsvMultipleHistories::netCash);
    csv.save("histories-tax-payable", histories, new File(configFile), sim, CsvMultipleHistories::taxPayable);
    csv.save("histories-gross-cash", histories, new File(configFile), sim, CsvMultipleHistories::grossCash);
    
    csv.save("histories-cpp", histories, new File(configFile), sim, CsvMultipleHistories::cpp);
    csv.save("histories-oas", histories, new File(configFile), sim, CsvMultipleHistories::oas);
    csv.save("histories-gis", histories, new File(configFile), sim, CsvMultipleHistories::gis);
    
    csv.save("histories-dividends", histories, new File(configFile), sim, CsvMultipleHistories::dividends);
    csv.save("histories-liquidation", histories, new File(configFile), sim, CsvMultipleHistories::liquidation);
    csv.save("histories-interest", histories, new File(configFile), sim, CsvMultipleHistories::interest);
  }
}
