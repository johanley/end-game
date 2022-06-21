package endgame.security.stock.price;

import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 Constant annual growth rate, the same for all stocks.
 Many simulations use this simple model, but it's not very realistic.  
 Nevertheless, it's included for purposes of comparison.
 
 <P>An interesting use case is that of 0% growth. 
 A common problem is deciding which account to liquidate first.
 So, it can be a useful data point to run simulations with fixed prices, for 
 purposes of comparison (even though taxes depend on stock prices).
*/
public final class FixedGrowthRate extends StockPricePolicyBase {

  /** Negative growth rates are permitted, if desired. */
  public FixedGrowthRate(String percent) {
    this.annualGrowthPercent = Util.percentFrom(percent);
  }
  
  /** The year-over-year growth rate is always the same. */
  @Override public Double yearOverYearFractionalGrowth(DateTime when) {
    return annualGrowthPercent;
  }
  
  @Override public String toString() {
    return "STOCK PRICES: fixed growth rate " + annualGrowthPercent;
  }
  
  private Double annualGrowthPercent;
}