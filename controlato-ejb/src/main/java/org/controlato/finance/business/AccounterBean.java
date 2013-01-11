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
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.controlato.business.exception.EntityConsistencyException;
import org.controlato.finance.entity.Account;
import org.controlato.finance.entity.Transaction;
import org.controlato.finance.entity.TransactionType;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class AccounterBean {

    @EJB
    private TransactionBean transactionBean;

    @EJB
    private AccountBean accountBean;

    public BigDecimal calculateAccountBalance(Account account) {
        List<Transaction> transactions = transactionBean.findTransactions(account);
        BigDecimal balance = BigDecimal.ZERO;
        for(Transaction transaction: transactions) {
            if(transaction.getTransactionType() == TransactionType.CREDIT) {
                balance = balance.add(transaction.getAmount());
            }
            else {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        return balance;
    }

    /**
     * Verifies whether the account has transactions associated to it and only
     * allows removal of the account when it has no associated transaction,
     * thus the balance is zero.
     */
    public void removeAccount(String id) {
        Account account = accountBean.findAccount(id);
        if(account != null) {
            List<Transaction> transactions = transactionBean.findTransactions(account);
            if(transactions == null || transactions.isEmpty()) {
                accountBean.remove(account);
            }
            else {
                throw new EntityConsistencyException("errorCode0001");
            }
        }
    }
}