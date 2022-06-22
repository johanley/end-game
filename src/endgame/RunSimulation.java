package endgame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import endgame.input.syntax.ParseException;
import endgame.input.syntax.ScenarioParser;
import endgame.model.Money;
import endgame.output.stats.yearly.AccountSet;
import endgame.output.stats.yearly.AccountSnapshot;
import endgame.output.stats.yearly.CashFlow;
import endgame.output.stats.yearly.History;
import endgame.output.stats.yearly.TaxSummary;
import endgame.output.stats.yearly.csv.CsvReports;
import endgame.survival.Survival;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import endgame.util.Log;
import endgame.util.MoneyFormatter;
import hirondelle.date4j.DateTime;

/**
 Run a simulation of your personal finances, as defined in a scenario.ini text file.
 The simulation is specific to retired Canadians.
 
 <P>A scenario may be run once or many times.
 The number of times is defined by the <em>num-monte-carlo-iterations</em> setting in your scenario.ini. 
 That setting should be 1 when no Monte Carlo randomness is needed (deterministic), for example when stock 
 prices are modeled as a having a fixed percentage increase per year, with no randomness.
 
 <P>Conversely, that setting should be more than 1 when randomness is needed (stochastic), for example 
 when changes in stock prices are modeled with a random generator of some sort.
 
 <P>Each run of the scenario is called a history. 
 So, this class may generate 1..N histories. 
   
 <P>This simulation moves a {@link Scenario} forward one day at a time, and checks for any applicable transactions,
  as defined in your scenario.ini. 
*/
public final class RunSimulation implements Runnable {

  /** This tool is hard-coded to a single version of the syntax: {@value}.  */
  public static final String SYNTAX_VERSION = ScenarioParser.SYNTAX_VERSION;

  /** 
   Run the simulation.
   The location of the configuration file (scenario.ini) can be passed as the first (and only) 
   command-line argument. (You can also hard code it. See below.)
  */
  public static void main(String... args) throws ParseException, IOException {
    //There are two styles for pointing to the scenario file.
    //You have to pick one or the other.
    //Use comments '//' to make one style active, and the other style inactive.
    //Then save your change using the menu, File->Save.
    
    //Style #1. This way just hard-codes the file location.
    //Note the doubling of the separators in the file name location!
    String scenario = "C:\\myworkspace\\end-game\\scenario\\01.1\\01.1-no-savings-CPP-0.ini";
    
    //Style #2. This alternate way uses arguments passed on the command line.
    //For beginners, this is a bit harder to use.
    //String scenario = args[0];
    
    RunSimulation runner = new RunSimulation(scenario);
    runner.run();
    //runner.parseOnly();
  }
  
  /**
   Constructor.  
   @param configFileLocation the absolute location of your config file, which has all the 
   settings needed to run the scenario.
  */
  public RunSimulation(String configFileLocation) throws ParseException, IOException {
    this.configFile = configFileLocation;
  }
  
  /** Parse the input scenario file, but don't run it. */
  void parseOnly() {
    Log.log("Parsing input only. ");
    try {
      Scenario scenario = readInputFile();
      scenario.populateAndValidate();
      Log.log("Done.");
    } 
    catch (Throwable ex) {
      Log.error(ex.toString(), ex);
    }
  }
  
  /** 
   Run the scenario.
   The scenario might be repeated N times, according to the <em>num-monte-carlo-iterations</em> setting.
  */
  @Override public void run() {
    Log.log("Running the scenario...");
    long beginTime = System.nanoTime();
    try {
      Scenario scenario = readInputFile();
      Log.log(scenario.description);
      scenario.populateAndValidate();
      DateTime startDate = new DateTime(scenario.startDate);
      DateTime endDate = new DateTime(scenario.endDate); 
      Integer numHistories = scenario.numIterations;

      if (numHistories>1) Log.enableLoggingToConsole(false);
          
      for(int hist = 1; hist <= numHistories; ++hist) {
        Log.forceConsole("History #" + hist);
        DateTime currentDate = startDate;
        boolean isStillAlive = true;
        Log.log("Incrementing one day at a time, starting with " + startDate);
        while (isStillAlive && currentDate.lteq(endDate)) {
          if (isYearStart(currentDate, startDate)) {
            resetForNewYear(scenario, currentDate);
          }
          transactionsUpdateThe(scenario, currentDate);
          if (isYearEnd(currentDate)) {
            isStillAlive = yearEndForThis(hist, scenario, currentDate);
          }
          currentDate = currentDate.plusDays(1);
        }
        if (hist < numHistories) {
          Log.log("Re-init of the scenario for the next history; start from scratch. Read the scenario file again.");
          scenario = readInputFile();
        }
        endHistory(scenario, hist, currentDate);
      }
      
      endAllProcessing(beginTime, scenario);
     
      Log.forceConsole("Done.");
      Log.flushLogBufferFor(scenario, new File(configFile));
    }
    catch(Throwable ex) {
      Log.error(ex.toString(), ex);
    }
  }

