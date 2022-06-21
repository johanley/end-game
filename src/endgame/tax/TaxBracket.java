package endgame.tax;

import endgame.model.Money;

/** Tax bracket for the core calculation of income tax. */
public final class TaxBracket {
  
  /**
   Factory method.
   @param rate for example '15.0%'
   @param max the maximum taxable income corresponding to the tax bracket.
  */
  public static TaxBracket valueOf(String rate, String max) {
    String withoutPercentSign = rate.substring(0, rate.length()-1);
    return new TaxBracket(Double.valueOf(withoutPercentSign)/100.00D, new Money(max));
  }
  
  /** Line 67. */
  public double rate() { return rate; }
  /** Just before line 64. */
  public Money max() { return max; }
  
  /**
   Line 65.  
   Base amount from max-ing the previous bracket
   IMPORTANT: this is set after calling the constructor because it's dependent on the other (lower) brackets, in sequence. 
  */
  public Money base() { return base; }
  public void setBase(Money base) { this.base = base; }
  
  /** The max of the previous (lower) tax bracket. */
  public Money previousMax() { return previousMax; }
  public void setPreviousMax(Money previousMax) { this.previousMax = previousMax; }

  /** Return the tax corresponding to the given taxable income. */
  public Money taxFor(Money taxableIncome) {
    Money newAmount = taxableIncome.minus(previousMax);
    Money newTax = newAmount.times(rate);
    return base.plus(newTax);
  }
  
  // PRIVATE 
  
  private double rate;
  private Money max;
  
  private Money base;
  private Money previousMax;
  
  private TaxBracket(double rate, Money max) {
    this.rate = rate;
    this.max = max;
  }
}