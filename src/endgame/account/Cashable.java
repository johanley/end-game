package endgame.account;

import endgame.model.Money;
import hirondelle.date4j.DateTime;

/** Basic cash operations, deposit and withdraw. */
public interface Cashable {

  void depositCash(Money amount, DateTime when);
  
  /** Returns the withholding tax (if any). */
  Money withdrawCash(Money amount, DateTime when);
  
  /** The cash balance in the account. */
  Money cash();

}
