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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import org.controlato.entity.AccessGroup;
import org.controlato.entity.ApplicationProperty;
import org.controlato.entity.Authentication;
import org.controlato.entity.DeactivationType;
import org.controlato.entity.Properties;
import org.controlato.entity.UserAccount;
import org.controlato.entity.UserGroup;
import org.controlato.util.Base64Encoder;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class UserAccountBean {

    @EJB
    private AccessGroupBean accessGroupBean;

    @EJB
    private UserGroupBean userGroupBean;

    @EJB
    private MessengerBean messengerBean;

    @EJB
    private ApplicationPropertyBean applicationPropertyBean;

    @PersistenceContext
    private EntityManager em;

    /**
     * Checks whether an user account exists.
     * @param username the username that unically identify users.
     * @return true if the account already exists.
     */
    public boolean existingAccount(String username) {
        if(username == null || username.isEmpty()) {
            throw new RuntimeException("It is not possible to check if the account exists because the username was not informed.");
        }

        UserAccount existing = findUserAccountByUsername(username);
        return existing != null;
    }

    /** Returns true is there is no account registered in the database. */
    public boolean noAccount() {
        Long totalUserAccounts = (Long)em.createQuery("select count(u) from UserAccount u").getSingleResult();
        if(totalUserAccounts == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    public UserAccount findUserAccount(String id) {
        return em.find(UserAccount.class, id);
    }

    public UserAccount findUserAccountByUsername(String username) {
        try {
            return (UserAccount) em.createQuery("select ua from UserAccount ua where ua.username = :username")
                                   .setParameter("username", username)
                                   .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    public UserAccount findUserAccountByEmail(String email) {
        try {
            return (UserAccount) em.createQuery("select ua from UserAccount ua where ua.email = :email")
                                   .setParameter("email", email)
                                   .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    public UserAccount findUserAccountByConfirmationCode(String confirmationCode) {
        try {
            return (UserAccount) em.createQuery("select ua from UserAccount ua where ua.confirmationCode = :confirmationCode")
                                   .setParameter("confirmationCode", confirmationCode)
                                   .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserAccount> findUserAccounts() {
        return em.createQuery("select ua from UserAccount ua where ua.deactivated = :deactivated and ua.confirmationCode is null order by ua.firstName")
                 .setParameter("deactivated", Boolean.FALSE)
                 .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<UserAccount> findUserAccountsOrderedByRegistration() {
        return em.createQuery("select ua from UserAccount ua where ua.confirmationCode is null and ua.deactivated = :deactivated order by ua.registrationDate")
                 .setParameter("deactivated", Boolean.FALSE)
                 .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<UserAccount> findRegisteredUsersSince(Date date) {
        return em.createQuery("select ua from UserAccount ua where ua.registrationDate >= :date and ua.deactivated = :deactivated order by ua.registrationDate desc")
                 .setParameter("date", date)
                 .setParameter("deactivated", Boolean.FALSE)
                 .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<UserAccount> findUserAccountsStartingWith(String firstLetter) {
        return em.createQuery("select ua from UserAccount ua where ua.firstName like '"+ firstLetter +"%' and ua.deactivated = :deactivated order by ua.firstName")
                 .setParameter("deactivated", Boolean.FALSE)
                 .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<UserAccount> findDeactivatedUserAccounts() {
        return em.createQuery("select ua from UserAccount ua where ua.deactivated = :deactivated order by ua.deactivationDate desc")
                 .setParameter("deactivated", Boolean.TRUE)
                 .getResultList();
    }

    /**
     * @param userAccount the id of the user who has authentication credentials registered.
     * @return the user's authentication data.
     */
    public Authentication findAuthenticationUser(String userAccount) {
        try {
            return (Authentication) em.createQuery("select a from Authentication a where a.userAccount.id = :userAccount")
                                   .setParameter("userAccount", userAccount)
                                   .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    /**
     * @param userAccount the user who has authentication credentials registered.
     * @return the user's authentication data.
     */
    public Authentication findAuthenticationUser(UserAccount userAccount) {
        try {
            return (Authentication) em.createQuery("select a from Authentication a where a.userAccount = :userAccount")
                                   .setParameter("userAccount", userAccount)
                                   .getSingleResult();
        }
        catch(NoResultException nre) {
            return null;
        }
    }

    public void register(UserAccount userAccount, Authentication authentication) {
        boolean noAccount = noAccount();

        userAccount.setConfirmationCode(generateConfirmationCode());
        userAccount.setRegistrationDate(Calendar.getInstance(TimeZone.getDefault()).getTime());
        userAccount.setId(EntitySupport.INSTANCE.generateEntityId());
        em.persist(userAccount);

        if(noAccount) {
            userAccount.resetConfirmationCode();
            AccessGroup adminGroup = accessGroupBean.findAdministrativeGroup();
            UserGroup userGroup = new UserGroup(adminGroup, authentication);
            userGroupBean.add(userGroup);
        }
        else {
            ApplicationProperty appProp = applicationPropertyBean.findApplicationProperty(Properties.SEND_EMAILS);
            if(appProp.sendEmailsEnabled()) {
                ApplicationProperty url = applicationPropertyBean.findApplicationProperty(Properties.URL);
                String serverAddress = url.getPropertyValue();
                messengerBean.sendEmailConfirmationRequest(userAccount, serverAddress);
            }
        }
    }

    public void confirmUser(String confirmationCode) {
        try {
            UserAccount userAccount = (UserAccount)em.createQuery("select ua from UserAccount ua where ua.confirmationCode = :code")
                                                     .setParameter("code", confirmationCode)
                                                     .getSingleResult();
            userAccount.setConfirmationCode(null);
            userAccount.setRegistrationDate(Calendar.getInstance(TimeZone.getDefault()).getTime());

            // This step effectivelly allows the user to access the application.
            AccessGroup defaultGroup = accessGroupBean.findUserDefaultGroup();
            Authentication authentication = findAuthenticationUser(userAccount);
            UserGroup userGroup = new UserGroup(defaultGroup, authentication);
            userGroupBean.add(userGroup);

            ApplicationProperty appProp = applicationPropertyBean.findApplicationProperty(Properties.SEND_EMAILS);
            if(appProp.sendEmailsEnabled()) {
                messengerBean.sendWelcomeMessage(userAccount);
            }

            if(appProp.sendEmailsEnabled()) {
                AccessGroup administrativeGroup = accessGroupBean.findAdministrativeGroup();
                List<UserAccount> leaders = userGroupBean.findUsersGroup(administrativeGroup);
                messengerBean.sendNewMemberAlertMessage(userAccount, leaders);
            }
        }
        catch(NoResultException nre) {
            throw new IllegalArgumentException("Confirmation code "+ confirmationCode +" does not match any existing pendent account.", nre.getCause());
        }
    }

    public void save(UserAccount userAccount) {
        userAccount.setLastUpdate(Calendar.getInstance(TimeZone.getDefault()).getTime());
        em.merge(userAccount);
    }

    public void deactivateRegistration(UserAccount userAccount) {
        UserAccount existingUserAccount = findUserAccount(userAccount.getId());

        existingUserAccount.setDeactivated(Boolean.TRUE);
        existingUserAccount.setDeactivationDate(Calendar.getInstance(TimeZone.getDefault()).getTime());
        existingUserAccount.setDeactivationReason(userAccount.getDeactivationReason());
        existingUserAccount.setDeactivationType(DeactivationType.ADMINISTRATIVE);

        save(existingUserAccount);
        userGroupBean.removeUserFromAllGroups(userAccount);

        ApplicationProperty appProp = applicationPropertyBean.findApplicationProperty(Properties.SEND_EMAILS);

        if(!existingUserAccount.getDeactivationReason().trim().isEmpty() && appProp.sendEmailsEnabled()) {
            messengerBean.sendDeactivationReason(existingUserAccount);
        }
        AccessGroup administrativeGroup = accessGroupBean.findAdministrativeGroup();
        List<UserAccount> leaders = userGroupBean.findUsersGroup(administrativeGroup);

        if(appProp.sendEmailsEnabled()) {
            messengerBean.sendDeactivationAlertMessage(existingUserAccount, leaders);
        }
    }

    public void deactivateOwnRegistration(UserAccount userAccount) {
        UserAccount existingUserAccount = findUserAccount(userAccount.getId());

        existingUserAccount.setDeactivated(Boolean.TRUE);
        existingUserAccount.setDeactivationDate(Calendar.getInstance(TimeZone.getDefault()).getTime());
        existingUserAccount.setDeactivationReason(userAccount.getDeactivationReason());
        existingUserAccount.setDeactivationType(DeactivationType.OWNWILL);

        ApplicationProperty appProp = applicationPropertyBean.findApplicationProperty(Properties.SEND_EMAILS);

        save(existingUserAccount);
        userGroupBean.removeUserFromAllGroups(userAccount);
        if(!existingUserAccount.getDeactivationReason().trim().isEmpty() && appProp.sendEmailsEnabled()) {
            messengerBean.sendDeactivationReason(existingUserAccount);
        }
        AccessGroup administrativeGroup = accessGroupBean.findAdministrativeGroup();
        List<UserAccount> leaders = userGroupBean.findUsersGroup(administrativeGroup);

        if(appProp.sendEmailsEnabled()) {
            messengerBean.sendDeactivationAlertMessage(existingUserAccount, leaders);
        }
    }

    private String encryptPassword(String string) {
        MessageDigest md = null;
        byte stringBytes[] = null;
        try {
            md = MessageDigest.getInstance("MD5");
            stringBytes = string.getBytes("UTF8");
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new SecurityException("The Requested encoding algorithm was not found in this execution platform.", nsae);
        }
        catch(UnsupportedEncodingException uee) {
            throw new SecurityException("UTF8 is not supported in this execution platform.", uee);
        }

        byte stringCriptBytes[] = md.digest(stringBytes);
        char[] encoded = Base64Encoder.encode(stringCriptBytes);
        return String.valueOf(encoded);
    }

    private String generateConfirmationCode() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

    public void requestConfirmationPasswordChange(String username, String serverAddress) {
        UserAccount userAccount = findUserAccountByUsername(username);

        ApplicationProperty appProp = applicationPropertyBean.findApplicationProperty(Properties.SEND_EMAILS);

        if(userAccount != null) {
            userAccount.setConfirmationCode(generateConfirmationCode());
            if(appProp.sendEmailsEnabled()) {
                messengerBean.sendConfirmationCode(userAccount, serverAddress);
            }
        }
        else {
            throw new PersistenceException("Usuário inexistente.");
        }
    }

    /**
     * Compares the informed password with the one stored in the database.
     * @param userAccount the user account that has authentication credentials.
     * @param passwordToCheck the password to be compared with the one in the database.
     * @return true if the password matches.
     */
    public Boolean passwordMatches(UserAccount userAccount, String passwordToCheck) {
        try {
            Authentication authentication = (Authentication) em.createQuery("select a from Authentication a where a.userAccount = :userAccount and a.password = :password")
                                            .setParameter("userAccount", userAccount)
                                            .setParameter("password", (new Authentication()).hashPassword(passwordToCheck))
                                            .getSingleResult();
            if(authentication != null) {
                return Boolean.TRUE;
            }
        }
        catch(NoResultException nre) {
            return Boolean.FALSE;
        }

        return Boolean.FALSE;
    }

    /**
     * @param userAccount account of the user who wants to change his password.
     * @param newPassword the new password of the user.
     */
    public void changePassword(UserAccount userAccount, String newPassword) {
        try {
            // Retrieve the user authentication where the password is saved.
            Authentication authentication = (Authentication) em.createQuery("select a from Authentication a where a.userAccount = :userAccount")
                                            .setParameter("userAccount", userAccount)
                                            .getSingleResult();
            if(authentication != null) {
                authentication.setPassword(newPassword);
                userAccount.resetConfirmationCode();
                save(userAccount);
            }
        }
        catch(NoResultException nre) {
            throw new RuntimeException("User account not found. It is not possible to change the password.");
        }
    }

    /**
     * Changes the email address of the user without having to repeat the
     * registration process.
     * @param userAccount the user account that intends to change its email address.
     * @param newEmail the new email address of the user account.
     * @exception BusinessLogicException in case the newEmail is already registered.
     */
    public void changeEmail(UserAccount userAccount, String newEmail) {
        // Check if the new email already exists in the UserAccounts
        UserAccount existingUserAccount = findUserAccountByEmail(newEmail);

        if(existingUserAccount != null) {
            throw new RuntimeException("errorCode0002");
        }

        // Change the email address in the UserAccount
        userAccount.setUnverifiedEmail(newEmail);
        em.merge(userAccount);

        // Since the email address is also the username, change the username in the Authentication and in the UserGroup
        userGroupBean.changeUsername(userAccount, newEmail);

        // Send an email to the user to confirm the new email address
        ApplicationProperty url = applicationPropertyBean.findApplicationProperty(Properties.URL);
        messengerBean.sendEmailVerificationRequest(userAccount, url.getPropertyValue());
    }


    public void confirmEmailChange(UserAccount userAccount) {
        if(userAccount.getUnverifiedEmail() == null) {
            throw new RuntimeException("errorCode0003");
        }

        userAccount.resetConfirmationCode();
        userAccount.setEmail(userAccount.getUnverifiedEmail());
        userAccount.setUnverifiedEmail(null);
        save(userAccount);
    }

    public void remove(String userId) {
        UserAccount userAccount = em.find(UserAccount.class, userId);
        em.remove(userAccount);
    }
}