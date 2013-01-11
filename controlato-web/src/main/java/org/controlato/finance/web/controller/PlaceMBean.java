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
package org.controlato.finance.web.controller;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.controlato.finance.business.ExpenditureBean;
import org.controlato.finance.business.PlaceBean;
import org.controlato.finance.entity.Expenditure;
import org.controlato.finance.entity.Place;
import org.controlato.web.controller.UserProfileMBean;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class PlaceMBean {

    @EJB
    private PlaceBean placeBean;

    @EJB
    private ExpenditureBean expenditureBean;

    @ManagedProperty(value = "#{param.id}")
    private String id;

    @ManagedProperty(value = "#{userProfileBean}")
    private UserProfileMBean userProfileBean;

    private Place place;

    private List<Place> places;

    private List<Expenditure> recentExpenses;

    public PlaceMBean() {
        this.place = new Place();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserProfileMBean getUserProfileBean() {
        return userProfileBean;
    }

    public void setUserProfileBean(UserProfileMBean userProfileBean) {
        this.userProfileBean = userProfileBean;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public List<Place> getPlaces() {
        if (this.places == null) {
            this.places = placeBean.findPlaces();
        }
        return this.places;
    }

    public List<Expenditure> getRecentExpenses() {
        if(this.recentExpenses == null) {
            this.recentExpenses = expenditureBean.findExpenditures(this.place);
        }
        return this.recentExpenses;
    }

    @PostConstruct
    public void load() {
        if (id != null && !id.isEmpty()) {
            this.place = placeBean.findPlace(id);
        }
    }

    public String create() {
        placeBean.save(this.place);

        return "places?faces-redirect=true";
    }

    public String save() {
        placeBean.save(this.place);

        return "place?id=" + this.place.getId() + "&faces-redirect=true";
    }

    public String remove() {
        placeBean.remove(this.place.getId());
        return "places?faces-redirect=true";
    }
}