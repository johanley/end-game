package endgame.transaction;

import endgame.Scenario;
import endgame.account.Account;
import endgame.util.Log;
import hirondelle.date4j.DateTime;

/** 
 Abstract Base Class for executing transactions.
 An action that changes the state of the simulation, usually by changing the state of an {@link Account}.
 Transactions can be defined to be executed periodically, or on one specific day, and between a start-date and stop-date.
*/
public abstract class Transactional {
  
  /** 
   Constructor.
   
   See {@link TransactionDates} for the syntax rules on this parameter.
  */
  protected Transactional(String rawWhenControl) {
    whenControl = new TransactionDates(rawWhenControl); 
  }

  protected Transactional(TransactionDates whenControl) {
    this.whenControl = whenControl;
  }
  
  /** 
   Template method. 
   If the current date matches up with the dates of this transaction, then call {@link #execute(DateTime, Scenario)}. 
  */
  public void executeOnDate(DateTime currentDate, Scenario sim) {
    if (isInStartStopRange(currentDate) && executionDatesMatchThe(currentDate)) {
      execute(currentDate, sim);
    }
  }
  
  /** 
   Implemented by concrete subclasses.
   The {@link Scenario} object is passed here so that the transaction will have access to 
   any state that it may need in order to get its work done. 
  */
  abstract protected void execute(DateTime when, Scenario sim);

  /** For debugging only. Logs the date-time in a uniform, convenient way. */
  protected void logMe(DateTime when, Object text) {
    Log.log(when + ":" + this + " " + text.toString());
  }
  
  // PRIVATE 
  
  private TransactionDates whenControl;
  
  /** Return true only if the currentDate matches the dates associated with this transaction.  */
  private boolean executionDatesMatchThe(DateTime currentDate) {
    boolean result = false;
    String whenDate = currentDate.toString();
    for(String matchingDate : whenControl.matchingDates()) {
      if (whenDate.endsWith(matchingDate)) {
        result = true;
        break;
      }
    }
    return result;
  }
  
  private boolean isInStartStopRange(DateTime currentDate) {
    return 
      whenControl.startDate().lteq(currentDate) && 
      whenControl.stopDate().gteq(currentDate)
    ;
  }
}