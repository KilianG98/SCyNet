package org.cytoscape.sample.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.*;

public class Nodes {

    final private CyNetwork oldNetwork;
    private final CyNetwork newNetwork;
    public Set<String> compartments;
    public Set<String> internalCompartments;
    public List<CyNode> extNodes;
    private HashMap<CyNode, List<CyNode>> outEdges;
    private HashMap<CyNode, List<CyNode>> inEdges;
    private HashMap<String, CyNode> sharedNameToNewNode;
    private HashMap<CyNode, String> nodeToCompartment;
    private HashMap<String, CyNode> compartmentToNode;
    private HashMap<CyNode, CyNode> oldToNewNodes;
    private HashMap<CyNode, List<CyNode>> newToOldNodes;


    // Constructor
    public Nodes(CyNetwork oldNetwork, CyNetwork newNetwork) {
        this.oldNetwork = oldNetwork;
        this.newNetwork = newNetwork;
        this.compartments = createComps();
        this.internalCompartments = createIntComps();
        this.extNodes = createExtNodes();
        createCompNodes();
        // creatingEdges();
    }

    // Private Methods
    private Set<String> createComps() {
        // These are all compartments (external + internal) without the exchange compartment
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
        System.out.println(comps);
        return comps;
    }

    private Set<String> createIntComps() {
        // This creates the internal Compartments using all Compartments as input
        Set<String> intCompNodeNames = new HashSet<>();
        for (String compartment : compartments) {
            if (compartment.charAt(compartment.length()-2) != 'e') {
                intCompNodeNames.add(compartment);
            }
        }
        return intCompNodeNames;
    }

    private List<CyNode> createExtNodes() {
        // This creates all external Nodes and the Edgemaps for them
        // We also create two HashMaps to translate old Nodes to new ones and vice versa
        List<CyNode> externalNodes = new ArrayList<>();
        List<CyNode> allNodes = oldNetwork.getNodeList();

        HashMap<String, CyNode> externalNodeNamesToNewNode = new HashMap<>();
        HashMap<CyNode, CyNode> oldNodeToNewNode = new HashMap<>();
        HashMap<CyNode, List<CyNode>> newNodeToOldNodes = new HashMap<>();
        HashMap<CyNode, List<CyNode>> outgoingEdges = new HashMap<>();
        HashMap<CyNode, List<CyNode>> incomingEdges = new HashMap<>();

        for (CyNode currentNode : allNodes) {
            String currentComp = fromWhichComp(currentNode);
            if (currentComp.charAt(currentComp.length()-2) == 'e') {
                String currentSharedName = oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("shared name", String.class);
                if (!externalNodeNamesToNewNode.containsKey(currentSharedName)) {
                    // adding the Node to the new Network and updating externalNodes as well as the Node-Translators
                    CyNode newNode = newNetwork.addNode();
                    newNetwork.getDefaultNodeTable().getRow(newNode.getSUID()).set("shared name", currentSharedName);
                    externalNodes.add(newNode);
                    oldNodeToNewNode.put(currentNode, newNode);
                    newNodeToOldNodes.put(newNode, new ArrayList<>(Arrays.asList(currentNode)));

                    // adding shared name to externalNodeNamesToNewNode
                    externalNodeNamesToNewNode.put(currentSharedName, newNode);

                    // making first entry in EdgeMap for this Node
                    List<CyNode> outTargets = getTargets(currentNode);
                    List<CyNode> inSources = getSources(currentNode);

                    // THESE TARGETS AND SOURCES ARE NOT SUFFICIENT BECAUSE WE NEED TO GO ONE FURTHER
                    outgoingEdges.put(newNode, outTargets);
                    incomingEdges.put(newNode, inSources);
                } else {
                    // we specify the new Node which has already been placed but is duplicate in old Network
                    CyNode alreadyPlacedNode = externalNodeNamesToNewNode.get(currentSharedName);

                    // here we update the EdgeMap because teh Node already exists
                    List<CyNode> outTargets = getTargets(currentNode);
                    List<CyNode> inSources = getSources(currentNode);

                    // THESE TARGETS AND SOURCES ARE NOT SUFFICIENT BECAUSE WE NEED TO GO ONE FURTHER
                    outgoingEdges.get(alreadyPlacedNode).addAll(outTargets);
                    incomingEdges.get(alreadyPlacedNode).addAll(inSources);

                    // we also update the Node-Translators
                    oldNodeToNewNode.put(currentNode, alreadyPlacedNode);
                    newNodeToOldNodes.get(alreadyPlacedNode).add(currentNode);
                }
            }
        }
        this.outEdges = outgoingEdges;
        this.inEdges = incomingEdges;
        this.sharedNameToNewNode = externalNodeNamesToNewNode;
        this.oldToNewNodes = oldNodeToNewNode;
        this.newToOldNodes = newNodeToOldNodes;
        return externalNodes;
    }

