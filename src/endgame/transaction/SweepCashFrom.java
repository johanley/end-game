package endgame.transaction;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import hirondelle.date4j.DateTime;

/** Move the entire cash balance in an investment account into your bank account. */
public final class SweepCashFrom extends Transactional {
  
  public SweepCashFrom(Account account, String when) {
    super(when);
    this.account = account;
  }

  /** 
   Move the cash only if there's a positive balance in the source account.
   
   <P>In the case of the RIF, there's often withholding tax, which the bank will never see. 
   The withholding tax goes to the CRA, and is reflected as an installment on the tax return.
   The bank may eventually see some money via the tax return.
   
   <P>In the case of the TFSA, the tfsa-room will increase by the amount of the withdrawal. 
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    Money fullBalance = account.cash();
    if (fullBalance.isPlus()) {
      Money withheld = account.withdrawCash(fullBalance, when);
      Money sweptAmount = fullBalance.minus(withheld);
      sim.bank.depositCash(fullBalance, when);
      sim.yearlyCashFlows.cashSwept = sim.yearlyCashFlows.cashSwept.plus(fullBalance);
      //sim.bank.depositCash(sweptAmount, when);
      //sim.yearlyCashFlows.cashSwept = sim.yearlyCashFlows.cashSwept.plus(sweptAmount);
      if (withheld.isZero()) {
        logMe(when, fullBalance); 
      }
      else {
        logMe(when, fullBalance + " withheld " + withheld); 
      }
    }
  }
  
  @Override public String toString() {
    return "SWEEP cash from " + account.getClass().getSimpleName();
  }
  
  private Account account;
}