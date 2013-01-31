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
package org.controlato.finance.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import org.controlato.entity.Identified;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name="expenditure")
public class Expenditure implements Operation, Serializable, Identified {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name="receiver")
    private Place receiver;

    @ManyToOne
    @JoinColumn(name="category")
    private CategoryOperation category;

    @ManyToOne
    @JoinColumn(name="account")
    private Account account;

    @ManyToOne
    @JoinColumn(name="transaction")
    private Transaction transaction;

    private BigDecimal amount;

    @Column(name="debit_date")
    @Temporal(TemporalType.DATE)
    private Date debitDate;

    private String description;

    private Boolean checked;

    @Transient
    private boolean accountChanged;

    public Expenditure() {
    }

    public Expenditure(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Place getReceiver() {
        return receiver;
    }

    public void setReceiver(Place receiver) {
        this.receiver = receiver;
    }

    public CategoryOperation getCategory() {
        return category;
    }

    public void setCategory(CategoryOperation category) {
        this.category = category;
    }

    public Account getAccount() {
        return account;
    }

    /**
     * Changes the account of the expenditure and changes also the account of
     * the transaction since the expenditure is attached to it. If the account
     * was previously set and this invocation is trying to reset the account
     * with a different account, then the status accountChanged is set to true.
     */
    public void setAccount(Account account) {
        if(this.account != null && !this.account.equals(account)) {
            this.accountChanged = true;
        }
        this.account = account;
        if(this.transaction != null) {
            this.transaction.setAccount(this.account);
        }
    }

    public boolean accountChanged() {
        return this.accountChanged;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Changes the amount of the expenditure and changes also the
     * amount of the transaction since the expenditure is very attached to
     * its transaction.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        if(this.transaction != null) {
            this.transaction.setAmount(this.amount);
        }
    }

    public Date getDebitDate() {
        return debitDate;
    }

    /**
     * Changes the debit date of the expenditure and changes also the
     * registered_date of the transaction since the expenditure is very attached
     * to its transaction.
     */
    public void setDebitDate(Date debitDate) {
        this.debitDate = debitDate;
        if(this.transaction != null) {
            this.transaction.setDateRegistered(this.debitDate);
        }
    }

    public String getDescription() {
        return description;
    }

    /**
     * Changes the description of the expenditure and changes also the
     * description of the transaction since the expenditure is very attached to
     * its transaction.
     */
    public void setDescription(String description) {
        this.description = description;
        if(this.transaction != null) {
            this.transaction.setDescription(this.description);
        }
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    @Override
    public Transaction createTransaction() {
        if(this.transaction == null) {
            transaction = new Transaction();
            transaction.setAccount(this.account);
            transaction.setAmount(this.amount);
            transaction.setDateRegistered(this.debitDate);
            transaction.setDescription(this.description);
            transaction.setTransactionType(TransactionType.DEBIT);
        }
        return this.transaction;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Expenditure)) {
            return false;
        }
        Expenditure other = (Expenditure) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.description;
    }
}