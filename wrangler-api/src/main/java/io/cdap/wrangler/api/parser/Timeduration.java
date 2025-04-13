package io.cdap.wrangler.api.parser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token for time durations with units (ns, ms, s, m, h, d).
 */
public class TimeDuration extends Token {
  private static final Pattern PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([NnMmSsHhDd]{1,2})");
  private final double value;
  private final String unit;
  private final long nanoseconds;

  public TimeDuration(String value) {
    super(value);
    Matcher matcher = PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + value);
    }
    
    this.value = Double.parseDouble(matcher.group(1));
    this.unit = matcher.group(2).toLowerCase(Locale.ENGLISH);
    this.nanoseconds = calculateNanoseconds();
  }

  /**
   * Calculates the number of nanoseconds based on the value and unit.
   */
  private long calculateNanoseconds() {
    switch (unit) {
      case "ns":
        return (long) value;
      case "ms":
        return (long) (value * 1_000_000);
      case "s":
        return (long) (value * 1_000_000_000);
      case "m":
        return (long) (value * 60 * 1_000_000_000L);
      case "h":
        return (long) (value * 60 * 60 * 1_000_000_000L);
      case "d":
        return (long) (value * 24 * 60 * 60 * 1_000_000_000L);
      default:
        throw new IllegalArgumentException("Unknown time unit: " + unit);
    }
  }

  /**
   * Returns the duration in nanoseconds.
   */
  public long getNanoseconds() {
    return nanoseconds;
  }

  /**
   * Returns the duration in milliseconds.
   */
  public double getMilliseconds() {
    return nanoseconds / 1_000_000.0;
  }

  /**
   * Returns the duration in seconds.
   */
  public double getSeconds() {
    return nanoseconds / 1_000_000_000.0;
  }

  /**
   * Returns the duration in minutes.
   */
  public double getMinutes() {
    return nanoseconds / (60.0 * 1_000_000_000.0);
  }

  /**
   * Returns the duration in hours.
   */
  public double getHours() {
    return nanoseconds / (60.0 * 60.0 * 1_000_000_000.0);
  }

  /**
   * Returns the duration in days.
   */
  public double getDays() {
    return nanoseconds / (24.0 * 60.0 * 60.0 * 1_000_000_000.0);
  }

  /**
   * Returns the numeric value of the duration.
   */
  public double getValue() {
    return value;
  }

  /**
   * Returns the unit of the duration.
   */
  public String getUnit() {
    return unit;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }
}
