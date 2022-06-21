package endgame.security.gic;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** Sets of transactions for GICs. */
public final class GicTransactions {

  /** 
   Purchase and redemption.
   If and only if the account is the NRA, then include yearly accrual of interest on the tax return, 
   on each anniversary of the original purchase.
  */
  public static void buyAccrueAndRedeem(
     Scenario sim, String purchaseDate, Account account, String principal, String soldBy, String interestRate /*5.5%*/, String term
   ){
    GtdInvestmentCert gic = GtdInvestmentCert.fromPurchaseDate(
      new Money(principal), soldBy, Util.percentFrom(interestRate), new DateTime(purchaseDate), Integer.valueOf(term)
    );
    addTransactions(gic, account, sim, INCLUDE_BUY);
  }
  
  /** 
   Transactions for GICs that are present in the initial holdings of the investment accounts. 
   Called only upon start-up.
   In this case, there's no buy transaction, since the purchase was executed before the beginning of the simulation.
   There's always a redemption, and there's often accrual transactions for the remaining years until redemption. 
  */
  public static void accrueAndRedeemInitialPositions(Scenario sim) {
    for(Account account : sim.investmentAccounts()) {
      for(GtdInvestmentCert gic : account.gics()) {
        addTransactions(gic, account, sim, EXCLUDE_BUY);
      }
    }
  }
  
  /** Buy, accrued interest, and redemption. */
  private static void addTransactions(GtdInvestmentCert gic, Account account, Scenario sim, boolean includeBuy) {
    if (includeBuy) {
      //the purchase fails if there's not enough cash in the account when the transaction executes
      //if that's the case, the other transactions will still be created, but will log the fact that the GIC is missing
      BuyGic gicPurchase = new BuyGic(gic.purchaseDate().toString(), account, gic);
      sim.transactionals.add(gicPurchase);
    }
    
    //accrual comes before redemption, because accrual checks to see if the GIC is in the account
    DateTime startDate = new DateTime(sim.startDate);
    if (account == sim.nra) {
      for(DateTime anniversary : gic.anniversaryDates() /*up to the redemption date*/) {
        if (anniversary.gteq(startDate)) { //needed for initial holdings 
          GicInterestAccrual interestAccrual = new GicInterestAccrual(anniversary.format("YYYY-MM-DD"), account, gic);
          sim.transactionals.add(interestAccrual);
        }
      }
    }
    
    RedeemGic gicRedemption = new RedeemGic(gic.maturityDate().format("YYYY-MM-DD"), account, gic);
    sim.transactionals.add(gicRedemption);
  }
  
  private static final boolean INCLUDE_BUY = true;
  private static final boolean EXCLUDE_BUY = false;
}
