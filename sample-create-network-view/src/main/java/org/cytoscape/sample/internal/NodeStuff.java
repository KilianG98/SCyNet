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
<<<<<<< Updated upstream
    private HashMap<String, List<CyNode>> extNamesToNodes = new HashMap<>();
    final private List<String> allCompartments;
    final private List<String> internalCompartments;
    private List<CyNode> extNodes = new ArrayList<>();
=======
    private final HashMap<String, List<CyNode>> extNamesToNodes = new HashMap<>();
    private List<String> allCompartments;
    private List<String> internalCompartments;
    private final List<CyNode> extNodes = new ArrayList<>();
>>>>>>> Stashed changes
    // Constructor
    public NodeStuff(CyNetwork oldNetwork, CyNetwork newNetwork) {
        this.oldNetwork = oldNetwork;
        this.newNetwork = newNetwork;
        createComps();
        createIntComps();
        createExtNodes();
        addExtNodesToNewNetwork(extNodes, newNetwork);
        addCompNodesToNewNetwork(internalCompartments, newNetwork);
    }

    // Private Methods

    private void newCreateComps() {
        List<String> compartments = new ArrayList<>();
        CyColumn cyIdColumn = oldNetwork.getDefaultNodeTable().getColumn("cyId");
        List<String> cyIdCol = cyIdColumn.getValues(String.class);

        for (String compartment : cyIdCol) {
            if (compartment != null){
                String[] colParts = compartment.split("_");

                // Here I NEED to change the algorithm that it only takes the ones begiining with 'meta' and ending in 'c0' or 'e0'

                if (compartment.length() > 1) {
                    if (compartments.contains(compartment)) {
                        continue;
                    }
                    compartments.add(compartment);
                }
            }
        }
        System.out.println(compartments);
        this.allCompartments = compartments;
    }
    private void createComps() {
        // These are all compartments (external + internal) and the exchange compartment
        List<String> compartments = new ArrayList<>();
        List<CyNode> allNodes = oldNetwork.getNodeList();
        for (CyNode currentNode : allNodes) {
            if (oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("sbml compartment", String.class) != null) {
                String newName = oldNetwork.getDefaultNodeTable().getRow(currentNode.getSUID()).get("sbml compartment", String.class);
                if (newName.length() > 1) {
                    if (compartments.contains(newName)) {
                        continue;
                    } else {
                        compartments.add(newName);
                    }
                }
            }
        }
<<<<<<< Updated upstream
        return compartments;
=======
        System.out.println(compartments);
        this.allCompartments = compartments;
>>>>>>> Stashed changes
    }

    private void createIntComps() {
        // This creates the internal Compartments using all Compartments as input
        List<String> intCompNodeNames = new ArrayList<String>();
        for (String compartment : allCompartments) {
            if (compartment.charAt(2) != 'e') {
                intCompNodeNames.add(compartment);
            }
        }
        intCompNodeNames.remove("exchg");
        this.internalCompartments = intCompNodeNames;
    }

    private void createExtNodes() {
        // creates a Map that maps the Name of Nodes of the old Network to them
        // normally this should be just one Node
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
                    List<CyNode> externalNodeList = new ArrayList<>(Arrays.asList(currentNode));
                    extNamesToNodes.put(currentName, externalNodeList);
                } else {
                    //adding Nodes to the same-name list
                    // extNodes.add(currentNode); // I think this is needed, to allow for multiple Nodes combined in one
                    // new Node with different Targets/Sources. Needs to be CORRECTED for in EdgeStuff
                    extNamesToNodes.get(currentName).add(currentNode);
                }
            }
        }
    }

    private void addExtNodesToNewNetwork(List<CyNode> externalNodes, CyNetwork newNetwork) {
        // adds all the external Nodes from old to new Network and creates Maps translating in both directions
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
        // this uses the internal Compartments to create the compartment Nodes in the new Network and
        // also makes Maps translating Name to Node and vice versa
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
        // gives us the Compartment name of a certain Node of the old Network
        String name = oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("sbml id", String.class);
        if (name == null) {return "unknown";}
        if (name.length() == 0) {return "unknown";}
        if (name.charAt(0) == 'M') {
            String comp = name.substring(name.lastIndexOf('_') + 1);
            if (allCompartments.contains(comp)) {return comp;}
        }
        return "unknown";
    }

    // Public Methods [sorted by output]

    public CyNode getNewNode(CyNode oldNode) {
        return oldToNewNodes.get(oldNode);
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

<<<<<<< Updated upstream
        if (Objects.equals(name, "")) {
            return "unknown";
        }
        String comp = name.substring(name.lastIndexOf('_') + 1);
        if (allCompartments.contains(comp)) {
            return comp;
        }
=======
        if (Objects.equals(comp, ""))    {return "unknown";}
        if (comp == null)                   {return "unknown";}
        if (comp.contains("_"))             {comp = comp.substring(comp.lastIndexOf('_') + 1).toString();}
        if (allCompartments.contains(comp)) {return comp;}
>>>>>>> Stashed changes
        return "unknown";
    }

    public String getCompNameFromNode(CyNode compNode) {
        return compNodeToCompName.get(compNode);
    }

    public String getNodeSharedName(CyNode oldNode) {return oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("shared name", String.class);}

<<<<<<< Updated upstream
    public String getNodeCyID(CyNode node) {
        return oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("CyID", String.class);
=======
    public String getNodeCyID(CyNode node) {return oldNetwork.getDefaultNodeTable().getRow(node.getSUID()).get("CyID", String.class);}

    public CyNode getIntCompNodeForAnyNode(CyNode node){
        String compartment = getCompOfNode(node);
        if (compartment == "unknown"){
            compartment = getNodeCyID(node);
            if (compartment.contains("_"))  {compartment=compartment.substring(compartment.lastIndexOf("_")+1);}
            else                            {System.out.println("critical error!!"); return getCompNodeFromName("erc0");}
        }
        if (compartment.charAt(2) == 'e'){
            compartment = getIntCompNameFromExtCompName(compartment);
        }
        return getCompNodeFromName(compartment);
>>>>>>> Stashed changes
    }

    public List<String> getIntComps(){
        return internalCompartments;
    }

<<<<<<< Updated upstream
=======
    public String getIntCompNameFromExtCompName(String extComp){
        for(String intComp: internalCompartments){
            if (intComp.substring(0,2).equals(extComp.substring(0,2))){
                return intComp;
            }
        }
        return"unknown";
    }
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
            if (compartment.substring(0, 2).equals(extCompartment.substring(0, 2))) {return getCompNodeFromName(compartment);}
        }
        return null;
    }
    public CyNode getOldNode(CyNode newNode) {
        return newToOldNodes.get(newNode);
    }

    public List<String> getAllComps() {
        return allCompartments;
    }

>>>>>>> Stashed changes
}