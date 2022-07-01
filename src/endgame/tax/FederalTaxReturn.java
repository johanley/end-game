package endgame.tax;

import static endgame.util.Consts.ZERO;

import java.math.BigDecimal;

import endgame.Scenario;
import endgame.model.Money;
import endgame.model.MoneyRange;
import endgame.tax.provincial.ProvincialTax;
import endgame.util.Consts;
import endgame.util.Util;
import hirondelle.date4j.DateTime;

/**
 Data and operations for a single yearly tax return.
  
 Tax returns have a yearly cycle (mostly). 
 Data is gathered during the year, and then the tax return is calculated after year-end.
 Some items can be active over more than a single year (for example, capital losses), and represent
 an exception to that rule.
 
 <P>The various actions that occur during the year in different accounts 
 often affect the data in this class.
 
 <P>The implementation logic is as of 2020, but the data can be updated in following years
 using settings in the scenario file. 
*/
public final class FederalTaxReturn {
  
  public static FederalTaxReturn valueOf (
    Scenario scenario, 
    String year, String dateOfBirth, String personalAmount,String personalAmountAdditional,String personalAmountClawback, 
    String ageAmount, String ageAmountClawback,
    String pensionAmount, TaxBrackets taxBrackets, TaxBrackets rifWithholdingTaxBrackets,
    String stdRetAge, String taxCapGainFrac, String divTaxCreditNum, String divTaxCreditDenom
  ) {
    return new FederalTaxReturn(
      scenario, Integer.valueOf(year), new DateTime(dateOfBirth), new Money(personalAmount), new Money(personalAmountAdditional), MoneyRange.valueOf(personalAmountClawback),   
      new Money(ageAmount), new Money(ageAmountClawback), new Money(pensionAmount),
      taxBrackets, rifWithholdingTaxBrackets, Integer.valueOf(stdRetAge), Double.valueOf(taxCapGainFrac), 
      Integer.valueOf(divTaxCreditNum), Integer.valueOf(divTaxCreditDenom)
    );
  }
  
  /** 
   UNUSUAL: this is called after the object is constructed!
   The inter-dependence of the federal and provincial taxes is a bit weird. 
   That might indicate a defective design. 
  */ 
  public void setProvincialReturn(ProvincialTax provTax) {
    this.provTax = provTax;
  }
  
  public Integer year() { return year; }
  public DateTime dateOfBirth() {return dateOfBirth;}
  
  /** 
   Add to line 47600.
   Do not explicitly call this method for RIF-LIF withholding tax.
   That's handled by {@link #addRifIncome(Money)} (which calls this method internally). 
  */
  public void addInstallment(Money installment) {
    coll.installments = coll.installments.plus(installment); 
  }
  /** Line 47600. */
  public Money installments() { return coll.installments; }

  /** Line 10100. */
  public void addEmploymentIncome(Money amount) {
    coll.employmentIncome = coll.employmentIncome.plus(amount); 
  }
  /** Line 10100. */
  public Money employmentIncome() { return coll.employmentIncome; }

  /** 
   Monthly OAS money, deposited directly to a bank account. Excludes GIS, if present.
   Line 11300.
   There is one deposit to your bank account for the total of OAS + GIS.
  */
  public void addOasIncome(Money monthly) {
    coll.oasIncome = coll.oasIncome.plus(monthly); 
  }
  /** Line 11300. Excludes GIS. */
  public Money oasIncome() { return coll.oasIncome; }
  
  /** Monthly GIS money, if any, deposited as part of the OAS payment. */
  public void addGisIncome(Money monthly) {
    coll.gisIncome = coll.gisIncome.plus(monthly); 
  }
  public Money gisIncome() { return coll.gisIncome; }
  
  /** Monthly CPP money, deposited directly to a bank account. Line 11400.*/
  public void addCppIncome(Money monthly) {
    coll.cppIncome = coll.cppIncome.plus(monthly); 
  }
  /** Line 11400. */
  public Money cppIncome() { return coll.cppIncome; }
  
