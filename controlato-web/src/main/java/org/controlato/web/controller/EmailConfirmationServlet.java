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

import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.controlato.business.UserAccountBean;

/**
 *
 * @author Hildeberto Mendonca  - http://www.hildeberto.com
 */
public class EmailConfirmationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserAccountBean userAccountBean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String confirmationCode = request.getParameter("code");

        if (confirmationCode == null || confirmationCode.equals("")) {
            try (PrintWriter out = response.getWriter()) {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Yasmim - Email Confirmation Failure</title>");
                out.println("<link href=\"/controlato/resources/css/default.css\" rel=\"stylesheet\" type=\"text/css\"/>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Email Confirmation Failure</h1>");
                out.println("<p>This email confirmation is invalid. Check if the address on the browser coincides with the address sent by email.</p>");
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                String contextPath = request.getContextPath();
                out.println("<p><a href=\"" + scheme + "://" + serverName + (serverPort != 80 ? ":" + serverPort : "") + (contextPath.equals("") ? "" : contextPath) + "\">Go to Homepage</a>.");
                out.println("</body>");
                out.println("</html>");
            }
        }
        try {
            userAccountBean.confirmUser(confirmationCode);
        } catch (Exception e) {
            try (PrintWriter out = response.getWriter()) {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Yasmim - Email Confirmation Failure</title>");
                out.println("<link href=\"/controlato/resources/css/default.css\" rel=\"stylesheet\" type=\"text/css\"/>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Email Confirmation Failure</h1>");
                out.println("<p>This email confirmation is invalid.</p>");
                out.println("<p class=\"alertMessage\">Cause: " + e.getCause().getMessage() + "</p>");
                out.println("<p>Check if the address on the browser coincides with the address sent my email.</p>");
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                String contextPath = request.getContextPath();
                out.println("<p><a href=\"" + scheme + "://" + serverName + (serverPort != 80 ? ":" + serverPort : "") + (contextPath.equals("") ? "" : contextPath) + "\">Go to website</a>");
                out.println("</body>");
                out.println("</html>");
            }
        }
        response.sendRedirect("login.xhtml");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}