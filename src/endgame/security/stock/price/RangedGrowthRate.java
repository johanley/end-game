package endgame.security.stock.price;

import java.util.concurrent.ThreadLocalRandom;

import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 The annual growth rate is randomly distributed in a specific range.
 This policy will often show more variation than {@link GaussianGrowthRate}. 
*/
public final class RangedGrowthRate extends StockPricePolicyBase {

  /** The upper limit should be positive. The lower limit can be negative. */
  public RangedGrowthRate(String lowerLimit, String upperLimit) {
    this.percentLowerLimit = Util.percentFrom(lowerLimit);
    this.percentUpperLimit = Util.percentFrom(upperLimit);
  }
  
  /** The year-over-year growth rate is randomly chosen between a lower limit and an upper limit. */
  @Override public Double yearOverYearFractionalGrowth(DateTime when) {
    return randomRateInRange();
  }

  @Override public String toString() {
    return "STOCK PRICES: growth rate in range " + percentLowerLimit + ".." + percentUpperLimit;
  }
  
  private Double percentLowerLimit;
  private Double percentUpperLimit;
  
  private Double randomRateInRange() {
    ThreadLocalRandom generator = ThreadLocalRandom.current();
    return generator.nextDouble(percentLowerLimit, percentUpperLimit);
  }
}