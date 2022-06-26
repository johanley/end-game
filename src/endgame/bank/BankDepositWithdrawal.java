package endgame.bank;

import endgame.Scenario;
import endgame.model.Money;
import endgame.transaction.Transactional;
import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 Bank deposits and withdrawals (with no tax consequences).
 Examples: monthly rent, household spending, windfalls, one-time big-ticket items. 
*/
public final class BankDepositWithdrawal extends Transactional {

  /** Constructor. One of deposit and withdrawal must be empty. */
  public BankDepositWithdrawal(String deposit, String withdrawal, String when) {
    super(when);
    this.deposit = Util.isPresent(deposit) ? new Money(deposit) : null;
    this.withdrawal = Util.isPresent(withdrawal) ? new Money(withdrawal) : null;
  }
  
  /** Deposit or withdraw an amount from your bank account. */
  @Override protected void execute(DateTime when, Scenario sim) {
    Money amt = amount();
    logMe(when, amt);
    if (isDeposit()) {
      sim.bank.depositCash(amt, when);
    }
    else {
      sim.bank.withdrawCash(amt, when);
    }
  }
  
  @Override public String toString() {
    return isDeposit() ? "BANK DEPOSIT: " : "BANK WITHDRAWAL: "; 
  }
  
  private Money deposit;
  private Money withdrawal;
  
  private Money amount() {
    return isDeposit() ? deposit : withdrawal;
  }
  
  private boolean isDeposit() {
    return deposit != null;
  }
}
