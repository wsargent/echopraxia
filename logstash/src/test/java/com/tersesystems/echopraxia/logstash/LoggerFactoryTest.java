package com.tersesystems.echopraxia.logstash;

import static org.assertj.core.api.Assertions.assertThat;

import com.tersesystems.echopraxia.Field;
import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import org.junit.jupiter.api.Test;

public class LoggerFactoryTest {

  @Test
  public void testLoggerFactory() {
    // Check that the SPI works
    final Logger<Field.Builder> logger = LoggerFactory.getLogger(getClass());
    assertThat(logger).isNotNull();
  }
}
