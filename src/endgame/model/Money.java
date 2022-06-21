package endgame.model;

import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import static java.math.BigDecimal.ZERO;
import java.math.RoundingMode;

/**
* Represent an amount of money in any currency.
*
* <P>This class assumes <em>decimal currency</em>, without funky divisions 
* like 1/5 and so on. <code>Money</code> objects are immutable. Like {@link BigDecimal}, 
* many operations return new <code>Money</code> objects. In addition, most operations 
* involving more than one <code>Money</code> object will throw a 
* <code>MismatchedCurrencyException</code> if the currencies don't match.
* 
* <h2>Decimal Places and Scale</h2>
* Monetary amounts can be stored in the database in various ways. Let's take the 
* example of dollars. It may appear in the database in the following ways :
* <ul>
*  <li>as <code>123456.78</code>, with the usual number of decimal places 
*    associated with that currency.
*  <li>as <code>123456</code>, without any decimal places at all.
*  <li>as <code>123</code>, in units of thousands of dollars.
*  <li>in some other unit, such as millions or billions of dollars.
* </ul>
* 
* <P>The number of decimal places or style of units is referred to as the 
* <em>scale</em> by {@link java.math.BigDecimal}. This class's constructors 
* take a <code>BigDecimal</code>, so you need to understand its idea of scale.
*  
* <P>The scale can be negative. Using the above examples :
* <table border='1' cellspacing='0' cellpadding='3'>
*  <tr><th>Number</th><th>Scale</th></tr>
*  <tr><td>123456.78</th><th>2</th></tr>
*  <tr><td>123456</th><th>0</th></tr>
*  <tr><td>123 (thousands)</th><th>-3</th></tr>
* </table>
* 
* <P>Note that scale and rounding are two separate issues.
* In addition, rounding is only necessary for multiplication and division operations.
* It doesn't apply to addition and subtraction.
* 
* <h2>Operations and Scale</h2>
* <P>Operations can be performed on items having <em>different scale</em>. 
* For example, these  operations are valid (using an <em>ad hoc</em> 
* symbolic notation): 
* <PRE>
* 10.plus(1.23) => 11.23
* 10.minus(1.23) => 8.77
* 10.gt(1.23) => true
* 10.eq(10.00) => true
* </PRE> 
* This corresponds to typical user expectations. 
* An important exception to this rule is that {@link #equals(Object)} is sensitive 
* to scale (while {@link #eq(Money)} is not). That is,  
* <PRE>
*   10.equals(10.00) => false
* </PRE>
*   
* <h2>Multiplication, Division and Extra Decimal Places</h2>
* <P>Operations involving multiplication and division are different, since the result
* can have a scale which exceeds that expected for the given currency. For example 
* <PRE>($10.00).times(0.1256) => $1.256</PRE>
* which has more than two decimals. In such cases, <em>this class will always round 
* to the expected number of decimal places for that currency.</em> 
* This is the simplest policy, and likely conforms to the expectations of most 
* end users.
* 
* <P>This class takes either an <code>int</code> or a {@link BigDecimal} for its 
* multiplication and division methods. It doesn't take <code>float</code> or 
* <code>double</code> for those methods, since those types don't interact well with
* <code>BigDecimal</code>. Instead, the <code>BigDecimal</code> class must be used when the 
* factor or divisor is a non-integer.
*  
* <P><em>The {@link #init(Currency, RoundingMode)} method must be called at least 
* once before using the other members of this class.</em> It establishes your 
* desired defaults. Typically, it will be called once (and only once) upon startup.
*  
* <P>Various methods in this class have unusually terse names, such as 
* {@link #lt} and {@link #gt}. The intent is that such names will improve the 
* legibility of mathematical expressions. Example : 
* <PRE> if (amount.lt(hundred)) {
*     cost = amount.times(price); 
*  }</PRE>
*/
public final class Money implements Comparable<Money>, Serializable {
  
  /**
  * Thrown when a set of <code>Money</code> objects do not have matching currencies. 
  * 
  * <P>For example, adding together Euros and Dollars does not make any sense.
  */
  public static final class MismatchedCurrencyException extends RuntimeException { 
    MismatchedCurrencyException(String message){
      super(message);
    }
  }
  
