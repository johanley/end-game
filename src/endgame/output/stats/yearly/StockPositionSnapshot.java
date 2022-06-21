package endgame.output.stats.yearly;

import endgame.model.Money;
import endgame.security.stock.StockPosition;

/** An IMMUTABLE form of {@link StockPosition}. */
public final class StockPositionSnapshot {

  StockPositionSnapshot(StockPosition sp, Money marketPrice){
    this.symbol = sp.stock().symbol();
    this.numShares = sp.numShares();
    this.marketPrice = marketPrice;
  }
  
  public String symbol() { return symbol; }
  public Integer numShares() { return numShares; }
  public Money marketPrice() { return marketPrice; }
  public Money marketValue() { return marketPrice.times(numShares); }
  
  private String symbol;
  private Integer numShares;
  private Money marketPrice;
}