  // PRIVATE
  
  /** 
   Note that the parser object is not stored - only the file location.
   This lets the code re-read the config, and start from scratch for each Monte Carlo iteration.
   This ensures there's no cross-talk between the various histories. 
  */
  private String configFile = "";
  
  /** History of a single iteration, from the start date to the end date. */
  private History history = new History();
  private Map<Integer /*iteration*/, History> histories = new LinkedHashMap<>();
  
  private String format(Money money) {
    MoneyFormatter fmt = new MoneyFormatter();
    return fmt.format(money);
  }
  
  /** The scenario is created by reading a text configuration file. */
  private Scenario readInputFile() throws ParseException, IOException {
    Log.log("Reading the input scenario file: " + configFile);
    checkVersionBeforeFullParse(configFile);
    return ScenarioParser.parse(configFile);
  }
  
  private void checkVersionBeforeFullParse(String fileName) throws IOException {
    Log.log("Checking the syntax-version of the file. It should be " + SYNTAX_VERSION + ", otherwise the scenario file can't be read properly.");
    Path path = Paths.get(fileName);
    try (Scanner scanner =  new Scanner(path, Consts.ENCODING.name())){
      while (scanner.hasNextLine()){
        String line = scanner.nextLine().trim();
        if (line.startsWith("syntax-version")) {
          String[] parts = line.split(Pattern.quote("="));
          String version = parts[1].trim();
          if (!version.equalsIgnoreCase(SYNTAX_VERSION)) {
            Log.log(" *** PROBLEM *** : Version detected as " + version + ". Doesn't match the expected value of '" + SYNTAX_VERSION + "'.");
            Log.log("The code is incompatible with the given scenario file.");
            Log.log("You may have to change your scenario file to be compatible with the new code. Please see docs for more info.");
            throw new RuntimeException("Aborted. The code version is not compatible with the syntax-version of your scenario file.");
          }
          break;
        }
      }      
    }
  }

  private boolean isYearEnd(DateTime currentDate) {
    return currentDate.getMonth() == 12 && currentDate.getDay() == 31;
  }
  
  private boolean isYearStart(DateTime currentDate, DateTime startDate) {
    boolean result = false;
    if (currentDate.gt(startDate)) {
      result = currentDate.getMonth() == 1 && currentDate.getDay() == 1;
    }
    return result;
  }
  
  /** Return true only if the person has survived the year. */
  private boolean yearEndForThis(Integer iter, Scenario sim, DateTime currentDate) {
    boolean result = true;
    Log.log(currentDate.getYear() + " year-end.");
    takeSnapshotsAndCashFlows(sim, currentDate);
    logYearlySnapshotsAndCashFlows(sim, currentDate);
    Survival survival = new Survival();
    result = survival.hasSurvivedThe(currentDate.getYear(), sim);
    //RIF minimums are already checked by the tax return.
    //TFSA limits are validated by TfsaRoom
    return result;
  }
  
  private void takeSnapshotsAndCashFlows(Scenario sim, DateTime currentDate) {
    history.takeSnapshotOf(sim.taxReturn);
    history.takeSnapshotOf(sim.investmentAccounts(), sim.bank, currentDate);
    history.cashFlow.put(currentDate.getYear(), sim.yearlyCashFlows);
    sim.lastYearsTaxSummary = history.taxSummary.get(currentDate.getYear());
  }

  private void logYearlySnapshotsAndCashFlows(Scenario sim, DateTime currentDate) {
    int year = currentDate.getYear();
    Log.log(history.cashFlow.get(year));
    Log.log(history.taxSummary.get(year));
    //the accounts are multiple in number
    Log.log("Accounts");
    AccountSet accountSet = history.accountSet.get(year);
    for (AccountSnapshot snap : accountSet.accountSnapshots()) {
      Log.log(snap);
    }
    Log.log(format(sim.netWorth()) + " Net worth");
    Log.log(format(sim.investmentsWorth()) + " Investments worth" + Consts.NL);
  }
  
