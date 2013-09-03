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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Message template with variables to be fulfilled with object attributes.
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name = "message_template")
@IdClass(MessageTemplateId.class)
public class MessageTemplate implements Serializable {

    private static final String VAR_PATTERN = "\\#\\{([a-z][a-zA-Z_0-9]*(\\.)?)+\\}";

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Id
    @ManyToOne
    @JoinColumn(name = "language", nullable = false)
    private Language language;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    public MessageTemplate() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public EmailMessage replaceVariablesByValues(Map<String, Object> values) {
        EmailMessage emailMessage = new EmailMessage();
        String subject = this.title;
        String message = this.body;

        Pattern pattern = Pattern.compile(VAR_PATTERN);

        List<String> variables = findVariables(pattern, this.getTitle());
        Object value;
        for(String variable: variables) {
            variable = variable.substring(2, variable.length() - 1);
            value = values.get(variable);
            if(value != null) {
                subject = subject.replace("#{" + variable + "}", values.get(variable).toString());
            }
        }
        emailMessage.setSubject(subject);

        variables = findVariables(pattern, this.getBody());
        for(String variable: variables) {
            variable = variable.substring(2, variable.length() - 1);
            value = values.get(variable);
            if(value != null) {
                message = message.replace("#{" + variable + "}", values.get(variable).toString());
            }
        }
        emailMessage.setBody(message);

        return emailMessage;
    }

    private List<String> findVariables(Pattern pattern, CharSequence charSequence) {
        Matcher m = pattern.matcher(charSequence);
        List<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessageTemplate other = (MessageTemplate) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.title;
    }
}