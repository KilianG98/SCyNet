package org.cytoscape.sample.internal;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.*;

public class NodeStuff {

    final private CyNetwork oldNetwork;
    private CyNetwork newNetwork;
    private HashMap<CyNode, CyNode> oldToNewNodes;
    private HashMap<CyNode, CyNode> newToOldNodes;
    private HashMap<String, CyNode> compNameToCompNode;
    private HashMap<CyNode, String> compNodeToCompName;
    private HashMap<String, List<CyNode>> extNamesToNodes = new HashMap<>();
    final private List<String> allCompartments;
    final private List<String> internalCompartments;
    private List<CyNode> extNodes = new ArrayList<>();
    // Constructor
    public NodeStuff(CyNetwork oldNetwork, CyNetwork newNetwork) {
        this.oldNetwork = oldNetwork;
        this.newNetwork = newNetwork;
        this.allCompartments = createComps();
        this.internalCompartments = createIntComps();
        createExtNodes();
        addExtNodesToNewNetwork(extNodes, newNetwork);
        addCompNodesToNewNetwork(internalCompartments, newNetwork);
    }

    // Private Methods
    private List<String> createComps() {
        // These are all compartments (external + internal) and the exchange compartment
        List<String> compartments = new ArrayList<>();
        CyColumn compartmentColumn = oldNetwork.getDefaultNodeTable().getColumn("sbml compartment");
        List<String> compartmentsCol = compartmentColumn.getValues(String.class);

        for (String compartment : compartmentsCol) {
            if (compartment != null){
                if (compartment.length() > 1) {
                    if (compartments.contains(compartment)) {
                        continue;
                    }
                    compartments.add(compartment);
                }
            }
        }
        return compartments;
    }

    private List<String> createIntComps() {
        List<String> intCompNodeNames = new ArrayList<String>();
        for (String compartment : allCompartments) {
            if (compartment.charAt(2) != 'e') {
                intCompNodeNames.add(compartment);
            }
        }
        intCompNodeNames.remove("exchg");
        return intCompNodeNames;
    }

    private void createExtNodes() {
        List<CyNode> allNodes = oldNetwork.getNodeList();
        List<String> externalNodeNames = new ArrayList<>();

        for (CyNode currentNode : allNodes) {
            String currentComp = getCompOfMetaboliteNode(currentNode);

            if (currentComp.charAt(2) == 'e') {
                String currentName = oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("shared name", String.class);

                if (!externalNodeNames.contains(currentName)) {
                    extNodes.add(currentNode);
                    externalNodeNames.add(currentName);
                    //creating a list of the Nodes which have the same name
                    List<CyNode> externalNodeList = new ArrayList<>();
                    externalNodeList.add(currentNode);
                    extNamesToNodes.put(currentName, externalNodeList);
                } else {
                    //adding nodes to the same name list
                    extNamesToNodes.get(currentName).add(currentNode);
                }
            }
        }
    }

    private void addExtNodesToNewNetwork(List<CyNode> externalNodes, CyNetwork newNetwork) {
        HashMap<CyNode, CyNode> oldNewTranslation = new HashMap<>();
        HashMap<CyNode, CyNode> newOldTranslation = new HashMap<>();
        for (CyNode oldNode : externalNodes) {
            CyNode newNode = newNetwork.addNode();

            String oldName = oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("shared name", String.class);
            newNetwork.getDefaultNodeTable().getRow(newNode.getSUID()).set("shared name", oldName);

            oldNewTranslation.put(oldNode, newNode);
            newOldTranslation.put(newNode, oldNode);
        }
        this.oldToNewNodes = oldNewTranslation;
        this.newToOldNodes = newOldTranslation;
    }

    private void addCompNodesToNewNetwork(List<String> compList, CyNetwork newNetwork) {
        HashMap<String, CyNode> compNameTranslation = new HashMap<>();
        HashMap<CyNode, String> compNodeTranslation = new HashMap<>();
        for (String compartment : compList) {
            CyNode compNode = newNetwork.addNode();

            newNetwork.getDefaultNodeTable().getRow(compNode.getSUID()).set("shared name", compartment);

            compNameTranslation.put(compartment, compNode);
            compNodeTranslation.put(compNode, compartment);
        }
        this.compNameToCompNode = compNameTranslation;
        this.compNodeToCompName = compNodeTranslation;
    }

    private String getCompOfMetaboliteNode(CyNode node) {
        String name = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);

        if (name == null) {
            return "unknown";
        }
        if (name.length() == 0){return "unknown";}
        if (name.charAt(0) == 'M'){
            String comp = name.substring(name.lastIndexOf('_') + 1);
            if (allCompartments.contains(comp)) {
                return comp;
            }
        }
        return "unknown";
    }

    // Public Methods [sorted by output]

    public CyNode getIntCompNodeFromExtNode(CyNode externalNode) {
        // This gives us the compartment for the INCOMING edges [DIRECT IN CyID]
        String extCompartment = getNodeCyID(externalNode);
        if (extCompartment.contains("_")) {
            // This gives us the compartment for the OUTGOING edges [LAST CHARACTERS]
            int bIndex = extCompartment.lastIndexOf('_') + 1;
            extCompartment = extCompartment.substring(bIndex);
        }
        // This has to possible inputs: A NODE in the compartment or THE COMPARTMENT node
        for (String compartment: internalCompartments){
            if (compartment.substring(0, 2).equals(extCompartment.substring(0, 2))){

                return getCompNodeFromName(compartment);
            }
        }
        return null;
    }

    public CyNode getNewNode(CyNode oldNode) {
        return oldToNewNodes.get(oldNode);
    }

    public CyNode getOldNode(CyNode newNode) {
        return newToOldNodes.get(newNode);
    }

    public CyNode getCompNodeFromName(String compName) {
        return compNameToCompNode.get(compName);
    }

    public List<CyNode> getExtNodes() {
        return extNodes;
    }

    public List<CyNode> getExtNodesFromName(String nodeName) {
        return extNamesToNodes.get(nodeName);
    }

    public String getCompOfNode(CyNode node) {

        String name = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);

        if (Objects.equals(name, "")) {
            return "unknown";
        }
        String comp = name.substring(name.lastIndexOf('_') + 1);
        if (allCompartments.contains(comp)) {
            return comp;
        }
        return "unknown";
    }

    public String getCompNameFromNode(CyNode compNode) {
        return compNodeToCompName.get(compNode);
    }

    public String getNodeSharedName(CyNode oldNode) {
        return oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("shared name", String.class);
    }

    public String getNodeCyID(CyNode node) {
        return oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("CyID", String.class);
    }

    public List<String> getAllComps() {
        return allCompartments;
    }

    public List<String> getIntComps(){
        return internalCompartments;
    }

}