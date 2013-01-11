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
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.controlato.finance.business.CategoryOperationBean;
import org.controlato.finance.business.ExpenditureBean;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.entity.TransactionType;
import org.controlato.web.util.MonthDecorator;
import org.controlato.web.util.ResourceBundle;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class CategoryOperationMBean {

    public static final Logger LOGGER = Logger.getLogger(CategoryOperationMBean.class.getName());

    public static final String TEXT_GREEN = "textGreen";
    public static final String TEXT_BLUE = "textBlue";
    public static final String TEXT_RED = "textRed";
    public static final String TEXT_GRAY = "textGray";

    @EJB
    private CategoryOperationBean categoryOperationBean;

    @EJB
    private ExpenditureBean expenditureBean;

    private CategoryOperation categoryOperation;

    private TreeNode debitRoot;
    private TreeNode creditRoot;

    private List<CategoryOperation> parentCategories;

    private String selectedParentCategory;

    @ManagedProperty(value="#{param.id}")
    private String id;

    @ManagedProperty(value="#{operationFilterMBean}")
    private OperationFilterMBean operationFilterMBean;

    // Attributes used exclusively in the validation of their respective fields.
    private BigDecimal validationAllocatedBudget = BigDecimal.ZERO;
    private BigDecimal validationBudgetLimit = BigDecimal.ZERO;

    // Attributes used to calculate the totals of the categories values.
    private BigDecimal totalAllocatedBudget = BigDecimal.ZERO;
    private BigDecimal totalBudgetLimit = BigDecimal.ZERO;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal totalEarned = BigDecimal.ZERO;

    public CategoryOperationMBean() {
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

    public CategoryOperation getCategoryOperation() {
        if(this.categoryOperation == null) {
            this.categoryOperation = new CategoryOperation();
        }
        return this.categoryOperation;
    }

    public void setCategoryOperation(CategoryOperation categoryOperation) {
        this.categoryOperation = categoryOperation;
    }

    public TreeNode getDebitRoot() {
        loadDebitRootCategories();
        return this.debitRoot;
    }

    public TreeNode getCreditRoot() {
        loadCreditRootCategories();
        return this.creditRoot;
    }

    public String getSelectedParentCategory() {
        return selectedParentCategory;
    }

    public void setSelectedParentCategory(String selectedParentCategory) {
        this.selectedParentCategory = selectedParentCategory;
    }

    public BigDecimal getTotalAllocatedBudget() {
        loadDebitRootCategories();
        return totalAllocatedBudget;
    }

    public BigDecimal getTotalBudgetLimit() {
        loadDebitRootCategories();
        return totalBudgetLimit;
    }

    public BigDecimal getTotalSpent() {
        loadDebitRootCategories();
        return totalSpent;
    }

    public BigDecimal getTotalEarned() {
        loadCreditRootCategories();
        return totalEarned;
    }

    public List<Integer> getYears() {
        return operationFilterMBean.getYears();
    }

    public List<MonthDecorator> getMonths() {
        return operationFilterMBean.getMonths();
    }

    public List<CategoryOperation> getParentCategories() {
        if(this.parentCategories == null) {
            this.parentCategories = categoryOperationBean.findCategoriesOperation(this.categoryOperation);
            Collections.sort(this.parentCategories);
        }

        return this.parentCategories;
    }

    private void loadDebitRootCategories() {
        if(this.debitRoot == null) {
            this.debitRoot = new DefaultTreeNode("root", null);
            List<CategoryOperation> rootCategories = categoryOperationBean.findRootCategoriesOperation(TransactionType.DEBIT,
                                                                                                       operationFilterMBean.getSelectedYear(),
                                                                                                       operationFilterMBean.getSelectedMonth());

            for(CategoryOperation rootCategory: rootCategories) {
                totalAllocatedBudget = totalAllocatedBudget.add(rootCategory.getAllocatedBudget());
                totalBudgetLimit = totalBudgetLimit.add(rootCategory.getBudgetLimit());
                totalSpent = totalSpent.add(rootCategory.getTotalOperations());
            }

            loadCategoriesTree(this.debitRoot, rootCategories);
        }
    }

    private void loadCreditRootCategories() {
        if(this.creditRoot == null) {
            this.creditRoot = new DefaultTreeNode("root", null);
            List<CategoryOperation> rootCategories = categoryOperationBean.findRootCategoriesOperation(TransactionType.CREDIT,
                                                                                                       operationFilterMBean.getSelectedYear(),
                                                                                                       operationFilterMBean.getSelectedMonth());

            for(CategoryOperation rootCategory: rootCategories) {
                totalEarned = totalEarned.add(rootCategory.getTotalOperations());
            }

            loadCategoriesTree(this.creditRoot, rootCategories);
        }
    }

    private void loadCategoriesTree(TreeNode parentTreeNode, List<CategoryOperation> entityNodes) {
        TreeNode treeNode;
        List<CategoryOperation> childEntityNodes;
        for(CategoryOperation entityNode: entityNodes) {
            decorateCategory(entityNode);
            treeNode = new DefaultTreeNode(entityNode, parentTreeNode);
            childEntityNodes = categoryOperationBean.findChildrenCategoryOperation(entityNode, operationFilterMBean.getSelectedYear(), operationFilterMBean.getSelectedMonth());
            if(childEntityNodes != null && !childEntityNodes.isEmpty()) {
                loadCategoriesTree(treeNode, childEntityNodes);
            }
        }
    }

    @PostConstruct
    public void load() {
        operationFilterMBean.setOperationFilterable(expenditureBean);

        if(id != null && !id.isEmpty()) {
            this.categoryOperation = categoryOperationBean.findCategoryOperation(id);
            if(this.categoryOperation != null && this.categoryOperation.getParentCategory() != null) {
                this.selectedParentCategory = this.categoryOperation.getParentCategory().getId();
            }
            else {
                ResourceBundle bundle = ResourceBundle.getInstance();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getMessage("infoRecordNotFound")));
            }
        }
    }

    public String save() {
        if(this.selectedParentCategory != null && !this.selectedParentCategory.isEmpty()) {
            this.categoryOperation.setParentCategory(categoryOperationBean.findCategoryOperation(this.selectedParentCategory));
        }

        categoryOperationBean.save(this.categoryOperation);
        return "categories?faces-redirect=true";
    }

    public String remove() {
        categoryOperationBean.remove(this.categoryOperation.getId());
        return "categories?faces-redirect=true";
    }

    private void decorateCategory(CategoryOperation category) {
        BigDecimal zero = new BigDecimal("0.00");
        BigDecimal allocatedBudget = category.getAllocatedBudget();
        BigDecimal budgetLimit = category.getBudgetLimit();
        BigDecimal totalOperations = category.getTotalOperations();

        if (!allocatedBudget.equals(zero) && budgetLimit.equals(zero)) {
            if (allocatedBudget.compareTo(totalOperations) > 0) {
                category.setDecoration(TEXT_GREEN);
            }
            else if (allocatedBudget.compareTo(totalOperations) == 0) {
                category.setDecoration(TEXT_BLUE);
            }
            else {
                category.setDecoration(TEXT_RED);
            }
        }
        else if (allocatedBudget.equals(zero) && !budgetLimit.equals(zero)) {
            if (budgetLimit.compareTo(totalOperations) > 0) {
                category.setDecoration(TEXT_GREEN);
            }
            else if (budgetLimit.compareTo(totalOperations) == 0) {
                category.setDecoration(TEXT_BLUE);
            }
            else {
                category.setDecoration(TEXT_RED);
            }
        }
        else if (!allocatedBudget.equals(zero) && !budgetLimit.equals(zero)) {
            if (allocatedBudget.compareTo(totalOperations) > 0) {
                category.setDecoration(TEXT_GREEN);
            }
            else if (allocatedBudget.compareTo(totalOperations) <= 0 && budgetLimit.compareTo(totalOperations) >= 0) {
                category.setDecoration(TEXT_BLUE);
            }
            else {
                category.setDecoration(TEXT_RED);
            }
        }
        else {
            category.setDecoration(TEXT_GRAY);
        }
    }

    public void validateAllocatedBudget (FacesContext context, UIComponent component, Object value) {
        this.validationAllocatedBudget = (BigDecimal) value;
    }

    public void validateBudgetLimit(FacesContext context, UIComponent component, Object value) {
        this.validationBudgetLimit = (BigDecimal) value;
        if(this.validationBudgetLimit.compareTo(this.validationAllocatedBudget) < 0) {
            ResourceBundle rb = ResourceBundle.getInstance();
            throw new ValidatorException(new FacesMessage(rb.getMessage("validationAllocatedBudgetGreaterThanBudgetLimit")));
        }
    }
}