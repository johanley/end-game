package endgame.security.stock.price;

import java.util.ArrayList;
import java.util.List;

import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 Annual percentage change is specified in an explicit list of percentages. 
*/
public final class ExplicitGrowthList extends StockPricePolicyBase {
  
  /**
   Constructor.
   
   @param rawList example '5.0%, 3.2%, -5.7%'. Must have a minimum of two items in the list.
   IMPORTANT: the order of these items corresponds to the year, going forward in time.
   CIRCULAR: if the simulation gets to the end of this list before the end of the simulation, then 
   this class will go back to the start of the list, and go through it again from the beginning.
  */
  public ExplicitGrowthList(String rawList) {
    List<String> parts = Util.chopList(rawList);
    for(String part : parts) {
      Double rate = Util.percentFrom(part);
      explicitGrowthRates.add(rate);
    }
    if (explicitGrowthRates.size() < 2) {
      //throw new IllegalArgumentException("Number of explicit growth rates is too small: " + explicitGrowthRates.size());
    }
  }

  /** Returns the next rate in sequence, from the list passed to the constructor. */
  @Override public Double yearOverYearFractionalGrowth(DateTime when) {
    return nextRateInList(when);
  }
  
  @Override public String toString() {
    return "STOCK PRICES: explicit list: " + explicitGrowthRates;
  }
  
  // PRIVATE
  
  private List<Double> explicitGrowthRates = new ArrayList<Double>();
  private Integer firstYear;
  
  private Double nextRateInList(DateTime when) {
    if (firstYear == null) {
      firstYear = when.getYear();
    }
    Integer numYears = when.getYear() - firstYear;
    Integer cursor = numYears % explicitGrowthRates.size(); //cycles if it gets to the end
    return explicitGrowthRates.get(cursor);
  }
}