package eu.europeana.normalization.cleaning;

import net.htmlparser.jericho.Source;

public class TestJerico {

  public static void main(String[] args) {
    String htmlText = "lkjdf<b>bold</b> sff<br/>bbbbb Quite<made-up-tag>tag</made-up-tag>tag<table>table isolated ";
    Source source = new Source(htmlText);
    System.out.println(source.getTextExtractor().toString());
  }
}
