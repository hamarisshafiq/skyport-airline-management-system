import java.time.LocalDateTime;

public class Flight {
    private int id;
    private String flightCode; // new string identifier
    private int airlineId;
    private String airlineName; // optional for display
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departDateTime;
    private LocalDateTime arriveDateTime;
    private int durationMinutes;
    private int economySeats;
    private int businessSeats;
    private double economyPrice;
    private double businessPrice;
    private boolean isInternational;
    private String status; // SCHEDULED/CANCELLED/DELAYED

    public Flight() {
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(int airlineId) {
        this.airlineId = airlineId;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String f) {
        this.flightNumber = f;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String o) {
        this.origin = o;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String d) {
        this.destination = d;
    }

    public LocalDateTime getDepartDateTime() {
        return departDateTime;
    }

    public void setDepartDateTime(LocalDateTime dt) {
        this.departDateTime = dt;
    }

    public LocalDateTime getArriveDateTime() {
        return arriveDateTime;
    }

    public void setArriveDateTime(LocalDateTime dt) {
        this.arriveDateTime = dt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int m) {
        this.durationMinutes = m;
    }

    public int getEconomySeats() {
        return economySeats;
    }

    public void setEconomySeats(int s) {
        this.economySeats = s;
    }

    public int getBusinessSeats() {
        return businessSeats;
    }

    public void setBusinessSeats(int s) {
        this.businessSeats = s;
    }

    public double getEconomyPrice() {
        return economyPrice;
    }

    public void setEconomyPrice(double p) {
        this.economyPrice = p;
    }

    public double getBusinessPrice() {
        return businessPrice;
    }

    public void setBusinessPrice(double p) {
        this.businessPrice = p;
    }

    public boolean isInternational() {
        return isInternational;
    }

    public void setInternational(boolean b) {
        this.isInternational = b;
    }

    private String originCountry;
    private String destinationCountry;

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        this.status = s;
    }

    @Override
    public String toString() {
        return (flightCode != null ? flightCode + " | " : "") + id + " | " + flightNumber + " | " + origin + " -> "
                + destination + " | dep:" + departDateTime + " arr:" + arriveDateTime + " | price(e):" + economyPrice;
    }
}