  /** Monthly superannuation, deposited directly to a bank account. Line 11500. */
  public void addPensionIncome(Money monthly) {
    coll.pensionIncome = coll.pensionIncome.plus(monthly);
  }
  /** Part of Line 11500, along with RIF after the age of 65. */
  public Money pensionIncome() { return coll.pensionIncome; }
  
  /** Non-registered account, dividend income. Line 12000. */
  public void addNraDivdIncome(Money dividend) {
    coll.nraDvdIncome = coll.nraDvdIncome.plus(dividend); 
  }
  /** Line 12000. */
  public Money nraDivdIncome() { return coll.nraDvdIncome; }
  
  /** Non-registered account, interest income. Line 12100. */
  public void addNraInterestIncome(Money interest) {
    coll.nraInterestIncome = coll.nraInterestIncome.plus(interest); 
  }
  /** Line 12100. */
  public Money nraInterestIncome() { return coll.nraInterestIncome; }

  /** 
   Withdrawals from a RIF account (cash or in-kind) usually have withholding tax applied.
   This method returns the withholding tax for this withdrawal. 
   Side-effect: calls {@link #addInstallment(Money)}, using the withholding tax.
   Line 13000 before 65, and line 11500 after 65.
  */
  public Money addRifIncome(Money rifWithdrawal) {
    Money oldTax = coll.rifWithholdingTax;
    
    coll.rifIncome = coll.rifIncome.plus(rifWithdrawal);
    Money amountAboveMin = coll.rifIncome.minus(rifMinimum());
    Money newTax = rifLifWithholdingTaxBrackets.taxFor(amountAboveMin);
    Money increaseInWithholdingTax = newTax.minus(oldTax);
    
    coll.rifWithholdingTax = newTax;
    addInstallment(increaseInWithholdingTax);
    return increaseInWithholdingTax;
  }
  /** Check this at the end of the year, to see if it has met the minimum. Line 13000 before 65, line 11500 after 65. */
  public Money rifIncome() { return coll.rifIncome; }

  /** Similar to {@link #addRifIncome(Money)}. */
  public Money addLifIncome(Money lifWithdrawal) {
    Money oldTax = coll.lifWithholdingTax;
    
    coll.lifIncome = coll.lifIncome.plus(lifWithdrawal);
    Money amountAboveMin = coll.lifIncome.minus(lifMinimum());
    Money newTax = rifLifWithholdingTaxBrackets.taxFor(amountAboveMin);
    Money increaseInWithholdingTax = newTax.minus(oldTax);
    
    coll.lifWithholdingTax = newTax;
    addInstallment(increaseInWithholdingTax);
    return increaseInWithholdingTax;
  }
  /** 
   Check this at the end of the year, to see if it has met the minimum, and not exceeded the max. 
   Line 13000 before 65, line 11500 after 65. 
  */
  public Money lifIncome() { return coll.lifIncome; }

  /** Line 15000. Side effect: checks RIF-LIF min and max. */
  public Money totalIncome() {
    Money result = new Money(new BigDecimal("0.00"));
    if (coll.rifIncome.lt(rifMinimum())) {
      throw new RuntimeException("RIF income " + coll.rifIncome + " is less than the minimum " + rifMinimum());
    }
    if (coll.lifIncome.lt(lifMinimum())) {
      throw new RuntimeException("LIF income " + coll.lifIncome + " is less than the minimum " + lifMinimum());
    }
    if (coll.lifIncome.gt(lifMaximum())) {
      throw new RuntimeException("LIF income " + coll.lifIncome + " is greater than the maximum " + lifMaximum());
    }
    result = result.plus(coll.employmentIncome);
    result = result.plus(coll.oasIncome);
    //excludes GIS!
    result = result.plus(coll.cppIncome);
    result = result.plus(coll.pensionIncome); 
    result = result.plus(coll.rifIncome);
    result = result.plus(coll.lifIncome);
    result = result.plus(dividendGrossUp());
    result = result.plus(coll.nraInterestIncome);
    result = result.plus(taxableCapitalGain());
    return result;
  }
  
  /** Line 23400. Input for the OAS clawback. */
  public Money netIncomeBeforeAdjustments() {
    return totalIncome();
  }

  /** Line 23600. Blows up if your RIF income is less than the minimum for the year.  */
  public Money netIncome() {
    return totalIncome();
  }
 
