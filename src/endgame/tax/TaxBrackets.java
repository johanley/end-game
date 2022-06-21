package endgame.tax;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import endgame.model.Money;

/** The core tax calculation. */
public final class TaxBrackets {

  /** IMPORTANT: the brackets MUST be added in increasing order. */
  public void add(TaxBracket bracket) {
    brackets.add(bracket);
  }

  /** Return the tax owed corresponding to the given taxable income. */
  public Money taxFor(Money taxableIncome) {
    initializeBrackets();
    Money result = ZERO;
    if (taxableIncome.isPlus()) {
      for (TaxBracket tb : brackets) {
        if (taxableIncome.gt(tb.previousMax()) && taxableIncome.lteq(tb.max())) {
          result = tb.taxFor(taxableIncome);
          break;
        }
      }
    }
    return result;
  }
  
  public double lowestTaxRate() {
    initializeBrackets();
    return brackets.get(0).rate();
  }

  // PRIVATE 
  
  private List<TaxBracket> brackets = new ArrayList<TaxBracket>();
  private boolean hasInitializedAllBrackets;
  private static Money ZERO = new Money(new BigDecimal("0.00"));
  
  private void initializeBrackets() {
    if (!hasInitializedAllBrackets) {
      Money base = ZERO;
      Money previousMax = ZERO;
      for(TaxBracket tb : brackets) {
        tb.setBase(base); //the first bracket has a base of 0
        tb.setPreviousMax(previousMax);

        //items carried forward to the next tb
        Money tbFullAmount = tb.max().minus(tb.previousMax());
        base = base.plus(tbFullAmount.times(tb.rate())); 
        previousMax = tb.max();
      }
      hasInitializedAllBrackets = true;
    }
  } 
}