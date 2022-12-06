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
        makeEdgesToNode(externalNodes);
        makeEdgesFromNode(externalNodes);
    }
    private void makeEdgesToNode(List<CyNode> oldExtNodes) {
        int counterID = 0;
        for (CyNode oldExternalNode : oldExtNodes) {
            CyNode newExternalNode = nodeStuff.getNewNode(oldExternalNode);
            List<CyNode> oldSources = getSourcesOld(oldExternalNode);
            List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExternalNode));

            for (CyNode similarNode : similarNodes) {
                oldSources.addAll(getSourcesOld(similarNode));
            }
            for (CyNode oldSource : oldSources) {
                //String temp = Integer.toString(counterID).concat("-");
                if (externalNodes.contains(oldSource)) {//this never happens in our network
                    String edgeID = Long.toString(oldSource.getSUID()).concat("_".concat(Long.toString(oldExternalNode.getSUID())));
                    String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("name", String.class);
                    String targetName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class);

                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(oldSource, newExternalNode, true);

                        counterID += 1;
                        String temp = String.format("%07d" , counterID).concat("-");
                        String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("name", String.class).concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

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
                    String edgeID = Long.toString(compartmentNode.getSUID()).concat("_".concat(Long.toString(oldExternalNode.getSUID())));
                    String sourceName = nodeStuff.getCompNameFromNode(compartmentNode);
                    String targetName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class);
                    //add the edge, only if it does not already exist in our new network
                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(compartmentNode, newExternalNode, true);

                        counterID += 1;
                        String temp = String.format("%07d" , counterID).concat("-");
                        String edgeName = exp.concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                    }
                }
            }
        }
    }
    private void makeEdgesFromNode(List<CyNode> oldExternalNodes) {
        int counterID = 0;
        for (CyNode oldExternalNode:oldExternalNodes) {

            CyNode newExternalNode = nodeStuff.getNewNode(oldExternalNode);
            List<CyNode> oldTargets = getTargetsOld(oldExternalNode);
            List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExternalNode));

            for (CyNode similarNode : similarNodes) {
                oldTargets.addAll(getTargetsOld(similarNode));
            }
            for (CyNode oldTarget : oldTargets) {
                if (externalNodes.contains(oldTarget)) {//this happens
                    String edgeID = Long.toString(oldExternalNode.getSUID()).concat("_".concat(Long.toString(oldTarget.getSUID())));
                    String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class);
                    String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class);

                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(newExternalNode, oldTarget, true);

                        counterID += 1;
                        String temp = String.format("%07d" , counterID).concat("-");
                        String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class).concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                    } else {
                        System.out.println("duplicate");
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
                    if (compartmentNode == null) {//this never happens
                        System.out.println("failure");
                        continue;
                    }
                    String edgeID = Long.toString(oldExternalNode.getSUID()).concat("_".concat(Long.toString(compartmentNode.getSUID())));
                    String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class);
                    String targetName = nodeStuff.getCompNameFromNode(compartmentNode);

                    //add the edge only if its not already part of the new network
                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        CyEdge newEdge = newNetwork.addEdge(newExternalNode, compartmentNode, true);

                        counterID += 1;
                        String temp = String.format("%07d" , counterID).concat("-");
                        String edgeName = imp.concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class)));
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                    } else {
                        System.out.println("duplicate");
                    }

                }
            }
        }
    }
    private List<CyNode> getTargetsOld(CyNode cyNode) {
        List<CyNode> adjacencyList = new ArrayList<>();
        List<CyEdge> edgeTargets = outgoingEdges.get(cyNode);
        for (CyEdge edgeTarget : edgeTargets) {
            adjacencyList.add(edgeTarget.getTarget());
        }
        return adjacencyList;
    }
    private List<CyNode> getSourcesOld(CyNode cyNode) {
        List<CyNode> adjacencyList = new ArrayList<>();
        List<CyEdge> edgeSources = incomingEdges.get(cyNode);
        for (CyEdge edgeSource : edgeSources) {
            adjacencyList.add(edgeSource.getSource());
        }
        return adjacencyList;
    }
}


