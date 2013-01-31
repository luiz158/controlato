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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.entity.ApplicationProperty;
import org.controlato.entity.DeactivationType;
import org.controlato.entity.EmailMessage;
import org.controlato.entity.HistoryMessage;
import org.controlato.entity.MessageTemplate;
import org.controlato.entity.MessageTemplateId;
import org.controlato.entity.Properties;
import org.controlato.entity.UserAccount;
import org.controlato.util.EntitySupport;

/**
 * This class deals with all email messages that have to be sent to system users.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class MessengerBean {

    public static final Logger LOGGER = Logger.getLogger(MessengerBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Resource(name = "mail/controlato")
    private Session mailSession;

    @EJB
    private MessageTemplateBean messageTemplateBean;

    @EJB
    private ApplicationPropertyBean applicationPropertyBsn;

    /**
     * If the application is configured to send emails, it creates a email message
     * based on the message template. The message is saved in the history and an
     * attempt to send the message is done. If successful the historical message
     * is set as sent, otherwise the message is set as not sent and new attempts
     * will be carried out later until the sessage is successfully sent.
     * @param recipients The list of users for who the message will be sent.
     * @param messageTemplate The template of the message to be sent.
     */
    public void sendEmailMessage(List<UserAccount> recipients, MessageTemplate messageTemplate) throws MessagingException {
        ApplicationProperty appProp = applicationPropertyBsn.findApplicationProperty(Properties.SEND_EMAILS);
        if(appProp.sendEmailsEnabled()) {
            EmailMessage emailMessage;
            HistoryMessage historyMessage;
            MessagingException messagingException = null;
            for(UserAccount recipient: recipients) {
                emailMessage = new EmailMessage();
                emailMessage.setRecipient(recipient);
                emailMessage.setSubject(messageTemplate.getTitle());
                emailMessage.setBody(messageTemplate.getBody());

                historyMessage = new HistoryMessage(emailMessage);
                historyMessage = saveHistoryMessage(historyMessage);

                try {
                    Transport.send(emailMessage.createMimeMessage(mailSession));
                    historyMessage.setMessageSent(Boolean.TRUE);
                    historyMessage.setDateSent(Calendar.getInstance().getTime());
                }
                catch(MessagingException me) {
                    historyMessage.setMessageSent(Boolean.FALSE);
                    messagingException = me;
                }
            }

            if(messagingException != null) {
                throw messagingException;
            }
        }
    }

    public void sendEmailMessage(UserAccount recipient, MessageTemplate messageTemplate) throws MessagingException {
        List<UserAccount> recipients = new ArrayList<>();
        recipients.add(recipient);
        sendEmailMessage(recipients, messageTemplate);
    }

    public void sendEmailConfirmationRequest(UserAccount userAccount, String serverAddress) {
        MessageTemplate messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("E3F122DCC87D42248872878412B34CEE", userAccount.getLanguage().getAcronym()));
        em.detach(messageTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("serverAddress", serverAddress);
        values.put("userAccount.confirmationCode", userAccount.getConfirmationCode());
        messageTemplate.replaceVariablesByValues(values);

        try {
            sendEmailMessage(userAccount, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending the mail confirmation. The registration was not finalized.", me);
        }
    }

    public void sendWelcomeMessage(UserAccount userAccount) {
        MessageTemplate messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("47DEE5C2E0E14F8BA4605F3126FBFAF4", userAccount.getLanguage().getAcronym()));
        em.detach(messageTemplate);
        try {
            sendEmailMessage(userAccount, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending the deactivation reason to user "+ userAccount.getPostingEmail(), me);
        }
    }

    public void sendNewMemberAlertMessage(UserAccount userAccount, List<UserAccount> leaders) {
        MessageTemplate messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("0D6F96382D91454F8155A720F3326F1B", userAccount.getLanguage().getAcronym()));
        em.detach(messageTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("userAccount.registrationDate", userAccount.getRegistrationDate());
        messageTemplate.replaceVariablesByValues(values);

        try {
            sendEmailMessage(leaders, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending alert to administrators about the registration of "+ userAccount.getPostingEmail(), me);
        }
    }

    public void sendDeactivationReason(UserAccount userAccount) {
        MessageTemplate messageTemplate;
        if(userAccount.getDeactivationType() == DeactivationType.ADMINISTRATIVE) {
            messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("03BD6F3ACE4C48BD8660411FC8673DB4", userAccount.getLanguage().getAcronym()));
        }
        else {
            messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("IKWMAJSNDOE3F122DCC87D4224887287", userAccount.getLanguage().getAcronym()));
        }

        em.detach(messageTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("userAccount.deactivationReason", userAccount.getDeactivationReason());
        messageTemplate.replaceVariablesByValues(values);

        try {
            sendEmailMessage(userAccount, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending the deactivation reason to user "+ userAccount.getPostingEmail(), me);
        }
    }

    public void sendDeactivationAlertMessage(UserAccount userAccount, List<UserAccount> leaders) {
        MessageTemplate messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("0D6F96382IKEJSUIWOK5A720F3326F1B", userAccount.getLanguage().getAcronym()));
        em.detach(messageTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("userAccount.deactivationReason", userAccount.getDeactivationReason());

        messageTemplate.replaceVariablesByValues(values);

        try {
            sendEmailMessage(leaders, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending the deactivation reason from "+ userAccount.getPostingEmail() +" to leaders.", me);
        }
    }

    public void sendConfirmationCode(UserAccount userAccount, String serverAddress) {
        MessageTemplate messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("67BE6BEBE45945D29109A8D6CD878344", userAccount.getLanguage().getAcronym()));
        em.detach(messageTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("serverAddress", serverAddress);
        values.put("userAccount.confirmationCode", userAccount.getConfirmationCode());
        messageTemplate.replaceVariablesByValues(values);

        try {
            sendEmailMessage(userAccount, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending the mail confirmation. The registration was not finalized.", me);
        }
    }

    /**
     * Sends a email to the user that requested to change his/her email address,
     * asking him/her to confirm the request by clicking on the informed link. If
     * the user successfully click on the link it means that his/her email address
     * is valid since he/she could receive the email message successfully.
     * @param userAccount the user who wants to change his/her email address.
     * @param serverAddress the URL of the server where the application is deployed.
     * it will be used to build the URL that the user will click to validate his/her
     * email address.
     */
    public void sendEmailVerificationRequest(UserAccount userAccount, String serverAddress) {
        MessageTemplate messageTemplate = messageTemplateBean.findMessageTemplate(new MessageTemplateId("KJZISKQBE45945D29109A8D6C92IZJ89", userAccount.getLanguage().getAcronym()));
        em.detach(messageTemplate);
        Map<String, Object> values = new HashMap<>();
        values.put("serverAddress", serverAddress);
        values.put("userAccount.email", userAccount.getEmail());
        values.put("userAccount.unverifiedEmail", userAccount.getUnverifiedEmail());
        values.put("userAccount.confirmationCode", userAccount.getConfirmationCode());
        messageTemplate.replaceVariablesByValues(values);

        try {
            sendEmailMessage(userAccount, messageTemplate);
        }
        catch(MessagingException me) {
            LOGGER.log(Level.WARNING, "Error when sending the mail confirmation. The registration was not finalized.", me);
        }
    }

    public HistoryMessage findHistoryMessage(String id) {
        if(id != null) {
            return em.find(HistoryMessage.class, id);
        }
        else {
            return null;
        }
    }

    public List<HistoryMessage> findHistoryMessageByRecipient(UserAccount recipient) {
    	List<HistoryMessage> messages = em.createQuery("select hm from HistoryMessage hm where hm.recipient = :userAccount order by hm.dateSent desc")
                                          .setParameter("userAccount", recipient)
                                          .getResultList();
        return messages;
    }

    public HistoryMessage saveHistoryMessage(HistoryMessage message) {
    	if(EntitySupport.INSTANCE.isIdNotValid(message)) {
            message.setId(EntitySupport.INSTANCE.generateEntityId());
            em.persist(message);
            return message;
        }
        else {
            return em.merge(message);
        }
    }

    public void removeHistoryMessage(String id) {
        HistoryMessage historyMessage = findHistoryMessage(id);
        if(historyMessage != null) {
            em.remove(historyMessage);
        }
    }
}