  /**
  * Set default values for currency and rounding style.
  * 
  * <em>Your application must call this method upon startup</em>.
  * This method should usually be called only once (upon startup).
  * 
  * <P>The recommended rounding style is {@link RoundingMode#HALF_EVEN}, also called 
  * <em>banker's rounding</em>; this rounding style introduces the least bias.
  * 
  * <P>Setting these defaults allow you to use the more terse constructors of this class, 
  * which are much more convenient.
  *  
  * <P>(In a servlet environment, each app has its own classloader. Calling this 
  * method in one app will never affect the operation of a second app running in the same 
  * servlet container. They are independent.)
  */
  public static void init(Currency defaultCurrency, RoundingMode defaultRounding){
    DEFAULT_CURRENCY = defaultCurrency;
    DEFAULT_ROUNDING = defaultRounding;
  }
  
  /**
  * Full constructor.
  * 
  * @param amount is required, can be positive or negative. The number of 
  * decimals in the amount cannot <em>exceed</em> the maximum number of 
  * decimals for the given {@link Currency}. It's possible to create a 
  * <code>Money</code> object in terms of 'thousands of dollars', for instance. 
  * Such an amount would have a scale of -3.
  * @param currency is required.
  * @param roundingStyle is required, must match a rounding style used by 
  * {@link BigDecimal}.
  */
  public Money(BigDecimal amount, Currency currency, RoundingMode roundingStyle){
    this.amount = amount;
    this.currency = currency;
    this.rounding = roundingStyle;
    validateState();
  }
  
  /**
  * Constructor taking only the money amount. 
  * 
  * <P>The currency and rounding style both take default values.
  * @param amount is required, can be positive or negative.
  */
  public Money(BigDecimal amount){
    this(amount, DEFAULT_CURRENCY, DEFAULT_ROUNDING);
  }
  
  public Money(String amount) {
    this(new BigDecimal(amount), DEFAULT_CURRENCY, DEFAULT_ROUNDING);
  }
  
  /**
  * Constructor taking the money amount and currency. 
  * 
  * <P>The rounding style takes a default value.
  * @param amount is required, can be positive or negative.
  * @param currency is required.
  */
  public Money(BigDecimal amount, Currency currency){
    this(amount, currency, DEFAULT_ROUNDING);
  }
  
  /** Return the amount passed to the constructor. */
  public BigDecimal getAmount() { return amount; }
  
  /** Return the currency passed to the constructor, or the default currency. */
  public Currency getCurrency() { return currency; }
  
  /** Return the rounding style passed to the constructor, or the default rounding style. */
  public RoundingMode getRoundingStyle() { return rounding; }
  
  /**
  * Return <code>true</code> only if <code>that</code> <code>Money</code> has the same currency 
  * as this <code>Money</code>.
  */
  public boolean isSameCurrencyAs(Money that){
    boolean result = false;
     if ( that != null ) { 
       result = this.currency.equals(that.currency);
     }
     return result; 
  }
  
  /** Return <code>true</code> only if the amount is positive. */
  public boolean isPlus(){
    return amount.compareTo(ZERO) > 0;
  }
  
  /** Return <code>true</code> only if the amount is negative. */
  public boolean isMinus(){
    return amount.compareTo(ZERO) <  0;
  }
  
  /** Return <code>true</code> only if the amount is zero. */
  public boolean isZero(){
    return amount.compareTo(ZERO) ==  0;
  }
  
  /** 
  * Add <code>that</code> <code>Money</code> to this <code>Money</code>.
  * Currencies must match.  
  */
  public Money plus(Money that){
    checkCurrenciesMatch(that);
    return new Money(amount.add(that.amount), currency, rounding);
  }

  /** 
  * Subtract <code>that</code> <code>Money</code> from this <code>Money</code>. 
  * Currencies must match.  
  */
  public Money minus(Money that){
    checkCurrenciesMatch(that);
    return new Money(amount.subtract(that.amount), currency, rounding);
  }

  /**
  * Sum a collection of <code>Money</code> objects.
  * Currencies must match. You are encouraged to use database summary functions 
  * whenever possible, instead of this method. 
  * 
  * @param moneys collection of <code>Money</code> objects, all of the same currency.
  * If the collection is empty, then a zero value is returned.
  * @param currencyIfEmpty is used only when <code>moneys</code> is empty; that way, this 
  * method can return a zero amount in the desired currency.
  */
  public static Money sum(Collection<Money> moneys, Currency currencyIfEmpty){
    Money sum = new Money(ZERO, currencyIfEmpty);
    for(Money money : moneys){
      sum = sum.plus(money);
    }
    return sum;
  }
  
