package endgame.output.stats.yearly;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import endgame.model.Money;
import endgame.util.Consts;

/** 
 Collects all accounts together as one, such that they can be stored as one object in history.
 This class makes it easier to calculate the net worth at any point in the history.
 It also keeps the account history in sync (same indexes) with the history of {@link TaxSummary} and {@link CashFlow}. 
*/
public final class AccountSet {
  
  public void add(AccountSnapshot snap) {
    snaps.add(snap);
  }
  
  public Set<AccountSnapshot> accountSnapshots(){
    return Collections.unmodifiableSet(snaps);
  }
  
  public Money netWorth() {
    Money result = Consts.ZERO;
    for(AccountSnapshot snap : snaps) {
      result = result.plus(snap.marketValue());
    }
    return result;
  }
  
  private Set<AccountSnapshot> snaps = new LinkedHashSet<AccountSnapshot>();
}