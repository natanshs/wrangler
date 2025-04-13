package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link TimeDuration} token.
 */
public class TimeDurationTest {

  @Test
  public void testParsingNanoseconds() {
    TimeDuration timeDuration = new TimeDuration("100ns");
    Assert.assertEquals(100L, timeDuration.getNanoseconds());
    Assert.assertEquals(TokenType.TIME_DURATION, timeDuration.type());
  }

  @Test
  public void testParsingMilliseconds() {
    TimeDuration timeDuration = new TimeDuration("10ms");
    Assert.assertEquals(10 * 1_000_000L, timeDuration.getNanoseconds());
    Assert.assertEquals(10.0, timeDuration.getMilliseconds(), 0.001);
  }

  @Test
  public void testParsingSeconds() {
    TimeDuration timeDuration = new TimeDuration("1.5s");
    Assert.assertEquals((long)(1.5 * 1_000_000_000), timeDuration.getNanoseconds());
    Assert.assertEquals(1.5, timeDuration.getSeconds(), 0.001);
  }

  @Test
  public void testParsingMinutes() {
    TimeDuration timeDuration = new TimeDuration("2m");
    Assert.assertEquals(2L * 60 * 1_000_000_000, timeDuration.getNanoseconds());
    Assert.assertEquals(2.0, timeDuration.getMinutes(), 0.001);
  }

  @Test
  public void testParsingHours() {
    TimeDuration timeDuration = new TimeDuration("0.5h");
    Assert.assertEquals((long)(0.5 * 60 * 60 * 1_000_000_000), timeDuration.getNanoseconds());
    Assert.assertEquals(0.5, timeDuration.getHours(), 0.001);
  }

  @Test
  public void testParsingDays() {
    TimeDuration timeDuration = new TimeDuration("1d");
    Assert.assertEquals(1L * 24 * 60 * 60 * 1_000_000_000, timeDuration.getNanoseconds());
    Assert.assertEquals(1.0, timeDuration.getDays(), 0.001);
  }

  @Test
  public void testUppercaseUnits() {
    TimeDuration timeDuration = new TimeDuration("5MS");
    Assert.assertEquals(5 * 1_000_000L, timeDuration.getNanoseconds());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("10X");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingUnit() {
    new TimeDuration("10");
  }
}
