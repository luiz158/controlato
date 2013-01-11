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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.controlato.business.UserAccountBean;
import org.controlato.entity.UserAccount;
import org.controlato.web.report.MembershipGrowthRange;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@SessionScoped
public class MemberMBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserAccountBean userAccountBean;

    private List<UserAccount> userAccounts;

    private String userId;
    private UserAccount userAccount;
    private String emailCriteria;
    private String firstLetterCriteria;

    public MemberMBean() {
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

    public List<UserAccount> getUserAccounts() {
        return this.userAccounts;
    }

    public List<UserAccount> getRecentUserAccounts() {
        return userAccountBean.findRegisteredUsersSince(getLastSevenDays());
    }

    public List<UserAccount> getDeactivatedUserAccounts() {
        List<UserAccount> deactivatedUsers = userAccountBean.findDeactivatedUserAccounts();
        return deactivatedUsers;
    }

    public String findUserAccountByEmail() {
        List<UserAccount> uas = new ArrayList<>(1);
        UserAccount ua = userAccountBean.findUserAccountByEmail(this.emailCriteria);
        if(ua != null) {
            uas.add(ua);
        }
        this.userAccounts = uas;
        this.firstLetterCriteria = null;
        return "users?faces-redirect=true";
    }

    public String findUserAccountByFirstLetter(String firstLetterCriteria) {
        this.firstLetterCriteria = firstLetterCriteria;
        this.userAccounts = userAccountBean.findUserAccountsStartingWith(this.firstLetterCriteria);
        this.emailCriteria = null;

        return "users?faces-redirect=true";
    }

    public List<MembershipGrowthRange> getMembershipGrowthRanges() {
        return MembershipGrowthRange.generateSeries(userAccountBean.findUserAccountsOrderedByRegistration());
    }

    public Date getLastSevenDays() {
        Calendar sevenDaysAgo = Calendar.getInstance(TimeZone.getDefault());
        sevenDaysAgo.add(Calendar.DAY_OF_YEAR, -7);
        return sevenDaysAgo.getTime();
    }

    public String getEmailCriteria() {
        return emailCriteria;
    }

    public void setEmailCriteria(String emailCriteria) {
        this.emailCriteria = emailCriteria;
    }

    public String getFirstLetterCriteria() {
        return firstLetterCriteria;
    }

    public void setFirstLetterCriteria(String firstLetterCriteria) {
        this.firstLetterCriteria = firstLetterCriteria;
    }

    public boolean isConfirmed() {
        if(userAccount.getConfirmationCode() == null || userAccount.getConfirmationCode().isEmpty()) {
            return true;
        }
        return false;
    }

    public void validateUserId(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
        String usrId = (String) value;
        if(-1 == usrId.indexOf("@")) {
            throw new ValidatorException(new FacesMessage("Invalid email address."));
        }
    }

    @PostConstruct
    public void load() {
        this.userAccounts = getRecentUserAccounts();
    }

    public String load(String userId) {
        this.userId = userId;
        this.userAccount = userAccountBean.findUserAccount(this.userId);
        return "user?faces-redirect=true";
    }

    public String save() {
        UserAccount existingUserAccount = userAccountBean.findUserAccount(userAccount.getId());

        userAccountBean.save(existingUserAccount);
        return "users?faces-redirect=true";
    }

    public String deactivateMembership() {
        userAccountBean.deactivateRegistration(userAccount);
        return "users?faces-redirect=true";
    }

    public String confirm() {
        try {
            userAccountBean.confirmUser(userAccount.getConfirmationCode());
        } catch(IllegalArgumentException iae) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(iae.getMessage()));
            return "user";
        }
        removeSessionScoped();
        return "users?faces-redirect=true";
    }

    public String remove() {
        userAccountBean.remove(this.userAccount.getId());
        return "users?faces-redirect=true";
    }

    private void removeSessionScoped() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getSessionMap().remove("memberBean");
    }
}