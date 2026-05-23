import java.time.LocalDateTime;
public class TourFlight {
    private int id;
    private int tourLegId;
    private int flightId;
    private String direction; // OUTBOUND / RETURN
    private double price;
    private LocalDateTime flightDatetime;

    // getters/setters...
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public int getTourLegId(){return tourLegId;} public void setTourLegId(int t){this.tourLegId=t;}
    public int getFlightId(){return flightId;} public void setFlightId(int f){this.flightId=f;}
    public String getDirection(){return direction;} public void setDirection(String d){this.direction=d;}
    public double getPrice(){return price;} public void setPrice(double p){this.price=p;}
    public LocalDateTime getFlightDatetime(){return flightDatetime;} public void setFlightDatetime(LocalDateTime dt){this.flightDatetime=dt;}
}
