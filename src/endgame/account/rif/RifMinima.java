package endgame.account.rif;

import java.util.LinkedHashMap;
import java.util.Map;

import endgame.model.Money;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** 
 Yearly minimum withdrawals from your RIF account.
 
 The percent that you must withdraw is fixed by a formula/table which depends on your age on January 1. 
 The final dollar amount is found by multiplying the percent by the market value of your 
 RIF at the end of business of the previous year (or the start of business of the current year, 
 if you wish).
 
 <P>At the end of the year, there are two tasks: make sure 
 the minimum has been reached for this year, and calc the new minimum for next year.
 
 <P>This object needs to be long-lived because it's backed by data read upon start-up.
 
 <P>Please see {@link endgame.account.rsp} for more information.
*/
public final class RifMinima {
  
  public RifMinima(String dateOfBirth) {
    this.dateOfBirth = new DateTime(dateOfBirth);
  }
  
  /** 
   Add a row of the table that computes percentage of the RIF account value.
   This method must be called upon startup.
   This data is in the scenario file.
  */
  public void addTableRow(Integer age, Double percent) {
    rifMinimumPercentages.put(age, percent);
  }

  /** 
   Compute the minimum RIF withdrawal for the given year.
   If the year is before the year-of-rsp-conversion + 1, then the minimum is 0. 
  */
  public Money compute(Money rifValueOnJan1, Integer year, DateTime rspToRifConversionDate) {
    Money result = Consts.ZERO;
    if(isMinimumApplicable(year, rspToRifConversionDate)) {
      Integer ageOnJan1 = year - dateOfBirth.getYear();
      Double fraction = 0.0D;
      //some hard-coded constants used here!
      if (ageOnJan1 < 71) {
        fraction = 1.0D / (90 - ageOnJan1);
      }
      else if (ageOnJan1 < 95) {
        fraction = rifMinimumPercentages.get(ageOnJan1);
      }
      else {
        fraction = 0.20D;
      }
      result = rifValueOnJan1.times(fraction);
    }
    return result;
  }
  
  @Override public String toString() {
    return "Rif Minima date-of-birth:" + dateOfBirth + " " + rifMinimumPercentages;   
  }
  
  /** Hard-coded convenience method for testing only. */
  private static final RifMinima forTesting() {
    RifMinima result = new RifMinima("1962-03-01");
    result.addTableRow(71, 0.0528D);
    result.addTableRow(72, 0.0540D);
    result.addTableRow(73, 0.0553D);
    result.addTableRow(74, 0.0567D);
    result.addTableRow(75, 0.0582D);
    result.addTableRow(76, 0.0598D);
    result.addTableRow(77, 0.0617D);
    result.addTableRow(78, 0.0636D);
    result.addTableRow(79, 0.0658D);
    result.addTableRow(80, 0.0682D);
    result.addTableRow(81, 0.0708D);
    result.addTableRow(82, 0.0738D);
    result.addTableRow(83, 0.0771D);
    result.addTableRow(84, 0.0808D);
    result.addTableRow(85, 0.0851D);
    result.addTableRow(86, 0.0899D);
    result.addTableRow(87, 0.0955D);
    result.addTableRow(88, 0.1021D);
    result.addTableRow(89, 0.1099D);
    result.addTableRow(90, 0.1192D);
    result.addTableRow(91, 0.1306D);
    result.addTableRow(92, 0.1449D);
    result.addTableRow(93, 0.1634D);
    result.addTableRow(94, 0.1879D);
    return result;
  }
  
  private Map<Integer, Double> rifMinimumPercentages = new LinkedHashMap<Integer, Double>();
  private DateTime dateOfBirth;
  
  private boolean isMinimumApplicable(Integer year, DateTime rspToRifConversionDate) {
    return year >= rspToRifConversionDate.getYear() + 1;
  }
}
