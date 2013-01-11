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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.controlato.business.LanguageBean;
import org.controlato.business.UserAccountBean;
import org.controlato.entity.Language;
import org.controlato.entity.UserAccount;
import org.controlato.web.util.ResourceBundle;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@SessionScoped
public class UserProfileMBean {

    @EJB
    private LanguageBean languageBean;

    @EJB
    private UserAccountBean userAccountBean;

    private UserAccount userAccount;

    private Language language;

    public UserProfileMBean() {
        this.language = new Language();
    }

    public UserAccount getUserAccount() {
        if (userAccount == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
            String username = request.getRemoteUser();
            this.userAccount = userAccountBean.findUserAccountByUsername(username);
            if (this.userAccount != null) {
                if (this.userAccount.getLanguage() != null) {
                    this.language = this.userAccount.getLanguage();
                }
            }
        }
        return userAccount;
    }

    public String getLanguage() {
        if (this.language != null) {
            return this.language.getAcronym();
        } else {
            this.language = new Language();
            return this.language.getAcronym();
        }
    }

    public String changeLanguage(String acronym) {
        this.language = languageBean.findLanguage(acronym);
        FacesContext fc = FacesContext.getCurrentInstance();
        Locale locale = new Locale(language.getAcronym());
        fc.getViewRoot().setLocale(locale);
        return "index?faces-redirect=true";
    }

    public List<SelectItem> getLanguages() {
        List<Language> languages = languageBean.findLanguages();

        List<SelectItem> selectLanguages = new ArrayList<>();
        ResourceBundle bundle = ResourceBundle.getInstance();
        SelectItem selectItem = new SelectItem("", bundle.getMessage("select"));
        selectLanguages.add(selectItem);
        for (Language lang : languages) {
            selectItem = new SelectItem(lang.getAcronym(), lang.getName());
            selectLanguages.add(selectItem);
        }
        return selectLanguages;
    }

    public String getSelectedLanguage() {
        if (this.language != null) {
            return this.language.getAcronym();
        } else {
            return null;
        }
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.language = languageBean.findLanguage(selectedLanguage);
    }

    public String save() {
        this.userAccount.setLanguage(language);
        userAccountBean.save(userAccount);

        return "profile?faces-redirect=true";
    }
}