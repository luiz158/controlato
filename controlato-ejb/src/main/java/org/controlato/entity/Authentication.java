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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.controlato.util.Base64Encoder;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Entity
@Table(name = "authentication")
public class Authentication implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String username;

    private String password;

    @ManyToOne
    @JoinColumn(name="user_account")
    private UserAccount userAccount;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the hashed password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Receive a new password, hash it and set the password attribute.
     * @param password row password as informed by the user. This method should
     * be invoked only in case of changing the password.
     */
    public void setPassword(String password) {
        this.password = hashPassword(password);
    }


    /**
     * @return the userAccount that is associated to the authentication credentials.
     */
    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    /**
     * Hash a raw password using the MD5 algorithm.
     * @param rawPassword non-hashed password informed by the user.
     * @return the hashed password.
     */
    public String hashPassword(String rawPassword) {
        MessageDigest md = null;
        byte stringBytes[] = null;
        try {
            md = MessageDigest.getInstance("MD5");
            stringBytes = rawPassword.getBytes("UTF8");
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
}