    private void createCompNodes() {
        // this uses the internal Compartments to create the compartment Nodes in the new Network and
        // also makes Maps translating Name to Node and vice versa
        HashMap<String, CyNode> compNameTranslation = new HashMap<>();
        HashMap<CyNode, String> compNodeTranslation = new HashMap<>();

        for (String internalCompartment : internalCompartments) {
            System.out.println(internalCompartment);
            CyNode compNode = newNetwork.addNode();
            newNetwork.getDefaultNodeTable().getRow(compNode.getSUID()).set("shared name", internalCompartment);
            compNameTranslation.put(internalCompartment, compNode);
            compNodeTranslation.put(compNode, internalCompartment);

            String extComp = internalCompartment.substring(0,internalCompartment.length()-2).concat("e0");
            System.out.println(extComp);
            compNameTranslation.put(extComp, compNode);
        }
        this.compartmentToNode = compNameTranslation;
        this.nodeToCompartment = compNodeTranslation;
    }

    private String getCompTypeOfNode(CyNode node) {
        // gives us the type of compartment of a certain Node of the old Network
        String name = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);
        if (name == null) {return "unknown";}
        if (name.length() > 0 && name.charAt(0) == 'M') {
            String comp = name.substring(name.lastIndexOf('_') + 1);
            return comp; // if (compartments.contains(comp)) {return comp;}
        }
        return "unknown";
    }

    private String fromWhichComp(CyNode node) {
        if (oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class) != null) {
            String name = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);
            String compName = name.substring(Math.max(name.length() - 2, 0));
            if (compName.equals("c0")) {
                String[] listId = name.split("_", 0);
                if (listId.length > 3) {
                    return listId[1].concat("c0");
                }
            } else if (compName.equals("e0")) {
                String[] listId = name.split("_", 0);
                if (listId.length > 3) {
                    return listId[1].concat("e0");
                }
            }
        }
        return "ERROR";
    }

    private void creatingEdges() {
        for (CyNode externalNode : extNodes) {

            List<CyNode> oldTargets = outEdges.get(externalNode);
            for (CyNode oldTarget : oldTargets) {
                CyEdge newEdge = newNetwork.addEdge(externalNode, oldTarget, true);
            }

            List<CyNode> oldSources = inEdges.get(externalNode);
            for (CyNode oldSource : oldSources) {
                CyEdge newEdge = newNetwork.addEdge(oldSource, externalNode, true);
            }
        }
    }

    private List<CyNode> getTargets(CyNode node) {
        List<CyNode> nodes = new ArrayList<>();
        nodes.add(node);
        return nodes;
    }

    private List<CyNode> getSources(CyNode node) {
        List<CyNode> nodes = new ArrayList<>();
        nodes.add(node);
        return nodes;
    }

    // Public Methods
    public CyNode getCompNodeFromName(String name) {
        return compartmentToNode.get(name);
    }

    public Set<String> getIntComps(){
        return internalCompartments;
    }
}
