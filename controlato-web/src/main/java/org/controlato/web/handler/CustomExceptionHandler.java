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
package org.controlato.web.handler;

import java.util.Iterator;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

	private ExceptionHandler parent;

	public CustomExceptionHandler(ExceptionHandler parent) {
		this.parent = parent;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return this.parent;
	}

	@Override
	public void handle() throws FacesException {
		ExceptionQueuedEvent event;
		ExceptionQueuedEventContext eventContext;
		Throwable t;
		for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();i.hasNext();) {
			event = i.next();
			eventContext = (ExceptionQueuedEventContext) event.getSource();
			t = eventContext.getException();
			if(t instanceof ViewExpiredException) {
				ViewExpiredException vee = (ViewExpiredException) t;
				FacesContext fc = FacesContext.getCurrentInstance();
				NavigationHandler nav = fc.getApplication().getNavigationHandler();
				try {
					fc.getExternalContext().getFlash().put("currentViewId", vee.getViewId());
					nav.handleNavigation(fc, null, "/login?faces-redirect=true");
					fc.renderResponse();
				}
				finally {
					i.remove();
				}
			}
		}
		getWrapped().handle();
	}
}