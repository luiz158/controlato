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
import org.controlato.finance.entity.Transaction;
import org.controlato.finance.entity.Transfer;
import org.controlato.util.CalendarUtils;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class TransferBean implements OperationFilterable {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private AccountBean accountBean;

    @EJB
    private TransactionBean transactionBean;

    private static final Calendar TODAY = Calendar.getInstance(TimeZone.getDefault());

    public Transfer findTransfer(String id) {
        return em.find(Transfer.class, id);
    }

    public List<Transfer> findTransfers(Account account) {
        if(account == null) {
            return null;
        }

        return em.createQuery("select t from Transfer t where t.sourceAccount = :account or t.targetAccount = :account order by T.dateTransfer desc")
                 .setParameter("account", account)
                 .getResultList();
    }

    public List<Transfer> findTransfers(Integer year, Integer month, Integer day) {
        List<Transfer> transfers = new ArrayList<>();

        if(year == null && month == null && day == null) {
            return transfers;
        }

        Calendar[] interval = CalendarUtils.buildInterval(year, month, day);

        transfers = findTransfersInInterval(interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        return transfers;
    }

    public List<Transfer> findTransfersInInterval(Calendar from, Calendar to) {
        if(from == null) {
            from = Calendar.getInstance(TimeZone.getDefault());
        }

        if(to == null) {
            to = Calendar.getInstance(TimeZone.getDefault());
        }

        if(from.after(to)) {
            throw new IllegalArgumentException("fromBiggerThanTo");
        }

        return em.createQuery("select t from Transfer t where (t.dateTransfer >= :from and t.dateTransfer <= :to) order by t.dateTransfer desc")
                 .setParameter("from", from.getTime())
                 .setParameter("to", to.getTime())
                 .getResultList();
    }

    @Override
    public List<Integer> findYears() {
        List<Date> dates = em.createQuery("select t.dateTransfer from Transfer t order by t.dateTransfer desc").getResultList();
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

        List<Transfer> transfers = findTransfersInInterval(interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        List<Integer> months = new ArrayList<>();
        Calendar cDate;
        Integer currentMonth;

        if(TODAY.get(Calendar.YEAR) == year) {
            months.add(TODAY.get(Calendar.MONTH));
        }

        for(Transfer transfer: transfers) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(transfer.getDateTransfer());
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

        List<Transfer> transfers = findTransfersInInterval(interval[CalendarUtils.FROM], interval[CalendarUtils.TO]);

        List<Integer> days = new ArrayList<>();
        Calendar cDate;
        Integer currentDay;

        if(TODAY.get(Calendar.YEAR) == year && TODAY.get(Calendar.DAY_OF_MONTH) == month) {
            days.add(TODAY.get(Calendar.DAY_OF_MONTH));
        }

        for(Transfer transfer: transfers) {
            cDate = Calendar.getInstance(TimeZone.getDefault());
            cDate.setTime(transfer.getDateTransfer());
            currentDay = cDate.get(Calendar.DAY_OF_MONTH);
            if(!days.contains(currentDay)) {
                days.add(currentDay);
            }
        }
        return days;
    }

    public void checkTransfer(Transfer transfer) {
        transfer = findTransfer(transfer.getId());
        if(transfer == null) {
            return;
        }

        if(transfer.getChecked() == null || transfer.getChecked().equals(Boolean.FALSE)) {
            transfer.setChecked(Boolean.TRUE);
        }
        else {
            transfer.setChecked(Boolean.FALSE);
        }
    }

    public void save(Transfer transfer) {
       if(EntitySupport.INSTANCE.isIdNotValid(transfer)) {
            transfer.setId(EntitySupport.INSTANCE.generateEntityId());

            Transaction sourceTransaction = transfer.createSourceTransaction();
            transactionBean.save(sourceTransaction);

            Transaction targetTransaction = transfer.createTargetTransaction();
            transactionBean.save(targetTransaction);

            em.persist(transfer);
        }
        else {
            Transfer existingTransfer = findTransfer(transfer.getId());

            existingTransfer.setDescription(transfer.getDescription());
            existingTransfer.setAmount(transfer.getAmount());
            existingTransfer.setDateTransfer(transfer.getDateTransfer());
            existingTransfer.setSourceAccount(accountBean.findAccount(transfer.getSourceAccount().getId()));
            existingTransfer.setTargetAccount(accountBean.findAccount(transfer.getTargetAccount().getId()));
            existingTransfer.setChecked(transfer.getChecked());
            em.merge(existingTransfer);

            Transaction existingSourceTransaction = existingTransfer.getSourceTransaction();
            transactionBean.save(existingSourceTransaction);

            Transaction existingTargetTransaction = existingTransfer.getTargetTransaction();
            transactionBean.save(existingTargetTransaction);
        }
    }

    public void remove(String id) {
        Transfer transfer = findTransfer(id);
        if(transfer != null) {
            transactionBean.remove(transfer.getSourceTransaction());
            transactionBean.remove(transfer.getTargetTransaction());
            em.remove(transfer);
        }
    }
}