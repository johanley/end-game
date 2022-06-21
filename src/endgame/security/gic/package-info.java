/** 
 Guaranteed Investment Certificates (GICs).
 
 <P>In this (simplified) implementation, GICs:
 <ul> 
  <li>are held to its redemption date, in the same account it was purchased in
  <li>have a term of 1..N years
  <li>have a fixed interest rate, compounded annually
  <li>pay only at the end of term, interest plus principal
  <li>have interest accrued yearly on your tax bill, even if you have not yet received the funds
 <ul>
 
 <P>I believe this is the most common case.
 There are many possible variations on GICs, but I'm currently sticking with the above, at least for the moment.
 
 <P>If you hold the GIC in a NRA (non-registered account), you'll pay tax yearly on the accrued amount for that year.
 That's a bit wacky: you pay tax on money you haven't actually received yet.
 
 <P>Ref:
 https://www.canada.ca/en/revenue-agency/services/tax/individuals/topics/about-your-tax-return/tax-return/completing-a-tax-return/personal-income/line-12100-interest-other-investment-income/line-12100-bank-accounts-term-deposits-guaranteed-income-certificates-gics-other-similar-investments.html
 https://www.ratehub.ca/gics/what-is-a-gic
*/
package endgame.security.gic;