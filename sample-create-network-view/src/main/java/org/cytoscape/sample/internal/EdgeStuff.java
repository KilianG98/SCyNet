package org.cytoscape.sample.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import java.util.*;

public class EdgeStuff {
    private final List<CyNode> cyNodeList;
    private final CyNetwork oldNetwork;
    private final CyNetwork newNetwork;
    private final NodeStuff nodeStuff;
    private final HashMap<CyNode, List<CyEdge>> outgoingEdges;
    private final HashMap<CyNode, List<CyEdge>> incomingEdges;
    private final List<String> edgeIDs;
<<<<<<< Updated upstream
    private final List<CyNode> externalNodes;
    private  HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullSourceNodeToEdgeMap;
    private  HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullTargetNodeToEdgeMap;
=======
    private final List<CyNode> oldExternalNodes;

    private List<CyEdge> EdgeIds = new ArrayList<>();
    private HashMap<CyNode, Set<CyNode>> sourceToTargets = new HashMap<>();
    private HashMap <CyNode, Set<CyNode>> targetToSources = new HashMap<>();
>>>>>>> Stashed changes

    public EdgeStuff(CyNetwork oldNetwork, CyNetwork newNetwork, NodeStuff nodeStuff) {
        this.edgeIDs = new ArrayList<>();
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
        this.nodeStuff = nodeStuff;
        this.cyNodeList = oldNetwork.getNodeList();
        this.externalNodes = nodeStuff.getExtNodes();
        this.outgoingEdges = mkOutEdges();
        this.incomingEdges = mkInEdges();
        makeAllEdges();
    }
    private HashMap<CyNode, List<CyEdge>> mkOutEdges(){
        HashMap<CyNode, List<CyEdge>> outEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            outEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.OUTGOING));
        }
        return outEdges;
    }
    private HashMap<CyNode, List<CyEdge>> mkInEdges(){
        HashMap<CyNode, List<CyEdge>> inEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            inEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.INCOMING));
        }
        return inEdges;
    }
    private void makeAllEdges() {
        // makes all columns in the new Edge-Table and then creates all the Edges to and from a Node
        newNetwork.getDefaultEdgeTable().createColumn("source", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("target", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("edgeID", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("stoichiometry", Double.class, true);
        HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullOutMap = new HashMap<>();
        HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullInMap = new HashMap<>();
        makeEdgesToNode(externalNodes);
        makeEdgesFromNode(externalNodes);
    }
    private List<CyEdge> getTargetsOld(CyNode cyNode) {
        List<CyEdge> edgeTargets = outgoingEdges.get(cyNode);
        return new ArrayList<>(edgeTargets);
    }
    private List<CyEdge> getSourcesOld(CyNode cyNode) {
        List<CyEdge> edgeSources = incomingEdges.get(cyNode);
        return new ArrayList<>(edgeSources);
    }

<<<<<<< Updated upstream
    private void makeEdgesToNode(List<CyNode> oldExtNodes) {
        int counterID = 0;
        // This I will need WHEN I SEPARATE THE METHODS
        HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullSourceNodeToEdgeMap = new HashMap<>();
        for (CyNode oldExternalNode : oldExtNodes) {
            CyNode newExternalNode = nodeStuff.getNewNode(oldExternalNode);
            // List<CyNode> oldSources = new ArrayList<>();     MAYBE NOT NEEDED (are keys of HashMap)
            HashMap<CyNode, List<CyEdge>> sourceNodeToEdgeMap = new HashMap<>();

            List<CyEdge> oldEdges = getSourcesOld(oldExternalNode);
            List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExternalNode));

            for (CyNode similarNode : similarNodes) {
                oldEdges.addAll(getSourcesOld(similarNode));
            }
            for (CyEdge oldEdge : oldEdges) {
                CyNode oldSourceNode = oldEdge.getSource();
                List<CyEdge> oldSourceEdges = new ArrayList<>();
                oldSourceEdges.add(oldEdge);
                if (sourceNodeToEdgeMap.containsKey(oldSourceNode)) {
                    System.out.printf("WARNING! Already contains %s%n", oldSourceNode);
                    sourceNodeToEdgeMap.get(oldSourceNode).add(oldEdge);
                    // oldSources.add(oldSourceNode);       MAYBE NOT NEEDED (are keys of HashMap)

                } else {
                    sourceNodeToEdgeMap.put(oldSourceNode, oldSourceEdges);
                    // oldSources.add(oldSourceNode);       MAYBE NOT NEEDED (are keys of HashMap)
=======
    private void makeEdgesToNode() {
        // using the extNodes from NodeStuff, this creates all Edges to every external Node
        for (CyNode oldExtNode : oldExternalNodes) {
            List<CyNode> oldSources = getAllNeighbors(oldExtNode, "Sources");
            for (CyNode oSource : oldSources) {
                if (oldExternalNodes.contains(oSource)) {
                    CyEdge edge = makeEdge(nodeStuff.getNewNode(oSource), nodeStuff.getNewNode(oldExtNode));
                    edgeTributes(edge, oSource, oldExtNode);
                    } else {
                    CyNode comp = nodeStuff.getIntCompNodeForAnyNode(oSource);
                    CyEdge edge = makeEdge(comp,nodeStuff.getNewNode(oldExtNode));
                    edgeTributesComp(edge, oSource, oldExtNode, true);
>>>>>>> Stashed changes
                }
            }
            fullSourceNodeToEdgeMap.put(oldExternalNode, sourceNodeToEdgeMap);

            // I need oldExternalNode, OldSources, sourceNodeToEdgeMap
            // we give the first (as keys) and last one to the full map
        }
        this.fullSourceNodeToEdgeMap = fullSourceNodeToEdgeMap;
        makeEdgesIn();
    }

<<<<<<< Updated upstream
    private void makeEdgesFromNode(List<CyNode> oldExtNodes) {
        int counterID = 0;
        // This I will need WHEN I SEPARATE THE METHODS
        HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullTargetNodeToEdgeMap = new HashMap<>();
        for (CyNode oldExtNode : oldExtNodes) {
            CyNode newExternalNode = nodeStuff.getNewNode(oldExtNode);
            // List<CyNode> oldTargets = new ArrayList<>();     MAYBE NOT NEEDED (are keys of HashMap)
            HashMap<CyNode, List<CyEdge>> targetNodeToEdgeMap = new HashMap<>();

            List<CyEdge> oldEdges = getTargetsOld(oldExtNode);
            // oldEdges should be empty here, because the oldExtNode is present in similar nodes
            List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExtNode));

            for (CyNode similarNode : similarNodes) {
                oldEdges.addAll(getTargetsOld(similarNode));
            }
            for (CyEdge oldEdge : oldEdges) {
                CyNode oldTargetNode = oldEdge.getTarget();
                List<CyEdge> oldTargetEdges = new ArrayList<>();
                oldTargetEdges.add(oldEdge);
                if (targetNodeToEdgeMap.containsKey(oldTargetNode)) {
                    System.out.printf("WARNING! Already contains %s%n", oldTargetNode);
                    targetNodeToEdgeMap.get(oldTargetNode).add(oldEdge);
                    // oldSources.add(oldSourceNode);       MAYBE NOT NEEDED (are keys of HashMap)

=======
    private void makeEdgesFromNode(){
        // using the extNodes from NodeStuff, this creates all Edges from every external Node
        for (CyNode oldExtNode : oldExternalNodes) {
            List<CyNode> oldTargets = getAllNeighbors(oldExtNode, "Targets");
            for (CyNode oTarget : oldTargets) {
                if (oldExternalNodes.contains(oTarget)) {
                    System.out.println("es passiert!!");
                    CyEdge edge = makeEdge(nodeStuff.getNewNode(oldExtNode), nodeStuff.getNewNode(oTarget));
                    edgeTributes(edge, oldExtNode, oTarget);
>>>>>>> Stashed changes
                } else {
                    targetNodeToEdgeMap.put(oldTargetNode, oldTargetEdges);
                    // oldSources.add(oldSourceNode);       MAYBE NOT NEEDED (are keys of HashMap)
                }
            }
            fullTargetNodeToEdgeMap.put(oldExtNode, targetNodeToEdgeMap);
            // I can make the hashmap easier to understand if i just map to the list of connected nodes and then later
            // use the method to get the connecting edge between the external node and all the nodes in the list.
            // I need oldExternalNode, OldSources, sourceNodeToEdgeMap
            // we give the first (as keys) and last one to the full map
        }
        this.fullTargetNodeToEdgeMap = fullTargetNodeToEdgeMap;
        makeEdgesOut();
    }

<<<<<<< Updated upstream
    private void makeEdgesIn() {
        Set<CyNode> oldExtNodes = fullSourceNodeToEdgeMap.keySet();
        int counterID = 0;
        for (CyNode oldExtNode : oldExtNodes) {
            CyNode newExternalNode =  nodeStuff.getNewNode(oldExtNode);
            HashMap<CyNode, List<CyEdge>> currentNodeToEdgeMap = fullSourceNodeToEdgeMap.get(oldExtNode);
            List<CyNode> oldSources = new ArrayList<>(currentNodeToEdgeMap.keySet());

            for (CyNode oldSource : oldSources) {
                Double current_stoichiometry = 0.0;
                for (CyEdge oldSourceEdge : currentNodeToEdgeMap.get(oldSource)) {
                    Double next_stoichiometry = oldNetwork.getDefaultEdgeTable().getRow(oldSourceEdge.getSUID()).get("stoichiometry", Double.class);
                    if (next_stoichiometry != null) {
                        current_stoichiometry += next_stoichiometry;
=======
    private HashMap<CyNode, List<CyEdge>> mkMapOfOutEdges() {
        // makes Map for outgoing Edges of every Node from the old Network
        HashMap<CyNode, List<CyEdge>> outEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            outEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.OUTGOING));
        }
        return outEdges;
    }

    private HashMap<CyNode, List<CyEdge>> mkMapOfInEdges() {
        // makes Map for incoming Edges of every Node from the old Network
        HashMap<CyNode, List<CyEdge>> inEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            inEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.INCOMING));
        }
        return inEdges;
    }

    private List<CyNode> getAllNeighbors (CyNode oldExtNode, String direction ) {
        // uses every Node with the same name in the old Network to create either a list of outgoing or incoming Node connections
        List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExtNode));
        List<CyNode> oldNeighbors = new ArrayList<>();

        for (CyNode sNode : similarNodes) {
            if (direction.equals("Sources")) {
                for (CyEdge oldEdge : getSourcesOld(sNode)) {
                    oldNeighbors.add(oldEdge.getSource());
                    if (!sourceToTargets.containsKey(sNode)) {
                        sourceToTargets.put(sNode, new HashSet<CyNode>());
>>>>>>> Stashed changes
                    }
                }
                if (externalNodes.contains(oldSource)) {//this never happens in our network
                    String edgeID = Long.toString(oldSource.getSUID()).concat("_".concat(Long.toString(oldExtNode.getSUID())));

                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(oldSource, newExternalNode, true);

                        counterID += 1;
                        String temp = String.format("%07d", counterID).concat("-");
                        String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("name", String.class).concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("name", String.class);
                        String targetName = oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("stoichiometry", current_stoichiometry);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                    } else {
                        System.out.println("DUPLICATE");
                    }
                } else {
                    String exp = "Export-";
                    // HERE I COULD ALSO USE getCyIDFromNode IN THE LEFT BRACKETS
                    CyNode compartmentNode = nodeStuff.getCompNodeFromName(nodeStuff.getCompOfNode(oldSource));
                    // This happens if we look at external compartment nodes
                    if (compartmentNode == null) {
                        //Get the external Compartment name and convert it to the corresponding internal compNode
                        compartmentNode = nodeStuff.getIntCompNodeFromExtNode(oldSource);
                    }

                    if (compartmentNode == null) {//this never happens in our network
                        System.out.println("failure");
                        continue;
                    }
                    //create edgename as concat of node names
                    String edgeID = Long.toString(compartmentNode.getSUID()).concat("_".concat(Long.toString(oldExtNode.getSUID())));
                    //add the edge, only if it does not already exist in our new network
                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(compartmentNode, newExternalNode, true);

                        counterID += 1;
                        String temp = String.format("%07d", counterID).concat("-");
                        String edgeName = exp.concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        String sourceName = nodeStuff.getCompNameFromNode(compartmentNode);
                        String targetName = oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("stoichiometry", current_stoichiometry);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                    } else {
                        System.out.println("DUPlICATTTe!!");
                    }
                }
            }
        }
<<<<<<< Updated upstream
    }

    private void makeEdgesOut() {
        Set<CyNode> oldExtNodes = fullTargetNodeToEdgeMap.keySet();
        int counterID = 0;
        for (CyNode oldExtNode : oldExtNodes) {
            CyNode newExternalNode =  nodeStuff.getNewNode(oldExtNode);
            HashMap<CyNode, List<CyEdge>> currentNodeToEdgeMap = fullTargetNodeToEdgeMap.get(oldExtNode);
            List<CyNode> oldTargets = new ArrayList<>(currentNodeToEdgeMap.keySet());

            for (CyNode oldTarget : oldTargets) {
                Double current_stoichiometry = 0.0;
                for (CyEdge oldTargetEdge : currentNodeToEdgeMap.get(oldTarget)) {
                    Double next_stoichiometry = oldNetwork.getDefaultEdgeTable().getRow(oldTargetEdge.getSUID()).get("stoichiometry", Double.class);
                    if (next_stoichiometry != null) {
                        current_stoichiometry += next_stoichiometry;
                    }
                }
                if (externalNodes.contains(oldTarget)) {//this never happens in our network
                    String edgeID = Long.toString(oldExtNode.getSUID()).concat("_".concat(Long.toString(oldTarget.getSUID())));

                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(newExternalNode, oldTarget,true);

                        counterID += 1;
                        String temp = String.format("%07d", counterID).concat("-");
                        String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class).concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class);
                        String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("stoichiometry", current_stoichiometry);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                    } else {
                        System.out.println("DUPLICATE");
                    }
                } else {
                    String imp = "Import-";
                    // HERE I COULD ALSO USE getCyIDFromNode IN THE LEFT BRACKETS
                    CyNode compartmentNode = nodeStuff.getCompNodeFromName(nodeStuff.getCompOfNode(oldTarget));
                    // This happens if we look at external compartment nodes
                    if (compartmentNode == null) {
                        //Get the external Compartment name and convert it to the corresponding internal compNode
                        compartmentNode = nodeStuff.getIntCompNodeFromExtNode(oldTarget);
                    }

                    if (compartmentNode == null) {//this never happens in our network
                        System.out.println("failure");
                        continue;
                    }
                    //create edgename as concat of node names
                    String edgeID = Long.toString(oldExtNode.getSUID()).concat("_".concat(Long.toString(compartmentNode.getSUID())));
                    //add the edge, only if it does not already exist in our new network
                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(newExternalNode, compartmentNode, true);

                        counterID += 1;
                        String temp = String.format("%07d", counterID).concat("-");
                        String edgeName = imp.concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        String targetName = nodeStuff.getCompNameFromNode(compartmentNode);
                        String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldExtNode.getSUID()).get("name", String.class);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("stoichiometry", current_stoichiometry);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                    } else {
                        System.out.println("dupppppppppplicate");
                    }
                }
            }
