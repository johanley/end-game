package endgame.security.stock.commission;

import endgame.model.Money;
import endgame.util.Util;

/** 
 Fixed percentage of the gross dollar amount of the trade.
*/
public final class FixedPercentCommish implements Commission {
  
  public FixedPercentCommish(String fixedPercent) {
    this.percent = Util.percentFrom(fixedPercent);
  }

  /** Always return the same percentage of the gross amount. */
  @Override public Money commissionOn(Integer numShares, Money price) {
    return price.times(numShares).times(percent);
  }  
    
  private Double percent;

}
