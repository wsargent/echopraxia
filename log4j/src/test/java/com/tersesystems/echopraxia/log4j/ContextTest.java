package com.tersesystems.echopraxia.log4j;

import static org.assertj.core.api.Assertions.assertThat;

import com.tersesystems.echopraxia.Field;
import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.junit.jupiter.api.Test;

public class ContextTest {

  @Test
  void testMarkers() {
    Marker securityMarker = MarkerManager.getMarker("SECURITY");
    final Log4JCoreLogger core = new Log4JCoreLogger(LogManager.getLogger());
    Logger<?> logger =
        LoggerFactory.getLogger(core.withMarkers(securityMarker), Field.Builder.instance());
    logger = logger.withFields(f -> f.onlyString("context_name", "context_value"));

    // XXX Should render both context field and argument field.
    logger.error("Message {}", fb -> fb.onlyString("field_name", "field_value"));
  }

  @Test
  void testMarkerPredicate() {
    // Because MarkerFilter with ACCEPT exists,
    // AND we have a SECURITY marker in context
    // isTraceEnabled should return true even without an explicit marker.
    final Marker securityMarker = MarkerManager.getMarker("SECURITY");
    final Log4JCoreLogger core = new Log4JCoreLogger(LogManager.getLogger());
    Logger<?> logger =
        LoggerFactory.getLogger(core.withMarkers(securityMarker), Field.Builder.instance());

    org.apache.logging.log4j.Logger log4jLogger = core.logger();

    // Calling log4j directly should return true...
    assertThat(log4jLogger.isTraceEnabled(securityMarker)).isTrue();

    // But otherwise TRACE is not enabled...
    assertThat(log4jLogger.isTraceEnabled()).isFalse();

    // And as the marker is in context, it should be true as well.
    assertThat(logger.isTraceEnabled()).isTrue();
  }
}
