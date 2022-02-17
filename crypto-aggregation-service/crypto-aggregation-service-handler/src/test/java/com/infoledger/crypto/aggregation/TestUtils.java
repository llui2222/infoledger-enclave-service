package com.infoledger.crypto.aggregation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestUtils {

  public static byte[] loadResource(String location) throws IOException, URISyntaxException {
    ClassLoader classLoader = TestUtils.class.getClassLoader();
    Path path = Paths.get(classLoader.getResource(location).toURI());

    return Files.readAllBytes(path);
  }
}
