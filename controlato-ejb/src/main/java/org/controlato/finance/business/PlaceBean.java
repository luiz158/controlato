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
package org.controlato.finance.business;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.controlato.finance.entity.Place;
import org.controlato.util.EntitySupport;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@Stateless
@LocalBean
public class PlaceBean {

    @PersistenceContext
    private EntityManager em;

    public Place findPlace(String id) {
        return em.find(Place.class, id);
    }

    public List<Place> findPlaces() {
        return em.createQuery("select p from Place p order by p.name asc")
                 .getResultList();
    }

    public void save(Place place) {
        if(place.getId() == null || place.getId().isEmpty()) {
            place.setId(EntitySupport.generateEntityId());
            em.persist(place);
        }
        else {
            em.merge(place);
        }
    }

    public void remove(String id) {
        Place place = em.find(Place.class, id);
        if(place != null) {
            em.remove(place);
        }
    }
}