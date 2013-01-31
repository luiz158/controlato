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
package org.controlato.finance.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.controlato.entity.Identified;

/**
 *
 * @author Hildeberto Mendonca
 */
@Entity
@Table(name = "place")
public class Place implements Serializable, Identified {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column(name = "name", nullable=false)
    private String name;

    private String location;

    private String city;

    @Column(name="postal_code")
    private String postalCode;

    @Column(name="phone_number")
    private String phoneNumber;

    private String website;

    private String email;

    public Place() {
    }

    public Place(String id) {
        this.id = id;
    }

    public Place(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        fullName.append(this.name);

        if(this.city != null && !this.city.isEmpty()) {
            fullName.append(" (");
            fullName.append(this.city);
            fullName.append(")");
        }

        return fullName.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        StringBuilder address = new StringBuilder();
        if(this.location != null && !this.location.isEmpty()) {
            address.append(this.location);
        }

        if(this.postalCode != null && !this.postalCode.isEmpty()) {
            if(address.length() > 0) {
                address.append(" ");
            }
            address.append(this.postalCode);
        }

        if(this.city != null && !this.city.isEmpty()) {
            address.append(" - ");
            address.append(this.city);
        }

        return address.toString();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Place)) {
            return false;
        }
        Place other = (Place) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getFullName();
    }
}