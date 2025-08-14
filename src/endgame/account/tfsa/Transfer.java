package endgame.account.tfsa;

import endgame.model.Money;

/** Struct to carry the details of the transfer of stock into the TFSA. */
public final class Transfer {
  
  public String account;
  public Money value;
  public String symbol;
  public Integer numShares;
  
  /** 
   True only if only part of the position in the account was transferred, not all.
   Most top-ups will transfer only part of a position. 
  */
  public Boolean isPartial;
  
  @Override public String toString() {
    return account + "Transfer to TFSA from " + account + " " + numShares + " " + symbol + " value: " + value; 
  }

}
