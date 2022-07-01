package endgame.account.lif;

import java.util.Set;

import endgame.account.rif.Rif;
import endgame.model.Money;
import endgame.security.gic.GtdInvestmentCert;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.tax.FederalTaxReturn;
import hirondelle.date4j.DateTime;

/** 
 Self-directed Life Income Fund (LIF).
 
 Before the conversion date:
  <ul>
   <li>this object acts as a LIRA
   <li>no assets can move into or out of the account
  </ul>
  
 <P>After the conversion date:
  <ul>
   <li>there is minimum yearly withdrawal, whose logic is the same as a RIf
   <li>there is maximum yearly withdrawal, whose logic depends or your age on Jan 1, the jurisdiction of the account, and the account's value on Jan 1 of the year
   <li>withholding tax applies to amounts which exceed the minimum, same as a RIF
  </ul>

 <P>If the conversion date is in year n, the min-max logic starts in year n+1.
*/
public final class Lif extends Rif {

  public static Lif valueOf(
    String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, FederalTaxReturn taxReturn, 
    String liraToRifConversionDate, String jurisdiction, String dob
  ) {
    return new Lif(cash, stocks, gics, taxReturn, liraToRifConversionDate, jurisdiction, dob);
  }
  
  /** 
   Counts as taxable income.
   Returns the withholding tax, if any.
   An error occurs if you call this method before the conversion date. 
   It fails if you try to withdraw too much cash. 
  */
  @Override public Money withdrawCash(Money grossAmount, DateTime when) {
    if (when.gteq(conversionDate)) {
      super.baseWithdrawCash(grossAmount, when);
      Money withholdingTax = taxReturn.addLifIncome(grossAmount);
      return withholdingTax;
    }
    else {
      blowUp();
      return null;
    }
  }
  
  /** 
   Counts as taxable income, using fair market value on the date of the transfer.
   Possible withholding tax, to be paid with funds already in the account! 
   An error occurs if you call this method before the conversion date. 
   It fails if you try to withdraw too many shares, or if the account has no such position. 
  */
  @Override public Integer transferSharesOut(Integer numShares, Stock stock, DateTime when) {
    if (when.gteq(conversionDate)) {
      Integer numOrigShares = baseTransferSharesOut(numShares, stock, when);
      Money marketValue = stock.price().times(numShares);
      taxReturn.addLifIncome(marketValue);
      return numOrigShares;
    }
    else {
      blowUp();
      return null;
    }
  }
  
  /** ON for Ontario, etc. CA for federal. */
  public String jurisdiction() { return jurisdiction; }

  /** The yearly max you can withdraw from the account. */
  public Money withdrawalMax(Money accountValueOnJan1, Integer year) {
    LifMaxima max = new LifMaxima(dateOfBirth);
    return max.withdrawalMax(accountValueOnJan1, year, conversionDate, jurisdiction);
  }

  @Override public String toString() {
    return 
      "Lif {cash:" + cash + " stocks:" + stockPositions + " GICs:" + gics + " jurisdiction:" + jurisdiction + 
      "date of birth" + dateOfBirth + " conversion date:" + conversionDate + 
    "}";  
  }
  
  /** ON etc. CA for federal. */
  private String jurisdiction;
  
  private Lif(
    String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, FederalTaxReturn taxReturn, 
    String conversionDate, String jurisdiction, String dob
  ) {
    super(cash, stocks, gics, taxReturn, conversionDate, dob);
    this.jurisdiction = jurisdiction;
  }
}
