package com.bq.corbel.lib.ws.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.izettle.metrics.influxdb.InfluxDbHttpSender;
import com.izettle.metrics.influxdb.InfluxDbReporter;
import com.izettle.metrics.influxdb.InfluxDbSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class DropwizardMetricsIoc {

    private final static String INFLUX_PROPERTIES_PREFIX = "metrics.reporters.influxdb";

    @Autowired
    private Environment env;

    @Bean
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public InfluxDbReporter getInfluxDbReporter() {
        String protocol = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".protocol", "http");
        String host = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".host");
        Integer port = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".port", Integer.class);
        String database = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".database");
        String auth = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".auth");

        Long frequency = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".frequency", Long.class, 30l);

        if (host != null && port != null && database != null) {
            try {
                InfluxDbSender influxDbSender = new InfluxDbHttpSender(protocol, host, port, database, auth, TimeUnit.MILLISECONDS, 1000, 1000, "");
                influxDbSender.setTags(getInfluxDbTags());
                InfluxDbReporter reporter = InfluxDbReporter.forRegistry(getMetricRegistry()).build(influxDbSender);
                reporter.start(frequency, TimeUnit.SECONDS);
                return reporter;
            } catch (Exception e) {
                // It's ok, we just don't log metrics to influxdb
            }
        }

        return null;
    }

    private Map<String, String> getInfluxDbTags() {
        List<String> tagKeys = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".tags.keys", List.class, null);
        List<String> tagValues = env.getProperty(INFLUX_PROPERTIES_PREFIX + ".tags.values", List.class, null);

        Map<String, String> tags = new HashMap<>();
        for (int i = 0; i < tagKeys.size(); i++) {
            tags.put(tagKeys.get(i), tagValues.get(i));
        }
        return tags;
    }

    @PostConstruct
    public void registerMetrics() {
        getMetricRegistry().registerAll(new MemoryUsageGaugeSet());
        getMetricRegistry().registerAll(new GarbageCollectorMetricSet());
    }
}
