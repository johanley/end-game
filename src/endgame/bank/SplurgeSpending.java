package endgame.bank;

import endgame.Scenario;
import endgame.model.Money;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** 
 Spend the extra cash that has built up in your bank account.
 
 <P>If this is not done, then there's a tendency for your bank account to build up a 
 large balance, which isn't very realistic.
 Of course, this class assumes that you don't want to invest such cash.  
*/
public final class SplurgeSpending extends Transactional {
  
  /**
   Constructor.
   @param minBalance spend only as much as will keep the bank account balance 
   over this minimum value.
  */
  public SplurgeSpending(Money minBalance, String when) {
    super(when);
    this.minBalance = minBalance;
  }

  /** Withdraw from your bank account all of the money over a certain minimum. */
  @Override protected void execute(DateTime when, Scenario sim) {
    Money currentBal = sim.bank.value();
    if (currentBal.gt(minBalance)) {
      Money amount = currentBal.minus(minBalance);
      sim.bank.withdrawCash(amount, when);
      logMe(when, amount);
    }
  }
  
  @Override public String toString() {
    return "SPLURGE SPENDING: "; 
  }
  
  private Money minBalance;

}
