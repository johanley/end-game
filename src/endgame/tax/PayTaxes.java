package endgame.tax;

import endgame.Scenario;
import endgame.model.Money;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** 
 Pay your yearly taxes.
 This transaction is unusual in that it isn't configured for its date.
 The date is hard-coded to the last day of the year.
 This transaction must be executed as the last transaction of the day and year.
*/
public final class PayTaxes extends Transactional {

  /**
   In this simulation, you always pay your taxes on the last day of the year. 
   This simplification is not realistic, but the consequences of that are minor. 
  */
  public PayTaxes(String rawDates) {
    super(rawDates);
  }
  
  /** Withdraw the amount owing on this year's taxes from your bank account. */
  @Override protected void execute(DateTime when, Scenario sim) {
    Money owed = sim.taxReturn.balanceOwing();
    if (owed.isPlus()) {
      sim.bank.withdrawCash(owed, when);
    }
    else {
      sim.bank.depositCash(owed, when); 
    }
    logMe(when, owed);
  }
  
  @Override public String toString() {
    return "PAY TAXES: "; 
  }
}
