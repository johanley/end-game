package endgame.tax.provincial;

import endgame.input.syntax.ScenarioParser;
import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.tax.TaxBrackets;
import endgame.util.Consts;
import endgame.util.Util;

/**
 Catch-all for data appearing in provincial tax returns.
 Not all fields apply to all jurisdictions! 
*/
public final class ProvincialTaxFields {

  public String jurisdiction;
  
  public String personalAmt;
  public String personalAmtSupplement;
  public String personalAmtThreshold;
  public String personalAmtRate;
  
  public String ageAmt;
  public String ageAmtThreshold;
  public String ageAmtSupplement;
  public String ageAmtSupplementThreshold;
  public String ageAmtSupplementRate;

  public String ageTaxCredit;
  public String ageTaxCreditThreshold;

  public String pensionIncomeMax;
  public String pensionIncomeRate;
  public String dvdGrossUpMult;
  public TaxBrackets taxBrackets;
  
  public String lowIncomeBasic;
  public String lowIncomeAge;
  public String lowIncomeThreshold;
  public String lowIncomeRate;

  public String surtaxThreshold1;
  public String surtaxRate1;
  public String surtaxThreshold2;
  public String surtaxRate2;
  public TaxBrackets healthPremiumTaxBrackets;
  
  public String scheduleBThreshold;
  public String scheduleBRate;
  public String liveAloneAmt;
  
  /**
   Return the implementation of {@link ProvincialTax} consistent with the supplied fields.
   
   <P>This method is really doing the job of the {@link ScenarioParser}. 
   But with so many jurisdictions and possibilities, implementing it there seems a bit onerous at the moment.
  */
  public ProvincialTax deduceFromFieldsPresent(FederalTaxReturn fed) {
    ProvincialTax result = null;
    ProvTaxFields fields = convertFromStrings();
    String juris = fields.jurisdiction;
    int CORE = 5;
    //hard coded!
    if (juris.equals("NB")) {
      check(CORE+3, lowIncomeBasic, lowIncomeThreshold, lowIncomeRate);
      result = new NBTaxReturn(fields, fed);
    }
    else if (juris.equals("BC")) {
      //same structure as NB, in this impl
      check(CORE+3,  lowIncomeBasic, lowIncomeThreshold, lowIncomeRate);
      result = new BCTaxReturn(fields, fed);
    }
    else if (juris.equals("NL")) {
      //same structure as NB, in this impl
      check(CORE+3,  lowIncomeBasic, lowIncomeThreshold, lowIncomeRate);
      result = new NLTaxReturn(fields, fed);
    }
    else if (juris.equals("PE")) {
      //one difference from NB, in this impl
      check(CORE+4, lowIncomeBasic, lowIncomeThreshold, lowIncomeRate, lowIncomeAge);
      result = new PETaxReturn(fields, fed);
    }
    else if (juris.equals("NS")) {
      check(
        CORE+11, 
        personalAmtThreshold, personalAmtSupplement, personalAmtRate, ageAmtSupplement, ageAmtSupplementRate, ageAmtSupplementThreshold, 
        lowIncomeBasic, lowIncomeThreshold, lowIncomeRate, ageTaxCredit, ageTaxCreditThreshold
      );
      result = new NSTaxReturn(fields, fed);
    }
    else if (juris.equals("ON")) {
      check(CORE+6, lowIncomeBasic, surtaxThreshold1, surtaxRate1, surtaxThreshold1, surtaxRate2, healthPremiumTaxBrackets);
      result = new ONTaxReturn(fields, fed);
    }
    else if (juris.equals("QC")) {
      check(8, 
        scheduleBThreshold, scheduleBRate, personalAmt, ageAmt, 
        liveAloneAmt, pensionIncomeMax, pensionIncomeRate, dvdGrossUpMult
      );
      result = new QCTaxReturn(fields, fed);
    }
    else {
      // used for: MB, SK, AB, YT, NT, NU
      check(CORE);
      result = new GENERICTaxReturn(fields, fed);
    }
    return result;
  }

