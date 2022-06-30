package endgame.transaction;

import endgame.Scenario;

import endgame.model.Money;
import endgame.account.Cashable;
import hirondelle.date4j.DateTime;

/** 
 Move cash from one account to another.
 Move either the entire cash balance from the source account, or a specified amount.
*/
public final class MoveCash extends Transactional {

  /** Move the full cash balance from the source account to the target account. */
  public static MoveCash fullBalance(Cashable from, Cashable to, String when) {
    return new MoveCash(from, to, null, when);
  }
  
  /** Move a specific amount from the source account to the target account. */
  public static MoveCash specificAmount(Cashable from, Cashable to, String amount, String when) {
    return new MoveCash(from, to, new Money(amount), when);
  }

  /** 
   Move the cash only if there's a positive balance in the source account.
   If an amount has been specified, and there's not enough money in the source account, then the operation will fail.
   
   <P>Deposits to a RIF account will fail. 
   Withdrawals from a RIF will contribute to taxable income, and will often incur withholding tax. 
   When a withholding tax is incurred, the target account will receive an amount reduced by the withholding tax.
   
   <P>In the case of the TFSA, this action will alter the TFSA-room. 
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    boolean moveSpecificAmount = amount != null;
    Money grossAmount = moveSpecificAmount ? amount : fromAcct.cash();
    if (moveSpecificAmount) {
      if (grossAmount.gt(fromAcct.cash())) {
        throw new RuntimeException(
          "Insufficient funds. Cannot move " + grossAmount + " out of " + name(fromAcct) + 
          ". Only " + fromAcct.cash() + " available."
        );
      }
    }
    if (fromAcct.cash().isPlus()) {
      Money withheld = fromAcct.withdrawCash(grossAmount, when);
      Money netAmount = grossAmount.minus(withheld);
      toAcct.depositCash(netAmount, when); //will fail for a RIF/RSP.
      if (sim.bank == toAcct) {
        sim.yearlyCashFlows.cashSwept = sim.yearlyCashFlows.cashSwept.plus(netAmount); 
      }
      if (withheld.isZero()) {
        logMe(when, netAmount); 
      }
      else {
        logMe(when, "net " + netAmount + " plus " + withheld + " withheld."); 
      }
    }
  }
  
  @Override public String toString() {
    return "MOVE CASH from " + name(fromAcct) +  " to " + name(toAcct);
  }
  
  private Cashable fromAcct;
  private Cashable toAcct;
  /** If null, then move the full cash balance out of the from-account. */
  private Money amount;
  
  private MoveCash(Cashable from, Cashable to, Money amount, String when) {
    super(when);
    this.fromAcct = from;
    this.toAcct = to;
    this.amount = amount;
    if (fromAcct == toAcct) {
      throw new RuntimeException("Can't move cash from the " + name(fromAcct) + " account back to " + name(toAcct) + " account. Specify different accounts.");
    }
  }
  
  private String name(Object thing) {
    return thing.getClass().getSimpleName();
  }
}