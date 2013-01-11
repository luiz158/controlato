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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.entity.EmailMessage;
import org.controlato.entity.MessageTemplate;
import org.controlato.entity.MessageTemplateId;

/**
 * Business logic related to MessageTemplate entity class.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class MessageTemplateBean {

    @PersistenceContext
    private EntityManager em;

    private static final String VAR_PATTERN = "\\#\\{([a-z][a-zA-Z_0-9]*(\\.)?)+\\}";

    public MessageTemplate findMessageTemplate(MessageTemplateId id) {
        return em.find(MessageTemplate.class, id);
    }

    public List<MessageTemplate> findMessageTemplates() {
        return em.createQuery("select mt from MessageTemplate mt order by mt.title").getResultList();
    }

    public void save(MessageTemplate messageTemplate) {
        em.merge(messageTemplate);
    }

    public void applyEmailMessageTemplate(EmailMessage emailMessage, MessageTemplate template, Map<String, Object> values) {
        Pattern pattern = Pattern.compile(VAR_PATTERN);
        List<String> variables = findVariables(pattern, template.getBody());
        String body = template.getBody();
        Object value;
        for(String variable: variables) {
            variable = variable.substring(2, variable.length() - 1);
            value = values.get(variable);
            if(value != null) {
                body = body.replace("#{" + variable + "}", values.get(variable).toString());
            }
        }
        emailMessage.setSubject(template.getTitle());
        emailMessage.setBody(body);
    }

    private List<String> findVariables(Pattern pattern, CharSequence charSequence) {
        Matcher m = pattern.matcher(charSequence);
        List<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }
}