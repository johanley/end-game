package endgame.security.stock.liquidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.security.stock.Stock.HistoricalPrice;
import endgame.security.stock.StockPosition;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 Sell stocks in a defined order, first by account and then by stock.
 
 <P>The amount to be sold (gross, before commission) can be stated as a dollar amount or as a percentage of your investments.
*/
public final class SequentialLiquidation extends Transactional implements Liquidate {

  /**
   Sell stocks from your accounts.
   Don't sell a stock if its current price is below any of the prices of the past few years. 
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    List<Sale> sales = sellStock(when, sim);
    sim.yearlyCashFlows.liquidationProceeds = sim.yearlyCashFlows.liquidationProceeds.plus(totalProceedsOf(sales));
    logMe(when, sales);
  }
  
  /**
   Factory method.
   The caller passes either an amount or a percent, but not both; one must be null.
   
   @param avoidDownturnYears don't sell if the current price is less than any price in the past, up to 
   this number of years ago. To turn this feature off, set to 0.
   @param accounts the order of which defines the order in which accounts are liquidated.
   @param stocks the order of which defines the order in which stocks are liquidated.
   @param amount the gross amount to be sold.
   @param percent the percent of your investment holdings to be sold.
  */
  public static SequentialLiquidation valueOf(String avoidDownturnYears, List<Account> accounts, List<Stock> stocks, String amount, String percent, String when) {
    Money amt = Util.isPresent(amount) ? new Money(amount) : null;
    Double pct = Util.isPresent(percent) ? Util.percentFrom(percent) : null;
    if (amt != null && pct != null) {
      throw new IllegalArgumentException("One of amount and percent must be null.");
    }
    return new SequentialLiquidation(avoidDownturnYears, accounts, stocks, amt, pct, when);
  }
  
  /**
   Liquidate accounts/stocks in a specific sequence, by selling shares.
   
   <P>The order of preference (sequence) used to decide what exactly to sell is defined by the items passed to the factory method.
   In some border cases, multiple stocks/accounts may be used in order to generate the desired gross amount for a given year. 
   In some scenarios, you may never fully deplete the first account specified. 
  */
  @Override public List<Sale> sellStock(DateTime when, Scenario sim) {
    List<Sale> sales = new ArrayList<Sale>();
    Money totalGrossSold = Consts.ZERO;
    Money targetGross = grossAmount(when, sim);
    //UNUSUAL: need to exit from multiple loops; using a label to do that:
    sellShares:
    for (Account account : accounts) {
      for(Stock stock : stocks) {
        Optional<StockPosition> position = account.positionFor(stock);
        if (position.isPresent()) {
          if (!recentDownturnFor(stock)) {
            Sale sale = numSharesToSell(position.get(), targetGross.minus(totalGrossSold));
            if (sale.numShares > 0) {
              Money commission = sim.commission.commissionOn(sale.numShares, position.get().stock().price());
              sale.proceeds = account.sellShares(sale.numShares, position.get().stock(), commission);
              sale.account = account.getClass().getSimpleName();
              totalGrossSold = totalGrossSold.plus(sale.gross);
              sales.add(sale);
              if (sale.isPartial) {
                break sellShares; //exit the account loop
              }
            }
          }
        }
      }
    }
    return sales;
  }

  /** 
   Return true only if any historical price from the last few years is ABOVE the current price.
   Returns false if you have passed 0 to the constructor for <tt>avoidDownturnYears</tt>.
  */
  boolean recentDownturnFor(Stock stock) {
    boolean result = false;
    if (avoidDownturnYears > 0 && stock.priceHistory().size() > 0) {
      List<HistoricalPrice> oldPrices = stock.priceHistory();
      int currentYear = oldPrices.get(oldPrices.size()-1).when().getYear();
      int lookBackUntilYear = currentYear - avoidDownturnYears;
      //interesting: this loop can exit in 3 different ways!
      for (int i = oldPrices.size() - 1; i >= 0; --i) {
        HistoricalPrice historical = oldPrices.get(i);
        if (historical.when().getYear() < lookBackUntilYear) {
          break; //stop iterating
        }
        if (historical.price().gt(stock.price())) {
          result = true;
          break;
        }
      }
    }
    return result;
  }
  
  @Override public String toString() {
    return "LIQUIDATE: ";
  }
  
  // PRIVATE 
 
  private Integer avoidDownturnYears;
  private List<Account> accounts;
  private List<Stock> stocks;
  private Money amount;
  private Double percent;
  
  private SequentialLiquidation(String avoidNumYears, List<Account> accounts, List<Stock> stocks, Money amount, Double percentage, String when) {
    super(when);
    this.avoidDownturnYears = Integer.valueOf(avoidNumYears);
    this.accounts = accounts;
    this.stocks = stocks;
    this.amount = amount;
    this.percent = percentage;
  }
  
  private Money grossAmount(DateTime when, Scenario sim) {
    return amount != null ? amount : sim.investmentsWorth().times(percent);
  }
  
  /** 
   Sell all or part of a position.
   Selling a partial position only happens when selling all of the position would result in 
   selling more than the target amount. 
  */ 
  private Sale numSharesToSell(StockPosition position, Money remaining) {
    Sale result = new Sale();
    result.symbol = position.stock().symbol();
    Money posGross = position.marketValue();
    if (posGross.lteq(remaining)) {
      //all shares can be sold
      result.isPartial = Boolean.FALSE;
      result.numShares = position.numShares();
      result.gross = posGross;
    }
    else {
      //sell a part of the position, but not all
      //if this is true, then this is the final sale of shares this year, and 
      //no others should be sold after this 
      result.isPartial = Boolean.TRUE;
      double price = position.stock().price().asDouble();
      result.numShares = remaining.flooredDiv(price);
      result.gross = position.stock().price().times(result.numShares);
    }
    return result;
  }
  
  private Money totalProceedsOf(List<Sale> sales) {
    Money result = Consts.ZERO;
    for (Sale sale : sales) {
      result = result.plus(sale.proceeds);
    }
    return result;
  }
}