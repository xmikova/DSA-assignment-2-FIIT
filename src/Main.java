import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner obj = new Scanner(System.in);

        System.out.println("The testing of BDD structure:");
        System.out.println("If you want to test specific function, enter 1.");
        System.out.println("If you want to test one random function, enter 2.");
        System.out.println("If you want to test 100 random functions, enter 3.");
        System.out.println("For a full automated time complexity test, enter 4.");
        System.out.println("Enter a number: ");
        int input = obj.nextInt();
        obj.nextLine();

        switch(input){
            case 1:
                System.out.println("Enter a function: ");
                String function = obj.nextLine();
                System.out.println("Enter an order of variables: ");
                String order = obj.nextLine();
                testTheBDD(function,order);
                break;
            case 2:
                System.out.println("Enter the number of variables for function: ");
                int vars = obj.nextInt();
                testTheBDDRandom(vars);
                break;
            case 3:
                System.out.println("Enter the number of variables for function: ");
                int vars2 = obj.nextInt();
                testTheBDD100Times(vars2);
                break;
            case 4:
                timeComplexityTest();
                break;
        }
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
}