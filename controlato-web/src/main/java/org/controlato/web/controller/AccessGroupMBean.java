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
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.controlato.business.AccessGroupBean;
import org.controlato.business.UserAccountBean;
import org.controlato.business.UserGroupBean;
import org.controlato.entity.AccessGroup;
import org.controlato.entity.UserAccount;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class AccessGroupMBean {

    @EJB
    private AccessGroupBean accessGroupBean;

    @EJB
    private UserAccountBean userAccountBean;

    @EJB
    private UserGroupBean userGroupBean;

    @ManagedProperty(value="#{param.id}")
    private String groupId;

    private AccessGroup group;

    // List of members for the picklist.
    private DualListModel<UserAccount> members;

    public AccessGroupMBean() {}

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public AccessGroup getGroup() {
        return group;
    }

    public void setGroup(AccessGroup group) {
        this.group = group;
    }

    public List<AccessGroup> getGroups() {
        return accessGroupBean.findAccessGroups();
    }

    public DualListModel<UserAccount> getMembers() {
        return members;
    }

    public void setMembers(DualListModel<UserAccount> members) {
        this.members = members;
    }

    @PostConstruct
    public void load() {
        List<UserAccount> allUsers = userAccountBean.findUserAccounts();
        List<UserAccount> target = new ArrayList<>();

        if(groupId != null && !groupId.isEmpty()) {
            this.group = accessGroupBean.findAccessGroup(this.groupId);

            target.addAll(userGroupBean.findUsersGroup(group));
            allUsers.removeAll(target);
        }
        else {
            this.group = new AccessGroup();
        }
        this.members = new DualListModel<>(allUsers, target);
    }

    @SuppressWarnings("rawtypes")
	public String save() {
        List<UserAccount> selectedMembers = new ArrayList<>();
        List membersIds = this.members.getTarget();
        UserAccount userAccount;
        for(int i = 0;i < membersIds.size();i++) {
            userAccount = new UserAccount(((UserAccount)membersIds.get(i)).getId());
            selectedMembers.add(userAccount);
        }

        accessGroupBean.save(this.group, selectedMembers);
        return "groups?faces-redirect=true";
    }

    public String remove(String groupId) {
        accessGroupBean.remove(groupId);
        return "groups?faces-redirect=true";
    }
}