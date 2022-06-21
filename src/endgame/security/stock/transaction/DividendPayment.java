package endgame.security.stock.transaction;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.transaction.Transactional;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/** Stock dividend payments. */
public final class DividendPayment extends Transactional {

  public DividendPayment(Stock stock) {
    super(stock.dividend().getDates()); 
    this.stock = stock;
  }
  
  /** 
   A stock automatically pays a periodic dividend to all accounts that hold a position in the stock.
   
   In this simulation, the dividend increases by the same fixed percentage each year.
   With many blue-chip dividend stocks, this is an imperfect but fair approximation.  
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    for (Account account : sim.investmentAccounts()) {
      for (StockPosition sp : account.stockPositions()) {
        if (sp.stock().symbol().equals(stock.symbol())) {
          Money dividendPerShare = thisYearsDividend(new DateTime(sim.startDate), when);
          Money amount = dividendPerShare.times(sp.numShares());
          account.dividend(amount);
          sim.yearlyCashFlows.dividends = sim.yearlyCashFlows.dividends.plus(amount);
          Log.log(when + ":DIVIDEND: " +  stock.symbol() +  " " + account.getClass().getSimpleName() + " " + sp.numShares() + "@" + dividendPerShare + " = "  + amount);
        }
      }
    }
  }
  
  @Override public String toString() {
    return "DIVIDEND: " + stock + ": ";
  }
  
  private Stock stock;

  /** 
   Increases by the same fixed percentage, year after year. 
   In this simulation, dividend growth is not stochastic. 
  */
  private Money thisYearsDividend(DateTime startDate, DateTime when) {
    Double yearlyGrowth = stock.dividend().getGrowth();
    Integer numYears = when.getYear() - startDate.getYear();
    Double growthFactor = Math.pow(1.0d + yearlyGrowth, numYears);
    return stock.dividend().getAmount().times(growthFactor);
    /* 
    int scale = stock.dividend().getAmount().getAmount().scale();
    return stock.dividend().getAmount().times(growthFactor, scale);
    */
  }
}