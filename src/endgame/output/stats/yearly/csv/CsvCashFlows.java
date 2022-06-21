package endgame.output.stats.yearly.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import endgame.Scenario;
import endgame.model.Money;
import endgame.output.stats.yearly.History;
import endgame.survival.Survival;
import hirondelle.date4j.DateTime;

/**
 Generate a .csv file for yearly cash flows (in-flows), showing you the various ways 
 in which cash is being generated.
 
 <P>This report applies to a single history only.  
*/
final class CsvCashFlows {

  /** {@value} */
  static final String FILE_NAME = "cash-flows";
  
  /** 
   Save stats as a .csv file.
  
   <P>A hard-coded naming convention is used here. 
   A scenario file named 101.6-blah.ini results in a file named 101.6-cash-flows.csv.
  */
  void saveToCsv(History history, File scenarioFile, Scenario sim) {
    List<String> lines = new ArrayList<>();
    int startYear = new DateTime(sim.startDate).getYear();
    int numYears = history.numYears();
    CsvFile csv = new CsvFile(FILE_NAME, scenarioFile);
    Map<Integer, Double> relProbSurvival = relativeProbOfSurvival(sim);    
    lines.add(csv.lineFrom("Year", "Age", "CPP", "OAS", "GIS", "Pension", "Dividends", "Liquidation", "Interest", "Survival Chances"));
    for(int year = startYear; year < (startYear + numYears); ++year) {
      Integer age = year - Integer.valueOf(sim.dateOfBirth.substring(0, 4));
      Money cpp = history.cashFlow.get(year).cpp;
      Money oas = history.cashFlow.get(year).oas;
      Money gis = history.cashFlow.get(year).gis;
      Money pension = history.cashFlow.get(year).pension;
      Money dividends = history.cashFlow.get(year).dividends;
      Money liquidation = history.cashFlow.get(year).liquidationProceeds;
      Money interest = history.cashFlow.get(year).interest;
      Double probOfSurvival = relProbSurvival.get(year);
      String line = csv.lineFrom(year, age, cpp, oas, gis, pension, dividends, liquidation, interest, probOfSurvival);
      lines.add(line);
    }
    csv.save(lines);
  }
  
  private Map<Integer, Double> relativeProbOfSurvival(Scenario sim) {
    Survival survival = new Survival();
    return survival.relativeProbabilityOfSurvival(sim, Optional.empty());
  }
}
