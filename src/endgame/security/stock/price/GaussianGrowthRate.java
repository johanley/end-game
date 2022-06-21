package endgame.security.stock.price;

import java.util.concurrent.ThreadLocalRandom;

import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 The annual growth rate has a 'normal' (Gaussian) distribution, defined with a mean and standard deviation.
*/
public final class GaussianGrowthRate extends StockPricePolicyBase {

  /** The percentMean and percentVariance should be positive. */
  public GaussianGrowthRate(String percentMean, String percentStandardDeviation) {
    this.mean = Util.percentFrom(percentMean);
    this.standardDeviation = Util.percentFrom(percentStandardDeviation);
  }

  /** 
   The year-over-year growth rate is randomly chosen using a Gaussian distribution, defined with a 
   mean and standard deviation. 
  */
  @Override public Double yearOverYearFractionalGrowth(DateTime when) {
    return randomRateNormalDistribution();
  }
  
  @Override public String toString() {
    return "STOCK PRICES: gaussian growth rate, mean:" + mean + " std dev:" + standardDeviation;
  }
  
  private Double mean;
  private Double standardDeviation; //not the variance!
  
  private Double randomRateNormalDistribution() {
    ThreadLocalRandom generator = ThreadLocalRandom.current();
    return mean + generator.nextGaussian() * standardDeviation;
  }
}
