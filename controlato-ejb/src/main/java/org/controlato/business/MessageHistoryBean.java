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
package org.controlato.business;

import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.entity.MessageHistory;
import org.controlato.entity.UserAccount;
import org.controlato.util.EntitySupport;

/**
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class MessageHistoryBean {

    @PersistenceContext
    private EntityManager em;

    static final Logger logger = Logger.getLogger(MessageHistoryBean.class.getName());

    public MessageHistory findHistoricalMessage(String id) {
        if(id != null) {
            return em.find(MessageHistory.class, id);
        }
        else {
            return null;
        }
    }

    public List<MessageHistory> findHistoricalMessageByRecipient(UserAccount recipient) {
    	List<MessageHistory> messages = em.createQuery("select hm from MessageHistory hm where hm.recipient = :userAccount order by hm.dateSent desc")
                                          .setParameter("userAccount", recipient)
                                          .getResultList();
        return messages;
    }

    public MessageHistory saveHistoricalMessage(MessageHistory message) {
    	if(EntitySupport.INSTANCE.isIdNotValid(message)) {
            message.setId(EntitySupport.INSTANCE.generateEntityId());
            em.persist(message);
            return message;
        }
        else {
            return em.merge(message);
        }
    }

    public void removeHistoricalMessage(String id) {
        MessageHistory messageHistory = findHistoricalMessage(id);
        if(messageHistory != null) {
            em.remove(messageHistory);
        }
    }
}