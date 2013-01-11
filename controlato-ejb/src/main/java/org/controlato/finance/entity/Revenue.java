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

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name="revenue")
public class Revenue implements Operation, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name="payer")
    private Place payer;

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

    @Column(name="credit_date")
    @Temporal(TemporalType.DATE)
    private Date creditDate;

    private String description;

    private Boolean checked;

    public Revenue() {}

    public Revenue(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Place getPayer() {
        return payer;
    }

    public void setPayer(Place payer) {
        this.payer = payer;
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

    public void setAccount(Account account) {
        this.account = account;
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

    public Date getCreditDate() {
        return creditDate;
    }

    public void setCreditDate(Date creditDate) {
        this.creditDate = creditDate;
        if(this.transaction != null) {
            this.transaction.setDateRegistered(this.creditDate);
        }
    }

    public String getDescription() {
        return description;
    }

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
            transaction.setDateRegistered(this.creditDate);
            transaction.setDescription(this.description);
            transaction.setTransactionType(TransactionType.CREDIT);
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
        if (!(object instanceof Revenue)) {
            return false;
        }
        Revenue other = (Revenue) object;
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