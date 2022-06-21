package endgame.tax;

import static endgame.util.Consts.*;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import endgame.model.Money;
import endgame.util.Log;

/**
 Offset capital gains with capital losses, in order to reduce capital gains tax.
 
 This object is long-lived, lasts over multiple years, and stores all gains and losses until death 
 (or the end of the run).
 
 The metaphor is that of two buckets, one for gains and one for losses.
 The losses can offset a corresponding gain.
 A loss can be applied to a gain that is up to 3 years old.
 There's no time limit or expiry on losses. 
 
 <P>Transfer-out of stocks from a NRA (non-registered account) can result in a gain or loss.
 If it's a gain, it's treated in the usual way.
 If it's a loss, it's treated as a "superficial loss", not as a regular loss, and can't be used to offset a gain.
 
 <P>See schedule 3 of the return.
*/
public final class CapitalGainLoss {
  
  /** Each gain or loss is recorded by calling this method. */
  public void addGainOrLoss(Integer year, Money amount) {
    if (amount.isPlus()) {
      Log.log("Capital gain " + amount + " in " + year);
      gains.add(new GainLoss(year, amount, Type.GAIN));
    }
    else if (amount.isMinus()){
      Log.log("Capital loss " + amount + " in " + year);
      losses.add(new GainLoss(year, amount, Type.LOSS));
    }
    else {
      //do nothing if the amount is 0
    }
  }
  
  /**
   Offset gains with losses. 
   Must be called when calculating a tax return (and only then).
   Return the remaining capital gain after offsetting losses have been applied. 
  */
  public Money gainAfterOffsetsApplied(Integer currentYear) {
    Money result = ZERO;
    List<GainLoss> offsetableGains = offsetable(gains, currentYear);
    if (totalOffsetableAmt(offsetableGains).isPlus()) {
      result = gainAfterOffsettingLosses(offsetableGains, currentYear);
    }
    return result;
  }
  
  @Override public String toString() {
    return "Capital gains:" + gains + " losses:" + losses;
  }

  // PRIVATE 
  
  /** Gains can only be offset if they are not too old. Losses have no time limit. */
  private enum Type {
    GAIN(3), 
    LOSS(100000); // hacky: just a large number of years that will never be seen in practice */
    Integer numYears(){ return ageLimit; }
    Type(Integer ageLimit) {
      this.ageLimit = ageLimit;
    }
    private Integer ageLimit;
  }
  
  private static class GainLoss {
    GainLoss(Integer year, Money originalAmount, Type type){
      this.year = year;
      this.originalAmount = originalAmount;
      this.offsetableAmount = originalAmount;
      this.type = type;
    }
    
    /** 
     An offset can be applied only once to a gain/loss. 
     After that, the amount is no longer available for offsetting.
     
     Returns the remaining amount, that could not be offset by this amount. 
    */
    Money offsetBy(Money amount) {
      Money result = null;
      if (amount.gt(this.offsetableAmount)) {
        //the amount is bigger than this amount; not all of the amount can be offset
        result = amount.minus(this.offsetableAmount);
        this.offsetableAmount = ZERO;
      }
      else {
        //the amount is smaller than (or equal to) this amount; all of it can be offset
        this.offsetableAmount = this.offsetableAmount.minus(amount);
        result = ZERO;
      }
      return result;
    }
    
    Integer year;
    
    /** The original amount of the gain/loss. */
    Money originalAmount;
    
    /** 
     The whole or part of the original amount which is still available for offsetting.
     Starts off as the originalAmount, and usually reduces to 0 over time.
     Once it reaches zero, this gain/loss can no longer be used for offsetting.  
    */
    Money offsetableAmount;
    
    Type type;
    
    @Override public String toString() {
      return type + " year:" + year + " amt:" + originalAmount + " offsetableAmt:" + offsetableAmount; 
    }
  }
  
  private List<GainLoss> gains = new ArrayList<GainLoss>();
  private List<GainLoss> losses = new ArrayList<GainLoss>();

  private List<GainLoss> offsetable(List<GainLoss> list, Integer currentYear) {
    Predicate<GainLoss> filter =  gl -> 
      gl.offsetableAmount.isPlus() && 
      (currentYear - gl.year < gl.type.numYears()) 
    ;
    return list.stream().filter(filter).collect(toList());
  }
  
  private Money totalOffsetableAmt(List<GainLoss> list) {
    Money result = ZERO;
    for(GainLoss gl : list) {
      result = result.plus(gl.offsetableAmount);
    }
    return result;
  }
  
  private Money gainAfterOffsettingLosses(List<GainLoss> gs, Integer currentYear) {
    List<GainLoss> ls = offsetable(losses, currentYear);
    
    //at this level, gains and losses (gs and ls) have non-zero offset amounts that are ready to be matched up
    boolean losersAreBiggest = totalOffsetableAmt(ls).gt(totalOffsetableAmt(gs));
    //'a' is always a bigger bucket than 'b'
    List<GainLoss> as = losersAreBiggest ? ls : gs;
    List<GainLoss> bs = losersAreBiggest ? gs : ls;
    
    //all of 'b' gets used up
    Money useUpTotal = totalOffsetableAmt(bs);
    bs.forEach(b -> b.offsetableAmount = ZERO);
    
    //while only part of 'a' gets used up (usually)
    Money remainder = useUpTotal;
    for (GainLoss a : as) {
      remainder = a.offsetBy(remainder);
      if (!remainder.isPlus()) {
        break;
      }
    }
    return totalOffsetableAmt(gs);
  }
}