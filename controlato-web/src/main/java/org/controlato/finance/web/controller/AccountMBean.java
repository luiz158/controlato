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
import org.controlato.business.exception.EntityConsistencyException;
import org.controlato.finance.business.AccountBean;
import org.controlato.finance.business.AccounterBean;
import org.controlato.finance.business.TransactionBean;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.AccountType;
import org.controlato.finance.entity.Transaction;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class AccountMBean {

    @EJB
    private AccountBean accountBean;

    @EJB
    private AccounterBean accounterBean;

    @EJB
    private TransactionBean transactionBean;

    private Account account;

    private List<Account> accounts;
    private List<Transaction> transactions;

    @ManagedProperty(value="#{param.id}")
    private String id;

    private BigDecimal totalBalance = BigDecimal.ZERO;

    public AccountMBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getAccount() {
        if(this.account == null) {
            this.account = new Account();
            this.account.setAccountType(AccountType.DEBIT);
        }
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<Account> getAccounts() {
        loadAccounts();
        return this.accounts;
    }

    public List<Transaction> getTransactions() {
        if(this.transactions == null) {
            this.transactions = transactionBean.findTransactions(this.account);
        }
        return this.transactions;
    }

    public BigDecimal getTotalBalance() {
        loadAccounts();
        return totalBalance;
    }

    private void loadAccounts() {
        if(this.accounts == null) {
            this.accounts = accountBean.findAccounts();
            for(Account acc:this.accounts) {
                this.totalBalance = this.totalBalance.add(acc.getBalance());
            }
        }
    }

    @PostConstruct
    public void load() {
        if(id != null && !id.isEmpty()) {
            this.account = accountBean.findAccount(id);
        }
    }

    public String save() {
        accountBean.save(this.account);
        return "accounts?faces-redirect=true";
    }

    public String remove() {
        try {
            accounterBean.removeAccount(this.account.getId());
        }
        catch(EntityConsistencyException ece) {
            ResourceBundle rb = ResourceBundle.getInstance();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(rb.getMessage(ece.getMessage())));
            return "account_edit";
        }
        return "accounts?faces-redirect=true";
    }
}