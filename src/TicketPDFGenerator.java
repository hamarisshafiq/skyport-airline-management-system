import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketPDFGenerator {

    public static void generateTickets(Flight flight, List<Passenger> passengers) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================\n");
        sb.append("                       SKYPORT AIRLINES TICKET                  \n");
        sb.append("================================================================\n\n");

        sb.append("FLIGHT DETAILS:\n");
        sb.append("----------------------------------------------------------------\n");
        sb.append("Flight Code:   ").append(flight.getFlightCode()).append("\n");
        sb.append("Airline:       ").append(flight.getAirlineName()).append("\n");
        sb.append("From:          ").append(flight.getOrigin()).append(" (").append(flight.getOriginCountry())
                .append(")\n");
        sb.append("To:            ").append(flight.getDestination()).append(" (").append(flight.getDestinationCountry())
                .append(")\n");
        sb.append("Departure:     ").append(flight.getDepartDateTime()).append("\n");
        sb.append("Arrival:       ").append(flight.getArriveDateTime()).append("\n");
        sb.append("Duration:      ").append(flight.getDurationMinutes()).append(" mins\n\n");

        sb.append("PASSENGER & PRICING DETAILS:\n");
        sb.append("----------------------------------------------------------------\n");

        double totalAmount = 0;

        for (Passenger p : passengers) {
            // We need to fetch base price again or pass it...
            // Better to assume recalculation or carry it?
            // In generateTickets signature we only have List<Passenger>.
            // Passenger object stores final price. Does it store base? No.
            // But we can infer discount = Base - Final? No, base varies by class.
            // I should update Passenger to store Base Price or recalculate it here.
            // Recalculating is safer for now as we have flight object.
            double basePrice = p.getTravelClass().equals("Business") ? flight.getBusinessPrice()
                    : flight.getEconomyPrice();
            double discountAmount = basePrice - p.getFinalPrice();

            sb.append("Name:          ").append(p.getName()).append("\n");
            sb.append("Document:      ").append(p.getDocument()).append("\n");
            sb.append("Age:           ").append(p.getAge()).append("\n");
            sb.append("Class:         ").append(p.getTravelClass()).append("\n");
            sb.append("Base Price:    $").append(basePrice).append("\n");
            sb.append("Discount:      -$").append(String.format("%.2f", discountAmount))
                    .append(" (").append(p.getDiscountReason()).append(")\n");
            sb.append("Final Price:   $").append(String.format("%.2f", p.getFinalPrice())).append("\n");
            sb.append("----------------------------------------------------------------\n");
            totalAmount += p.getFinalPrice();
        }

        sb.append("\n");
        sb.append("================================================================\n");
        sb.append("TOTAL PAID:    $").append(String.format("%.2f", totalAmount)).append("\n");
        sb.append("================================================================\n");
        sb.append("Generated on:  ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");

        // Print to Console
        System.out.println(sb.toString());

        // Save to File
        try {
            String filename = "Ticket_" + flight.getFlightCode() + "_" + System.currentTimeMillis() + ".txt";
            File file = new File(filename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(sb.toString());
            }
            // Logic to open? Runtime.getRuntime().exec("notepad " + filename); logic
            // optional
            javax.swing.JOptionPane.showMessageDialog(null, "Ticket Generated: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error generating ticket file.");
        }
    }
}
