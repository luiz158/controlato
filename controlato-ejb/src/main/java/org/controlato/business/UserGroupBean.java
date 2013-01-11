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

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.entity.AccessGroup;
import org.controlato.entity.UserAccount;
import org.controlato.entity.UserGroup;

/**
 * Business logic to manage the relationship between users and access groups.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class UserGroupBean {

    @PersistenceContext
    EntityManager em;

    @EJB
    AccessGroupBean accessGroupBean;

    public List<UserAccount> findUsersGroup(AccessGroup accessGroup) {
        return em.createQuery("select ug.userAccount from UserGroup ug where ug.accessGroup = :accessGroup order by ug.userAccount.firstName")
                 .setParameter("accessGroup", accessGroup)
                 .getResultList();
    }

    public List<UserGroup> findUsersGroups(AccessGroup accessGroup) {
        return em.createQuery("select ug from UserGroup ug where ug.accessGroup = :accessGroup")
                 .setParameter("accessGroup", accessGroup)
                 .getResultList();
    }

    /**
     * @param userAccount the user account that is member of one or more groups.
     * @return the list of groups registrations of the informed user account.
     */
    public List<UserGroup> findUsersGroups(UserAccount userAccount) {
        return em.createQuery("select ug from UserGroup ug where ug.userAccount = :userAccount")
                 .setParameter("userAccount", userAccount)
                 .getResultList();
    }

    public List<AccessGroup> findGroupsUser(UserAccount userAccount) {
        return em.createQuery("select ug.accessGroup from UserGroup ug where ug.userAccount = :userAccount")
                 .setParameter("userAccount", userAccount)
                 .getResultList();
    }

    public void update(AccessGroup accessGroup, List<UserGroup> userGroups) {
        if(userGroups.isEmpty()) {
            em.createQuery("delete from UserGroup ug where ug.accessGroup = :accessGroup")
                    .setParameter("accessGroup", accessGroup)
                    .executeUpdate();
            return;
        }

        List<UserGroup> currentUserGroups = findUsersGroups(accessGroup);

        for(UserGroup userGroup: currentUserGroups) {
            if(!userGroups.contains(userGroup)) {
                em.remove(userGroup);
            }
        }

        for(UserGroup userGroup: userGroups) {
            if(!currentUserGroups.contains(userGroup)) {
                em.persist(userGroup);
            }
        }
    }

    public void removeUserFromAllGroups(UserAccount userAccount) {
        em.createQuery("delete from UserGroup ug where ug.userAccount = :userAccount")
                .setParameter("userAccount", userAccount)
                .executeUpdate();
    }

    public void add(UserGroup userGroup) {
        em.persist(userGroup);
    }

    /**
     * Change the username of the user in all groups that it is part of.
     * @param userAccount the user account whose username is going to change.
     * @param newUsername the new username of the user account.
     */
    public void changeUsername(UserAccount userAccount, String newUsername) {
        List<UserGroup> usersGroups = findUsersGroups(userAccount);
        for(UserGroup userGroup: usersGroups) {
            userGroup.setUsername(newUsername);
        }
    }
}