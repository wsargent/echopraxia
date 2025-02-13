package com.tersesystems.echopraxia.logstash;

import static com.tersesystems.echopraxia.Field.Value;
import static org.slf4j.event.EventConstants.*;

import com.tersesystems.echopraxia.*;
import com.tersesystems.echopraxia.core.CoreLogger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.slf4j.Marker;

/** Logstash implementation of CoreLogger. */
public class LogstashCoreLogger implements CoreLogger {

  // The logger context property used to set up caller info for async logging.
  public static final String ECHOPRAXIA_ASYNC_CALLER_PROPERTY = "echopraxia.async.caller";

  private final ch.qos.logback.classic.Logger logger;
  private final LogstashLoggingContext context;
  private final Condition condition;
  private final Executor executor;
  private final String fqcn;

  protected LogstashCoreLogger(String fqcn, ch.qos.logback.classic.Logger logger) {
    this.fqcn = fqcn;
    this.logger = logger;
    this.context = LogstashLoggingContext.empty();
    this.condition = Condition.always();
    this.executor = ForkJoinPool.commonPool();
  }

  public LogstashCoreLogger(
      String fqcn,
      ch.qos.logback.classic.Logger logger,
      LogstashLoggingContext context,
      Condition condition,
      Executor executor) {
    this.fqcn = fqcn;
    this.logger = logger;
    this.context = context;
    this.condition = condition;
    this.executor = executor;
  }

  /**
   * Returns the underlying SLF4J logger.
   *
   * @return the SLF4J logger.
   */
  public ch.qos.logback.classic.Logger logger() {
    return logger;
  }

  @Override
  @NotNull
  public String getName() {
    return logger.getName();
  }

  /**
   * Returns the condition.
   *
   * @return the condition.
   */
  @Override
  public @NotNull Condition condition() {
    return this.condition;
  }

  @Override
  public @NotNull String fqcn() {
    return fqcn;
  }

  @Override
  public <B extends Field.Builder> @NotNull CoreLogger withFields(
      Field.@NotNull BuilderFunction<B> f, @NotNull B builder) {
    LogstashLoggingContext newContext =
        new LogstashLoggingContext(() -> f.apply(builder), Collections::emptyList);
    return new LogstashCoreLogger(fqcn, logger, this.context.and(newContext), condition, executor);
  }

  @Override
  public @NotNull CoreLogger withThreadContext(
      @NotNull Function<Supplier<Map<String, String>>, Supplier<List<Field>>> mapTransform) {
    Supplier<List<Field>> fieldSupplier = mapTransform.apply(MDC::getCopyOfContextMap);
    LogstashLoggingContext newContext =
        new LogstashLoggingContext(fieldSupplier, Collections::emptyList);
    return new LogstashCoreLogger(fqcn, logger, this.context.and(newContext), condition, executor);
  }

  @Override
  public @NotNull CoreLogger withCondition(@NotNull Condition condition) {
    if (condition == Condition.always()) {
      return this;
    }
    if (condition == Condition.never()) {
      if (this.condition == Condition.never()) {
        return this;
      }
      return new LogstashCoreLogger(fqcn, logger, context, condition, executor);
    }
    return new LogstashCoreLogger(fqcn, logger, context, this.condition.and(condition), executor);
  }

  @Override
  public @NotNull CoreLogger withExecutor(@NotNull Executor executor) {
    return new LogstashCoreLogger(fqcn, logger, context, condition, executor);
  }

  @Override
  public @NotNull CoreLogger withFQCN(@NotNull String fqcn) {
    return new LogstashCoreLogger(fqcn, logger, context, condition, executor);
  }

  public LogstashCoreLogger withLocationEnabled(boolean locationEnabled) {
    return new LogstashCoreLogger(fqcn, logger, context, condition, executor);
  }

  public CoreLogger withMarkers(Marker... markers) {
    LogstashLoggingContext newContext =
        new LogstashLoggingContext(Collections::emptyList, () -> Arrays.asList(markers));
    return new LogstashCoreLogger(fqcn, logger, this.context.and(newContext), condition, executor);
  }

