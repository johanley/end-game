package endgame.bank;

import java.math.BigDecimal;

import endgame.account.Cashable;
import endgame.model.Money;
import endgame.util.Consts;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/** 
 Bank accounts have only two kinds of transaction: deposit cash and withdraw cash.
 They don't hold assets other than cash. 
*/
public final class BankAccount implements Cashable {
  
  public static BankAccount valueOf(String cash, String smallBalanceLimit) {
    return new BankAccount(cash, smallBalanceLimit);
  }

  /** Adds the given amount to the balance. */
  @Override public void depositCash(Money amount, DateTime when) {
    cash = cash.plus(amount);
  }
  
  /**
   Subtracts the given amount from the balance. Doesn't allow overdrafts. 
   Detects if the balance has fallen below a certain level, and logs the fact.
  */
  @Override public Money withdrawCash(Money amount, DateTime when) {
    if (cash.lt(amount)){
      throw new RuntimeException("You're trying to withdraw " + amount + " but you only have " + cash + ".");
    }
    cash = cash.minus(amount);
    if (cash.lt(smallBalanceLimit)) {
      Log.log("  Bank balance: " + cash  + " is under the small-balance limit of " + smallBalanceLimit);
    }
    return Consts.ZERO;
  }

  /** The account balance. */
  @Override public Money cash() { return cash; }
  public Money getSmallBalanceLimit() {return smallBalanceLimit; }

  @Override public String toString() {
    return "BANK {cash:" + cash + " limit:" + smallBalanceLimit + "}";  
  }
  
  // PRIVATE 
  private Money cash;
  private Money smallBalanceLimit;
  
  private BankAccount(String cash, String smallBalanceLimit) {
    this.cash = new Money(cash);
    this.smallBalanceLimit = new Money( new BigDecimal(smallBalanceLimit));
  }
}