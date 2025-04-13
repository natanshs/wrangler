package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TokenType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ByteSize} token.
 */
public class ByteSizeTest {

  @Test
  public void testParsingBytes() {
    ByteSize byteSize = new ByteSize("100B");
    Assert.assertEquals(100L, byteSize.getBytes());
    Assert.assertEquals(TokenType.BYTE_SIZE, byteSize.type());
  }

  @Test
  public void testParsingKilobytes() {
    ByteSize byteSize = new ByteSize("10KB");
    Assert.assertEquals(10 * 1024L, byteSize.getBytes());
    Assert.assertEquals(10.0, byteSize.getKilobytes(), 0.001);
  }

  @Test
  public void testParsingMegabytes() {
    ByteSize byteSize = new ByteSize("1.5MB");
    Assert.assertEquals((long)(1.5 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals(1.5, byteSize.getMegabytes(), 0.001);
  }

  @Test
  public void testParsingGigabytes() {
    ByteSize byteSize = new ByteSize("2GB");
    Assert.assertEquals(2L * 1024 * 1024 * 1024, byteSize.getBytes());
    Assert.assertEquals(2.0, byteSize.getGigabytes(), 0.001);
  }

  @Test
  public void testParsingTerabytes() {
    ByteSize byteSize = new ByteSize("0.1TB");
    Assert.assertEquals((long)(0.1 * 1024 * 1024 * 1024 * 1024), byteSize.getBytes());
    Assert.assertEquals(0.1, byteSize.getTerabytes(), 0.001);
  }

  @Test
  public void testLowercaseUnits() {
    ByteSize byteSize = new ByteSize("5kb");
    Assert.assertEquals(5 * 1024L, byteSize.getBytes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("10X");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingUnit() {
    new ByteSize("10");
  }
}
