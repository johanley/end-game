package endgame.security.stock;

import endgame.model.Money;

/** An accounts holds a position in a number of shares of a given {@link Stock}. */
public final class StockPosition {

  public static StockPosition valueOf(Stock stock, String numShares) {
    return new StockPosition(stock, Integer.valueOf(numShares));
  }
  
  public StockPosition(Stock stock, Integer numShares) {
    this.stock = stock;
    this.numShares = numShares;
  }
  
  public Stock stock() { return stock; }
  
  public Integer numShares() { 
    return numShares; 
  }
  
  public Money marketValue() { 
    return stock.price().times(numShares); 
  }
  
  /** It's the caller's responsibility to make sure this doesn't go below 0. */
  public void increase(Integer num) { numShares = numShares + num; }
  public void decrease(Integer num) { numShares = numShares - num; }
  
  @Override public String toString() {
    return "STOCK-POSITION {shares:" + numShares + " stock:" + stock + "}";  
  }
  
  private Stock stock;
  private Integer numShares;
}