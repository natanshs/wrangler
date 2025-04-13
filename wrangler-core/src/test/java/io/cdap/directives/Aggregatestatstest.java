package io.cdap.wrangler.statistics;

import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link AggregateStats} directive.
 */
public class AggregateStatsTest {

  @Test
  public void testUsageDefinition() throws Exception {
    AggregateStats directive = new AggregateStats();
    UsageDefinition usageDefinition = directive.define();
    
    Assert.assertEquals(AggregateStats.NAME, usageDefinition.name());
    Assert.assertTrue(usageDefinition.tokens().size() >= 4);
  }

  @Test
  public void testBasicAggregation() throws Exception {
    // Create test data
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("data_transfer_size", new ByteSize("10KB"));
    row1.add("response_time", new TimeDuration("100ms"));
    
    Row row2 = new Row();
    row2.add("data_transfer_size", new ByteSize("5MB"));
    row2.add("response_time", new TimeDuration("200ms"));
    
    Row row3 = new Row();
    row3.add("data_transfer_size", new ByteSize("2KB"));
    row3.add("response_time", new TimeDuration("50ms"));
    
    rows.add(row1);
    rows.add(row2);
    rows.add(row3);
    
    // Define recipe
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
    };
    
    // Set up mock ExecutorContext for the last batch
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Map<String, Object> storeInfo = new HashMap<>();
    Map<String, Object> aggregateStore = new HashMap<>();
    aggregateStore.put("totalBytes", 0L);
    aggregateStore.put("totalNanoseconds", 0L);
    aggregateStore.put("count", 0);
    storeInfo.put(AggregateStats.NAME, aggregateStore);
    
    Mockito.when(context.getStoreInfo()).thenReturn(storeInfo);
    Mockito.when(context.isLast()).thenReturn(true);
    
    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows, context);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    
    Row resultRow = results.get(0);
    
    // Expected values: 
    // 10KB + 5MB + 2KB = 10*1024 + 5*1024*1024 + 2*1024 bytes = 5,254,144 bytes = 5.01 MB
    // 100ms + 200ms + 50ms = 350ms = 0.35 seconds
    
    double expectedSizeMB = (10 * 1024 + 5 * 1024 * 1024 + 2 * 1024) / (1024.0 * 1024.0);
    double expectedTimeSec = (100 + 200 + 50) / 1000.0;
    
    Assert.assertEquals(expectedSizeMB, (Double) resultRow.getValue("total_size_mb"), 0.001);
    Assert.assertEquals(expectedTimeSec, (Double) resultRow.getValue("total_time_sec"), 0.001);
  }

  @Test
  public void testCustomOutputUnits() throws Exception {
    // Create test data
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("data_transfer_size", new ByteSize("1MB"));
    row1.add("response_time", new TimeDuration("2s"));
    
    rows.add(row1);
    
    // Define recipe with custom output units
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time total_size_kb total_time_ms KB ms"
    };
    
    // Set up mock ExecutorContext for the last batch
    ExecutorContext context = Mockito.mock(ExecutorContext.class);
    Map<String, Object> storeInfo = new HashMap<>();
    Map<String, Object> aggregateStore = new HashMap<>();
    aggregateStore.put("totalBytes", 0L);
    aggregateStore.put("totalNanoseconds", 0L);
    aggregateStore.put("count", 0);
    storeInfo.put(AggregateStats.NAME, aggregateStore);
    
    Mockito.when(context.getStoreInfo()).thenReturn(storeInfo);
    Mockito.when(context.isLast()).thenReturn(true);
    
    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows, context);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    
    Row resultRow = results.get(0);
    
    // Expected values:
    // 1MB = 1024 KB
    // 2s = 2000 ms
    
    Assert.assertEquals(1024.0, (Double) resultRow.getValue("total_size_kb"), 0.001);
    Assert.assertEquals(2000.0, (Double) resultRow.getValue("total_time_ms"), 0.001);
  }
}
