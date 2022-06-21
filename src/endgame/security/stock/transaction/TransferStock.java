package endgame.security.stock.transaction;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/**
 Transfer shares into or out of an account (transfer-in or transfer-out).
  
 <P>Transfer operations almost always appear in pairs: transfer-out plus transfer-in, in order to move shares from one 
 account to another. See {@link MoveStock}.
 
 <P>The transfer can be of a certain number of shares, or of a certain market value of the shares.
 
 <P>If a transfer-out does not have the required shares/amount, then an exception is thrown.
*/
public final class TransferStock extends Transactional {

  /** Transfer a specific number of shares. */
  public static TransferStock byNumShares(Account account,  String inOut, String numShares, String stockSymbol, String when) {
    TransferStock result = new TransferStock(account, inOut, stockSymbol, when);
    result.numShares = Integer.valueOf(numShares);
    return result;
  }
  
  /** Transfer a specific market-value of shares. */
  public static TransferStock byMarketValue(Account account,  String inOut, String marketValue, String stockSymbol, String when) {
    TransferStock result = new TransferStock(account, inOut, stockSymbol, when);
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
    String result = isTransferIn() ? "TRANSFER-IN: " : "TRANSFER-OUT: ";
    String sharesOrAmt = isByNumShares() ? numShares + " shares" : marketValue.toString();
    result = result + sharesOrAmt + " " + stockSymbol + " in " + account.getClass().getSimpleName();
    return result;
  }
  
  // PRIVATE
  
  private TransferStock(Account account,  String inOut, String stockSymbol, String when) {
    super(when);
    this.account = account;
    this.inOut = inOut;
    this.stockSymbol = stockSymbol;
  }

  private Account account; 
  private String inOut = "";
  private String stockSymbol;
  
  private Integer numShares = 0;
  private Money marketValue;
  
  private boolean isByNumShares() {
    return numShares > 0;
  }
  
  private boolean isTransferIn() {
    return inOut.equalsIgnoreCase("transfer-stock-in");
  }

  private void transferByNumShares(Integer numShares, DateTime when, Scenario sim) {
    Stock stock = sim.stockFrom(stockSymbol);
    if (isTransferIn()) {
      account.transferSharesIn(numShares, stock, when);
    }
    else {
      account.transferSharesOut(numShares, stock, when);
    }
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
