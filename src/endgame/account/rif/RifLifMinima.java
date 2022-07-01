package endgame.account.rif;

import java.util.LinkedHashMap;
import java.util.Map;

import endgame.model.Money;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** 
 Yearly minimum withdrawals from your RIF/LIF account.
 
 The percent that you must withdraw is fixed by a formula/table which depends on your age on January 1. 
 The final dollar amount is found by multiplying the percent by the market value of your 
 RIF/LIF at the end of business of the previous year (or the start of business of the current year, 
 if you wish).
 
 <P>At the end of the year, there are two tasks: make sure 
 the minimum has been reached for this year, and calc the new minimum for next year.
*/
public final class RifLifMinima {
  
  /** 
   Add a row of the table that computes percentage of the account value.
   This method must be called upon startup.
   This data is in the scenario file.
  */
  public static void addTableRow(Integer age, Double percent) {
    limitPercentages.put(age, percent);
  }
  
  /** Applies only in the conversion year + 1. */
  public static boolean isLimitApplicable(Integer year, DateTime conversionDate) {
    return year >= conversionDate.getYear() + 1;
  }
  
  public RifLifMinima(DateTime dob) {
    this.dateOfBirth = dob;
  }
  
  /** 
   Compute the withdrawal limit for the given year.
   If the year is before conversion-year + 1, then the minimum is 0.
  */
  public Money compute(Money accountValueOnJan1, Integer year, DateTime conversionDate) {
    Money result = Consts.ZERO;
    if(isLimitApplicable(year, conversionDate)) {
      result = limitFor(year, accountValueOnJan1);
    }
    return result;
  }

  @Override public String toString() {
    return "RIF-LIF Minima date-of-birth:" + dateOfBirth + " " + limitPercentages;   
  }
  
  private DateTime dateOfBirth;
  
  /** Making this static lets you avoid needing long-lived objects. */
  private static Map<Integer, Double> limitPercentages = new LinkedHashMap<Integer, Double>();
  
  /** Compute the minimum RIF-LIF withdrawal for the given year. */
  private Money limitFor(Integer year, Money accountValueOnJan1) {
    Integer ageOnJan1 = year - dateOfBirth.getYear();
    Double fraction = 0.0D;
    //some hard-coded constants used here!
    if (ageOnJan1 < 71) {
      fraction = 1.0D / (90 - ageOnJan1);
    }
    else if (ageOnJan1 < 95) {
      fraction = limitPercentages.get(ageOnJan1);
    }
    else {
      fraction = 0.20D;
    }
    return accountValueOnJan1.times(fraction);
  }
}
