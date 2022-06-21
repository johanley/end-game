package endgame.tax.provincial;

import static endgame.util.Consts.ZERO;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.util.Consts;

/** 
 Prince Edward Island provincial tax return.

 <P>The implementation logic is as of 2020, but may be valid for later years.
 <P>This implementation subclasses the implementation for NB, because it needs to overrides one of its methods.
*/
public final class PETaxReturn extends NBTaxReturn {
  
  public PETaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    super(fields, fed);
    this.lowIncomeAge = fields.lowIncomeAge;
  }
  
  /** Override the method for NB to include the age reduction specific to PE. Line 79. */
  @Override protected Money basicEtc() {
    Money line74 = ageReduction();
    return lowIncomeBasic.plus(line74);
  }
  
  /** Line 63380. */
  private Money lowIncomeAge;

  /** Line 63380. */
  private Money ageReduction() {
    Money result = ZERO;
    int numYearsSinceBorn = fed.ageYearsOnly();
    if (numYearsSinceBorn >= Consts.STANDARD_RETIREMENT_AGE) {
      result = lowIncomeAge;
    }
    return result;
  }
}
