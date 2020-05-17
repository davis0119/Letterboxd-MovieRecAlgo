import java.util.LinkedList;

/*
 * main method to initialize the parser and call methods in parser class
 * 
 * input: none output: none
 */
public class Main {

    public static void main(String[] args) {

        // input username to get top picks from!
        Parser bparse = new Parser("bratpitt");
        bparse.getArticles();
        bparse.getMovies();

        // change number of top picks user wishes to see
        bparse.getHighest(10);
    }

}
