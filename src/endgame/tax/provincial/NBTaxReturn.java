package endgame.tax.provincial;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.util.Util;

/** 
 New Brunswick provincial tax return.
 
 <P>The implementation logic is as of 2020, but may be valid for later years.
*/
public class NBTaxReturn extends GENERICTaxReturn {
  
  public NBTaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    super(fields, fed);
    this.lowIncomeBasic = fields.lowIncomeBasic;
    this.lowIncomeThreshold = fields.lowIncomeThreshold;
    this.lowIncomeRate = fields.lowIncomeRate;
  }
    
  /** Adds the low income reduction to the generic provincial return. Line 92. */
  @Override public Money netProvincialTax(){
    Money line71 = super.netProvincialTax();
    Money line81 = lowIncomeReduction();
    Money line82 = line71.minus(line81);
    return Util.nonNegative(line82);
  }
  
  /** Line 72 (61570). */
  protected Money basicEtc() {
    // overridden by PE, to include an age reduction amount as well
    return lowIncomeBasic;
  }

  /** Line 81. */
  private Money lowIncomeReduction() {
    return Util.baseMinusClawback(basicEtc(), fed.netIncome(), lowIncomeThreshold, lowIncomeRate);
  }
  
  /** Line 72 (61570). */
  protected Money lowIncomeBasic;
  
  /** Line 77. */
  private Money lowIncomeThreshold;
  
  /** Line 79. */
  private Double lowIncomeRate;
}