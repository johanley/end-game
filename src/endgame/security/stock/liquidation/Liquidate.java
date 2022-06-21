package endgame.security.stock.liquidation;

import java.util.List;

import endgame.Scenario;
import hirondelle.date4j.DateTime;

/** 
 Policy for selling stocks during retirement, to generate cash flow.
 
 <P>The main idea is to generate cash flow from assets.
 The main questions to answer are: 
 <ul>
  <li>which account should be used?
  <li>which stock should be sold?
  <li>how much should be sold, if any?
 </ul> 
 It may be the case that multiple stocks/accounts may be involved, in a given year.
*/
public interface Liquidate {
 
  /**
   Sell stock(s) in your account(s) on the given date, and return the details of all sales.
   An implementation may decide not to sell any stocks at all that year (in 
   which case the returned list is empty.) 
  */
  public List<Sale> sellStock(DateTime when, Scenario sim);

}
