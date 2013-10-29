package de.ks.persistence.converter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalTime;

/**
 *
 */
@Converter(autoApply = true)
public class LocalTimeConverter implements AttributeConverter<LocalTime, String> {
  @Override
  public String convertToDatabaseColumn(LocalTime attribute) {
    if (attribute == null) {
      return null;
    } else {
      return attribute.toString();
    }
  }

  @Override
  public LocalTime convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    } else {
      return LocalTime.parse(dbData);
    }
  }
}
