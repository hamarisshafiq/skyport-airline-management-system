import java.time.LocalDate;

public class TourLeg {
    private int id;
    private int tourId;
    private int seqNo;
    private String country;
    private String city;
    private LocalDate stayFrom;
    private LocalDate stayTo;

    // references (not persisted as FK here; DAO maps)
    private TourFlight outboundFlight;
    private TourFlight returnFlight;
    private TourHotel selectedHotel;

    // getters/setters...
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public int getTourId(){return tourId;} public void setTourId(int t){this.tourId=t;}
    public int getSeqNo(){return seqNo;} public void setSeqNo(int s){this.seqNo=s;}
    public String getCountry(){return country;} public void setCountry(String c){this.country=c;}
    public String getCity(){return city;} public void setCity(String c){this.city=c;}
    public java.time.LocalDate getStayFrom(){return stayFrom;} public void setStayFrom(java.time.LocalDate d){this.stayFrom=d;}
    public java.time.LocalDate getStayTo(){return stayTo;} public void setStayTo(java.time.LocalDate d){this.stayTo=d;}
    public TourFlight getOutboundFlight(){return outboundFlight;} public void setOutboundFlight(TourFlight f){this.outboundFlight=f;}
    public TourFlight getReturnFlight(){return returnFlight;} public void setReturnFlight(TourFlight f){this.returnFlight=f;}
    public TourHotel getSelectedHotel(){return selectedHotel;} public void setSelectedHotel(TourHotel h){this.selectedHotel=h;}
}
