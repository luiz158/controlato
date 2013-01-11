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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.controlato.business.LanguageBean;
import org.controlato.business.UserAccountBean;
import org.controlato.entity.Authentication;
import org.controlato.entity.Language;
import org.controlato.entity.UserAccount;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class UserAccountMBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserAccountBean userAccountBean;

    @EJB
    private LanguageBean languageBean;

    private String userId;
    private UserAccount userAccount;

    private String selectedLanguage;

    private String password;
    private String passwordConfirmation;

    public UserAccountMBean() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public Boolean getNoAccount() {
        return userAccountBean.noAccount();
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public List<SelectItem> getLanguages() {
        List<Language> languages = languageBean.findLanguages();

        List<SelectItem> selectLanguages = new ArrayList<>();
        ResourceBundle bundle = ResourceBundle.getInstance();
        SelectItem selectItem = new SelectItem("", bundle.getMessage("select"));
        selectLanguages.add(selectItem);
        for (Language language : languages) {
            selectItem = new SelectItem(language.getAcronym(), language.getName());
            selectLanguages.add(selectItem);
        }
        return selectLanguages;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Used to check if the user properly typed his password.
     */
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public boolean isConfirmed() {
        if (userAccount.getConfirmationCode() == null || userAccount.getConfirmationCode().isEmpty()) {
            return true;
        }
        return false;
    }

    public void validateUserId(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
        String usrId = (String) value;
        if (-1 == usrId.indexOf("@")) {
            throw new ValidatorException(new FacesMessage("Invalid email address."));
        }
    }

    @PostConstruct
    public void load() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String username = request.getRemoteUser();
        if (username != null) {
            this.userAccount = userAccountBean.findUserAccountByUsername(username);
        } else {
            this.userAccount = new UserAccount();
        }
    }

    public String register() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getInstance();

        if (!userAccount.isEmailConfirmed()) {
            context.addMessage(null, new FacesMessage(bundle.getMessage("emailConfirmationError")));
            context.validationFailed();
        }

        if (userAccountBean.existingAccount(this.userAccount.getUnverifiedEmail())) {
            context.addMessage(null, new FacesMessage(bundle.getMessage("emailDuplicationError")));
            context.validationFailed();
        }

        if (!isPasswordConfirmed()) {
            context.addMessage(null, new FacesMessage(bundle.getMessage("passwordConfirmationError")));
            context.validationFailed();
        }

        if(context.isValidationFailed()) {
            return "registration";
        }

        boolean isFirstUser = userAccountBean.noAccount();

        try {
            Authentication authentication = new Authentication();
            authentication.setUserAccount(this.userAccount);
            authentication.setUsername(this.userAccount.getUnverifiedEmail());
            authentication.setPassword(this.password);

            if (selectedLanguage != null && !selectedLanguage.isEmpty()) {
                userAccount.setLanguage(languageBean.findLanguage(selectedLanguage));
            }

            userAccountBean.register(userAccount, authentication);
        } catch (Exception e) {
            context.addMessage(userId, new FacesMessage(e.getCause().getMessage()));
            return "registration";
        }

        if (isFirstUser) {
            context.addMessage(userId, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getMessage("infoSuccessfulRegistration"), ""));
        } else {
            context.addMessage(userId, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getMessage("infoRegistrationConfirmationRequest"), ""));
        }

        return "registration_confirmation";
    }

    public String deactivateRegistration() {
        userAccountBean.deactivateOwnRegistration(userAccount);
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        try {
            request.logout();
            session.invalidate();
        } catch (ServletException se) {
        }

        return "/index?faces-redirect=true";
    }

    /**
     * Compares the informed password with its respective confirmation.
     * @return true if the password matches with its confirmation.
     */
    private boolean isPasswordConfirmed() {
        return this.password.equals(passwordConfirmation);
    }
}