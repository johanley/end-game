/** 
 Basic provincial (and territorial) tax returns.
 
 <P>Please test versus a real tax return! Example: {@link NBBasicTest}.

 <P>Some jurisdictions are quite similar in their tax logic.
 It's important to note that all 13 jurisdictions (10 provinces and 3 territories) are supported in this package.
 Jurisdictions without their own explicit class in this package  (MB, SK, AB, YT, NT, and NU) are implemented 
 using existing classes, because of the similarity of their tax logic.

 <P>Only the main elements of tax returns are implemented here.
 It is hoped that they will provide a reasonably accurate simulation of your finances.
 Examples of excluded items: logic for spouses, dependents, charitable donations,
 medical expenses, and minimum tax.
 
 <P>Please examine the code for your jurisdiction (as stated in the implementation of {@link ProvincialTaxFields}), 
 in order to see if it adequately reflects your own situation. If you wish the tax calculation to 
 be more realistic, you'll need to edit the code.
 
 <P>Ref: <a href='https://www.canada.ca/en/revenue-agency/services/forms-publications/tax-packages-years/general-income-tax-benefit-package.html'>link to forms</a>.
*/
package endgame.tax.provincial;