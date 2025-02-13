package com.tersesystems.echopraxia.logstash;

import ch.qos.logback.classic.LoggerContext;
import com.tersesystems.echopraxia.core.CoreLogger;
import com.tersesystems.echopraxia.core.CoreLoggerProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

/**
 * Logstash implementation of a logger provider.
 *
 * <p>This is the main SPI hook into the ServiceLoader.
 */
public class LogstashLoggerProvider implements CoreLoggerProvider {

  private final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

  public @NotNull CoreLogger getLogger(@NotNull String fqcn, @NotNull Class<?> clazz) {
    return getLogger(fqcn, clazz.getName());
  }

  public @NotNull CoreLogger getLogger(@NotNull String fqcn, @NotNull String name) {
    return new LogstashCoreLogger(fqcn, loggerContext.getLogger(name));
  }
}
