package endgame.model;

public final class MoneyRange {
  
  public static MoneyRange valueOf(String range) {
    return new MoneyRange(range);
  }
  
  public Money min() { return MIN; }
  public Money max() { return MAX; }
  public Money range() { return MAX.minus(MIN); }
  
  private Money MIN; 
  private Money MAX;
  
  private MoneyRange(String range) {
    String SEP = "_";
    String[] parts = range.split(SEP);
    this.MIN = new Money(parts[0]);
    this.MAX = new Money(parts[1]);
  }
}