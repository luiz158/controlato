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
package org.controlato.finance.business;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.entity.TransactionType;
import org.controlato.util.CalendarUtils;
import org.controlato.util.EntitySupport;
import org.controlato.util.OrderType;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class CategoryOperationBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private ExpenditureBean expenditureBean;

    public CategoryOperation findCategoryOperation(String id) {
        if(id == null || id.isEmpty()) {
            return null;
        }

        return em.find(CategoryOperation.class, id);
    }

    public List<CategoryOperation> findCategoriesOperation() {
        return em.createQuery("select co from CategoryOperation co order by co.name asc")
                 .getResultList();
    }

    public List<CategoryOperation> findCategoriesOperation(TransactionType transactionType) {
        return em.createQuery("select co from CategoryOperation co where co.transactionType = :type order by co.name asc")
                 .setParameter("type", transactionType)
                 .getResultList();
    }

    public List<CategoryOperation> findCategoriesOperation(CategoryOperation except) {
        if(except != null) {
            return em.createQuery("select co from CategoryOperation co where co != :except order by co.name asc")
                     .setParameter("except", except)
                     .getResultList();
        }
        else {
            return findCategoriesOperation();
        }
    }

    /**
     * @param from Optional parameter, used in case temporal data related to the categories are needed. If different
     *             from null, it defines from which date the sum of expenses for each category will be considered.
     * @param to Optional parameter, used in case temporal data related to the categories are needed. If different
     *             from null, it defines until which date the sum of expenses for each category will be considered.
     * @return Categories that doesn't have a parent category, thus root categories. They are useful to start building
     * category hierarchies.
     * */
    public List<CategoryOperation> findRootCategoriesOperation(TransactionType transactionType, Integer year, Integer month) {
        List<CategoryOperation> rootCategories = em.createQuery("select co from CategoryOperation co where co.transactionType = :type and co.parentCategory is null order by co.name asc")
                                                   .setParameter("type", transactionType)
                                                   .getResultList();

        if(year != null && month != null) {
            Calendar[] interval = CalendarUtils.buildInterval(year, month, null);

            List<CategoryOperation> categoriesWithTotalSpent = expenditureBean.findTotalSpentByCategory(interval[CalendarUtils.FROM].getTime(), interval[CalendarUtils.TO].getTime(), OrderType.ASC);
            if(categoriesWithTotalSpent != null) {
                for(CategoryOperation category: rootCategories) {
                    for(CategoryOperation categoryWithTotalSpent: categoriesWithTotalSpent) {
                        if(category.equals(categoryWithTotalSpent)) {
                            category = categoryWithTotalSpent;
                            break;
                        }
                    }
                }
            }
        }

        return rootCategories;
    }

    /**
     * @param parentCategory the category whose sub-categories are expected to be found.
     * @param from Optional parameter, used in case temporal data related to the categories are needed. If different
     *             from null, it defines from which date the sum of expenses for each category will be considered.
     * @param to Optional parameter, used in case temporal data related to the categories are needed. If different
     *             from null, it defines until which date the sum of expenses for each category will be considered.
     * @return Categories that does have a parent category, thus children categories. They are useful to start building
     * category hierarchies.
     * */
    public List<CategoryOperation> findChildrenCategoryOperation(CategoryOperation parentCategory, Integer year, Integer month) {
        if(parentCategory == null) {
            return null;
        }

        List<CategoryOperation> childrenCategories = em.createQuery("select co from CategoryOperation co where co.parentCategory = :parent order by co.name asc")
                                                       .setParameter("parent", parentCategory)
                                                       .getResultList();

        if(year != null && month != null) {
            Calendar[] interval = CalendarUtils.buildInterval(year, month, null);

            List<CategoryOperation> categoriesWithTotalSpent = expenditureBean.findTotalSpentByCategory(interval[CalendarUtils.FROM].getTime(), interval[CalendarUtils.TO].getTime(), OrderType.ASC);
            if(categoriesWithTotalSpent != null) {
                for(CategoryOperation category: childrenCategories) {
                    for(CategoryOperation categoryWithTotalSpent: categoriesWithTotalSpent) {
                        if(category.equals(categoryWithTotalSpent)) {
                            category = categoryWithTotalSpent;
                            break;
                        }
                    }
                }
            }
        }

        return childrenCategories;
    }

    public void save(CategoryOperation categoryOperation) {
        if(categoryOperation.getId() == null || categoryOperation.getId().isEmpty()) {
            categoryOperation.setId(EntitySupport.generateEntityId());
            em.persist(categoryOperation);
        }
        else {
            em.merge(categoryOperation);
        }

        updateFiguresParentCategories(categoryOperation.getParentCategory());
    }

    private void updateFiguresParentCategories(CategoryOperation parentCategory) {
        if(parentCategory == null) {
            return;
        }

        List<CategoryOperation> siblings = findChildrenCategoryOperation(parentCategory, null, null);
        BigDecimal subTotalAllocatedBudget = BigDecimal.ZERO;
        BigDecimal subTotalBudgetLimit = BigDecimal.ZERO;

        // Sum the figures from sub-categories ...
        for(CategoryOperation category: siblings) {
            subTotalAllocatedBudget = subTotalAllocatedBudget.add(category.getAllocatedBudget());
            subTotalBudgetLimit = subTotalBudgetLimit.add(category.getBudgetLimit());
        }

        // ... to set the figures of their parent category.
        parentCategory.setAllocatedBudget(subTotalAllocatedBudget);
        parentCategory.setBudgetLimit(subTotalBudgetLimit);
        em.merge(parentCategory);
        updateFiguresParentCategories(parentCategory.getParentCategory());
    }

    public void remove(String id) {
        CategoryOperation categoryActivity = em.find(CategoryOperation.class, id);
        if(categoryActivity != null) {
            em.remove(categoryActivity);
        }
    }
}