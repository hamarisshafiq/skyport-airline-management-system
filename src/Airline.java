public class Airline {
    private int id;
    private String name;
    private String code;
    private String country;

    public Airline() {}
    public Airline(String name, String code, String country) {
        this.name = name; this.code = code; this.country = country;
    }
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public String getCountry(){return country;} public void setCountry(String country){this.country=country;}
    public String toString(){ return id+" | "+name+" ("+code+") - "+country; }
}
