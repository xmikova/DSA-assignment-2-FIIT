import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args){
        BDD bdd = new BDD();
        bdd = BDD.BDD_create("ABCD+!ABCD","ABCD");
        testTheBDD("ABCD+!ABCD","ABCD");
    }

    public static void testTheBDD(String bfunction, String order){
        BDD bdd = new BDD();
        BDD bddBestOrder = new BDD();
        Set<Character> variables = BDD.retrieveVariablesFromBfunction(bfunction);
        int numberOfVars = variables.size();

        long start = System.currentTimeMillis();
        bdd = BDD.BDD_create(bfunction,order);
        long end = System.currentTimeMillis();

        long total = end - start;
        bddBestOrder = BDD.BDD_create_with_best_order(bfunction);
        int fullBDDSize = (int)Math.pow(2,numberOfVars +1)-1;
        int bddSize = bdd.getSize();
        int bddBestOrderSize = bddBestOrder.getSize();
        double reductionRateBDD = ((double)(fullBDDSize - bddSize)/fullBDDSize) * 100.0;
        double reductionRateBDDBestOrder = ((double)(fullBDDSize - bddBestOrderSize)/fullBDDSize) * 100.0;
        double BDDtoBDDbestorderRatio = (double)bddBestOrderSize / bddSize * 100.0;



        System.out.println("Function: " + bfunction);
        System.out.println();
        System.out.println("Time of creation of BDD in milliseconds: " + total);
        System.out.println("Number of nodes of full BDD: " + fullBDDSize);
        System.out.println("Number of nodes of reduced BDD with input order: " + bdd.getSize());
        System.out.println("Number of nodes of reduced BDD with best order: " + bddBestOrder.getSize());
        System.out.println("Reduction rate of BDD with input order: " + String.format("%.2f", reductionRateBDD) + "%");
        System.out.println("Reduction rate of BDD with best order: " +String.format("%.2f", reductionRateBDDBestOrder) + "%");
        System.out.println("Input order reduced BDD to best order reduced BDD size ratio: " + String.format("%.2f", BDDtoBDDbestorderRatio) + "%");
        System.out.println();
        System.out.println("Input order BDD success rate:");
        BDD.testCorrectnessOfAllPossibleOutputs(bdd);
        System.out.println();
        System.out.println("Best order BDD success rate:");
        BDD.testCorrectnessOfAllPossibleOutputs(bddBestOrder);
        System.out.println();

    }

    public static void testTheBDDRandom(int numberOfVars){
      String bfunction = BDD.generateRandomFunction(numberOfVars);
      String order = BDD.generateRandomOrder(bfunction);
      System.out.println(order);
      BDD bdd = new BDD();
      BDD bddBestOrder = new BDD();

      long start = System.currentTimeMillis();
      bdd = BDD.BDD_create(bfunction,order);
      long end = System.currentTimeMillis();

      long total = end - start;
      bddBestOrder = BDD.BDD_create_with_best_order(bfunction);
      int fullBDDSize = (int)Math.pow(2,numberOfVars +1)-1;
      int bddSize = bdd.getSize();
      int bddBestOrderSize = bddBestOrder.getSize();
      double reductionRateBDD = ((double)(fullBDDSize - bddSize)/fullBDDSize) * 100.0;
      double reductionRateBDDBestOrder = ((double)(fullBDDSize - bddBestOrderSize)/fullBDDSize) * 100.0;
      double BDDtoBDDbestorderRatio = (double)bddBestOrderSize / bddSize * 100.0;



      System.out.println("Function: " + bfunction);
      System.out.println();
      System.out.println("Time of creation of BDD in milliseconds: " + total);
      System.out.println("Number of nodes of full BDD: " + fullBDDSize);
      System.out.println("Number of nodes of reduced BDD with random order: " + bdd.getSize());
      System.out.println("Number of nodes of reduced BDD with best order: " + bddBestOrder.getSize());
      System.out.println("Reduction rate of BDD with random order: " + String.format("%.2f", reductionRateBDD) + "%");
      System.out.println("Reduction rate of BDD with best order: " +String.format("%.2f", reductionRateBDDBestOrder) + "%");
      System.out.println("Random order reduced BDD to best order reduced BDD size ratio: " + String.format("%.2f", BDDtoBDDbestorderRatio) + "%");
      System.out.println();
      System.out.println("Random order BDD success rate:");
      BDD.testCorrectnessOfAllPossibleOutputs(bdd);
      System.out.println();
      System.out.println("Best order BDD success rate:");
      BDD.testCorrectnessOfAllPossibleOutputs(bddBestOrder);
      System.out.println();

    }

    public static void testTheBDD100Times(int numberOfVars){
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++){
            testTheBDDRandom(numberOfVars);
        }
        long end = System.currentTimeMillis();
        long total = end - start;

        System.out.println("Time in milliseconds to create 100 random BDDs with " + numberOfVars + " variables: " + total);
    }

    public static void timeComplexityTest(){
        List<Integer> numsOfVars = new ArrayList<>();
        for (int i = 13; i <= 20; i++) {
            numsOfVars.add(i);
        }

        for (int i = 0; i < numsOfVars.size(); i++){
            long start = System.currentTimeMillis();
            for (int j = 0; j < 100; j++){
                String bfunction = BDD.generateRandomFunction(numsOfVars.get(i));
                String order = BDD.generateRandomOrder(bfunction);
                BDD bdd = BDD.BDD_create(bfunction,order);
            }
            long end = System.currentTimeMillis();
            long total = end - start;

            System.out.println("Time in milliseconds to create 100 random BDDs with " + numsOfVars.get(i) + " variables: " + total);
        }
    }

    public static void timeComplexityTestBestBDD(){
        List<Integer> numsOfVars = new ArrayList<>();
        for (int i = 13; i <= 20; i++) {
            numsOfVars.add(i);
        }

        for (int i = 0; i < numsOfVars.size(); i++){
            long start = System.currentTimeMillis();
            for (int j = 0; j < 100; j++){
                String bfunction = BDD.generateRandomFunction(numsOfVars.get(i));
                BDD bdd = BDD.BDD_create_with_best_order(bfunction);
            }
            long end = System.currentTimeMillis();
            long total = end - start;

            System.out.println("Time in milliseconds to create 100 random BDDs with " + numsOfVars.get(i) + " variables: " + total);
        }
    }
}