import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TourDAO {

    // create tour (inserts tour + legs + flights + hotels) inside transaction
    public int createTour(Tour tour) {
        String insertTour = "INSERT INTO tours (title, type, total_cost, discount_pct, final_cost) VALUES (?,?,?,?,?)";
        try (Connection c = ConnectionDB.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(insertTour, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tour.getTitle());
                ps.setString(2, tour.getType());
                ps.setDouble(3, tour.getTotalCost());
                ps.setDouble(4, tour.getDiscountPct());
                ps.setDouble(5, tour.getFinalCost());
                int aff = ps.executeUpdate();
                if (aff == 0) {
                    c.rollback();
                    return -1;
                }
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int tourId = rs.getInt(1);
                        // insert legs
                        for (TourLeg leg : tour.getLegs()) {
                            leg.setTourId(tourId);
                            insertLeg(c, leg);
                        }
                        c.commit();
                        return tourId;
                    }
                }
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        } catch (Exception e) {
            System.out.println("createTour: " + e.getMessage());
        }
        return -1;
    }

    private void insertLeg(Connection c, TourLeg leg) throws SQLException {
        String s = "INSERT INTO tour_legs (tour_id, seq_no, country, city, stay_from, stay_to) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(s, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, leg.getTourId());
            ps.setInt(2, leg.getSeqNo());
            ps.setString(3, leg.getCountry());
            ps.setString(4, leg.getCity());
            LocalDate from = leg.getStayFrom();
            LocalDate to = leg.getStayTo();
            if (from != null)
                ps.setDate(5, Date.valueOf(from));
            else
                ps.setNull(5, Types.DATE);
            if (to != null)
                ps.setDate(6, Date.valueOf(to));
            else
                ps.setNull(6, Types.DATE);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int legId = rs.getInt(1);
                    // insert flights
                    if (leg.getOutboundFlight() != null)
                        insertFlight(c, legId, leg.getOutboundFlight());
                    if (leg.getReturnFlight() != null)
                        insertFlight(c, legId, leg.getReturnFlight());
                    // insert hotel
                    if (leg.getSelectedHotel() != null)
                        insertHotel(c, legId, leg.getSelectedHotel());
                }
            }
        }
    }

    private void insertFlight(Connection c, int legId, TourFlight tf) throws SQLException {
        String sql = "INSERT INTO tour_flights (tour_leg_id, flight_id, direction, price, flight_datetime) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, legId);
            ps.setInt(2, tf.getFlightId());
            ps.setString(3, tf.getDirection());
            ps.setDouble(4, tf.getPrice());
            LocalDateTime fdt = tf.getFlightDatetime();
            if (fdt != null)
                ps.setTimestamp(5, Timestamp.valueOf(fdt));
            else
                ps.setNull(5, Types.TIMESTAMP);
            ps.executeUpdate();
        }
    }

    private void insertHotel(Connection c, int legId, TourHotel th) throws SQLException {
        String sql = "INSERT INTO tour_hotels (tour_leg_id, hotel_id, price_total) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, legId);
            ps.setInt(2, th.getHotelId());
            ps.setDouble(3, th.getPriceTotal());
            ps.executeUpdate();
        }
    }

    // get all tours (simple)
    public List<Tour> getAll() {
        List<Tour> list = new ArrayList<>();
        String sql = "SELECT id, title, type, total_cost, discount_pct, final_cost, created_at FROM tours ORDER BY created_at DESC";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tour t = new Tour();
                t.setId(rs.getInt("id"));
                t.setTitle(rs.getString("title"));
                t.setType(rs.getString("type"));
                t.setTotalCost(rs.getDouble("total_cost"));
                t.setDiscountPct(rs.getDouble("discount_pct"));
                t.setFinalCost(rs.getDouble("final_cost"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("getAllTours: " + e.getMessage());
        }
        return list;
    }

    // delete tour
    public boolean deleteTour(int id) {
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM tours WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("deleteTour: " + e.getMessage());
        }
        return false;
    }

    // count
    public int getCount() {
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM tours");
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("getCount: " + e.getMessage());
        }
        return 0;
    }
}
