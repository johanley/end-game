package endgame.util;

import java.util.Formatter;

import endgame.model.Money;

/** Right-justified, with a comma, and two decimal places. */
public final class MoneyFormatter {
  
  public String format(Money money) {
    Formatter formatter = new Formatter(new StringBuilder());
    formatter.format(MONEY_FORMAT, money.getAmount());
    String result = formatter.toString();
    formatter.close();
    return result;
  }
  
  /** Right-justified, with a comma, and two decimal places. */
  private static final String MONEY_FORMAT = "%,13.2f";

}
