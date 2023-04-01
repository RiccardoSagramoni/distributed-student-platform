package it.unipi.dsmt.student_platform.servlets;

import it.unipi.dsmt.student_platform.dto.BookingDTO;
import it.unipi.dsmt.student_platform.dto.LoggedUserDTO;
import it.unipi.dsmt.student_platform.enums.UserRole;
import it.unipi.dsmt.student_platform.interfaces.BookingEJB;
import it.unipi.dsmt.student_platform.utility.AccessController;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;


import java.io.IOException;
import java.util.List;

@WebServlet(name = "BookingServlet", value = "/student/booking")
public class BookingServlet extends HttpServlet {

    @EJB
    private BookingEJB bookingEJB;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        int id = request.getParameter("id") == null? 0 : Integer.parseInt(request.getParameter("id"));
        int offset = request.getParameter("offset")==null ? 0 : Integer.parseInt(request.getParameter("offset"));
        
        String action = request.getParameter("action") == null ? null : request.getParameter("action");
        
        if(action != null && action.equals("offsetChange")){
            response.sendRedirect(request.getContextPath() + "/student/booking?id=" + id + "&offset=" + offset);
            return;
        }

        // List of available slots
        List<BookingDTO> bDTOs = bookingEJB.getSlots(id, offset);

        int iterator = request.getParameter("timeslot").isEmpty() ? -1 : Integer.parseInt(request.getParameter("timeslot"));
        if(iterator == -1){
            response.sendRedirect(request.getContextPath() + "/student/booking?id=" + id + "&r=error");
            System.out.println("Error: no timeslot selected");
            return;
        }

        LoggedUserDTO loggedUser = (LoggedUserDTO) request.getSession().getAttribute("logged_user");
        // Send the query for the requesting user TODO Check correctness after Rick's login changes
        boolean ret = bookingEJB.bookSlot(loggedUser.getId(), id, bDTOs.get(iterator), offset);

        if(!ret){
            response.sendRedirect(request.getContextPath() + "/student/booking?id=" + id + "&r=error&offset=" + offset);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/student/booking?id=" + id + "&r=success&offset=" + offset);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Check if logged user is a student
        if (!AccessController.checkAccess(request, response, UserRole.student)) {
           return;
        }
        
        int id = request.getParameter("id") == null? 0 : Integer.parseInt(request.getParameter("id"));
        int offset = request.getParameter("offset") == null ? 0 : Integer.parseInt(request.getParameter("offset"));

        List<BookingDTO> bDTOs = bookingEJB.getSlots(id, offset);
        request.setAttribute("slots", bDTOs);

        request.getRequestDispatcher("/WEB-INF/jsp/student/booking.jsp")
                .forward(request, response);

    }
}