package com.inmobi.databus.utils;
/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarHelper {
  static Logger logger = Logger.getLogger(CalendarHelper.class);

  // TODO - all date/time should be returned in a common time zone GMT

  public static Calendar getDate(String year, String month, String day) {
    return new GregorianCalendar(new Integer(year).intValue(), new Integer(
        month).intValue() - 1, new Integer(day).intValue());
  }

  public static Calendar getDate(Integer year, Integer month, Integer day) {
    return new GregorianCalendar(year.intValue(), month.intValue() - 1,
        day.intValue());
  }

  public static String getCurrentMinute() {
    Calendar calendar;
    calendar = new GregorianCalendar();
    String minute = Integer.toString(calendar.get(Calendar.MINUTE));
    return minute;
  }

  public static Calendar getNowTime() {
    return new GregorianCalendar();
  }

  private static String getCurrentDayTimeAsString(boolean includeHourMinute) {
    Calendar calendar;
    String minute = null;
    String hour = null;
    String fileNameInnYYMMDDHRMNFormat = null;
    calendar = new GregorianCalendar();
    String year = Integer.toString(calendar.get(Calendar.YEAR));
    String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
    String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
    if (includeHourMinute) {
      hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
      minute = Integer.toString(calendar.get(Calendar.MINUTE));
    }
    if (includeHourMinute)
      fileNameInnYYMMDDHRMNFormat = year + "-" + month + "-" + day + "-" + hour
          + "-" + minute;
    else
      fileNameInnYYMMDDHRMNFormat = year + "-" + month + "-" + day;
    logger.debug("getCurrentDayTimeAsString ::  ["
        + fileNameInnYYMMDDHRMNFormat + "]");
    return fileNameInnYYMMDDHRMNFormat;

  }

  public static String getCurrentDayTimeAsString() {
    return getCurrentDayTimeAsString(true);
  }

  public static String getCurrentDateAsString() {
    return getCurrentDayTimeAsString(false);
  }

}
