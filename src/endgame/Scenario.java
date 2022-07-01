package endgame;

import static endgame.util.Consts.NL;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import endgame.account.Account;
import endgame.account.lif.Lif;
import endgame.account.lif.LifMaxima;
import endgame.account.rif.Rif;
import endgame.account.rif.RifLifMinima;
import endgame.account.tfsa.TfsaRoom;
import endgame.bank.BankAccount;
import endgame.entitlements.GisAmount;
import endgame.model.Money;
import endgame.model.YearZero;
import endgame.output.stats.yearly.CashFlow;
import endgame.output.stats.yearly.TaxSummary;
import endgame.security.stock.Stock;
import endgame.security.stock.commission.Commission;
import endgame.security.stock.price.StockPricePolicy;
import endgame.survival.Sex;
import endgame.survival.Survival;
import endgame.tax.CapitalGainLoss;
import endgame.tax.FederalTaxReturn;
import endgame.tax.provincial.ProvincialTax;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** 
 Captures the data in a simulation, running from the start-year to the end-year.
 This class is mainly pointers to various objects representing the parts of the simulation/calculation, with very little code.
 It's a big struct. 
 
 <P>Design note: the main scenario data here is simply public fields.
 Almost all fields in this class have configuration that comes from a scenario.ini text file.
 
 <P>Most of these objects remain in place, and are mutated during the entirety of the simulation.
 A few objects are 'reset' in certain ways at the beginning of each new year.
 
 <P>This object is often called a 'simulation' or 'sim' in code.
 
 <P>This object is a global 'God' object, which points to all of the major elements in the simulation.
 Although objectionable on those grounds, it's also true that the complexity of the problem means that
 calculations can depend on many different parts of the problem, in a way that's often hard to predict.
*/
public final class Scenario {

  /** Full location of the scenario's .ini file. */
  public String scenarioFile = "";
  
  /** Succinct description of the scenario as a whole. */
  public String description = "";
  
  /** 
   The number of times no run the scenario. 
   1 for deterministic scenarios, N for stochastic scenarios (affected by stochastic changes in stock prices). 
  */
  public Integer numIterations = 0;
  
  public String dateOfBirth = "";
  /** 
   If true, then use life tables to determine if the person dies in a given year (on Dec 31).
   If the person does die, then the history terminates before the {@link #endDate} of the simulation.  
  */
  public Boolean annualTestForSurvival = Boolean.FALSE;
  /** The sex of the person whose finances are being modeled. Used by life tables. */
  public Sex sex;
  
  /** The start date of the simulation. Must be the start of the year, Jan  1. */
  public String startDate = "";
  /** 
   The end date of the simulation. Must be the end of the year, Dec 31.
   See {@link #annualTestForSurvival}. 
  */
  public String endDate = "";

  /** All stocks referenced by the scenario. */
  public List<Stock> stocks = new ArrayList<Stock>();

  /** The person's bank account. */
  public BankAccount bank = null;
  
  /** The person's Tax Free Savings Account. */
  public Account tfsa = null;
  /** The contribution room that the person has in their Tax Free Savings Account, in a given year. */
  public TfsaRoom tfsaRoom = null;
  
  /** The person's Retirement Income Fund. Can have RSP or RIF, but not both. */
  public Rif rif = null;
  /** 
   The market value of the RIF account at the start of business on Jan 1. Updated each year.
   If the value is zero, the account will have a minimum yearly withdrawal of 0. 
  */
  public Money rifValueJan1 = Consts.ZERO;
  
  /** The person's Non-registered Account (sometimes referred to as a Cash Account). */
  public Account nra = null;
  /** Capital gains and losses in a non-registered account. Has no entry in scenario.ini. */
  public CapitalGainLoss capitalGainLoss = new CapitalGainLoss();

  public Lif lif = null;
  /** 
   The market value of the LIF account at the start of business on Jan 1. Updated each year.
   If the value is zero, the account will have withdrawal limits of 0 dollars (max and min). 
  */
  public Money lifValueJan1 = Consts.ZERO;

  /** Yearly federal tax return. */
  public FederalTaxReturn taxReturn = null;
  /** Yearly provincial tax return. */
  public ProvincialTax provincialTaxReturn = null;
  /** Used for various tax calculations. Null for the first year of the simulation! */
  public TaxSummary lastYearsTaxSummary = null; 

