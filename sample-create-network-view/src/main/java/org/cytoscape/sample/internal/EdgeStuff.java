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
    private final List<String> edgeNames = new ArrayList<>();
    private final List<CyNode> externalNodes;

    public EdgeStuff(CyNetwork oldNetwork, CyNetwork newNetwork, NodeStuff nodeStuff) {
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
        this.cyNodeList = oldNetwork.getNodeList();
        this.nodeStuff = nodeStuff;
        this.externalNodes = nodeStuff.getExternalNodes();
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
        List<CyNode> externalNodes = nodeStuff.getExternalNodes();
        makeEdgesToNode(externalNodes);
        makeEdgesFromNode(externalNodes);
    }
    private void makeEdgesToNode(List<CyNode> oldExtNodes) {
        for (CyNode oldExternalNode : oldExtNodes) {
            CyNode newExternalNode = nodeStuff.getNodeNew(oldExternalNode);
            List<CyNode> oldSources = getSourcesOld(oldExternalNode);
            List<CyNode> similarNodes = nodeStuff.getExternalNodesFromName(nodeStuff.getNodeSharedName(oldExternalNode));

            for (CyNode similarNode : similarNodes) {
                oldSources.addAll(getSourcesOld(similarNode));
            }
            for (CyNode oldSource : oldSources) {

                if (externalNodes.contains(oldSource)) {//this never happens in our network
                    String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("name", String.class).concat(oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class));

                    if (!edgeNames.contains(edgeName)) {
                        edgeNames.add(edgeName);
                        CyEdge newEdge = newNetwork.addEdge(oldSource, newExternalNode, true);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);
                    }
                } else {
                    //get the Node corresponding to the compartment of the old Source
                    CyNode compartmentNode = nodeStuff.getCompNode(nodeStuff.getCompOfNode(oldSource));
                    // This happens if we look at external compartment nodes
                    if (compartmentNode == null) {
                        //Get the external Compartment name and convert it to the corresponding internal comNode
                        compartmentNode = nodeStuff.getInternalCompFromExternalNode(oldSource);
                    }

                    if (compartmentNode == null) {//this never happens in our network
                        System.out.println("failure");
                        continue;
                    }
                    //create edgename as concat of node names
                    String edgeName = nodeStuff.getCompName(compartmentNode).concat(oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class));
                    //add the edge, only if it does not already exist in our new network
                    if (!edgeNames.contains(edgeName)) {
                        edgeNames.add(edgeName);
                        CyEdge newEdge = newNetwork.addEdge(compartmentNode, newExternalNode, true);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);
                    }
                }
            }
        }
    }
    private void makeEdgesFromNode(List<CyNode> oldExternalNodes) {
        for (CyNode oldExternalNode:oldExternalNodes) {

            CyNode newExternalNode = nodeStuff.getNodeNew(oldExternalNode);
            List<CyNode> oldTargets = getTargetsOld(oldExternalNode);
            List<CyNode> similarNodes = nodeStuff.getExternalNodesFromName(nodeStuff.getNodeSharedName(oldExternalNode));

            for (CyNode similarNode : similarNodes) {
                oldTargets.addAll(getTargetsOld(similarNode));
            }

            for (CyNode oldTarget : oldTargets) {

                if (externalNodes.contains(oldTarget)) {//this never happens
                    String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class).concat(oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class));

                    if (!edgeNames.contains(edgeName)) {
                        edgeNames.add(edgeName);
                        CyEdge newEdge = newNetwork.addEdge(newExternalNode, oldTarget, true);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);
                    } else {
                        System.out.println("duplicate");
                    }

                } else {
                    CyNode compartmentNode = nodeStuff.getCompNode(nodeStuff.getCompOfNode(oldTarget));
                    // This happens if we look at external compartment nodes
                    if (compartmentNode == null) {
                        //Get the external Compartment name and convert it to the corresponding internal comNode
                        compartmentNode = nodeStuff.getInternalCompFromExternalNode(oldTarget);
                    }
                    if (compartmentNode == null) {//this never happens
                        System.out.println("failure");
                        continue;
                    }

                    String edgeName = (oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class)).concat(nodeStuff.getCompName(compartmentNode));
                    //add the edge only if its not already part of the new network
                    if (!edgeNames.contains(edgeName)) {
                        edgeNames.add(edgeName);
                        CyEdge newEdge = newNetwork.addEdge(newExternalNode, compartmentNode, true);
                        newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);
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


