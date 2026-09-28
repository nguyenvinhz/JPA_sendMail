package murach.email;

import java.io.*;
import javax.mail.MessagingException;
import javax.servlet.*;
import javax.servlet.http.*;
import murach.business.User;
import murach.data.UserDB;
import murach.util.*;

public class EmailListServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String url = "/index.html";

        // get current action
        String action = request.getParameter("action");
        if (action == null) {
            action = "join"; // default action
        }

        // perform action and set URL to appropriate page
        if (action.equals("join")) {
            url = "/index.jsp"; // the "join" page
        } else if (action.equals("add")) {
            // get parameters from the request
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            // store data in User object
            User user = new User(firstName, lastName, email);

            // validate the parameters and check if email exists in database
            String message;
            if (UserDB.emailExists(user.getEmail())) {
                message = "This email address already exists.<br>" +
                        "Please enter another email address.";
                url = "/index.jsp";
            } else {
                message = "";
                UserDB.insert(user);
                url = "/thanks.jsp";

                // send email to user
                String to = email;
                String from = MailUtilBrevo.getFromAddress();
                String subject = "Welcome to our email list";
                String body = "Dear " + firstName + ",\n\n"
                        + "Thanks for joining our email list. We'll make sure to send "
                        + "you announcements about new products and promotions.\n"
                        + "Have a great day and thanks again!\n\n"
                        + "Kelly Slivkoff\n"
                        + "Mike Murach & Associates";
                boolean isBodyHTML = false;

                try {
                    MailUtilBrevo.sendMail(to, from, subject, body, isBodyHTML);
                } catch (MessagingException e) {
                    String errorMessage = "ERROR: Unable to send email. " + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    this.log(
                            "Unable to send email. \n"
                                    + "Here is the email you tried to send: \n"
                                    + "=====================================\n"
                                    + "TO: " + email + "\n"
                                    + "FROM: " + from + "\n"
                                    + "SUBJECT: " + subject + "\n\n"
                                    + body + "\n\n");
                }
            }
            request.setAttribute("user", user);
            request.setAttribute("message", message);
        }

        // forward request and response objects to specified URL
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
