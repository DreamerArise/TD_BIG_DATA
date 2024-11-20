import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TFIDFCalculator {

    public static void main(String[] args) {
        String directoryPath = "D:/BigData"; // Replace with your directory path
        try {
            // Calculate term frequencies for each document
            Map<String, Map<String, Integer>> termFrequencies = calculateTermFrequencies(directoryPath);
            // Calculate the IDF for each term
            Map<String, Double> idfScores = calculateInverseDocumentFrequency(termFrequencies);
            
            // Calculate the TF-IDF scores
            Map<String, Map<String, Double>> tfidfScores = calculateTFIDF(termFrequencies, idfScores);

            // Print the results in a table format
            for (String doc : tfidfScores.keySet()) {
                System.err.println("_______________________________________________________________________________________________\n");
                System.out.println("                       DOCUMENT:" +doc+"\n");
                System.out.printf("%-20s%-10s%-10s%n", "|Term|", "|TF|", "|TF-IDF|"); // Table headers
                tfidfScores.get(doc).entrySet()
                        .stream()
                        .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))  // Sort by TF-IDF score
                        .forEach(entry -> System.out.printf("%-20s%-10d%-10.4f%n", entry.getKey(), termFrequencies.get(doc).get(entry.getKey()), entry.getValue()));  // Print each entry in the table
                System.out.println(""); // Add a new line after each document
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Map<String, Integer>> calculateTermFrequencies(String directoryPath) throws IOException {
        Map<String, Map<String, Integer>> termFrequencies = new HashMap<>();
        File dir = new File(directoryPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt")); // Filter .txt files

        if (files != null) {
            for (File file : files) {
                Map<String, Integer> termFrequency = new HashMap<>();
                int totalWords = 0;

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] words = line.toLowerCase().split("\\W+"); // Split the words
                        for (String word : words) {
                            if (!word.isEmpty() && word.length() > 1) { // Filter empty and too short words
                                termFrequency.put(word, termFrequency.getOrDefault(word, 0) + 1);
                                totalWords++;
                            }
                        }
                    }
                }

                // Normalize term frequencies relative to total word count
                for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
                    double normalizedFreq = (double) entry.getValue() / totalWords;
                    termFrequency.put(entry.getKey(), (int) (normalizedFreq * 100));  // Optional: scale to percentage
                }

                termFrequencies.put(file.getName(), termFrequency);
            }
        }

        return termFrequencies;
    }

    private static Map<String, Double> calculateInverseDocumentFrequency(Map<String, Map<String, Integer>> termFrequencies) {
        Map<String, Double> idfScores = new HashMap<>();
        int totalDocuments = termFrequencies.size();

        for (Map<String, Integer> termFrequency : termFrequencies.values()) {
            for (String term : termFrequency.keySet()) {
                idfScores.put(term, idfScores.getOrDefault(term, 0.0) + 1);
            }
        }

        // Calculate the IDF
        for (String term : idfScores.keySet()) {
            idfScores.put(term, Math.log((double) totalDocuments / (1 + idfScores.get(term)))); // Adjust to avoid negative IDF
        }

        return idfScores;
    }

    private static Map<String, Map<String, Double>> calculateTFIDF(Map<String, Map<String, Integer>> termFrequencies, Map<String, Double> idfScores) {
        Map<String, Map<String, Double>> tfidfScores = new HashMap<>();

        for (String doc : termFrequencies.keySet()) {
            Map<String, Integer> termFrequency = termFrequencies.get(doc);
            Map<String, Double> tfidfForDoc = new HashMap<>();

            for (String term : termFrequency.keySet()) {
                double tf = termFrequency.get(term);  // You can keep this value if you normalize frequencies in calculateTermFrequencies
                double idf = idfScores.getOrDefault(term, 0.0);
                tfidfForDoc.put(term, tf * idf);  // Calculate the TF-IDF score
            }

            tfidfScores.put(doc, tfidfForDoc);
        }

        return tfidfScores;
    }
}
