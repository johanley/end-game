package endgame.security.gic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import endgame.model.Money;
import endgame.util.Consts;
import endgame.util.Util;
import hirondelle.date4j.DateTime;

/** 
 Non-callable and non-transferable GIC, held to maturity, with a fixed interest rate compounded annually.
 
 The GIC pays only upon maturity. See package-info for more information.
 
 <P>For multi-year GICs, the GIC has accrued interest each year, which is reported as taxable income; 
 but that amount is not actually distributed to the buyer until the GIC matures.
 
 <P>This class is immutable. A GIC doesn't change state over time.
*/
public final class GtdInvestmentCert {

  /** Used when the GIC is purchased. */
  static GtdInvestmentCert fromPurchaseDate(Money principal, String soldBy, Double interestRate, DateTime purchaseDate, Integer term) {
    DateTime redemptionDate = plusYears(purchaseDate, term);
    return new GtdInvestmentCert(principal, soldBy, interestRate, purchaseDate, redemptionDate, term);
  }

  /** Used when the GIC is stated as an inital holding in an account. */
  public static GtdInvestmentCert fromRedemptionDate(String principal, String soldBy, String interestRate, String redemptionDate, String term) {
    return fromRedemptionDate(new Money(principal), soldBy, Util.percentFrom(interestRate), new DateTime(redemptionDate), Integer.valueOf(term));
  }

  /** Used when the GIC is stated as an inital holding in an account. */
  static GtdInvestmentCert fromRedemptionDate(Money principal, String soldBy, Double interestRate, DateTime redemptionDate, Integer term) {
    DateTime purchaseDate = minusYears(redemptionDate, term);
    return new GtdInvestmentCert(principal, soldBy, interestRate, purchaseDate, redemptionDate, term);
  }
  
  /** The amount paid when the GIC was purchased. */
  public Money principal() { return principal; }
  
  /** The full interest paid to the buyer of the GIC, compounded annually. */
  public Money totalInterest() {
    return interestUpTo(term);
  }
  
  /** The principal and all interest. */
  public Money redemptionValue() {
    return principal().plus(totalInterest());
  }
  
  /** 
   The interest accrued for the given anniversary of the day of purchase, including the redemption date.
   The accrued interest is reported each year as taxable income, but it isn't actually distributed to 
   the buyer until the GIC matures!
  */
  public Money accruedInterestFor(DateTime anniversaryOfPurchase) {
    if (!anniversaryDates().contains(anniversaryOfPurchase)) {
      throw new RuntimeException("Date is not an accrual date / redemption date for the GIC : " + anniversaryOfPurchase + ". Anniversaries: " + anniversaryDates());
    }
    int yearNum = anniversaryOfPurchase.getYear() - purchaseDate.getYear();
    Money result = interestUpTo(yearNum).minus(interestUpTo(yearNum-1));
    return result;
  }
  
  /** Name of the bank or institution that sold the GIC. */
  public String soldBy() { return soldBy; }
  
  /** The interest rate of the GIC, compounded annually. Returns 0.05, not 5%. */
  public Double interestRate() { return interestRate; }
  
  /** When the buyer of the GIC receives their principal plus interest. */
  public DateTime maturityDate() { return redemptionDate; }
  
  /** When the GIC was bought. */
  public DateTime purchaseDate() { return purchaseDate; }
  
  /** The term of the GIC. */
  public Integer numYears() { return term; }
  
  /**
   The anniversary dates of the purchase date, for which an accrued interest transaction applies to that year's tax return.
   Includes the year of the redemption itself. 
   
   <P>Accrued interest applies only to GICs held in a non-reg account, and with terms greater than 1 year.
   If you hold such a GIC, then your tax return for the year "in the middle" has accrued interest 
   for that year, even though you have not yet received the interest (because the interest is paid upon redemption).
  */
  public List<DateTime> anniversaryDates(){
    List<DateTime> result = new ArrayList<>();
    for(int yr = 1; yr <= term; ++yr) {
      result.add(plusYears(purchaseDate, yr));
    }
    return result;
  }
  
  /** Excludes the principal and date of purchase. */
  public String shortName() {
    return soldBy + " GIC " + interestRate*100.0 + "% " + redemptionDate;
  }
  
  /** This string is used to identify the GIC, but the equals method should be the main way of identifying a GIC.  */
  @Override public String toString() {
    return soldBy + " " + term + "-year GIC, " + principal + " @ " + interestRate*100.0 + "% matures " + redemptionDate.format("YYYY-MM-DD") + ", purchased on " + purchaseDate;
  }
  
  /** 
    GICs lack an identifier (like a stock ticker symbol).
    The equals method serves in lieu of an id. 
    It uses all of the object's data. 
   */
  @Override public boolean equals(Object aThat) {
    if (this == aThat) return true;
    if (!(aThat instanceof GtdInvestmentCert)) return false;
    GtdInvestmentCert that = (GtdInvestmentCert)aThat;
    for(int i = 0; i < this.getSigFields().length; ++i){
      if (!Objects.equals(this.getSigFields()[i], that.getSigFields()[i])){
        return false;
      }
    }
    return true;    
  }
  
  @Override public int hashCode() {
    return Objects.hash(getSigFields());
  }
  
  // PRIVATE 
  private Money principal;
  private String soldBy;
  private Double interestRate;
  private DateTime purchaseDate;
  private Integer term;
  private DateTime redemptionDate;

  private GtdInvestmentCert(Money principal, String soldBy, Double interestRate, DateTime purchaseDate, DateTime redemptionDate, Integer term) {
    this.principal = principal;
    this.soldBy = soldBy;
    this.interestRate = interestRate;
    this.purchaseDate = purchaseDate;
    this.term = term;
    this.redemptionDate = redemptionDate;
    checkForInvalidArgs();
  }
  
  private void checkForInvalidArgs(){
    if (principal.lt(Consts.ZERO)) {
      throw new IllegalArgumentException("Principal is negative: " + this);
    }
    if (interestRate < 0.0) {
      throw new IllegalArgumentException("Interest rate is negative: " + this);
    }
    if (term < 1 || term > 20) {
      throw new IllegalArgumentException("The term of the GIC is not in the range 1..20 years: " + this);
    }
  }
  
  private static DateTime plusYears(DateTime date, int numYears) {
    DateTime result = date.plus(numYears, 0, 0, 0, 0, 0, 0, DateTime.DayOverflow.Spillover);
    return DateTime.forDateOnly(result.getYear(), result.getMonth(), result.getDay());
  }
  
  private static DateTime minusYears(DateTime date, int numYears) {
    DateTime result = date.minus(numYears, 0, 0, 0, 0, 0, 0, DateTime.DayOverflow.Spillover);
    return DateTime.forDateOnly(result.getYear(), result.getMonth(), result.getDay());
  }

  /** For 0 years, returns 0 interest. */
  private Money interestUpTo(int year /*0..numYears*/) {
    if (year > term || year < 0) {
      throw new IllegalArgumentException("The year is not in the range 0.." + term + ": " + year);
    }
    double factor = Math.pow(1.0 + interestRate, year); 
    return principal.times(factor).minus(principal);
  }
  
  private Object[] getSigFields() {
    Object[] result = {
      soldBy, principal, interestRate, purchaseDate, redemptionDate, term
    };
    return result;
  }
}
