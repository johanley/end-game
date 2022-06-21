package endgame.security.gic;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.transaction.TransactionDates;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** 
 Apply accrued interest for a GIC to the tax return.
 This action applies only to the NRA account, and applies only when the term of the GIC is more than 1 year. 
*/
final class GicInterestAccrual extends Transactional {
  
  GicInterestAccrual(String yyyymmdd, Account account, GtdInvestmentCert gic){
    super(TransactionDates.fromYMD(yyyymmdd));
    this.account = account;
    this.gic = gic;
  }
  
  @Override protected void execute(DateTime anniversaryOfPurchase, Scenario sim) {
    if (account == sim.nra) {
      if (account.gics().contains(gic)) {
        Money accruedInterest = gic.accruedInterestFor(anniversaryOfPurchase);
        logMe(anniversaryOfPurchase, accruedInterest);
        sim.taxReturn.addNraInterestIncome(accruedInterest);
      }
      else {
        logMe(anniversaryOfPurchase, ". NRA doesn't contain the GIC.");
      }
    }
  }

  @Override public String toString() {
    return "ACCRUAL FOR GIC " + gic + " in " + account.getClass().getSimpleName(); 
  }

  private Account account;
  private GtdInvestmentCert gic;

}
