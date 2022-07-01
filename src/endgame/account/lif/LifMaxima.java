package endgame.account.lif;

import java.util.LinkedHashMap;
import java.util.Map;

import endgame.account.rif.RifLifMinima;
import endgame.model.Money;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** 
 Yearly maximum withdrawals from your LIF account.
 
 The percent that you must withdraw is fixed by a table which depends on your age on January 1, and on the jurisdiction
 in which the LIRA/LIF was established. That jurisdiction can be different from the one in which you are resident. 
 The final dollar amount is found by multiplying the percent by the market value of your 
 LIF at the end of business of the previous year (or the start of business of the current year, if you wish).
 
 <P>At the end of the year, there are two tasks: make sure 
 the max has not been reached for this year, and calculate the new max for next year.
 
 <P>Ref: https://ca.rbcwealthmanagement.com/delegate/services/file/3244485/content
*/
public final class LifMaxima {

  public LifMaxima(DateTime dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }
  
  /** 
   Add a row of the table that computes percentage of the account value.
   This method must be called upon startup.
   This data is in the scenario file.
  */
  public static void addTableRow(String jurisdictions, Integer age, Double percent) {
    if (limitPercentages.get(jurisdictions) == null) {
      limitPercentages.put(jurisdictions, new LinkedHashMap<Integer, Double>());
    }
    limitPercentages.get(jurisdictions).put(age, percent);
  }

  /** 
   Compute the maximum LIF withdrawal for the given year.
   @param jurisdiction CA, ON, AB, and so on. 
  */
  public Money withdrawalMax(Money accountValueOnJan1, Integer year, DateTime conversionDate, String jurisdiction) {
    Money result = Consts.ZERO;
    if(RifLifMinima.isLimitApplicable(year, conversionDate)) {
      result = limitFor(year, accountValueOnJan1, jurisdiction);
    }
    return result;
  }

  @Override public String toString() {
    return "Lif Maxima date-of-birth:" + dateOfBirth +  " percentages:" + limitPercentages;   
  }
  
  //PE is excluded here, since it has no LIRA/LIF
  public static final String CA_ETC = "CA-YT-NT-NU"; //this actually includes the territories
  public static final String MN_ETC = "MN-QC-NS";
  public static final String AB_ETC = "AB-BC-ON-NB-NL-SK";

  private DateTime dateOfBirth;
  
  private static final Integer FIRST_AGE = 55;
  private static final Integer LAST_AGE = 95;
  
  /** Making the core data static means you can avoid long-lived objects. */
  private static Map<String /*jurisdictions - plural!*/, Map<Integer /*year*/, Double /*percent 0.05*/>> limitPercentages = 
    new LinkedHashMap<String, Map<Integer, Double>>()
  ;
  
  private static final String[] ALL_KEYS = {CA_ETC, MN_ETC, AB_ETC};
  private String keyFor(String jurisdiction) {
    String result = "";
    for(String key : ALL_KEYS) {
      if (key.contains(jurisdiction)) {
        result = key;
        break;
      }
    }
    return result;
  }
  
  private Money limitFor(Integer year, Money accountValueOnJan1, String jurisdiction) {
    Integer ageOnJan1 = year - dateOfBirth.getYear();
    if (ageOnJan1 < FIRST_AGE) {
      throw new RuntimeException("Age is " + ageOnJan1 +", but expecting minimum age of " + FIRST_AGE);
    }
    
    Double fraction = 0.0;
    if (ageOnJan1 <= LAST_AGE) {
      fraction = limitPercentages.get(keyFor(jurisdiction)).get(ageOnJan1);
    }
    else {
      fraction = limitPercentages.get(keyFor(jurisdiction)).get(LAST_AGE);
    }
    return accountValueOnJan1.times(fraction);
  }
}
