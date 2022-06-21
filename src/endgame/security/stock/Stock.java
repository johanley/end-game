package endgame.security.stock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import endgame.model.Money;
import endgame.security.stock.price.StockPricePolicy;
import hirondelle.date4j.DateTime;

/** 
 A stock with an eligible dividend.

 <P>The price of the stock can be modeled in various ways. 
 See {@link StockPricePolicy} and its implementations.
 
 <P>The dividends are modeled simply, using constant percentage growth.
 For many blue-chip dividend stocks, this is a reasonable approximation to the real world.  
*/
public final class Stock {

  public static Stock valueOf(String symbol, String price, Dividend dividend, String startDate) {
    return new Stock(symbol, price, dividend, new DateTime(startDate));  
  }
  
  public Money price() { return price; }
  
  /** Updated using the configured implementation of {@StockPricePolicy}. */
  public void updatePrice(Money price, DateTime when) {
    this.price = price; 
    this.priceHistory.add(HistoricalPrice.from(when, this.price));
  }
  
  public String symbol() { return symbol; }
  public Dividend dividend() { return dividend; }
  public List<HistoricalPrice> priceHistory() { return Collections.unmodifiableList(priceHistory); }

  /** Simple struct for historical stock prices. */
  public static final class HistoricalPrice {
    static HistoricalPrice from(DateTime when, Money price) {
      return new HistoricalPrice(when, price);
    }
    private HistoricalPrice(DateTime when, Money price) {
      this.when = when;
      this.price = price;
    }
    public Money price() {return price;}
    public DateTime when() {return when;}
    private Money price;
    private DateTime when;
  }
  
  /** Reduce the current and historical prices, and reduce the dividend amount (all by the given factor). */
  public void stockSplit(Integer factor) {
    price = price.divByInt(factor);
    //modify the list of historical prices in place
    for (ListIterator<HistoricalPrice> iter = priceHistory.listIterator(); iter.hasNext();) {
      HistoricalPrice histPrice = iter.next();
      DateTime sameDate = histPrice.when;
      Money smallerPrice = histPrice.price.divByInt(factor);
      HistoricalPrice newHistPrice = HistoricalPrice.from(sameDate, smallerPrice);
      iter.set(newHistPrice);
    }
    dividend.stockSplit(factor);
  }
  
  @Override public String toString() {
    return "STOCK {symbol:" + symbol + " price:" + price + " " + dividend + "}";  
  }
  
  // PRIVATE 
  
  private String symbol;
  private Money price;
  private Dividend dividend;
  private List<HistoricalPrice> priceHistory = new ArrayList<HistoricalPrice>();
  
  private Stock(String symbol, String price, Dividend dividend, DateTime when) {
    this.symbol = symbol;
    this.price = new Money(new BigDecimal(price));
    this.dividend = dividend;
    this.priceHistory.add(HistoricalPrice.from(when, this.price));
  }
}
