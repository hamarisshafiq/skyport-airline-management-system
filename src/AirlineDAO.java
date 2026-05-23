import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirlineDAO {

    // ---------------- CREATE AIRLINE ----------------
    public int createAirline(Airline a) {
        String sql = "INSERT INTO airlines (name, code, country) VALUES (?, ?, ?)";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getName());
            ps.setString(2, a.getCode());
            ps.setString(3, a.getCountry());

            int aff = ps.executeUpdate();
            if (aff == 0)
                return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                    return a.getId();
                }
            }
        } catch (SQLException e) {
            System.out.println("createAirline: " + e.getMessage());
        }
        return -1;
    }

    // ---------------- GET ALL AIRLINES ----------------
    public List<Airline> getAll() {
        List<Airline> list = new ArrayList<>();
        String sql = "SELECT id, name, code, country FROM airlines ORDER BY name";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Airline a = new Airline();
                a.setId(rs.getInt("id"));
                a.setName(rs.getString("name"));
                a.setCode(rs.getString("code"));
                a.setCountry(rs.getString("country"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.out.println("getAllAirlines: " + e.getMessage());
        }
        return list;
    }

    // ---------------- UPDATE AIRLINE ----------------
    public boolean updateAirline(Airline a) {
        String sql = "UPDATE airlines SET name=?, code=?, country=? WHERE id=?";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, a.getName());
            ps.setString(2, a.getCode());
            ps.setString(3, a.getCountry());
            ps.setInt(4, a.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("updateAirline: " + e.getMessage());
        }
        return false;
    }

    // ---------------- GET AIRLINE BY ID ----------------
    public Airline getById(int id) {
        String sql = "SELECT id, name, code, country FROM airlines WHERE id=?";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Airline a = new Airline();
                    a.setId(rs.getInt("id"));
                    a.setName(rs.getString("name"));
                    a.setCode(rs.getString("code"));
                    a.setCountry(rs.getString("country"));
                    return a;
                }
            }

        } catch (SQLException e) {
            System.out.println("getById: " + e.getMessage());
        }
        return null;
    }

    // ---------------- DELETE AIRLINE ----------------
    public boolean delete(int id) {
        String sql = "DELETE FROM airlines WHERE id=?";

        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("deleteAirline: " + e.getMessage());
        }

        return false;
    }

    // ---------------- COUNT ----------------
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM airlines";
        try (Connection c = ConnectionDB.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("getCount: " + e.getMessage());
        }
        return 0;
    }
}
