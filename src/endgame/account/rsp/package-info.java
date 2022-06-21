/** 
 Retirement Savings Plan (highly simplified: no contributions allowed).
 
 This package exists only to document how RSPs are treated here.
 It has no classes.
 
 <P>This implementation of RSPs is greatly simplified.
 
 <P>
 The amount of contribution room for RSPs is a certain percentage of what is called <em>earned income</em>, 
 which comes from employment, rental income, and other items.
 But this program is not about the earning phase of life, it's about the spending phase.
 The adopted policy here is that your earned income in retirement will be either zero or small, 
 such that contributions to the RSP can be neglected - withdrawals only, and no contribution.
 
 <P>In other words, here <b>an RSP is simply modeled as a RIF, whose minimum withdrawals are not enforced until after the RSP-to-RIF conversion date</b>. 
 
 <P>Internally, there is only a single account - a RIF account object.
 That object has a conversion date, the date on which the RSP is converted (fully) to a RIF.
 It's that date which controls whether or not RIF minimum withdrawals are enforced or not.
 Other than that, there's no distinction between a RIF and and RSP.
 
 <P>In your scenario file, the account is always referred to a "the rif account", even 
 during time periods when minimums are not being enforced. 
 I realize this is confusing. It's also a safer implementation: it reduces/eliminates bugs 
 that might occur when converting an RSP to a RIF - especially bugs regarding transactions.
 
 <P>In the scenario file, the account is always referred to as 'rif', even if it 
 starts out, in the real world, as an RSP towards which no contributions are being made.
*/
package endgame.account.rsp;