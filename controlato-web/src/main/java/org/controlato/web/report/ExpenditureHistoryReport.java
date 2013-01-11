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
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.controlato.finance.business.ExpenditureBean;
import org.controlato.finance.entity.Expenditure;
import org.controlato.util.OrderType;
import org.controlato.web.util.ResourceBundle;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class ExpenditureHistoryReport {

    @EJB
    private ExpenditureBean expenditureBean;

    private CartesianChartModel monthlyHistoryReportModel;

    public CartesianChartModel getMonthlyHistoryReportModel() {
        return monthlyHistoryReportModel;
    }

    public void setMonthlyHistoryReportModel(CartesianChartModel monthlyHistoryReportModel) {
        this.monthlyHistoryReportModel = monthlyHistoryReportModel;
    }

    @PostConstruct
    public void load() {
        monthlyHistoryReportModel = new CartesianChartModel();

        ResourceBundle bundle = ResourceBundle.getInstance();
        String[] months = {bundle.getMessage("januaryShort"),
                            bundle.getMessage("februaryShort"),
                            bundle.getMessage("marchShort"),
                            bundle.getMessage("aprilShort"),
                            bundle.getMessage("mayShort"),
                            bundle.getMessage("juneShort"),
                            bundle.getMessage("julyShort"),
                            bundle.getMessage("augustShort"),
                            bundle.getMessage("septemberShort"),
                            bundle.getMessage("octoberShort"),
                            bundle.getMessage("novemberShort"),
                            bundle.getMessage("decemberShort")};

        Calendar lastDay = Calendar.getInstance();
        lastDay.set(Calendar.MONTH, Calendar.DECEMBER);
        lastDay.set(Calendar.DATE, 31);
        lastDay.set(Calendar.HOUR, 11);
        lastDay.set(Calendar.MINUTE, 59);
        lastDay.set(Calendar.SECOND, 59);
        lastDay.set(Calendar.AM_PM, Calendar.PM);
        Date to = lastDay.getTime();

        Calendar firstDay = lastDay;
        firstDay.add(Calendar.YEAR, -1);
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        firstDay.set(Calendar.MONTH, 0);
        firstDay.set(Calendar.HOUR, 0);
        firstDay.set(Calendar.MINUTE, 0);
        lastDay.set(Calendar.SECOND, 0);
        lastDay.set(Calendar.AM_PM, Calendar.AM);
        Date from = firstDay.getTime();

        List<Expenditure> expenditures = expenditureBean.findExpendituresInInterval(from, to, OrderType.ASC);

        Calendar debitDate;
        BigDecimal[][] data = new BigDecimal[2][12];

        for(int i = 0;i < 2;i++) {
            for(int j = 0;j < 12;j++) {
                data[i][j] = BigDecimal.ZERO;
            }
        }

        int year, month, currentYear = 0;
        for(Expenditure expenditure: expenditures) {
            debitDate = Calendar.getInstance();
            debitDate.setTime(expenditure.getDebitDate());
            if(currentYear == 0) {
                currentYear = debitDate.get(Calendar.YEAR);
            }

            year = debitDate.get(Calendar.YEAR) - currentYear;
            month = debitDate.get(Calendar.MONTH);
            data[year][month] = data[year][month].add(expenditure.getAmount());
        }

        ChartSeries monthlyHistorySeries;
        for(int i = 0;i < 2;i++) {
            monthlyHistorySeries = new ChartSeries();
            monthlyHistorySeries.setLabel(String.valueOf(currentYear + i));
            for(int j = 0;j < 12;j++) {
                monthlyHistorySeries.set(months[j], data[i][j]);
            }
            monthlyHistoryReportModel.addSeries(monthlyHistorySeries);
        }
    }
}