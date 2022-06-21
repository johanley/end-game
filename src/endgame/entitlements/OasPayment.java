package endgame.entitlements;

import endgame.Scenario;
import endgame.model.Money;
import endgame.output.stats.yearly.TaxSummary;
import endgame.transaction.TransactionDates;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import endgame.util.Util;
import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

/** 
 OAS calculation and payment. Includes GIS, if applicable.
 
 <P>OAS clawback is calculated, but the repayment logic is deeply simplified from how it really works.
 In the real world, the clawback logic involves 4 years, since the repayment is made over the following 2 
 calendar years, by reducing the OAS payment.
 
 <P>In this implementation, the clawback logic involves 2 years, and everything is done 'up front', without delay.
 The clawback amount is deducted immediately from your payment, based on last year's tax return (line 23400, net income before adjustments).
 This greatly simplifies the logic, without being unacceptably different from the real world.
 In the end, you still receive the same amount.
*/
public final class OasPayment extends Transactional {
  
  public static final OasPayment valueOf(
      String chosenStartMonth /*2027-04*/, String monthlyAmountAt65, String dateOfBirth, String paymentDay /*28*/,
      String monthlyReward, String boostAge, String boostPercent, 
      String clawbackThreshold, String clawbackRate, String startWinBegin, String startWinEnd, String gisExempt      
   ) {
    DateTime chosenStartMon = new DateTime(chosenStartMonth + FIRST_OF_THE_MONTH); //coerce to the start of the month
    DateTime dob = new DateTime(dateOfBirth);
    DateTime monthOfBirth = DateTime.forDateOnly(dob.getYear(), dob.getMonth(), FIRST_DAY_OF_THE_MONTH);
    return new OasPayment(
      chosenStartMon /*2027-04-01*/, new Money(monthlyAmountAt65), dob, monthOfBirth, paymentDay /*28*/,
      Util.percentFrom(monthlyReward), Integer.valueOf(boostAge), Util.percentFrom(boostPercent),
      new Money(clawbackThreshold), Util.percentFrom(clawbackRate), 
      Integer.valueOf(startWinBegin), Integer.valueOf(startWinEnd), new Money(gisExempt)
    );
  }

