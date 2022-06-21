package endgame.estate;

import endgame.Scenario;
import endgame.transaction.Transactional;
import hirondelle.date4j.DateTime;

public final class TerminalTaxReturn extends Transactional {
  
  public TerminalTaxReturn(String when) {
    super(when);
  }

  @Override protected void execute(DateTime when, Scenario sim) {
    /*
     * 
     * terminal-return {
     *   executor-fee 5000.00 | 2.5%
     *   account-sequence nra, rif, tfsa  # does this actually not matter?
     *   rif left to spouse?
     *   "fees"?
     * }
     * 
     * https://www.nbc.ca/personal/advice/succession/canada-estate-taxes.html
     * https://www.canada.ca/en/revenue-agency/services/tax/individuals/life-events/what-when-someone-died/final-return.html
     * 
     * most capital: deemed disposition; most nra securities, rental properties
     *   exception: primary residence; roll-over to spouse
     * rsp/rif: deemed withdrawal; full amount is taxable
     *   exception: can be rolled over to spouse or beneficiary
     *   
     *   income bucket: RIF, cpp, oas, pension 
     *   cap gain bucket: cottage, non-reg account
     *   tax-free bucket: tfsa, home, life insurance
     *   
     * cpp pays a death benefit ($2500?)
     * 
     * "estate administrative tax (EAT)" about 1.5% of value of estate in the will
     * 
     *  naming beneficiaries can be very useful for avoiding taxes; can do it even on some non-reg items (which ones?)
     *  
     *  life insurance policies can pay a big fraction of the estate tax bill; you pay a little up front, and get a big pay out upon death
     * 
     * calculate the bill for the estate
     * CONFIG: trustee/executor may charge flat fee or percentage of net worth
     * compute the final tax return
     *   simple: die on Dec 31 only; no time between death and the terminating transactions (instead of a few months)
     *   tfsa: no taxes
     *   nra: deemed disposition on capital; tax on capital gains; this is being paid anyway, no matter what is sold
     *   rif: deemed disposition; tax on whole value as income; this is all being paid anyway, no matter what is sold
     *   rif: if given to spouse, no extra taxes
     *   compute the tax payable
     *   simple case: if left to spouse, then usually no extra taxes on the estate
     *   warning: the estate has NO personal items: basic personal amount, age amount
     *   in the terminal return, any outstanding cap losses go further, and reduce your actual income
     *   the terminal return needs to have its own implementation, for both feds and provinces
     *       can I subclass the existing, and set some data to fixed values?
     * 
     * all capital undergoes deemed disposition for tax purposes; but tax can still be avoided sometimes 
     * no cap gain: primary residence, GICs, bonds, cash, TFSA
     * 
     * convert GICs to principal amounts, as cash; no redemption of outstanding GICs
     * 
     * sweep investment accounts to the bank
     * 
     * calc remaining cash needed to pay for taxes and estate fees
     * sell stocks to make up the remainder
     * CONFIG: the order of the accounts
     * (it makes sense to use rif, nra first, since they are already paying taxes as if its all sold already; 
     *   likely the nra first, since tax is only on half the cap gain.)
     * in each account, sell an equal value of each stock
     *   there's no tax consequences for these sales, so that might need to be backed out; OR, maybe there's a way around that
     *      there's no need to 
     *   be careful with the 'quantization' for the integral number of shares   
     * sweep cash to the bank again
     * bank pays the full bill
     * THE END. The estate residue is logged.
     * 
     */
  }
  
  
}
