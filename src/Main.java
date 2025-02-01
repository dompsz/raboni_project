import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.nio.charset.Charset;
import java.util.*;
import java.io.*;

class CSVReader {
    public static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();
        Charset charset = StandardCharsets.UTF_8;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = cleanLine(line); // Odczytując usuwa błędne rekordy
                String[] values = line.split(";");
                if (values.length > 1) { // ignoruje puste linie
                    records.add(values);
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd odczytu pliku: " + filePath);
        }
        return records;
    }

    private static String cleanLine(String line) {
        // Zamienia błędne znaki na "?" i usuwa niewidoczne znaki
        return line.replaceAll("[^\\x20-\\x7E]", "?").trim();
    }
}

class MagazynRecord {
    String nrKarty, data, kod, firma;
    double masa;

    public MagazynRecord(String[] data) {
        this.nrKarty = data[0];
        this.data = data[1];
        this.kod = data[6]; // NR_ODPADU
        this.masa = parseMasa(data[2]);
        this.firma = data[4];
    }

    private double parseMasa(String masaStr) {
        try {
            return Double.parseDouble(masaStr.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Wybierz pole do sortowania: 1 - NRKARTY, 2 - KOD, 3 - FIRMA");
        int sortOption = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Ile rekordów wyświetlić?");
        int recordLimit = scanner.nextInt();
        scanner.nextLine();

        String pathMagazynp = "Magazynp.csv";
        String pathSlownik = "Slownik.csv";

        List<String[]> magazynp = CSVReader.readCSV(pathMagazynp);
        List<String[]> slownik = CSVReader.readCSV(pathSlownik);

        // mapa NR_ODPADU -> OPIS
        Map<String, String> slownikMap = slownik.stream()
                .collect(Collectors.toMap(row -> row[5], row -> row[4], (a, b) -> a));

        // Lista obiektów magazynowych
        List<MagazynRecord> magazynRecords = magazynp.stream()
                .map(MagazynRecord::new)
                .collect(Collectors.toList());

        // sortowanie
        Comparator<MagazynRecord> comparator;
        switch (sortOption) {
            case 2 -> comparator = Comparator.comparing(r -> r.kod);
            case 3 -> comparator = Comparator.comparing(r -> r.firma);
            default -> comparator = Comparator.comparing(r -> r.nrKarty);
        }
        magazynRecords.sort(comparator);

        System.out.printf("%-12s | %-10s | %-6s | %-8s | %-5s | %-50s%n",
                "NRKARTY", "DATA", "KOD", "MASA", "FIRMA", "OPIS");
        System.out.println("-------------------------------------------------------------------------------");

        magazynRecords.stream().limit(recordLimit).forEach(r -> System.out.printf(
                "%-12s | %-10s | %-6s | %-8.2f | %-5s | %-50s%n",
                r.nrKarty, r.data, r.kod, r.masa, r.firma, slownikMap.getOrDefault(r.kod, "Brak opisu")
        ));

        System.out.println("\nPodaj KOD, dla którego chcesz obliczyć sumę masy:");
        String selectedKod = scanner.nextLine();
        System.out.println("Podaj FIRMĘ:");
        String selectedFirma = scanner.nextLine();

        // obliczenie sumy masy
        double totalMasa = magazynRecords.stream()
                .filter(r -> r.kod.equals(selectedKod) && r.firma.equals(selectedFirma))
                .mapToDouble(r -> r.masa)
                .sum();

        System.out.printf("\nSuma masy dla KOD: %s i FIRMA: %s wynosi: %.2f Mg%n",
                selectedKod, selectedFirma, totalMasa);
    }
}