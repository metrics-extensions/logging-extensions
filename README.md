# logging-extensions
Logging Helpers

Maven
~~~~
<dependency>
  <groupId>com.groupon.aint.logging</groupId>
  <artifactId>logging-extensions</artifactId>
  <version>1.0.3</version>
</dependency>
~~~~

Usage
~~~~
final MetricsAppender appender = new MetricsAppender(metricsFactory);
appender.start();

final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(
    ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
rootLogger.addAppender(appender);
~~~~
