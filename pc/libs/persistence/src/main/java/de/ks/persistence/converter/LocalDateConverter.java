package de.ks.persistence.converter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;

/**
 *
 */
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {
  @Override
  public String convertToDatabaseColumn(LocalDate attribute) {
    if (attribute == null) {
      return null;
    } else {
      return attribute.toString();
    }
  }

  @Override
  public LocalDate convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    } else {
    }
    return LocalDate.parse(dbData);
  }
}
