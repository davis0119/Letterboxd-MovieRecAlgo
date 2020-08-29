import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedHashMap;

/*
 * class that contains methods to parse Letterboxd account
 * 
 * input: none output: none
 */

public class Parser {

    private String baseURL;
    private Document currentDoc;
    private Document currentDoc2;
    private Document currentDoc3;
    private Map<String, Document> MapURL;
    private Map<String, Integer> vector;
    private List<String> collection;
    private String textToCheck;

    /*
     * Constructor that initializes the base URL and loads the document produced
     * from that URL
     */
    public Parser(String x) {
        // establishing connection with 3 pages max for runtime efficiency
        this.baseURL = "https://letterboxd.com/" + x + "/followers";
        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
            try {
                this.currentDoc2 = Jsoup.connect(this.baseURL + "/page/2/").get();
                try {
                    this.currentDoc3 = Jsoup.connect(this.baseURL + "/page/3/").get();
                } catch (IOException e) {
                    System.out.println("Could not find username");
                }
            } catch (IOException e) {
                System.out.println("Could not find username");
            }
        } catch (IOException e) {
            System.out.println("Could not find username");
        }
    }

    /*
     * Creates article mappings and stores the html of the friends of the user
     * 
     * input: none output: none
     */
    public void getArticles() {
        String total = "";
        MapURL = new HashMap<String, Document>();

        // getting the usernames of the user friends
        Elements articleElement = this.currentDoc.getElementsByClass("title-3");
        Elements articleElement2 = this.currentDoc2.getElementsByClass("title-3");
        Elements articleElement3 = this.currentDoc3.getElementsByClass("title-3");
        textToCheck = articleElement.text() + " " + articleElement2.text() + " " + articleElement3.text();

        // parsing the username into readable format
        String description = articleElement.toString() + " " + articleElement2.toString() + " "
                + articleElement3.toString();
        Pattern r = Pattern.compile("href\\=.*?\\sc");
        Matcher m = r.matcher(description);
        while (m.find()) {
            String parsed = m.group(0);
            total = total + parsed;
        }

        // parsing one more time into readable format
        Pattern a = Pattern.compile("\\/.*?\\/");
        Matcher b = a.matcher(total);
        while (b.find()) {
            Document temp;
            String parsed = b.group(0);
            try {
                // establish a connection to that page and add info into MapURL
                // with the usernames as the keys and html as the sources
                temp = Jsoup.connect("https://letterboxd.com" + parsed + "likes/films").get();
                MapURL.put(parsed, temp);
            } catch (IOException e) {
                System.out.println("Could not find username");
            }
        }
        clean();
    }

    /*
     * Cleaning the scrap with unecessary material like the heading text
     * 
     * input: none 
     * output: none
     */
    public void clean() {
        collection = new LinkedList<String>();
        collection.add("\"24\"");
    }

    /*
     * filtering the text without the usernames of friends
     * 
     * input: text to check 
     * output: none
     */
    public boolean filter(String x) {
        boolean temp = true;
        // if the text to check is in the collection
        // of garbage material, then return false.
        for (String q : collection) {
            if (x.equals(q)) {
                temp = false;
            }
        }
        if (textToCheck.contains(x)) {
            temp = false;
        }
        return temp;
    }

    public void getMovies() {
        vector = new LinkedHashMap<String, Integer>();
        String total = "";
        for (String names : MapURL.keySet()) {
            // iterate through the htmls and find keyword by alt
            Elements movieElements = MapURL.get(names).getElementsByAttribute("alt");
            String movieDesc = movieElements.toString();

            // find specific part of code using pattern
            Pattern r = Pattern.compile("alt=\\\".*?>");
            Matcher m = r.matcher(movieDesc);
            while (m.find()) {
                String parsed = m.group(0);
                total = total + " " + parsed;
            }
        }

        // find specific part of code using pattern
        Pattern a = Pattern.compile("\\\".*?\\\"");
        Matcher b = a.matcher(total);
        while (b.find()) {
            // format the code properly
            String parsed = b.group(0);
            String update = parsed.replaceAll("\"", "");

            // if the code is "clean" by checking with the
            // filter function, then add to the vector
            // map appropriately
            if (filter(update) && filter(parsed)) {
                if (vector.containsKey(parsed)) {
                    vector.put(parsed, vector.get(parsed) + 1);
                } else if (!vector.containsKey(parsed)) {
                    vector.put(parsed, 1);
                }
            }
        }
    }

    /*
     * returns the highest values in the vector map
     * 
     * input: text to check 
     * output: none
     */
    public List<String> getHighest(int highest) {
        // list all movies in descending recommendation order
        LinkedList<String> descendingList = new LinkedList<String>();
        // keep track of all the times users overlap in liked movies
        TreeSet<Integer> keys = new TreeSet<Integer>(vector.values());
        // what's the top number of recommendations you want?
        ArrayList<String> topPicks = new ArrayList<String>();

        // keys are in ascending order, want the opposite
        Iterator<Integer> it = keys.descendingIterator();
        while (it.hasNext()) {
            int nextHighest = it.next();
            for (String movie : this.vector.keySet()) {
                if (this.vector.get(movie) == nextHighest) {
                    descendingList.add(movie);
                }
            }
        }
        for (int i = 0; i < highest; i++) {
            topPicks.add(descendingList.get(i));
        }
        System.out.println(topPicks);
        return topPicks;
    }

}
