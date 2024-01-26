package eu.europeana.metis.mediaprocessing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ModelObjectParser extends AbstractParser {

  private static final Set<MediaType> SUPPORTED_TYPES = Collections.singleton(MediaType.application("model/obj"));
  public static final String MODEL_OBJ_TYPE = "model/obj";

  @Override
  public Set<MediaType> getSupportedTypes(ParseContext context) {
    return SUPPORTED_TYPES;
  }

  @Override
  public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
      throws IOException, SAXException, TikaException {
    metadata.set(Metadata.CONTENT_TYPE, MODEL_OBJ_TYPE);

    XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
    xhtml.startDocument();
    xhtml.endDocument();

  }
}
