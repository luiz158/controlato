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
package org.controlato.web.controller;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.controlato.entity.MessageTemplate;
import org.controlato.entity.MessageTemplateId;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class MessageTemplateMBean {

    @EJB
    private org.controlato.business.MessageTemplateBean messageTemplateBean;

    @ManagedProperty(value="#{param.id}")
    private String id;

    @ManagedProperty(value="#{param.lang}")
    private String languageId;

    private MessageTemplate messageTemplate;

    public MessageTemplateMBean() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguageId() {
		return languageId;
	}

	public void setLanguageId(String languageId) {
		this.languageId = languageId;
	}

	public MessageTemplate getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(MessageTemplate messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public List<MessageTemplate> getMessageTemplates() {
        return messageTemplateBean.findMessageTemplates();
    }

    @PostConstruct
    public void load() {
        if(id != null && !id.isEmpty()) {
        	MessageTemplateId messageTemplateId = new MessageTemplateId(id, languageId);
            this.messageTemplate = messageTemplateBean.findMessageTemplate(messageTemplateId);
        }
        else {
            this.messageTemplate = new MessageTemplate();
        }
    }

    public String save() {
    	MessageTemplateId messageTemplateId = new MessageTemplateId(this.id, this.languageId);
    	MessageTemplate mt = messageTemplateBean.findMessageTemplate(messageTemplateId);
    	mt.setTitle(this.messageTemplate.getTitle());
    	mt.setBody(this.messageTemplate.getBody());
        messageTemplateBean.save(mt);
        return "message_templates?faces-redirect=true";
    }
}