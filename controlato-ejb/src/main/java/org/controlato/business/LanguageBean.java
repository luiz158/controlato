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

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.entity.Language;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class LanguageBean {

    @PersistenceContext
    private EntityManager em;

    public Language findLanguage(String acronym) {
        return em.find(Language.class, acronym);
    }

    public List<Language> findLanguages() {
        return em.createQuery("select l from Language l order by l.name asc").getResultList();
    }

    public void save(Language language) {
        if(language.getAcronym() == null || language.getAcronym().isEmpty()) {
            language.setAcronym(Language.DEFAULT_LANGUAGE);
            em.persist(language);
        }
        else {
            em.merge(language);
        }
    }

    public void remove(String acronym) {
        Language language = em.find(Language.class, acronym);
        if(language != null) {
            em.remove(language);
        }
    }
}