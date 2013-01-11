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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.controlato.finance.entity.Currency;

/**
 * Represents the user account.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name="user_account")
public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(name="email", nullable=false)
    private String email;

    @Column(name="unverified_email")
    private String unverifiedEmail;

    @Transient
    private String emailConfirmation;

    @ManyToOne
    @JoinColumn(name="language")
    private Language language;

    @ManyToOne
    @JoinColumn(name="currency")
    private Currency currency;

    @Column(name="confirmation_code")
    private String confirmationCode;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name="registration_date")
    private Date registrationDate;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name="last_update")
    private Date lastUpdate;

    @Column(name="deactivated", nullable=true)
    private Boolean deactivated = false;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name="deactivation_date")
    private Date deactivationDate;

    @Column(name="deactivation_reason")
    private String deactivationReason;

    @Enumerated(EnumType.STRING)
    @Column(name="deactivation_type")
    private DeactivationType deactivationType;

    public UserAccount() {}

    public UserAccount(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the email address of the user. Despite its validity, do not use
     * the returned value to send email messages to the user. Use getPostingEmail() instead.
     * @see #getPostingEmail()
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    /**
     * @return the unverifiedEmail is not null when the user's email is not
     * confirmed yet. Once the email is confirmed this method returns null.
     */
    public String getUnverifiedEmail() {
        return unverifiedEmail;
    }

    public void setUnverifiedEmail(String unverifiedEmail) {
        if(unverifiedEmail != null) {
            this.unverifiedEmail = unverifiedEmail.toLowerCase();
        }
        else {
            this.unverifiedEmail = null;
        }
    }

    /**
     * @return Independent of the verification of the email, this method returns
     * the available email address for posting email messages.
     */
    public String getPostingEmail() {
        /* In case there is an unverified email, it has the priority to be in
           the message recipient. */
        if(this.unverifiedEmail != null && !this.unverifiedEmail.isEmpty()) {
            return this.unverifiedEmail;
        }
        /* If unverified email is null it means that the email is valid and it
           can be used in the message recipient. */
        else {
            return this.email;
        }
    }

    public Language getLanguage() {
        if(this.language == null) {
            this.language = new Language();
        }
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Boolean getDeactivated() {
        if(deactivated != null) {
            return deactivated;
        }
        else {
            return false;
        }
    }

    public void setDeactivated(Boolean deactivated) {
        this.deactivated = deactivated;
    }

    public Date getDeactivationDate() {
        return deactivationDate;
    }

    public void setDeactivationDate(Date deactivationDate) {
        this.deactivationDate = deactivationDate;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public void setDeactivationReason(String deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    public DeactivationType getDeactivationType() {
        return deactivationType;
    }

    public void setDeactivationType(DeactivationType deactivationType) {
        this.deactivationType = deactivationType;
    }

    public String getEmailConfirmation() {
        return emailConfirmation;
    }

    public void setEmailConfirmation(String emailConfirmation) {
        emailConfirmation = emailConfirmation.toLowerCase();
        this.emailConfirmation = emailConfirmation;
    }

    public Boolean isEmailConfirmed() {
        if(this.unverifiedEmail != null) {
            return emailConfirmation.equals(unverifiedEmail);
        }
        else {
            return emailConfirmation.equals(email);
        }
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public void resetConfirmationCode() {
        this.confirmationCode = null;
    }

    public boolean getConfirmed() {
        return (confirmationCode == null);
    }

    @Override
    public String toString() {
        return getEmail();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserAccount other = (UserAccount) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}