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
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.controlato.entity.Identified;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name="transfer")
public class Transfer implements Serializable, Identified {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String description;

    private BigDecimal amount;

    @Column(name="date_transfer")
    @Temporal(TemporalType.DATE)
    private Date dateTransfer;

    @ManyToOne(optional=false)
    @JoinColumn(name="source_account")
    private Account sourceAccount;

    @ManyToOne(optional=false)
    @JoinColumn(name="target_account")
    private Account targetAccount;

    @ManyToOne(optional=false)
    @JoinColumn(name="source_transaction")
    private Transaction sourceTransaction;

    @ManyToOne(optional=false)
    @JoinColumn(name="target_transaction")
    private Transaction targetTransaction;

    private Boolean checked;

    public Transfer() {}

    public Transfer(String id) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        if(this.sourceTransaction != null && this.targetTransaction != null) {
            this.sourceTransaction.setDescription(this.description);
            this.targetTransaction.setDescription(this.description);
        }
    }

    public Date getDateTransfer() {
        return dateTransfer;
    }

    public void setDateTransfer(Date dateTransfer) {
        this.dateTransfer = dateTransfer;
        if(this.sourceTransaction != null && this.targetTransaction != null) {
            this.sourceTransaction.setDateRegistered(this.dateTransfer);
            this.targetTransaction.setDateRegistered(this.dateTransfer);
        }
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
        if(this.sourceTransaction != null) {
            this.sourceTransaction.setAccount(this.sourceAccount);
        }
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public void setTargetAccount(Account targetAccount) {
        this.targetAccount = targetAccount;
        if(this.targetTransaction != null) {
            this.targetTransaction.setAccount(this.targetAccount);
        }
    }

    public Transaction getSourceTransaction() {
        return sourceTransaction;
    }

    public void setSourceTransaction(Transaction sourceTransaction) {
        this.sourceTransaction = sourceTransaction;
    }

    public Transaction getTargetTransaction() {
        return targetTransaction;
    }

    public void setTargetTransaction(Transaction targetTransaction) {
        this.targetTransaction = targetTransaction;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        if(this.sourceTransaction != null && this.targetTransaction != null) {
            this.sourceTransaction.setAmount(this.amount);
            this.targetTransaction.setAmount(this.amount);
        }
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Transaction createSourceTransaction() {
        if(this.sourceTransaction == null) {
            this.sourceTransaction = new Transaction();
            this.sourceTransaction.setAccount(this.sourceAccount);
            this.sourceTransaction.setAmount(this.amount);
            this.sourceTransaction.setDateRegistered(this.dateTransfer);
            this.sourceTransaction.setDescription(this.description);
            this.sourceTransaction.setTransactionType(TransactionType.DEBIT);
        }
        return this.sourceTransaction;
    }

    public Transaction createTargetTransaction() {
        if(this.targetTransaction == null) {
            this.targetTransaction = new Transaction();
            this.targetTransaction.setAccount(this.targetAccount);
            this.targetTransaction.setAmount(this.amount);
            this.targetTransaction.setDateRegistered(this.dateTransfer);
            this.targetTransaction.setDescription(this.description);
            this.targetTransaction.setTransactionType(TransactionType.CREDIT);
        }
        return this.targetTransaction;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transfer other = (Transfer) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}