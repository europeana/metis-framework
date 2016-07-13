package eu.europeana.validation.edm.validation;

import org.w3c.dom.ls.LSResourceResolver;

/**
 * Created by ymamakis on 3/21/16.
 */
public interface AbstractLSResourceResolver extends LSResourceResolver {

    void setPrefix(String prefix);

    String getPrefix();

    SwiftProvider getSwiftProvider();
}
