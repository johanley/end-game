package endgame.security.gic;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.transaction.TransactionDates;
import endgame.transaction.Transactional;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/** Redeem a GIC on its redemption date (maturity date). */
final class RedeemGic extends Transactional {

  RedeemGic(String yyyymmdd, Account account, GtdInvestmentCert gic) {
    super(TransactionDates.fromYMD(yyyymmdd));
    this.account = account;
    this.gic = gic;
  }
  
  /** 
   The GIC is removed from the account, and its principal plus interest is credited to the account.
  */
  @Override protected void execute(DateTime redemptionDate, Scenario sim) {
    if (!account.gics().contains(gic)) {
      Log.log("  Trying to redeem " + gic + " in " + account.getClass().getSimpleName() + " but no position exists.");
    }
    else {
      Money proceeds = account.redeem(gic);
      sim.yearlyCashFlows.liquidationProceeds = sim.yearlyCashFlows.liquidationProceeds.plus(proceeds);
      sim.yearlyCashFlows.interest = sim.yearlyCashFlows.interest.plus(gic.totalInterest());
      logMe(redemptionDate, proceeds);
    }
  }
  
  @Override public String toString() {
    return "REDEEM GIC " + gic + " in " + account.getClass().getSimpleName(); 
  }

  // PRIVATE
  private Account account;
  private GtdInvestmentCert gic;
}
