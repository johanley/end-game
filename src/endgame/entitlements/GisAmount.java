package endgame.entitlements;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import endgame.model.Money;
import endgame.tax.FederalTaxReturn;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import endgame.util.Log;
import endgame.util.Util;

/**
 Guaranteed Income Supplement (GIS) amounts. 
 If the amount is not 0, then it is added on to your OAS payment.
 
 <P>GIS is intended mostly for low-income people.
 <P>The GIS amount (if any) is included in your {@link OasPayment}, and has the same 
 start date as OAS. This class is not a {@link Transactional}, but acts as a helper to ({@link OasPayment}), 
 which is a {@link Transactional}.
 GIS is tax-free, and has a separate bucket in the {@link FederalTaxReturn}.
 
 <P>Limitation: this implementation is currently for single people only (unmarried, no common-law spouse).
*/
public final class GisAmount {
  
  /** 
   Calculate your monthly GIS amount, if any, using numbers from last year's tax return. 
   @param netIncomeLastYear line 23600; excludes GIS, since GIS is tax-free. 
   @param oasLastYear excludes GIS
   @param employmentIncomeLastYear you can have a modest employment income without affecting GIS, but there's 
    an eventual clawback.
  */
  Money monthlyAmount(Money netIncomeLastYear, Money oasLastYear, Money employmentIncomeLastYear, Money exempt) {
    Money incomeForGis = netIncomeLastYear.minus(oasLastYear);
    Money result = tableLookup(incomeForGis);
    if (result.isPlus()) {
      result = result.minus(clawback(employmentIncomeLastYear, exempt));
      result = Util.nonNegative(result);
    }
    return result;
  }
  
  /** Called only once upon startup, to read the config file that specifies the GIS brackets. */
  public static void lookupGisBrackets(String projectRoot) {
    try {
      Path fileLocation = Path.of(projectRoot, "input", "gis", "gis-brackets.utf8");
      Log.log("Reading file " + fileLocation);
      List<String> lines = Util.read(fileLocation.toString());
      for (String line : lines) {
        BRACKETS.add(parseLine(line));
      }
      BRACKET_MAX = BRACKETS.get(BRACKETS.size()-1).max;
    }
    catch(IOException ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  // PRIVATE 
  
  private static final Double HALF = 0.5;
  
  private static List<Bracket> BRACKETS = new ArrayList<Bracket>();
  private static Money BRACKET_MAX;
  
  private static final class Bracket {
    Bracket(Money max, Money amount){
      this.max = max;
      this.amount = amount;
    }
    Money max;
    Money amount;
  }
  
  private Money tableLookup(Money incomeForGis) {
    Money result = Consts.ZERO;
    if (incomeForGis.lteq(BRACKET_MAX)) {
      for(Bracket bracket : BRACKETS) {
        if (incomeForGis.lteq(bracket.max)) {
          result = bracket.amount;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   You can have employment income up to {@link #EXEMPT}, with no penalty. 
   After that, it begins to be clawed back.  
   https://www.taxtips.ca/seniors/guaranteed-income-supplement.htm - see the detailed calculation. 
  */
  private Money clawback(Money employmentIncomeLastYear, Money exempt) {
    Money result = Consts.ZERO;
    if (employmentIncomeLastYear.lt(exempt)) {
      //do nothing - no clawback
    }
    else {
      Money lesser = lesser(employmentIncomeLastYear, exempt);
      Money exemption = exempt.plus(lesser);
      Money excess = employmentIncomeLastYear.minus(exemption);
      result = excess.times(HALF);     
    }
    return result;
  }
  
  private Money lesser(Money employmentIncomeLastYear, Money exempt) {
    Money excess = employmentIncomeLastYear.minus(exempt);
    Money halfExcess = excess.times(HALF);
    return halfExcess.lt(exempt) ? halfExcess : exempt;
  }
  
  /*
   Examples. First and last line of the file:
    0.00_23.99:935.72
    18,960.00_18,983.99:0.79
  */ 
  private static Bracket parseLine(String line) {
    String[] parts = line.split(Pattern.quote(":"));
    Money amount = new Money(noComma(parts[1]));
    String range = parts[0];
    String[] rangeParts = range.split(Pattern.quote("_"));
    Money max = new Money(noComma(rangeParts[1]));
    return new Bracket(max, amount);
  }
  
  private static String noComma(String amount) {
    return amount.replace(",", "");
  }
}