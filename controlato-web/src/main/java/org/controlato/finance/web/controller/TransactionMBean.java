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

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.controlato.finance.business.AccountBean;
import org.controlato.finance.business.TransactionBean;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.Transaction;
import org.controlato.web.util.MonthDecorator;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class TransactionMBean {

    @EJB
    private TransactionBean transactionBean;

    @EJB
    private AccountBean accountBean;

    private Transaction transaction;

    private List<Transaction> transactions;

    private List<Account> accounts;

    private String selectedAccount;

    @ManagedProperty(value="#{param.id}")
    private String id;

    @ManagedProperty(value="#{operationFilterMBean}")
    private OperationFilterMBean operationFilterMBean;

    public TransactionMBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public OperationFilterMBean getOperationFilterMBean() {
        return operationFilterMBean;
    }

    public void setOperationFilterMBean(OperationFilterMBean operationFilterMBean) {
        this.operationFilterMBean = operationFilterMBean;
    }

    public List<Account> getAccounts() {
        if(this.accounts == null) {
            this.accounts = accountBean.findAccounts();
        }
        return this.accounts;
    }

    public List<Transaction> getTransactions() {
        if(this.transactions == null) {
            Account account = null;
            if(this.selectedAccount != null && !this.selectedAccount.isEmpty()) {
                account = new Account(this.selectedAccount);
            }
            this.transactions = transactionBean.findTransactions(account,
                                                                 operationFilterMBean.getSelectedYear(),
                                                                 operationFilterMBean.getSelectedMonth(),
                                                                 operationFilterMBean.getSelectedDay());
        }
        return this.transactions;
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
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
        operationFilterMBean.setOperationFilterable(transactionBean);

        if(id != null && !id.isEmpty()) {
            this.transaction = transactionBean.findTransaction(id);
            if(this.transaction != null) {
                this.selectedAccount = this.transaction.getAccount().getId();
            }
            else {
                ResourceBundle bundle = ResourceBundle.getInstance();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getMessage("infoRecordNotFound")));
            }
        }
        else {
            this.transaction = new Transaction();
        }
    }

    public String save() {
        if(!this.selectedAccount.isEmpty()) {
            this.transaction.setAccount(accountBean.findAccount(this.selectedAccount));
        }

        transactionBean.save(this.transaction);
        return "transactions?faces-redirect=true";
    }

    public String remove() {
        transactionBean.remove(this.transaction.getId());
        return "transactions?faces-redirect=true";
    }
}
