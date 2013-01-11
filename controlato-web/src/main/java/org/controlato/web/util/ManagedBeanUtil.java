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
package org.controlato.web.util;

import javax.el.ELContext;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class ManagedBeanUtil {

    /**
     * Returns a managed bean by the given name. The bean must be registered
     * either in faces-config.xml or annotation. Injection is elegant, but
     * if beans are rare called, it’s not necessary to inject them
     * into other beans.
     */
    public static Object getManagedBean(final String beanName) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Object bean;

        try {
            ELContext elContext = fc.getELContext();
            bean = elContext.getELResolver().getValue(elContext, null, beanName);
        } catch (RuntimeException e) {
            throw new FacesException(e.getMessage(), e);
        }

        if (bean == null) {
            throw new FacesException("Managed bean with name '" + beanName
                + "' was not found. Check your faces-config.xml or @ManagedBean annotation.");
        }

        return bean;
    }
}