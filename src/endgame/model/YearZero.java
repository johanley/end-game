package endgame.model;

/** 
 Simple struct for amounts needed from before the start of the simulation.
 For example, the OAS calculation depends on an income number from the previous year's tax return.
 For the first year in the simulation, that number cannot be calculated, so it needs to be 
 explicitly stated. 
*/
public final class YearZero {
  
  public YearZero(String netIncBeforeAdj, String netInc, String oasInc, String empInc) {
    this.netIncomeBeforeAdjustments = new Money(netIncBeforeAdj);
    this.netIncome = new Money(netInc);
    this.oasIncome = new Money(oasInc);
    this.employmentIncome = new Money(empInc);
  }
  
  public Money netIncomeBeforeAdjustments; 
  public Money netIncome;
  public Money oasIncome;
  public Money employmentIncome;


}
