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
    private HashMap<String, CyNode> compToCompNode;
    private HashMap<CyNode, String> compNodeToComp;
    private HashMap<String, List<CyNode>> externalNodeNamesToNodes;
    final private List<String> allCompartments;
    final private List<String> internalCompartments;
    private List<CyNode> extNodes;
    // Constructor
    public NodeStuff(CyNetwork oldNetwork, CyNetwork newNetwork) {
        this.oldNetwork = oldNetwork;
        this.newNetwork = newNetwork;
        this.allCompartments = createAllCompartments();
        this.internalCompartments = createInternalCompartments();
        this.extNodes = getExtNodes();
        addExternalNodesToNew(extNodes, newNetwork);
        addInternalCompartmentsToNew(internalCompartments, newNetwork);
    }

    // Private Methods
    private List<String> createAllCompartments() {
        List<String> compartments = new ArrayList<String>();
        CyColumn helpVar = oldNetwork.getDefaultNodeTable().getColumn("sbml compartment");
        List<String> compartmentsCol = helpVar.getValues(String.class);

        for (String compartment : compartmentsCol) {
            if (compartment.length() > 1) {
                if (compartments.contains(compartment)) {
                    continue;
                }
                compartments.add(compartment);
            }
        }
        return compartments;
    }

    private List<String> createInternalCompartments() {
        List<String> intCompNodeNames = new ArrayList<String>();
        for (String compartment : allCompartments) {
            if (compartment.charAt(2) != 'e') {
                intCompNodeNames.add(compartment);
            }
        }
        intCompNodeNames.remove("exchg");
        return intCompNodeNames;
    }

    private List<CyNode> getExtNodes() {
        List<CyNode> externalNodes = new ArrayList<CyNode>();
        Set<String> externalNodeNames = new HashSet<String>();
        HashMap<String, List<CyNode>> externalNodeNamesToNodes = new HashMap<>();
        List<CyNode> allNodes = oldNetwork.getNodeList();
        //create List of exchg and external Nodes
        for (CyNode currentNode : allNodes) {
            String currentComp = getRealCompOfNode(currentNode);
            if (currentComp.charAt(2) == 'e') {
                String currentName = oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("shared name", String.class);
                if (!externalNodeNames.contains(currentName)) {
                    externalNodes.add(currentNode);
                    externalNodeNames.add(currentName);
                    //creating a list of the Nodes which have the same name
                    List<CyNode> externalNodeList = new ArrayList<>();
                    externalNodeList.add(currentNode);
                    externalNodeNamesToNodes.put(currentName, externalNodeList);
                } else {
                    //adding nodes to the same name list
                    externalNodeNamesToNodes.get(currentName).add(currentNode);
                }
            }
        }
        this.externalNodeNamesToNodes = externalNodeNamesToNodes;
        return externalNodes;
    }

    private void addExternalNodesToNew(List<CyNode> externalNodes, CyNetwork newNetwork) {
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

    private void addInternalCompartmentsToNew(List<String> compList, CyNetwork newNetwork) {
        HashMap<String, CyNode> compTranslation = new HashMap<>();
        HashMap<CyNode, String> compNodeTranslation = new HashMap<>();
        for (String compartment : compList) {
            CyNode compNode = newNetwork.addNode();

            newNetwork.getDefaultNodeTable().getRow(compNode.getSUID()).set("shared name", compartment);

            compTranslation.put(compartment, compNode);
            compNodeTranslation.put(compNode, compartment);
        }
        this.compToCompNode = compTranslation;
        this.compNodeToComp = compNodeTranslation;
    }

    private String getRealCompOfNode(CyNode node) {

        String name = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml compartment", String.class);

        if (Objects.equals(name, "")) {
            return "unknown";
        }
        if (allCompartments.contains(name)) {
            return name;
        }
        return "unknown";
    }

    // Public Methods
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

    public String getNodeSharedName(CyNode oldNode) {
        return oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("shared name", String.class);
    }

    public String getNodeCyID(CyNode node) {
        return oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("CyID", String.class);
    }

    public CyNode getNodeNew(CyNode oldNode) {
        return oldToNewNodes.get(oldNode);
    }

    public CyNode getNodeOld(CyNode newNode) {
        return newToOldNodes.get(newNode);
    }

    public String getCompName(CyNode compNode) {
        return compNodeToComp.get(compNode);
    }

    public CyNode getCompNode(String compName) {
        return compToCompNode.get(compName);
    }

    public List<String> getAllCompartments() {
        return allCompartments;
    }

    public List<CyNode> getExternalNodes() {
        return extNodes;
    }

    public List<CyNode> getExternalNodesFromName(String nodeName) {
        return externalNodeNamesToNodes.get(nodeName);
    }

    public CyNode getInternalCompFromExternalNode(CyNode externalNode) {
        String extCompartment = getNodeCyID(externalNode);
        //sometimes CyID returns the compartment, other times the compartment is in the last characters
        if (extCompartment.contains("_")) {
            int bIndex = extCompartment.lastIndexOf('_') + 1;
            extCompartment = extCompartment.substring(bIndex);
        }
        for (String compartment: internalCompartments){
            if (compartment.substring(0, 2).equals(extCompartment.substring(0, 2))){
                return getCompNode(compartment);
            }
        }
        return null;
    }
}