  @Override
  public boolean isEnabled(@NotNull Level level) {
    if (condition == Condition.never()) {
      return false;
    }
    Marker marker = context.getMarker();
    return logger.isEnabledFor(marker, convertLogbackLevel(level))
        && condition.test(level, context);
  }

  @Override
  public boolean isEnabled(@NotNull Level level, @NotNull Condition condition) {
    if (condition == Condition.always()) {
      return isEnabled(level);
    }
    if (condition == Condition.never()) {
      return false;
    }
    Marker marker = context.getMarker();
    return logger.isEnabledFor(marker, convertLogbackLevel(level))
        && this.condition.and(condition).test(level, context);
  }

  @Override
  public void log(@NotNull Level level, String message) {
    Marker m = context.getMarker();
    if (logger.isEnabledFor(m, convertLogbackLevel(level)) && condition.test(level, context)) {
      logger.log(m, fqcn, convertLevel(level), message, null, null);
    }
  }

  @Override
  public <FB extends Field.Builder> void log(
      @NotNull Level level,
      String message,
      Field.@NotNull BuilderFunction<FB> f,
      @NotNull FB builder) {
    // When passing a condition through with explicit arguments, we pull the args and make
    // them available through context.
    final List<Field> args = f.apply(builder);
    final Marker m = context.getMarker();
    final LogstashLoggingContext argContext =
        new LogstashLoggingContext(() -> args, Collections::emptyList);
    if (logger.isEnabledFor(m, convertLogbackLevel(level))
        && condition.test(level, context.and(argContext))) {
      final Object[] arguments = convertArguments(args);
      logger.log(m, fqcn, convertLevel(level), message, arguments, null);
    }
  }

  @Override
  public void log(@NotNull Level level, @NotNull Condition condition, String message) {
    final Marker m = context.getMarker();
    if (logger.isEnabledFor(m, convertLogbackLevel(level))
        && this.condition.and(condition).test(level, context)) {
      logger.log(m, fqcn, convertLevel(level), message, null, null);
    }
  }

  @Override
  public <B extends Field.Builder> void log(
      @NotNull Level level,
      @NotNull Condition condition,
      @Nullable String message,
      @NotNull Field.BuilderFunction<B> f,
      @NotNull B builder) {
    final Marker m = context.getMarker();
    // When passing a condition through with explicit arguments, we pull the args and make
    // them available through context.
    final List<Field> args = f.apply(builder);
    LogstashLoggingContext argContext =
        new LogstashLoggingContext(() -> args, Collections::emptyList);
    if (logger.isEnabledFor(m, convertLogbackLevel(level))
        && this.condition.and(condition).test(level, context.and(argContext))) {
      final Object[] arguments = convertArguments(args);
      logger.log(m, fqcn, convertLevel(level), message, arguments, null);
    }
  }

  @Override
  public <FB extends Field.Builder> void asyncLog(
      @NotNull Level level, @NotNull Consumer<LoggerHandle<FB>> consumer, @NotNull FB builder) {
    final LogstashCoreLogger callerLogger = asyncCallerLogger();
    runAsyncLog(
        consumer,
        new LoggerHandle<FB>() {
          @Override
          public void log(@Nullable String message) {
            callerLogger.log(level, message);
          }

          @Override
          public void log(@Nullable String message, @NotNull Field.BuilderFunction<FB> f) {
            callerLogger.log(level, message, f, builder);
          }
        });
  }

  @Override
  public <FB extends Field.Builder> void asyncLog(
      @NotNull Level level,
      @NotNull Condition c,
      @NotNull Consumer<LoggerHandle<FB>> consumer,
      @NotNull FB builder) {
    final LogstashCoreLogger callerLogger = asyncCallerLogger();
    runAsyncLog(
        consumer,
        new LoggerHandle<FB>() {
          @Override
          public void log(@Nullable String message) {
            callerLogger.log(level, c, message);
          }

          @Override
          public void log(@Nullable String message, @NotNull Field.BuilderFunction<FB> f) {
            callerLogger.log(level, c, message, f, builder);
          }
        });
  }

