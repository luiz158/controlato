/* Controlato is a web-based service conceived and designed to assist families
 * on the control of their daily lives.
 * Copyright (C) 2012 Controlato.org
 *
 * Controlato is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Controlato is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with Controlato. Look for the file LICENSE.txt at the root level.
 * If you do not, see http://www.gnu.org/licenses/.
 * */
package org.controlato.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class CalendarUtils {

    public static final int[] NUMBER_DAYS_MONTH = {31,28,31,30,31,30,31,31,30,31,30,31};

    public static final int FROM = 0;
    public static final int TO = 1;

    public static Calendar[] buildInterval(Integer year, Integer month, Integer day) {
        Calendar from = Calendar.getInstance(TimeZone.getDefault());
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);

        Calendar to = Calendar.getInstance(TimeZone.getDefault());
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);

        if(year != null) {
            from.set(Calendar.YEAR, year.intValue());
            from.set(Calendar.MONTH, Calendar.JANUARY);
            from.set(Calendar.DAY_OF_MONTH, 1);

            to.set(Calendar.YEAR, year.intValue());
            to.set(Calendar.MONTH, Calendar.DECEMBER);
            to.set(Calendar.DAY_OF_MONTH, 31);
        }

        if(month != null) {
            from.set(Calendar.MONTH, month.intValue());
            from.set(Calendar.DAY_OF_MONTH, 1);

            to.set(Calendar.MONTH, month.intValue());
            to.set(Calendar.DAY_OF_MONTH, CalendarUtils.NUMBER_DAYS_MONTH[month.intValue()]);
        }

        if(day != null) {
            from.set(Calendar.DAY_OF_MONTH, day.intValue());
            to.set(Calendar.DAY_OF_MONTH, day.intValue());
        }

        Calendar[] interval = new Calendar[2];
        interval[FROM] = from;
        interval[TO] = to;
        return interval;
    }
}