  /** Line 26000. In this implementation, this is currently the same as {@link #netIncome()}. */
  public Money taxableIncome() {
    //SHOULD I BE DOING SOMETHING FOR CAPITAL LOSSES?
    return netIncome();
  }
  
  /** Line 70, and line 108. */
  public Money federalTax() {
    return taxBrackets.taxFor(taxableIncome());
  }
  
  /** Line 35000. */
  public Money nonRefundableTaxCredits() {
    Money result = personalAmount();
    result = result.plus(ageAmount());
    result = result.plus(pensionIncomeAmount());
    result = result.times(lowestTaxRate());
    return result;
  }
  
  /** Line 42000. */
  public Money netFederalTax() {
    Money result = federalTax();
    result = result.minus(nonRefundableTaxCredits());
    result = result.minus(dividendTaxCredit());
    return Util.nonNegative(result);
  }

  /** Line 42800. */
  public Money netProvincialTax() {
    return provTax.netProvincialTax();
  }
  
  /** Line 43500. */
  public Money totalPayable() {
    return netFederalTax().plus(netProvincialTax());
  }
  
  /** Line 149. Performs all calculations. Can be negative. */
  public Money balanceOwing() {
    Money result = totalPayable();
    return result.minus(coll.installments);
  }

  /** Line 30100. */
  public Money ageAmountCalc(Money ageAmount, Money threshold) {
    Money result = ZERO;
    if (ageYearsOnly() >= standardRetirementAge) {
      result = Util.baseMinusClawback(ageAmount, netIncome(), threshold, lowestTaxRate());
    }
    return result;
  }
  
  /** Prepare for a new tax year. Remove data collected from the current year. */
  public void resetNewYear(Integer year) {
    this.year = year;
    coll.resetToZero();
  }
  
  /** Line 40425. */
  public Money dividendTaxCredit() {
    Money grossUp = dividendGrossUp();
    Money result = grossUp.minus(coll.nraDvdIncome).times(divTaxCreditNumer).divByInt(divTaxCreditDenom);
    return result;
  }

  /** Line 31400. */
  public Money pensionIncomeAmountCalc(Money pensionIncomeMax) {
    Money result = ZERO;
    if (ageOnDec31() >= standardRetirementAge) {
      //CPP is not included here!
      Money pensionPlusRif = coll.pensionIncome.plus(coll.rifIncome).plus(coll.lifIncome);
      result = Util.lesserOf(pensionPlusRif, pensionIncomeMax);
    }
    return result;
  }
  
  public Money dividendGrossUp() {
    return coll.nraDvdIncome.times(1.38);
  }
  
  /** 
   Only takes into account the year, not the month or day.
   Not always the same as the person's age!
   This is needed in a tax return where you see 'if born in 1955 or earlier', for example.
  */
  public int ageYearsOnly() {
    return year() - dateOfBirth().getYear();
  }
  
  // PRIVATE 
  
  private Scenario scenario;
  private DateTime dateOfBirth;
  private Money personalAmount = ZERO;
  private Money personalAmountAdditional = ZERO;
  private MoneyRange personalAmountThreshold;
  private Money ageAmount = ZERO;
  private Money ageAmountClawback = ZERO;
  private Money pensionAmount = ZERO;
  
  private Integer year = 0;
  private Collector coll = new Collector();
  
  private TaxBrackets rifLifWithholdingTaxBrackets;
  
  private TaxBrackets taxBrackets;
  private ProvincialTax provTax;
  
  private Integer standardRetirementAge;
  private Double taxableCapitalGainFrac;
  private Integer divTaxCreditNumer; 
  private Integer divTaxCreditDenom;
  
