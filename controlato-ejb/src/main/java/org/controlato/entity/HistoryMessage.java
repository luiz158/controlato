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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name = "history_message")
public class HistoryMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @ManyToOne
    @JoinColumn(name="recipient", nullable=false)
    private UserAccount recipient;

    @Column(name = "date_sent", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSent;

    @Column(name="message_sent")
    private Boolean messageSent;

    public HistoryMessage () {}

    public HistoryMessage(EmailMessage emailMessage) {
        this.subject = emailMessage.getSubject();
        this.body = emailMessage.getBody();
        this.recipient = emailMessage.getRecipient();
    }

    /**
     * @return A list of historical messages, one for each recipient of the message.
     */
    public static List<HistoryMessage> createHistoricMessages(EmailMessage emailMessage) {
        List<HistoryMessage> historicMessages = new ArrayList<>();
        HistoryMessage historicMessage;

        for(UserAccount userAccount: emailMessage.getRecipients()) {
            historicMessage = new HistoryMessage();
            historicMessage.setRecipient(userAccount);
            historicMessage.setSubject(emailMessage.getSubject());
            historicMessage.setBody(emailMessage.getBody());
            historicMessages.add(historicMessage);
        }

        return historicMessages;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    /**
     * @return the recipient
     */
    public UserAccount getRecipient() {
        return recipient;
    }

    /**
     * @param recipient the user account for whom the message was sent.
     */
    public void setRecipient(UserAccount recipient) {
        this.recipient = recipient;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    /**
     * @return true if the message was sent to the user by email. False if it was
     * not possible to send the message.
     */
    public Boolean getMessageSent() {
        return messageSent;
    }

    /**
     * @param messageSent set true if it was possible to send the message, or
     * false if some exception ocurred.
     */
    public void setMessageSent(Boolean messageSent) {
        this.messageSent = messageSent;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof HistoryMessage)) {
            return false;
        }
        HistoryMessage other = (HistoryMessage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.subject;
    }
}