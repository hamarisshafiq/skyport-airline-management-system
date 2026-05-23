public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private int age;
    private String city;
    private String gender;
    private String email;

    public User() {}

    public User(String username, String password, String fullName, int age, String city, String gender, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.age = age;
        this.city = city;
        this.gender = gender;
        this.email = email;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return id + " | " + username + " | " + fullName + " | " + age + " | " + city + " | " + gender + " | " + email;
    }
}

