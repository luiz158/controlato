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
package org.controlato.util;

import java.util.UUID;
import org.controlato.entity.Identified;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public enum EntitySupport {

    INSTANCE;

    /**
     * @return Returns a 32 characteres string to be used as id of entities that
     * implements the interface org.controlato.entity.Identified.
     */
    public String generateEntityId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * Verifies whether the id of an identified entity is not valid to persist
     * in the database.
     * @param identified entity class that implements the interface
     * org.controlato.entity.Identified.
     * @return true if the id is not valid.
     */
    public boolean isIdNotValid(Identified identified) {
        // TODO: lançar uma excessão se o parâmetro for nulo.
        if(identified == null || identified.getId() == null || identified.getId().isEmpty()) {
            return true;
        }
        return false;
    }
}