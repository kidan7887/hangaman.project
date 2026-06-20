package hangman.logic;

import java.io.*;
import java.util.*;

public class FileWordProvider implements WordProvider {

    private final File wordFile = new File("words.txt");
    private final Random random = new Random();

    public FileWordProvider() {
        if (!wordFile.exists()) {
            createDefaultWordFile();
        }
    }

    private void createDefaultWordFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(wordFile))) {
            writer.write("VARIABLE:EASY");
            writer.newLine();
            writer.write("LOOP:EASY");
            writer.newLine();
            writer.write("CLASS:MEDIUM");
            writer.newLine();
            writer.write("REUSABILITY:MEDIUM");
            writer.newLine();
            writer.write("ENCAPSULATION:HARD");
            writer.newLine();
            writer.write("POLYMORPHISM:HARD");
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error creating words.txt: " + e.getMessage());
        }
    }

    public String getWord(String difficulty) {
        ArrayList<String> matchingWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(wordFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String level = parts[1].trim();

                    if (level.equalsIgnoreCase(difficulty)) {
                        matchingWords.add(word);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading words.txt: " + e.getMessage());
        }

        if (matchingWords.isEmpty()) {
            return "JAVA";
        }

        return matchingWords.get(random.nextInt(matchingWords.size()));
    }
}
