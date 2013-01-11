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
package org.controlato.web.controller;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import org.controlato.business.LanguageBean;
import org.controlato.entity.Language;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
@ManagedBean
@RequestScoped
public class LanguageMBean {

    @EJB
    private LanguageBean languageBean;

    private Language language;

    private List<Language> languages;

    @ManagedProperty(value="#{param.acronym}")
    private String acronym;

    public LanguageMBean() {
        this.language = new Language();
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public List<Language> getLanguages() {
        if(this.languages == null) {
            this.languages = languageBean.findLanguages();
        }
        return this.languages;
    }

    @PostConstruct
    public void load() {
        if(this.acronym != null && !this.acronym.isEmpty()) {
            this.language = languageBean.findLanguage(this.acronym);
        }
    }

    public String save() {
        languageBean.save(this.language);
        return "languages?faces-redirect=true";
    }
}