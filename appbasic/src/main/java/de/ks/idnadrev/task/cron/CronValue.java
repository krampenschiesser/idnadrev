/*
 * Copyright [2016] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.task.cron;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class CronValue {
  int min, max;
  List<Integer> values = new ArrayList<>();
  boolean any;
  boolean all;
  boolean noValue;
  boolean noValueAllowed;

  public CronValue(int min, int max) {
    this(min, max, false);
  }

  public CronValue(int min, int max, boolean noValueAllowed) {
    this.min = min;
    this.max = max;
    this.noValueAllowed = noValueAllowed;
  }

  public CronValue setNoValue(boolean noValue) {
    this.noValue = noValue;
    return this;
  }

  public boolean isNoValue() {
    return noValue;
  }

  public boolean hasSingleValue() {
    return values.size() == 1;
  }

  public boolean isAll() {
    return all;
  }

  public boolean isAny() {
    return any;
  }

  public List<Integer> getValues() {
    return values;
  }

  public int getValue() {
    return values.get(0);
  }

  public void parse(String input) {
    values.clear();
    all = false;
    any = false;
    noValue = false;
    if (input.equals("*")) {
      all = true;
    } else if (input.equals("?")) {
      any = true;
    } else if (input.equals("_")) {
      noValue = true;
    } else {
      for (String value : StringUtils.split(input, ",")) {
        Integer val = Integer.valueOf(value);
        if (val < min || val > max) {
          throw new IllegalArgumentException("value out of range");
        }
        values.add(val);
      }
    }
  }

  public int getClosestValue(int value) {
    if (isAny() || isAll()) {
      return value;
    } else {
      ArrayList<Integer> sorted = new ArrayList<>(values);
      sorted.sort(Comparator.comparing(cur -> cur - value));
      sorted.removeIf(cur -> cur - value < 0);
      if (sorted.isEmpty()) {
        return (-1) * values.get(0);
      } else {
        return sorted.get(0);
      }
    }
  }
}
