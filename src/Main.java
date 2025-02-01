import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

class CSVReader {
    public static List<String[]> readCSV(String filePath) {
        List<String[]> records = new ArrayList<>();
        Charset charset = StandardCharsets.UTF_8;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = cleanLine(line); // Usuwamy błędy
                String[] values = line.split(";");
                if (values.length > 1) { // Ignorujemy puste linie
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
        return line.replaceAll("[^\\x20-\\x7EąćęłńóśźżĄĆĘŁŃÓŚŹŻ]", "?").trim();
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

        // Wybór pola sortowania
        System.out.println("Wybierz pole do sortowania: 1 - NRKARTY, 2 - KOD, 3 - FIRMA");
        int sortOption = scanner.nextInt();
        scanner.nextLine(); // Usunięcie znaku nowej linii

        // Wybór liczby rekordów
        System.out.println("Ile rekordów wyświetlić?");
        int recordLimit = scanner.nextInt();
        scanner.nextLine();

        String pathMagazynp = "Magazynp.csv";
        String pathSlownik = "Slownik.csv";

        List<String[]> magazynp = CSVReader.readCSV(pathMagazynp);
        List<String[]> slownik = CSVReader.readCSV(pathSlownik);

        // Mapa NR_ODPADU -> OPIS
        Map<String, String> slownikMap = slownik.stream()
                .collect(Collectors.toMap(row -> row[5], row -> row[4], (a, b) -> a));

        // Lista obiektów magazynowych
        List<MagazynRecord> magazynRecords = magazynp.stream()
                .map(MagazynRecord::new)
                .collect(Collectors.toList());

        // Sortowanie na podstawie wyboru użytkownika
        Comparator<MagazynRecord> comparator;
        switch (sortOption) {
            case 2 -> comparator = Comparator.comparing(r -> r.kod);
            case 3 -> comparator = Comparator.comparing(r -> r.firma);
            default -> comparator = Comparator.comparing(r -> r.nrKarty);
        }
        magazynRecords.sort(comparator);

        // Nagłówek tabeli
        System.out.printf("%-12s | %-10s | %-6s | %-8s | %-5s | %-50s%n",
                "NRKARTY", "DATA", "KOD", "MASA", "FIRMA", "OPIS");
        System.out.println("-------------------------------------------------------------------------------");

        // Wyświetlanie wybranej liczby rekordów
        magazynRecords.stream().limit(recordLimit).forEach(r -> System.out.printf(
                "%-12s | %-10s | %-6s | %-8.2f | %-5s | %-50s%n",
                r.nrKarty, r.data, r.kod, r.masa, r.firma, slownikMap.getOrDefault(r.kod, "Brak opisu")
        ));
    }
}
