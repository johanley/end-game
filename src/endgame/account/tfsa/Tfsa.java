package endgame.account.tfsa;

import java.util.Set;

import endgame.account.Account;
import endgame.model.Money;
import endgame.security.gic.GtdInvestmentCert;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import hirondelle.date4j.DateTime;

/**
  The TFSA is the simplest form of account.
  
  It has near-zero tax consequences, and permits all types of transactions.
  
  <P>The only complexity is calculating the 'room', the limit to the amount you can 
  contribute (cash or in-kind) to the account in a given year. 
*/
public final class Tfsa extends Account {

  public static Tfsa valueOf(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, TfsaRoom tfsaRoom) {
    return new Tfsa(cash, stocks, gics, tfsaRoom);
  }
  /** DELETE ME. */
  public static Tfsa valueOf(String cash, Set<StockPosition> stocks, TfsaRoom tfsaRoom) {
    return new Tfsa(cash, stocks, null, tfsaRoom);
  }

  @Override public String toString() {
    return "TFSA {cash:" + cash + " stocks:" + stockPositions + "}";  
  }

  @Override public void depositCash(Money amount, DateTime when) {
    super.depositCash(amount, when);
    
    tfsaRoom.reduceFromContribution(amount, when.getYear());
  }
  
  @Override public Money withdrawCash(Money amount, DateTime when) {
    Money result = super.withdrawCash(amount, when);
    
    tfsaRoom.increaseFromWithdrawal(amount, when.getYear());
    return result;
  }
  
  @Override public Integer transferSharesIn(Integer numShares, Stock stock, DateTime when) {
    Integer result = super.transferSharesIn(numShares, stock, when);
    
    Money amount = stock.price().times(numShares);
    tfsaRoom.reduceFromContribution(amount, when.getYear());
    return result;
  }
  
  @Override public Integer transferSharesOut(Integer numShares, Stock stock, DateTime when) {
    Integer result = super.transferSharesOut(numShares, stock, when);
    
    Money amount = stock.price().times(numShares);
    tfsaRoom.increaseFromWithdrawal(amount, when.getYear());
    return result;
  }
  
  // PRIVATE 
  
  private Tfsa(String cash, Set<StockPosition> stocks, Set<GtdInvestmentCert> gics, TfsaRoom tfsaRoom) {
    super(cash, stocks, gics);
    this.tfsaRoom = tfsaRoom;
  }
  
  private TfsaRoom tfsaRoom;
}
