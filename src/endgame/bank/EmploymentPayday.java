package endgame.bank;

import endgame.Scenario;
import endgame.model.Money;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** 
 You get a paycheck from employment.
 
 The model here is very simplified. 
 The idea is that the retiree has a small job on the side.
 The paycheck is simply a gross amount, with no deductions.
 The gross amount is added to the employment income line of your tax return, such 
 that you later pay taxes on the gross amount. 
*/
public final class EmploymentPayday extends Transactional {
  
  /**
   Constructor.
   @param startDate the first day on the job
   @param endDate the last day of the job
   @param monthlyGross how much you gross per month
   @param rawDates the day of the month on which you are paid. If you are paid more than once a month, 
   just pro-rate.
  */
  public EmploymentPayday(String startDate, String endDate, String monthlyGross, String rawDates) {
    super(rawDates);
    this.start = new DateTime(startDate);
    this.end = new DateTime(endDate);
    this.monthlyGross = new Money(monthlyGross);
  }
  
  /** Deposit to your bank account, and add to your employment income on your tax return. */
  @Override protected void execute(DateTime when, Scenario sim) {
    if(when.gteq(start) && when.lteq(end)) {
      sim.bank.depositCash(monthlyGross, when);
      sim.taxReturn.addEmploymentIncome(monthlyGross);
      logMe(when, monthlyGross);
    }
  }
  
  @Override public String toString() {
    return "PAYDAY: ";
  }
  
  private DateTime start;
  private DateTime end;
  private Money monthlyGross;
}