  /** 
  * Equals (insensitive to scale).
  * 
  * <P>Return <code>true</code> only if the amounts are equal.
  * Currencies must match. 
  * This method is <em>not</em> synonymous with the <code>equals</code> method.
  */
  public boolean eq(Money that) {
    checkCurrenciesMatch(that);
    return compareAmount(that) == 0;
  }

  /** 
  * Greater than.
  * 
  * <P>Return <code>true</code> only if  'this' amount is greater than
  * 'that' amount. Currencies must match. 
  */
  public boolean gt(Money that) { 
    checkCurrenciesMatch(that);
    return compareAmount(that) > 0;  
  }
  
  /** 
  * Greater than or equal to.
  * 
  * <P>Return <code>true</code> only if 'this' amount is 
  * greater than or equal to 'that' amount. Currencies must match. 
  */
  public boolean gteq(Money that) { 
    checkCurrenciesMatch(that);
    return compareAmount(that) >= 0;  
  }
  
  /** 
  * Less than.
  * 
  * <P>Return <code>true</code> only if 'this' amount is less than
  * 'that' amount. Currencies must match. 
  */
  public boolean lt(Money that) { 
    checkCurrenciesMatch(that);
    return compareAmount(that) < 0;  
  }
  
  /** 
  * Less than or equal to.
  * 
  * <P>Return <code>true</code> only if 'this' amount is less than or equal to
  * 'that' amount. Currencies must match.  
  */
  public boolean lteq(Money that) { 
    checkCurrenciesMatch(that);
    return compareAmount(that) <= 0;  
  }
  
  /**
  * Multiply this <code>Money</code> by an integral factor.
  * 
  * The scale of the returned <code>Money</code> is equal to the scale of 'this' 
  * <code>Money</code>.
  */
  public Money times(int aFactor){  
    BigDecimal factor = new BigDecimal(aFactor);
    BigDecimal newAmount = amount.multiply(factor);
    return new Money(newAmount, currency, rounding);
  }
  
  /**
  * Multiply this <code>Money</code> by an non-integral factor (having a decimal point).
  * Fixed scale, according to currency.
  */
  public Money times(double factor){
    if (factor % 1 == 0) {
      //it's really integral! avoid setting the scale explicitly
      //this was added because of dividends with fractional cents
      return times((int)factor);
    }
    
    BigDecimal newAmount = amount.multiply(asBigDecimal(factor));
    newAmount = newAmount.setScale(getNumDecimalsForCurrency(), rounding);
    return new Money(newAmount, currency, rounding);
  }
  
  /**
  * Divide this <code>Money</code> by an integral divisor (often a number of years).
  * 
  * <P>The scale of the returned <code>Money</code> is equal to the scale of 
  * 'this' <code>Money</code>. 
  */
  public Money divByInt(int aDivisor){
    BigDecimal divisor = new BigDecimal(aDivisor);
    BigDecimal newAmount = amount.divide(divisor, rounding);
    return new Money(newAmount, currency, rounding);
  }

  /*
   PROBLEMS WITH ROUNDING AND NUMBER OF DECIMALS; use flooredDiv instead.
  public Money div(double divisor){  
    BigDecimal newAmount = amount.divide(asBigDecimal(divisor), rounding);
    return new Money(newAmount, currency, rounding);
  }
  */
  
  /** The amount as a double; useful especially for dividing by a price. */
  public Double asDouble() {
    return amount.doubleValue();
  }
  
  /** Floor the result, and return as an Integer.*/
  public Integer flooredDiv(double divisor) {
    Double value = Math.floor(amount.doubleValue() / divisor);
    return value.intValue();
  }

  /** Return the absolute value of the amount. */
  public Money abs(){
    return isPlus() ? this : times(-1);
  }
  
  /** Return the amount x (-1). */
  public Money negate(){ 
    return times(-1); 
  }
  
  /**
  * Returns 
  * {@link #getAmount()}.getPlainString() + space + {@link #getCurrency()}.getSymbol().
  * 
  * <P>The return value uses the runtime's <em>default locale</em>, and will not 
  * always be suitable for display to an end user.
  */
  public String toString(){
    return amount.toPlainString();
  }
  
