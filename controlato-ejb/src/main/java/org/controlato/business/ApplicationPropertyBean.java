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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.controlato.entity.ApplicationProperty;
import org.controlato.entity.Properties;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class ApplicationPropertyBean {

    @PersistenceContext
    EntityManager em;

    public Map<String, String> findApplicationProperties() {
        Map<String, String> propertiesMap = new HashMap<>();
        List<ApplicationProperty> properties = em.createQuery("select ap from ApplicationProperty ap").getResultList();
        for(ApplicationProperty property: properties) {
            propertiesMap.put(property.getPropertyKey(), property.getPropertyValue());
        }

        // If there is no property in the database, it creates all properties according to the enumeration Properties.
        if(propertiesMap.isEmpty()) {
            Properties[] props = Properties.values();
            for(int i = 0;i < props.length;i++) {
                propertiesMap.put(props[i].getKey(), props[i].getDefaultValue());
            }
            create(propertiesMap);
        }
        // If there is more properties in the enumeration than in the database, then additional enumerations are persisted.
        else if(Properties.values().length > propertiesMap.size()) {
            Properties[] props = Properties.values();
            for(int i = 0;i < props.length;i++) {
                if(!propertiesMap.containsKey(props[i].getKey())) {
                    propertiesMap.put(props[i].getKey(), props[i].getDefaultValue());
                    create(props[i].getKey(), props[i].getDefaultValue());
                }
            }
        }
        // If there is more persisted properties than in the enumeration, then exceding properties are removed.
        else if(Properties.values().length < propertiesMap.size()) {
            Set<Map.Entry<String, String>> propEntries = propertiesMap.entrySet();
            Iterator iProps = propEntries.iterator();
            Map.Entry<String, String> entry;
            Properties[] props = Properties.values();
            while(iProps.hasNext()) {
                entry = (Map.Entry)iProps.next();
                for(int i = 0; i < props.length; i++) {
                    if(!entry.getKey().equals(props[i].getKey())) {
                        remove(entry.getKey());
                        break;
                    }
                }
            }
        }

        return propertiesMap;
    }

    public ApplicationProperty findApplicationProperty(Properties properties) {
        ApplicationProperty applicationProperty;
        try {
            applicationProperty = (ApplicationProperty)em.createQuery("select ap from ApplicationProperty ap where ap.propertyKey = :key")
                                                                         .setParameter("key", properties.getKey())
                                                                         .getSingleResult();
        }
        catch(NoResultException nre) {
            Map applicationProperties = findApplicationProperties();
            String key = properties.getKey();
            applicationProperty = new ApplicationProperty(key, (String)applicationProperties.get(key));
        }
        return applicationProperty;
    }

    public void save(Map<String, String> properties) {
        List<ApplicationProperty> existingProperties = em.createQuery("select ap from ApplicationProperty ap").getResultList();
        String value;
        for(ApplicationProperty property: existingProperties) {
            value = properties.get(property.getPropertyKey());
            property.setPropertyValue(value);
            em.merge(property);
        }
    }

    private void create(Map<String, String> properties) {
        Set<Map.Entry<String, String>> props = properties.entrySet();
        Iterator iProps = props.iterator();
        ApplicationProperty appProp;
        Map.Entry<String, String> entry;
        while(iProps.hasNext()) {
            entry = (Map.Entry)iProps.next();
            appProp = new ApplicationProperty(entry.getKey(), entry.getValue());
            em.persist(appProp);
        }
    }

    private void create(String key, String value) {
        ApplicationProperty appProp = new ApplicationProperty(key, value);
        em.persist(appProp);
    }

    private void remove(String key) {
        ApplicationProperty applicationProperty = em.find(ApplicationProperty.class, key);
        em.remove(applicationProperty);
    }
}