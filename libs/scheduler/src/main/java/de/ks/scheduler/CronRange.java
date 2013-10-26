package de.ks.scheduler;


import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;

@Embeddable
@Access(AccessType.FIELD)
@Cron
public class CronRange {
  protected final String content;
  protected final int min;
  protected final int max;

  public CronRange(String content, int min, int max) {
    this.content = content.replaceAll("\\s+", "");
    this.min = min;
    this.max = max;
  }

  public boolean isAny() {
    return content.equals("*");
  }

  public List<Integer> getAny() {
    ArrayList<Integer> retval = new ArrayList<>();
    for (int i = min; i <= max; i++) {
      retval.add(i);
    }
    return retval;
  }

  public boolean isRange() {
    return content.contains("-");
  }

  public List<Integer> getRange() {
    String[] split = content.split("\\-");
    assert split.length == 2;

    ArrayList<Integer> retval = new ArrayList<>();
    for (int i = Integer.valueOf(split[0]); i <= Integer.valueOf(split[1]); i++) {
      retval.add(i);
    }
    return retval;
  }

  public boolean isRate() {
    return content.startsWith("*/");
  }

  public List<Integer> getRate() {
    int dividend = Integer.valueOf(content.substring(2));

    ArrayList<Integer> retval = new ArrayList<>();
    for (int i = min; i <= max; i += dividend) {
      retval.add(i);
    }
    return retval;
  }

  public boolean isList() {
    return content.contains(",");
  }

  public List<Integer> getList() {
    String[] split = content.split("\\,");
    ArrayList<Integer> retval = new ArrayList<>();
    for (String number : split) {
      retval.add(Integer.valueOf(number));
    }
    return retval;
  }

  public boolean isSimple() {
    return !isList() && !isRange() && !isRate() && !isAny() && content.matches("-?\\d+(\\.\\d+)?");
  }

  public int getSimple() {
    return Integer.valueOf(content);

  }

}