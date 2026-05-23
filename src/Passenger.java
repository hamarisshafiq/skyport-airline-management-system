public class Passenger {

    private String name;
    private String document;
    private int age;
    private String travelClass;

    public Passenger(String name, String document, int age, String travelClass) {
        this.name = name;
        this.document = document;
        this.age = age;
        this.travelClass = travelClass;
    }

    private double finalPrice;
    private String discountReason;

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public int getAge() {
        return age;
    }

    public String getTravelClass() {
        return travelClass;
    }

    public void setFinalPrice(double p) {
        this.finalPrice = p;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setDiscountReason(String r) {
        this.discountReason = r;
    }

    public String getDiscountReason() {
        return discountReason;
    }
}
