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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.controlato.business.ApplicationPropertyBean;
import org.controlato.business.UserAccountBean;
import org.controlato.entity.ApplicationProperty;
import org.controlato.entity.Authentication;
import org.controlato.entity.Properties;
import org.controlato.entity.UserAccount;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class ChangePasswordMBean {

    @EJB
    private UserAccountBean userAccountBean;

    @ManagedProperty(value="#{param.cc}")
    private String confirmationCode;

    @EJB
    private ApplicationPropertyBean applicationPropertyBean;

    private String currentPassword;
    private String username;
    private String password;
    private String confirmPassword;
    private Boolean invalid;

    public ChangePasswordMBean() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public Boolean getInvalid() {
        return invalid;
    }

    public void setInvalid(Boolean invalid) {
        this.invalid = invalid;
    }

    @PostConstruct
    public void load() {
        if(confirmationCode != null && !confirmationCode.isEmpty()) {
            UserAccount userAccount = userAccountBean.findUserAccountByConfirmationCode(confirmationCode);
            Authentication authentication = userAccountBean.findAuthenticationUser(userAccount);
            if(userAccount != null) {
                this.username = authentication.getUsername();
            }
            else {
                invalid = true;
            }
        }
    }

    public String requestPasswordChange() {
        try {
            ApplicationProperty url = applicationPropertyBean.findApplicationProperty(Properties.URL);
            String serverAddress = url.getPropertyValue();
            userAccountBean.requestConfirmationPasswordChange(username, serverAddress);
        }
        catch(EJBException ee) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(ee.getCausedByException().getMessage()));
            return "request_password_change";
        }
        return "change_password";
    }

    /**
     * It changes the password in case the user has forgotten it. It checks whether
     * the confirmation code sent to the user's email is valid before proceeding
     * with the password change.
     * @return returns the next step in the navigation flow.
     */
    public String changeForgottenPassword() {
        UserAccount userAccount = userAccountBean.findUserAccountByConfirmationCode(confirmationCode);

        if(userAccount == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("The confirmation code does not match."));
            return "change_password";
        }

        if(!isPasswordConfirmed()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("The password confirmation does not match."));
            return "change_password";
        }

        userAccountBean.changePassword(userAccount, this.password);
        return "login?faces-redirect=true";
    }

    /**
     * It changes the password in case the user still knows his(er) own password.
     * @return returns the next step in the navigation flow.
     */
    public String changePassword() {
        HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        username = request.getRemoteUser();
        UserAccount userAccount = userAccountBean.findUserAccountByUsername(username);
        if(!userAccountBean.passwordMatches(userAccount, currentPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("The current password does not match."));
            return "change_password";
        }

        // If password doesn't match its confirmation.
        if(!isPasswordConfirmed()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("The password confirmation does not match."));
            return "change_password";
        }

        userAccountBean.changePassword(userAccount, this.password);
        return "profile?faces-redirect=true";
    }

    /**
     * Compares the informed password with its respective confirmation.
     * @return true if the password matches with its confirmation.
     */
    private boolean isPasswordConfirmed() {
        return password.equals(confirmPassword);
    }
}