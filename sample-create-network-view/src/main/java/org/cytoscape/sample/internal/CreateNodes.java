package org.cytoscape.sample.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.*;

public class CreateNodes {

    final private CyNetwork oldNetwork;
    private CyNetwork newNetwork;
    private HashMap<CyNode, CyNode> oldToNewNodes;
    private HashMap<CyNode, CyNode> newToOldNodes;
    private HashMap<String, CyNode> compNameToCompNode;
    private HashMap<CyNode, String> compNodeToCompName;
    private final HashMap<String, List<CyNode>> extNamesToNodes = new HashMap<>();
    final private Set<String> allCompartments;
    final private List<String> internalCompartments;
    private final List<CyNode> extNodes = new ArrayList<>();
    // Constructor
    public CreateNodes(CyNetwork oldNetwork, CyNetwork newNetwork) {
        this.oldNetwork = oldNetwork;
        this.newNetwork = newNetwork;
        this.allCompartments = createComps();
        this.internalCompartments = createIntComps();
        createExtNodes();
        addExtNodesToNewNetwork(extNodes);
        addCompNodesToNewNetwork(internalCompartments);
    }

    // Private Methods
    private Set<String> createComps() {
        // These are all compartments (external + internal) without the exchange compartment, which is added in the end
        Set<String> comps = new HashSet<>();
        List<CyNode> allNodes = oldNetwork.getNodeList();
        for (CyNode currentNode : allNodes) {
            if (oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("sbml id", String.class) != null) {
                String currentId = oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("sbml id", String.class);
                if (currentId.substring(Math.max(currentId.length() - 2, 0)).equals("c0")) {
                    String[] listId = currentId.split("_", 0);
                    if (listId.length > 3) {
                        comps.add(listId[1].concat("c0"));
                        comps.add(listId[1].concat("e0"));
                    }
                }
            }
        }
        comps.add("exchg");
        System.out.println(comps);
        return comps;
    }

    private List<String> createIntComps() {
        // makes a list of all internal compartments by removing the external ones from all compartments
        List<String> intCompNodeNames = new ArrayList<>();
        for (String compartment : allCompartments) {
            if (compartment.charAt(compartment.length() - 2) != 'e') {
                intCompNodeNames.add(compartment);
            }
        }
        intCompNodeNames.remove("exchg");
        return intCompNodeNames;
    }

    private void createExtNodes() {
        // here a list of external Nodes is created by looping through all Nodes and only adding one Node of a certain type (shared name)
        List<CyNode> allNodes = oldNetwork.getNodeList();
        List<String> externalNodeNames = new ArrayList<>();

        for (CyNode currentNode : allNodes) {
            String currentComp = getCompOfMetaboliteNode(currentNode);
            if (currentComp.charAt(currentComp.length() - 2) == 'e') {
                String currentName = oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("shared name", String.class);
                if (!externalNodeNames.contains(currentName)) {
                    extNodes.add(currentNode);
                    externalNodeNames.add(currentName);
                    List<CyNode> externalNodeList = new ArrayList<>();
                    externalNodeList.add(currentNode);
                    extNamesToNodes.put(currentName, externalNodeList);
                } else {
                    extNamesToNodes.get(currentName).add(currentNode);
                }
            }
        }
    }

    private void addExtNodesToNewNetwork(List<CyNode> externalNodes) {
        // here the external Nodes are added to the new Network and HashMaps mapping old to new Nodes is created simultaneously
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

    private void addCompNodesToNewNetwork(List<String> compList) {
        // here the compartment Nodes are added to the new Network and HashMaps mapping old to new Nodes is created simultaneously
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
        // here we return the compartment of a Node only if it is a Metabolite
        if (oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class) != null) {
            String currentId = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);
            if (currentId.length() > 0 && currentId.charAt(0) == 'M') {
                String comp = getCompOfSBML(currentId);
                if (allCompartments.contains(comp)) {return comp;}
            }
        }
        return "unknown";
    }

    private String getCompOfSBML(String sbmlId) {
        // this method is used to translate every sbmlId-string into the corresponding compartment
        String ending = sbmlId.substring(Math.max(sbmlId.length() - 2, 0));
        switch (ending) {
            case "c0": {
                String[] listId = sbmlId.split("_", 0);
                if (listId.length > 3) {
                    return listId[1].concat("c0");
                } else {
                    return sbmlId;
                }
            }
            case "e0": {
                String[] listId = sbmlId.split("_", 0);
                if (listId.length > 3) {
                    return listId[1].concat("e0");
                } else {
                    return sbmlId;
                }
            }
            case "hg": {
                String[] listId = sbmlId.split("_", 0);
                if (listId.length > 3) {
                    return listId[2];
                } else {
                    return sbmlId;
                }
            }
        }
        return "unknown";
    }

    private String getCompOfNode(CyNode node) {
        // here the compartment of a Node is calculated using its sbmlId
        if (oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class) != null) {
            String currentId = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);
            return getCompOfSBML(currentId);
        }
        return "unknown";
    }

    // Public Methods [sorted by output]

    public CyNode getIntCompNodeForAnyNode(CyNode node){
        // here the internal compartment corresponding to a Node is returned, regardless where the Node is placed
        String compartment = getCompOfNode(node);

        if (compartment.charAt(compartment.length() - 2) == 'e'){
            String comp = compartment.substring(0,compartment.length() - 2).concat("c0");
            return getCompNodeFromName(comp);
        } else {
            return getCompNodeFromName(compartment);
        }
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

    public String getCompNameFromNode(CyNode compNode) {
        return compNodeToCompName.get(compNode);
    }

    public String getNodeSharedName(CyNode oldNode) {
        return oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("shared name", String.class);
    }

    public Set<String> getAllComps() {
        return allCompartments;
    }

    public List<String> getIntComps(){
        return internalCompartments;
    }
}