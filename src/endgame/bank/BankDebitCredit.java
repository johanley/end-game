package endgame.bank;

import endgame.Scenario;
import endgame.model.Money;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** 
 Bank deposits and withdrawals (with no tax consequences).
 Examples: monthly rent, household spending; one-time big-ticket items. 
*/
public final class BankDebitCredit extends Transactional {

  /** 
   Constructor.
   @param amount positive amounts are treated as withdrawals, and  
   negative amounts are treated as deposits.
  */
  public BankDebitCredit(Money amount, String when) {
    super(when);
    this.amount = amount;
  }
  
  /** Deposit or withdraw an amount from your bank account. */
  @Override protected void execute(DateTime when, Scenario sim) {
    logMe(when, amount);
    if (amount.isPlus()) {
      sim.bank.withdrawCash(amount, when);
    }
    else {
      sim.bank.depositCash(amount, when);
    }
  }
  
  @Override public String toString() {
    return "BANK SPEND: "; 
  }
  
  private Money amount;
}
