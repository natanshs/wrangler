package io.cdap.wrangler.statistics;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotation.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A directive that performs aggregation on byte size and time duration columns.
 */
@Plugin(type = Directive.TYPE)
@Name(AggregateStats.NAME)
@Categories(categories = { "statistics", "aggregator" })
@Description("Aggregates byte size and time duration values with automatic unit conversions.")
public class AggregateStats implements Directive {
  public static final String NAME = "aggregate-stats";
  private String sizeColumnName;
  private String timeColumnName;
  private String totalSizeColumnName;
  private String totalTimeColumnName;
  private String sizeOutputUnit = "MB"; // Default to megabytes
  private String timeOutputUnit = "s";  // Default to seconds

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("size-column", TokenType.COLUMN_NAME);
    builder.define("time-column", TokenType.COLUMN_NAME);
    builder.define("total-size-column", TokenType.COLUMN_NAME);
    builder.define("total-time-column", TokenType.COLUMN_NAME);
    builder.define("size-output-unit", TokenType.IDENTIFIER, true);
    builder.define("time-output-unit", TokenType.IDENTIFIER, true);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumnName = ((ColumnName) args.value("size-column")).value();
    this.timeColumnName = ((ColumnName) args.value("time-column")).value();
    this.totalSizeColumnName = ((ColumnName) args.value("total-size-column")).value();
    this.totalTimeColumnName = ((ColumnName) args.value("total-time-column")).value();
    
    if (args.contains("size-output-unit")) {
      this.sizeOutputUnit = args.value("size-output-unit").toString();
    }
    
    if (args.contains("time-output-unit")) {
      this.timeOutputUnit = args.value("time-output-unit").toString();
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    // Use the store to track aggregations across batches
    @SuppressWarnings("unchecked")
    Map<String, Object> store = (Map<String, Object>) context.getStoreInfo().get(NAME);
    if (store == null) {
      store = new HashMap<>();
      store.put("totalBytes", 0L);
      store.put("totalNanoseconds", 0L);
      store.put("count", 0);
      context.getStoreInfo().put(NAME, store);
    }

    // Extract values from the store
    long totalBytes = (Long) store.get("totalBytes");
    long totalNanoseconds = (Long) store.get("totalNanoseconds");
    int count = (Integer) store.get("count");

    // Process all rows
    for (Row row : rows) {
      // Aggregate byte size
      if (row.has(sizeColumnName)) {
        Object sizeObj = row.getValue(sizeColumnName);
        if (sizeObj != null) {
          ByteSize byteSize;
          if (sizeObj instanceof ByteSize) {
            byteSize = (ByteSize) sizeObj;
          } else {
            // Try to parse the value as a ByteSize
            byteSize = new ByteSize(sizeObj.toString());
          }
          totalBytes += byteSize.getBytes();
        }
      }

      // Aggregate time duration
      if (row.has(timeColumnName)) {
        Object timeObj = row.getValue(timeColumnName);
        if (timeObj != null) {
          TimeDuration timeDuration;
          if (timeObj instanceof TimeDuration) {
            timeDuration = (TimeDuration) timeObj;
          } else {
            // Try to parse the value as a TimeDuration
            timeDuration = new TimeDuration(timeObj.toString());
          }
          totalNanoseconds += timeDuration.getNanoseconds();
        }
      }

      count++;
    }

    // Update the store
    store.put("totalBytes", totalBytes);
    store.put("totalNanoseconds", totalNanoseconds);
    store.put("count", count);

    // If this is the last batch, return the aggregated result
    if (context.isLast()) {
      Row result = new Row();
      
      // Convert bytes to the requested output unit
      switch (sizeOutputUnit.toUpperCase()) {
        case "B":
          result.add(totalSizeColumnName, totalBytes);
          break;
        case "KB":
          result.add(totalSizeColumnName, totalBytes / 1024.0);
          break;
        case "MB":
          result.add(totalSizeColumnName, totalBytes / (1024.0 * 1024.0));
          break;
        case "GB":
          result.add(totalSizeColumnName, totalBytes / (1024.0 * 1024.0 * 1024.0));
          break;
        case "TB":
          result.add(totalSizeColumnName, totalBytes / (1024.0 * 1024.0 * 1024.0 * 1024.0));
          break;
        default:
          // Default to MB if unit is not recognized
          result.add(totalSizeColumnName, totalBytes / (1024.0 * 1024.0));
          break;
      }
      
      // Convert nanoseconds to the requested output unit
      switch (timeOutputUnit.toLowerCase()) {
        case "ns":
          result.add(totalTimeColumnName, totalNanoseconds);
          break;
        case "ms":
          result.add(totalTimeColumnName, totalNanoseconds / 1_000_000.0);
          break;
        case "s":
          result.add(totalTimeColumnName, totalNanoseconds / 1_000_000_000.0);
          break;
        case "m":
          result.add(totalTimeColumnName, totalNanoseconds / (60.0 * 1_000_000_000.0));
          break;
        case "h":
          result.add(totalTimeColumnName, totalNanoseconds / (60.0 * 60.0 * 1_000_000_000.0));
          break;
        case "d":
          result.add(totalTimeColumnName, totalNanoseconds / (24.0 * 60.0 * 60.0 * 1_000_000_000.0));
          break;
        default:
          // Default to seconds if unit is not recognized
          result.add(totalTimeColumnName, totalNanoseconds / 1_000_000_000.0);
          break;
      }
      
      return List.of(result);
    }
    
    // For intermediate batches, return an empty list (aggregation in progress)
    return new ArrayList<>();
  }

  @Override
  public Lineage lineage() {
    Lineage lineage = new Lineage();
    lineage.addMany(sizeColumnName, timeColumnName)
           .addMany(List.of(Mutation.create), List.of(totalSizeColumnName, totalTimeColumnName));
    return lineage;
  }
}
