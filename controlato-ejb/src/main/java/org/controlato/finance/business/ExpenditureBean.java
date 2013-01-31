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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.CategoryOperation;
import org.controlato.finance.entity.CategoryTotalOperationsComparator;
import org.controlato.finance.entity.Expenditure;
import org.controlato.finance.entity.Place;
import org.controlato.finance.entity.Transaction;
import org.controlato.util.CalendarUtils;
import org.controlato.util.EntitySupport;
import org.controlato.util.OrderType;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class ExpenditureBean implements OperationFilterable {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private TransactionBean transactionBean;

    @EJB
    private AccountBean accountBean;

    private static final Calendar TODAY = Calendar.getInstance(TimeZone.getDefault());

    public Expenditure findExpenditure(String id) {
        return em.find(Expenditure.class, id);
    }

    public List<Expenditure> findExpenditures(Account account) {
        if(account == null) {
            return null;
        }

        return em.createQuery("select e from Expenditure e where e.account = :account order by e.debitDate desc")
                 .setParameter("account", account)
                 .getResultList();
    }

    public List<Expenditure> findExpenditures(Place receiver) {
        if(receiver == null) {
            return null;
        }

        return em.createQuery("select e from Expenditure e where e.receiver = :receiver order by e.debitDate desc")
                 .setParameter("receiver", receiver)
                 .getResultList();
    }

    public List<Expenditure> findExpenditures(CategoryOperation category, Date from, Date to) {
        if(category == null || from == null || to == null) {
            return null;
        }

        return em.createQuery("select e from Expenditure e where e.category = :category and e.debitDate >= :from and e.debitDate <= :to order by e.debitDate desc")
                 .setParameter("category", category)
                 .setParameter("from", from)
                 .setParameter("to", to)
                 .getResultList();
    }

    /**
     * Find all expenditures within the informed time interval and calculates the
     * total amount of expenses classified in each category.
     */
    public List<CategoryOperation> findTotalSpentByCategory(Date from, Date to, OrderType order) {
        if(from == null || to == null) {
            return null;
        }

        List<Object[]> totalCategories = em.createQuery("select e.category, sum(e.amount) from Expenditure e where e.debitDate >= :from and e.debitDate <= :to group by e.category")
                                      .setParameter("from", from)
                                      .setParameter("to", to)
                                      .getResultList();

        List<CategoryOperation> categoriesWithTotalSpent = null;
        if(totalCategories != null && !totalCategories.isEmpty()) {
            categoriesWithTotalSpent = new ArrayList<>();
            CategoryOperation category;
            BigDecimal totalSpent;
            for(Object[] totalCategory: totalCategories) {
                category = (CategoryOperation) totalCategory[0];
                totalSpent = (BigDecimal) totalCategory[1];
                category.setTotalOperations(totalSpent);
                categoriesWithTotalSpent.add(category);
            }
        }

        if(order == OrderType.DESC) {
            Collections.sort(categoriesWithTotalSpent, new CategoryTotalOperationsComparator());
        }

        return categoriesWithTotalSpent;
    }

    public List<CategoryOperation> findTotalSpentByCategory(Integer year, Integer month, OrderType order) {

        Calendar[] interval = CalendarUtils.buildInterval(year, month, null);

        return findTotalSpentByCategory(interval[0].getTime(), interval[1].getTime(), order);
    }

    public List<Expenditure> findExpenditures(Integer year, Integer month, Integer day) {
        List<Expenditure> expenditures = new ArrayList<>();

        if(year == null && month == null && day == null) {
            return expenditures;
        }

        Calendar[] interval = CalendarUtils.buildInterval(year, month, day);

        expenditures = findExpendituresInInterval(interval[CalendarUtils.FROM].getTime(), interval[CalendarUtils.TO].getTime(), OrderType.DESC);

        return expenditures;
    }

    public List<Expenditure> findExpendituresInInterval(Date from, Date to, OrderType orderType) {
        if(from == null) {
            from = Calendar.getInstance(TimeZone.getDefault()).getTime();
        }

        if(to == null) {
            to = Calendar.getInstance(TimeZone.getDefault()).getTime();
        }

        if(from.after(to)) {
            throw new IllegalArgumentException("fromBiggerThanTo");
        }

        if(orderType == null) {
            orderType = OrderType.ASC;
        }

        return em.createQuery("select e from Expenditure e where e.debitDate >= :from and e.debitDate <= :to order by e.debitDate "+ orderType.toString())
                 .setParameter("from", from)
                 .setParameter("to", to)
                 .getResultList();
    }

    @Override
    public List<Integer> findYears() {
        List<Date> dates = em.createQuery("select e.debitDate from Expenditure e order by e.debitDate desc").getResultList();
        List<Integer> years = new ArrayList<>();
        Calendar cDate;
        Integer currentYear;

        years.add(TODAY.get(Calendar.YEAR));

        for(Date date: dates) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(date);
            currentYear = cDate.get(Calendar.YEAR);
            if(!years.contains(currentYear)) {
                years.add(currentYear);
            }
        }

        Collections.sort(years);
        return years;
    }

    @Override
    public List<Integer> findMonths(Integer year) {

        Calendar[] interval = CalendarUtils.buildInterval(year, null, null);

        List<Expenditure> expenditures = findExpendituresInInterval(interval[CalendarUtils.FROM].getTime(), interval[CalendarUtils.TO].getTime(), OrderType.DESC);

        List<Integer> months = new ArrayList<>();
        Calendar cDate;
        Integer currentMonth;

        if(TODAY.get(Calendar.YEAR) == year) {
            months.add(TODAY.get(Calendar.MONTH));
        }

        for(Expenditure expenditure: expenditures) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(expenditure.getDebitDate());
            currentMonth = cDate.get(Calendar.MONTH);
            if(!months.contains(currentMonth)) {
                months.add(currentMonth);
            }
        }
        Collections.sort(months);

        return months;
    }

    @Override
    public List<Integer> findDays(Integer year, Integer month) {

        Calendar[] interval = CalendarUtils.buildInterval(year, month, null);

        List<Expenditure> expenditures = findExpendituresInInterval(interval[CalendarUtils.FROM].getTime(), interval[CalendarUtils.TO].getTime(), OrderType.DESC);

        List<Integer> days = new ArrayList<>();
        Calendar cDate;
        Integer currentDay;

        if(TODAY.get(Calendar.YEAR) == year && TODAY.get(Calendar.MONTH) == month) {
            days.add(TODAY.get(Calendar.DAY_OF_MONTH));
        }

        for(Expenditure expenditure: expenditures) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(expenditure.getDebitDate());
            currentDay = cDate.get(Calendar.DAY_OF_MONTH);
            if(!days.contains(currentDay)) {
                days.add(currentDay);
            }
        }
        Collections.sort(days);

        return days;
    }

    public void checkExpense(Expenditure expense) {
        expense = findExpenditure(expense.getId());
        if(expense == null) {
            return;
        }

        if(expense.getChecked() == null || expense.getChecked().equals(Boolean.FALSE)) {
            expense.setChecked(Boolean.TRUE);
        }
        else {
            expense.setChecked(Boolean.FALSE);
        }
    }

    public void save(Expenditure expenditure) {
        /* When the expenditure is new, a new transaction is created and associated to it. */
        if(EntitySupport.INSTANCE.isIdNotValid(expenditure)) {
            expenditure.setId(EntitySupport.INSTANCE.generateEntityId());

            Transaction transaction = expenditure.createTransaction();
            transactionBean.save(transaction);

            em.persist(expenditure);
        }
        else {
            Expenditure existingExpenditure = findExpenditure(expenditure.getId());

            Account previousAccount = existingExpenditure.getAccount();

            existingExpenditure.setCategory(expenditure.getCategory());
            existingExpenditure.setDebitDate(expenditure.getDebitDate());
            existingExpenditure.setDescription(expenditure.getDescription());
            existingExpenditure.setAmount(expenditure.getAmount());
            existingExpenditure.setAccount(expenditure.getAccount());
            existingExpenditure.setReceiver(expenditure.getReceiver());
            existingExpenditure.setChecked(expenditure.getChecked());
            em.merge(existingExpenditure);

            Transaction existingTransaction = existingExpenditure.getTransaction();
            transactionBean.save(existingTransaction);

            if(existingExpenditure.accountChanged()) {
                accountBean.updateBalance(previousAccount);
            }
        }
    }

    public void remove(String id) {
        Expenditure expenditure = em.find(Expenditure.class, id);
        if(expenditure != null) {
            transactionBean.remove(expenditure.getTransaction());
            em.remove(expenditure);
        }
    }
}