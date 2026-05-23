import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Tour {
    private int id;
    private String title;
    private String type; // "SINGLE" or "MULTI"
    private double totalCost;
    private double discountPct = 20.0;
    private double finalCost;
    private LocalDateTime createdAt;

    private List<TourLeg> legs = new ArrayList<>();

    public Tour() {}
    // getters / setters
    public int getId(){return id;} public void setId(int id){this.id=id;}
    public String getTitle(){return title;} public void setTitle(String t){this.title=t;}
    public String getType(){return type;} public void setType(String t){this.type=t;}
    public double getTotalCost(){return totalCost;} public void setTotalCost(double c){this.totalCost=c;}
    public double getDiscountPct(){return discountPct;} public void setDiscountPct(double d){this.discountPct=d;}
    public double getFinalCost(){return finalCost;} public void setFinalCost(double f){this.finalCost=f;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime t){this.createdAt=t;}
    public List<TourLeg> getLegs(){return legs;} public void setLegs(List<TourLeg> l){this.legs=l;}
}
