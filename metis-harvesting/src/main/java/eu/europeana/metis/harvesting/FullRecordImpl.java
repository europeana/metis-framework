package eu.europeana.metis.harvesting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import org.apache.commons.io.IOUtils;

public record FullRecordImpl(String relativeFilePath, ByteArrayInputStream entryContent) implements FullRecord {

  @Override
  public String getHarvestingIdentifier() {
    return relativeFilePath;
  }

  @Override
  public void writeContent(OutputStream outputStream) throws IOException {
    IOUtils.copy(entryContent, outputStream);
  }

  @Override
  public ByteArrayInputStream getContent() {
    return entryContent;
  }

  @Override
  public boolean isDeleted() {
    return false;
  }

  @Override
  public Instant getTimeStamp() {
    return null;
  }
}
