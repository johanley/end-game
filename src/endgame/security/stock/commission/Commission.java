package endgame.security.stock.commission;

import endgame.model.Money;

/** 
 The commission paid to an investment dealer when you execute an equity trade.
*/
public interface Commission {

  /**
   Return the commission paid to execute the trade.
   Must return a non-negative amount.
   
   Some businesses have commission schedules that depend on other factors, such as
   US/Canadian, market/limit orders, and so on. Those kinds of details are not 
   captured here, so you may have to make a reasonable approximate 
   to your real-world commission schedule. 
  */
  public Money commissionOn(Integer numShares, Money price);

}
