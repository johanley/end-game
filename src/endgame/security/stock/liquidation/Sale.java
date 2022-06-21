package endgame.security.stock.liquidation;

import endgame.model.Money;

/** Struct to carry the details of the sale of stock. */
public final class Sale {
  
  public Money gross;
  public Money proceeds;
  public String symbol;
  public String account;
  public Integer numShares;
  
  /** 
   True only if only part of the position in the account was sold, not all.
   Most liquidations will sell only part of a position. 
  */
  public Boolean isPartial;
  
  @Override public String toString() {
    return account + " sold " + numShares + " " + symbol + " net:" + proceeds; 
  }

}
