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

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import org.controlato.entity.AccessGroup;
import org.controlato.entity.Authentication;
import org.controlato.entity.UserAccount;
import org.controlato.entity.UserGroup;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class AccessGroupBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private UserAccountBean userAccountBean;

    @EJB
    private UserGroupBean userGroupBean;

    public static final String ADMIN_GROUP = "administrators";
    public static final String DEFAULT_GROUP = "users";

    public AccessGroup findAccessGroup(String groupId) {
        return em.find(AccessGroup.class, groupId);
    }

    public AccessGroup findUserDefaultGroup() {
        AccessGroup defaultUserGroup;
        try {
            defaultUserGroup = (AccessGroup) em.createQuery("select ag from AccessGroup ag where ag.userDefault = :default")
                                        .setParameter("default", Boolean.TRUE)
                                        .getSingleResult();
        }
        catch(NoResultException nre) {
            defaultUserGroup = new AccessGroup(DEFAULT_GROUP,"Default Members Group");
            defaultUserGroup.setId(EntitySupport.INSTANCE.generateEntityId());
            defaultUserGroup.setUserDefault(Boolean.TRUE);
            em.persist(defaultUserGroup);
        }
        return defaultUserGroup;
    }

    /** Returns the existing administrative group. If it doesn't find anyone
     *  then a new one is created and returned. */
    public AccessGroup findAdministrativeGroup() {
        AccessGroup group;
        try {
            group = (AccessGroup) em.createQuery("select ag from AccessGroup ag where ag.name = :name")
                                        .setParameter("name", ADMIN_GROUP)
                                        .getSingleResult();
        }
        catch(Exception nre) {
            group = new AccessGroup(ADMIN_GROUP,"JUG Leaders Group");
            group.setId(EntitySupport.INSTANCE.generateEntityId());
            em.persist(group);
        }
        return group;
    }

    @SuppressWarnings("unchecked")
    public List<AccessGroup> findAccessGroups() {
        return em.createQuery("select ag from AccessGroup ag order by ag.name").getResultList();
    }

    public AccessGroup findAccessGroupByName(String name) throws NoResultException {
        return (AccessGroup) em.createQuery("select ag from AccessGroup ag where ag.name = :name")
                               .setParameter("name", name)
                               .getSingleResult();
    }

    public void save(AccessGroup accessGroup, List<UserAccount> members) {
        if(accessGroup.getUserDefault()) {
            AccessGroup defaultGroup = findUserDefaultGroup();
            defaultGroup.setUserDefault(false);
        }

        if(accessGroup.getId() == null || accessGroup.getId().isEmpty()) {
            try {
                AccessGroup group = findAccessGroupByName(accessGroup.getName());
                if(group != null) {
                    throw new PersistenceException("A group named '"+ accessGroup.getName() +"' already exists.");
                }
            }
            catch(NoResultException nre) {
                accessGroup.setId(EntitySupport.INSTANCE.generateEntityId());
                em.persist(accessGroup);
            }
        }
        else {
            em.merge(accessGroup);
        }

        if(members != null) {
            Authentication auth;
            List<UserGroup> usersGroup = new ArrayList<>();
            for(UserAccount member: members) {
                auth = userAccountBean.findAuthenticationUser(member.getId());
                usersGroup.add(new UserGroup(accessGroup, auth));
            }
            userGroupBean.update(accessGroup, usersGroup);
        }
    }

    public void remove(String groupId) {
        AccessGroup accessGroup = em.find(AccessGroup.class, groupId);
        em.remove(accessGroup);
    }
}