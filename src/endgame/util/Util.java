package endgame.util;

import static endgame.util.Consts.ZERO;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import endgame.model.Money;
import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;

public final class Util {

  /** Has visible text. */
  public static final boolean isPresent(String text) {
    return text != null && text.trim().length() > 1;
  }
  
  /** The list separator is a comma. */
  public static List<String> chopList(String items) {
    return chopInput(items, Consts.DATE_SEP);
  }
  
  /** Remove the percent sign. */
  public static String chopPercent(String percent) {
    return percent.replace("%", "").trim();
  }
  
  /** '5%' is converted to 0.05. */ 
  public static Double percentFrom(String rawString) {
    String value = rawString.substring(0, rawString.length()-1);
    return Double.valueOf(value)/100.0;
  }
  
  /** Returns negative if b is before a. */
  public static Integer numMonthsBetween(DateTime a, DateTime b) {
    Integer result = 0;
    boolean aFirst = a.compareTo(b) < 0;
    if (! a.equals(b)) {
      int ONE_MONTH = 1;
      DateTime start = aFirst ? a : b;
      DateTime end = aFirst ? b : a;
      while (start.lt(end)) {
        ++result;
        start = start.plus(0, ONE_MONTH, 0, 0, 0, 0, 0, DayOverflow.FirstDay); 
      }
    }
    return aFirst ? result : -1 * result; 
  }
  
  public static Integer age(DateTime birth, DateTime present) {
    //coerce Feb 29 to Mar 1
    DateTime dob = birth;
    if (dob.getMonth() == 2 && dob.getDay() == 29) {
      dob = DateTime.forDateOnly(birth.getYear(), 3, 1);
    }
    
    int result = present.getYear() - dob.getYear();
    DateTime birthdayThisYear = DateTime.forDateOnly(present.getYear(), dob.getMonth(), dob.getDay());
    
    if (birthdayThisYear.gt(present)) {
      --result;
    }
    return result;
  }

  private static List<String> chopInput(String input, String sep){
    List<String> result = new ArrayList<String>();
    String[] parts = input.split(sep);
    for (String part : parts) {
      if (part.trim().length() > 0) {
        result.add(part.trim());
      }
    }
    return result;
  }
  
  /** Read the contents of the given file. */
  public static List<String> read(String fileLocation) throws IOException {
    List<String> result = new ArrayList<String>();
    Scanner scanner = new Scanner(new FileInputStream(fileLocation), Consts.ENCODING);
    try {
      while (scanner.hasNextLine()){
        result.add(scanner.nextLine());
      }
    }
    finally{
      scanner.close();
    }
    return result;
  }
  
  public static void saveLinesToFile(String fileName, List<String> lines) {
    Path path = Paths.get(fileName);
    try (BufferedWriter writer = Files.newBufferedWriter(path, Consts.ENCODING)){
      for(String line : lines){
        writer.write(line);
        writer.newLine();
      }
      lines.clear();
    }
    catch(IOException ex) {
      Log.error("Can't write to file.", ex);
    }
  }
  
  /** The income can be different income-lines from the tax return. */
  public static Money clawback2(Money income, Money threshold, Double rate) {
    Money result = income.minus(threshold).times(rate);
    return nonNegative(result);
  }
  
  /** 
   The income can be different income-lines from the tax return.
   This formula is repeated in many places. 
  */
  public static Money baseMinusClawback(Money base, Money income, Money threshold, Double rate) {
    Money clawback = nonNegative(income.minus(threshold).times(rate));
    return nonNegative(base.minus(clawback));
  }
  
  public static Money nonNegative(Money amount) {
    return amount.isMinus() ? ZERO : amount;
  }
  
  public static Money lesserOf(Money a, Money b) {
    Money result = ZERO;
    if (!a.isMinus() && !b.isMinus()) {
      if (a.lteq(b)) {
        result = a;
      }
      else {
        result = b;
      }
    }
    else {
      throw new IllegalArgumentException("Both items should be non-negative.");
    }
    return result;
  }
}
