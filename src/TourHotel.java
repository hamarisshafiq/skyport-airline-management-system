public class TourHotel {
    private int id;
    private int tourLegId;
    private int hotelId;
    private double priceTotal;

    public int getId(){return id;} public void setId(int id){this.id=id;}
    public int getTourLegId(){return tourLegId;} public void setTourLegId(int t){this.tourLegId=t;}
    public int getHotelId(){return hotelId;} public void setHotelId(int h){this.hotelId=h;}
    public double getPriceTotal(){return priceTotal;} public void setPriceTotal(double p){this.priceTotal=p;}
}
