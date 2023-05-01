import java.util.*;
import java.util.stream.Collectors;

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

        @Override
        public String toString() {
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

        public BDDNode() {
        }

        public BDDNode(boolean isTerminal, Boolean value) {
            this.isTerminal = isTerminal;
            this.value = value;
        }
    }

    //Implementation of three compulsory functions BDD_create, BDD_create_with_best_order and BDD_use
    public static BDD BDD_create(String bfunction, String order) {
        // Step 2: Parse the bFunction string to obtain a list of the variables
        Set<Character> variables;
        variables = retrieveVariablesFromBfunction(bfunction);

        // Step 3: Parse the variableOrder string to obtain a list of the variables in the order specified by the user
        List<Character> orderVariableList = new ArrayList<>();
        for (char c : order.toCharArray()) {
            if (variables.contains(Character.toUpperCase(c))) {
                orderVariableList.add(Character.toUpperCase(c));
            }
        }

        String[] termsarray = bfunction.split("\\+");
        List<String> terms = Arrays.asList(termsarray);

        List<String> modifiedTerms = new ArrayList<>();
        for (String term : terms) {
            StringBuilder sb = new StringBuilder(term);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '!') {
                    sb.deleteCharAt(i); // remove the exclamation mark
                    sb.insert(i, Character.toLowerCase(sb.charAt(i))); // insert the lowercase letter at the same position
                    sb.deleteCharAt(i + 1);
                }
            }
            modifiedTerms.add(sb.toString());
        }

        BDDNode trueLeaf = new BDDNode(true, true);
        BDDNode falseLeaf = new BDDNode(true, false);
        BDD bdd = new BDD();
        BDDNode root = null;
        BDDNode currentNode = null;
        bdd.orderOfVariables = order;
        bdd.numOfVariables = variables.size();
        bdd.bfunction = bfunction;
        HashMap<String, BDDNode> uniqueTable = new HashMap<String, BDDNode>();
        root = createBDD_helper(modifiedTerms, orderVariableList, root, currentNode, trueLeaf, falseLeaf, uniqueTable);
        bdd.setRoot(root);
        Set<String> visited = new HashSet<>();
        int uniqueNodes = countUniqueNodes(root, visited);
        bdd.size = uniqueNodes;
        return bdd;
    }

    public static BDD BDD_create_with_best_order(String bfunction) {
        Set<Character> variables;
        variables = retrieveVariablesFromBfunction(bfunction);
        Set<String> permutations = getPermutations(variables);
        List<String> permutationsList = new ArrayList<>(permutations);
        List<Integer> nodeCounts = new ArrayList<>();

        for (int i = 0; i < permutationsList.size(); i++){
            BDD bdd = BDD_create(bfunction, permutationsList.get(i));
            nodeCounts.add(bdd.size);
        }

        int minimum = Collections.min(nodeCounts);

        for (int j = 0; j < nodeCounts.size(); j++){
            if (nodeCounts.get(j) == minimum){
                BDD bestBDD = BDD_create(bfunction, permutationsList.get(j));
                return bestBDD;
            }
        }
        return null;
    }

    public static char BDD_use(BDD bdd, String input_values){
        boolean result = evaluateBDD(bdd.getRoot(),bdd.orderOfVariables,input_values);
        if (result){
            return '1';
        }else if (!result){
            return '0';
        }
        return 'E'; //error
    }


    //Helper methods:
    public static BDDNode createBDD_helper(List<String> Terms, List<Character> orderedV, BDDNode rootNode, BDDNode parentNode, BDDNode trueLeaf, BDDNode falseLeaf, HashMap<String, BDDNode> uniqueTable) {
        if (orderedV.size() == 1) { // check for the last variable in the list
            char variable = orderedV.get(0);
            Character lowerV = Character.toLowerCase(variable);
            CharSequence charSeq = new String(new char[]{variable});
            CharSequence charSeqL = new String(new char[]{lowerV});

            //Reduction: we only use two nodes representing leaf true and false nodes and set pointer of last
            //variables kids to them instead of creating two leaves for each last variable node

            //String key = "terms:" + concatTerms(Terms) + ":var:" + variable + ":" + trueLeaf.toString() + ":" + falseLeaf.toString();
            String key = "terms:" + concatTerms(Terms) + ":" + trueLeaf + ":" + falseLeaf;


            // Check if a node with the same key already exists in the unique table
            BDDNode existingNode = uniqueTable.get(key);
            if (existingNode != null) {
                return existingNode;
            }

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

            uniqueTable.put(key, variableNode);
            return rootNode;

        }


        // continue with the recursive function if not the last variable
        Character variable = orderedV.get(0);
        List<String> trueTerms = new ArrayList<>();
        List<String> falseTerms = new ArrayList<>();
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
                    falseTerms.add(Asociativity(Indempotent(cleanedTerm)));
                    //falseTerms.add(cleanedTerm);

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
                    trueTerms.add(Asociativity(Indempotent(cleanedTerm)));
                    falseTerms.add(Asociativity(Indempotent(cleanedTerm)));
                    //trueTerms.add(cleanedTerm);
                    //falseTerms.add(cleanedTerm);


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
                trueTerms.add(Asociativity(Indempotent(cleanedTerm)));
                //trueTerms.add(cleanedTerm);

            }
        }

        trueTerms = Absorption(InverseLaw(Annulment(Identity(removeDuplicates(trueTerms)))));
        //trueTerms = removeDuplicates(trueTerms);
        falseTerms = Absorption(InverseLaw(Annulment(Identity(removeDuplicates(falseTerms)))));
        //falseTerms = removeDuplicates(falseTerms);
        BDDNode trueNode;
        BDDNode falseNode;

        trueNode = createBDD_helper(trueTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode, trueLeaf, falseLeaf, uniqueTable);
        falseNode = createBDD_helper(falseTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode, trueLeaf, falseLeaf, uniqueTable);


        //String key = "terms:" + concatTerms(Terms) + ":var:" + variable + ":" + trueNode.toString() + ":" + falseNode.toString();
        String key = "terms:" + concatTerms(Terms) + ":" + trueNode + ":" + falseNode;

        //BDDNode existingNode = uniqueTable.get(key);
        BDDNode existingNode = null;


        if (existingNode != null) {
            return existingNode;
        } else {
            BDDNode variableNode = new BDDNode();
            variableNode.varIndex = variable;
            variableNode.highChild = trueNode;
            trueNode.parent = variableNode;
            variableNode.lowChild = falseNode;
            falseNode.parent = variableNode;
            variableNode.terms = Terms;
            uniqueTable.put(key, variableNode);
            reduction_typeS(variableNode, uniqueTable);


            if (parentNode == null) {
                rootNode = variableNode;
            } else if (parentNode.highChild == null) {
                parentNode.highChild = variableNode;
            } else {
                parentNode.lowChild = variableNode;

            }

            return rootNode;
        }
    }

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

    public static String Asociativity(String term){
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
                    terms.add("1");
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


    private static String concatTerms(List<String> terms) {
        Collections.sort(terms);
        return String.join("|", terms);
    }
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

    public static int countUniqueNodes(BDDNode root, Set<String> visited) {
        if (root == null) {
            return 0;
        }

        if (root.isTerminal) {
            return 1;
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

    private static void reduction_typeS(BDDNode node, HashMap<String, BDDNode> uniqueTable) {
        // check if both child nodes point to the same node
        if (node.lowChild.lowChild == node.lowChild.highChild && node.lowChild.lowChild != null && node.lowChild.highChild != null) {
            // set parent node to child node
            if (node.lowChild.parent != null ) {
                //String key = "terms:" + concatTerms(node.lowChild.terms) + ":var:" + (char)node.lowChild.varIndex + ":" + node.lowChild.highChild.toString() + ":" + node.lowChild.lowChild.toString();
                String key = "terms:" + concatTerms(node.lowChild.terms) + ":" + node.lowChild.highChild + ":" + node.lowChild.lowChild;
                node.lowChild.lowChild.parent = node.lowChild.parent;
                node.lowChild = node.lowChild.lowChild;
                uniqueTable.remove(key);
            } else {
                // node is root, replace it with the low child
                node.lowChild.lowChild.parent = null;
                node.lowChild.parent = null;
                node.lowChild.highChild = null;
                node.lowChild.terms.clear();
                node.lowChild.terms.addAll(node.terms);
                node = node.lowChild;
            }
        }
        if (node.highChild.lowChild == node.highChild.highChild && node.highChild.lowChild != null && node.highChild.highChild != null) {
            // set parent node to child node
            if (node.highChild.parent != null || node.highChild.isTerminal) {
                //String key = "terms:"+concatTerms(node.highChild.terms) + ":var:" + (char)node.highChild.varIndex + ":" + node.highChild.highChild.toString() + ":" + node.highChild.lowChild.toString();
                String key = "terms:" +concatTerms(node.highChild.terms) + ":" + node.highChild.highChild + ":" + node.highChild.lowChild;
                node.highChild.highChild.parent = node.highChild.parent;
                node.highChild = node.highChild.highChild;
                uniqueTable.remove(key);

            } else {
            // node is root, replace it with the high child
                node.highChild.highChild.parent = null;
                node.highChild.parent = null;
                node.highChild.lowChild = null;
                node.highChild.terms.clear();
                node.highChild.terms.addAll(node.terms);
                node = node.highChild;
            }
        }
    }

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

    public static String rewriteBfunction(String function){
        function = function.replace("+", "||");

        String[] terms = function.split("\\|\\|"); // split terms by the || operator
        for (int i = 0; i < terms.length; i++) {
            String term = terms[i].trim(); // remove any leading or trailing whitespace
            String modifiedTerm = ""; // initialize modified term string
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
            terms[i] = modifiedTerm; // replace original term with modified term
        }
        function = String.join(" || ", terms); // rejoin terms with the || operator

// Output the converted function
        return function;
     }

     public static Boolean evaluateBfunction (String function, String input, String order){
         // Convert input string to boolean array
         boolean[] inputs = new boolean[input.length()];
         for (int i = 0; i < input.length(); i++) {
             inputs[i] = (input.charAt(i) == '1');
         }

        // Evaluate function
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

     public static boolean evaluateBDD(BDDNode node, String variableOrder, String inputValues){
         // Base case: if node is a terminal node, return its value
         if (node == null){
             return false;
         }

         if (node.isTerminal()) {
            return node.getValue();
        }

         // Get the index of the variable associated with this node
         int varIndex = variableOrder.indexOf(node.getVarIndex());

         // Get the value of the variable from the input string
         boolean varValue = inputValues.charAt(varIndex) == '1';

         // Choose the appropriate child based on the variable value
         BDDNode child = varValue ? node.getHighChild() : node.getLowChild();

         // Recursively evaluate the child node
         return evaluateBDD(child, variableOrder, inputValues);
     }

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

    public static void testCorrectnessOfAllPossibleOutputs(BDD bdd){
        List<String> perms = generatePermutationsForTruthTable(bdd.numOfVariables);
        int allOutputs = perms.size();
        int correctOutputsFromBDD = 0;
        char result;


        for (int i = 0; i < perms.size(); i++){
            if (evaluateBDD(bdd.getRoot(),bdd.orderOfVariables,perms.get(i)) == true){
                result = '1';
            }else if  (evaluateBDD(bdd.getRoot(),bdd.orderOfVariables,perms.get(i)) == false){
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

    public static String generateRandomFunction(int numVariables) {
        Random RANDOM = new Random();
        StringBuilder sb = new StringBuilder();


        for (int i = 0; i < numVariables; i++) {
            // randomly decide whether to negate the variable
            boolean negate = RANDOM.nextBoolean();

            // add the variable (or its negation) to the term
            if (negate) {
                sb.append("!");
            }
            sb.append((char) ('A' + i));
        }


        int numTerms = RANDOM.nextInt(5,12);
        for (int i = 0; i < numTerms; i++) {
            // Generate a random term of length 1 to numVariables
            int termLength = RANDOM.nextInt(numVariables) + 1;
            for (int j = 0; j < termLength; j++) {
                boolean negate = RANDOM.nextBoolean();

                // add the variable (or its negation) to the term
                if (negate) {
                    sb.append("!");
                }
                char variable = (char) ('A' + RANDOM.nextInt(numVariables));
                sb.append(variable);
            }
            sb.append("+");
        }
        // Remove the trailing "+"
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String generateRandomOrder(String bfunction) {
        Set<Character> variables = retrieveVariablesFromBfunction(bfunction);
        // Convert the set to a list for easy shuffling
        List<Character> varList = new ArrayList<>(variables);
        int n = varList.size();

        // Shuffle the list using the Fisher-Yates algorithm
        Random rand = new Random();
        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            char temp = varList.get(i);
            varList.set(i, varList.get(j));
            varList.set(j, temp);
        }

        // Convert the shuffled list back to a string
        StringBuilder sb = new StringBuilder(n);
        for (char c : varList) {
            sb.append(c);
        }
        return sb.toString();
    }
}