package endgame.output.stats.yearly;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import endgame.account.Account;
import endgame.bank.BankAccount;
import endgame.model.Money;
import endgame.security.stock.StockPosition;
import endgame.tax.FederalTaxReturn;
import hirondelle.date4j.DateTime;

/** 
 Yearly snapshots and summaries for a single history (a single iteration).
 At the end of each year, add objectst to the data structures in this class.
*/
public final class History {
  
  public Map<Integer, CashFlow> cashFlow = new LinkedHashMap<>();
  public Map<Integer, TaxSummary> taxSummary = new LinkedHashMap<>();
  public Map<Integer, AccountSet> accountSet = new LinkedHashMap<>();
  
  /** The number of years in this history. */
  public int numYears() {
    int result = cashFlow.keySet().size();
    if (taxSummary.keySet().size() != result || accountSet.keySet().size() != result) {
      throw new RuntimeException("Coding error. Not all maps are the same size.");
    }
    return result;
  }
  
  /** Take a snapshot of all accounts at the end of the year. */
  public void takeSnapshotOf(List<Account> investmentAccts, BankAccount bank, DateTime when) {
    AccountSet acctSet = new AccountSet();
    for (Account investmentAcct : investmentAccts) {
      Map<String, Money> marketValues = new LinkedHashMap<String, Money>();
      for(StockPosition sp : investmentAcct.stockPositions()) {
        marketValues.put(sp.stock().symbol(), sp.stock().price());
      }
      AccountSnapshot snap = AccountSnapshot.forThe(investmentAcct.getClass().getSimpleName(), investmentAcct, marketValues);
      acctSet.add(snap);
    }
    AccountSnapshot bankSnap = AccountSnapshot.forThe(bank.getClass().getSimpleName(), bank.value());
    acctSet.add(bankSnap);
    accountSet.put(when.getYear(), acctSet);
  }
  
  /** Take a snapshot of a tax return at the end of the year. */
  public void takeSnapshotOf(FederalTaxReturn tr) {
    TaxSummary result = new TaxSummary(tr);
    taxSummary.put(tr.year(), result);
  }
}
