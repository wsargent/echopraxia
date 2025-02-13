package com.tersesystems.echopraxia.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.tersesystems.echopraxia.Field;
import com.tersesystems.echopraxia.core.Caller;
import com.tersesystems.echopraxia.core.CoreLoggerFactory;
import com.tersesystems.echopraxia.logstash.LogstashCoreLogger;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MarkerFactory;

public class SemanticLoggerTest {

  static class Person {
    final String name;
    final int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }
  }

  @Test
  public void testLogger() {
    SemanticLogger<Person> logger =
        SemanticLoggerFactory.getLogger(
            getClass(),
            Person.class,
            person -> "person.name = {}, person.age = {}",
            p -> b -> b.list(b.string("name", p.name), b.number("age", p.age)));

    Person eloise = new Person("Eloise", 1);
    logger.info(eloise);

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    ILoggingEvent event = list.get(0);
    assertThat(event.getFormattedMessage()).isEqualTo("person.name = Eloise, person.age = 1");
  }

  @Test
  public void testLoggerWithLogstashEscape() {
    LogstashCoreLogger coreLogger =
        (LogstashCoreLogger)
            CoreLoggerFactory.getLogger(SemanticLoggerFactory.FQCN, Caller.resolveClassName());
    SemanticLogger<Person> logger =
        SemanticLoggerFactory.getLogger(
            coreLogger.withMarkers(MarkerFactory.getMarker("SECURITY")),
            Person.class,
            person -> "person.name = {}, person.age = {}",
            p -> b -> b.list(b.string("name", p.name), b.number("age", p.age)),
            Field.Builder.instance());

    Person eloise = new Person("Eloise", 1);
    logger.info(eloise);

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    ILoggingEvent event = list.get(0);
    assertThat(event.getFormattedMessage()).isEqualTo("person.name = Eloise, person.age = 1");
  }

  @BeforeEach
  public void beforeEach() {
    getListAppender().list.clear();
  }

  LoggerContext loggerContext() {
    return (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
  }

  ListAppender<ILoggingEvent> getListAppender() {
    return (ListAppender<ILoggingEvent>)
        loggerContext().getLogger(ROOT_LOGGER_NAME).getAppender("LIST");
  }
}
