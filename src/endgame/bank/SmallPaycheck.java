package endgame.bank;

import endgame.Scenario;
import endgame.model.Money;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

/** 
  You get a small paycheck from employment.
  
  The model here is very simplified. 
  The idea is that the retiree has a small job on the side.
  The paycheck is simply a gross amount, with no deductions.
  The gross amount is added to the employment income line of your tax return, so  
  that you later pay taxes on the gross amount. 
  
  <P>There's no modeling of CPP contributions, or EI contributions.
*/
public final class SmallPaycheck extends Transactional {
  
  public static SmallPaycheck valueOf(String monthlyGross, String when) {
    return new SmallPaycheck(new Money(monthlyGross), when);
  }
  
  /** Deposit the paycheck to your bank (gross amount). On your tax return, add the gross amount to your employment income. */ 
  @Override protected void execute(DateTime when, Scenario sim) {
    sim.bank.depositCash(monthlyGross, when);
    sim.taxReturn.addEmploymentIncome(monthlyGross);
    logMe(when, monthlyGross);
  }

  @Override public String toString() {
    return "SMALL PAYCHECK: ";
  }
  
  private Money monthlyGross;
  
  private SmallPaycheck(Money monthlyAmount, String when) {
    super(when);
    this.monthlyGross = monthlyAmount;
  }
}
