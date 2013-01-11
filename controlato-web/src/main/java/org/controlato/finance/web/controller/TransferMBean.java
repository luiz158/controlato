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
import org.controlato.finance.business.TransferBean;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.Transfer;
import org.controlato.web.util.MonthDecorator;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class TransferMBean {

    @EJB
    private AccountBean accountBean;

    @EJB
    private TransferBean transferBean;

    private List<Account> sourceAccounts;
    private List<Account> targetAccounts;
    private List<Transfer> transfers;

    private Transfer transfer;

    private String selectedSourceAccount;
    private String selectedTargetAccount;

    private BigDecimal sourceBalance = BigDecimal.ZERO;
    private BigDecimal targetBalance = BigDecimal.ZERO;
    private BigDecimal sourceBalanceAfterTransfer = BigDecimal.ZERO;
    private BigDecimal targetBalanceAfterTransfer = BigDecimal.ZERO;
    private BigDecimal totalTransfers = BigDecimal.ZERO;

    @ManagedProperty(value="#{param.id}")
    private String id;

    @ManagedProperty(value="#{operationFilterMBean}")
    private OperationFilterMBean operationFilterMBean;

    public TransferMBean() {
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

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public List<Transfer> getTransfers() {
        if(this.transfers == null) {
            this.transfers = transferBean.findTransfers(operationFilterMBean.getSelectedYear(),
                                                        operationFilterMBean.getSelectedMonth(),
                                                        operationFilterMBean.getSelectedDay());
        }
        return this.transfers;
    }

    public BigDecimal getTotalTransfers() {
        calculateTotalTransfers();
        return totalTransfers;
    }

    public String getSelectedSourceAccount() {
        return selectedSourceAccount;
    }

    public void setSelectedSourceAccount(String selectedSourceAccount) {
        this.selectedSourceAccount = selectedSourceAccount;
        updateTargetAccounts();
    }

    public List<Account> getSourceAccounts() {
        updateSourceAccounts();
        return this.sourceAccounts;
    }

    private void updateSourceAccounts() {
        if(this.sourceAccounts == null) {
            Account except = null;
            if(this.selectedTargetAccount != null && this.selectedSourceAccount == null) {
                except = accountBean.findAccount(this.selectedTargetAccount);
            }
            this.sourceAccounts = accountBean.findAccounts(except);
        }
    }

    public String getSelectedTargetAccount() {
        return selectedTargetAccount;
    }

    public void setSelectedTargetAccount(String selectedTargetAccount) {
        this.selectedTargetAccount = selectedTargetAccount;
        updateSourceAccounts();
    }

    public List<Account> getTargetAccounts() {
        updateTargetAccounts();
        return this.targetAccounts;
    }

    private void updateTargetAccounts() {
        if(this.targetAccounts == null) {
            Account except = null;
            if(this.selectedSourceAccount != null && this.selectedTargetAccount == null) {
                except = accountBean.findAccount(this.selectedSourceAccount);
            }
            this.targetAccounts = accountBean.findAccounts(except);
        }
    }

    public BigDecimal getSourceBalance() {
        if(sourceBalance == null || sourceBalance.equals(BigDecimal.ZERO)) {
            Account sourceAccount = accountBean.findAccount(this.selectedSourceAccount);
            if(sourceAccount != null) {
                sourceBalance = sourceAccount.getBalance();
            }
        }
        return sourceBalance;
    }

    public BigDecimal getTargetBalance() {
        if(targetBalance == null || targetBalance.equals(BigDecimal.ZERO)) {
            Account targetAccount = accountBean.findAccount(this.selectedTargetAccount);
            if(targetAccount != null) {
                targetBalance = targetAccount.getBalance();
            }
        }
        return targetBalance;
    }

    public BigDecimal getSourceBalanceAfterTransfer() {
        /* This is necessary because in some requests getSourceBalance()
         * is not invoked. */
        BigDecimal sb = this.getSourceBalance();
        BigDecimal amount = this.transfer.getAmount();
        if(this.sourceBalanceAfterTransfer.equals(BigDecimal.ZERO) && amount != null && sb != null) {
            this.sourceBalanceAfterTransfer = sb.subtract(amount);
        }
        return this.sourceBalanceAfterTransfer;
    }

    public BigDecimal getTargetBalanceAfterTransfer() {
        /* This is necessary because in some requests getTargetBalance()
         * is not invoked. */
        BigDecimal tb = this.getTargetBalance();
        BigDecimal amount = this.transfer.getAmount();
        if(this.targetBalanceAfterTransfer.equals(BigDecimal.ZERO) && amount != null && tb != null) {
            this.targetBalanceAfterTransfer = amount.add(tb);
        }
        return this.targetBalanceAfterTransfer;
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

        operationFilterMBean.setOperationFilterable(transferBean);

        if(id != null && !id.isEmpty()) {
            this.transfer = transferBean.findTransfer(id);
            if(this.transfer != null) {
                this.selectedSourceAccount = this.transfer.getSourceAccount().getId();
                this.selectedTargetAccount = this.transfer.getTargetAccount().getId();
            }
            else {
                ResourceBundle bundle = ResourceBundle.getInstance();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getMessage("infoRecordNotFound")));
            }
        }
        else {
            this.transfer = new Transfer();
        }
    }

    private void calculateTotalTransfers() {
        if(this.totalTransfers == BigDecimal.ZERO) {
            this.transfers = getTransfers();
        }
        else { return; }

        for(Transfer transf: this.transfers) {
            this.totalTransfers = this.totalTransfers.add(transf.getAmount());
        }
    }

    public void checkTransfer(Transfer transfer) {
        transferBean.checkTransfer(transfer);
    }

    public String save() {
        this.transfer.setSourceAccount(accountBean.findAccount(this.selectedSourceAccount));
        this.transfer.setTargetAccount(accountBean.findAccount(this.selectedTargetAccount));

        transferBean.save(this.transfer);

        operationFilterMBean.updateAll();

        return "transfers?faces-redirect=true";
    }

    public String remove() {
        transferBean.remove(this.transfer.getId());
        return "transfers?faces-redirect=true";
    }
}