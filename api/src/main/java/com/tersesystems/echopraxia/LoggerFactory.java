package com.tersesystems.echopraxia;

import com.tersesystems.echopraxia.core.Caller;
import com.tersesystems.echopraxia.core.CoreLogger;
import com.tersesystems.echopraxia.core.CoreLoggerFactory;

/**
 * The LoggerFactory class. This is used to create the appropriate `Logger`.
 *
 * <p>{@code private static final Logger logger = LoggerFactory.getLogger(); }
 */
public class LoggerFactory {

  /**
   * Creates a logger using the given class name.
   *
   * @param clazz the logger class to use
   * @return the logger.
   */
  public static Logger<Field.Builder> getLogger(Class<?> clazz) {
    final CoreLogger core = CoreLoggerFactory.getLogger(clazz);
    return getLogger(core, Field.Builder.instance());
  }

  /**
   * Creates a logger using the given class name and explicit field builder.
   *
   * @param clazz the logger class to use
   * @param builder the field builder.
   * @return the logger.
   * @param <FB> the type of field builder.
   */
  public static <FB extends Field.Builder> Logger<FB> getLogger(Class<?> clazz, FB builder) {
    CoreLogger coreLogger = LoggerFactory.getLogger(clazz).core();
    return getLogger(coreLogger, builder);
  }

  /**
   * Creates a logger using the given name.
   *
   * @param name the logger name to use
   * @return the logger.
   */
  public static Logger<Field.Builder> getLogger(String name) {
    final CoreLogger core = CoreLoggerFactory.getLogger(name);
    return getLogger(core, Field.Builder.instance());
  }

  /**
   * Creates a logger using the given name and an explicit field builder.
   *
   * @param name the logger name to use
   * @param builder the field builder.
   * @param <FB> the type of field builder.
   * @return the logger.
   */
  public static <FB extends Field.Builder> Logger<FB> getLogger(String name, FB builder) {
    CoreLogger coreLogger = LoggerFactory.getLogger(name).core();
    return getLogger(coreLogger, builder);
  }

  /**
   * Creates a logger using the caller's class name.
   *
   * @return the logger.
   */
  public static Logger<Field.Builder> getLogger() {
    CoreLogger core = CoreLoggerFactory.getLogger(Caller.resolveClassName());
    return getLogger(core, Field.Builder.instance());
  }

  /**
   * Creates a logger using the caller's class name and an explicit field builder.
   *
   * @param fieldBuilder the field builder.
   * @return the logger.
   * @param <FB> the type of field builder.
   */
  public static <FB extends Field.Builder> Logger<FB> getLogger(FB fieldBuilder) {
    CoreLogger core = CoreLoggerFactory.getLogger(Caller.resolveClassName());
    return getLogger(core, fieldBuilder);
  }

  /**
   * Creates a logger from a core logger and a field builder.
   *
   * @param core logger
   * @param fieldBuilder the field builder.
   * @return the logger.
   * @param <FB> the type of field builder.
   */
  public static <FB extends Field.Builder> Logger<FB> getLogger(CoreLogger core, FB fieldBuilder) {
    return new Logger<>(core, fieldBuilder);
  }
}
