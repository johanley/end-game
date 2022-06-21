package endgame.security.stock.commission;

import endgame.model.Money;

/** 
 Fixed dollar amount per trade.
 Some discount brokers have this kind of policy. 
*/
public final class FixedAmountCommish implements Commission {
  
  public FixedAmountCommish(String fixedAmount) {
    this.amount = new Money(fixedAmount);
  }

  /** Always return the same amount, regardless of price or the number of shares. */
  @Override public Money commissionOn(Integer numShares, Money price) {
    return amount;
  }  
    
  private Money amount;

}
