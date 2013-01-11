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
package org.controlato.web.util;

/**
 *
 * @author Hildeberto Mendonça
 */
public class MonthDecorator {

    private static final String[] names = {"january","february","march","april","may","june","july","august","september","october","november","december"};
    private static final String[] shortNames = {"januaryShort", "februaryShort","marchShort","aprilShort","mayShort","juneShort","julyShort","augustShort","septemberShort","octoberShort","novemberShort","decemberShort"};

    private Integer number;

    private MonthDecorator(Integer number) {
        this.number = number;
    }

    public static MonthDecorator getInstance(Integer month) {
        return new MonthDecorator(month);
    }

    /** Returns the logic number of the month (the calendar number minus 1) for
        internal purposes. This number should not appear to the end-user. */
    public Integer getNumber() {
        return this.number;
    }

    public String getName() {
        if(number >= 0 && number < 12) {
            ResourceBundle bundle = ResourceBundle.getInstance();
            return bundle.getMessage(names[number]);
        }
        else {
            return "";
        }
    }

    public String getShortName() {
        if(number >= 0 && number < 12) {
            ResourceBundle bundle = ResourceBundle.getInstance();
            return bundle.getMessage(shortNames[number]);
        }
        else {
            return "";
        }
    }

    @Override
    public String toString() {
        return this.getName();
    }
}