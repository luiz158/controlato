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
package org.controlato.web.report;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.controlato.finance.business.ExpenditureBean;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.entity.Expenditure;
import org.controlato.util.CalendarUtils;
import org.controlato.web.util.MonthDecorator;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class CategoryHistoryReport {

    private static final Logger LOGGER = Logger.getLogger(CategoryHistoryReport.class.getName());
    public static final int NUMBER_YEARS = 2;
    public static final int NUMBER_MONTHS = 12;

    @EJB
    private ExpenditureBean expenditureBean;

    private CartesianChartModel categoryHistoryMontlyModel;

    private List<Expenditure> expenses;
    private Integer[] years = new Integer[NUMBER_YEARS];
    private BigDecimal[][] data;
    private CategoryOperation categoryOperation;

    private int selectedSeries;
    private int selectedItem;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private Integer selectedYear;
    private Integer selectedMonth;

    @ManagedProperty(value="#{param.id}")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CategoryOperation getCategoryOperation() {
        return categoryOperation;
    }

    public void setCategoryOperation(CategoryOperation categoryOperation) {
        this.categoryOperation = categoryOperation;
    }

    public List<Expenditure> getExpenses() {
        if(this.expenses == null || this.expenses.isEmpty()) {
            loadDetailedExpenses();
        }
        return this.expenses;
    }

    public CartesianChartModel getCategoryHistoryMontlyModel() {
        return categoryHistoryMontlyModel;
    }

    public void setCategoryHistoryMontlyModel(CartesianChartModel categoryHistoryMontlyModel) {
        this.categoryHistoryMontlyModel = categoryHistoryMontlyModel;
    }

    public String getSelectedMonth() {
        return this.selectedMonth == null ? "" : MonthDecorator.getInstance(this.selectedMonth).getName();
    }

    public String getSelectedYear() {
        return this.selectedYear == null? "" : " - " + String.valueOf(this.selectedYear);
    }

    public BigDecimal getTotalExpenses() {
        return calculateTotalExpenses();
    }

    @PostConstruct
    public void load() {
        if(this.id == null) {
            this.categoryOperation = new CategoryOperation();
            return;
        }
        else {
            this.categoryOperation = new CategoryOperation(this.id);
        }
        loadChartData();
    }

    private void loadChartData() {
        categoryHistoryMontlyModel = new CartesianChartModel();

        Calendar lastDay = Calendar.getInstance();
        lastDay.set(Calendar.MONTH, Calendar.DECEMBER);
        lastDay.set(Calendar.DATE, 31);
        lastDay.set(Calendar.HOUR, 11);
        lastDay.set(Calendar.MINUTE, 59);
        lastDay.set(Calendar.SECOND, 59);
        lastDay.set(Calendar.AM_PM, Calendar.PM);
        Date to = lastDay.getTime();
        years[1] = lastDay.get(Calendar.YEAR);

        Calendar firstDay = lastDay;
        firstDay.add(Calendar.YEAR, -1);
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        firstDay.set(Calendar.MONTH, 0);
        firstDay.set(Calendar.HOUR, 0);
        firstDay.set(Calendar.MINUTE, 0);
        firstDay.set(Calendar.SECOND, 0);
        firstDay.set(Calendar.AM_PM, Calendar.AM);
        Date from = firstDay.getTime();
        years[0] = firstDay.get(Calendar.YEAR);

        // Load expenses within the last 2 years from the database.
        List<Expenditure> chartExpenses = expenditureBean.findExpenditures(this.categoryOperation, from, to);

        Calendar debitDate;
        data = new BigDecimal[NUMBER_YEARS][NUMBER_MONTHS];

        // Initializes the data matrix to avoid null pointer exception.
        for(int i = 0;i < NUMBER_YEARS;i++) {
            for(int j = 0;j < NUMBER_MONTHS;j++) {
                data[i][j] = BigDecimal.ZERO;
            }
        }

        /* Populates the matrix with data from the database. The idea is to
         * organize the data before creating the chart's series. */
        int yearIndex, monthIndex;
        for(Expenditure expense: chartExpenses) {
            debitDate = Calendar.getInstance();
            debitDate.setTime(expense.getDebitDate());

            yearIndex = (debitDate.get(Calendar.YEAR) == years[0]) ? 0 : 1;
            monthIndex = debitDate.get(Calendar.MONTH);
            data[yearIndex][monthIndex] = data[yearIndex][monthIndex].add(expense.getAmount());
        }

        /* Create chart series for each loaded year and add those series to the
         * chart model. The chart model is the object used by the JSF tag to
         * build the chart. */
        ChartSeries categoryHistoryMontlySeries;
        for(int i = 0;i < NUMBER_YEARS;i++) {
            categoryHistoryMontlySeries = new ChartSeries();
            categoryHistoryMontlySeries.setLabel(String.valueOf(years[i]));
            for(int j = 0;j < NUMBER_MONTHS;j++) {
                categoryHistoryMontlySeries.set(MonthDecorator.getInstance(j).getShortName(), data[i][j]);
            }
            categoryHistoryMontlyModel.addSeries(categoryHistoryMontlySeries);
        }
    }

    /**
     * Invoked when the user clicks on one of the chart's columns.
     */
    public void itemSelected(ItemSelectEvent itemSelectEvent) {
        this.selectedSeries = itemSelectEvent.getSeriesIndex();
        this.selectedItem = itemSelectEvent.getItemIndex();
        loadDetailedExpenses();
    }

    private void loadDetailedExpenses() {
        if(data == null) {
            loadChartData();
        }

        Calendar lastDay = Calendar.getInstance();
        if(this.selectedSeries == 0) {
            lastDay.add(Calendar.YEAR, -1);
        }

        lastDay.set(Calendar.MONTH, selectedItem);
        lastDay.set(Calendar.DATE, CalendarUtils.NUMBER_DAYS_MONTH[selectedItem]);
        lastDay.set(Calendar.HOUR, 11);
        lastDay.set(Calendar.MINUTE, 59);
        lastDay.set(Calendar.SECOND, 59);
        lastDay.set(Calendar.AM_PM, Calendar.PM);
        Date to = lastDay.getTime();

        this.selectedYear = lastDay.get(Calendar.YEAR);
        this.selectedMonth = lastDay.get(Calendar.MONTH);

        Calendar firstDay = lastDay;
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        firstDay.set(Calendar.MONTH, selectedItem);
        firstDay.set(Calendar.HOUR, 0);
        firstDay.set(Calendar.MINUTE, 0);
        lastDay.set(Calendar.SECOND, 0);
        lastDay.set(Calendar.AM_PM, Calendar.AM);
        Date from = firstDay.getTime();

        this.expenses = expenditureBean.findExpenditures(this.categoryOperation, from, to);
    }

    private BigDecimal calculateTotalExpenses() {
        if(this.totalExpenses == BigDecimal.ZERO) {
            this.expenses = getExpenses();
        }
        else { return BigDecimal.ZERO; }

        for(Expenditure expense: this.expenses) {
            this.totalExpenses = this.totalExpenses.add(expense.getAmount());
        }
        return this.totalExpenses;
    }
}