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
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name="category_operation")
public class CategoryOperation implements Serializable, Comparable<CategoryOperation> {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="transaction_type")
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name="parent_category")
    private CategoryOperation parentCategory;

    @OneToMany(mappedBy="parentCategory")
    private List<CategoryOperation> subCategories;

    @Column(name="budget_allocated")
    private BigDecimal allocatedBudget = BigDecimal.ZERO;

    @Column(name="budget_limit")
    private BigDecimal budgetLimit = BigDecimal.ZERO;

    @Transient
    private BigDecimal totalOperations = BigDecimal.ZERO;

    @Transient
    private String decoration;

    public CategoryOperation() {}

    public CategoryOperation(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return this.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public CategoryOperation getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(CategoryOperation parentCategory) {
        this.parentCategory = parentCategory;
        this.setTransactionType(this.parentCategory.getTransactionType());
    }

    public List<CategoryOperation> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryOperation> subCategories) {
        this.subCategories = subCategories;
    }

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
    }

    public BigDecimal getBudgetLimit() {
        return this.budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    /**
     * @return The total of operations classified in this category plus the sum
     * of the total of operations classified in its sub-categories. This value
     * is calculated at runtime by business logic operations.
     */
    public BigDecimal getTotalOperations() {
        BigDecimal total = totalOperations;
        if(subCategories != null) {
            for(CategoryOperation subCategory: subCategories) {
                total = total.add(subCategory.getTotalOperations());
            }
        }
        return total;
    }

    /**
     * @param totalOperations The total of operations is not going to be persisted
     * in the database.
     */
    public void setTotalOperations(BigDecimal totalOperations) {
        this.totalOperations = totalOperations;
    }

    /**
     * @return Value used by the user interface to visually differentiate a
     * category from another.
     */
    public String getDecoration() {
        return decoration;
    }

    /**
     * @param decoration This value is not going to be persisted in the database.
     */
    public void setDecoration(String decoration) {
        this.decoration = decoration;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CategoryOperation)) {
            return false;
        }
        CategoryOperation other = (CategoryOperation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (this.parentCategory != null?this.parentCategory.toString() + " - ":"") + this.name;
    }

    @Override
    public int compareTo(CategoryOperation otherCategory) {
            if(otherCategory == null || !(otherCategory instanceof CategoryOperation)) {
            return 0;
        }

        CategoryOperation other = (CategoryOperation) otherCategory;
        return this.getFullName().compareToIgnoreCase(other.getFullName());
    }
}