  /** Transactions to be applied during the simulation. OAS, CPP, bank spending, dividend payments, and so on. */
  public List<Transactional> transactionals = new ArrayList<Transactional>();
  
  /** How to determine stock prices. */
  public StockPricePolicy stockPrices = null;
  /** Commission charged by an investment dealer. */
  public Commission commission = null;

  /** Unusual: this is reset to a fresh object once a year. */
  public CashFlow yearlyCashFlows = new CashFlow();
  
  /** Amounts needed to jump start the first year of the simulation. */
  public YearZero yearZero = null;

  /** Investment dealer accounts; excludes bank accounts. */
  public List<Account> investmentAccounts(){
    List<Account> result = new ArrayList<Account>();
    addAccount(rif, result);
    addAccount(lif, result);
    addAccount(tfsa, result);
    addAccount(nra, result);
    return Collections.unmodifiableList(result);
  }

  /** Stock data, given the ticker symbol. */
  public Stock stockFrom(String stockSymbol) {
    Stock result = null;
    for(Stock stock : stocks) {
      if (stock.symbol().equalsIgnoreCase(stockSymbol)) {
        result = stock;
        break;
      }
    }
    if (result == null) throw new RuntimeException("Unknown stock symbol:" + stockSymbol);
    return result;
  }
  
  /** Net worth of all investment accounts, plus the bank account. */
  public Money netWorth() {
    Money result = investmentsWorth();
    result = result.plus(bank.cash());
    return result;  
  }
  
  /** Net worth of the investment accounts. */
  public Money investmentsWorth() {
    Money result = Consts.ZERO;
    for (Account account : investmentAccounts()) {
      result = result.plus(account.value());
    }
    return result;  
  }

  /** For debugging, a dump of all fields in this class. */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    addLineToString("scenario", description, result);
    addLineToString("configFile", scenarioFile, result);
    addLineToString("sex", sex, result);
    addLineToString("numMonteCarloIterations", numIterations, result);
    addLineToString("dateOfBirth", dateOfBirth, result);
    addLineToString("annualTestForSurvival", annualTestForSurvival, result);
    addLineToString("simulationStartDate", startDate, result);
    addLineToString("simulationEndDate", endDate, result);
    addLineToString("stocks", stocks, result);
    addLineToString("stock prices", stockPrices, result);
    addLineToString("bank", bank, result);
    addLineToString("tfsa", tfsa, result);
    addLineToString("tfsaRoom", tfsaRoom, result);
    addLineToString("rif", rif, result);
    addLineToString("lif", lif, result);
    addLineToString("nra", nra, result);
    addLineToString("capitalGainLoss", capitalGainLoss, result);
    addLineToString("taxReturn", taxReturn, result);
    addLineToString("provincialTaxReturn", provincialTaxReturn, result);
    addLineToString("transactionals", transactionals, result);
    return result.toString();
  }

  /** Read in data files, and do miscellaneous checks on the data, that aren't otherwise validated. */
  public void populateAndValidate() {
    String projRoot = System.getProperty("user.dir") + File.separator;
    GisAmount.lookupGisBrackets(projRoot); 
    Survival.populateTables(projRoot);
    checkStartAndEndDates();
    checkFedTaxReturnInitialYear();
  }
  
  // PRIVATE 
  
  private void addLineToString(String name, Object value, StringBuilder builder) {
    builder.append(name + ": " + value + NL) ;
  }
  
  private void addAccount(Account account, List<Account> result) {
    if (account != null) {
      result.add(account);
    }
  }
  
  private void checkStartAndEndDates() {
    DateTime start = new DateTime(startDate);
    DateTime end = new DateTime(endDate);
    if (start.gteq(end)) {
      throw new RuntimeException("The start date " + startDate + " isn't before the end date " + endDate);
    }
    if (!startDate.endsWith("-01-01")) {
      throw new RuntimeException("The start date " + startDate + " isn't on Jan 1");
    }
    if (!endDate.endsWith("-12-31")) {
      throw new RuntimeException("The end date " + endDate + " isn't on Dec 31");
    }
  }
  
  private void checkFedTaxReturnInitialYear() {
    DateTime start = new DateTime(startDate);
    if (taxReturn.year().intValue() != start.getYear().intValue() ) {
      throw new RuntimeException("The federal tax return initial-year " + taxReturn.year() + " doesn't match the start-date " + startDate);
    }
  }


}