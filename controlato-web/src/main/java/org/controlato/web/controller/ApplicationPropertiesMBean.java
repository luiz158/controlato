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

import java.io.Serializable;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.controlato.business.ApplicationPropertyBean;
import org.controlato.entity.Properties;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class ApplicationPropertiesMBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private ApplicationPropertyBean applicationPropertyBean;

    private Map<String, String> applicationProperties;

    private Boolean sendEmails;

    public Map<String, String> getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(Map<String, String> applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Boolean getSendEmails() {
        return sendEmails;
    }

    public void setSendEmails(Boolean sendEmails) {
        this.sendEmails = sendEmails;
    }

    public String save() {
        this.applicationProperties.put(Properties.SEND_EMAILS.getKey(), sendEmails.toString());
        applicationPropertyBean.save(this.applicationProperties);

        ResourceBundle bundle = ResourceBundle.getInstance();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getMessage("infoPropertiesSaved"), ""));

        return "properties";
    }

    @PostConstruct
    public void load() {
        applicationProperties = applicationPropertyBean.findApplicationProperties();

        if(applicationProperties.get(Properties.URL.getKey()).toString().equals("")) {
            applicationProperties.put(Properties.URL.getKey(), getUrl());
        }

        if(applicationProperties.get(Properties.SEND_EMAILS.getKey()).equals("true")) {
            sendEmails = true;
        }
    }

    private String getUrl() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String serverAddress = serverName + (serverPort != 80 ? ":" + serverPort : "") + (contextPath.equals("") ? "" : contextPath);
        return serverAddress;
    }
}