package endgame.output.stats.yearly;

import static endgame.util.Consts.NL;
import static endgame.util.Consts.SPACE;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import endgame.account.Account;
import endgame.model.Money;
import endgame.security.gic.GtdInvestmentCert;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.util.Log;
import endgame.util.MoneyFormatter;

/**
 An IMMUTABLE snapshot of the state of an account.
 This class is used only for reporting, not for calculating changes to positions.
*/
public final class AccountSnapshot {
  
  public static AccountSnapshot forThe(String name, Account account, Map<String, Money> marketPrices) {
    return new AccountSnapshot(name, account, marketPrices);
  }
  
  public static AccountSnapshot forThe(String name, Money cash) {
    return new AccountSnapshot(name, cash);
  }
  
  public String name() {return name;}
  public Money cash() {return cash;}
  public Set<StockPositionSnapshot> stockPositions() {return Collections.unmodifiableSet(stockPositionSnapshots);}
  public Set<GtdInvestmentCert> gics() { return Collections.unmodifiableSet(gics); }
  
  public Money marketValue() {
    Money result = cash;
    for (StockPositionSnapshot sp : stockPositionSnapshots) {
      result = result.plus(sp.marketValue());
    }
    for (GtdInvestmentCert gic : gics) {
      result = result.plus(gic.principal());
    }
    return result;
  }
  
  /**
   This toString() is used for human-readable reporting. Example:
   <pre>
   203,737.40 -RIF-
    78,437.84 ABC 1516sh @ 51.74
   125,299.56 XYZ 813sh @ 154.12
     7,500.00 Home Trust GIC 2.5% 2023-06-21
         0.00 Cash      
   </pre>
  */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    MoneyFormatter money = new MoneyFormatter();
    result.append(money.format(marketValue()) + SPACE + "-"+name.toUpperCase()+"-" + NL);
    for (StockPositionSnapshot sp : stockPositionSnapshots) {
      result.append(money.format(sp.marketValue()) + SPACE + sp.symbol() + SPACE + sp.numShares() + "sh @ " + sp.marketPrice() + NL);
    }
    for(GtdInvestmentCert gic : gics) {
      result.append(money.format(gic.principal()) + SPACE + gic.shortName() + NL);
    }
    result.append(money.format(cash) + SPACE + "Cash" +NL);
    return result.toString();
  }
 
  // PRIVATE 
  
  private String name;
  private Money cash;
  private Set<StockPositionSnapshot> stockPositionSnapshots = new LinkedHashSet<StockPositionSnapshot>();
  private Set<GtdInvestmentCert> gics = new LinkedHashSet<>();
  
  private AccountSnapshot(String name, Account account, Map<String, Money> marketPrices) {
    this.name = name;
    this.cash = account.cash();
    for (StockPosition sp : account.stockPositions()) {
      this.stockPositionSnapshots.add(new StockPositionSnapshot(sp, marketPrices.get(sp.stock().symbol())));
    }
    for(GtdInvestmentCert gic : account.gics()) {
      this.gics.add(gic);
    }
  }
  
  private AccountSnapshot(String name, Money cash) {
    this.name = name;
    this.cash = cash;
  }
  
  /** Informal test harness. */
  private static void main(String... args) {
    
    Set<StockPosition> stockPositions = new LinkedHashSet<StockPosition>();
    String start = "2021-08-03";
    Stock enb = Stock.valueOf("ENB", "49.50", null, start);
    Stock cm = Stock.valueOf("CM", "114.20", null, start);
    stockPositions.add(StockPosition.valueOf(enb, "3400"));
    stockPositions.add(StockPosition.valueOf(cm, "1310"));
    
    Set<GtdInvestmentCert> gics = new LinkedHashSet<>();
    
    Map<String, Money> marketPrices = new LinkedHashMap<String, Money>();
    marketPrices.put("ENB", new Money("57.25"));
    marketPrices.put("CM", new Money("149.50"));

    Account account = new Account("7512.45", stockPositions, gics);
    AccountSnapshot acb = new AccountSnapshot("rif", account, marketPrices);
    Log.log(acb);
  }
}