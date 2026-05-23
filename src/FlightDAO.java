import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAO {

    public FlightDAO() {
        ensureCountryColumns();
    }

    private void ensureCountryColumns() {
        try (Connection c = ConnectionDB.getConnection();
                Statement s = c.createStatement()) {
            // Add columns if they don't exist. MySQL syntax for this is tricky without
            // checking metadata.
            // Or just try ALTER IGNORE? No.
            // Check metadata.
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getColumns(null, null, "flights", "origin_country");
            if (!rs.next()) {
                s.executeUpdate("ALTER TABLE flights ADD COLUMN origin_country VARCHAR(255)");
            }
            rs = md.getColumns(null, null, "flights", "destination_country");
            if (!rs.next()) {
                s.executeUpdate("ALTER TABLE flights ADD COLUMN destination_country VARCHAR(255)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= CREATE =================
    public int createFlight(Flight f) {
        String sql = """
                    INSERT INTO flights
                    (flight_code, airline_id, flight_number, origin, destination,
                     depart_datetime, arrive_datetime, duration_minutes,
                     economy_seats, business_seats,
                     economy_price, business_price,
                     is_international, status, origin_country, destination_country)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, f.getFlightCode());
            ps.setInt(2, f.getAirlineId());
            ps.setString(3, f.getFlightNumber());
            ps.setString(4, f.getOrigin());
            ps.setString(5, f.getDestination());
            ps.setTimestamp(6, Timestamp.valueOf(f.getDepartDateTime()));
            ps.setTimestamp(7, Timestamp.valueOf(f.getArriveDateTime()));
            ps.setInt(8, f.getDurationMinutes());
            ps.setInt(9, f.getEconomySeats());
            ps.setInt(10, f.getBusinessSeats());
            ps.setDouble(11, f.getEconomyPrice());
            ps.setDouble(12, f.getBusinessPrice());
            ps.setBoolean(13, f.isInternational());
            ps.setString(14, f.getStatus());
            ps.setString(15, f.getOriginCountry());
            ps.setString(16, f.getDestinationCountry());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ================= UPDATE =================
    public boolean updateFlight(Flight f) {
        String sql = """
                    UPDATE flights SET
                    airline_id=?, flight_number=?, origin=?, destination=?,
                    depart_datetime=?, arrive_datetime=?, duration_minutes=?,
                    economy_seats=?, business_seats=?,
                    economy_price=?, business_price=?,
                    is_international=?, status=?, origin_country=?, destination_country=?
                    WHERE id=?
                """;

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, f.getAirlineId());
            ps.setString(2, f.getFlightNumber());
            ps.setString(3, f.getOrigin());
            ps.setString(4, f.getDestination());
            ps.setTimestamp(5, Timestamp.valueOf(f.getDepartDateTime()));
            ps.setTimestamp(6, Timestamp.valueOf(f.getArriveDateTime()));
            ps.setInt(7, f.getDurationMinutes());
            ps.setInt(8, f.getEconomySeats());
            ps.setInt(9, f.getBusinessSeats());
            ps.setDouble(10, f.getEconomyPrice());
            ps.setDouble(11, f.getBusinessPrice());
            ps.setBoolean(12, f.isInternational());
            ps.setString(13, f.getStatus());
            ps.setString(14, f.getOriginCountry());
            ps.setString(15, f.getDestinationCountry());
            ps.setInt(16, f.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= CANCEL (SOFT) =================
    public boolean cancelFlight(int id) {
        String sql = "UPDATE flights SET status='CANCELLED' WHERE id=?";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= GET ALL =================
    public List<Flight> getAllGroupedByAirline() {
        List<Flight> list = new ArrayList<>();
        String sql = """
                    SELECT f.*, a.name AS airline_name
                    FROM flights f
                    JOIN airlines a ON f.airline_id = a.id
                    ORDER BY a.name, f.depart_datetime
                """;

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= MAPPER =================
    private Flight map(ResultSet rs) throws SQLException {
        Flight f = new Flight();
        f.setId(rs.getInt("id"));
        f.setFlightCode(rs.getString("flight_code"));
        f.setAirlineId(rs.getInt("airline_id"));
        f.setAirlineName(rs.getString("airline_name"));
        f.setFlightNumber(rs.getString("flight_number"));
        f.setOrigin(rs.getString("origin"));
        f.setDestination(rs.getString("destination"));
        f.setDepartDateTime(rs.getTimestamp("depart_datetime").toLocalDateTime());
        f.setArriveDateTime(rs.getTimestamp("arrive_datetime").toLocalDateTime());
        f.setDurationMinutes(rs.getInt("duration_minutes"));
        f.setEconomySeats(rs.getInt("economy_seats"));
        f.setBusinessSeats(rs.getInt("business_seats"));
        f.setEconomyPrice(rs.getDouble("economy_price"));
        f.setBusinessPrice(rs.getDouble("business_price"));
        f.setInternational(rs.getBoolean("is_international"));
        f.setStatus(rs.getString("status"));
        try {
            f.setOriginCountry(rs.getString("origin_country"));
            f.setDestinationCountry(rs.getString("destination_country"));
        } catch (SQLException e) {
            // column might not exist yet if query was raw check or old version
        }
        return f;
    }

    // ================= SEARCH =================
    public List<Flight> search(String origin, String destination) {
        return searchWithJoin(origin, destination);
    }

    private List<Flight> searchWithJoin(String origin, String destination) {
        List<Flight> list = new ArrayList<>();
        String sql = """
                    SELECT f.*, a.name AS airline_name
                    FROM flights f
                    JOIN airlines a ON f.airline_id = a.id
                    WHERE (f.origin LIKE ? OR f.origin_country LIKE ?)
                      AND (f.destination LIKE ? OR f.destination_country LIKE ?)
                    ORDER BY f.depart_datetime
                """;

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            String term1 = "%" + origin + "%";
            String term2 = "%" + destination + "%";

            ps.setString(1, term1);
            ps.setString(2, term1);
            ps.setString(3, term2);
            ps.setString(4, term2);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Flight> search(String origin, String destination, LocalDateTime date) {
        List<Flight> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, a.name AS airline_name FROM flights f JOIN airlines a ON f.airline_id = a.id " +
                        "WHERE (f.origin LIKE ? OR f.origin_country LIKE ?) AND (f.destination LIKE ? OR f.destination_country LIKE ?)");

        if (date != null) {
            sql.append(" AND DATE(f.depart_datetime) = DATE(?)");
        }
        sql.append(" ORDER BY f.depart_datetime");

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql.toString())) {

            String term1 = "%" + origin + "%";
            String term2 = "%" + destination + "%";

            ps.setString(1, term1);
            ps.setString(2, term1);
            ps.setString(3, term2);
            ps.setString(4, term2);

            if (date != null) {
                ps.setTimestamp(5, Timestamp.valueOf(date));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= COUNT =================
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM flights";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