  private void transactionsUpdateThe(Scenario sim, DateTime currentDate) {
    for (Transactional transaction : sim.transactionals) {
      transaction.executeOnDate(currentDate, sim);
    }
  }
  
  /** Housekeeping of various data objects. Not called for very first Jan 1. */
  private void resetForNewYear(Scenario sim, DateTime when) {
    //fresh object needed; the old one has already been added to history
    sim.yearlyCashFlows = new CashFlow();
    
    Integer year = when.getYear();

    //remember data needed for the rif minimum
    if (sim.rif != null && sim.rifValueJan1.isPlus()) {
      sim.rifValueJan1 = sim.rif.value();
      Money rifMin = sim.rifMinima.compute(sim.rifValueJan1, year, sim.rif.rspToRifConversionDate());
      if (rifMin.eq(Consts.ZERO)) {
        Log.log(format(Consts.ZERO) + " No RIF minimum for " + year + " (RSP converts on " + sim.rif.rspToRifConversionDate() + ")." );
      }
      else {
        Log.log(format(rifMin) + " RIF minimum for " + year);
      }
    }
    
    sim.taxReturn.resetNewYear(year);
    //reset of the provincial is not needed
    
    //recalc tfsa room for this year, reflecting both the new yearly-limit and past withdrawals
    if (sim.tfsaRoom != null) {
      sim.tfsaRoom.yearlyIncrease(); // increase by the 'standard' yearly amount
      Money tfsaRoom = sim.tfsaRoom.roomFor(year); //includes past withdrawals
      Log.log(format(tfsaRoom) + " TFSA room for " + year  );
    }
  }

  private void endHistory(Scenario sim, Integer iteration, DateTime currentDate) {
    histories.put(iteration, history);
    
    Log.log("Summations (nominal)");
    CashFlow cashFlowSum = CashFlow.sumOver(history.cashFlow.values());
    Log.log(cashFlowSum);
    
    TaxSummary taxReturnSum = TaxSummary.sumOver(history.taxSummary.values());
    Log.log(taxReturnSum);
    
    Log.log("Summations (per year)");
    cashFlowSum = CashFlow.sumOverPerYear(history.cashFlow.values());
    Log.log(cashFlowSum);
    
    taxReturnSum = TaxSummary.sumOverPerYear(history.taxSummary.values());
    Log.log(taxReturnSum);
    
    logNetGrossAndTax(sim, currentDate);
    
    history = new History();
  }

  /** For each year, log the after-tax income, before-tax income, and tax paid. */
  private void logNetGrossAndTax(Scenario sim, DateTime currentDate) {
    Log.log("# Net, gross, tax paid:");
    Money netTotal = Consts.ZERO;
    Money grossTotal = Consts.ZERO;
    Money taxTotal = Consts.ZERO;
    int startYear = new DateTime(sim.startDate).getYear();
    int numYears = history.numYears();
    for(int year = startYear; year < (startYear + numYears); ++year) {
      Money gross = history.cashFlow.get(year).cashFlow();
      Money tax = history.taxSummary.get(year).taxPayable;
      Money net = gross.minus(tax);
      Integer age = year - Integer.valueOf(sim.dateOfBirth.substring(0, 4));
      Log.log("# " + year + " net: " + net + " = " + gross + " - " + tax + ".  [Net worth:" + history.accountSet.get(year).netWorth() + "] (age " + age + ")");
      netTotal = netTotal.plus(net);
      grossTotal = grossTotal.plus(gross);
      taxTotal = taxTotal.plus(tax);
    }
    Log.log("#   Total: " + netTotal + " = " + grossTotal + " - " + taxTotal + "   (Net Worth: " + sim.netWorth()+")");
  }

  private void endAllProcessing(long beginTime, Scenario sim) {
    CsvReports reports = new CsvReports();
    reports.save(sim, histories, configFile);

    Log.log("Scenario description: " + sim.description);
    Log.log("Start-date: " + sim.startDate + " end-date: " + sim.endDate);
    long endTime = System.nanoTime();
    Log.log("Execution time: " + (endTime - beginTime)/1000000000.0D + "s");
  }
  
}