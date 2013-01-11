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
import org.controlato.finance.entity.Place;
import org.controlato.finance.entity.Revenue;
import org.controlato.finance.entity.Transaction;
import org.controlato.util.CalendarUtils;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class RevenueBean implements OperationFilterable {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private TransactionBean transactionBean;

    private static final Calendar TODAY = Calendar.getInstance(TimeZone.getDefault());

    public Revenue findRevenue(String id) {
        return em.find(Revenue.class, id);
    }

    public List<Revenue> findRevenues(Account account) {
        if(account == null) {
            return null;
        }

        return em.createQuery("select r from Revenue r where r.account = :account order by r.creditDate desc")
                 .setParameter("account", account)
                 .getResultList();
    }

    public List<Revenue> findRevenues(Place place) {
        if(place == null) {
            return null;
        }

        return em.createQuery("select r from Revenue r where r.place = :place order by r.creditDate desc")
                 .setParameter("place", place)
                 .getResultList();
    }

    public List<Revenue> findRevenues(CategoryOperation category) {
        if(category == null) {
            return null;
        }

        return em.createQuery("select r from Revenue r where r.category = :category order by r.creditDate desc")
                 .setParameter("category", category)
                 .getResultList();
    }

    public List<Revenue> findRevenues(Integer year, Integer month, Integer day) {
        List<Revenue> revenues = new ArrayList<>();

        if(year == null && month == null && day == null) {
            return revenues;
        }

        Calendar[] interval = CalendarUtils.buildInterval(year, month, day);

        revenues = findRevenuesInInterval(interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        return revenues;
    }

    public List<Revenue> findRevenuesInInterval(Calendar from, Calendar to) {
        if(from == null) {
            from = Calendar.getInstance(TimeZone.getDefault());
        }

        if(to == null) {
            to = Calendar.getInstance(TimeZone.getDefault());
        }

        if(from.after(to)) {
            throw new IllegalArgumentException("fromBiggerThanTo");
        }

        return em.createQuery("select r from Revenue r where (r.creditDate >= :from and r.creditDate <= :to) order by r.creditDate desc")
                 .setParameter("from", from.getTime())
                 .setParameter("to", to.getTime())
                 .getResultList();
    }

    @Override
    public List<Integer> findYears() {
        List<Date> dates = em.createQuery("select r.creditDate from Revenue r order by r.creditDate desc").getResultList();
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
        return years;
    }

    @Override
    public List<Integer> findMonths(Integer year) {

        Calendar[] interval = CalendarUtils.buildInterval(year, null, null);

        List<Revenue> revenues = findRevenuesInInterval(interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        List<Integer> months = new ArrayList<>();
        Calendar cDate;
        Integer currentMonth;

        if(TODAY.get(Calendar.YEAR) == year) {
            months.add(TODAY.get(Calendar.MONTH));
        }

        for(Revenue revenue: revenues) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(revenue.getCreditDate());
            currentMonth = cDate.get(Calendar.MONTH);
            if(!months.contains(currentMonth)) {
                months.add(currentMonth);
            }
        }
        return months;
    }

    @Override
    public List<Integer> findDays(Integer year, Integer month) {

        Calendar[] interval = CalendarUtils.buildInterval(year, month, null);

        List<Revenue> revenues = findRevenuesInInterval(interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        List<Integer> days = new ArrayList<>();
        Calendar cDate;
        Integer currentDay;

        if(TODAY.get(Calendar.YEAR) == year && TODAY.get(Calendar.DAY_OF_MONTH) == month) {
            days.add(TODAY.get(Calendar.DAY_OF_MONTH));
        }

        for(Revenue revenue: revenues) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(revenue.getCreditDate());
            currentDay = cDate.get(Calendar.DAY_OF_MONTH);
            if(!days.contains(currentDay)) {
                days.add(currentDay);
            }
        }
        return days;
    }

    public void checkRevenue(Revenue revenue) {
        revenue = findRevenue(revenue.getId());
        if(revenue == null) {
            return;
        }

        if(revenue.getChecked() == null || revenue.getChecked().equals(Boolean.FALSE)) {
            revenue.setChecked(Boolean.TRUE);
        }
        else {
            revenue.setChecked(Boolean.FALSE);
        }
    }

    public void save(Revenue revenue) {
        /* When the revenue is new, a new transaction is created and associated to it. */
        if(revenue.getId() == null || revenue.getId().isEmpty()) {
            revenue.setId(EntitySupport.generateEntityId());

            Transaction transaction = revenue.createTransaction();
            transactionBean.save(transaction);

            em.persist(revenue);
        }
        else {
            Revenue existingRevenue = findRevenue(revenue.getId());

            existingRevenue.setCategory(revenue.getCategory());
            existingRevenue.setCreditDate(revenue.getCreditDate());
            existingRevenue.setDescription(revenue.getDescription());
            existingRevenue.setAmount(revenue.getAmount());
            existingRevenue.setPayer(revenue.getPayer());
            existingRevenue.setChecked(revenue.getChecked());
            em.merge(existingRevenue);

            Transaction existingTransaction = existingRevenue.getTransaction();
            transactionBean.save(existingTransaction);
        }
    }

    public void remove(String id) {
        Revenue revenue = em.find(Revenue.class, id);
        if(revenue != null) {
            transactionBean.remove(revenue.getTransaction());
            em.remove(revenue);
        }
    }
}