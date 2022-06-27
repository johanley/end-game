package endgame.entitlements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import endgame.util.Consts;
import endgame.util.Log;

/** 
 Fetch and parse GIS brackets from the CRA's website.
 This should be run at least once a year.
*/
public final class GisBracketFetcher {
  
  /**
   Run the tool. Fetch, parse, and overwrite local text files.
   This implementation uses the JSoup tool (jsoup.org). 
  */
  public static void main(String... args) throws IOException {
    Log.log("Fetching data for GIS brackets from the GOC web site. Number of pages = " + NUM_PAGES);
    GisBracketFetcher fetcher = new GisBracketFetcher();
    fetcher.run();
    Log.log("Done");
  }

  /** 
   Example URL https://www.canada.ca/en/services/benefits/publicpensions/cpp/old-age-security/payments/tab1-1.html
   It goes from tab1-1 to tab1-52.
  */
  public static final String BASE_URL = "https://www.canada.ca/en/services/benefits/publicpensions/cpp/old-age-security/payments/tab1-";
  
  /** WARNING: THIS NUMBER INCREASES YEARLY. Make sure you don't truncate the data. */
  public static final Integer NUM_PAGES = 54;
  
  // PRIVATE
  
  /** Run the tool. */
  private void run() throws IOException {
    List<String> lines = new ArrayList<String>();
    for (int i = 1; i <= NUM_PAGES; ++i) {
      String url = BASE_URL + i + ".html";
      List<String> brackets = parseAndMassage(url);
      lines.addAll(brackets);
      Log.log(url + " has " + brackets.size() + " brackets.");
    }
    overwriteInputFile(lines);
  }
  
  private List<String> parseAndMassage(String url) throws IOException {
    List<String> result = new ArrayList<String>();
    Document doc = Jsoup.connect(url).get();
    //there's only one table
    Elements rows = doc.getElementsByTag("tr");
    for(Element row : rows) {
      if (row.child(0).text().startsWith("Yearly Income")) {
        continue; //throw away the header row, which has only header text
      }
      Element incomeRange = row.child(0);
      Element gis = row.child(1);
      String line = incomeRange.text() + ":" + gis.text();
      line = line.replace("$", "");
      line = line.replace(" - ", "_");
      //log(line);
      result.add(line);
    }
    return result;
  }
  
  private void overwriteInputFile(List<String> lines) throws IOException {
    Path path = Paths.get(System.getProperty("user.dir"),"input","gis", "gis-brackets.utf8");
    Files.write(path, lines, Consts.ENCODING);
  }
}
