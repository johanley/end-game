package endgame.account.rif;

import java.util.Set;

import endgame.account.Account;
import endgame.model.Money;
import endgame.security.gic.GtdInvestmentCert;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.tax.FederalTaxReturn;
import hirondelle.date4j.DateTime;

/** 
 Self-directed Retirement Income Fund (RIF).
 
 Before the conversion date, the account:
 <ul> 
  <li>acts like an RSP to which no contributions are made
  <li>has no minimum annual withdrawal
  <li>has withholding tax applied to all withdrawals
 </ul>

 <P>After the conversion date:
  <ul>
   <li>there is minimum yearly withdrawal
   <li>the minimum depends on your age on Jan 1, and the value of the account on Jan 1
   <li>withholding tax applies to amounts above the minimum
  </ul>
 
 <P>If the conversion date is in year n, the minimum-logic starts in year n+1.
*/
public class Rif extends Account {

  public static Rif valueOf(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, FederalTaxReturn taxReturn, String conversionDate, String dob) {
    return new Rif(cash, stocks, gics, taxReturn, conversionDate, dob);
  }
  
  /** Not permitted. */
  @Override public void depositCash(Money amount, DateTime when) { 
    blowUp();  
  }
  
  /** Not permitted. */
  @Override public Integer transferSharesIn(Integer numShares, Stock stock, DateTime when) { 
    blowUp();  return null;
  }
  
  /** 
   Counts as taxable income.
   Returns the withholding tax, if any. 
  */
  @Override public Money withdrawCash(Money grossAmount, DateTime when) {
    super.withdrawCash(grossAmount, when); 
    Money withholdingTax = taxReturn.addRifIncome(grossAmount);
    return withholdingTax;
  }
  
  /** 
   Counts as taxable income, using fair market value on the date of the transfer.
   Possible withholding tax, to be paid with funds already in the account.
  */
  @Override public Integer transferSharesOut(Integer numShares, Stock stock, DateTime when) {
    Integer numOrigShares = super.transferSharesOut(numShares, stock, when);
    Money marketValue = stock.price().times(numShares);
    taxReturn.addRifIncome(marketValue);
    return numOrigShares;
  }

  @Override public String toString() {
    return "Rif {cash:" + cash + " stocks:" + stockPositions + " GICs:" + gics + " date of birth:" + dateOfBirth + " conversion date:" + conversionDate + "}";  
  }
  
  /** When the RSP was converted to a RIF. */
  public DateTime conversionDate() {
    return conversionDate;
  }
  
  /** The yearly minimum you can withdraw from the account. */
  public Money withdrawalMin(Money accountValueOnJan1, Integer year) {
    RifLifMinima min = new RifLifMinima(dateOfBirth);
    return min.compute(accountValueOnJan1, year, conversionDate);
  }
  
  protected Rif(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, FederalTaxReturn taxReturn, String rspToRifConversionDate, String dob) {
    super(cash, stocks, gics);
    this.taxReturn = taxReturn;
    this.conversionDate = new DateTime(rspToRifConversionDate);
    this.dateOfBirth = new DateTime(dob);
    validateTheConversionDate();
  }

  protected DateTime dateOfBirth;
  protected DateTime conversionDate;
  protected FederalTaxReturn taxReturn;
  
  /** Provided to add access to this code from the Lif subclass. */
  protected Money baseWithdrawCash(Money grossAmount, DateTime when) {
    return super.withdrawCash(grossAmount, when); 
  }
  
  /** Provided to add access to this code from the Lif subclass. */
  protected Integer baseTransferSharesOut(Integer numShares, Stock stock, DateTime when) {
    return super.transferSharesOut(numShares, stock, when);
  }
  
  /** Last day is x-12-31, where x is the year you turn 71. */
  private void validateTheConversionDate() {
    Integer yearTurn71 = dateOfBirth.getYear() + 71; 
    DateTime lastChance = new DateTime(yearTurn71 + "-12-31");
    if (conversionDate.gt(lastChance)){
      throw new RuntimeException("Conversion date " + conversionDate + " is too late. The last day is " + lastChance);
    }
  }
}
