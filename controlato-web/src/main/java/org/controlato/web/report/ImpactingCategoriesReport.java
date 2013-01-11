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

import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.controlato.finance.business.ExpenditureBean;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.web.controller.OperationFilterMBean;
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
public class ImpactingCategoriesReport {

    private static final Logger LOGGER = Logger.getLogger(ImpactingCategoriesReport.class.getName());

    @EJB
    private ExpenditureBean expenditureBean;

    private CartesianChartModel impactingCategoriesModel;

    private List<CategoryOperation> categoriesWithTotalSpent;

    @ManagedProperty(value="#{operationFilterMBean}")
    private OperationFilterMBean operationFilterMBean;

    public CartesianChartModel getImpactingCategoriesModel() {
        return impactingCategoriesModel;
    }

    public void setImpactingCategoriesModel(CartesianChartModel impactingCategoriesModel) {
        this.impactingCategoriesModel = impactingCategoriesModel;
    }

    public OperationFilterMBean getOperationFilterMBean() {
        return operationFilterMBean;
    }

    public void setOperationFilterMBean(OperationFilterMBean operationFilterMBean) {
        this.operationFilterMBean = operationFilterMBean;
    }

    @PostConstruct
    public void load() {
        operationFilterMBean.setOperationFilterable(expenditureBean);

        categoriesWithTotalSpent = expenditureBean.findTotalSpentByCategory(this.operationFilterMBean.getSelectedYear(), this.operationFilterMBean.getSelectedMonth(), OrderType.DESC);

        loadChartData();
    }

    private void loadChartData() {
        impactingCategoriesModel = new CartesianChartModel();

        ChartSeries impactingCategoriesSeries;
        impactingCategoriesSeries = new ChartSeries();
        impactingCategoriesSeries.setLabel(ResourceBundle.getInstance().getMessage("impactingCategories"));
        for(CategoryOperation category: categoriesWithTotalSpent) {
            impactingCategoriesSeries.set(category.getName(), category.getTotalOperations());
        }

        impactingCategoriesModel.addSeries(impactingCategoriesSeries);
    }
}