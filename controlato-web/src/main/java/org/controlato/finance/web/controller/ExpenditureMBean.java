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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.controlato.finance.business.AccountBean;
import org.controlato.finance.business.CategoryOperationBean;
import org.controlato.finance.business.ExpenditureBean;
import org.controlato.finance.business.PlaceBean;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.entity.Expenditure;
import org.controlato.finance.entity.Place;
import org.controlato.finance.entity.TransactionType;
import org.controlato.web.util.MonthDecorator;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class ExpenditureMBean {

    public static final Logger LOGGER = Logger.getLogger(CategoryOperationMBean.class.getName());

    @EJB
    private ExpenditureBean expenditureBean;

    @EJB
    private AccountBean accountBean;

    @EJB
    private CategoryOperationBean categoryOperationBean;

    @EJB
    private PlaceBean placeBean;

    private Expenditure expenditure;

    private List<Expenditure> expenditures;

    private List<Account> accounts;
    private List<CategoryOperation> categories;
    private List<Place> receivers;

    private String selectedAccount;
    private String selectedCategory;
    private String selectedReceiver;

    private BigDecimal accountBalance = BigDecimal.ZERO;
    private BigDecimal accountBalanceAfterPayment = BigDecimal.ZERO;
    private BigDecimal totalExpenditure = BigDecimal.ZERO;

    @ManagedProperty(value="#{param.id}")
    private String id;

    @ManagedProperty(value="#{operationFilterMBean}")
    private OperationFilterMBean operationFilterMBean;

    public ExpenditureMBean() {
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

    public Expenditure getExpenditure() {
        return expenditure;
    }

    public void setExpenditure(Expenditure expenditure) {
        this.expenditure = expenditure;
    }

    public List<Account> getAccounts() {
        if(this.accounts == null) {
            this.accounts = accountBean.findAccounts();
        }
        return this.accounts;
    }

    public List<CategoryOperation> getCategories() {
        if(this.categories == null) {
            this.categories = categoryOperationBean.findCategoriesOperation(TransactionType.DEBIT);
            Collections.sort(this.categories);
        }
        return categories;
    }

    public List<Place> getReceivers() {
        if(this.receivers == null) {
            this.receivers = placeBean.findPlaces();
        }
        return receivers;
    }

    public List<Expenditure> getExpenditures() {
        if(this.expenditures == null) {
            this.expenditures = expenditureBean.findExpenditures(operationFilterMBean.getSelectedYear(),
                                                                 operationFilterMBean.getSelectedMonth(),
                                                                 operationFilterMBean.getSelectedDay());
        }
        return this.expenditures;
    }

    public BigDecimal getTotalExpenditure() {
        calculateTotalExpenditure();
        return totalExpenditure;
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

    public String getSelectedReceiver() {
        return selectedReceiver;
    }

    public void setSelectedReceiver(String selectedReceiver) {
        this.selectedReceiver = selectedReceiver;
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

        if(this.accountBalanceAfterPayment.equals(BigDecimal.ZERO) && this.expenditure.getAmount() != null && ab != null) {
            this.accountBalanceAfterPayment = ab.subtract(this.expenditure.getAmount());
        }
        return this.accountBalanceAfterPayment;
    }

    @PostConstruct
    public void load() {
        operationFilterMBean.setOperationFilterable(expenditureBean);

        if(id != null && !id.isEmpty()) {
            this.expenditure = expenditureBean.findExpenditure(id);
            if(this.expenditure != null) {
                this.selectedAccount = this.expenditure.getAccount().getId();
                this.selectedCategory = this.expenditure.getCategory().getId();
                this.selectedReceiver = this.expenditure.getReceiver().getId();
            }
            else {
                ResourceBundle bundle = ResourceBundle.getInstance();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getMessage("infoRecordNotFound")));
            }
        }
        else {
            this.expenditure = new Expenditure();
        }
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

    private void calculateTotalExpenditure() {
        if(this.totalExpenditure == BigDecimal.ZERO) {
            this.expenditures = getExpenditures();
        }
        else { return; }

        for(Expenditure expen: this.expenditures) {
            this.totalExpenditure = this.totalExpenditure.add(expen.getAmount());
        }
    }

    public void checkExpense(String id) {
        LOGGER.log(Level.INFO, "Expense id: {0}", id);
        Expenditure expense = new Expenditure(id);
        expenditureBean.checkExpense(expense);
    }

    public String save() {
        if(!this.selectedAccount.isEmpty()) {
            this.expenditure.setAccount(accountBean.findAccount(this.selectedAccount));
        }

        if(!this.selectedCategory.isEmpty()) {
            this.expenditure.setCategory(categoryOperationBean.findCategoryOperation(this.selectedCategory));
        }

        if(!this.selectedReceiver.isEmpty()) {
            this.expenditure.setReceiver(placeBean.findPlace(this.selectedReceiver));
        }

        expenditureBean.save(this.expenditure);

        operationFilterMBean.updateAll();

        return "expenditures?faces-redirect=true";
    }

    public String remove() {
        expenditureBean.remove(this.expenditure.getId());
        return "expenditures?faces-redirect=true";
    }
}