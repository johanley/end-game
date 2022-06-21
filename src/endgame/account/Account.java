package endgame.account;

import static endgame.util.Consts.ZERO;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import endgame.bank.BankAccount;
import endgame.model.Money;
import endgame.security.gic.GtdInvestmentCert;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** 
 Core operations on an investment account, with NO tax consequences.
 For your bank account, see {@link BankAccount}.
 
 <P>The core operations on an account are fairly simple. 
 The real complexity comes from tax considerations.
 
 <P>This class is designed for subclassing. All methods are overridable.
 If an operation is not applicable for a given account, then an exception must be thrown. 
 
 <P>A subclass that has tax consequences for a given operation 
 will typically first call a method in this base class (using <tt>super</tt>), and then perform further operations.
 
 <P>Design note: some method parameters are not used by all implementations (for example, the DateTime param).
 In those cases, the params are simply ignored. 
 This was chosen because it is simpler to ignore unneeded data, than to pass items around.
*/
public class Account {
  
  public Account(String cash, Set<StockPosition> stockPositions, Set<GtdInvestmentCert> gics) {
    this.cash = new Money(new BigDecimal(cash));
    this.stockPositions = stockPositions;
    this.gics = gics;
  }

  /** The current market value, cash plus securities. */
  public Money value() {
    Money result = ZERO;
    result = result.plus(cash);
    for(StockPosition sp : stockPositions) {
      Money marketValue = sp.stock().price().times(sp.numShares());
      result = result.plus(marketValue);
    }
    for(GtdInvestmentCert gic : gics) {
      result = result.plus(gic.principal());
    }
    return result;
  }
  
  public Money cash() {  return cash; }
  /** The stocks held by the account. */
  public Set<StockPosition> stockPositions() { return Collections.unmodifiableSet(stockPositions); }
  /** The GICs held by the account. */
  public Set<GtdInvestmentCert> gics(){ return Collections.unmodifiableSet(gics);}
  
  public void depositCash(Money amount, DateTime when) {
    cash = cash.plus(amount);
  }
  /** Returns the withholding tax (if any). */
  public Money withdrawCash(Money amount, DateTime when) {
    if (cash.lt(amount)){
      throw new RuntimeException("Can't withdraw more money than you have.");
    }
    cash = cash.minus(amount);
    return ZERO;
  }

  /** Returns the total cost, including commission (used for capital gain/loss). */
  public Money buyShares(Integer numShares, Stock stock, Money commission) {
    Money costToMe = stock.price().times(numShares).plus(commission);
    if (cash.lt(costToMe)) {
      throw new RuntimeException("Can't buy stock because the account has insufficient cash.");
    }
    cash = cash.minus(costToMe);
    increasePosition(numShares, stock);
    return costToMe;
  }
  /** Returns the total proceeds of the sale, less commission (used for capital gain/loss). */
  public Money sellShares(Integer numShares, Stock stock, Money commission) {
    Money proceeds = stock.price().times(numShares).minus(commission);
    cash = cash.plus(proceeds);
    reducePosition(numShares, stock);
    return proceeds;
  }
  
  /** Returns the original number of shares in the position. Increase an existing position, or create new. */
  public Integer transferSharesIn(Integer numShares, Stock stock, DateTime when) {
    return increasePosition(numShares, stock);
  }
  /** 
   Returns the original number of shares in the position.
   Throws an exception if no such position is found, or if insufficient shares are found. 
  */
  public Integer transferSharesOut(Integer numShares, Stock stock, DateTime when) {
    return reducePosition(numShares, stock);
  }
  
  /**
   Buy a GIC and hold it in the account.
   In this simplified implementation, an account always holds a GIC until it matures. 
  */
  public void buy(GtdInvestmentCert gic) {
    if (gics.contains(gic)) {
      throw new RuntimeException("Can't buy a GIC because the account already holds it.");
    }
    Money cost = gic.principal();
    if (cash.lt(cost)) {
      throw new RuntimeException("Trying to buy a GIC for " + cost + " but the account has only " + cash);
    }
    cash = cash.minus(cost);
    gics.add(gic);
  }
  /**
   Redeem a GIC that is being held in the account. 
   Cash in for principal plus interest, and remove the GIC from the account holdings.
   Return the proceeds of the redemption. 
  */
  public Money redeem(GtdInvestmentCert gic) {
    Money result = Consts.ZERO;
    if (!gics.contains(gic)) {
      throw new IllegalStateException("Trying to redeem a GIC, but can't find it in the account.");
    }
    result = gic.redemptionValue();
    cash = cash.plus(result);
    gics.remove(gic);
    return result;
  }
  
  /** Note that this simulation assumes all dividends are eligible dividends. */
  public void dividend(Money amount) {
    cash = cash.plus(amount);
  }
  
  /** Returns nothing if no position is held by this account for the given stock. */
  public Optional<StockPosition> positionFor(Stock stock) {
    Optional<StockPosition> result = Optional.empty();
    for(StockPosition sp : stockPositions) {
      if (sp.stock().symbol().equals(stock.symbol())) {
        result = Optional.of(sp);
        break;
      }
    }
    return result;
  }
  
  // toString is left out, since all objects will be subclasses of Account.

  protected Money cash;
  protected Set<StockPosition> stockPositions = new LinkedHashSet<>();
  protected Set<GtdInvestmentCert> gics = new LinkedHashSet<>();
  
  protected void blowUp() {
    throw new RuntimeException("Unsupported operation.");
  }
  
  protected Optional<StockPosition> lookUp(String symbol) {
    return stockPositions.stream().filter(sp -> sp.stock().symbol().equals(symbol)).findFirst();
  }
  
  /** 
   Reduce existing. Remove if no more left. 
   Blow up if removing too many, or if no position found.
   Return the original number of shares. 
  */
  private Integer reducePosition(Integer numShares, Stock stock) {
    Integer result = 0;
    Optional<StockPosition> position = lookUp(stock.symbol());
    if (position.isPresent()) {
      if (numShares > position.get().numShares()) {
        throw new IllegalStateException("Sell/transfer-out " + numShares + " of " + stock.symbol() + ", but position is only " + position.get().numShares());
      }
      result = position.get().numShares();
      position.get().decrease(numShares);
      stockPositions.removeIf(pos -> pos.numShares().equals(0));
    }
    else {
      throw new IllegalStateException("Sell/transfer-out for shares of " + stock.symbol() + ", but no position found.");
    }
    return result;
  }

  /** Increase existing, or create new. Return the original number of shares. */
  private Integer increasePosition(Integer numShares, Stock stock) {
    Integer result = 0;
    Optional<StockPosition> position = lookUp(stock.symbol());
    if (position.isPresent()) {
      result = position.get().numShares();
      position.get().increase(numShares);
    }
    else {
      stockPositions.add(new StockPosition(stock, numShares));
    }
    return result;
  }
}
