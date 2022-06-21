package endgame.security.stock;

import java.math.BigDecimal;

import endgame.model.Money;
import endgame.util.Util;

/** Eligible dividend for a stock. */
public final class Dividend {

  public static Dividend valueOf(String amount, String when, String growth) {
    return new Dividend(amount, when, growth);
  }
  
  public Money getAmount() { return amount; }
  public String getDates(){ return when; }
  public Double getGrowth() { return growth; }
  
  public void stockSplit(Integer factor) {
    amount = amount.divByInt(factor);
  }
  
  @Override public String toString() {
    return "DVD {amount:" + amount + " dates:" + when + " growth:" + growth + "}";  
  }

  //PRIVATE 
  
  private Money amount;
  private String when = "";
  private Double growth;
  
  private Dividend(String amount, String when, String growth) {
    this.amount = new Money(new BigDecimal(amount));
    this.when = when;
    this.growth = Double.valueOf(Util.chopPercent(growth))/100.0D;
  }
}
