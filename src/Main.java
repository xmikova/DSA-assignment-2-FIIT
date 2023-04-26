public class Main {
    public static void main(String[] args) {
        String function = "AB+AC+AD+AE+BCD!E";
        String order = "ABCDE";
        //String function = "A!B!C+ABC+!AB!C+!A!BC";
        //String order = "ABC";
        BDD bdd = new BDD();
        bdd = BDD.create_BDD(function,order);
        // do something with the created BDD object



    }
}