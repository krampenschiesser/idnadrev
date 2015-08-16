/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.entity.cost;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class BookingCsvTemplate extends NamedPersistentObject<BookingCsvTemplate> {
  private static final long serialVersionUID = 1L;
  protected String separator;
  protected String amountColumns;
  protected int descriptionColumn;
  protected int dateColumn;
  protected Integer timeColumn;
  protected String timePattern;
  protected String datePattern;
  protected boolean useComma;

  @ManyToOne(optional = false)
  @NotNull
  protected Account account;

  protected BookingCsvTemplate() {
  }

  public BookingCsvTemplate(String name) {
    super(name);
  }

  public String getSeparator() {
    return separator;
  }

  public BookingCsvTemplate setSeparator(String separator) {
    this.separator = separator;
    return this;
  }

  public String getAmountColumnString() {
    return amountColumns;
  }

  public BookingCsvTemplate setAmountColumnString(String amountColumns) {
    this.amountColumns = amountColumns;
    return this;
  }

  public List<Integer> getAmountColumns() {
    String[] split = amountColumns.split("\\,");
    List<Integer> collect = Arrays.asList(split).stream().mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
    return collect;
  }

  public BookingCsvTemplate setAmountColumns(List<Integer> amountColumns) {
    this.amountColumns = amountColumns.stream().map(String::valueOf).collect(Collectors.joining(","));
    return this;
  }

  public int getDescriptionColumn() {
    return descriptionColumn;
  }

  public BookingCsvTemplate setDescriptionColumn(int descriptionColumn) {
    this.descriptionColumn = descriptionColumn;
    return this;
  }

  public int getDateColumn() {
    return dateColumn;
  }

  public BookingCsvTemplate setDateColumn(int dateColumn) {
    this.dateColumn = dateColumn;
    return this;
  }

  public Integer getTimeColumn() {
    return timeColumn;
  }

  public BookingCsvTemplate setTimeColumn(int timeColumn) {
    this.timeColumn = timeColumn;
    return this;
  }

  public String getTimePattern() {
    return timePattern;
  }

  public BookingCsvTemplate setTimePattern(String timePattern) {
    this.timePattern = timePattern;
    return this;
  }

  public String getDatePattern() {
    return datePattern;
  }

  public BookingCsvTemplate setDatePattern(String datePattern) {
    this.datePattern = datePattern;
    return this;
  }

  public Account getAccount() {
    return account;
  }

  public BookingCsvTemplate setAccount(Account account) {
    this.account = account;
    return this;
  }

  public boolean isUseComma() {
    return useComma;
  }

  public BookingCsvTemplate setUseComma(boolean useComma) {
    this.useComma = useComma;
    return this;
  }
}
