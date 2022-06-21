package endgame.security.stock.transaction;

import java.util.Set;

import endgame.Scenario;
import endgame.account.Account;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** N-to-1 stock split. */
public final class StockSplit extends Transactional {
  
  public static StockSplit valueOf(String when, Set<String> tickers, String factor) {
    Integer fact = Integer.valueOf(factor);
   return new StockSplit(when, tickers, fact);
  }
  
  /** 
   Stock split for a given set of stock ticker symbols.
   
   Reduce the current and historical prices.
   Reduce the dividend amount.
   Increase positions.
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    for (String ticker : tickers) {
      Stock target = sim.stockFrom(ticker);
      target.stockSplit(factor);
      
      for(Account account : sim.investmentAccounts()) {
        for(StockPosition stockPosition : account.stockPositions()) {
          if(stockPosition.stock().symbol().equalsIgnoreCase(ticker)) {
            Integer currentNum = stockPosition.numShares();
            Integer newNum = currentNum * factor;
            Integer increase = newNum - currentNum;
            stockPosition.increase(increase);
            logMe(when, account.getClass().getSimpleName() + " " + ticker + " position increased from " + currentNum + " to " + stockPosition.numShares());
          }
        }
      }
    }
  }

  @Override public String toString() {
    return "STOCK SPLIT " + tickers + " " + factor + "-to-1";
  }
  
  private Set<String> tickers;
  private Integer factor;
  
  private StockSplit(String when, Set<String> tickers, Integer factor) {
    super(when);
    this.tickers = tickers;
    this.factor = factor;
  }
}