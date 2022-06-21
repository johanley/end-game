package endgame.output.stats.yearly.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import endgame.Scenario;
import endgame.model.Money;
import endgame.output.stats.yearly.History;
import hirondelle.date4j.DateTime;

/** Save reports for multiple histories. */
final class CsvMultipleHistories {
  
  void save(String fileName, Map<Integer, History> histories, File scenarioFile, Scenario sim, BiFunction<History, Integer, Money> function) {
    CsvFile csv = new CsvFile(fileName, scenarioFile);
    List<String> lines = new ArrayList<>();
    int startYear = new DateTime(sim.startDate).getYear();
    int endYear = new DateTime(sim.endDate).getYear();
    for(Integer year = startYear; year <= endYear; ++year) {
      List<Object> itemsInLine = new ArrayList<>();
      itemsInLine.add(year);
      for(int iteration= 1; iteration <= sim.numIterations; ++iteration) {
        History history = histories.get(iteration);
        
        Money money = function.apply(history, year);
        
        itemsInLine.add(money);
      }
      String line = csv.lineFrom(itemsInLine);
      lines.add(line);
    }
    csv.save(lines);
  }
  
  
  static Money netCash(History history, Integer year) {
    Money result = null;
    if (history.cashFlow.get(year) != null) {
      Money cashGenerated = history.cashFlow.get(year).cashFlow();
      Money taxPayable = history.taxSummary.get(year).taxPayable;
      result = cashGenerated.minus(taxPayable); //net cash
    }
    return result;
  }
  static Money taxPayable(History history, Integer year) {
    return history.taxSummary.get(year) == null ? null : history.taxSummary.get(year).taxPayable; 
  }
  static Money grossCash(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).cashFlow();
  }
  static Money cpp(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).cpp;
  }
  static Money oas(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).oas;
  }
  static Money gis(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).gis;
  }
  static Money dividends(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).dividends;
  }
  static Money liquidation(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).liquidationProceeds;
  }
  static Money interest(History history, Integer year) {
    return history.cashFlow.get(year) == null ? null : history.cashFlow.get(year).interest;
  }
}
