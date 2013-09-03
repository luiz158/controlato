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
package org.controlato.entity;

import java.util.ArrayList;
import java.util.List;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Adapts a mime email message to the application domain, considering a
 * UserAccount as a usual recipient.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class EmailMessage {

    private List<UserAccount> recipients;
    private String subject;
    private String body;

    public List<UserAccount> getRecipients() {
        return recipients;
    }

    public UserAccount getRecipient() {
        if(recipients != null) {
            return recipients.get(0);
        }

        return null;
    }

    public void setRecipients(List<UserAccount> recipients) {
        if(recipients == null) {
            return;
        }

        this.recipients = recipients;
    }

    public void setRecipient(UserAccount recipientTo) {
        if(recipients == null) {
            recipients = new ArrayList<>();
        }
        recipients.add(recipientTo);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public MimeMessage createMimeMessage(Session mailSession) {
        try {
            MimeMessage msg = new MimeMessage(mailSession);
            msg.setSubject(this.getSubject(), "UTF-8");
            Address[] jRecipients; // JavaMail recipients.

            if(recipients != null) {
                jRecipients = new Address[recipients.size()];
                for(int i = 0;i < recipients.size();i++) {
                    jRecipients[i] = new InternetAddress(recipients.get(i).getPostingEmail());
                }
                msg.setRecipients(RecipientType.TO, jRecipients);
            }

            msg.setText(getBody(), "UTF-8");
            msg.setHeader("Content-Type", "text/html;charset=UTF-8");

            return msg;
        } catch (MessagingException me) {
            throw new RuntimeException("Error when sending the mail confirmation.", me);
        }
    }
}