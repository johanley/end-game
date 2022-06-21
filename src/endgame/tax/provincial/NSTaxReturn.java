package endgame.tax.provincial;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.util.Consts;
import endgame.util.Util;

/** 
 Nova Scotia provincial tax return.
 
 <P>The implementation logic is as of 2020, but may be valid for later years.
*/
public class NSTaxReturn extends GENERICTaxReturn {
  
  public NSTaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    super(fields, fed);
    this.personalAmtThreshold = fields.personalAmtThreshold;
    this.personalAmtSupplement = fields.personalAmtSupplement;
    this.personalAmtRate = fields.personalAmtRate;
    
    this.ageAmtSupplement = fields.ageAmtSupplement;
    this.ageAmtSupplementThreshold = fields.ageAmtSupplementThreshold;
    this.ageAmtSupplementRate = fields.ageAmtSupplementRate;
    
    this.lowIncomeBasic = fields.lowIncomeBasic;
    this.lowIncomeThreshold = fields.lowIncomeThreshold;
    this.lowIncomeRate = fields.lowIncomeRate;

    this.ageTaxCredit = fields.ageTaxCredit;
    this.ageTaxCreditThreshold = fields.ageTaxCreditThreshold;
  }
    
  /** Line 92. */
  @Override public Money netProvincialTax(){
    Money result = super.netProvincialTax();
    result = result.minus(lowIncomeReduction());
    result = result.minus(ageTaxCredit());
    return Util.nonNegative(result);
  }

  /** Line 9 (58040). */
  @Override Money personalAmount() {
    Money line1 = personalAmt;
    Money line8 = Util.baseMinusClawback(personalAmtSupplement, fed.taxableIncome(), personalAmtThreshold, personalAmtRate);
    return line1.plus(line8);
  }
  
  /** Line 10 (58080). */
  @Override Money ageAmount() {
    Money line7 = fed.ageAmountCalc(ageAmt, ageAmtThreshold);
    Money line14 = Util.baseMinusClawback(ageAmtSupplement, fed.taxableIncome(), ageAmtSupplementThreshold, ageAmtSupplementRate);
    return line7.plus(line14);
  }

  /** Line 76. */
  private Money lowIncomeReduction() {
    return Util.baseMinusClawback(lowIncomeBasic, fed.netIncome(), lowIncomeThreshold, lowIncomeRate);
  }
  
  /** Line 91. */
  private Money ageTaxCredit() {
    Money result = Consts.ZERO;
    if (fed.ageYearsOnly() >= Consts.STANDARD_RETIREMENT_AGE) {
      if (fed.taxableIncome().lt(ageTaxCreditThreshold) ) {
        result = ageTaxCredit;
      }
    }
    return result;
  }
 
  /** Line 2.*/
  private Money personalAmtSupplement;
  /** Line 4.*/
  private Money personalAmtThreshold;
  /** Line 6.*/
  private Double personalAmtRate;
 
  /** Line 8.*/
  private Money ageAmtSupplement;
  /** Line 10.*/
  private Money ageAmtSupplementThreshold;
  /** Line 12.*/
  private Double ageAmtSupplementRate;
  
  /** Line 65.*/
  private Money lowIncomeBasic;
  /** Line 72.*/
  private Money lowIncomeThreshold;
  /** Line 74.*/
  private Double lowIncomeRate;
  
  /** Line 91.*/
  private Money ageTaxCreditThreshold;
  /** Line 91.*/
  private Money ageTaxCredit;
}