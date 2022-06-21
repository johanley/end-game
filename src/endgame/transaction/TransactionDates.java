package endgame.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import hirondelle.date4j.DateTime;

/** Control when a transaction is executed, using dates specified textually in a number of different ways. */
public final class TransactionDates {

  /**
   Created from input in the scenario file.
    
   The input parameter is in two parts. The first part looks like this :
   <pre>
     on 2022-02-28   # one specific day
     on *-12-25      # every December 25
     on *-01         # the first day of every month  
    </pre>
    The above can be combined in a list as well:
   <pre>
     on *-01-04, *-04-04, *-08-04, *-12-04  # quarterly, on the 4th of the month
     on 2021-01-15, 2022-01-15              # two specific days
   </pre>
   
   <P>The second part is optional, and uses an ellipsis '..' to define a valid date range for a transaction. 
   If not specified, the valid date range is for the full duration of the simulation.
   You can specify a start-date, an end-date, or both.
   <pre>
     on *-12-25 | 2022-01-01..2027-12-31   # every Christmas, in years 2022 to 2027
     on *-12-25 | 2030-01-01..             # start in 2030, and never stop
     on *-12-25 | ..2042-12-25             # start at the beginning of the simulation, and end on Christmas 2042
    </pre>
  */
  TransactionDates(String when) {
    assignStartStopIfPresent(when);
    findMatchingDates(when);
  }
  
  public static TransactionDates fromWhen(String when) {
    return new TransactionDates(when);
  }
  
  public static TransactionDates fromYMD(String yyyymmdd) {
    return new TransactionDates(ON + yyyymmdd);
  }
  
  public static TransactionDates fromStartDateAndDD(String startDate, String paymentDay) {
    return new TransactionDates(ON + STAR_PREFIX + paymentDay + PIPE + startDate + ELLIPSIS);
  }
  
  /** 
   Start of the date range for the transaction (inclusive).
   Defaults to 0001-01-01 if not present.
  */
  DateTime startDate() { return start; }
  
  /** 
   End of the date range for the transaction (inclusive).
   Defaults to 9999-12-31 if not present.
  */
  DateTime stopDate() { return stop; }
  
  /** 
   Dates occurring between the start and stop dates, on which the transaction is executed.
   The matching relies on simple textual matching, and the order yyyy-mm-dd: if the date ends with the given text,
   then it's a match, and the transaction will execute on that date.
  */
  List<String> matchingDates() { return Collections.unmodifiableList(matchingDates); }
  
  // PRIVATE 
  
  /* 
   Magic default values, if not present in the input.
   These magic values simplify the logic somewhat, and avoid check-for-null. 
  */
  private DateTime start = new DateTime("0001-01-01");
  private DateTime stop = new DateTime("9999-12-31");
  
  /** The strings that specify the dates on which this transaction is executed. */
  private List<String> matchingDates = new ArrayList<>();

  /* Delimiters in the input. */
  private static final String PIPE = "|";
  private static final String COMMA = ",";
  private static final String ELLIPSIS = "..";
  
  /* Distinguish the various cases just by the length of the text. */
  private static final int YMD_MODE = "9999-01-31".length();
  private static final String STAR_PREFIX = "*-";
  private static final String ON = "on ";
  
  private boolean isPresent(int index) {
    return index != -1;
  }
  
  private void assignStartStopIfPresent(String rawDateControl) {
    int pipe = rawDateControl.indexOf(PIPE);
    if (isPresent(pipe)) {
      String dateRange = rawDateControl.substring(pipe+1).trim();
      int ellip = dateRange.indexOf(ELLIPSIS);
      if (dateRange.startsWith(ELLIPSIS)) {
        stop = new DateTime(dateRange.substring(ellip + ELLIPSIS.length()));
      }
      else if (dateRange.endsWith(ELLIPSIS)) {
        start = new DateTime(dateRange.substring(0, ellip));
      }
      else {
        start = new DateTime(dateRange.substring(0, ellip));
        stop = new DateTime(dateRange.substring(ellip + ELLIPSIS.length()));
      }
    }
  }
  
  private void findMatchingDates(String rawDateControl) {
    int sep = rawDateControl.indexOf(PIPE);
    String rawDates = (!isPresent(sep)) ? rawDateControl.trim() : rawDateControl.substring(0, sep).trim();
    rawDates = removeONFrom(rawDates);
    String[] parts = rawDates.split(Pattern.quote(COMMA));
    for(String part : parts) {
      String input = part.trim();
      if (YMD_MODE == input.length()) {
        matchingDates.add(input);
      }
      else  {
        matchingDates.add(removeStarPrefixFromThe(input));
      }
    }
  }
  
  private String removeONFrom(String rawDates) {
    return rawDates.substring(ON.length());
  }

  private String removeStarPrefixFromThe(String input) {
    return input.substring(STAR_PREFIX.length());
  }
}