package com.tersesystems.echopraxia.log4j;

import static org.assertj.core.api.Assertions.assertThat;

import com.tersesystems.echopraxia.Field;
import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.junit.jupiter.api.Test;

public class LoggerTest extends TestBase {

  @Test
  public void testLoggerWithStringField() {
    Logger<?> logger = LoggerFactory.getLogger(getClass());
    logger.info("my argument is {}", fb -> fb.onlyString("random_key", "random_value"));

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("my argument is random_value");

    final JsonObject fields = entry.getJsonObject("fields");
    assertThat(fields.getString("random_key")).isEqualTo("random_value");
  }

  @Test
  public void testLoggerWithObjectField() {
    Logger<?> logger = LoggerFactory.getLogger(getClass());
    logger.info(
        "my argument is {}",
        fb -> {
          final Field field1 = fb.string("key1", "value1");
          final Field field2 = fb.string("key2", "value2");
          return fb.onlyObject("random_object", field1, field2);
        });

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("my argument is random_object={value1, value2}");

    final JsonObject fields = entry.getJsonObject("fields");
    final JsonObject randomObject = fields.getJsonObject("random_object");
    assertThat(randomObject.getString("key1")).isEqualTo("value1");
    assertThat(randomObject.getString("key2")).isEqualTo("value2");
  }

  @Test
  public void testLoggerWithSeveralObjectField() {
    Logger<?> logger = LoggerFactory.getLogger(getClass());
    logger.info(
        "my arguments are {} {}",
        fb ->
            fb.list(
                fb.object(
                    "object1", //
                    fb.string("key1", "value1"),
                    fb.string("key2", "value2")),
                fb.object(
                    "object2", //
                    fb.string("key3", "value3"),
                    fb.string("key4", "value4"))));
    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message)
        .isEqualTo("my arguments are object1={value1, value2} object2={value3, value4}");

    final JsonObject fields = entry.getJsonObject("fields");

    final JsonObject object1 = fields.getJsonObject("object1");
    assertThat(object1.getString("key1")).isEqualTo("value1");
    assertThat(object1.getString("key2")).isEqualTo("value2");

    final JsonObject object2 = fields.getJsonObject("object2");
    assertThat(object2.getString("key3")).isEqualTo("value3");
    assertThat(object2.getString("key4")).isEqualTo("value4");
  }

  @Test
  public void testLoggerWithArrayField() {
    Logger<?> logger = LoggerFactory.getLogger(getClass());
    logger.info(
        "my argument is {}",
        fb -> {
          Number[] intArray = {1, 2, 3};
          final List<Field.Value<?>> values = Field.Value.asList(intArray, Field.Value::number);
          return fb.onlyArray("random_key", values);
        });

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("my argument is random_key=[1, 2, 3]");

    final JsonObject fields = entry.getJsonObject("fields");
    final JsonArray jsonArray = fields.getJsonArray("random_key");
    final List<Integer> arrayList = jsonArray.getValuesAs(JsonNumber::intValue);
    assertThat(arrayList).isEqualTo(Arrays.asList(1, 2, 3));
  }

  @Test
  public void testLoggerWithThrowable() {
    Logger<?> logger = LoggerFactory.getLogger(getClass());
    Exception exception = new RuntimeException("Some exception");
    logger.error("Message", exception);

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("Message");

    final JsonValue ex = entry.get("exception");
    assertThat(ex).isNotNull();
  }

  @Test
  public void testLoggerWithThrowableField() {
    Logger<?> logger = LoggerFactory.getLogger(getClass());
    Exception exception = new RuntimeException("Some exception");
    logger.error("Message {}", fb -> fb.onlyException(exception));

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("Message exception=java.lang.RuntimeException: Some exception");

    final JsonValue ex = entry.get("exception");
    assertThat(ex).isNotNull();
  }

  @Test
  public void testLoggerWithContextField() {
    Logger<?> logger =
        LoggerFactory.getLogger(getClass())
            .withFields(fb -> fb.onlyString("context_name", "context_field"));
    logger.error("Message");

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("Message");

    final JsonObject fields = entry.getJsonObject("fields");
    assertThat(fields.getString("context_name")).isEqualTo("context_field");
  }

  @Test
  public void testLoggerWithContextAndArgumentField() {
    Logger<?> logger =
        LoggerFactory.getLogger(getClass())
            .withFields(fb -> fb.onlyString("context_name", "context_field"));
    logger.error("Message {}", fb -> fb.onlyString("arg_name", "arg_field"));

    JsonObject entry = getEntry();
    final String message = entry.getString("message");
    assertThat(message).isEqualTo("Message arg_field");

    final JsonObject fields = entry.getJsonObject("fields");
    assertThat(fields.getString("arg_name")).isEqualTo("arg_field");
    assertThat(fields.getString("context_name")).isEqualTo("context_field");
  }
}
