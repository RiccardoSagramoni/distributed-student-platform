package it.unipi.dsmt.student_platform.ejb;

import it.unipi.dsmt.student_platform.dao.BookingDAO;
import it.unipi.dsmt.student_platform.dto.BookingDTO;
import it.unipi.dsmt.student_platform.dto.StudentBookedMeetingDTO;
import it.unipi.dsmt.student_platform.interfaces.BookingEJB;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class BookingEJBImpl implements BookingEJB {
    
    /**
     * Datasource used to access the database.
     */
    @Resource(lookup = "jdbc/StudentPlatformPool")
    private DataSource dataSource;
    BookingDAO bookingDAO = new BookingDAO();

    // This method extract required data of a specific course of known id
    public List<BookingDTO> getSlots(int id, int offset){
        LocalDate start = LocalDate.now().plusMonths(offset);
        
        if(offset != 0){
            start = start.withDayOfMonth(1);
        }
        
        
        LocalDate end = start.withDayOfMonth(start.getMonth().length(start.isLeapYear()));

        // Get data
        List<BookingDTO> bookedSlots = bookingDAO.getBookedSlots(start, end, id, dataSource);

        List<BookingDTO> allSlots = bookingDAO.getAllPossibleSlots(id, dataSource);

        if(allSlots == null){
            System.out.println("Error: No slots available");
            return new ArrayList<>();
        }

        ArrayList<BookingDTO> monthlySlots = new ArrayList<>();

        for(BookingDTO slot : allSlots){
            if(slot.getDayOfWeek() == 6 || slot.getDayOfWeek() == 7){
                System.out.println("Error: Saturday and Sunday are not allowed");
                continue;
            }

            // Get the first day of the next month
            LocalDate stop = start.with(TemporalAdjusters.firstDayOfNextMonth());

            // Get starting day (today or first day of specified month)
            LocalDate day = start.with(
                    TemporalAdjusters.nextOrSame(
                            DayOfWeek.of( slot.getDayOfWeek() )));
            //Until next month add all dates to ArrayList
            while( day.isBefore( stop ) ) {
                // Create a list of all possible slots
                monthlySlots.add(new BookingDTO(slot.getStart(), day, slot.getDayOfWeek(), slot.getId()));
                // Set up the next loop.
                day = day.plusWeeks( 1 );
            }
        }

        // If no bookings are available then all slots are free
        if(bookedSlots == null){
            return monthlySlots;
        }

        // otherwise remove from the list all unavailable slots
        for(BookingDTO bs : bookedSlots){
            // If days and times coincide then the slots isn't available
            monthlySlots.removeIf(as -> (bs.getDate().equals(as.getDate())) && (bs.getStart().equals(as.getStart())));
        }

        return monthlySlots;
    }


    @Override
    public boolean bookSlot(String studentID, int courseID, BookingDTO dto, int offset){

        List<BookingDTO> allSlots = getSlots(courseID, offset);
        if(allSlots == null){
            System.out.println("Error: No slots available");
            return false;
        }

        String meetingID = null;
        for (BookingDTO slot : allSlots) {
            if(slot.getDate().equals(dto.getDate()) && slot.getStart().equals(dto.getStart())){
                meetingID = slot.getId();
            }
        }
        
        return bookingDAO.bookSlot(studentID, meetingID, dto, dataSource);
    }

    @Override
    public List<StudentBookedMeetingDTO> getBookedMeetingsForStudent(String id){
        List<StudentBookedMeetingDTO> bookedMeeting = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            // Get details of requested course
            String query =  "SELECT BIN_TO_UUID(bm.id) as id, c.name, bm.date, ms.starting_time " +
                            "FROM booked_meeting bm " +
                            "     INNER JOIN " +
                            "     meeting_slot ms on bm.time_slot = ms.id " +
                            "     INNER JOIN " +
                            "     course c on ms.course = c.id " +
                            "WHERE bm.student = UUID_TO_BIN(?);";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                // Set parameters in prepared statement
                preparedStatement.setString(1, id);

                // Execute query
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // If the query returned a result set,
                    // then wrap it inside a CourseDTO object and return it
                    while (resultSet.next()){
                        bookedMeeting.add(new StudentBookedMeetingDTO(
                                                resultSet.getString("id"),
                                                resultSet.getString("c.name"),
                                                resultSet.getDate("bm.date").toLocalDate(),
                                                resultSet.getTime("ms.starting_time")
                                          )
                        );
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return bookedMeeting;
    }
}
