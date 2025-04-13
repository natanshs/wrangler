package io.cdap.wrangler.api.parser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token for byte sizes with units (B, KB, MB, GB, TB).
 */
public class ByteSize extends Token {
  private static final Pattern PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([KkMmGgTt]?[Bb])");
  private final double value;
  private final String unit;
  private final long bytes;

  public ByteSize(String value) {
    super(value);
    Matcher matcher = PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + value);
    }
    
    this.value = Double.parseDouble(matcher.group(1));
    this.unit = matcher.group(2).toUpperCase(Locale.ENGLISH);
    this.bytes = calculateBytes();
  }

  /**
   * Calculates the number of bytes based on the value and unit.
   * Uses the binary system (1024-based) for conversions.
   */
  private long calculateBytes() {
    switch (unit) {
      case "B":
        return (long) value;
      case "KB":
        return (long) (value * 1024);
      case "MB":
        return (long) (value * 1024 * 1024);
      case "GB":
        return (long) (value * 1024 * 1024 * 1024);
      case "TB":
        return (long) (value * 1024 * 1024 * 1024 * 1024);
      default:
        throw new IllegalArgumentException("Unknown byte unit: " + unit);
    }
  }

  /**
   * Returns the size in bytes.
   */
  public long getBytes() {
    return bytes;
  }

  /**
   * Returns the size in kilobytes.
   */
  public double getKilobytes() {
    return bytes / 1024.0;
  }

  /**
   * Returns the size in megabytes.
   */
  public double getMegabytes() {
    return bytes / (1024.0 * 1024.0);
  }

  /**
   * Returns the size in gigabytes.
   */
  public double getGigabytes() {
    return bytes / (1024.0 * 1024.0 * 1024.0);
  }

  /**
   * Returns the size in terabytes.
   */
  public double getTerabytes() {
    return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
  }

  /**
   * Returns the numeric value of the size.
   */
  public double getValue() {
    return value;
  }

  /**
   * Returns the unit of the size.
   */
  public String getUnit() {
    return unit;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }
}