  /** 
   Make a direct deposit to your bank account near the end of the month.
   Tracked by your tax return as OAS income (and GIS income, if present).
   The first payment is near the end of the month AFTER you elect to start OAS. 
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    if (hasPaymentThisMonth(when)) {
      Money oasAmount = monthlyAmountIncludingClawback(when, sim);
      Money gisAmount = getMonthlyGisAmount(sim);
      Money totalAmount = oasAmount.plus(gisAmount);
      sim.bank.depositCash(totalAmount, when);
      
      sim.taxReturn.addOasIncome(oasAmount);
      sim.yearlyCashFlows.oas = sim.yearlyCashFlows.oas.plus(oasAmount);
      
      if (gisAmount.isPlus()) {
        sim.taxReturn.addGisIncome(gisAmount);
        sim.yearlyCashFlows.gis = sim.yearlyCashFlows.gis.plus(gisAmount);
      }
      logMe(when, totalAmount);
    }
  }    
  
  @Override public String toString() {
    return "OAS/GIS payment:";
  }
  
  // PRIVATE 
  
  private OasPayment(
    DateTime chosenStartMonth /*2027-04-01*/, Money monthlyAmountAt65, DateTime dateOfBirth, 
    DateTime monthOfBirth, String paymentDay /*28*/, Double monthlyReward, Integer boostAge, Double boostPercent, 
    Money clawbackThreshold, Double clawbackRate, Integer startWinBegin, Integer startWinEnd, Money gisExempt
  ) {
    super(TransactionDates.fromStartDateAndDD(chosenStartMonth.toString(), paymentDay));
    if (Integer.valueOf(paymentDay) > 28) {
      throw new IllegalArgumentException("Payment day cannot exceeed 28 (since you won't get paid in Feb).");
    }
    this.chosenStartMonth = chosenStartMonth;
    this.monthlyAmountAt65 = monthlyAmountAt65;
    this.dateOfBirth = dateOfBirth;
    this.monthOfBirth = monthOfBirth;
    this.monthOfFirstCheque = theMonthAfterThe(chosenStartMonth);
    Integer ageAtStart = ageOnChosenStartMonth();
    if( ageAtStart < startWinBegin || startWinEnd < ageAtStart) {
      throw new IllegalArgumentException("At OAS start-date your age of " + ageAtStart + " is not in the range " + startWinBegin + ".." + startWinEnd);
    }
    this.monthlyReward = monthlyReward;
    this.boostAge = boostAge;
    this.boostPercent = boostPercent;
    this.clawbackThreshold = clawbackThreshold;
    this.clawbackRate = clawbackRate;
    this.startWinBegin = startWinBegin;
    this.gisExempt = gisExempt;
  }
  
  /** Coerce the day to the 1st. */
  private DateTime chosenStartMonth;
  private Money monthlyAmountAt65;
  private DateTime dateOfBirth;
  /** Coerce the day to the 1st. */
  private DateTime monthOfBirth;
  /** Coerce the day to the 1st. One month AFTER the chosen start month. */
  private DateTime monthOfFirstCheque;
  
  private Double monthlyReward;
  private int boostAge;
  private Double boostPercent;
  private Money clawbackThreshold;
  private Double clawbackRate;
  private Integer startWinBegin;
  private Money gisExempt;
  
  private static final Integer FIRST_DAY_OF_THE_MONTH = 1;
  private static final String FIRST_OF_THE_MONTH = "-01";
  private static final int ONE_MONTH = 1;
  private static final int PER_MONTH = 12;
  
  private Integer ageOnChosenStartMonth() {
    return Util.age(dateOfBirth, chosenStartMonth);
  }
  
  private Integer age(DateTime when) {
    return Util.age(dateOfBirth, when);
  }
  
  private Money monthlyAmountIncludingClawback(DateTime when, Scenario sim) {
    Money result = monthlyAmountAt65;
    
    int numMonths = Util.numMonthsBetween(monthTurn65(), chosenStartMonth);
    if (numMonths > 0) {
      Money adjustment = result.times(numMonths * monthlyReward);
      result = result.plus(adjustment);
    }
    
    if (age(when) >= boostAge) {
      result = result.times(1.0 + boostPercent);
    }
    
    result = result.minus(clawbackAmount(sim));
    return result.gt(Consts.ZERO) ? result : Consts.ZERO;
  }

  /**
   Ref: https://www.taxtips.ca/seniors/oas-clawback.htm
   Rate: 15% of excess of your taxable-income-before-adjustments (line 23400) over threshold;
   Note that the tax is paid in the NEXT TWO tax years, as reductions in the OAS payment.
  */
  private Money clawbackAmount(Scenario sim) {
    Money result = Consts.ZERO;
    Money income = Consts.ZERO;
    if (sim.lastYearsTaxSummary != null) {
      income = sim.lastYearsTaxSummary.netIncomeBeforeAdjustments;
    }
    else {
      income = sim.yearZero.netIncomeBeforeAdjustments;
    }
    Money excess = income.minus(clawbackThreshold);
    if (excess.isPlus()) {
      result = excess.times(clawbackRate);
      result = result.divByInt(PER_MONTH);
    }
    return result;
  }

  /** 
   You receive the payment on the month AFTER you select.
   The government needs to process a complete month's transactions, then calc the result, and 
   send you a cheque in the following month.
   
   <P>WARNING: this can change the year, if the given month is in December.
  */
  private DateTime theMonthAfterThe(DateTime givenMonth) {
    return givenMonth.plus(0, ONE_MONTH, 0, 0, 0, 0, 0, DayOverflow.FirstDay);
  }
  
  /** Coerce to the first of the month. */
  private DateTime monthTurn65() {
    return DateTime.forDateOnly(monthOfBirth.getYear() + startWinBegin, monthOfBirth.getMonth(), FIRST_DAY_OF_THE_MONTH);
  }
  
  /** True only if the month is at least one month AFTER the chosen start month. */
  private boolean hasPaymentThisMonth(DateTime when) {
    return !when.lt(monthOfFirstCheque);
  }
  
  private Money getMonthlyGisAmount(Scenario sim) {
    Money result = Consts.ZERO;
    if (sim.lastYearsTaxSummary != null) {
      GisAmount gis = new GisAmount();
      TaxSummary lastYear = sim.lastYearsTaxSummary;
      result = gis.monthlyAmount(lastYear.netIncome, lastYear.oas, lastYear.employmentIncome, gisExempt);
    }
    else {
      GisAmount gis = new GisAmount();
      result = gis.monthlyAmount(sim.yearZero.netIncome, sim.yearZero.oasIncome, sim.yearZero.employmentIncome, gisExempt);
    }
    return result;
  }
}