public class Hotel {
    private int id;
    private String name;
    private String country;
    private String city;
    private String category;
    private String address;
    private String email;
    private String contact;
    private double pricePerNight;

    public Hotel() {}

    public Hotel(String name, String country, String city, String category,
                 String address, String email, String contact, double pricePerNight) {
        this.name = name;
        this.country = country;
        this.city = city;
        this.category = category;
        this.address = address;
        this.email = email;
        this.contact = contact;
        this.pricePerNight = pricePerNight;
    }

    // Getters / Setters (COMPLETE)
    public int getId(){ return id; }
    public void setId(int id){ this.id=id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name=name; }

    public String getCountry(){ return country; }
    public void setCountry(String country){ this.country=country; }

    public String getCity(){ return city; }
    public void setCity(String city){ this.city=city; }

    public String getCategory(){ return category; }
    public void setCategory(String category){ this.category=category; }

    public String getAddress(){ return address; }
    public void setAddress(String address){ this.address=address; }

    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email=email; }

    public String getContact(){ return contact; }
    public void setContact(String contact){ this.contact=contact; }

    public double getPricePerNight(){ return pricePerNight; }
    public void setPricePerNight(double pricePerNight){ this.pricePerNight = pricePerNight; }

    public String toString() {
        return id + " | " + name + " | " + city + " | " + country + " | " + category;
    }
}
