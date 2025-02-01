import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

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
        String pathDostawcy = "Dostawcy.csv";
        String pathMagazynp = "Magazynp.csv";
        String pathSlownik = "Slownik.csv";

        List<String[]> dostawcy = CSVReader.readCSV(pathDostawcy);
        List<String[]> magazynp = CSVReader.readCSV(pathMagazynp);
        List<String[]> slownik = CSVReader.readCSV(pathSlownik);

        // Mapowanie NR_ODPADU na opis
        Map<String, String> slownikMap = slownik.stream()
                .collect(Collectors.toMap(row -> row[5], row -> row[4], (a, b) -> a));

        // Tworzenie listy rekordów magazynowych
        List<MagazynRecord> magazynRecords = magazynp.stream()
                .map(MagazynRecord::new)
                .collect(Collectors.toList());

        // Sortowanie po NRKARTY
        magazynRecords.sort(Comparator.comparing(r -> r.nrKarty));

        // Wyświetlenie zestawienia
        System.out.println("NRKARTY | DATA | KOD | MASA | FIRMA | OPIS");
        magazynRecords.forEach(r -> System.out.printf("%s | %s | %s | %.2f Mg | %s | %s%n",
                r.nrKarty, r.data, r.kod, r.masa, r.firma, slownikMap.getOrDefault(r.kod, "Brak opisu")));

        // Obliczenie sumy masy dla wybranego KODU i FIRMY
        String wybranyKod = "130208";
        String wybranaFirma = "KLIENT1";

        double sumaMasa = magazynRecords.stream()
                .filter(r -> r.kod.equals(wybranyKod) && r.firma.equals(wybranaFirma))
                .mapToDouble(r -> r.masa)
                .sum();

        System.out.printf("Suma masy dla kodu %s i firmy %s: %.2f Mg%n", wybranyKod, wybranaFirma, sumaMasa);
    }
}
