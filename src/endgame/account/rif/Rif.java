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
 
 See the {@link endgame.account.rsp} package for information about RSPs
*/
public final class Rif extends Account {

  public static Rif valueOf(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, FederalTaxReturn taxReturn, String rspToRifConversionDate) {
    return new Rif(cash, stocks, gics, taxReturn, rspToRifConversionDate);
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
   Possible withholding tax, to be paid with funds already in the account! 
  */
  @Override public Integer transferSharesOut(Integer numShares, Stock stock, DateTime when) {
    Integer numOrigShares = super.transferSharesOut(numShares, stock, when);
    Money marketValue = stock.price().times(numShares);
    taxReturn.addRifIncome(marketValue);
    return numOrigShares;
  }

  @Override public String toString() {
    return "Rif {cash:" + cash + " stocks:" + stockPositions + " GICs:" + gics + "}";  
  }
  
  public DateTime rspToRifConversionDate() {
    return rspToRifConversionDate;
  }
  
  /** Last day is x-12-31, where x is the year you turn 71. */
  public void validateTheRspConversionDate(String dateOfBirth) {
    DateTime dob = new DateTime(dateOfBirth);
    Integer yearTurn71 = dob.getYear() + 71; 
    DateTime lastChance = new DateTime(yearTurn71 + "-12-31");
    if (rspToRifConversionDate.gt(lastChance)){
      throw new RuntimeException("Rsp-to-rif conversion date " + rspToRifConversionDate + " is too late. The last day is " + lastChance);
    }
  }
  
  private FederalTaxReturn taxReturn;
  private DateTime rspToRifConversionDate;
  
  private Rif(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, FederalTaxReturn taxReturn, String rspToRifConversionDate) {
    super(cash, stocks, gics);
    this.taxReturn = taxReturn;
    this.rspToRifConversionDate = new DateTime(rspToRifConversionDate);
  }
  
}
