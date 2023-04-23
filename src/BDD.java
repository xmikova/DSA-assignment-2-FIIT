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
        private int varIndex;
        private BDDNode lowChild;
        private BDDNode highChild;
        private boolean isTerminal;
        private Boolean value;
        private int refCount;


        public BDDNode(int varIndex, BDDNode lowChild, BDDNode highChild, boolean isTerminal, Boolean value) {
            this.varIndex = varIndex;
            this.lowChild = lowChild;
            this.highChild = highChild;
            this.isTerminal = isTerminal;
            this.value = value;
            this.refCount = 0;
        }

        public BDDNode() {
        }

        public BDDNode(Boolean value) {
            this.value = value;
        }

        public BDDNode(boolean isTerminal, Boolean value) {
            this.isTerminal = isTerminal;
            this.value = value;
        }
    }

    private int numOfVariables;
    private String orderOfVariables;
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
        bdd.size = 0;
        root = createBDDNode(modifiedTerms, orderVariableList, root, currentNode, bdd.size);
        bdd.setRoot(root);
        return bdd;
    }

    public static BDDNode createBDDNode(List<String> Terms, List<Character> orderedV, BDDNode rootNode, BDDNode parentNode, int size) {
        if (orderedV.isEmpty()) {
            // Create terminal nodes
            if (Terms.isEmpty()) {
                return new BDDNode(true);
            } else {
                return new BDDNode(false);
            }
        }

        Character variable = orderedV.get(0);
        List<String> trueTerms = new ArrayList<>();
        List<String> falseTerms = new ArrayList<>();
        List<String> bothTerms = new ArrayList<>();

        for (String term : Terms) {
            if (term.indexOf(variable) == -1) {
                if (term.indexOf(Character.toLowerCase(variable)) != -1) {
                    falseTerms.add(term);
                } else {
                    bothTerms.add(term);
                }
            } else {
                trueTerms.add(term);
            }
        }

        BDDNode trueNode = null;
        BDDNode falseNode = null;

        if (!trueTerms.isEmpty()) {
            trueNode = createBDDNode(trueTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode, size + 1);
        }
        if (!falseTerms.isEmpty()) {
            falseNode = createBDDNode(falseTerms, orderedV.subList(1, orderedV.size()), rootNode, parentNode, size + 1);
        }

        BDDNode variableNode = new BDDNode();
        variableNode.varIndex = variable;

        if (trueNode == null) {
            variableNode.highChild = falseNode;
            variableNode.lowChild = falseNode;
        } else if (falseNode == null) {
            variableNode.highChild = trueNode;
            variableNode.lowChild = trueNode;
        } else {
            variableNode.highChild = trueNode;
            variableNode.lowChild = falseNode;
        }

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





