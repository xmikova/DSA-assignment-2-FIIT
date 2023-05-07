import java.util.*;
import java.util.stream.Collectors;

//The BDD class.
public class BDD {
    public BDD() {}
    private BDDNode root;
    private String bfunction;
    private int numOfVariables;
    private String orderOfVariables;
    private int size;

    public BDDNode getRoot() {
        return root;
    }
    public void setRoot(BDDNode root) {
        this.root = root;
    }
    public int getSize() {
        return size;
    }

    //The BDD node class.
    static class BDDNode {
        public BDDNode parent;
        List<String> terms;
        private int varIndex;
        private BDDNode lowChild;
        private BDDNode highChild;
        private boolean isTerminal;
        private Boolean value;

        public boolean isTerminal() {
            return isTerminal;
        }
        public int getVarIndex() {
            return varIndex;
        }
        public BDDNode getLowChild() {
            return lowChild;
        }
        public BDDNode getHighChild() {
            return highChild;
        }
        public Boolean getValue() {
            return value;
        }

        public BDDNode() {
        }

        //A toString() function for BDDNode objects for hashing them into keys.
        @Override
        public String toString() {
            if (this == null){
                return null;
            }

            String lowChildStr;
            if (this.lowChild == null) {
                lowChildStr = "null";
            } else {
                lowChildStr = lowChild.toString();
            }
            String highChildStr;
            if (this.highChild == null) {
                highChildStr = "null";
            } else {
                highChildStr = highChild.toString();
            }
            return "(" + varIndex + "," + lowChildStr + "," + highChildStr + ")";
        }

        public BDDNode(boolean isTerminal, Boolean value) {
            this.isTerminal = isTerminal;
            this.value = value;
        }
    }

