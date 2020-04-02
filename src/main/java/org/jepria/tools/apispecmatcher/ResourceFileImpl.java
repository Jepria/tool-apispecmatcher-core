package org.jepria.tools.apispecmatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class ResourceFileImpl implements Resource {

  protected final File file;

  public ResourceFileImpl(File file) {
    this.file = file;
  }

  public ResourceFileImpl(String file) {
    this(new File(file));
  }

  @Override
  public Location location() {
    return new Location() {
      @Override
      public String asString() {
        return file.getAbsolutePath();
      }
    };
  }
  @Override
  public Reader newReader() {
    try {
      return new FileReader(file);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e); // TODO
    }
  }
}
