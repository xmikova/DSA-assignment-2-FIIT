import java.util.*;

public class BDD {

    public BDD() {
    }

    private BDDNode root;

    public BDD(BDDNode root) {
        this.root = root;
    }

    public BDDNode getRoot() {
        return root;
    }

    public void setRoot(BDDNode root) {
        this.root = root;
    }

    static class BDDNode {
        public static int nodeCount = 0;

        private int varIndex;
        private BDDNode lowChild;
        private BDDNode highChild;
        private boolean isTerminal;

        public static int getNodeCount() {
            return nodeCount;
        }

        private Boolean value;
        private int refCount;


        public BDDNode(int varIndex, BDDNode lowChild, BDDNode highChild, boolean isTerminal, Boolean value, int nodeCount) {
            this.varIndex = varIndex;
            this.lowChild = lowChild;
            this.highChild = highChild;
            this.isTerminal = isTerminal;
            this.value = value;
            this.refCount = 0;
            nodeCount++;
        }

        public BDDNode() {
            nodeCount++;
        }

        public BDDNode(Boolean value) {
            this.value = value;
        }

        public BDDNode(boolean isTerminal, Boolean value) {
            this.isTerminal = isTerminal;
            this.value = value;
            nodeCount++;
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

        BDD bdd = new BDD();
        BDDNode root = null;
        BDDNode currentNode = null;
        root = createBDDNode(modifiedTerms, orderVariableList, root, currentNode);
        bdd.setRoot(root);
        bdd.size = BDDNode.getNodeCount();
        return bdd;
    }

    public static BDDNode createBDDNode(List<String> Terms, List<Character> orderedV, BDDNode rootNode, BDDNode parentNode) {
        if (orderedV.size() == 1) { // check for the last variable in the list
            char variable = orderedV.get(0);
            Character lowerV = Character.toLowerCase(variable);
            CharSequence charSeq = new String(new char[] {variable});
            CharSequence charSeqL = new String(new char[] {lowerV});

            BDDNode trueNode = new BDDNode(true, null);
            BDDNode falseNode = new BDDNode(true, null);

            if (Terms.isEmpty()){
                trueNode.value = false;
                falseNode.value = false;
            }
            else if (Terms.size()== 1){
                for (String term : Terms){
                if (term.contains(charSeq)){
                    trueNode.value = true;
                    falseNode.value = false;
                }else if (term.contains(charSeqL)) {
                    trueNode.value = false;
                    falseNode.value = true;
                }else if (term.contains("1")){
                    trueNode.value = true;
                    falseNode.value = true;
                }else if (term.contains("0")){
                    trueNode.value = false;
                    falseNode.value = false;
                }
                }}
            else {
                    trueNode.value = true;
                    falseNode.value = true;
                }


            BDDNode variableNode = new BDDNode();
            variableNode.varIndex = variable;
            variableNode.highChild = trueNode;
            variableNode.lowChild = falseNode;
            if (parentNode == null) {
                rootNode = variableNode;
            } else if (parentNode.highChild == null) {
                parentNode.highChild = variableNode;
            } else {
                parentNode.lowChild = variableNode;
            }
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
                    if (cleanedTerm.isEmpty()){
                        cleanedTerm = "0";
                    }
                    falseTerms.add(cleanedTerm);
                } else {
                    trueTerms.add(term);
                    falseTerms.add(term);
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
                if (cleanedTerm.isEmpty()){
                    cleanedTerm = "1";
                }
                trueTerms.add(cleanedTerm);
            }
        }

        removeDuplicates(trueTerms);
        removeDuplicates(falseTerms);
        BDDNode trueNode = null;
        BDDNode falseNode = null;

        trueNode = createBDDNode(trueTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode);
        falseNode = createBDDNode(falseTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode);

        BDDNode variableNode = new BDDNode();
        variableNode.varIndex = variable;
        variableNode.highChild = trueNode;
        variableNode.lowChild = falseNode;

        if (parentNode == null) {
            rootNode = variableNode;
        } else if (parentNode.highChild == null) {
            parentNode.highChild = variableNode;
        } else {
            parentNode.lowChild = variableNode;
        }
        return rootNode;
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
}