  /**
   * Returns a core logger with a context containing a LogstashCallerMarker with the caller data
   * needed for the logging event.
   *
   * @return the core logger.
   */
  @NotNull
  protected LogstashCoreLogger asyncCallerLogger() {
    if (isAsyncCallerEnabled()) {
      LogstashCallerMarker callerMarker = new LogstashCallerMarker(fqcn, new Throwable());
      LogstashLoggingContext callerContext =
          new LogstashLoggingContext(
              Collections::emptyList, () -> Collections.singletonList(callerMarker));
      final LogstashLoggingContext contextWithCaller = context.and(callerContext);
      return new LogstashCoreLogger(fqcn, logger, contextWithCaller, condition, executor);
    } else {
      return this;
    }
  }

  /**
   * Returns true if the logback context property "echopraxia.async.caller" is "true", false
   * otherwise.
   *
   * @return if caller data is enabled.
   */
  protected boolean isAsyncCallerEnabled() {
    String locationEnabled =
        logger.getLoggerContext().getProperty(ECHOPRAXIA_ASYNC_CALLER_PROPERTY);
    return Boolean.parseBoolean(locationEnabled);
  }

  // Top level conversion to Logback must be StructuredArgument, with an optional throwable
  // at the end of the array.
  protected Object[] convertArguments(List<Field> args) {
    // Top level arguments must be StructuredArguments, +1 for throwable
    Value<Throwable> throwable = null;
    List<Object> arguments = new ArrayList<>(args.size() + 1);
    for (Field field : args) {
      final Value<?> value = field.value();
      if (value.type() == Value.ValueType.EXCEPTION) {
        throwable = (Value.ExceptionValue) value;
      } else {
        final String name = field.name();
        StructuredArgument array =
            field instanceof ValueField
                ? StructuredArguments.value(name, value)
                : StructuredArguments.keyValue(name, value);
        arguments.add(array);
      }
    }
    // If the exception exists, it must be raw so the varadic case will pick it up.
    if (throwable != null) {
      arguments.add(throwable.raw());
    }
    return arguments.toArray();
  }

  protected <FB extends Field.Builder> void runAsyncLog(
      Consumer<LoggerHandle<FB>> consumer, LoggerHandle<FB> handle) {
    final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
    Runnable runnable =
        () -> {
          if (copyOfContextMap != null) {
            MDC.setContextMap(copyOfContextMap);
          }
          consumer.accept(handle);
        };

    // exceptionally is available in JDK 1.8, we can't use exceptionallyAsync as it's 12 only
    CompletableFuture.runAsync(runnable, executor)
        .exceptionally(
            e -> {
              // Usually we get to this point when you have thread local dependent code in your
              // logger.withContext() block, and your executor doesn't have those thread locals
              // so you NPE.
              //
              // We need to log this error, but since it could be part of the logger context
              // that is causing this error, we can't log the error with the same logger.
              //
              // Fallback to the underlying SLF4J logger to render it.
              final Throwable cause = e.getCause(); // strip the CompletionException
              logger.error("Uncaught exception when running asyncLog", cause);
              return null;
            });
  }

  private ch.qos.logback.classic.Level convertLogbackLevel(Level level) {
    switch (level) {
      case ERROR:
        return ch.qos.logback.classic.Level.ERROR;
      case WARN:
        return ch.qos.logback.classic.Level.WARN;
      case INFO:
        return ch.qos.logback.classic.Level.INFO;
      case DEBUG:
        return ch.qos.logback.classic.Level.DEBUG;
      case TRACE:
        return ch.qos.logback.classic.Level.TRACE;
    }
    throw new IllegalStateException("No level found!");
  }

  private int convertLevel(Level level) {
    switch (level) {
      case ERROR:
        return ERROR_INT;
      case WARN:
        return WARN_INT;
      case INFO:
        return INFO_INT;
      case DEBUG:
        return DEBUG_INT;
      case TRACE:
        return TRACE_INT;
    }
    throw new IllegalStateException("No level found!");
  }

  public String toString() {
    return "LogstashCoreLogger[" + logger.getName() + "]";
  }
}
