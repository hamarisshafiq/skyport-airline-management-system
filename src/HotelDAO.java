import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    // CREATE
    public int createHotel(Hotel h) {
        String sql = "INSERT INTO hotels (name, country, city, category, address, email, contact, price_per_night) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, h.getName());
            ps.setString(2, h.getCountry());
            ps.setString(3, h.getCity());
            ps.setString(4, h.getCategory());
            ps.setString(5, h.getAddress());
            ps.setString(6, h.getEmail());
            ps.setString(7, h.getContact());
            ps.setDouble(8, h.getPricePerNight());

            int aff = ps.executeUpdate();
            if (aff == 0)
                return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setId(rs.getInt(1));
                    return h.getId();
                }
            }

        } catch (SQLException e) {
            System.out.println("createHotel: " + e.getMessage());
        }
        return -1;
    }

    // READ ALL
    public List<Hotel> getAll() {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT * FROM hotels ORDER BY name";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                list.add(mapRow(rs));

        } catch (Exception e) {
            System.out.println("getAll: " + e.getMessage());
        }

        return list;
    }

    // READ BY ID
    public Hotel getById(int id) {
        String sql = "SELECT * FROM hotels WHERE id=?";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }

        } catch (Exception e) {
            System.out.println("getById: " + e.getMessage());
        }

        return null;
    }

    // UPDATE
    public boolean updateHotel(Hotel h) {
        String sql = "UPDATE hotels SET name=?, country=?, city=?, category=?, address=?, email=?, contact=?, price_per_night=? WHERE id=?";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, h.getName());
            ps.setString(2, h.getCountry());
            ps.setString(3, h.getCity());
            ps.setString(4, h.getCategory());
            ps.setString(5, h.getAddress());
            ps.setString(6, h.getEmail());
            ps.setString(7, h.getContact());
            ps.setDouble(8, h.getPricePerNight());
            ps.setInt(9, h.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("updateHotel: " + e.getMessage());
        }

        return false;
    }

    // DELETE
    public boolean deleteHotel(int id) {
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM hotels WHERE id=?")) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("deleteHotel: " + e.getMessage());
        }

        return false;
    }

    // SEARCH FUNCTIONS
    public List<Hotel> searchByName(String name) {
        return searchGeneric("name", name);
    }

    public List<Hotel> searchByCountry(String country) {
        return searchGeneric("country", country);
    }

    public List<Hotel> searchByCity(String city) {
        return searchGeneric("city", city);
    }

    private List<Hotel> searchGeneric(String column, String value) {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT * FROM hotels WHERE " + column + " LIKE ?";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + value + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("search: " + e.getMessage());
        }

        return list;
    }

    // HELPER
    private Hotel mapRow(ResultSet rs) throws Exception {
        Hotel h = new Hotel();
        h.setId(rs.getInt("id"));
        h.setName(rs.getString("name"));
        h.setCountry(rs.getString("country"));
        h.setCity(rs.getString("city"));
        h.setCategory(rs.getString("category"));
        h.setAddress(rs.getString("address"));
        h.setEmail(rs.getString("email"));
        h.setContact(rs.getString("contact"));
        h.setPricePerNight(rs.getDouble("price_per_night"));
        return h;
    }

    // COUNT
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM hotels";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("getCount: " + e.getMessage());
        }
        return 0;
    }
}
