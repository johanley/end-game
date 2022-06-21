package endgame.security.stock.price;

import endgame.model.Money;
import endgame.security.stock.Stock;
import hirondelle.date4j.DateTime;

/** 
 Policies for changing the price of stocks.
 The configured policy applies universally to all stocks.
 In this simulation, the prices are updated only once a year. 
*/
public interface StockPricePolicy {

  /**
   Change the current price of a stock.
   This simulation will call this method once a year, at the end of the year.
   This method will call {@link Stock#updatePrice(endgame.model.Money, DateTime)}, 
   and it will return the new price.
   
   <P>Repeated calls to this method, with the exact same arguments in the exact same state, may not result 
   in the same price for the stock! This is because prices are often generated randomly.
   <P>This method is meant to be called only in temporal order, as the simulation progresses forward through time.
   <P>Design note: the {@link Stock} class itself 'remembers' its historical prices (for reporting purposes).
  */
  public Money updateThePriceOfThe(Stock stock, DateTime when);
  
}
