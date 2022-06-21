package endgame.security.gic;

import endgame.Scenario;
import endgame.account.Account;
import endgame.transaction.TransactionDates;
import endgame.transaction.Transactional;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/**
 Buy a Guaranteed investment certificate (GIC) and hold it to maturity. 
 If there are insufficient funds in the account, then no action is taken, but the fact is logged.
*/
public final class BuyGic extends Transactional {

  BuyGic(String yyyymmdd, Account account, GtdInvestmentCert gic) {
    super(TransactionDates.fromYMD(yyyymmdd));
    this.account = account;
    this.gic = gic;
  }

  /** Return the total cost. */ 
  @Override protected void execute(DateTime purchaseDate, Scenario sim) {
    if (account.cash().lt(gic.principal())) {
      Log.log("  INSUFFICIENT CASH IN " + account.getClass().getSimpleName() + " " + account.cash() + ". Can't buy " + gic);
    }
    else {
      logMe(purchaseDate, "");
      account.buy(gic);
      sim.yearlyCashFlows.liquidationProceeds = sim.yearlyCashFlows.liquidationProceeds.minus(gic.principal());
    }
  }
  
  @Override public String toString() {
    return "BUY GIC " + gic + " in " + account.getClass().getSimpleName(); 
  }

  private Account account;
  private GtdInvestmentCert gic;
}