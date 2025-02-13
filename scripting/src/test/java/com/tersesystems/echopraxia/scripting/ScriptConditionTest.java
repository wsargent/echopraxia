package com.tersesystems.echopraxia.scripting;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.tersesystems.echopraxia.Condition;
import com.tersesystems.echopraxia.Field;
import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScriptConditionTest {

  public String buildScript() {
    StringBuilder b = new StringBuilder("library echopraxia {");
    b.append("  function evaluate: (string level, dict fields) ->");
    b.append("    fields[:correlation_id] == \"match\";");
    b.append("}");
    return b.toString();
  }

  @Test
  public void testStringCondition() {
    Condition condition = ScriptCondition.create(false, buildScript(), Throwable::printStackTrace);
    Logger<?> logger = LoggerFactory.getLogger(getClass()).withCondition(condition);
    logger
        .withFields(fb -> fb.only(fb.string("correlation_id", "match")))
        .info("data of interest found");

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    ILoggingEvent event = list.get(0);
    assertThat(event.getMessage()).isEqualTo("data of interest found");
  }

  @Test
  public void testException() {
    Path path = Paths.get("src/test/tweakflow/exception.tf");
    Condition condition = ScriptCondition.create(false, path, Throwable::printStackTrace);

    Logger<?> logger = LoggerFactory.getLogger(getClass()).withCondition(condition);
    logger
        .withFields(fb -> fb.only(fb.exception(new RuntimeException("testing"))))
        .info("data of interest found");

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    ILoggingEvent event = list.get(0);
    assertThat(event.getMessage()).isEqualTo("data of interest found");
  }

  @Test
  public void testCondition() {
    Path path = Paths.get("src/test/tweakflow/condition.tf");
    Condition condition = ScriptCondition.create(false, path, Throwable::printStackTrace);

    Logger<?> logger = LoggerFactory.getLogger(getClass()).withCondition(condition);
    logger
        .withFields(bf -> bf.only(bf.string("correlation_id", "match")))
        .info("data of interest found");

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    ILoggingEvent event = list.get(0);
    assertThat(event.getMessage()).isEqualTo("data of interest found");
  }

  @Test
  public void testNoCondition() {
    Path path = Paths.get("src/test/tweakflow/condition.tf");
    Condition condition = ScriptCondition.create(false, path, Throwable::printStackTrace);

    Logger<?> logger = LoggerFactory.getLogger(getClass()).withCondition(condition);
    logger.info("this should not log");

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    assertThat(list).isEmpty();
  }

  @Test
  public void testComplexCondition() {
    Path path = Paths.get("src/test/tweakflow/complex.tf");
    Condition condition = ScriptCondition.create(false, path, Throwable::printStackTrace);

    Logger<?> logger = LoggerFactory.getLogger(getClass()).withCondition(condition);
    logger
        .withFields(
            fb -> {
              Field name = fb.string("name", "Will");
              Field age = fb.number("age", 13);
              Field toys = fb.array("toys", "binkie");
              Field person = fb.object("person", name, age, toys);
              return singletonList(person);
            })
        .info("data of interest found");

    ListAppender<ILoggingEvent> listAppender = getListAppender();
    List<ILoggingEvent> list = listAppender.list;
    ILoggingEvent event = list.get(0);
    assertThat(event.getMessage()).isEqualTo("data of interest found");
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
