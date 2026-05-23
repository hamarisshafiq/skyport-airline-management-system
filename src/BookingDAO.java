import java.sql.*;

public class BookingDAO {

    public boolean createBooking(
            int userId,
            String bookingType, // FLIGHT / HOTEL / TOUR
            int refId, // flight_id etc
            String bookingSubType, // ONEWAY / RETURN
            int seats,
            double pricePaid) {

        String sql = """
                    INSERT INTO user_bookings
                    (user_id, booking_type, ref_id, booking_subtype,
                     seats_booked, price_paid, booking_status)
                    VALUES (?,?,?,?,?,?,?)
                """;

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, bookingType);
            ps.setInt(3, refId);
            ps.setString(4, bookingSubType);
            ps.setInt(5, seats);
            ps.setDouble(6, pricePaid);
            ps.setString(7, "CONFIRMED");

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<Booking> getBookingsByUserId(int userId) {
        java.util.List<Booking> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM user_bookings WHERE user_id = ? ORDER BY booking_date DESC";
        // booking_date might be created_at commonly, let's assume 'created_at' or
        // automatic timestamp if not in insert.
        // The insert query didn't specify date, so it's likely auto-generated or
        // missing.
        // Let's check the schema in FixSchema or similar?
        // Actually, let's just SELECT * and see what we get.
        // Safest is to specificy columns or just read what we know.
        // Re-reading createBooking: we insert 7 cols. Table likely has id (AI) and
        // created_at (TIMESTAMP).

        sql = "SELECT id, booking_type, ref_id, booking_subtype, seats_booked, price_paid, booking_status FROM user_bookings WHERE user_id = ? ORDER BY id DESC";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    b.setId(rs.getInt("id"));
                    b.setUserId(userId);
                    b.setBookingType(rs.getString("booking_type"));
                    b.setRefId(rs.getInt("ref_id"));
                    b.setBookingSubType(rs.getString("booking_subtype"));
                    b.setSeats(rs.getInt("seats_booked"));
                    b.setPrice(rs.getDouble("price_paid"));
                    b.setStatus(rs.getString("booking_status"));
                    list.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.List<Integer> getUserIdsForFlight(int flightId) {
        java.util.List<Integer> userIds = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT user_id FROM user_bookings WHERE booking_type = 'FLIGHT' AND ref_id = ?";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }

    public static class Booking {
        private int id;
        private int userId;
        private String bookingType;
        private int refId;
        private String bookingSubType;
        private int seats;
        private double price;
        private String status;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int u) {
            this.userId = u;
        }

        public String getBookingType() {
            return bookingType;
        }

        public void setBookingType(String t) {
            this.bookingType = t;
        }

        public int getRefId() {
            return refId;
        }

        public void setRefId(int r) {
            this.refId = r;
        }

        public String getBookingSubType() {
            return bookingSubType;
        }

        public void setBookingSubType(String s) {
            this.bookingSubType = s;
        }

        public int getSeats() {
            return seats;
        }

        public void setSeats(int s) {
            this.seats = s;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double p) {
            this.price = p;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String s) {
            this.status = s;
        }
    }
}
