package endgame.tax.provincial;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;

/** 
 Newfoundland and Labrador provincial tax return.
 
 <P>The implementation logic is as of 2020, but may be valid for later years.
 <P>By coincidence, this implementation is the same as that for NB; they have the same basic structure.
*/
public final class NLTaxReturn implements ProvincialTax {
  
  public NLTaxReturn(ProvTaxFields fields, FederalTaxReturn fed) {
    this.nb = new NBTaxReturn(fields, fed);
  }
  
  @Override public Money netProvincialTax() {
    return nb.netProvincialTax();
  }

  /** Call-forward to NB; don't subclass it. */
  private NBTaxReturn nb;

}