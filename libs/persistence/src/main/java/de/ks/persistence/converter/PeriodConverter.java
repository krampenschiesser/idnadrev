package de.ks.persistence.converter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Period;

/**
 *
 */
@Converter(autoApply = true)
public class PeriodConverter implements AttributeConverter<Period, String> {
  @Override
  public String convertToDatabaseColumn(Period attribute) {
    if (attribute == null) {
      return null;
    } else {
      return attribute.toString();
    }
  }

  @Override
  public Period convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    } else {
    }
    return Period.parse(dbData);
  }
}