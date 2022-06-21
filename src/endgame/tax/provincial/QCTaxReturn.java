package endgame.tax.provincial;

import static endgame.util.Consts.ZERO;
import static endgame.util.Util.nonNegative;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.tax.TaxBrackets;
import endgame.util.Consts;
import endgame.util.Util;

public final class QCTaxReturn implements ProvincialTax {

  public QCTaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    this.personalAmt = fields.personalAmt;
    this.ageAmt = fields.ageAmt;
    this.pensionIncomeMax = fields.pensionIncomeMax;
    this.pensionIncomeRate = fields.pensionIncomeRate;
    this.scheduleBRate = fields.scheduleBRate;
    this.scheduleBThreshold = fields.scheduleBThreshold;
    this.dvdGrossUpMult = fields.dvdGrossUpMult;
    this.taxBrackets = fields.taxBrackets;
    this.fed = fed;
  }
  
  /** Line 450 (and line 42800 on the federal return for 2020). */
  @Override public Money netProvincialTax(){
    Money result = provincialTax();
    result = nonNegative(result.minus(nonRefundableTaxCredits()));
    result = nonNegative(result.minus(dividendTaxCredit()));
    return result;
  }
  
  /** The core tax bracket calc. Line 401. */
  public Money provincialTax() {
    return taxBrackets.taxFor(fed.taxableIncome());
  }

  /** 
   Line 399. 
   Includes personal amount, live-alone amount, age amount, pension income amount, and the lowest tax rate.
   See Schedule B. 
  */
  Money nonRefundableTaxCredits() {
    Money line350 = personalAmount();
    Money line361 = scheduleB();
    Money line377_1 = line350.plus(line361).times(lowestTaxRate());
    return line377_1;
  }
  
  /** The personal amount passed to the constructor. Line 350. */
  Money personalAmount() {
    return personalAmt;
  }
  
  /** Line 361. Line 34 of section B of Schedule B.*/
  Money scheduleB() {
    Money line18 = Util.nonNegative(fed.taxableIncome().minus(scheduleBThreshold));
    
    Money line20 = liveAloneAmount();
    Money line22 = ageAmount();
    Money line27 = pensionIncomeAmount();
    Money line30 = line20.plus(line22).plus(line27);
    
    Money line31 = line18.times(scheduleBRate);
    
    Money line34 = nonNegative(line30.minus(line31));
    return line34;
  }
  
  /** Line 20 of schedule B. The scenario file must set this to 0.00 if the person lives alone! */
  Money liveAloneAmount() {
    return liveAloneAmt;
  }

  /** Line 22 of schedule B. */
  Money ageAmount() {
    Money result = ZERO;
    if (fed.ageYearsOnly() >= Consts.STANDARD_RETIREMENT_AGE) {
      result = ageAmt;
    }
    return result;
  }

  /** Line 27 of schedule B. */
  Money pensionIncomeAmount() {
    Money result = ZERO;
    //there's no age restriction on this in QC
    result = Util.lesserOf(fed.rifIncome().times(pensionIncomeRate), pensionIncomeMax);
    return result;
  }
  
  /** Lowest rate in the lowest provincial tax bracket. */
  double lowestTaxRate() {
    return taxBrackets.lowestTaxRate();
  }
  
  /** 
   Line 415. Similar to the calc in the federal case, but uses a different multiplier. 
   Eligible dividends only.
   Ref: https://www.revenuquebec.ca/en/citizens/income-tax-return/completing-your-income-tax-return/completing-your-income-tax-return/line-by-line-help/400-to-447-income-tax-and-contributions/line-415/    
  */
  Money dividendTaxCredit() {
    Money grossUp = fed.dividendGrossUp();
    Money result = grossUp.times(dvdGrossUpMult);
    return result;
  }

  private Money personalAmt = ZERO;
  
  private Money ageAmt = ZERO;
  
  private Money liveAloneAmt = ZERO;
  
  private Money pensionIncomeMax = ZERO;
  private Double pensionIncomeRate = 0.0D;
  
  private Double scheduleBRate = 0.0D;
  private Money scheduleBThreshold = ZERO;
  
  private Double dvdGrossUpMult = 0.0D;
  
  /** The provincial tax brackets. */
  private TaxBrackets taxBrackets;
  
  /** Link to the federal tax return. */
  private FederalTaxReturn fed;
}
