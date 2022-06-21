package endgame.account.tfsa;

import java.util.List;
import java.util.Optional;

import endgame.Scenario;
import endgame.account.Account;
import endgame.model.Money;
import endgame.security.stock.Stock;
import endgame.security.stock.StockPosition;
import endgame.transaction.Transactional;
import endgame.util.Consts;
import hirondelle.date4j.DateTime;

/** 
 Use up the current room in the TFSA by transferring stock in kind from other investment accounts.
 In the current implementation, your bank account is excluded.

 <P>WARNING: avoid executing this transaction on January 1, because that's the day the system recalculates your TFSA room for the year. 
 It's best to avoid the possibility of the code mis-timing the sequence of actions. 
*/
public final class TfsaTopUp extends Transactional {
  
  public TfsaTopUp(String when, List<Account> accountSequence, List<Stock> stockSequence) {
    super(when);
    this.accounts = accountSequence;
    this.stocks = stockSequence;
  }
  
  /* Similar to {@link LiquidateInSequence}.  */
  
  @Override protected void execute(DateTime when, Scenario sim) {
    Money tfsaRoom = sim.tfsaRoom.roomFor(when.getYear());
    logMe(when, "TFSA room for " + when.getYear() + " " + tfsaRoom);
    Money totalTransferredSoFar = Consts.ZERO;
    //unusual: need to exit from multiple loops; using a label to do that:
    transferShares:
    for(Account account : accounts) {
      for(Stock stock : stocks) {
        Optional<StockPosition> position = account.positionFor(stock);
        if (position.isPresent()) {
          Money remainingRoom = tfsaRoom.minus(totalTransferredSoFar);
          Transfer transfer = numSharesToTransfer(position.get(), remainingRoom);
          if (transfer.numShares > 0) { 
            transfer.account = account.getClass().getSimpleName();
            totalTransferredSoFar = totalTransferredSoFar.plus(transfer.value);
            
            account.transferSharesOut(transfer.numShares, stock, when);
            sim.tfsa.transferSharesIn(transfer.numShares, stock, when); //this adjusts the room 
            logMe(when, " Transfer from " + transfer.account + " " + transfer.value + " (" +  transfer.numShares + " shares) of "+ transfer.symbol + ". Room remaining " + sim.tfsaRoom.roomFor(when.getYear()));
            
            if (transfer.isPartial) {
              break transferShares; //exit the account loop
            }
          }
        }
      }
    }
  }

  @Override public String toString() {
    return "TOP-UP TFSA: ";
  }

  private List<Account> accounts;
  private List<Stock> stocks;
  
  private Transfer numSharesToTransfer(StockPosition position, Money remaining) {
    Transfer result = new Transfer();
    result.symbol = position.stock().symbol();
    Money value = position.marketValue();
    if (value.lteq(remaining)) {
      //all shares can be transferred
      result.isPartial = Boolean.FALSE;
      result.numShares = position.numShares();
      result.value = value;
    }
    else {
      //transfer a part of the position, but not all
      //if this is true, then this is the final transfer of shares this year, and 
      //no others should be transferred after this 
      result.isPartial = Boolean.TRUE;
      double price = position.stock().price().asDouble();
      result.numShares = remaining.flooredDiv(price);
      result.value = position.stock().price().times(result.numShares);
    }
    return result;
  }
}
