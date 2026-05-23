import java.util.List;
import java.util.Scanner;

public class MainApp {

    // Hardcoded admin credentials (change as needed)
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserDAO dao = new UserDAO();

        while (true) {
            System.out.println("\n--- MAIN ---");
            System.out.println("1) ADMIN");
            System.out.println("2) USER");
            System.out.println("0) EXIT");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                adminFlow(sc);
            } else if (choice.equals("2")) {
                userFlow(sc, dao);
            } else if (choice.equals("0")) {
                System.out.println("Goodbye.");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }

    private static void adminFlow(Scanner sc) {
        System.out.print("Admin username: ");
        String u = sc.nextLine().trim();
        System.out.print("Admin password: ");
        String p = sc.nextLine().trim();

        if (ADMIN_USER.equals(u) && ADMIN_PASS.equals(p)) {
            System.out.println("Admin login successful.");
            // Admin actions here: show all users
            UserDAO dao = new UserDAO();
            List<User> all = dao.getAll();
            System.out.println("--- All registered users ---");
            if (all.isEmpty()) {
                System.out.println("No users found.");
            } else {
                for (User user : all) {
                    System.out.println(user);
                }
            }
        } else {
            System.out.println("Admin login invalid.");
        }
    }

    private static void userFlow(Scanner sc, UserDAO dao) {
        while (true) {
            System.out.println("\n--- USER ---");
            System.out.println("1) Login");
            System.out.println("2) New User (Register)");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();

            if (c.equals("1")) {
                System.out.print("Username: ");
                String username = sc.nextLine().trim();
                System.out.print("Password: ");
                String password = sc.nextLine().trim();

                User found = dao.getByUsername(username);
                if (found != null && password.equals(found.getPassword())) {
                    System.out.println("Login successful. Welcome " + found.getFullName());
                } else {
                    System.out.println("Invalid username/password.");
                }
            } else if (c.equals("2")) {
                System.out.print("Enter username: ");
                String username = sc.nextLine().trim();
                if (username.isEmpty()) { System.out.println("Username required."); continue; }
                if (dao.getByUsername(username) != null) { System.out.println("Username taken."); continue; }

                System.out.print("Enter password: ");
                String password = sc.nextLine().trim();
                if (password.isEmpty()) { System.out.println("Password required."); continue; }

                System.out.print("Full name: ");
                String fullName = sc.nextLine().trim();

                System.out.print("Age (number): ");
                int age = 0;
                try {
                    String s = sc.nextLine().trim();
                    age = Integer.parseInt(s);
                } catch (Exception ex) {
                    age = 0;
                }

                System.out.print("City: ");
                String city = sc.nextLine().trim();

                System.out.print("Gender: ");
                String gender = sc.nextLine().trim();

                System.out.print("Email: ");
                String email = sc.nextLine().trim();

                User newUser = new User(username, password, fullName, age, city, gender, email);
                boolean ok = dao.createUser(newUser);
                if (ok) {
                    System.out.println("Registration successful. Your ID = " + newUser.getId());
                } else {
                    System.out.println("Registration failed. Check console for error.");
                }

            } else if (c.equals("0")) {
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
