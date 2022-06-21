package endgame.security.stock.price;

import endgame.Scenario;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.transaction.TransactionDates;
import endgame.transaction.Transactional;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/** 
 Change the price of all stocks in the simulation, once a year.
 Not really a transaction in an account, but somewhat related.
 Uses the configured implementation of {@link StockPricePolicy}.
*/
public final class UpdateStockPrices extends Transactional {
  
  public UpdateStockPrices(String when) {
    super(TransactionDates.fromWhen(when)); 
  }

  /** Update all stock prices, on December 31 of each year. */
  @Override protected void execute(DateTime when, Scenario sim) {
    for(Stock stock : sim.stocks) {
      Money oldPrice = stock.price();
      Money newPrice = sim.stockPrices.updateThePriceOfThe(stock, when);
      Log.log(when + ":" + this  + " " + stock.symbol() + " " + newPrice + " [" + percent(oldPrice, newPrice) + "%]");
    }
  }
  
  @Override public String toString() {
    return "STOCK-PRICE-UPDATE: " ;
  }
  
  /** For logging only. Round to two decimals. */
  private Double percent(Money oldPrice, Money newPrice) {
    Double delta = newPrice.minus(oldPrice).asDouble();
    Double frac = delta / oldPrice.asDouble(); //.057365
    Double percent = frac * 100.0D; // 5.7365
    percent = Math.round(percent * 100.0 /*573.65*/) / 100.0D;
    return percent; // 5.73
  }
}