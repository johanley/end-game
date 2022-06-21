package endgame.security.stock.transaction;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/**
 Move shares from one account to another.
  
 <P>The transfer can be of a certain number of shares, or of a certain market value of the shares.
 <P>If a transfer-out does not have the required shares/amount, then an exception is thrown.
 <P>For a single leg of a transfer, either transfer-in or transfer-out, see {@link TransferStock}.
*/
public final class MoveStock extends Transactional {

  /** Transfer a specific number of shares. */
  public static MoveStock byNumShares(Account fromAccount, Account toAccount, String numShares, String stockSymbol, String when) {
    MoveStock result = new MoveStock(fromAccount, toAccount, stockSymbol, when);
    result.numShares = Integer.valueOf(numShares);
    return result;
  }
  
  /** Transfer a specific market-value of shares. */
  public static MoveStock byMarketValue(Account fromAccount, Account toAccount, String marketValue, String stockSymbol, String when) {
    MoveStock result = new MoveStock(fromAccount, toAccount, stockSymbol, when);
    result.marketValue = new Money(marketValue);
    return result;
  }
  
  @Override protected void execute(DateTime when, Scenario sim) {
    if (isByNumShares()) {
      transferByNumShares(numShares, when, sim);
    }
    else {
      transferByMarketValue(when, sim);
    }
    logMe(when, "");
  }
  
  @Override public String toString() {
    String result = "MOVE from " + fromAccount.getClass().getSimpleName() + " to " + toAccount.getClass().getSimpleName(); 
    String sharesOrAmt = isByNumShares() ? numShares + " shares " : marketValue.toString();
    result = result + " " + sharesOrAmt + " " + stockSymbol;
    return result;
  }
  
  // PRIVATE
  
  private MoveStock(Account fromAccount, Account toAccount, String stockSymbol, String when) {
    super(when);
    this.fromAccount = fromAccount;
    this.toAccount = toAccount;
    this.stockSymbol = stockSymbol;
  }

  private Account fromAccount; 
  private Account toAccount; 
  private String stockSymbol;
  
  private Integer numShares = 0;
  private Money marketValue;
  
  private boolean isByNumShares() {
    return numShares > 0;
  }
  
  private void transferByNumShares(Integer numShares, DateTime when, Scenario sim) {
    Stock stock = sim.stockFrom(stockSymbol);
    fromAccount.transferSharesOut(numShares, stock, when);
    toAccount.transferSharesIn(numShares, stock, when);
  }

  private void transferByMarketValue(DateTime when, Scenario sim) {
    Integer numSharesForAmount = numSharesFor(marketValue, sim);
    transferByNumShares(numSharesForAmount, when, sim);
  }

  /** Market value amount divided by price, and floored to the nearest integer. */
  private Integer numSharesFor(Money marketValue, Scenario sim) {
    Stock stock = sim.stockFrom(stockSymbol);
    Double price = stock.price().asDouble();
    return marketValue.flooredDiv(price);
  }
}