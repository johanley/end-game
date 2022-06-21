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
 Generate a .csv file for yearly totals for cash generated, taxes paid, taxable income, etc.
 In most cases, the main output of a scenario is the cash it generates each year, minus taxes paid.
 
 <P>This report applies to a single history only.  
*/
final class CsvCashAndTaxSummary {

  /** {@value} */
  static final String FILE_NAME = "cash-and-tax-summary";
  
  /** 
   Save stats as a .csv file.
   
   <P>A hard-coded naming convention is used here. 
   A scenario file named 101.6-blah.ini results in a file named 101.6-net-gross-tax-networth.csv.
  */
  void saveToCsv(History history, File scenarioFile, Scenario sim) {
    List<String> lines = new ArrayList<>();
    int startYear = new DateTime(sim.startDate).getYear();
    int numYears = history.numYears();
    CsvFile csv = new CsvFile(FILE_NAME, scenarioFile);
    Map<Integer, Double> relProbSurvival = relativeProbOfSurvival(sim);    
    lines.add(csv.lineFrom("Year", "Age", "Net Cash", "Cash Generated", "Taxable Income", "Tax Payable", "Net Worth", "Survival Chances"));
    for(int year = startYear; year < (startYear + numYears); ++year) {
      Integer age = year - Integer.valueOf(sim.dateOfBirth.substring(0, 4));
      Money cashGenerated = history.cashFlow.get(year).cashFlow();
      Money taxPayable = history.taxSummary.get(year).taxPayable;
      Money taxableIncome = history.taxSummary.get(year).taxableIncome;
      Money netCash = cashGenerated.minus(taxPayable);
      Money netWorth = history.accountSet.get(year).netWorth();
      Double probOfSurvival = relProbSurvival.get(year);
      String line = csv.lineFrom(year, age, netCash, cashGenerated, taxableIncome, taxPayable, netWorth, probOfSurvival);
      lines.add(line);
    }
    csv.save(lines);
  }
  
  private Map<Integer, Double> relativeProbOfSurvival(Scenario sim) {
    Survival survival = new Survival();
    return survival.relativeProbabilityOfSurvival(sim, Optional.empty());
  }
}
