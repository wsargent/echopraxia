package com.tersesystems.echopraxia.logstash;

import com.tersesystems.echopraxia.Field;
import com.tersesystems.echopraxia.LoggingContext;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Marker;

/**
 * Logstash logging context implementation.
 *
 * <p>Note that this makes field evaluation lazy so that functions can pull things out of a thread
 * local (typically hard to do if when loggers are set up initially).
 */
public class LogstashLoggingContext implements LoggingContext {

  protected final Supplier<List<Field>> fieldsSupplier;
  protected final Supplier<List<Marker>> markersSupplier;

  protected LogstashLoggingContext(Supplier<List<Field>> f, Supplier<List<Marker>> m) {
    this.fieldsSupplier = f;
    this.markersSupplier = m;
  }

  @Override
  public List<Field> getFields() {
    return fieldsSupplier.get();
  }

  public List<Marker> getMarkers() {
    return markersSupplier.get();
  }

  /**
   * Joins the two contexts together, concatenating the lists in a supplier function.
   *
   * @param context the context to join
   * @return the new context containing fields and markers from both.
   */
  public LogstashLoggingContext and(LogstashLoggingContext context) {
    if (context != null) {
      Supplier<List<Field>> joinedFields;
      final List<Field> thisFields = LogstashLoggingContext.this.getFields();
      final List<Field> ctxFields = context.getFields();
      if (thisFields.isEmpty()) {
        joinedFields = () -> ctxFields;
      } else if (ctxFields.isEmpty()) {
        joinedFields = () -> thisFields;
      } else {
        joinedFields =
            () ->
                Stream.concat(thisFields.stream(), ctxFields.stream()).collect(Collectors.toList());
      }

      final List<Marker> markers = context.getMarkers();
      final List<Marker> thisMarkers = LogstashLoggingContext.this.getMarkers();
      Supplier<List<Marker>> joinedMarkers;
      if (markers.isEmpty()) {
        joinedMarkers = () -> thisMarkers;
      } else if (thisMarkers.isEmpty()) {
        joinedMarkers = () -> markers;
      } else {
        joinedMarkers =
            () ->
                Stream.concat(thisMarkers.stream(), markers.stream()).collect(Collectors.toList());
      }
      return new LogstashLoggingContext(joinedFields, joinedMarkers);
    } else {
      return this;
    }
  }
}
