import java.util.*;

public class BDD {

    public static int nodeCount = 2;
    public BDD() {
    }

    private BDDNode root;

    public BDDNode getRoot() {
        return root;
    }

    public void setRoot(BDDNode root) {
        this.root = root;
    }

    static class BDDNode {
        public BDDNode parent;
        List<String> terms;
        private int varIndex;
        private BDDNode lowChild;
        private BDDNode highChild;
        private boolean isTerminal;

        private Boolean value;

        @Override
        public String toString() {
            String lowChildStr = (lowChild != null) ? lowChild.toString() : "null";
            String highChildStr = (highChild != null) ? highChild.toString() : "null";
            return "(" + varIndex + "," + lowChildStr + "," + highChildStr + ")";
        }


        public BDDNode() {
        }

        public BDDNode(boolean isTerminal, Boolean value) {
            this.isTerminal = isTerminal;
            this.value = value;
        }
    }


    private int numOfVariables;
    private String orderOfVariables;

    public void setSize(int size) {
        this.size = size;
    }

    private int size;

    public static BDD create_BDD(String bfunction, String order) {
        // Step 2: Parse the bFunction string to obtain a list of the variables
        Set<Character> variables = new HashSet<>();
        for (int i = 0; i < bfunction.length(); i++) {
            char c = bfunction.charAt(i);
            if (Character.isLetter(c)) {
                variables.add(c);
            }
        }

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
        HashMap<String, BDDNode> uniqueTable = new HashMap<String, BDDNode>();
        root = createBDDNode(modifiedTerms, orderVariableList, root, currentNode, trueLeaf, falseLeaf, uniqueTable);
        bdd.setRoot(root);
        bdd.setSize(nodeCount);
        System.out.println(bdd.size);
        return bdd;
    }

    public static BDDNode createBDDNode(List<String> Terms, List<Character> orderedV, BDDNode rootNode, BDDNode parentNode, BDDNode trueLeaf, BDDNode falseLeaf, HashMap<String, BDDNode> uniqueTable) {
        if (orderedV.size() == 1) { // check for the last variable in the list
            char variable = orderedV.get(0);
            Character lowerV = Character.toLowerCase(variable);
            CharSequence charSeq = new String(new char[]{variable});
            CharSequence charSeqL = new String(new char[]{lowerV});

            //Reduction: we only use two nodes representing leaf true and false nodes and set pointer of last
            //variables kids to them instead of creating two leaves for each last variable node

            String key = concatTerms(Terms) + ":" + trueLeaf.toString() + ":" + falseLeaf.toString();

            // Check if a node with the same key already exists in the unique table
            BDDNode existingNode = uniqueTable.get(key);
            if (existingNode != null) {
                return existingNode;
            }

            BDDNode variableNode = new BDDNode();
            nodeCount++;
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
                        cleanedTerm = "0";
                    }
                    falseTerms.add(Asociativity(Indempotent(cleanedTerm)));
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
            }
        }

        trueTerms = Absorption(InverseLaw(Annulment(Identity(removeDuplicates(trueTerms)))));
        falseTerms = Absorption(InverseLaw(Annulment(Identity(removeDuplicates(falseTerms)))));
        BDDNode trueNode;
        BDDNode falseNode;

        trueNode = createBDDNode(trueTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode, trueLeaf, falseLeaf, uniqueTable);
        falseNode = createBDDNode(falseTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode, trueLeaf, falseLeaf, uniqueTable);


        String key = concatTerms(Terms) + ":" + trueNode.toString() + ":" + falseNode.toString();
        BDDNode existingNode = uniqueTable.get(key);

        if (existingNode != null) {
            return existingNode;
        } else {
            BDDNode variableNode = new BDDNode();
            nodeCount++;
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
        for (int i = 0; i < term1.length(); i++) {
            char c1 = term1.charAt(i);
            char c2 = term2.charAt(i);
            if (Character.isLowerCase(c1) && Character.isUpperCase(c2) && Character.toLowerCase(c1) == c2 ||
                    Character.isLowerCase(c2) && Character.isUpperCase(c1) && Character.toLowerCase(c2) == c1) {
                return true;
            }
        }
        return false;
    }

    private static String concatTerms(List<String> terms) {
        Collections.sort(terms);
        return String.join("", terms);
    }

    private static void reduction_typeS(BDDNode node, HashMap<String, BDDNode> uniqueTable) {

        // check if both child nodes point to the same node
        if (node.lowChild.lowChild == node.lowChild.highChild) {
            // set parent node to child node
            if (node.lowChild.parent != null) {
                String key = concatTerms(node.lowChild.terms) + ":" + node.lowChild.highChild.toString() + ":" + node.lowChild.lowChild.toString();
                node.lowChild = node.lowChild.lowChild;
                uniqueTable.remove(key);
                nodeCount--;
            }
        } else if (node.highChild.lowChild == node.highChild.highChild) {
            // set parent node to child node
            if (node.highChild.parent != null) {
                String key = concatTerms(node.highChild.terms) + ":" + node.highChild.highChild.toString() + ":" + node.highChild.lowChild.toString();
                node.lowChild.parent = node.parent;
                node.highChild = node.highChild.highChild;
                uniqueTable.remove(key);
                nodeCount--;
            }
        }
    }
}

