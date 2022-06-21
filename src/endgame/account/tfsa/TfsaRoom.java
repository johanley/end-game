package endgame.account.tfsa;

import static endgame.util.Consts.ZERO;

import java.util.ArrayList;
import java.util.List;

import endgame.model.Money;

/** 
 The amount you are allowed to contribute to your TFSA in the current year.
 
 Metaphor: a bucket of water. 
 TFSA-contributions drain the bucket.
 Each year the bucket is topped up by a standard amount.
 It's also topped up by TFSA-withdrawals from previous years.  
*/
public final class TfsaRoom {

  public static TfsaRoom valueOf(String initialRoom, String yearlyLimit) {
    return new TfsaRoom(new Money(initialRoom), new Money(yearlyLimit));
  }

  /**
   Called on Jan 1, to increase your room by the standard amount defined by the CRA.
   In this simulation, the amount of the increase is treated as fixed as the current yearly amount.
   This is in line with a simulation using 'real dollars', since the idea that an increase to the 
   TFSA contribution limit is (usually) meant to track inflation. 
  */
  public void yearlyIncrease() {
    mainRoom = mainRoom.plus(yearlyLimit);
  }
  
  /**
   Contributions to a TFSA reduce your room. 
   Blows up if this single contribution amount exceeds the available room, but be careful:
   the caller may make multiple contributions per year. 
  */
  public void reduceFromContribution(Money contribAmount, Integer year) {
    reduceBecauseOfContribution(contribAmount, year);
  }

  /** Withdrawals from a TFSA increase your room, but only for future years (after the given year). */
  public void increaseFromWithdrawal(Money withdrawalAmount, Integer year) {
    Withdrawal wd = new Withdrawal(year, withdrawalAmount);
    pastWithdrawalsRoom.add(wd);
  }
  
  /** 
   How much you can contribute to your TFSA, for the CURRENT year.
   Every time you make a contribution to your TFSA, it cannot exceed this limit.
   WARNING: this will not give you HISTORICAL values of your room.
  */
  public Money roomFor(Integer currentYear) {
    Money result = mainRoom;
    for (Withdrawal pastWithdrawal : pastWithdrawalsRoom) {
      if (currentYear > pastWithdrawal.year) {
        result = result.plus(pastWithdrawal.amount);
      }
    }
    return result;
  }
  
  @Override public String toString() {
    return "Tfsa room " + mainRoom + " past w/d:" + pastWithdrawalsRoom;
  }
  
  // PRIVATE 
  
  /** 
   The simple 'standard' yearly amount from the CRA.
   This amount doesn't depend on any actions you have taken in the past.
   This amount is used for all future years (which is a reasonable approximation).
  */
  private Money yearlyLimit;
  
  /**
   The current room available for contributions, but without taking into account 
   past withdrawals, which are handled as separate buckets attached to a year.
  */
  private Money mainRoom;
  
  private static final class Withdrawal {
    Withdrawal(Integer year, Money amount){
      this.year = year;
      this.amount = amount;
    }
    /** The year the withdrawal is made. */
    Integer year;
    Money amount;
    @Override public String toString() {
      return "year: " + year + " amt:" + amount;
    }
  }
  
  /**
   When you withdraw from a TFSA, that amount is added to your room. 
   But it's added in a special way: it's only added for future years, so it's not 
   immediately available when you make a withdrawal.
   
   Metaphor: a separate bucket of water is created for each withdrawal.
   These buckets add to your 'real' room, but only if the year-logic is satisfied. 
   
   The withdrawals are added in time-order.
   When a bucket is emptied, it's removed from the list.
  */
  private List<Withdrawal> pastWithdrawalsRoom = new ArrayList<Withdrawal>();
  
  private TfsaRoom(Money initialRoom, Money yearlyLimit) {
    this.mainRoom = initialRoom;
    this.yearlyLimit = yearlyLimit;
  }
  
  /** Progressive; if multiple contributions are made in a year, this figures out when the limit is exceeded. */
  private void reduceBecauseOfContribution(Money contribAmount, Integer year) {
    if (contribAmount.gt(roomFor(year))) {
      throw new IllegalArgumentException("TFSA contribution " + contribAmount + " exceeds room " + roomFor(year));
    }
    
    //first take away from withdrawalsRoom
    Money remainder = contribAmount; //reduce this to 0
    for (Withdrawal pastWithdrawal : pastWithdrawalsRoom) {
      if (year > pastWithdrawal.year) {
        if(remainder.lteq(pastWithdrawal.amount)) {
          //the bucket can eat up all of what's left
          pastWithdrawal.amount = pastWithdrawal.amount.minus(remainder);
          remainder = ZERO;
          break;
        }
        else {
          //the bucket can't eat up all of what's left
          remainder = remainder.minus(pastWithdrawal.amount);
          pastWithdrawal.amount = ZERO;
        }
      }
    }
    
    //cleanup: remove any empty withdrawal buckets; they are no longer useful
    pastWithdrawalsRoom.removeIf(withdrawal -> withdrawal.amount.isZero());
    
    //now take away the remainder from mainRoom
    mainRoom = mainRoom.minus(remainder);
    
    if (mainRoom.isMinus()) {
      //this shouldn't happen if I've coded this correctly; just being defensive here
      throw new IllegalArgumentException("TFSA contribution " + contribAmount + " exceeds room for year " + year);
    }
  }
}