package endgame.security.stock.commission;

import endgame.model.Money;
import endgame.util.Consts;

/** 
 Your investment dealer's commission schedule - which you have to implement!
 
 There are many different possible forms for a commission schedule.
 There's no way to make this code generic.
 That means you will need to alter this class manually, if you want to reflect your own 
 situation. 
*/
public final class CustomCommish implements Commission {
  
  /** This method returns 0, until you manually change the code! */
  @Override public Money commissionOn(Integer numShares, Money price) {
    
    //YOUR CODE GOES HERE!!
    
    return Consts.ZERO;
  }

}
