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

import java.util.Map;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.controlato.business.UserAccountBean;
import org.controlato.entity.UserAccount;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class SecurityBackingMBean {

    @EJB
    private UserAccountBean userAccountBean;

    @ManagedProperty(value="#{sessionScope}")
    private Map<String, Object> sessionMap;

    public UserAccount getSignedUser() {
        return (UserAccount) sessionMap.get("signedUser");
    }

    public void setSignedUser(UserAccount signedUser) {
        sessionMap.remove("signedUser");
        if(null != signedUser) {
            sessionMap.put("signedUser", signedUser);
        }
    }

    public boolean isUserSignedIn() {
        return sessionMap.containsKey("signedUser");
    }

    public String login() {
        if(userAccountBean.noAccount()) {
            ResourceBundle bundle = ResourceBundle.getInstance();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getMessage("infoFirstUser"), ""));
            return "registration";
        }
        else {
            return "login?faces-redirect=true";
        }
    }

    public String logout() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        try {
            request.logout();
            session.invalidate();
        }
        catch(ServletException se) {}
        return "/index?faces-redirect=true";
    }

    public Boolean getIsUserLeader() {
        Boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        Object request = context.getExternalContext().getRequest();
        if(request instanceof HttpServletRequest) {
            result = ((HttpServletRequest)request).isUserInRole("administrator");
        }
        return result;
    }

    public Map<String, Object> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}