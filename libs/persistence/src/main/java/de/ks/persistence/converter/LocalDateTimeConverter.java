package de.ks.persistence.converter;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;

/**
 *
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {
  private static final Logger log = LogManager.getLogger(LocalDateTimeConverter.class);

  @Override
  public String convertToDatabaseColumn(LocalDateTime attribute) {
    if (attribute == null) {
      return null;
    } else {
      return attribute.toString();
    }

  }

  @Override
  public LocalDateTime convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    } else {
      return LocalDateTime.parse(dbData);
    }
  }
}
