package endgame.security.stock.price;

import endgame.model.Money;
import endgame.security.stock.Stock;
import hirondelle.date4j.DateTime;

/** Abstract Base Class for year-over-year percentage change in the stock price. */
public abstract class StockPricePolicyBase implements StockPricePolicy {
  
  /** 
   Template method which calls {@link #yearOverYearFractionalGrowth()}.
   The year-over-year growth is applied to the current price of the stock. 
  */
  @Override public Money updateThePriceOfThe(Stock stock, DateTime when) {
    Double fractionalGrowth = yearOverYearFractionalGrowth(when);
    Double yearlyMultiplier = 1 + fractionalGrowth;
    Money currentPrice = stock.price();
    Money newPrice = currentPrice.times(yearlyMultiplier);
    stock.updatePrice(newPrice, when);
    return newPrice;
  }
  
  /** 
   The yearly growth in the stock, as a fraction of its previous value.
   For example, returning 0.05 from this method means a 5% annual growth rate.
   
   <P>The implementation may be deterministic (no randomness) or stochastic (with randomness).
   If the implementation is deterministic, then each year all stocks will share the same 
   percentage growth. If stochastic, then different stocks will almost always have different annual growth, 
   on a given year.
  */
  public abstract Double yearOverYearFractionalGrowth(DateTime when);
}
