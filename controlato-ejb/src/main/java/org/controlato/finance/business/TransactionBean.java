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
import javax.persistence.Query;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.Transaction;
import org.controlato.util.CalendarUtils;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class TransactionBean implements OperationFilterable {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private AccountBean accountBean;

    private static final Calendar TODAY = Calendar.getInstance(TimeZone.getDefault());

    public Transaction findTransaction(String id) {
        return em.find(Transaction.class, id);
    }

    public List<Transaction> findTransactions(Account account) {
        if(account == null) {
            return null;
        }

        return em.createQuery("select t from Transaction t where t.account = :account order by t.dateRegistered desc")
                 .setParameter("account", account)
                 .getResultList();
    }

    public List<Transaction> findTransactions(Account account, Integer year, Integer month, Integer day) {
        List<Transaction> transactions = new ArrayList<>();

        if(year == null && month == null && day == null) {
            return transactions;
        }

        Calendar[] interval = CalendarUtils.buildInterval(year, month, day);

        transactions = findTransactionsInInterval(account, interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        return transactions;
    }

    public List<Transaction> findTransactionsInInterval(Account account, Calendar from, Calendar to) {
        if(from == null) {
            from = Calendar.getInstance(TimeZone.getDefault());
        }

        if(to == null) {
            to = Calendar.getInstance(TimeZone.getDefault());
        }

        if(from.after(to)) {
            throw new IllegalArgumentException("fromBiggerThanTo");
        }

        StringBuilder strQuery = new StringBuilder();
        strQuery.append("select t from Transaction t where t.dateRegistered >= :from and t.dateRegistered <= :to");
        if(account != null) {
            strQuery.append(" and t.account = :account");
        }
        strQuery.append(" order by t.dateRegistered desc");

        Query query = em.createQuery(strQuery.toString());
        query.setParameter("from", from.getTime());
        query.setParameter("to", to.getTime());
        if(account != null) {
            account = accountBean.findAccount(account.getId());
            query.setParameter("account", account);
        }

        return query.getResultList();
    }

    @Override
    public List<Integer> findYears() {
        List<Date> dates = em.createQuery("select t.dateRegistered from Transaction t order by t.dateRegistered desc").getResultList();
        List<Integer> years = new ArrayList<>();
        Calendar cDate;
        Integer currentYear;

        years.add(TODAY.get(Calendar.YEAR));

        for(Date dateRegistered: dates) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(dateRegistered);
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

        List<Transaction> transactions = findTransactionsInInterval(null, interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        List<Integer> months = new ArrayList<>();
        Calendar cDate;
        Integer currentMonth;

        if(TODAY.get(Calendar.YEAR) == year) {
            months.add(TODAY.get(Calendar.MONTH));
        }

        for(Transaction transaction: transactions) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(transaction.getDateRegistered());
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

        List<Transaction> transactions = findTransactionsInInterval(null, interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        List<Integer> days = new ArrayList<>();
        Calendar cDate;
        Integer currentDay;

        int todayYear = TODAY.get(Calendar.YEAR);
        int todayMonth  = TODAY.get(Calendar.MONTH);
        if(todayYear == year.intValue() && todayMonth == month.intValue()) {
            days.add(TODAY.get(Calendar.DAY_OF_MONTH));
        }

        for(Transaction transaction: transactions) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(transaction.getDateRegistered());
            currentDay = cDate.get(Calendar.DAY_OF_MONTH);
            if(!days.contains(currentDay)) {
                days.add(currentDay);
            }
        }
        Collections.sort(days);

        return days;
    }

    public void save(Transaction transaction) {
        if(transaction.getId() == null || transaction.getId().isEmpty()) {
            transaction.setId(EntitySupport.generateEntityId());
            em.persist(transaction);

            accountBean.updateBalance(transaction.getAccount());
        }
        else {
            Transaction existingTransaction = findTransaction(transaction.getId());

            existingTransaction.setAmount(transaction.getAmount());
            existingTransaction.setDescription(transaction.getDescription());
            existingTransaction.setDateRegistered(transaction.getDateRegistered());

            em.merge(existingTransaction);

            Account transactionAccount = existingTransaction.getAccount();
            accountBean.updateBalance(transactionAccount);
        }
    }

    public void remove(String id) {
        Transaction transaction = em.find(Transaction.class, id);
        remove(transaction);
    }

    public void remove(Transaction transaction) {
        if(transaction == null) {
            return;
        }

        Account account = transaction.getAccount();

        em.remove(transaction);

        if(account != null) {
            accountBean.updateBalance(account);
        }
    }
}