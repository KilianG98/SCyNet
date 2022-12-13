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
    private final List<CyNode> externalNodes;
    private  HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullSourceNodeToEdgeMap;
    private  HashMap<CyNode, HashMap<CyNode, List<CyEdge>>> fullTargetNodeToEdgeMap;

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
                }
            }
            fullSourceNodeToEdgeMap.put(oldExternalNode, sourceNodeToEdgeMap);

            // I need oldExternalNode, OldSources, sourceNodeToEdgeMap
            // we give the first (as keys) and last one to the full map
        }
        this.fullSourceNodeToEdgeMap = fullSourceNodeToEdgeMap;
        makeEdgesIn();
    }

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
        }
    }
}


