package endgame.output.stats.yearly;

import static endgame.util.Consts.NL;
import static endgame.util.Consts.SPACE;
import static endgame.util.Consts.ZERO;

import java.util.Collection;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.util.MoneyFormatter;

/** 
 An immutable snapshot of a completed tax return.
 Contains most but not all of the full data-set of a tax return.
 (If something is missing, it can be added in later.) 
*/
public final class TaxSummary {
  
  public Integer year;
  public Money taxableIncome = ZERO;
  public Money federalTax = ZERO;
  public Money provincialTax = ZERO;
  public Money taxPayable = ZERO;
  public Money dividendTaxCredit = ZERO;
  public Money installments = ZERO;
  public Money balanceOwing = ZERO;
  
  public Money netIncome = ZERO;
  public Money netIncomeBeforeAdjustments = ZERO;
  public Money oas = ZERO;
  public Money employmentIncome = ZERO;
  public Money rifLifIncome = ZERO;
  
  public TaxSummary(FederalTaxReturn tr) {
    this.year = tr.year();
    this.taxableIncome = tr.taxableIncome();
    this.federalTax = tr.netFederalTax();
    this.provincialTax = tr.netProvincialTax();
    this.taxPayable = tr.totalPayable();
    this.dividendTaxCredit = tr.dividendTaxCredit();
    this.installments = tr.installments();
    this.balanceOwing = tr.balanceOwing();
    this.netIncome = tr.netIncome();
    this.netIncomeBeforeAdjustments = tr.netIncomeBeforeAdjustments();
    this.oas = tr.oasIncome();
    this.employmentIncome = tr.employmentIncome();
    this.rifLifIncome = tr.rifIncome().plus(tr.lifIncome());
  }
  
  /** Sum all of the monetary items in a completed list of {@link TaxSummary} objects. */
  public static TaxSummary sumOver(Collection<TaxSummary> taxReturnHistory) {
    TaxSummary result = new TaxSummary();
    for (TaxSummary trs : taxReturnHistory) {
      result.balanceOwing = result.balanceOwing.plus(trs.balanceOwing);
      result.dividendTaxCredit = result.dividendTaxCredit.plus(trs.dividendTaxCredit);
      result.federalTax = result.federalTax.plus(trs.federalTax);
      result.installments = result.installments.plus(trs.installments);
      result.provincialTax = result.provincialTax.plus(trs.provincialTax);
      result.taxableIncome = result.taxableIncome.plus(trs.taxableIncome);
      result.taxPayable = result.taxPayable.plus(trs.taxPayable);
      result.netIncome = result.netIncome.plus(trs.netIncome);
      result.netIncomeBeforeAdjustments = result.netIncomeBeforeAdjustments.plus(trs.netIncomeBeforeAdjustments);
      result.oas = result.oas.plus(trs.netIncome);
      result.employmentIncome = result.employmentIncome.plus(trs.employmentIncome);
      result.rifLifIncome = result.rifLifIncome.plus(trs.rifLifIncome);
    }
    return result;
  }
  
  public static TaxSummary sumOverPerYear(Collection<TaxSummary> taxReturnHistory) {
    TaxSummary result = sumOver(taxReturnHistory);
    int numYears = taxReturnHistory.size();
    result.balanceOwing = result.balanceOwing.divByInt(numYears);
    result.dividendTaxCredit = result.dividendTaxCredit.divByInt(numYears);
    result.federalTax = result.federalTax.divByInt(numYears);
    result.installments = result.installments.divByInt(numYears);
    result.provincialTax = result.provincialTax.divByInt(numYears);
    result.taxableIncome = result.taxableIncome.divByInt(numYears);
    result.taxPayable = result.taxPayable.divByInt(numYears);
    result.netIncome = result.netIncome.divByInt(numYears);
    result.oas = result.oas.divByInt(numYears);
    result.employmentIncome = result.employmentIncome.divByInt(numYears);
    result.rifLifIncome = result.rifLifIncome.divByInt(numYears);
    return result;
  }

  /** This toString is used for human-readable reporting. */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    String yr = year == null ? "" : year.toString();
    result.append("Tax return" + SPACE + yr + NL);
    result.append(lineFor(taxableIncome, "Taxable income"));
    result.append(lineFor(federalTax, "Federal tax"));
    result.append(lineFor(provincialTax, "Provincial tax"));
    result.append(lineFor(taxPayable, "Tax Payable"));
    result.append(lineFor(dividendTaxCredit, "Dividend tax credits"));
    result.append(lineFor(installments, "Installments"));
    result.append(lineFor(balanceOwing, "Balance owing"));
    result.append(lineFor(netIncome, "Net income"));
    result.append(lineFor(netIncomeBeforeAdjustments, "Net income before adj"));
    result.append(lineFor(oas, "OAS income"));
    result.append(lineFor(employmentIncome, "Employment income"));
    result.append(lineFor(rifLifIncome, "RIF-LIF withdrawals"));
    return result.toString();
  }

  private MoneyFormatter money = new MoneyFormatter();
  
  private TaxSummary() {}
 
  private String lineFor(Money amount, String name) {
    return money.format(amount) + SPACE + name + NL;
  }
}