    //Implementation of three compulsory functions BDD_create, BDD_create_with_best_order and BDD_use.
    public static BDD BDD_create(String bfunction, String order) {
        //Parse the boolean function string to obtain a list of the variables.
        Set<Character> variables;
        variables = retrieveVariablesFromBfunction(bfunction);

        //Parse the variableOrder string to obtain a list of the variables in the order specified by the user.
        List<Character> orderVariableList = new ArrayList<>();
        for (char c : order.toCharArray()) {
            if (variables.contains(Character.toUpperCase(c))) {
                orderVariableList.add(Character.toUpperCase(c));
            }
        }

        //Split the function at + sign and store terms into a list.
        String[] termsarray = bfunction.split("\\+");
        List<String> terms = Arrays.asList(termsarray);

        //Modify the terms in a way that negations of variables are replaced with the lower case version of those variables.
        List<String> modifiedTerms = new ArrayList<>();
        for (String term : terms) {
            StringBuilder sb = new StringBuilder(term);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '!') {
                    sb.deleteCharAt(i);
                    sb.insert(i, Character.toLowerCase(sb.charAt(i)));
                    sb.deleteCharAt(i + 1);
                }
            }
            modifiedTerms.add(sb.toString());
        }
        //Create the two terminal nodes.
        BDDNode trueLeaf = new BDDNode(true, true);
        BDDNode falseLeaf = new BDDNode(true, false);
        BDD bdd = new BDD();
        BDDNode root = null;
        BDDNode currentNode = null;
        bdd.orderOfVariables = order;
        bdd.numOfVariables = variables.size();
        bdd.bfunction = bfunction;
        HashMap<String, BDDNode> uniqueTable = new HashMap<String, BDDNode>();
        root = createBDD_helper(modifiedTerms, orderVariableList, orderVariableList, root, currentNode, trueLeaf, falseLeaf, uniqueTable);
        bdd.setRoot(root); //Set the root to the BDD structure.
        Set<String> visited = new HashSet<>();
        int uniqueNodes = countUniqueNodes(bdd.root, visited);
        bdd.size = uniqueNodes + 2;
        return bdd;
    }

    public static BDD BDD_create_with_best_order(String bfunction) {
        //Retrieve the variables from the function and obtain permutations to try.
        Set<Character> variables;
        variables = retrieveVariablesFromBfunction(bfunction);
        Set<String> permutations = getPermutations(variables);
        List<String> permutationsList = new ArrayList<>(permutations);
        List<Integer> nodeCounts = new ArrayList<>();

        //Create BDD for each permutation and store its size into a List.
        for (int i = 0; i < permutationsList.size(); i++){
            BDD bdd = BDD_create(bfunction, permutationsList.get(i));
            nodeCounts.add(bdd.size);
        }

        //Get the minimal node count.
        int minimum = Collections.min(nodeCounts);

        //Return the BDD with this node count.
        for (int j = 0; j < nodeCounts.size(); j++){
            if (nodeCounts.get(j) == minimum){
                BDD bestBDD = BDD_create(bfunction, permutationsList.get(j));
                return bestBDD;
            }
        }
        return null;
    }

    public static char BDD_use(BDD bdd, String input_values){
        //Check for correct input.
        if (input_values.length() != bdd.numOfVariables || !input_values.matches("[01]+")){
            return 'E'; //error
        }else{ //Evaluate the BDD.
            boolean result = evaluateBDD(bdd.getRoot(),bdd.orderOfVariables,input_values);
            if (result){
                return '1';
            }else if (!result){
                return '0';
            }
        }
        return 'E';
    }


    //Helper methods:
    public static BDDNode createBDD_helper(List<String> Terms, List<Character> orderedV, List<Character> orderedVstatic, BDDNode rootNode, BDDNode parentNode, BDDNode trueLeaf, BDDNode falseLeaf, HashMap<String, BDDNode> uniqueTable) {
        if (orderedV.size() == 1) { //Check for the last variable in the list.
            char variable = orderedV.get(0);
            Character lowerV = Character.toLowerCase(variable);
            CharSequence charSeq = new String(new char[]{variable});
            CharSequence charSeqL = new String(new char[]{lowerV});

            String key = "terms:" + concatTerms(Terms) + ":" + trueLeaf.toString() + ":" + falseLeaf.toString();

            //Check if a node with the same key already exists in the unique table (reduction type I).
            BDDNode existingNode = uniqueTable.get(key);
            if (existingNode != null) {
                return existingNode;
            }

            //If not, create a new one and continue in the process.
            BDDNode variableNode = new BDDNode();
            variableNode.varIndex = variable;
            variableNode.terms = Terms;


            if (Terms.isEmpty()) {
                variableNode.lowChild = falseLeaf;
                variableNode.highChild = falseLeaf;
            } else if (Terms.size() == 1) {
                for (String term : Terms) {
                    if (term.contains(charSeq)) {
                        variableNode.lowChild = falseLeaf;
                        variableNode.highChild = trueLeaf;
                    } else if (term.contains(charSeqL)) {
                        variableNode.lowChild = trueLeaf;
                        variableNode.highChild = falseLeaf;
                    } else if (term.contains("1")) {
                        variableNode.lowChild = trueLeaf;
                        variableNode.highChild = trueLeaf;
                    } else if (term.contains("0")) {
                        variableNode.lowChild = falseLeaf;
                        variableNode.highChild = falseLeaf;
                    }
                }
            } else {
                for (int i = 0; i < Terms.size(); i++) {
                    if (i == (Terms.size() - 1)) {
                        break;
                    }
                    if (Terms.get(i).contains(charSeq)) {
                        if (Terms.get(i + 1).contains(charSeqL)) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = trueLeaf;
                        } else if (Terms.get(i + 1).contains("1")) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = trueLeaf;
                        } else if (Terms.get(i + 1).contains("0")) {
                            variableNode.lowChild = falseLeaf;
                            variableNode.highChild = trueLeaf;
                        }
                    } else if (Terms.get(i).contains(charSeqL)) {
                        if (Terms.get(i + 1).contains(charSeq)) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = trueLeaf;
                        } else if (Terms.get(i + 1).contains("1")) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = trueLeaf;
                        } else if (Terms.get(i + 1).contains("0")) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = falseLeaf;
                        }
                    } else if (Terms.get(i).contains("1")) {
                        if (Terms.get(i + 1).contains(charSeq)) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = trueLeaf;
                        } else if (Terms.get(i + 1).contains(charSeqL)) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = trueLeaf;
                        }
                    } else if (Terms.get(i).contains("0")) {
                        if (Terms.get(i + 1).contains(charSeq)) {
                            variableNode.lowChild = falseLeaf;
                            variableNode.highChild = trueLeaf;
                        } else if (Terms.get(i + 1).contains(charSeqL)) {
                            variableNode.lowChild = trueLeaf;
                            variableNode.highChild = falseLeaf;
                        }
                    }
                }
            }

            if (parentNode == null) {
                rootNode = variableNode;
            } else if (parentNode.highChild == null) {
                parentNode.highChild = variableNode;
            } else {
                parentNode.lowChild = variableNode;
            }

            //Put the node into table and return it.
            uniqueTable.put(key, variableNode);
            return rootNode;

        }


        //Continue with the recursive function if not the last variable.
        Character variable = orderedV.get(0);
        List<String> trueTerms = new ArrayList<>();
        List<String> falseTerms = new ArrayList<>();

        //Determine the true and false terms of current variable.
        for (String term : Terms) {
            if (term.indexOf(variable) == -1) {
                if (term.indexOf(Character.toLowerCase(variable)) != -1) {
                    String newTerm;
                    newTerm = term.replace(Character.toLowerCase(variable), ' ');
                    String cleanedTerm = "";
                    for (int i = 0; i < newTerm.length(); i++) {
                        char c = newTerm.charAt(i);
                        if (Character.isLetter(c)) {
                            cleanedTerm += c;
                        }
                    }
                    if (cleanedTerm.isEmpty()) {
                        cleanedTerm = "1";
                    }
                    falseTerms.add(Associativity(Indempotent(cleanedTerm)));
                } else {
                    String newTerm;
                    newTerm = term.replace(variable, ' ');
                    String cleanedTerm = "";
                    for (int i = 0; i < newTerm.length(); i++) {
                        char c = newTerm.charAt(i);
                        if (Character.isLetter(c)) {
                            cleanedTerm += c;
                        }
                    }
                    if (cleanedTerm.isEmpty()) {
                        cleanedTerm = "1";
                    }
                    trueTerms.add(Associativity(Indempotent(cleanedTerm)));
                    falseTerms.add(Associativity(Indempotent(cleanedTerm)));
                }
            } else {
                String newTerm;
                newTerm = term.replace(variable, ' ');
                String cleanedTerm = "";
                for (int i = 0; i < newTerm.length(); i++) {
                    char c = newTerm.charAt(i);
                    if (Character.isLetter(c)) {
                        cleanedTerm += c;
                    }
                }
                if (cleanedTerm.isEmpty()) {
                    cleanedTerm = "1";
                }
                trueTerms.add(Associativity(Indempotent(cleanedTerm)));
            }
        }

        //Apply Boolean algerba laws to the terms.
        trueTerms = Absorption(InverseLaw(Annulment(Identity(removeDuplicates(trueTerms)))));
        falseTerms = Absorption(InverseLaw(Annulment(Identity(removeDuplicates(falseTerms)))));

        BDDNode trueNode;
        BDDNode falseNode;

        //Recursively create high and low children.
        trueNode = createBDD_helper(trueTerms, orderedV.subList(1, orderedV.size()),orderedVstatic, rootNode, parentNode, trueLeaf, falseLeaf, uniqueTable);
        falseNode = createBDD_helper(falseTerms, orderedV.subList(1, orderedV.size()), orderedVstatic, rootNode, parentNode, trueLeaf, falseLeaf, uniqueTable);

        //Obtain the key of current node.
        String key = "terms:" + concatTerms(Terms) + ":" + trueNode.toString() + ":" + falseNode.toString();

        BDDNode existingNode = uniqueTable.get(key);

        //If the node already exists, return in (reduction type I).
        if (existingNode != null) {
            return existingNode;
        } else { //Else create a new node and set its attributes.
            BDDNode variableNode = new BDDNode();
            variableNode.varIndex = variable;
            variableNode.highChild = trueNode;
            trueNode.parent = variableNode;
            variableNode.lowChild = falseNode;
            falseNode.parent = variableNode;
            variableNode.terms = Terms;
            uniqueTable.put(key, variableNode);
            reduction_typeS(variableNode, uniqueTable); //Check if reduction type S is possible to perform on current node.


            if (parentNode == null) {
                rootNode = variableNode;
            } else if (parentNode.highChild == null) {
                parentNode.highChild = variableNode;
            } else {
                parentNode.lowChild = variableNode;

            }
            //Reduction type S for the root variable
            if (rootNode.varIndex == orderedVstatic.get(0) && rootNode.lowChild.lowChild != null && rootNode.lowChild.highChild != null &&  rootNode.highChild.lowChild != null && rootNode.highChild.highChild != null){
            String rootLowchildkey = "terms:" + concatTerms(rootNode.lowChild.terms) + ":" + rootNode.lowChild.lowChild.toString() + ":" + rootNode.lowChild.highChild.toString();
            String rootHighchildkey = "terms:" + concatTerms(rootNode.highChild.terms) + ":" + rootNode.highChild.lowChild.toString() + ":" + rootNode.highChild.highChild.toString();
                if (rootLowchildkey.equals(rootHighchildkey)) {
                    if (rootNode.lowChild.parent != null ) {
                        rootNode.lowChild.parent = null;
                        rootNode.lowChild.terms.addAll(rootNode.terms);
                        rootNode = rootNode.lowChild;
                        return rootNode;
                    }
                }
            }
            //Return the node.
            return rootNode;
        }
    }

    //Here come the Boolean algebra laws methods.
    public static List<String> removeDuplicates(List<String> terms) {
        Set<String> uniqueTerms = new HashSet<>();
        List<String> nonDuplicateTerms = new ArrayList<>();
        for (String term : terms) {
            if (!uniqueTerms.contains(term)) {
                uniqueTerms.add(term);
                nonDuplicateTerms.add(term);
            }
        }
        return nonDuplicateTerms;
    }

    public static String Indempotent(String term){
        String nonDuplicateTerm = "";
        Set<Character> uniqueChars = new HashSet<>();
        for (int i = 0; i < term.length(); i++) {
            char c = term.charAt(i);
            if (!uniqueChars.contains(c)) {
                uniqueChars.add(c);
                nonDuplicateTerm += c;
            }
        }
        return nonDuplicateTerm;
    }

    public static String Associativity(String term){
        char[] charArray = term.toCharArray();
        Arrays.sort(charArray);
        String sortedStr = String.valueOf(charArray);
        return sortedStr;
    }

    public static List<String> Absorption(List<String> terms) {
        List<String> result = new ArrayList<>(terms);

        for (int i = 0; i < terms.size(); i++) {
            String term1 = terms.get(i);
            for (int j = i + 1; j < terms.size(); j++) {
                String term2 = terms.get(j);
                if (term1.length() < term2.length() && term2.startsWith(term1)) {
                    result.remove(term2);
                } else if (term2.length() < term1.length() && term1.startsWith(term2)) {
                    result.remove(term1);
                    break;
                }
            }
        }

        return result;
    }

    public static List<String> Annulment(List<String> terms) {
        List<String> simplifiedTerms = new ArrayList<>();
        if (terms.contains("1")) {
            simplifiedTerms.add("1");
        } else {
            simplifiedTerms.addAll(terms);
        }
        return simplifiedTerms;
    }

    public static List<String> Identity(List<String> terms) {
        if (terms.contains("0")) {
            terms.remove("0");
        }
        return terms;
    }

    public static List<String> InverseLaw(List<String> terms) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);
            boolean inverseFound = false;
            for (int j = i + 1; j < terms.size(); j++) {
                String otherTerm = terms.get(j);
                if (otherTerm.length() == term.length() &&
                        areInverseTerms(term, otherTerm)) {
                    inverseFound = true;
                    terms.remove(term);
                    terms.remove(otherTerm);
                    result.add("1");
                    break;
                }
            }
            if (!inverseFound) {
                result.add(term);
            }
        }
        return result;
    }

    private static boolean areInverseTerms(String term1, String term2) {
        if (term1.length() != 1 || term2.length() != 1) {
            return false; // Only work for terms of length 1
        }
        char c1 = term1.charAt(0);
        char c2 = term2.charAt(0);
        if (Character.isLowerCase(c1)) {
            if (Character.toUpperCase(c1) == c2) {
                return true;
            }
        } else if (Character.isUpperCase(c1)) {
            if (Character.toLowerCase(c1) == c2) {
                return true;
            }
        }
        return false;
    }

    //Method for putting terms into a single string for a key for a hashtable.
    private static String concatTerms(List<String> terms) {
        if (terms != null){
        Collections.sort(terms);
        return String.join("|", terms);}
        return null;
    }

    //Method for retrieving the variables from a Boolean function.
    public static Set<Character> retrieveVariablesFromBfunction(String bfunction){
        Set<Character> variables = new HashSet<>();
        for (int i = 0; i < bfunction.length(); i++) {
            char c = bfunction.charAt(i);
            if (Character.isLetter(c)) {
                variables.add(c);
            }
        }
        return variables;
    }

    //Method that traverses the BDD and only count every node once even though it is visited multiple times.
    public static int countUniqueNodes(BDDNode root, Set<String> visited) {
        if (root == null) {
            return 0;
        }

        if (root.isTerminal) {
            return 0;
        }

        String hash = "terms:" + concatTerms(root.terms) + ":" + root.highChild + ":" + root.lowChild;

        if (visited.contains(hash)) {
            return 0;
        }
        visited.add(hash);
        int count = 1;
        count += countUniqueNodes(root.getLowChild(), visited);
        count += countUniqueNodes(root.getHighChild(), visited);
        return count;
    }

    //Method that performs a reduction type S on a node checking if its children are equal.
    private static void reduction_typeS(BDDNode node, HashMap<String, BDDNode> uniqueTable) {
        if (node.lowChild.lowChild == node.lowChild.highChild && node.lowChild.lowChild != null && node.lowChild.highChild != null) {
            if (node.lowChild.parent != null) {
                String key = "terms:" + concatTerms(node.lowChild.terms) + ":" + node.lowChild.highChild.toString() + ":" + node.lowChild.lowChild.toString();
                node.lowChild.lowChild.parent = node.lowChild.parent;
                node.lowChild = node.lowChild.lowChild;
                uniqueTable.remove(key);
            }
        }
        if (node.highChild.lowChild == node.highChild.highChild && node.highChild.lowChild != null && node.highChild.highChild != null) {
            if (node.highChild.parent != null || node.highChild.isTerminal) {
                String key = "terms:" + concatTerms(node.highChild.terms) + ":" + node.highChild.highChild.toString() + ":" + node.highChild.lowChild.toString();
                node.highChild.highChild.parent = node.highChild.parent;
                node.highChild = node.highChild.highChild;
                uniqueTable.remove(key);
            }
        }
    }

    //Method that generates permutations from variables for BDD_create_with_best_order.
    public static Set<String> getPermutations(Set<Character> variables) {
        Set<String> permutations = new HashSet<>();
        List<Character> list = new ArrayList<>(variables);
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                List<Character> permutation = new ArrayList<>(list);
                Collections.swap(permutation, i, j);
                permutations.add(permutation.stream().map(String::valueOf).collect(Collectors.joining()));
            }
        }
        return permutations;
    }

    //Method that rewrites a Boolean function into a function that can be parsed by Java.
    public static String rewriteBfunction(String function){
        function = function.replace("+", "||");

        String[] terms = function.split("\\|\\|");
        for (int i = 0; i < terms.length; i++) {
            String term = terms[i].trim(); //Remove any leading or trailing whitespace.
            String modifiedTerm = "";
            for (int j = 0; j < term.length(); j++) {
                char c = term.charAt(j);
                if (Character.isLetter(c)) {
                    if (j != term.length()-1) {
                        modifiedTerm += c + " && ";
                    }else {
                        modifiedTerm += c;
                    }
                } else {
                    modifiedTerm += c;
                }
            }
            terms[i] = modifiedTerm; //Replace original term with modified term.
        }
        function = String.join(" || ", terms);
        return function;
    }

    //Method that parses the Boolean function and evaluates it for the testing of correctness of the BDD.
     public static Boolean evaluateBfunction (String function, String input, String order){
         //Convert input string to boolean array.
         boolean[] inputs = new boolean[input.length()];
         for (int i = 0; i < input.length(); i++) {
             inputs[i] = (input.charAt(i) == '1');
         }

         function = rewriteBfunction(function);

        //Evaluate function.
         for (int i = 0; i < order.length(); i++) {
             char var = order.charAt(i);
             boolean value = inputs[i];
             function = function.replaceAll(Character.toString(var), Boolean.toString(value));

         }

         function = function.replaceAll("!true", "false");
         function = function.replaceAll("!false","true");

         boolean result = false;
         for (String conj : function.split("\\|\\|")) {
             boolean b = true;
             for (String literal : conj.split("&&"))
                 b &= Boolean.parseBoolean(literal.trim());
             result |= b;
         }

         if (result) {
             return true;
         } else if (!result) {
             return false;
         }
         return null;
     }

     //Evaluate BDD using the BDD structure.
     public static boolean evaluateBDD(BDDNode node, String variableOrder, String inputValues){
         if (node == null){
             return false;
         }

         //Return the result at the terminal node.
         if (node.isTerminal()) {
            return node.getValue();
        }

         //Get the index of the variable associated with this node.
         int varIndex = variableOrder.indexOf(node.getVarIndex());

         //Get the value of the variable from the input string.
         boolean varValue = inputValues.charAt(varIndex) == '1';

         //Choose the appropriate child based on the variable value.
         BDDNode child = varValue ? node.getHighChild() : node.getLowChild();

         //Recursively evaluate the child node.
         return evaluateBDD(child, variableOrder, inputValues);
     }

    //Generate all the possible permutations of 0 and 1s for certain amount of variables.
    public static List<String> generatePermutationsForTruthTable(int n) {
        List<String> permutations = new ArrayList<>();
        int numPermutations = (int) Math.pow(2, n);

        for (int i = 0; i < numPermutations; i++) {
            String binaryString = Integer.toBinaryString(i);
            while (binaryString.length() < n) {
                binaryString = "0" + binaryString;
            }
            permutations.add(binaryString);
        }

        return permutations;
    }

    //Method for comparing the results from evaluating the function using Java boolean operators and using BDD.
    public static void testCorrectnessOfAllPossibleOutputs(BDD bdd){
        List<String> perms = generatePermutationsForTruthTable(bdd.numOfVariables);
        int allOutputs = perms.size();
        int correctOutputsFromBDD = 0;
        char result;


        for (int i = 0; i < perms.size(); i++){
            if (evaluateBfunction(bdd.bfunction, perms.get(i), bdd.orderOfVariables) == true){
                result = '1';
            }else if  (evaluateBfunction(bdd.bfunction, perms.get(i), bdd.orderOfVariables) == false){
                result = '0';
            }else result = 'E';

            if (result != BDD_use(bdd, perms.get(i))){
                System.out.println("ERROR");
            }else{
                correctOutputsFromBDD++;
            }
        }
        System.out.println("Number of all possible outputs/Number of correct outputs from BDD: " + allOutputs + "/" + correctOutputsFromBDD);
        double successRate = correctOutputsFromBDD/allOutputs * 100;
        System.out.println("Success rate: " + successRate + "%");
    }

    //Method for generating a random function containing certain amount of variables for testing.
    public static String generateRandomFunction(int numVariables) {
        Random RANDOM = new Random();
        StringBuilder sb = new StringBuilder();

        int numTerms = RANDOM.nextInt(5,10);
        for (int i = 0; i < numTerms; i++) {
            int termLength = RANDOM.nextInt(numVariables) + 1;

            Set<Character> variablesUsed = new HashSet<>();

            StringBuilder termBuilder = new StringBuilder();
            for (int j = 0; j < termLength; j++) {
                char variable;
                boolean negate;

                do {
                    negate = RANDOM.nextBoolean();
                    variable = (char) ('A' + RANDOM.nextInt(numVariables));
                } while (variablesUsed.contains(variable) ||
                        (negate && variablesUsed.contains(Character.toLowerCase(variable))) ||
                        (!negate && variablesUsed.contains(Character.toUpperCase(variable))));

                if (negate) {
                    termBuilder.append("!");
                }
                termBuilder.append(variable);

                variablesUsed.add(variable);
                if (negate) {
                    variablesUsed.add(Character.toLowerCase(variable));
                } else {
                    variablesUsed.add(Character.toUpperCase(variable));
                }
            }
            sb.append(termBuilder.toString()).append("+");
        }
        sb.deleteCharAt(sb.length() - 1); //Remove the trailing +.
        return sb.toString();
    }

    //Generate random order for a certain Boolean function for testing.
    public static String generateRandomOrder(String bfunction) {
        Set<Character> variables = retrieveVariablesFromBfunction(bfunction);
        List<Character> varList = new ArrayList<>(variables);
        int n = varList.size();

        //Shuffle the list using the Fisher-Yates algorithm.
        Random rand = new Random();
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            char temp = varList.get(i);
            varList.set(i, varList.get(j));
            varList.set(j, temp);
        }

        StringBuilder sb = new StringBuilder(n);
        for (char c : varList) {
            sb.append(c);
        }
        return sb.toString();
    }
}