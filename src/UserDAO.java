import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Register / insert user (returns true on success)
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, age, city, gender, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword()); // demo: plain-text
            ps.setString(3, user.getFullName());
            ps.setInt(4, user.getAge());
            ps.setString(5, user.getCity());
            ps.setString(6, user.getGender());
            ps.setString(7, user.getEmail());

            int affected = ps.executeUpdate();
            if (affected == 0) return false;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) user.setId(rs.getInt(1));
            }
            return true;

        } catch (SQLException e) {
            System.out.println("createUser error: " + e.getMessage());
            return false;
        }
    }

    // Get user by username
    public User getByUsername(String username) {
        String sql = "SELECT id, username, password, full_name, age, city, gender, email FROM users WHERE username = ?";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPassword(rs.getString("password"));
                    u.setFullName(rs.getString("full_name"));
                    u.setAge(rs.getInt("age"));
                    u.setCity(rs.getString("city"));
                    u.setGender(rs.getString("gender"));
                    u.setEmail(rs.getString("email"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.out.println("getByUsername error: " + e.getMessage());
        }
        return null;
    }

    // List all users (simple)
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, full_name, age, city, gender, email FROM users ORDER BY id";
        try (Connection conn = ConnectionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setFullName(rs.getString("full_name"));
                u.setAge(rs.getInt("age"));
                u.setCity(rs.getString("city"));
                u.setGender(rs.getString("gender"));
                u.setEmail(rs.getString("email"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.out.println("getAll error: " + e.getMessage());
        }
        return list;
    }
}