  private FederalTaxReturn(
   Scenario scenario, Integer year, DateTime dateOfBirth, Money personalAmount, Money personalAmountAdditional, 
   MoneyRange personalAmountClawback, Money ageAmount, Money ageAmountClawback, Money pensionAmount, 
   TaxBrackets taxBrackets, TaxBrackets rifWithholdingTaxBrackets, 
   Integer stdRetAge, Double taxCapGainFrac, 
   Integer divTaxCreditNumer, Integer divTaxCreditDenom
  ) {
    this.scenario = scenario;
    this.year = year;
    this.dateOfBirth = dateOfBirth;
    this.personalAmount = personalAmount;
    this.personalAmountAdditional = personalAmountAdditional;
    this.personalAmountThreshold = personalAmountClawback;
    this.ageAmount = ageAmount;
    this.ageAmountClawback = ageAmountClawback;
    this.pensionAmount = pensionAmount;
    this.taxBrackets = taxBrackets;
    this.rifLifWithholdingTaxBrackets = rifWithholdingTaxBrackets;
    this.standardRetirementAge = stdRetAge;
    this.taxableCapitalGainFrac = taxCapGainFrac;
    this.divTaxCreditNumer = divTaxCreditNumer;
    this.divTaxCreditDenom = divTaxCreditDenom;
  }

  /** Items collected over the course of a year, because of transactions in the accounts.*/
  private static class Collector {
    Money installments = ZERO;
    Money oasIncome = ZERO; 
    Money gisIncome = ZERO; //paid at the same time as OAS, but GIS isn't taxable, so it needs a separate bucket 
    Money cppIncome = ZERO;
    Money pensionIncome = ZERO; //other than CPP
    Money employmentIncome = ZERO;
    Money rifIncome = ZERO;
    Money rifWithholdingTax = ZERO;
    Money lifIncome = ZERO;
    Money lifWithholdingTax = ZERO;
    Money nraDvdIncome = ZERO;
    Money nraInterestIncome = ZERO;
    void resetToZero() {
      this.installments = ZERO;
      this.oasIncome = ZERO;
      this.cppIncome = ZERO;
      this.pensionIncome = ZERO;
      this.employmentIncome = ZERO;
      this.rifIncome = ZERO;
      this.rifWithholdingTax = ZERO;
      this.nraDvdIncome = ZERO;
      this.nraInterestIncome = ZERO;
    }
  }

  Integer ageOnDec31() {
    return Util.age(dateOfBirth,  DateTime.forDateOnly(year,12,31));
  }
  
  /** Line 30000. Clawed back, but only partially, and only to a small extent. */
  private Money personalAmount() {
    Money result = ZERO;
    if (netIncome().lt(personalAmountThreshold.min())){
      result = personalAmount.plus(personalAmountAdditional);
    }
    else if (netIncome().lt(personalAmountThreshold.max())) {
      //only part of the "additional amount"; not the usual clawback logic (variation)
      Money excess = netIncome().minus(personalAmountThreshold.min());
      double fullRange = personalAmountThreshold.range().getAmount().doubleValue();
      double frac = excess.getAmount().doubleValue() / fullRange;
      Money partOfAdditional = personalAmountAdditional.times(frac);
      result = personalAmount.plus(partOfAdditional);
    }
    else {
      //no additional amount at all
      result = personalAmount; 
    }
    return result;
  }

  /** Line 30100. Clawed back. */
  private Money ageAmount() {
    return ageAmountCalc(ageAmount, ageAmountClawback);
  }
  
  /** Line 31400. */
  private Money pensionIncomeAmount() {
    return pensionIncomeAmountCalc(pensionAmount);
  }
  
  private double lowestTaxRate() {
    return taxBrackets.lowestTaxRate();
  }
  
  /** Schedule 3. */
  private Money taxableCapitalGain() {
    return scenario.capitalGainLoss.gainAfterOffsetsApplied(year).times(taxableCapitalGainFrac);
  }
  
  private Money rifMinimum() {
    Money result = Consts.ZERO;
    if (scenario.rif != null) {
      result = scenario.rif.withdrawalMin(scenario.rifValueJan1, year);
    }
    return result;
  }
  
  private Money lifMinimum() {
    Money result = Consts.ZERO;
    if (scenario.lif != null) {
      result = scenario.lif.withdrawalMin(scenario.lifValueJan1, year);
    }
    return result;
  }
  
  private Money lifMaximum() {
    Money result = Consts.ZERO;
    if (scenario.lif != null) {
      result = scenario.lif.withdrawalMax(scenario.lifValueJan1, year);
    }
    return result;
  }
}