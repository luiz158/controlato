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

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.finance.entity.Account;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class AccountBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private AccounterBean accounterBean;

    public Account findAccount(String id) {
        if(id == null || id.isEmpty()) {
            return null;
        }

        return em.find(Account.class, id);
    }

    public List<Account> findAccounts() {
        return em.createQuery("select a from Account a order by a.name asc").getResultList();
    }

    /**
     * Returns a list with all accounts except the one passed by parameter. If the
     * parameter is null, then the returned list will contain all accounts.
     */
    public List<Account> findAccounts(Account except) {
        if(except == null) {
            return findAccounts();
        }

        return em.createQuery("select a from Account a where a != :except order by a.name asc")
                 .setParameter("except", except)
                 .getResultList();
    }

    public void save(Account account) {
        if(account.getId() == null || account.getId().isEmpty()) {
            account.setId(EntitySupport.generateEntityId());
            em.persist(account);
        }
        else {
            Account existingAccount = findAccount(account.getId());
            existingAccount.setName(account.getName());
            existingAccount.setReferenceNumber(account.getReferenceNumber());
            existingAccount.setIban(account.getIban());
            existingAccount.setSwift(account.getSwift());
            em.merge(existingAccount);
        }
    }

    public void updateBalance(Account account) {
        if(account != null) {
            Account existingAccount = findAccount(account.getId());
            existingAccount.setBalance(accounterBean.calculateAccountBalance(existingAccount));
            em.merge(existingAccount);
        }
    }

    /**
     * Removes the account without verifying whether it is possible.
     * @see org.controlato.finance.business.AccounterBean to remove an account
     * after checking if there is not transactions for that account.
     */
    void remove(Account account) {
        if(account != null) {
            em.remove(account);
        }
    }
}