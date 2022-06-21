package endgame.tax.provincial;

import endgame.model.Money;
import endgame.tax.TaxBrackets;

/** Fields converted from String to the proper type. */
public final class ProvTaxFields {
  
  public String jurisdiction;
  public Money personalAmt;
  public Money personalAmtSupplement;
  public Money personalAmtThreshold;
  public Double personalAmtRate;
  
  public Money ageAmt;
  public Money ageAmtThreshold;
  public Money ageAmtSupplement;
  public Money ageAmtSupplementThreshold;
  public Double ageAmtSupplementRate;

  public Money ageTaxCredit;
  public Money ageTaxCreditThreshold;

  public Money pensionIncomeMax;
  public Double pensionIncomeRate;
  public Double dvdGrossUpMult;
  public TaxBrackets taxBrackets;
  
  public Money lowIncomeBasic;
  public Money lowIncomeAge;
  public Money lowIncomeThreshold;
  public Double lowIncomeRate;

  public Money surtaxThreshold1;
  public Double surtaxRate1;
  public Money surtaxThreshold2;
  public Double surtaxRate2;
  public TaxBrackets healthPremiumTaxBrackets;
  
  public Money scheduleBThreshold;
  public Double scheduleBRate;
  public Money liveAloneAmt;
  
}
