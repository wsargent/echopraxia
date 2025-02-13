package com.tersesystems.echopraxia;

import static org.assertj.core.api.Assertions.assertThat;

import com.tersesystems.echopraxia.core.CoreLogger;
import com.tersesystems.echopraxia.core.CoreLoggerFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class FilterTests {

  @Test
  public void testFilter() {
    AtomicReference<List<Field>> myFields = new AtomicReference<>();

    CoreLogger logger = CoreLoggerFactory.getLogger(Logger.class.getName(), "example.Logger");
    Condition condition =
        new Condition() {
          @Override
          public boolean test(Level level, LoggingContext context) {
            List<Field> fields = context.getFields();
            myFields.set(fields);
            return true;
          }
        };
    logger.log(Level.INFO, condition, "Hello");

    List<Field> fields = myFields.get();
    Field field = fields.get(0);
    assertThat(field.name()).isEqualTo("example_field");
  }
}
