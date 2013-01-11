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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents the allocation of users in groups.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name = "user_group")
public class UserGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected UserGroupId id;

    @ManyToOne
    @JoinColumn(name="user_id", insertable = false, updatable = false)
    private UserAccount userAccount;

    @ManyToOne
    @JoinColumn(name="group_id", insertable = false, updatable = false)
    private AccessGroup accessGroup;

    private String username;

    @Column(name = "group_name")
    private String groupName;

    public UserGroup() {
    }

    public UserGroup(AccessGroup accessGroup, Authentication authentication) {
        this.accessGroup = accessGroup;
        this.userAccount = authentication.getUserAccount();
        this.id = new UserGroupId(this.accessGroup.getId(), this.userAccount.getId());
        this.username = authentication.getUsername();
        this.groupName = this.accessGroup.getName();
    }

    public UserGroupId getId() {
        return this.id;
    }

    public void setId(UserGroupId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setAuthentication(Authentication authentication) {
        this.userAccount = authentication.getUserAccount();

        if(this.id == null) {
            this.id = new UserGroupId();
        }
        this.id.setUserId(this.userAccount.getId());
        this.username = authentication.getUsername();
    }

    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(AccessGroup accessGroup) {
        this.accessGroup = accessGroup;

        if(this.id == null) {
            this.id = new UserGroupId();
        }
        this.id.setGroupId(accessGroup.getId());
        this.groupName = accessGroup.getName();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserGroup)) {
            return false;
        }
        UserGroup other = (UserGroup) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return username;
    }
}