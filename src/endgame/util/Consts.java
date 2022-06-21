package endgame.util;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import endgame.model.Money;

public final class Consts {

  /** Newline character.*/
  public static final String NL = System.getProperty("line.separator");
  
  /** Empty space. */
  public static final String SPACE = " ";
  
  /** 0.00 amount. */
  public static final Money ZERO = new Money(new BigDecimal("0.00"));
  
  /** You need to set this in your execution environment. */
  public static final String PROJECT_PATH = System.getProperty("project_path");
  
  public static final Integer NOT_FOUND = -1;
  
  /** 
   All text files should have this encoding (UTF-8).
   Files without this encoding may not be properly read by this simulator.
  */
  public final static Charset ENCODING = StandardCharsets.UTF_8;  
  
  public static final String DATE_SEP = ",";
  
  /** Value: {@value}. */
  public static final int NUM_DAYS_IN_FEBRUARY = 28;
  
  public static final int STANDARD_RETIREMENT_AGE = 65;

}