  /**
  * Like {@link BigDecimal#equals(java.lang.Object)}, this <code>equals</code> method 
  * is also sensitive to scale.
  * 
  * For example, <code>10</code> is <em>not</em> equal to <code>10.00</code>
  * The {@link #eq(Money)} method, on the other hand, is <em>not</em> 
  * sensitive to scale.
  */
  public boolean equals(Object aThat){
    if (this == aThat) return true;
    if (! (aThat instanceof Money) ) return false;
    Money that = (Money)aThat;
    for(int i = 0; i < this.getSigFields().length; ++i){
      if (!Objects.equals(this.getSigFields()[i], that.getSigFields()[i])){
        return false;
      }
    }
    return true;    
  }
  
  public int hashCode(){
    return Objects.hash(getSigFields());
  }
  
  public int compareTo(Money that) {
    final int EQUAL = 0;
    
    if ( this == that ) return EQUAL;

    //the object fields are never null 
    int comparison = this.amount.compareTo(that.amount);
    if ( comparison != EQUAL ) return comparison;

    comparison = this.currency.getCurrencyCode().compareTo(
      that.currency.getCurrencyCode()
    );
    if ( comparison != EQUAL ) return comparison;    

    
    comparison = this.rounding.compareTo(that.rounding);
    if ( comparison != EQUAL ) return comparison;    
    
    return EQUAL;
  }
  
  // PRIVATE //
  
  /** 
  * The money amount. 
  * Never null. 
  * @serial 
  */
  private BigDecimal amount;
  
  /** 
  * The currency of the money, such as US Dollars or Euros.
  * Never null. 
  * @serial 
  */
  private final Currency currency;
  
  /** 
  * The rounding style to be used. 
  * See {@link BigDecimal}.
  * @serial  
  */
  private final RoundingMode rounding;
  
  /**
  * The default currency to be used if no currency is passed to the constructor. 
  */ 
  private static Currency DEFAULT_CURRENCY = Currency.getInstance("CAD");
  
  /**
  * The default rounding style to be used if no currency is passed to the constructor.
  * See {@link BigDecimal}. 
  */ 
  private static RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
  
  private Object[] getSigFields() {
    return new Object[] {amount, currency, rounding};
  }
  
  /**
  * Determines if a deserialized file is compatible with this class.
  *
  * Maintainers must change this value if and only if the new version
  * of this class is not compatible with old versions. See Sun docs
  * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
  * /serialization/spec/version.doc.html> details. </a>
  *
  * Not necessary to include in first version of the class, but
  * included here as a reminder of its importance.
  */
  private static final long serialVersionUID = 7526471155622776147L;

  /**
  * Always treat de-serialization as a full-blown constructor, by
  * validating the final state of the de-serialized object.
  */  
  private void readObject(
    ObjectInputStream inputStream
  ) throws ClassNotFoundException, IOException {
    //always perform the default de-serialization first
    inputStream.defaultReadObject();
    //defensive copy for mutable date field
    //BigDecimal is not technically immutable, since its non-final
    amount = new BigDecimal( amount.toPlainString() );
    //ensure that object state has not been corrupted or tampered with maliciously
    validateState();
  }

  private void writeObject(ObjectOutputStream outputStream) throws IOException {
    //perform the default serialization for all non-transient, non-static fields
    outputStream.defaultWriteObject();
  }  

  private void validateState(){
    if( amount == null ) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if( currency == null ) {
      throw new IllegalArgumentException("Currency cannot be null");
    }
  }
  
  private int getNumDecimalsForCurrency(){
    return currency.getDefaultFractionDigits();
  }
  
  private void checkCurrenciesMatch(Money aThat){
    if (! this.currency.equals(aThat.getCurrency())) {
       throw new MismatchedCurrencyException(
         aThat.getCurrency() + " doesn't match the expected currency : " + currency
       ); 
    }
  }
  
  /** Ignores scale: 0 same as 0.00 */
  private int compareAmount(Money aThat){
    return this.amount.compareTo(aThat.amount);
  }
  
  private BigDecimal asBigDecimal(double aDouble){
    String asString = Double.toString(aDouble);
    return new BigDecimal(asString);
  }
} 
