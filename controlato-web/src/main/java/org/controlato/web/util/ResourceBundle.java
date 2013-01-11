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

import java.util.Locale;
import java.util.MissingResourceException;
import javax.faces.context.FacesContext;

/**
 * Encapsulates the complexity of getting the resource bundle from the context.
 * Actually, this is so complex that a better approach should be investigated, or
 * a solution presented to spec leaders.
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class ResourceBundle {

    private static final String BUNDLE_NAME = "org.controlato.web.bundles.Resources";

    private static ResourceBundle singletonInstance;

    private ResourceBundle() {
    }

    public static ResourceBundle getInstance() {
        if(singletonInstance == null) {
            singletonInstance = new ResourceBundle();
        }
        return singletonInstance;
    }

    public String getMessage(String key) {
        return getMessageFromResourceBundle(key);
    }

    private String getMessageFromResourceBundle(String key) {
        java.util.ResourceBundle bundle;
        String message;
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();

        try {
            bundle = java.util.ResourceBundle.getBundle(BUNDLE_NAME, locale, getCurrentLoader(BUNDLE_NAME));

            if (bundle == null) {
                return "?";
            }
        } catch (MissingResourceException e) {
            return "?";
        }

        try {
            message = bundle.getString(key);
        } catch (Exception e) {
            return "?";
        }

        return message;
    }

    private ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }
}