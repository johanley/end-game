package endgame.survival;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import endgame.Scenario;
import endgame.util.Consts;
import endgame.util.Log;
import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** See package level comments for more details. */
public final class Survival {

  /** Called upon startup, to read in data tables. */
  public static void populateTables(String projectRoot) {
    populate(Sex.MALE, projectRoot);
    populate(Sex.FEMALE, projectRoot);
  }
  
  /**
   Return true only if the person has survived the current year of the simulation.
   This test is performed only on December 31 of each year.
   Controlled by {@link Scenario#annualTestForSurvival}. 
   In this simulation, the person dies only on December 31, and survives at least the first year.
  */
  public boolean hasSurvivedThe(Integer currentYear, Scenario sim) {
    boolean result = true;
    if (sim.annualTestForSurvival) {
      DateTime dob = new DateTime(sim.dateOfBirth);
      Integer ageOnDec31 = currentYear - dob.getYear();
      double p = probabilityOfSurvivingAnotherYear(ageOnDec31 - 1, sim.sex); // [0..1]
      ThreadLocalRandom generator = ThreadLocalRandom.current();
      double r = generator.nextDouble(); // [0..1)
      result = (r < p);
    }
    return result;
  }
  
  /**
   The relative probability of a Canadian surviving from the start-year of the simulation to future years.
   ({@link Scenario.sex} is used here.) 
   The returned data is useful for time-series plots, because it shows the probability of actually surviving to plotted future dates.

   @param maxValue lets the caller control the scale the data, to make it suitable for plotting on a graph that already  
   contains some principal data. The maxValue is usually the largest value of the principal data.
   If this param is empty, then the data is not scaled, and reflects the underlying lx number, based 
   on a cohort of 100,000 at birth (age 0). 
   
   @return the key of the returned map is a future year.
   The value of the returned map is the relative probability that the person will survive from
   {@link Scenario#startDate} to the future year.
  */
  public Map<Integer /*year*/, Double /*rel prob*/> relativeProbabilityOfSurvival(Scenario sim, Optional<Double> maxValue){
    // the core data works with age, but the map returned here uses the actual year, not the person's age
    Map<Integer, Double> result = new LinkedHashMap<>();
    
    DateTime dob = new DateTime(sim.dateOfBirth);
    Integer startAge = new DateTime(sim.startDate).getYear() - dob.getYear(); 
    Integer endAge = new DateTime(sim.endDate).getYear() - dob.getYear();
    
    Map<Integer /*age*/, Integer> table = tableFor(sim.sex);
    Integer startCohortPopulation = table.get(startAge); //assumption: monotonic decreasing from start-year
    Double scaleFactor = maxValue.isPresent() ? maxValue.get() / startCohortPopulation : 1.0; //avoid int div
    for(Integer age : table.keySet()) {
      if (startAge <= age && age <= endAge) {
        Double val = scaleFactor * table.get(age).doubleValue();
        val = Math.round(val * 100.0) / 100.0; //round to 2 decimal places
        int year = age + dob.getYear();
        result.put(year, val);
      }
    }
    return result;
  }
  
  //PRIVATE 
  
  /** Static because it's read from files upon startup, and needs to be long-lived. */
  private static Map<Integer /*age*/, Integer/*cohort population, originally 100,000*/> MALE_LX = new LinkedHashMap<>();
  private static Map<Integer, Integer> FEMALE_LX = new LinkedHashMap<>();
  
  private static Map<Integer, Integer> tableFor(Sex sex){
    return Sex.MALE == sex ? MALE_LX : FEMALE_LX;
  }
  
  private static void populate(Sex sex, String projectRoot) {
    Map<Integer, Integer> table = tableFor(sex);
    try {
      String fileLocation= projectRoot + "input\\probability-of-survival\\" + sex.name().toLowerCase() + "-lx.utf8";
      Log.log("Reading file " + fileLocation);
      List<String> lines = Util.read(fileLocation);
      for (String line : lines) {
        if (Util.isPresent(line)) {
          if (!line.trim().startsWith("#")) {
            addLineTo(table, line);
          }
        }
      }
    }
    catch(IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }
  
  /*
   "0 years","100,000"
   "1 year","99,515"
    ..
   "109 years","12"
   "110 years and over","6"
   */
  private static void addLineTo(Map<Integer, Integer> table, String line) {
    int comma = line.indexOf(",");
    try {
      String age = line.substring(0,comma).trim();
      int space = age.indexOf(Consts.SPACE);
      Integer yearsOfAge = Integer.valueOf(age.substring(1,space));
      
      String cohortSize = line.substring(comma+1).trim().replace(",", ""); 
      Integer cohortPopulation = Integer.valueOf(cohortSize.substring(1, cohortSize.length()-1));
      table.put(yearsOfAge, cohortPopulation);
    }
    catch(Throwable ex ) {
      throw new RuntimeException(ex + " Can't parse the line in the 'lx' life table file: " + line);
    }
  }
  
  /** Return the probability that a Canadian of a given age and sex will survive one more year. */
  private Double probabilityOfSurvivingAnotherYear(Integer age, Sex sex) {
    return probabilityOfSurvivingToTargetAge(age, age+1, sex);
  }
 
  /**
  The probability of someone with a given start-age (50+) reaching a later target-age (up to 100).

  <P>For the returned map, the first key is the start-age, the second key is the target-age, 
  and the final value is the probability.
  
   <p>This data is not dependent on the scenario details. 
   It's merely the life-table data presented in a useful way that many people would want to see.
  */
  private Map<Integer /*start-age*/, Map<Integer /*target-age*/, Double /*prob-of-survival*/>> probabilityOfSurvivalTable(Sex sex){
    Map<Integer, Map<Integer, Double>> result = new LinkedHashMap<>();
    int START = 50;
    int END = 100;
    for(int startAge = START; startAge <= END; ++startAge) {
      Map<Integer,Double> map = new LinkedHashMap<>(); 
      for(int targetAge = startAge + 1; targetAge <= END+1; ++targetAge) {
        double prob = probabilityOfSurvivingToTargetAge(startAge, targetAge, sex);
        map.put(targetAge, prob);
      }
      result.put(startAge, map);
    }
    return result;
  }
  
  /** Return the probability that a Canadian of a given age and sex will survive to a later target age. */
  private Double probabilityOfSurvivingToTargetAge(Integer startingAge, Integer targetAge, Sex sex) {
    Map<Integer, Integer> table = tableFor(sex);
    Integer popAtStartingAge = table.get(startingAge);
    Integer popAtTargetAge = table.get(targetAge);
    double result = popAtTargetAge.doubleValue() / popAtStartingAge.doubleValue(); // AVOID INTEGER DIVISION
    return result;
  }
}