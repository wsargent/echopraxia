
<!---freshmark shields
output = [
	link(shield('mvnrepository', 'mvnrepository', '{{group}}', 'blue'), 'https://mvnrepository.com/artifact/{{group}}'),
	link(shield('License Apache', 'license', 'Apache', 'blue'), 'https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)'),
	].join('\n')
-->
[![mvnrepository](https://img.shields.io/badge/mvnrepository-com.tersesystems.echopraxia-blue.svg)](https://mvnrepository.com/artifact/com.tersesystems.echopraxia)
[![License Apache](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))
<!---freshmark /shields -->
# Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia) is a Java logging API designed around structured logging, rich context, and conditional logging.  There are Logback and Log4J2 implementations, but Echopraxia's API is completely dependency-free, meaning it can be implemented with any logging API, i.e. jboss-logging, JUL, JEP 264, or even directly.

Echopraxia is a sequel to the Scala logging API [Blindsight](https://github.com/tersesystems/blindsight), hence the name: "Echopraxia is the involuntary repetition or imitation of an observed action."

Echopraxia is based around several main concepts that build and leverage on each other:

* Structured Logging (API based around structured fields and values)
* Contextual Logging (API based around building state in loggers)
* Conditions (API based around context-aware functions and dynamic scripting)
* Semantic Logging (API based around typed arguments)
* Fluent Logging (API based around log entry builder)
* Filters (pipeline for adding fields and conditions to loggers)

Although Echopraxia is tied on the backend to an implementation, it is designed to hide implementation details from you, just as SLF4J hides the details of the logging implementation.  For example, `logstash-logback-encoder` provides `Markers` or `StructuredArguments`, but you will not see them in the API.  Instead, Echopraxia works with independent `Field` and `Value` objects that are converted by a `CoreLogger` provided by an implementation.

Please see the [blog posts](https://tersesystems.com/category/logging/) for more background on logging stuff.

## Examples 

Simple examples and integrations with [dropwizard metrics](https://metrics.dropwizard.io/4.2.0/) and [OSHI](https://github.com/oshi/oshi) are available at [echopraxia-examples](https://github.com/tersesystems/echopraxia-examples).

For a web application example,
see this [Spring Boot Project](https://github.com/tersesystems/echopraxia-spring-boot-example).

## Statement of Intent

**Echopraxia is not a replacement for SLF4J**.  It is not an attempt to compete with Log4J2 API, JUL, commons-logging for the title of "one true logging API" and restart the [logging mess](https://techblog.bozho.net/the-logging-mess/).  SLF4J won that fight [a long time ago](https://www.semanticscholar.org/paper/Studying-the-Use-of-Java-Logging-Utilities-in-the-Chen-Jiang/be39720a72f04c92b9aece9548171d5fa3a627e6).

Echopraxia is a structured logging API.  It is an appropriate solution **when you control the logging implementation** and have decided you're going to do structured logging, e.g. a web application where you've decided to use [logstash-logback-encoder](https://github.com/logfellow/logstash-logback-encoder) already. 

SLF4J is an appropriate solution **when you do not control the logging output**, e.g. in an open-source library that could be used in arbitrary situations by anybody.  

Echopraxia is best described as a specialization or augmentation for application code -- as you're building framework support code for your application and build up your domain objects, you can write custom field builders, then log everywhere in your application with a consistent schema.

## Benchmarks

Benchmarks show [performance inline with straight calls to the implementation](BENCHMARKS.md).  

Please be aware that how fast and how much you can log is [dramatically impacted](https://tersesystems.com/blog/2019/06/03/application-logging-in-java-part-6/) by your use of an asynchronous appender, your available I/O, your storage, and your ability to manage and process logs.  

Logging can be categorized as either diagnostic (DEBUG/TRACE) or operational (INFO/WARN/ERROR).

If you are doing significant diagnostic logging, consider using an appender optimized for fast local logging, such as [Blacklite](https://github.com/tersesystems/blacklite/), and consider writing to `tmpfs`.

If you are doing significant operational logging, you should commit to a budget for operational costs i.e. storage, indexing, centralized logging infrastructure.  It is very likely that you will run up against budget constraints long before you ever need to optimize your logging for greater throughput.

## Logstash

There is a Logback implementation based around [logstash-logback-encoder](https://github.com/logfellow/logstash-logback-encoder) implementation of [event specific custom fields](https://github.com/logfellow/logstash-logback-encoder#event-specific-custom-fields).

Maven:

```
<dependency>
  <groupId>com.tersesystems.echopraxia</groupId>
  <artifactId>logstash</artifactId>
  <version><VERSION></version>
</dependency>
```

Gradle:

```
implementation "com.tersesystems.echopraxia:logstash:<VERSION>" 
```

## Log4J

There is a Log4J implementation that works with the [JSON Template Layout](https://logging.apache.org/log4j/2.x/manual/json-template-layout.html).

Maven:

```
<dependency>
  <groupId>com.tersesystems.echopraxia</groupId>
  <artifactId>log4j</artifactId>
  <version><VERSION></version>
</dependency>
```

Gradle:

```
implementation "com.tersesystems.echopraxia:log4j:<VERSION>" 
```

You will need to integrate the `com.tersesystems.echopraxia.log4j.layout` package into your `log4j2.xml` file, e.g. by using the `packages` attribute, and add an `EventTemplateAdditionalField` element:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" packages="com.tersesystems.echopraxia.log4j.layout">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT" follow="true">
            <JsonTemplateLayout eventTemplateUri="classpath:LogstashJsonEventLayoutV1.json">
                <EventTemplateAdditionalField
                        key="fields"
                        format="JSON"
                        value='{"$resolver": "echopraxiaFields"}'/>
            </JsonTemplateLayout>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console" />
        </Root>
    </Loggers>
</Configuration>
```

If you want to separate the context fields from the argument fields, you can define them separately:

```xml
<JsonTemplateLayout eventTemplateUri="classpath:LogstashJsonEventLayoutV1.json">
    <EventTemplateAdditionalField
            key="arguments"
            format="JSON"
            value='{"$resolver": "echopraxiaArgumentFields"}'/>
    <EventTemplateAdditionalField
            key="context"
            format="JSON"
            value='{"$resolver": "echopraxiaContextFields"}'/>
</JsonTemplateLayout>
```

Unfortunately, I don't know of a way to "flatten" fields so that they show up on the root object instead of under an additional field.  If you know how to do this, let me know!

## Basic Usage

For almost all use cases, you will be working with the API which is a single import:

```
import com.tersesystems.echopraxia.*;
```

First you define a logger (usually in a controller or singleton -- `getClass()` is particularly useful for abstract controllers):

```java
final Logger<?> basicLogger = LoggerFactory.getLogger(getClass());
```

Logging simple messages and exceptions are done as in SLF4J: 

```java
try {
  ...
  basicLogger.info("Simple message");
} catch (Exception e) {
  basicLogger.error("Error message", e);  
}
```

However, when you log arguments, you pass a function which provides you with a field builder and returns a list of fields:

```java
basicLogger.info("Message name {} age {}", fb -> fb.list(
  fb.string("name", "value"),
  fb.number("age", 13)
));
```

You can specify a single field using `only`:

```java
basicLogger.info("Message name {}", fb -> fb.only(fb.string("name", "value")));
```

And there are some shortcut methods like `onlyString` that combine `only` and `string`:

```java
basicLogger.info("Message name {}", fb -> fb.onlyString("name", "value"));
```

You can log multiple arguments and include the exception if you want the stack trace:

```java
basicLogger.info("Message name {}", fb -> fb.list(
  fb.string("name", "value"),
  fb.exception(e)
));
```

So far so good. But logging strings and numbers can get tedious.  Let's go into custom field builders.  

### Custom Field Builders

Echopraxia lets you specify custom field builders whenever you want to log domain objects:

```java
public class BuilderWithDate implements Field.Builder {
  public BuilderWithDate() {}

  // Renders a date as an ISO 8601 string.
  public StringValue dateValue(Date date) {
    return Value.string(DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
  }

  public Field date(String name, Date date) {
    return string(name, dateValue(date));
  }

  // Renders a date using the `only` idiom returning a list of `Field`.
  // This is a useful shortcut when you only have one field you want to add.
  public List<Field> onlyDate(String name, Date date) {
    return only(date(name, date));
  }
}
```

And now you can render a date automatically:

```java
Logger<BuilderWithDate> dateLogger = basicLogger.withFieldBuilder(BuilderWithDate.class);
dateLogger.info("Date {}", fb -> fb.onlyDate("creation_date", new Date()));
```

This also applies to more complex objects:

```java
public class PersonBuilder implements Field.Builder {

  // Renders a `Person` as an object field.
  public Field person(String fieldName, Person p) {
    return keyValue(fieldName, personValue(p));
  }

  public Value<?> personValue(Person p) {
    if (p == null) {
      return Value.nullValue();
    }
    Field name = string("name", p.name());
    Field age = number("age", p.age());
    // optional returns either an object value or null value, keyValue is untyped
    Field father = keyValue("father", Value.optional(p.getFather().map(this::personValue)));
    Field mother = keyValue("mother", Value.optional(p.getMother().map(this::personValue)));
    Field interests = array("interests", p.interests());
    return Value.object(name, age, father, mother, interests);
  }
}
```

And then you can do the same by calling `fb.person`:

```java
Person user = ...
Logger<PersonBuilder> personLogger = basicLogger.withFieldBuilder(PersonBuilder.class);
personLogger.info("Person {}", fb -> fb.only(fb.person("user", user)));
```

### Custom Logger Factories

If you are using a particular set of field builders for your domain and want them available by default, it's easy to create your own logger with your own field builder, using the support classes and interfaces.  

Creating your own logger will also remove the type parameter from your code, so you don't have to type `Logger<?>` everywhere.

```java
import com.tersesystems.echopraxia.core.*;
import com.tersesystems.echopraxia.support.*;

public class MyLogger extends AbstractLoggerSupport<MyLogger, PersonBuilder>
        implements DefaultLoggerMethods<PersonBuilder> {

  public MyLogger(CoreLogger core, PersonBuilder fieldBuilder) {
    super(core, fieldBuilder);
  }

  @Override
  protected MyLogger newLogger(CoreLogger core) {
    return new MyLogger(core, fieldBuilder());
  }

  // Custom method for this logger
  public void personInfo(String personName, Person p) {
    core.log(Level.INFO, "{}", fb -> fb.onlyPerson(personName, p), fieldBuilder());
  }
}

public class MyLoggerFactory {
  private static final String FQCN = MyLogger.class.getName();
  private static final PersonBuilder fieldBuilder = new PersonBuilder();

  public static MyLogger getLogger(Class<?> clazz) {
    final CoreLogger core = CoreLoggerFactory.getLogger(FQCN, clazz);
    return new MyLogger(core, fieldBuilder);
  }
}

public class Main {
  private static final MyLogger logger = MyLoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    // ... create person object...
    logger.personInfo("user", user);
  }
}
```

### Nulls and Exceptions

By default, values are `@NotNull`, and passing in `null` to values is not recommended.  If you want to handle nulls, you can extend the field builder as necessary:

```java
public interface NullableFieldBuilder extends Field.Builder {
  // extend as necessary
  default Field nullableString(String name, String nullableString) {
    Value<?> nullableValue = (value == null) ? Value.nullValue() : Value.string(nullableString);
    return keyValue(name, nullableValue);
  }
}
```

Field names are never allowed to be null.  If a field name is null, it will be replaced at runtime with `unknown-echopraxia-N` where N is an incrementing number.

```java
logger.info("Message name {}", fb -> 
  fb.only(fb.string(null, "some-value")) // null field names not allowed
);
```

In addition, `fb.only()` will return an empty list if a null field is passed in:


```java
logger.info("Message name {}", fb -> 
  Field field = null;
  return fb.only(field); // returns an empty list of fields.
);
```

Because a field builder function runs in a closure, if an exception occurs it will be caught by the default thread exception handler.  If it's the main thread, it will print to console and terminate the JVM, but other threads [will swallow the exception whole](https://stackoverflow.com/questions/24834702/do-errors-thrown-within-uncaughtexceptionhandler-get-swallowed).  Consider setting a [default thread exception handler](https://www.logicbig.com/tutorials/core-java-tutorial/java-se-api/default-uncaught-exception-handler.html) that additionally logs, and avoid uncaught exceptions in field builder closures:

```java
logger.info("Message name {}", fb -> {
  String name = methodThatThrowsException(); // BAD
  return fb.only(fb.string(name, "some-value"));
});
```

Instead, only call field builder methods inside the closure and keep any construction logic outside:

```java
String name = methodThatThrowsException(); // GOOD
logger.info("Message name {}", fb -> {
  return fb.only(fb.string(name, "some-value"));
});
```

## Context

You can also add fields directly to the logger using `logger.withFields` for contextual logging:

```java
Logger<?> loggerWithFoo = basicLogger.withFields(fb -> fb.onlyString("foo", "bar"));

// will log "foo": "bar" field in a JSON appender.
loggerWithFoo.info("JSON field will log automatically") 
```

This works very well for HTTP session and request data such as correlation ids.

One thing to be aware of that the popular idiom of using `public static final Logger<?> logger` can be limiting in cases where you want to include context data.  For example, if you have a number of objects with their own internal state, it may be more appropriate to create a logger field on the object.

```java
public class PlayerData {

  // the date is scoped to an instance of this player
  private Date lastAccessedDate = new Date();

  // logger is not static because lastAccessedDate is an instance variable
  private final Logger<BuilderWithDate> logger =
      LoggerFactory.getLogger()
          .withFieldBuilder(BuilderWithDate.class)
          .withFields(fb -> fb.onlyDate("last_accessed_date", lastAccessedDate));

}
```

### Thread Context

You can also resolve any fields in Mapped Diagnostic Context (MDC) into fields, using `logger.withThreadContext()`.  This method provides a pre-built function that calls `fb.string` for each entry in the map.

Because MDC is thread local, if you pass the logger between threads or use asynchronous processing i.e. `CompletionStage/CompletableFuture`, you may have inconsistent results.

```java
org.slf4j.MDC.put("mdckey", "mdcvalue");
myLogger.withThreadContext().info("This statement has MDC values in context");
```

### Thread Safety

Thread safety is something to be aware of when using context fields.  While fields are thread-safe and using a context is far more convenient than using MDC, you do still have to be aware when you are accessing non-thread safe state.

For example, `SimpleDateFormat` is infamously not thread-safe, and so the following code is not safe to use in a multi-threaded context:

```java
private final static DateFormat df = new SimpleDateFormat("yyyyMMdd");

// UNSAFE EXAMPLE
private static final Logger<?> logger =
        LoggerFactory.getLogger()
        .withFields(fb -> fb.onlyString("unsafe_date", df.format(new Date())));
```

## Conditions

Logging conditions can be handled gracefully using `Condition` functions.  A `Condition` will take a `Level` and a `LoggingContext` which will return the fields of the logger.

```java
final Condition errorCondition = new Condition() {
  @Override
  public boolean test(Level level, LoggingContext context) {
    return level.equals(Level.ERROR);
  }
};
```

Conditions can be used either on the logger, on the statement, or against the predicate check.

There are two elemental conditions, `Condition.always()` and `Condition.never()`.  Echopraxia has optimizations for conditions; it will treat `Condition.always()` as a no-op, and return a `NeverLogger` that has no operations for logging.  The JVM can recognize that logging has no effect at all, and will [eliminate the method call as dead code](https://shipilev.net/jvm/anatomy-quarks/27-compiler-blackholes/).

Conditions are a great way to manage diagnostic logging in your application with more flexibility than global log levels can provide. Consider enabling setting your application logging to `DEBUG` i.e. `<logger name="your.application.package" level="DEBUG"/>` and using [conditions to turn on and off debugging as needed](https://tersesystems.com/blog/2019/07/22/targeted-diagnostic-logging-in-production/).  Conditions come with `and`, `or`, and `xor` functionality, and can provide more precise and expressive logging criteria than can be managed with filters and markers.  This is particularly useful when combined with [filters](#filters).

For example, if you want to have logging that only activates during business hours, you can use the following:

```java
import com.tersesystems.echopraxia.Condition;

public class MyBusinessConditions {
  private static final Clock officeClock = Clock.system(ZoneId.of("America/Los_Angeles")) ;

  public Condition businessHoursOnly() {
    return Condition.operational().and(weekdays().and(from9to5()));
  }
  
  public Condition weekdays() {
    return (level, context) -> {
      LocalDate now = LocalDate.now(officeClock);
      final DayOfWeek dayOfWeek = now.getDayOfWeek();
      return ! (dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY));
    };
  }
  
  public Condition from9to5() {
    return (level, context) -> LocalTime.now(officeClock).query(temporal -> {
      // hour is zero based, so adjust for readability
      final int hour = temporal.get(ChronoField.HOUR_OF_DAY) + 1;
      return (hour >= 9) && (hour <= 17); // 8 am to 5 pm
    });
  }
}
```

If you want to log at an operational level most of the time but log debug statements when certain fields show up, use `anyMatch`:

```java
public class DebugConditions {
  public Condition diagnosticOnFields(Set<String> fieldNames) {
    // operational() is "ERROR" and "WARN" and "INFO" levels only
    // diagnostic() is "DEBUG" and "TRACE" levels only
    return Condition.operational().or(
      Condition.diagnostic().and(Condition.anyMatch(f -> fieldNames.contains(f.name())))
    );
  }
}
```

Likewise, if you want things not to be logged if there are some fields set, then use `noneMatch`, or use `valueMatch` if the value of the field is significant:

```java
public class Logging {
  public Condition suppressOnFieldPresence(Set<String> fieldNames) {
    return Condition.noneMatch(f -> fieldNames.contains(f.name()));
  }
  
  public Condition suppressOnFlag() {
    return Condition.valueMatch("suppressFlag", v -> v.type() == BOOLEAN ? (Boolean) v.raw() : false);
  }
}
```

### JSON Path

In situations where you're looking through fields for a condition, using the Java Stream API can be verbose.  As an alternative, you can use [JSONPath](https://github.com/json-path/JsonPath#jayway-jsonpath) to find values from the logging context in a condition.

Tip: You can integrate IntelliJ IDEA with JSONPath using the [inject language](https://www.jetbrains.com/idea/guide/tips/evaluate-json-path-expressions/) settings page and adding `LoggingContext`.


The `context.find` methods take a class as a type, and a [JSON path](https://www.ietf.org/archive/id/draft-ietf-jsonpath-base-03.html), which can be used to search through context fields (or arguments, if the condition is used in a logging statement).

The basic types are `String`, the `Number` subclasses such as `Integer`, and `Boolean`.  If no matching path is found, an empty `Optional` is returned.

```java
Optional<String> optName = context.findString("$.person.name");
```

This also applies to `Throwable` which are usually passed in as arguments:

```java
Optional<Throwable> optThrowable = context.findThrowable();
```

Finding an explicitly null value return a `boolean`:

```java
// fb.onlyNull("keyWithNullValue") sets an explicitly null value
boolean isNull = context.findNull("$.keyWithNullValue");
```

Finding an object will return a `Map`:

```java
Optional<Map<String, ?>> mother = context.findObject("$.person.mother");
```

For a `List`, in the case of an array value or when using indefinite queries:

```java
List<String> interests = context.findList("$.person.mother.interests");
```

You can use [inline predicates](https://github.com/json-path/JsonPath#inline-predicates), which will return a `List` of the results:

```java
final Condition cheapBookCondition =
  (level, context) -> ! context.findList("$.store.book[?(@.price < 10)]").isEmpty();
```

The inline and filter predicates are not available for exceptions. Instead, you must use `filter`:

```java
class FindException {
  void logException() {
    Condition throwableCondition =
      (level, ctx) ->
        ctx.findThrowable()
          .filter(e -> "test message".equals(e.getMessage()))
          .isPresent();
    
    logger.error(throwableCondition, "Error message", new RuntimeException("test message"));
  }
}
```

And you can also use [filter predicates](https://github.com/json-path/JsonPath#filter-predicates) using the `?` placeholder:

```java
import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.*;

class FindWithFilterPredicates {
  void logWithPredicate() {
    Condition cheapBookCondition =
      (level, context) -> {
        Filter cheapFictionFilter = filter(where("category").is("fiction").and("price").lte(10D));
        List<Map<String, ?>> books = context.findList("$.store.book[?]", cheapFictionFilter);
        return books.size() > 0;
      };

    logger.info(
      cheapBookCondition,
      "found cheap books",
      fb -> {
        Field category = fb.string("category", "fiction");
        Field price = fb.number("price", 5);
        Field book = fb.object("book", category, price);
        return fb.onlyObject("store", book);
      });
  }
}
```

There are many more options available using JSONPath.  You can try out the [online evaluator](https://jsonpath.herokuapp.com/) to test out expressions.

### Logger

You can use conditions in a logger, and statements will only log if the condition is met:

```java
Logger<?> loggerWithCondition = logger.withCondition(condition);
```

You can also build up conditions:

```java
Logger<?> loggerWithAandB = logger.withCondition(conditionA).withCondition(conditionB);
```

### Statement

You can use conditions in an individual statement:

```java
logger.info(mustHaveFoo, "Only log if foo is present");
```

### Predicates

Conditions can also be used in predicate blocks for expensive objects.

```java
if (logger.isInfoEnabled(condition)) {
  // only true if condition and is info  
}
```

## Asynchronous Logging

By default, conditions are evaluated in the running thread.  This can be a problem if conditions rely on external elements such as network calls or database lookups, or involve resources with locks.

Echopraxia provides an `AsyncLogger` that will evaluate conditions and log using another executor, so that the main business logic thread is not blocked on execution.  All statements are placed on a work queue and run on a thread specified by the executor at a later time.

All the usual logging statements are available in `AsyncLogger`, i.e. `logger.debug` will log as usual.  

However, there are no `isLogging*` methods in the `AsyncLogger`. Instead, a `Consumer` of `LoggerHandle` is used, which serves the same purpose as the `if (isLogging*()) { .. }` block.

```java
AsyncLogger<?> logger = AsyncLoggerFactory.getLogger().withExecutor(loggingExecutor);
logger.info(handle -> {
  // do conditional logic that would normally happen in an if block
  // this may be expensive or blocking because it runs asynchronously
  handle.log("Message template {}", fb -> fb.onlyString("foo", "bar");
});
```

In the unfortunate event of an exception, the underlying  logger will be called at `error` level from the relevant core logger:

```java
// only happens if uncaught exception from consumer
slf4jLogger.error("Uncaught exception when running asyncLog", cause);
```

One important detail is that the logging executor should be a daemon thread, so that it does not block JVM exit:

```java
private static final Executor loggingExecutor =
  Executors.newSingleThreadExecutor(
      r -> {
        Thread t = new Thread(r);
        t.setDaemon(true); // daemon means the thread won't stop JVM from exiting
        t.setName("logging-thread");
        return t;
      });
```

Using a single thread executor is nice because you can keep ordering of your logging statements, but it may not scale up in production.  Generally speaking, if you are CPU bound and want to distribute load over several cores, you should use `ForkJoinPool.commonPool()` or a bounded fork-join work stealing pool as your executor.  If your conditions involve blocking, or work is IO bound, you should configure a thread pool executor.  

Likewise, if your conditions involve calls to external services (for example, calling Consul or a remote HTTP service), you should consider using a failure handling library like [failsafe](https://failsafe.dev/) to set up appropriate circuit breakers, bulkheads, timeouts, and rate limiters to manage interactions.

Because of parallelism and concurrency, your logging statements may not appear in order if you have multiple threads running in your executor.  You can add extra fields to ensure you can reorder statements appropriately.

Putting it all together:

```java
public class Async {
  private static final ExecutorService loggingExecutor =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // daemon means the thread won't stop JVM from exiting
            t.setName("echopraxia-logging-thread");
            return t;
          });

  private static final Condition expensiveCondition = new Condition() {
    @Override
    public boolean test(Level level, LoggingContext context) {
      try {
        Thread.sleep(1000L);
        return true;
      } catch (InterruptedException e) {
        return false;
      }
    };
  };

  private static final AsyncLogger<?> logger = AsyncLoggerFactory.getLogger()
    .withExecutor(loggingExecutor)
    .withCondition(expensiveCondition);

  public static void main(String[] args) throws InterruptedException {
    System.out.println("BEFORE logging block");
    for (int i = 0; i < 10; i++) {
      // This should take no time on the rendering thread :-)
      logger.info("Prints out after expensive condition");
    }
    System.out.println("AFTER logging block");
    System.out.println("Sleeping so that the JVM stays up");
    Thread.sleep(1001L * 10L);
  }
}
```

### Managing Thread Local State 

Note that because logging is asynchronous, you must be very careful when accessing thread local state.  Thread local state associated with logging, i.e. MDC / ThreadContext is automatically carried through, but in some cases you may need to do additional work.

For example, if you are using Spring Boot and are using `RequestContextHolder.getRequestAttributes()` when constructing context fields, you must call `RequestContextHolder.setRequestAttributes(requestAttributes)` so that the attributes are available to the thread:

```java
public class GreetingController {
  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  private static final AsyncLogger<HttpRequestFieldBuilder> logger =
    AsyncLoggerFactory.getLogger()
      .withFieldBuilder(HttpRequestFieldBuilder.class)
      .withExecutor(ForkJoinPool.commonPool())
      .withFields(
        fb -> {
          // Any fields that you set in context you can set conditions on later,
          // i.e. on the URI path, content type, or extra headers.
          HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
              .getRequest();
          return fb.requestFields(request);
        });

  @GetMapping("/greeting")
  public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
    logger.info(wrap(h -> h.log("This logs asynchronously with HTTP request fields")));
    return new Greeting(counter.incrementAndGet(), String.format(template, name));
  }

  private Consumer<LoggerHandle<HttpRequestFieldBuilder>> wrap(Consumer<LoggerHandle<HttpRequestFieldBuilder>> c) {
    // Because this takes place in the fork-join common pool, we need to set request
    // attributes in the thread before logging so we can get request fields.
    final RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    return h -> {
      try {
        RequestContextHolder.setRequestAttributes(requestAttributes);
        c.accept(h);
      } finally {
        RequestContextHolder.resetRequestAttributes();
      }
    };
  }
}
```

### Managing Caller Info

Caller information -- the caller class, method, and line number -- is typically implemented in frameworks by processing a stacktrace.  This is a problem for asynchronous logging, because the caller information is in a different ~~castle~~thread.

Echopraxia can fix this by capturing the caller information just before starting the async thread, but it must do so on request.  Crucially, although the throwable is created, the processing of stack trace elements still happens on the executor thread, ameliorating the [cost of caller information](https://ionutbalosin.com/2018/06/an-even-faster-way-than-stackwalker-api-for-asynchronously-processing-the-stack-frames/).

You must specifically configure the implementation to capture async caller data.

#### Log4J

For Log4J, you must set `includeLocation=true` on the logger you want to capture caller data, and configure the appender to render caller data:

```xml
<Configuration packages="...">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
        <!-- must have location info enabled to get caller data -->
        <JsonTemplateLayout 
          eventTemplateUri="classpath:JsonLayout.json" 
          locationInfoEnabled="true">
            
        </JsonTemplateLayout>
    </Console>
  </Appenders>
    
  <Loggers>
    <!-- Must set includeLocation=true specifically for async caller info -->
    <Root level="debug" includeLocation="true">
      <AppenderRef ref="Console"/>
    </Root>
</Loggers>
</Configuration>      
```

#### Logback

Logback is a little more complicated, because there is no direct way to get the logging event from a logger.  Instead, a special `caller` marker is added, and an appender is used to extract caller data from the marker and set it on the event, 

To enable it, you must set the context property `echopraxia.async.caller` to `true`, and wrap your appenders with `com.tersesystems.echopraxia.logstash.CallerDataAppender`:

```xml
<configuration>
    
    <property scope="context" name="echopraxia.async.caller" value="true"/>

    <!-- loosen the rule so appenders use appender-refs -->
    <newRule pattern="*/appender/appender-ref"
             actionClass="ch.qos.logback.core.joran.action.AppenderRefAction"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!-- https://logback.qos.ch/manual/layouts.html#caller -->
        <encoder>
            <pattern>%date{H:mm:ss.SSS} %highlight(%-5level) [%thread]: %message%n%ex%caller{2}</pattern>
        </encoder>
    </appender>
    
    <appender name="JSON" class="net.logstash.logback.appender.LoggingEventAsyncDisruptorAppender">
      <appender class="ch.qos.logback.core.FileAppender">
        <filename>application.log</filename>
        <!-- Or https://github.com/logfellow/logstash-logback-encoder#caller-info-fields -->
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
          <providers>
            <!-- must add caller data to see method / line info in json -->
            <callerData/>
          </providers>
        </encoder>
      </appender>
    </appender>

    <root level="INFO">
        <!--
           CallerDataAppender sets the caller data on event from marker.
           Note this depends on setting the newRule above!
         -->
        <appender class="com.tersesystems.echopraxia.logstash.CallerDataAppender">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="JSON"/>
        </appender>
    </root>
</configuration>
```

## Dynamic Conditions with Scripts

One of the limitations of logging is that it's not that easy to change logging levels in an application at run-time.  In modern applications, you typically have complex inputs and may want to enable logging for some very specific inputs without turning on your logging globally.

Script Conditions lets you tie your conditions to scripts that you can change and re-evaluate at runtime.

The security concerns surrounding Groovy or Javascript make them unsuitable in a logging environment.  Fortunately, Echopraxia provides a [Tweakflow](https://twineworks.github.io/tweakflow) script integration that lets you evaluate logging statements **safely**.  Tweakflow comes with a [VS Code integration](https://marketplace.visualstudio.com/items?itemName=twineworks.tweakflow), a [reference guide](https://twineworks.github.io/tweakflow/reference.html), and a [standard library](https://twineworks.github.io/tweakflow/modules/std.html) that contains useful regular expression and date manipulation logic.

Because Scripting has a dependency on Tweakflow, it is broken out into a distinct library that you must add to your build.

Maven:

```
<dependency>
  <groupId>com.tersesystems.echopraxia</groupId>
  <artifactId>scripting</artifactId>
  <version><VERSION></version>
</dependency>
```

Gradle:

```
implementation "com.tersesystems.echopraxia:scripting:<VERSION>" 
```

### String Based Scripts

You also have the option of passing in a string directly:

```java
StringBuilder b = new StringBuilder("");
b.append("library echopraxia {");
b.append("  function evaluate: (string level, dict fields) ->");
b.append("    level == \"INFO\";");
b.append("}");
String scriptString = b.toString();  
Condition c = ScriptCondition.create(false, scriptString, Throwable::printStackTrace);
```

### File Based Scripts

Creating a script condition is done with `ScriptCondition.create`:

```java
import com.tersesystems.echopraxia.scripting.*;

Path path = Paths.get("src/test/tweakflow/condition.tf");
Condition condition = ScriptCondition.create(false, path, Throwable::printStackTrace);

Logger<?> logger = LoggerFactory.getLogger(getClass()).withCondition(condition);
```

Where `condition.tf` contains a tweakflow script, e.g.

```tweakflow
import * as std from "std";
alias std.strings as str;

library echopraxia {
  # level: the logging level
  # fields: the dictionary of fields
  function evaluate: (string level, dict fields) ->
    str.lower_case(fields[:person][:name]) == "will";   
}
```

### Watched Scripts

You can also change file based scripts while the application is running, if they are in a directory watched by `ScriptWatchService`.  

To configure `ScriptWatchService`, pass it the directory that contains your script files:

```java
final Path watchedDir = Paths.get("/your/script/directory");
ScriptWatchService watchService = new ScriptWatchService(watchedDir);

Path filePath = watchedDir.resolve("myscript.tf");

Logger logger = LoggerFactory.getLogger();

final ScriptHandle watchedHandle = watchService.watchScript(filePath, 
        e -> logger.error("Script compilation error", e));
final Condition condition = ScriptCondition.create(watchedHandle);

logger.info(condition, "Statement only logs if condition is met!")
        
// After that, you can edit myscript.tf and the condition will 
// re-evaluate the script as needed automatically!
        
// You can delete the file, but doing so will log a warning from `ScriptWatchService`
// Recreating a deleted file will trigger an evaluation, same as modification.

// Note that the watch service creates a daemon thread to watch the directory.
// To free up the thread and stop watching, you should call close() as appropriate:
watchService.close();
```

## Semantic Logging

Semantic Loggers are strongly typed, and will only log a particular kind of argument.  All the work of field building and
setting up a message is done from setup.

### Basic Usage

To set up a logger for a `Person` with `name` and `age` properties, you would do the following:

```java
import com.tersesystems.echopraxia.semantic.*;

SemanticLogger<Person> logger =
    SemanticLoggerFactory.getLogger(
        getClass(),
        Person.class,
        person -> "person.name = {}, person.age = {}",
        p -> fb -> fb.list(fb.string("name", p.name), fb.number("age", p.age)));

Person person = new Person("Eloise", 1);
logger.info(person);
```

### Conditions

Semantic loggers take conditions in the same way that other loggers do, either through predicate:

```java
if (logger.isInfoEnabled(condition)) {
  logger.info(person);
}
```

or directly on the method:

```java
logger.info(condition, person);
```

or on the logger:

```java
logger.withCondition(condition).info(person);
```

### Context

Semantic loggers can add fields to context in the same way other loggers do.

```java
SemanticLogger<Person> loggerWithContext =
  logger.withFields(fb -> fb.onlyString("some_context_field", contextValue));
```

### Installation

Semantic Loggers have a dependency on the `api` module, but do not have any implementation dependencies.

Maven:

```
<dependency>
  <groupId>com.tersesystems.echopraxia</groupId>
  <artifactId>semantic</artifactId>
  <version><VERSION></version>
</dependency>
```

Gradle:

```
implementation "com.tersesystems.echopraxia:semantic:<VERSION>" 
```

## Fluent Logging

Fluent logging is done using a `FluentLoggerFactory`.  

It is useful in situations where arguments may need to be built up over time.

```java
import com.tersesystems.echopraxia.fluent.*;

FluentLogger<?> logger = FluentLoggerFactory.getLogger(getClass());

Person person = new Person("Eloise", 1);

logger
    .atInfo()
    .message("name = {}, age = {}")
    .argument(sfb -> sfb.string("name", person.name)) // note only a single field
    .argument(sfb -> sfb.number("age", person.age))
    .log();
```

### Installation

Fluent Loggers have a dependency on the `api` module, but do not have any implementation dependencies.

Maven:

```
<dependency>
  <groupId>com.tersesystems.echopraxia</groupId>
  <artifactId>fluent</artifactId>
  <version><VERSION></version>
</dependency>
```

Gradle:

```
implementation "com.tersesystems.echopraxia:fluent:<VERSION>" 
```

## Core Logger 

Because Echopraxia provides its own implementation independent API, some implementation features are not exposed normally.  If you want to use implementation specific features like markers, you will need to use a core logger.

### Logstash API

First, import the `logstash` package and the `core` package.  This gets you access to the `CoreLoggerFactory` and  `CoreLogger`, which can be cast to `LogstashCoreLogger`:

```java
import com.tersesystems.echopraxia.logstash.*;
import com.tersesystems.echopraxia.core.*;

LogstashCoreLogger core = (LogstashCoreLogger) CoreLoggerFactory.getLogger();
```

The `LogstashCoreLogger` has a `withMarkers` method that takes an SLF4J marker:

```java
Logger<?> logger = LoggerFactory.getLogger(
      core.withMarkers(MarkerFactory.getMarker("SECURITY")), Field.Builder.instance);
```

If you have markers set as context, you can evaluate them in a condition through casting to `LogstashLoggingContext`:

```java
Condition hasAnyMarkers = (level, context) -> {
   LogstashLoggingContext c = (LogstashLoggingContext) context;
   List<org.slf4j.Marker> markers = c.getMarkers();
   return markers.size() > 0;
};
```

If you need to get at the SLF4J logger from a core logger, you can cast and call `core.logger()`:

```java
Logger<?> baseLogger = LoggerFactory.getLogger();
LogstashCoreLogger core = (LogstashCoreLogger) baseLogger.core();
org.slf4j.Logger slf4jLogger = core.logger();
```

### Log4J

Similar to Logstash, you can get access to Log4J specific features by importing 

```java
import com.tersesystems.echopraxia.log4j.*;
import com.tersesystems.echopraxia.core.*;

Log4JCoreLogger core = (Log4JCoreLogger) CoreLoggerFactory.getLogger();
```

The `Log4JCoreLogger` has a `withMarker` method that takes a Log4J marker:

```java
final Marker securityMarker = MarkerManager.getMarker("SECURITY");
Logger<?> logger = LoggerFactory.getLogger(
      core.withMarker(securityMarker), Field.Builder.instance);
```

If you have a marker set as context, you can evaluate it in a condition through casting to `Log4JLoggingContext`:

```java
Condition hasAnyMarkers = (level, context) -> {
   Log4JLoggingContext c = (Log4JLoggingContext) context;
   Marker m = c.getMarker();
   return securityMarker.equals(m);
};
```

If you need to get the Log4j logger from a core logger, you can cast and call `core.logger()`:

```java
Logger<?> baseLogger = LoggerFactory.getLogger();
Log4JCoreLogger core = (Log4JCoreLogger) baseLogger.core();
org.apache.logging.log4j.Logger log4jLogger = core.logger();
```

## Filters

There are times when you want to add a field or a condition to all loggers.  Although you can wrap individual loggers or create your own wrapper around `LoggerFactory`, this can be a labor-intensive process that requires lots of code modification, and must be handled for fluent, semantic, async, and regular loggers.

Echopraxia includes filters that wrap around the `CoreLogger` returned by `CoreLoggerFactory` that provides the ability to modify the core logger from a single pipeline in the code.

For example, to add a `uses_filter` field to every Echopraxia logger:

```java
package example;

import com.tersesystems.echopraxia.*;
import com.tersesystems.echopraxia.core.*;

public class ExampleFilter implements CoreLoggerFilter {
  @Override
  public CoreLogger apply(CoreLogger coreLogger) {
    return coreLogger
        .withFields(fb -> fb.onlyBool("uses_filter", true), Field.Builder.instance());
  }
}
```

Filters must extend the `CoreLoggerFilter` interface, and must have a no-args constructor.

Filters must have a fully qualified class name in the `/echopraxia.properties` file as a resource somewhere in your classpath.  The format is `filter.N` where N is the order in which filters should be loaded.

```properties
filter.0=example.ExampleFilter
```

Filters are particularly helpful when you need to provide "out of context" information for your conditions. 

For example, imagine that you have a situation in which the program uses more CPU or memory than normal in production, but works fine in a staging environment.  Using [OSHI](https://github.com/oshi/oshi) and a filter, you can provide the [machine statistics](https://speakerdeck.com/lyddonb/what-is-happening-attempting-to-understand-our-systems?slide=133) and evaluate with dynamic conditions.

```java
public class SystemInfoFilter implements CoreLoggerFilter {

  private final SystemInfo systemInfo;

  public SystemInfoFilter() {
     systemInfo = new SystemInfo();
  }

  @Override
  public CoreLogger apply(CoreLogger coreLogger) {
    HardwareAbstractionLayer hardware = systemInfo.getHardware();
    GlobalMemory mem = hardware.getMemory();
    CentralProcessor proc = hardware.getProcessor();
    double[] loadAverage = proc.getSystemLoadAverage(3);

    // Now you can add conditions based on these fields, and conditionally
    // enable logging based on your load and memory!
    return coreLogger.withFields(fb -> {
        Field loadField = fb.object("load_average", //
                fb.number("1min", loadAverage[0]), //
                fb.number("5min", loadAverage[1]), //
                fb.number("15min", loadAverage[2]));
        Field memField = fb.object("mem", //
                fb.number("available", mem.getAvailable()), //
                fb.number("total", mem.getTotal()));
        Field sysinfoField = fb.object("sysinfo", loadField, memField);
        return fb.only(sysinfoField);
      }, Field.Builder.instance());
  }
}
```

