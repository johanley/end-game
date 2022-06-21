package endgame.account.rif;

import endgame.Scenario;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/**
 Convert an RSP (no min withdrawal) to a RIF (with minimum withdrawals). 
*/
public final class ConvertRspToRif extends Transactional {
  
  public ConvertRspToRif(String rawDate) {
    super(rawDate);
  }
  
  @Override protected void execute(DateTime when, Scenario sim) {
    //setting this to a non-zero value results in a non-zero value for the rif minimal withdrawal
    sim.rifValueJan1 = sim.rif.value(); //assumes Jan 1 is the date
    logMe(when, "");
  }
  
  @Override public String toString() {
    return "Convert RSP to RIF. Minimum withdrawals now enforced. Transactions for the rsp will now be directed to the rif.";
  }

}
