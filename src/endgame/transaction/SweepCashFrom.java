package endgame.transaction;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import hirondelle.date4j.DateTime;

/** Move cash from an investment account into your bank account. */
public final class SweepCashFrom extends Transactional {
  
  public SweepCashFrom(Account account, String when) {
    super(when);
    this.account = account;
  }

  /** Move the cash only if there's a positive balance in the source account. */
  @Override protected void execute(DateTime when, Scenario sim) {
    Money amount = account.cash();
    if (amount.isPlus()) {
      Money withheld = account.withdrawCash(amount, when);
      sim.bank.depositCash(amount, when);
      sim.yearlyCashFlows.cashSwept = sim.yearlyCashFlows.cashSwept.plus(amount);
      if (withheld.isZero()) {
        logMe(when, amount); 
      }
      else {
        logMe(when, amount + " withheld " + withheld); 
      }
    }
  }
  
  @Override public String toString() {
    return "SWEEP cash from " + account.getClass().getSimpleName();
  }
  
  private Account account;
}