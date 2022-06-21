package endgame.security.stock.transaction;

import java.util.Optional;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/**
 Buy or sell stock on the given date. 
 If there are insufficient funds (to buy) or shares (to sell), then no action is taken, but the fact is logged.
*/
public final class BuySellStock extends Transactional {
  
  public BuySellStock(String when, String numShares, Account account, String stockSymbol, String buySell) {
    super(when);
    this.numShares = Integer.valueOf(numShares);
    this.account = account;
    this.stockSymbol = stockSymbol;
    this.buySell = buySell;
  }
  
  @Override protected void execute(DateTime when, Scenario sim) {
    Stock stock = sim.stockFrom(stockSymbol);
    Money result = Consts.ZERO;
    if (isSell()) {
      result = sell(stock, sim);
    }
    else {
      result = buy(stock, sim);
    }
    
    if (result.isPlus()) {
      logMe(when, result);
    }
  }
  
  @Override public String toString() {
    String result = isSell() ? "SELL: " : "BUY: ";
    result = result + stockSymbol + " " + account.getClass().getSimpleName()+ " " +  numShares + "@" + price + " commish:" + comm; 
    return result;
  }
  
  private String buySell = "";
  private Integer numShares;
  private Account account;
  private String stockSymbol;
  private Money price;
  private Money comm;
  
  private boolean isSell() {
    return buySell.trim().equalsIgnoreCase("sell-stock");
  }
  
  /** Return the proceeds. */
  private Money sell(Stock stock, Scenario sim) {
    Money result = Consts.ZERO;
    Optional<StockPosition> position = account.positionFor(stock);
    if (position.isEmpty()) {
      Log.log("  Trying to sell " + stock.symbol() + " in " + account.getClass().getSimpleName() + " but no position exists.");
    }
    else if (position.get().numShares() < numShares) {
      Log.log("  Trying to sell " + numShares + " " + stock.symbol() + " in " + account.getClass().getSimpleName() + " but account has only " + position.get().numShares() + " shares.");
    }
    else {
      price = stock.price();
      comm = sim.commission.commissionOn(numShares, price);
      result = account.sellShares(numShares, stock, comm);
      sim.yearlyCashFlows.liquidationProceeds = sim.yearlyCashFlows.liquidationProceeds.plus(result);
    }
    return result;
  }
  
  /** Return the total cost. */ 
  private Money buy(Stock stock, Scenario sim) {
    Money result = Consts.ZERO;
    price = stock.price();
    comm = sim.commission.commissionOn(numShares, price);
    Money amount = price.times(numShares).plus(comm);
    if (account.cash().lt(amount)) {
      Log.log("  Insufficient cash: " + account.cash() + ". Can't buy " + numShares + " of " + stockSymbol + " in " + account.getClass().getSimpleName());
    }
    else {
      result = account.buyShares(numShares, stock, comm);
      sim.yearlyCashFlows.liquidationProceeds = sim.yearlyCashFlows.liquidationProceeds.minus(result);
    }
    return result;
  }
}