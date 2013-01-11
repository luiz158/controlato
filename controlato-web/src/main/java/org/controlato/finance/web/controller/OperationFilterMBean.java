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
package org.controlato.finance.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.controlato.finance.business.OperationFilterable;
import org.controlato.web.util.MonthDecorator;

/**
 * Session scoped managed bean to save the filters selected by the user during
 * the session and reuse the values to populate other similar filters of
 * operations.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@SessionScoped
public class OperationFilterMBean {

    private static final Logger LOGGER = Logger.getLogger(OperationFilterMBean.class.getName());

    private OperationFilterable operationFilterable;

    private Integer selectedYear;
    private Integer selectedMonth;
    private Integer selectedDay;

    private Integer lastSelectedYear;
    private Integer lastSelectedMonth;

    private List<Integer> years;
    private List<MonthDecorator> months;
    private List<Integer> days;

    public OperationFilterMBean() {
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        selectedYear = today.get(Calendar.YEAR);
        selectedMonth = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(Integer selectedYear) {
        if(this.selectedYear == null) {
            this.selectedYear = selectedYear;
        }
        else {
            this.lastSelectedYear = this.selectedYear;

            if(!this.lastSelectedYear.equals(selectedYear)) {
                this.selectedYear = selectedYear;
                this.selectedMonth = null;
                this.selectedDay = null;
            }
        }
    }

    public Integer getSelectedMonth() {
        return selectedMonth;
    }

    public void setSelectedMonth(Integer selectedMonth) {
        if(this.selectedMonth == null) {
            this.selectedMonth = selectedMonth;
        }
        else {
            this.lastSelectedMonth = this.selectedMonth;

            if(!this.lastSelectedMonth.equals(selectedMonth)) {
                this.selectedMonth = selectedMonth;
                this.selectedDay = null;
            }
        }
        this.selectedMonth = selectedMonth;
    }

    public Integer getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(Integer selectedDay) {
        this.selectedDay = selectedDay;
    }

    public List<Integer> getYears() {
        return this.years;
    }

    public List<MonthDecorator> getMonths() {
        if(this.selectedYear != null && !this.selectedYear.equals(this.lastSelectedYear)) {
            updateMonths();
            this.lastSelectedYear = this.selectedYear;
        }
        return this.months;
    }

    public List<Integer> getDays() {
        if(this.selectedMonth != null && !this.selectedMonth.equals(this.lastSelectedMonth)) {
            updateDays();
            this.lastSelectedMonth = this.selectedMonth;
        }
        return this.days;
    }

    public void setOperationFilterable(OperationFilterable operationFilterable) {
        this.operationFilterable = operationFilterable;
        this.updateAll();
    }

    @PostConstruct
    public void load() {
        LOGGER.log(Level.INFO, "OperationFilterMBean loaded");
        updateAll();
    }

    /**
     * Updates all filters based on the informed operations. Usually called after
     * a change in one of the operations, which may change filter values.
     */
    public void updateAll() {
        updateYears();
        updateMonths();
        updateDays();
    }

    private void updateYears() {
        if(operationFilterable == null) {
            return;
        }
        this.years = operationFilterable.findYears();
    }

    private void updateMonths() {
        if(selectedYear == null || operationFilterable == null) {
            return;
        }

        List<Integer> rawMonths = operationFilterable.findMonths(selectedYear);
        this.months = new ArrayList<>();
        for(Integer rawMonth: rawMonths) {
            this.months.add(MonthDecorator.getInstance(rawMonth));
        }
    }

    private void updateDays() {
        if(selectedYear == null || selectedMonth == null || operationFilterable == null) {
            return;
        }

        this.days = operationFilterable.findDays(selectedYear, selectedMonth);
    }
}