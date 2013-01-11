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
package org.controlato.web.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.controlato.entity.DeactivationType;
import org.controlato.web.util.ResourceBundle;

/**
 * DeactivationType is a Enum. This converter is responsible for transforming
 * that Enum in something readable for the end-user, using ResourceBundle.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@FacesConverter(value="DeactivationTypeEnumToString")
public class DeactivationTypeEnumToString implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        switch (value) {
            case "ownwill":
                return DeactivationType.OWNWILL;
            case "administrative":
                return DeactivationType.ADMINISTRATIVE;
            default:
                return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        DeactivationType deactivationType = (DeactivationType) value;
        ResourceBundle bundle = ResourceBundle.getInstance();
        if(deactivationType == DeactivationType.OWNWILL) {
            return bundle.getMessage("ownWill");
        }
        else if(deactivationType == DeactivationType.ADMINISTRATIVE) {
            return bundle.getMessage("administrative");
        }
        else {
            return "";
        }
    }
}