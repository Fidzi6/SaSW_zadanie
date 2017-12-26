import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SentiWordNetDemoCode {

    private Map<String, Double> dictionary;

    public SentiWordNetDemoCode(String pathToSWN) throws IOException {
        // This is our main dictionary representation
        dictionary = new HashMap<String, Double>();

        // From String to list of doubles.
        HashMap<String, HashMap<Integer, Double>> tempDictionary = new HashMap<String, HashMap<Integer, Double>>();

        BufferedReader csv = null;
        try {
            csv = new BufferedReader(new FileReader(pathToSWN));
            int lineNumber = 0;

            String line;
            while ((line = csv.readLine()) != null) {
                lineNumber++;

                // If it's a comment, skip this line.
                if (!line.trim().startsWith("#")) {
                    // We use tab separation
                    String[] data = line.split("\t");
                    String wordTypeMarker = data[0];

                    // Example line:
                    // POS ID PosS NegS SynsetTerm#sensenumber Desc
                    // a 00009618 0.5 0.25 spartan#4 austere#3 ascetical#2
                    // ascetic#2 practicing great self-denial;...etc

                    // Is it a valid line? Otherwise, through exception.
                    if (data.length != 6) {
                        throw new IllegalArgumentException(
                                "Incorrect tabulation format in file, line: "
                                        + lineNumber);
                    }

                    // Calculate synset score as score = PosS - NegS
                    Double synsetScore = Double.parseDouble(data[2])
                            - Double.parseDouble(data[3]);

                    // Get all Synset terms
                    String[] synTermsSplit = data[4].split(" ");

                    // Go through all terms of current synset.
                    for (String synTermSplit : synTermsSplit) {
                        // Get synterm and synterm rank
                        String[] synTermAndRank = synTermSplit.split("#");
                        String synTerm = synTermAndRank[0] + "#"
                                + wordTypeMarker;

                        int synTermRank = Integer.parseInt(synTermAndRank[1]);
                        // What we get here is a map of the type:
                        // term -> {score of synset#1, score of synset#2...}

                        // Add map to term if it doesn't have one
                        if (!tempDictionary.containsKey(synTerm)) {
                            tempDictionary.put(synTerm,
                                    new HashMap<Integer, Double>());
                        }

                        // Add synset link to synterm
                        tempDictionary.get(synTerm).put(synTermRank,
                                synsetScore);
                    }
                }
            }

            // Go through all the terms.
            for (Map.Entry<String, HashMap<Integer, Double>> entry : tempDictionary
                    .entrySet()) {
                String word = entry.getKey();
                Map<Integer, Double> synSetScoreMap = entry.getValue();

                // Calculate weighted average. Weigh the synsets according to
                // their rank.
                // Score= 1/2*first + 1/3*second + 1/4*third ..... etc.
                // Sum = 1/1 + 1/2 + 1/3 ...
                double score = 0.0;
                double sum = 0.0;
                 for (Map.Entry<Integer, Double> setScore : synSetScoreMap
                        .entrySet()) {
                    score += setScore.getValue() / (double) setScore.getKey();
                    sum += 1.0 / (double) setScore.getKey();
                }
                score /= sum;

                dictionary.put(word, score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (csv != null) {
                csv.close();
            }
        }
    }

    public double extract(String word, String pos) {
        return dictionary.get(word + "#" + pos);
    }

    public static void main(String [] args) throws IOException {
        SentiWordNetDemoCode sentiwordnet = new SentiWordNetDemoCode("src\\swn\\SentiWordNet_3.0.0_20130122.txt");

        sentiwordnet.showEmotion(sentiwordnet);
    }

    public void showEmotion(SentiWordNetDemoCode sentiwordnet) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your sentence: ");
        String sentence = scanner.next();

        // String veta = "Good day";
        sentence = sentence.toLowerCase();
        String[] tokens = sentence.split(" ");

        Double positivity = new Double(0);
        Double longOfSentence = new Double(0);
        Double activity = new Double(0);
        Double pasivity = new Double(0);

        for (String t : tokens) {
            Double word = new Double(0);
            Double devide = new Double(0);
            try {
                word = word + sentiwordnet.extract(t, "a");
                devide++;
            } catch (Exception exception) {
                //System.out.println("not exist");
            }

            try {
                word = word + sentiwordnet.extract(t, "n");
                devide++;
            } catch (Exception exception) {
                //System.out.println("not exist");
            }

            word = word / devide;
            positivity = positivity + word;

            if (word > 0) {
                activity++;
            } else if (word <= 0) {
                pasivity--;
            }

            longOfSentence++;
        }

        positivity = positivity / longOfSentence * 100;
        Double emotion = (activity + pasivity);

        System.out.println("Positivity of sentence is: " + positivity + "%");
        System.out.println("Emotion number is: " + emotion);
        //System.out.println("Activity number is: " + activity);
        //System.out.println("Pasivity number is: " + pasivity);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");

        if (positivity > 0) {
            System.out.println("Positive sentence");
            if (emotion > 0) {
                System.out.println("I am surpirsed");
            } else if (emotion < 0) {
                System.out.println("I am afraid");
            }
        } else if (positivity < 0) {
            System.out.println("Negative sentence");
            if (emotion > 0) {
                if(emotion > 0.5){
                    System.out.println("I am angry");
                }else {
                    System.out.println("I am afraid");
                }
            } else if (emotion < 0) {
                System.out.println("I am sad");
            }
        }else {
            System.out.println("Neutral sentence");
        }

            sentiwordnet.showEmotion(sentiwordnet);
        }

}