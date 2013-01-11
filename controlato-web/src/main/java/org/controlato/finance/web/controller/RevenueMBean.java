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

import java.math.BigDecimal;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.controlato.finance.business.AccountBean;
import org.controlato.finance.business.CategoryOperationBean;
import org.controlato.finance.business.PlaceBean;
import org.controlato.finance.business.RevenueBean;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.entity.Place;
import org.controlato.finance.entity.Revenue;
import org.controlato.finance.entity.TransactionType;
import org.controlato.web.util.MonthDecorator;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class RevenueMBean {

    @EJB
    private RevenueBean revenueBean;

    @EJB
    private AccountBean accountBean;

    @EJB
    private CategoryOperationBean categoryOperationBean;

    @EJB
    private PlaceBean placeBean;

    private Revenue revenue;

    private List<Revenue> revenues;

    private List<Account> accounts;
    private List<CategoryOperation> categories;
    private List<Place> payers;

    private String selectedAccount;
    private String selectedCategory;
    private String selectedPayer;

    private BigDecimal accountBalance = BigDecimal.ZERO;
    private BigDecimal accountBalanceAfterReceiving = BigDecimal.ZERO;
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @ManagedProperty(value="#{param.id}")
    private String id;

    @ManagedProperty(value="#{operationFilterMBean}")
    private OperationFilterMBean operationFilterMBean;

    public RevenueMBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OperationFilterMBean getOperationFilterMBean() {
        return operationFilterMBean;
    }

    public void setOperationFilterMBean(OperationFilterMBean operationFilterMBean) {
        this.operationFilterMBean = operationFilterMBean;
    }

    public Revenue getRevenue() {
        return revenue;
    }

    public void setRevenue(Revenue revenue) {
        this.revenue = revenue;
    }

    public List<Account> getAccounts() {
        if(this.accounts == null) {
            this.accounts = accountBean.findAccounts();
        }
        return this.accounts;
    }

    public List<CategoryOperation> getCategories() {
        if(this.categories == null) {
            this.categories = categoryOperationBean.findCategoriesOperation(TransactionType.CREDIT);
        }
        return categories;
    }

    public List<Place> getPayers() {
        if(this.payers == null) {
            this.payers = placeBean.findPlaces();
        }
        return payers;
    }

    public List<Revenue> getRevenues() {
        if(this.revenues == null) {
            this.revenues = revenueBean.findRevenues(operationFilterMBean.getSelectedYear(),
                                                     operationFilterMBean.getSelectedMonth(),
                                                     operationFilterMBean.getSelectedDay());
        }
        return this.revenues;
    }

    public BigDecimal getTotalRevenue() {
        calculateTotalRevenue();
        return totalRevenue;
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getSelectedPayer() {
        return selectedPayer;
    }

    public void setSelectedPayer(String selectedPayer) {
        this.selectedPayer = selectedPayer;
    }

    private void calculateTotalRevenue() {
        if(this.totalRevenue == BigDecimal.ZERO) {
            this.revenues = getRevenues();
        }
        else { return; }

        for(Revenue rev: this.revenues) {
            this.totalRevenue = this.totalRevenue.add(rev.getAmount());
        }
    }

    public BigDecimal getAccountBalance() {
        if(accountBalance == null || accountBalance.equals(BigDecimal.ZERO)) {
            Account account = accountBean.findAccount(this.selectedAccount);
            if(account != null) {
                accountBalance = account.getBalance();
            }
        }
        return accountBalance;
    }

    public BigDecimal getAccountBalanceAfterPayment() {
        BigDecimal ab = this.getAccountBalance();

        if(this.accountBalanceAfterReceiving.equals(BigDecimal.ZERO) && this.revenue.getAmount() != null && ab != null) {
            this.accountBalanceAfterReceiving = ab.subtract(this.revenue.getAmount());
        }
        return this.accountBalanceAfterReceiving;
    }

    public List<Integer> getYears() {
        return operationFilterMBean.getYears();
    }

    public List<MonthDecorator> getMonths() {
        return operationFilterMBean.getMonths();
    }

    public List<Integer> getDays() {
        return operationFilterMBean.getDays();
    }

    @PostConstruct
    public void load() {

        operationFilterMBean.setOperationFilterable(revenueBean);

        if(id != null && !id.isEmpty()) {
            this.revenue = revenueBean.findRevenue(id);
            if(this.revenue != null) {
                this.selectedAccount = this.revenue.getAccount().getId();
                this.selectedCategory = this.revenue.getCategory().getId();
                this.selectedPayer = this.revenue.getPayer().getId();
            }
            else {
                ResourceBundle bundle = ResourceBundle.getInstance();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getMessage("infoRecordNotFound")));
            }
        }
        else {
            this.revenue = new Revenue();
        }
    }

    public void checkRevenue(Revenue revenue) {
        revenueBean.checkRevenue(revenue);
    }

    public String save() {
        if(this.selectedAccount != null && !this.selectedAccount.isEmpty()) {
            this.revenue.setAccount(accountBean.findAccount(this.selectedAccount));
        }

        if(!this.selectedCategory.isEmpty()) {
            this.revenue.setCategory(categoryOperationBean.findCategoryOperation(this.selectedCategory));
        }

        if(!this.selectedPayer.isEmpty()) {
            this.revenue.setPayer(placeBean.findPlace(this.selectedPayer));
        }

        revenueBean.save(this.revenue);

        operationFilterMBean.updateAll();

        return "revenues?faces-redirect=true";
    }

    public String remove() {
        revenueBean.remove(this.revenue.getId());
        return "revenues?faces-redirect=true";
    }
}