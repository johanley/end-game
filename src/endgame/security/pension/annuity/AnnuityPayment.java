package endgame.security.pension.annuity;

import endgame.Scenario;
import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** Deposit an annuity payment to your bank account. */
public final class AnnuityPayment extends Transactional {
  
  public AnnuityPayment(String when, String amount) {
    super(when);
    this.amount = new Money(amount);
  }

  /** 
   A fixed amount is deposited to the bank. 
   On the tax return, it's added to your pension income {@link FederalTaxReturn#addPensionIncome(Money)}.
   See line 11500. 
   Used by line 31400 as well (pension income amount).
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    sim.bank.depositCash(amount, when);
    sim.taxReturn.addPensionIncome(amount);
    sim.yearlyCashFlows.pension = sim.yearlyCashFlows.pension.plus(amount);
    logMe(when, amount);
  }
  
  @Override public String toString() {
    return "ANNUITY payment: ";
  }
  
  private Money amount = Consts.ZERO;
}
