package endgame.account.nra;

import static endgame.util.Consts.ZERO;

import java.math.BigDecimal;

import endgame.model.Money;
import endgame.security.stock.StockPosition;

/** 
 The book-value of a stock held in a non-registered account.
 This is explicitly separated from {@link StockPosition}.
*/
public final class BookValue {
  
  public static BookValue valueOf(String symbol, String amount) {
    return new BookValue(symbol, new Money(new BigDecimal(amount)));
  }
 
  BookValue(String symbol, Money amount) {
    this.symbol = symbol;
    this.amount = amount;
  }

  public String getSymbol() { return symbol; }
  public Money getAmount() { return amount; }
  
  /** The cost of acquisition includes commissions, fees. */
  public void increaseBy(Money costOfAcquisition) { 
    amount = amount.plus(costOfAcquisition); 
  }

  /** 
   The amount is set to zero if the whole position is involved.
   Otherwise, if only part of the position is involved, then pro-rate according to the number of shares.
   Blow up if the number of shares involved is more than the number currently held 
  */
  public void decrease(Integer currentlyHeld, Integer numShares) {
    if (numShares > currentlyHeld) {
      throw new RuntimeException("Holding " + currentlyHeld + " shares, and trying to sell/move " + numShares);
    }
    else if (currentlyHeld.equals(numShares)) {
      //the whole position
      amount = ZERO;
    }
    else {
      //part of the existing position, not the whole thing
      //pro rate according to the number of shares
      double fractionSold = (1.0D * numShares) / (1.0D * currentlyHeld); // avoid integer divsion!!!
      double fractionRemaining = 1.0 - fractionSold; 
      amount = amount.times(fractionRemaining);
    }
  }
  
  @Override public String toString() {
    return "BOOK-VALUE {symbol:" + symbol + " amount:" + amount + "}";  
  }
  
  private String symbol;
  private Money amount;
}