=======
        return oldNeighbors;
    }

    private List<CyEdge> getTargetsOld (CyNode cyNode){
        // gives us a list of Edges which are outgoing from a Node
        List<CyEdge> edgeTargets = outgoingEdges.get(cyNode);
        return new ArrayList<>(edgeTargets);
    }

    private List<CyEdge> getSourcesOld (CyNode cyNode){
        // gives us a list of Edges which are incoming from a Node
        List<CyEdge> edgeSources = incomingEdges.get(cyNode);
        return new ArrayList<>(edgeSources);
    }

    private CyEdge makeEdge (CyNode source, CyNode target){
        // makes a Edge from Source to Target and adds an ID to the edgeIDs and returns it (first created if one already exists
        String edgeID = source.getSUID().toString().concat("-".concat(target.getSUID().toString()));
        if(!edgeIDs.contains(edgeID)) {
            edgeIDs.add(edgeID);
            CyEdge newEdge = newNetwork.addEdge(source, target, true);
            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Source", newNetwork.getDefaultNodeTable().getRow(source.getSUID()).get("shared Name", String.class));
            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Target", newNetwork.getDefaultNodeTable().getRow(target.getSUID()).get("shared Name", String.class));
            return newEdge;
        }else {return newNetwork.getConnectingEdgeList(source, target, CyEdge.Type.DIRECTED).get(0);}
    }

    private void edgeTributes(CyEdge currentEdge, CyNode oldSource, CyNode oldTarget){
        // adds all the Attributes of an Edge to it using the information from its Source and Target
        List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
        String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
        String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
        // Getting the correct stoichiometry?
        Double stoichiometry = 0.0;
        for (CyEdge oldEdge : oldEdges) {
            Double currentStoichiometry = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
            if(currentStoichiometry!=null) {stoichiometry += currentStoichiometry;}
        }
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("stoichiometry", stoichiometry);
    }

    private void edgeTributesComp(CyEdge currentEdge, CyNode oldSource, CyNode oldTarget, boolean sourceIsComp){
        // adds all the Attributes of an Edge to it using the information from its Source and Target if Source or Target are a compartment Node
        if (sourceIsComp){
            String sourceName = nodeStuff.getCompNameFromNode(nodeStuff.getIntCompNodeForAnyNode(oldSource));
            String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
            String sharedName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "EXPORT");
        } else {
            String targetName = nodeStuff.getCompNameFromNode(nodeStuff.getIntCompNodeForAnyNode(oldTarget));
            String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
            String sharedName = sourceName.concat(" - Import");
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "IMPORT");
        }
        // Getting the correct stoichiometry?
        List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
        Double stoichiometry = 0.0;
        for (CyEdge oldEdge : oldEdges) {
            Double stoich = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
            if(stoich !=null) {stoichiometry += stoich;}
>>>>>>> Stashed changes
        }
    }
}


