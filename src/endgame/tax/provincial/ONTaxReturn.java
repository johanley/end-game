package endgame.tax.provincial;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.tax.TaxBrackets;
import endgame.util.Util;

/** 
 Ontario provincial tax return.

 <P>The implementation logic is as of 2020, but may be valid for later years.
*/
public final class ONTaxReturn implements ProvincialTax {
  
  public ONTaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    generic = new GENERICTaxReturn(fields, fed);
    this.lowIncomeBasic = fields.lowIncomeBasic;
    this.surtaxThreshold1 = fields.surtaxThreshold1;
    this.surtaxRate1 = fields.surtaxRate1;
    this.surtaxThreshold2 = fields.surtaxThreshold2;
    this.surtaxRate2 = fields.surtaxRate2;
    this.healthPremiumTaxBrackets = fields.healthPremiumTaxBrackets;
  }

  /** Line 87. */
  @Override public Money netProvincialTax() {
    Money line58 = generic.netProvincialTax(); //includes non-refundable tax credits; includes dividend tax credit already!
    Money line66 = line58.plus(surtaxOn(line58));
    Money line78 = Util.nonNegative(line66.minus(lowIncomeBasic));
    Money line86 = healthPremiumOn(generic.fed.taxableIncome());
    return line78.plus(line86);
  }

  /** Call-forward instead of subclass, because I'm not overriding anything in the generic class. */
  private GENERICTaxReturn generic; 
  
  /** Line 75, where it's multiplied by 2! */
  private Money lowIncomeBasic;
  
  /** Line 63.*/
  private Money surtaxThreshold1;
  /** Line 63.*/
  private Double surtaxRate1;
  
  /** Line 64.*/
  private Money surtaxThreshold2;
  /** Line 64.*/
  private Double surtaxRate2;
  
  private TaxBrackets healthPremiumTaxBrackets;

  /** Line 65.*/
  private Money surtaxOn(Money taxLine58) {
    Money one = Util.nonNegative(taxLine58.minus(surtaxThreshold1).times(surtaxRate1));
    Money two = Util.nonNegative(taxLine58.minus(surtaxThreshold2).times(surtaxRate2));
    Money line65 = one.plus(two);
    return line65;
  }
  
  /** Line 86.*/
  private Money healthPremiumOn(Money taxableInc) {
    return healthPremiumTaxBrackets.taxFor(taxableInc);
  }
}
