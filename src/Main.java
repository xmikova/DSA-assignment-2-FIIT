public class Main {
    public static void main(String[] args) {
        String function = "!ABC+A!BC+AB!C+ABC";
        String order = "ABC";
        BDD bdd = new BDD();
        bdd = bdd.create_BDD(function,order);
        // do something with the created BDD object



    }
}