  /** Convert string fields to the appropriate types. */
  public ProvTaxFields convertFromStrings() {
    ProvTaxFields result = new ProvTaxFields();
    result.jurisdiction = jurisdiction; // no conversion needed
    result.personalAmt = from(personalAmt);
    result.personalAmtThreshold = from(personalAmtThreshold);
    result.personalAmtSupplement = from(personalAmtSupplement);
    result.personalAmtRate = fromPercent(personalAmtRate);
    result.ageAmt = from(ageAmt);
    result.ageAmtThreshold = from(ageAmtThreshold);
    result.pensionIncomeMax = from(pensionIncomeMax);
    result.pensionIncomeRate = fromPercent(pensionIncomeRate);
    result.dvdGrossUpMult = fromPercent(dvdGrossUpMult);
    result.taxBrackets = taxBrackets; //no conversion needed
    result.lowIncomeThreshold = from(lowIncomeThreshold);
    result.lowIncomeAge = from(lowIncomeAge);
    result.lowIncomeBasic = from(lowIncomeBasic);
    result.lowIncomeRate = fromPercent(lowIncomeRate);
    result.ageAmtSupplement = from(ageAmtSupplement);
    result.ageAmtSupplementThreshold = from(ageAmtSupplementThreshold);
    result.ageAmtSupplementRate = fromPercent(ageAmtSupplementRate);
    result.ageTaxCredit = from(ageTaxCredit);
    result.ageTaxCreditThreshold = from(ageTaxCreditThreshold);
    result.surtaxThreshold1 = from(surtaxThreshold1);
    result.surtaxThreshold2 = from(surtaxThreshold2);
    result.surtaxRate1 = fromPercent(surtaxRate1);
    result.surtaxRate2 = fromPercent(surtaxRate2);
    result.healthPremiumTaxBrackets = healthPremiumTaxBrackets; // no conversion needed
    result.scheduleBRate = fromPercent(scheduleBRate);
    result.scheduleBThreshold = from(scheduleBThreshold);
    result.liveAloneAmt = from(liveAloneAmt);
    return result;
  }
  
  // PRIVATE
  
  private Money from(String amount) {
    Money result = null;
    if (amount != null) {
      result = new Money(amount);
    }
    return result;
  }
  
  private Double fromPercent(String doubl) {
    Double result = null;
    if (doubl != null) {
      result = Util.percentFrom(doubl);
    }
    return result;
  }
  
  /** Excludes jurisdiction, fed, and tax brackets, since they are always present. */
  private int numFieldsPresentInScenarioFile() {
    int result = 0;
    Object[] fields = {
      personalAmt, personalAmtSupplement, personalAmtThreshold, personalAmtRate, 
      ageAmt, ageAmtThreshold, 
      ageAmtSupplement, ageAmtSupplementThreshold, ageAmtSupplementRate,
      pensionIncomeMax, pensionIncomeRate, 
      dvdGrossUpMult, 
      lowIncomeBasic, lowIncomeAge, lowIncomeThreshold, lowIncomeRate,
      ageTaxCredit, ageTaxCreditThreshold,
      surtaxThreshold1, surtaxRate1, surtaxThreshold2, surtaxRate2, 
      healthPremiumTaxBrackets,
      scheduleBRate, scheduleBThreshold, liveAloneAmt
    };
    for (Object field : fields) {
      if (field != null) {
        ++result;
      }
    }
    return result;
  }
  
  /**
   This is really doing a job that could be done by the parser, if the parser was more complex, and 
   knew about the structure of the jurisdictions. 
  */
  private void check(int numFields, Object... optionalFieldsPresent) {
    int expected = numFieldsPresentInScenarioFile();
    String errorMsg = "";
    int diff = numFields - expected;
    if (diff > 0) {
      errorMsg = jurisdiction + " provincial tax problem: " + diff + " unexpected field(s) present." + Consts.NL;
    }
    else if (diff < 0) {
      errorMsg = jurisdiction + " provincial tax problem: " + diff + "  field(s) missing." + Consts.NL;
    }
    
    int countMissing = 0;
    for(Object thing : optionalFieldsPresent) {
      if (thing == null) {
        ++countMissing;
      }
    }
    if (countMissing > 0) {
      errorMsg = errorMsg + jurisdiction  + " provincial tax problem: missing " + countMissing + " expected items."; 
    }
    
    if (errorMsg.length() > 0) {
      throw new RuntimeException(errorMsg);
    }
  }
}
