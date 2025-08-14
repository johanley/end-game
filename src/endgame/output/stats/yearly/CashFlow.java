package endgame.output.stats.yearly;

import static endgame.util.Consts.NL;
import static endgame.util.Consts.SPACE;
import static endgame.util.Consts.ZERO;

import java.util.Collection;
import java.util.List;

import endgame.model.Money;
import endgame.util.MoneyFormatter;

/** 
 Various yearly totals, focusing on cash flows of various sorts.
*/
public final class CashFlow {
  
  public Money cpp = ZERO;
  public Money oas = ZERO;
  public Money gis = ZERO;
  /** Superannuation, excludes CPP.*/
  public Money pension = ZERO; 
  public Money dividends = ZERO;
  /** Net proceeds of sells, minus buys. */
  public Money liquidationProceeds = ZERO;
  public Money interest = ZERO;
  
  /** Cash moved from investment accounts into the bank account. */
  public Money cashSwept = ZERO;

  /** Entitlements (CPP, OAS, GIS), dividends, pensions, and sales of stocks. */
  public Money cashFlow() {
    Money result = ZERO;
    result = result.plus(cpp);
    result = result.plus(oas);
    result = result.plus(gis);
    result = result.plus(pension);
    result = result.plus(dividends);
    result = result.plus(liquidationProceeds);
    result = result.plus(interest);
    return result;
  }
  
  public Money entitlementsAndSweeps() {
    Money result = ZERO;
    result = result.plus(cpp);
    result = result.plus(oas);
    result = result.plus(gis);
    result = result.plus(pension);
    result = result.plus(cashSwept);
    return result;
  }
  
  /** Sums of all the items in a completed list of {@link CashFlow} objects. */
  public static CashFlow sumOver(Collection<CashFlow> cashFlowHistory) {
    CashFlow result = new CashFlow();
    for (CashFlow cf : cashFlowHistory) {
      result.cashSwept = result.cashSwept.plus(cf.cashSwept);
      result.cpp = result.cpp.plus(cf.cpp);
      result.oas = result.oas.plus(cf.oas);
      result.gis = result.gis.plus(cf.gis);
      result.pension = result.pension.plus(cf.pension);
      result.dividends = result.dividends.plus(cf.dividends);
      result.liquidationProceeds = result.liquidationProceeds.plus(cf.liquidationProceeds);
      result.interest = result.interest.plus(cf.interest);
    }
    return result;
  }
  
  /** 
   Sums of all data items in a completed list of {@link CashFlow} objects, divided by the number 
   of items in the list. 
  */
  public static CashFlow sumOverPerYear(Collection<CashFlow> cashFlowHistory) {
    CashFlow result = sumOver(cashFlowHistory);
    int numYears = cashFlowHistory.size();
    result.cashSwept = result.cashSwept.divByInt(numYears);
    result.cpp = result.cpp.divByInt(numYears);
    result.oas = result.oas.divByInt(numYears);
    result.gis = result.gis.divByInt(numYears);
    result.pension = result.pension.divByInt(numYears);
    result.dividends = result.dividends.divByInt(numYears);
    result.liquidationProceeds = result.liquidationProceeds.divByInt(numYears);
    result.interest = result.interest.divByInt(numYears);
    return result;
  }
  
  
  /** Suitable for reporting. */
  @Override public String toString() {
    StringBuilder result = new StringBuilder("Cash generated" + NL);
    MoneyFormatter money = new MoneyFormatter();
    result.append(money.format(entitlementsAndSweeps()) + SPACE + "Entitlements and Sweeps" + NL);
    result.append(money.format(cpp) + SPACE + "CPP" + NL);
    result.append(money.format(oas) + SPACE + "OAS" + NL);
    result.append(money.format(gis) + SPACE + "GIS" + NL);
    result.append(money.format(pension) + SPACE + "Pension" + NL);
    result.append(money.format(dividends) + SPACE + "Dividends" + NL);
    result.append(money.format(liquidationProceeds) + SPACE + "Net liquidation (sell-minus-buy)" + NL);
    result.append(money.format(interest) + SPACE + "Interest" + NL);
    result.append(money.format(cashSwept) + SPACE + "Total cash swept from investment acct's into the bank" + NL);
    result.append(money.format(cashFlow()) + SPACE + "Total cash generated" + NL);
    return result.toString();
  }
}