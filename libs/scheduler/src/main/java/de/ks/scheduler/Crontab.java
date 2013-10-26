package de.ks.scheduler;

import javax.persistence.*;

/**
 * Used to store schedule information in an entity.
 */
@Embeddable
@Access(AccessType.FIELD)
public class Crontab {
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "content", column = @Column(name = "min_content")), //
          @AttributeOverride(name = "min", column = @Column(name = "min_min")), //
          @AttributeOverride(name = "max", column = @Column(name = "min_max")) //
  })
  @Cron
  protected CronRange minutes;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "content", column = @Column(name = "h_content")), //
          @AttributeOverride(name = "min", column = @Column(name = "h_min")), //
          @AttributeOverride(name = "max", column = @Column(name = "h_max")) //
  })
  @Cron
  protected CronRange hours;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "content", column = @Column(name = "dom_content")), //
          @AttributeOverride(name = "min", column = @Column(name = "dom_min")), //
          @AttributeOverride(name = "max", column = @Column(name = "dom_max")) //
  })
  @Cron
  protected CronRange dayOfMonth;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "content", column = @Column(name = "month_content")), //
          @AttributeOverride(name = "min", column = @Column(name = "month_min")), //
          @AttributeOverride(name = "max", column = @Column(name = "month_max")) //
  })
  @Cron
  protected CronRange month;
  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "content", column = @Column(name = "dow_content")), //
          @AttributeOverride(name = "min", column = @Column(name = "dow_min")), //
          @AttributeOverride(name = "max", column = @Column(name = "dow_max")) //
  })
  @Cron
  protected CronRange dayOfWeek;

  protected Crontab() {
    //
  }

  public Crontab(String crontab) {
    String[] split = crontab.split(" ");
    assert split.length == 5;

    minutes = new CronRange(split[0], 0, 59);
    hours = new CronRange(split[1], 0, 23);
    dayOfMonth = new CronRange(split[2], 1, 31);
    month = new CronRange(split[3], 1, 12);
    dayOfWeek = new CronRange(split[4], 0, 6);
  }

  public CronRange getMinutes() {
    return minutes;
  }

  public CronRange getHours() {
    return hours;
  }

  public CronRange getDayOfMonth() {
    return dayOfMonth;
  }

  public CronRange getMonth() {
    return month;
  }

  public CronRange getDayOfWeek() {
    return dayOfWeek;
  }

  public Temporal getTemporal() {
    return null;
  }
}
