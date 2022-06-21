package endgame.account.nra;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import endgame.account.Account;
import endgame.model.Money;
import endgame.security.gic.GtdInvestmentCert;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.tax.CapitalGainLoss;
import endgame.tax.FederalTaxReturn;
import hirondelle.date4j.DateTime;

/** 
 Non-registered account. 
 
 All operations are permitted, and most have tax consequences (book value, dividends, and 
 capital gain/loss). 
*/
public final class Nra extends Account {
  
  public static Nra valueOf(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, Set<BookValue> bookValues, FederalTaxReturn taxReturn, CapitalGainLoss capGainLoss) {
    return new Nra(cash, stocks, gics, bookValues, taxReturn, capGainLoss);
  }
  
  @Override public String toString() {
    return "NRA {cash:" + cash + " stocks:" + stockPositions + " book-values: " + bookValues + "}";  
  }
  
  /** The book value of the stock needs to be set/adjusted. */
  @Override public Money buyShares(Integer numShares, Stock stock, Money commission) {
    Money costOfAcquisition = super.buyShares(numShares, stock, commission);
    increaseBookValueBy(costOfAcquisition, stock.symbol());
    return costOfAcquisition;
  }
  
  /**
   Both the position and the book value are increased.
   In practice, it's likely very rare that stocks are transfered into this account from a registered one.
   NOTE: I can't find any documentation confirming how this operation works; I assume the book value is taken 
   from the current fair market value. 
  */
  @Override public Integer transferSharesIn(Integer inShares, Stock stock, DateTime when) {
    Money costOfAcquisition = stock.price().times(inShares);
    increaseBookValueBy(costOfAcquisition, stock.symbol());
    return super.transferSharesIn(inShares, stock, when);
  }

  /** 
   Capital gain/loss only happens during a sell. 
   If only a partial sale of the position, then pro-rating with the number of shares is used for the 
   book value and capital gain-loss. 
  */
  @Override public Money sellShares(Integer numShares, Stock stock, Money commission) {
    Optional<StockPosition> sp = lookUp(stock.symbol()); 
    Optional<BookValue> bv = lookUpBookValue(stock.symbol());
    if (bv.isEmpty() || sp.isEmpty()) {
      throw new RuntimeException("Trying to sell stock, but the account is missing the position and/or book value: " + stock.symbol());
    }
    Money proceeds = null;
    Money capitalGainLoss = null;
    Integer origNumShares = sp.get().numShares(); 
    if (origNumShares.equals(numShares)) {
      //the whole position
      Money fullBookValue = bv.get().getAmount();
      proceeds = super.sellShares(numShares, stock, commission); //position to 0, is removed
      capitalGainLoss = proceeds.minus(fullBookValue);
      
      bv.get().decrease(origNumShares, numShares); // book value to 0, is removed
      bookValues.removeIf(bookVal -> bookVal.getSymbol().equals(stock.symbol()));
    }
    else {
      //only a part of the position
      //pro-rate using the number of shares
      Money origBookValue = bv.get().getAmount();
      bv.get().decrease(origNumShares, numShares); //decrease the book value
      Money usedBookValue = origBookValue.minus(bv.get().getAmount());
      proceeds = super.sellShares(numShares, stock, commission); //decrease the position
      capitalGainLoss = proceeds.minus(usedBookValue);
    }
    processCapitalGainLoss(capitalGainLoss, SuperficialLoss.NO);
    return proceeds;
  }
  
  /**
   The transfer may be all of the position, or part thereof.
   If partial, then book value of the stock needs to be reduced (pro-rated).
  
   Transfers from an NRA into a registered account affects capital gains tax in a specific way. 
   The idea is that a 'pretend' capital gain/loss is actuated in the NRA (deemed disposition), 
   at the current market value. Gains are taxed, but losses are abandoned (superficial loss rule).
  */
  @Override public Integer transferSharesOut(Integer outShares, Stock stock, DateTime when) {
    Optional<StockPosition> sp = lookUp(stock.symbol()); 
    Optional<BookValue> bv = lookUpBookValue(stock.symbol());
    if (bv.isEmpty() || sp.isEmpty()) {
      throw new RuntimeException("Trying to transfer-out stock, but the account is missing the position and/or book value: " + stock.symbol());
    }
    Integer origNumShares = sp.get().numShares(); 
    Money proceeds = stock.price().times(outShares); //deemed disposition of the transferred-out shares!
    Money capitalGainLoss = null;
    if (origNumShares.equals(outShares)) {
      //the whole position
      Money fullBookValue = bv.get().getAmount();
      super.transferSharesOut(outShares, stock, when); //position goes to 0 and is removed (ignore the return value)
      capitalGainLoss = proceeds.minus(fullBookValue);
      
      bv.get().decrease(origNumShares, outShares); //book value goes to 0 and is removed
      bookValues.removeIf(b -> b.getSymbol().equals(stock.symbol()));
    }
    else {
      //only a part of the position
      //adjust by pro-rating with the number of shares
      Money origBookValue = bv.get().getAmount();
      bv.get().decrease(origNumShares, outShares); //book value is reduced
      Money partialBookValue = origBookValue.minus(bv.get().getAmount());
      super.transferSharesOut(outShares, stock, when); //position is reduced (ignore the return value)
      
      capitalGainLoss = proceeds.minus(partialBookValue);
    }
    processCapitalGainLoss(capitalGainLoss, SuperficialLoss.YES);
    return origNumShares;
  }
  
  /** Dividend tax credit. */
  @Override public void dividend(Money amount) {
    super.dividend(amount);
    taxReturn.addNraDivdIncome(amount);
  }

  /** 
   Explicitly separated from the stock positions, in order to keep the
   stock-position logic "clean", in the sense of avoiding code of the sort: 
   "this data only applies if non-reg". This makes for some 'parallelism' in the code.
  */
  private Set<BookValue> bookValues = new LinkedHashSet<BookValue>();
  private FederalTaxReturn taxReturn;
  private CapitalGainLoss capGainLoss;
 
  private Nra(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, Set<BookValue> bookValues, FederalTaxReturn taxReturn, CapitalGainLoss capGainLoss) {
    super(cash, stocks, gics);
    this.bookValues = bookValues;
    this.taxReturn = taxReturn;
    this.capGainLoss = capGainLoss;
  }
  
  private Optional<BookValue> lookUpBookValue(String symbol) {
    return bookValues.stream().filter(bv -> bv.getSymbol().equals(symbol)).findFirst();
  }
  
  private void increaseBookValueBy(Money costOfAcquisition, String symbol) {
    Optional<BookValue> bv = lookUpBookValue(symbol);
    if (bv.isEmpty()) {
      bookValues.add(new BookValue(symbol, costOfAcquisition));
    }
    else {
      bv.get().increaseBy(costOfAcquisition);
    }
  }

  /** Transfers out: losses can't be carried over; they are simply abandoned. */
  private enum SuperficialLoss { YES, NO; }
  
  private void processCapitalGainLoss(Money gainLoss, SuperficialLoss isSuperficial) {
    if (gainLoss.isMinus() && isSuperficial == SuperficialLoss.YES) {
      //abandon it; do nothing
    }
    else {
      capGainLoss.addGainOrLoss(taxReturn.year(), gainLoss);
    }
  }
}