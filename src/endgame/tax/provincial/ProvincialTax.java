package endgame.tax.provincial;

import endgame.model.Money;

/** 
 Provincial or territorial tax returns.
 These returns have similarities, but they are rarely exactly the same.
 Unfortunately, each jurisdiction needs its own implementation of this interface. 
*/
public interface ProvincialTax {

  /** Net provincial tax, line 42800 on the federal tax return. */
  public Money netProvincialTax();
  
}
