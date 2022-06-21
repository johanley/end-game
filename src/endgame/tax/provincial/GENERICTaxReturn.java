package endgame.tax.provincial;

import static endgame.util.Consts.ZERO;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.tax.TaxBrackets;
import endgame.util.Util;

/** 
 A generic provincial tax return, with a minimal number of core inputs.
 
 All methods are can be overridden.
 
 <P>For reference and clarity, line numbers are stated for <b>the NB428 tax return of 2020, and its associated worksheet</b>. 
 This doesn't prevent this class from being useful for other jurisdictions.
*/
public class GENERICTaxReturn implements ProvincialTax {

  GENERICTaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    this.personalAmt = fields.personalAmt;
    this.ageAmt = fields.ageAmt;
    this.ageAmtThreshold = fields.ageAmtThreshold;
    this.pensionIncomeMax = fields.pensionIncomeMax;
    this.dvdGrossUpMult = fields.dvdGrossUpMult;
    this.taxBrackets = fields.taxBrackets;
    this.fed = fed;
  }
  
  /** Line 92 (and line 42800 on the federal return for 2020). */
  @Override public Money netProvincialTax(){
    Money result = provincialTax();
    result = result.minus(nonRefundableTaxCredits());
    result = result.minus(dividendTaxCredit());
    return Util.nonNegative(result);
  }
  
  /** The core tax bracket calc. Line 8 and line 49. */
  public Money provincialTax() {
    return taxBrackets.taxFor(fed.taxableIncome());
  }

  /** Includes personal amount, age amount, pension income amount, and the lowest tax rate.  Line 48 (61500). */
  Money nonRefundableTaxCredits() {
    Money result = personalAmount();
    result = result.plus(ageAmount());
    result = result.plus(pensionIncomeAmount());
    result = result.times(lowestTaxRate());
    return result;
  }
  
  /** The personal amount passed to the constructor, with no clawback. Line 9 (58040). */
  Money personalAmount() {
    return personalAmt;
  }

  /** Age amount, same calc as at the federal level, with a clawback. Line 7 of the worksheet, and line 58080. */
  Money ageAmount() {
    return fed.ageAmountCalc(ageAmt, ageAmtThreshold);
  }

  /** Pension income amount, same calc as the federal level. Line 25 (58360). */
  Money pensionIncomeAmount() {
    return fed.pensionIncomeAmountCalc(pensionIncomeMax);
  }
  
  /** Lowest rate in the lowest provincial tax bracket. */
  double lowestTaxRate() {
    return taxBrackets.lowestTaxRate();
  }
  
  /** Similar to the calc in the federal case, but uses a different multiplier. Line 55 (61520). Eligible dividends only. */
  Money dividendTaxCredit() {
    Money grossUp = fed.dividendGrossUp();
    Money result = grossUp.times(dvdGrossUpMult);
    return result;
  }

  /** Line 9 (58040). */
  Money personalAmt = ZERO;
  
  /** Line 58080 (1) on the worksheet. */
  Money ageAmt = ZERO;
  
  /** Line 58080 (3) on the worksheet. */
  Money ageAmtThreshold = ZERO;
  
  /** Line 25 (58360). */
  Money pensionIncomeMax = ZERO;
  
  /** Line 61520 (2) on the worksheet. */
  Double dvdGrossUpMult = 0.0D;
  
  /** The provincial tax brackets. */
  TaxBrackets taxBrackets;
  
  /** Link to the federal tax return. */
  FederalTaxReturn fed;
}
