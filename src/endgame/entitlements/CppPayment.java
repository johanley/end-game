package endgame.entitlements;

import static endgame.util.Consts.ZERO;

import endgame.Scenario;

import static endgame.util.Consts.NUM_DAYS_IN_FEBRUARY;

import endgame.model.Money;
import endgame.transaction.TransactionDates;
import endgame.transaction.Transactional;
import endgame.util.Util;
import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

/** CPP calculation and payment. */
public final class CppPayment extends Transactional {
  
  public static final CppPayment valueOf(
    String chosenStartMonth /*2027-04*/, String nominalMonthlyAmount, String dateOfBirth, String paymentDay /*28*/,
    String monthlyReward, String monthlyPenalty, String nominalStart, String startWinBeg, String startWinEnd,
    String survivorAmt, String survivorDate
  ) {
    DateTime chosenStartMon = new DateTime(chosenStartMonth + FIRST_OF_THE_MONTH);
    DateTime dob = new DateTime(dateOfBirth);
    DateTime monthOfBirth = DateTime.forDateOnly(dob.getYear(), dob.getMonth(), FIRST_DAY_OF_THE_MONTH);
    Money survivorAmount = Util.isPresent(survivorAmt) ? new Money(survivorAmt) : null;
    DateTime survivorStart = Util.isPresent(survivorDate) ? new DateTime(survivorDate) : null;
    return new CppPayment(
      chosenStartMon /*2027-04-01*/, new Money(nominalMonthlyAmount), monthOfBirth, paymentDay /*28*/, 
      Util.percentFrom(monthlyReward), Util.percentFrom(monthlyPenalty), Integer.valueOf(nominalStart), 
      Integer.valueOf(startWinBeg), Integer.valueOf(startWinEnd), survivorAmount, survivorStart
    );
  }

  /** 
   Paid to your bank account near the end of the month.
   Tracked by your tax return as CPP income.
   Paid only as of the month (after) you elect to start CPP.
   
   <P>In this simplified implementation, an optional survivor benefit is paid only if you are already receiving regular CPP payments.
   In that case, your CPP payment can be increased by a survivor benefit. 
  */
  @Override protected void execute(DateTime when, Scenario sim) {
    if (hasPaymentThisMonth(when)) {
      Money amount = getMonthlyAmount();
      if (survivorDate !=null && when.gteq(survivorDate)) {
        amount = amount.plus(survivorAmt);
      }
      sim.bank.depositCash(amount, when);
      sim.taxReturn.addCppIncome(amount);
      sim.yearlyCashFlows.cpp = sim.yearlyCashFlows.cpp.plus(amount);
      logMe(when, amount);
    }
  }
  
  @Override public String toString() {
    return "CPP payment: ";
  }
  
  // PRIVATE 
  
  private CppPayment(
    DateTime chosenStartMonth, Money nominalMonthlyAmount, DateTime dateOfBirth, String paymentDay, 
    Double monthlyReward, Double monthlyPenalty, Integer nominalStart, Integer startWinBeg, Integer startWinEnd, 
    Money survivorAmount, DateTime survivorDate
  ) {
    super(TransactionDates.fromStartDateAndDD(chosenStartMonth.toString(), paymentDay));
    Integer payDay = Integer.valueOf(paymentDay);
    if (payDay > NUM_DAYS_IN_FEBRUARY) {
      throw new IllegalArgumentException("Payment day cannot exceeed " + NUM_DAYS_IN_FEBRUARY + " (since you won't get paid in Feb).");
    }

    this.chosenStartMonth = chosenStartMonth;
    this.monthOfFirstCheque = theMonthAfterThe(chosenStartMonth);
    this.nominalMonthlyAmount = nominalMonthlyAmount;
    this.monthOfBirth = dateOfBirth;
    this.startWindowBegin = startWinBeg;
    this.startWindowEnd = startWinEnd;
    this.monthlyReward = monthlyReward;
    this.monthlyPenalty = monthlyPenalty;
    this.nominalStart = nominalStart;
    if( ageOnChosenStartMonth() < this.startWindowBegin || this.startWindowEnd < ageOnChosenStartMonth()) {
      throw new IllegalArgumentException(
        "Start date for CPP " + chosenStartMonth +  " doesn't have age in range " + this.startWindowBegin + ".." + this.startWindowEnd
       );
    }
    this.monthlyAmount = monthlyAmount(); //core cpp, without survivor benefit.
    this.survivorAmt = survivorAmount;
    this.survivorDate = survivorDate;
  }
  
  /** Coerce the day to the 1st. */
  private DateTime monthOfBirth;
  
  /** Coerce the day to the 1st. */
  private DateTime chosenStartMonth;
  
  /** Coerce the day to the 1st. One month AFTER the chosen start month. */
  private DateTime monthOfFirstCheque;
  
  /** The 'standard' amount, if taken at the standard retirement age (65). */
  private Money nominalMonthlyAmount;
  
  /** Adjusted from the nominal amount, according to chosen start-month. Core benefit, without the survivor benefit. */
  private Money monthlyAmount;
  
  private Double monthlyPenalty;
  private Double monthlyReward;
  private Integer nominalStart;
  private Integer startWindowBegin;
  private Integer startWindowEnd;
  private Money survivorAmt;
  private DateTime survivorDate;
  
  private static final Integer FIRST_DAY_OF_THE_MONTH = 1;
  private static final String FIRST_OF_THE_MONTH = "-01";
  private static final int ONE_MONTH = 1;

  private Integer ageOnChosenStartMonth() {
    return Util.age(monthOfBirth, chosenStartMonth);
  }
  
  /**
   Depends on your chosen start month, and how much it differs from the 
   month you turn 65. Excludes survivor benefit, which is added later.
  */
  private Money monthlyAmount() {
    Money result = nominalMonthlyAmount;
    int numMonths = Util.numMonthsBetween(chosenStartMonth, monthTurn65());
    if (numMonths != 0) {
      Money adjustment = ZERO;
      if (numMonths > 0) {
        adjustment = result.times(numMonths * monthlyPenalty);
        result = result.minus(adjustment);
      }
      else {
        adjustment = result.times(numMonths * monthlyReward);
        result = result.plus(adjustment);
      }
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
    return DateTime.forDateOnly(monthOfBirth.getYear() + nominalStart, monthOfBirth.getMonth(), FIRST_DAY_OF_THE_MONTH);
  }
  
  /** True only if the month is at least one month AFTER the chosen start month. */
  private boolean hasPaymentThisMonth(DateTime day) {
    return !day.lt(monthOfFirstCheque);
  }

  /** Adjusted from the nominal amount at 65, given the chosen start month. */
  private Money getMonthlyAmount() {
    return monthlyAmount;
  }
}