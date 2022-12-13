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
    private final List<CyNode> oldExternalNodes;
    private List<CyEdge> EdgeIds = new ArrayList<>();
    private HashMap<CyNode, Set<CyNode>> sourceToTargets = new HashMap<>();
    private HashMap <CyNode, Set<CyNode>> targetToSources = new HashMap<>();

    public EdgeStuff(CyNetwork oldNetwork, CyNetwork newNetwork, NodeStuff nodeStuff) {
        this.edgeIDs = new ArrayList<>();
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
        this.nodeStuff = nodeStuff;
        this.cyNodeList = oldNetwork.getNodeList();
        this.oldExternalNodes = nodeStuff.getExtNodes();
        this.outgoingEdges = mkMapOfOutEdges();
        this.incomingEdges = mkMapOfInEdges();
        makeAllEdges();
    }

    private void makeAllEdges() {
        newNetwork.getDefaultEdgeTable().createColumn("source", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("target", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("edgeID", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("stoichiometry", Double.class, true);
        makeEdgesToNode();
        makeEdgesFromNode();
    }

    private void makeEdgesToNode() {
        for (CyNode oldExtNode : oldExternalNodes) {

            List<CyNode> oldSources = getAllNeighbors(oldExtNode, "Sources");
            for (CyNode oSource : oldSources) {
                if (oldExternalNodes.contains(oSource)) {
                    makeEdge(nodeStuff.getNewNode(oSource), nodeStuff.getNewNode(oldExtNode));
                    } else {
                    CyNode comp = nodeStuff.getIntCompNodeForAnyNode(oSource);
                    makeEdge(comp,nodeStuff.getNewNode(oldExtNode));
                }

            }
        }
    }
    private void makeEdgesFromNode(){

        for (CyNode oldExtNode : oldExternalNodes) {
            List<CyNode> oldTargets = getAllNeighbors(oldExtNode, "Targets");

            for (CyNode oTarget : oldTargets) {

                if (oldExternalNodes.contains(oTarget)) {
                    makeEdge(nodeStuff.getNewNode(oldExtNode), nodeStuff.getNewNode(oTarget));
                } else {
                    CyNode comp = nodeStuff.getIntCompNodeForAnyNode(oTarget);
                    CyEdge edge = makeEdge(nodeStuff.getNewNode(oldExtNode), comp);
                    edgeTributes(edge, oldExtNode, oTarget);
                }

            }
        }

    }

    private HashMap<CyNode, List<CyEdge>> mkMapOfOutEdges() {
        HashMap<CyNode, List<CyEdge>> outEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            outEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.OUTGOING));
        }
        return outEdges;
    }
    private HashMap<CyNode, List<CyEdge>> mkMapOfInEdges() {
        HashMap<CyNode, List<CyEdge>> inEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            inEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.INCOMING));
        }
        return inEdges;
    }
    private List<CyNode> getAllNeighbors (CyNode oldExtNode, String direction ) {
        List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExtNode));
        List<CyNode> oldNeighbors = new ArrayList<>();

        for (CyNode sNode : similarNodes) {
            if (direction.equals("Sources")) {
                for (CyEdge oldEdge : getSourcesOld(sNode)) {
                    oldNeighbors.add(oldEdge.getSource());
                    if (!sourceToTargets.containsKey(sNode)) {
                        sourceToTargets.put(sNode, new HashSet<CyNode>());
                    }
                    sourceToTargets.get(sNode).add(oldEdge.getSource());
                }
            } else {
                for (CyEdge oldEdge : getTargetsOld(sNode)) {
                    oldNeighbors.add(oldEdge.getTarget());
                    if (!targetToSources.containsKey(sNode)) {
                        targetToSources.put(sNode, new HashSet<CyNode>());
                    }
                    targetToSources.get(sNode).add(oldEdge.getSource());
                }
            }

        }
        return oldNeighbors;
    }
    private List<CyEdge> getTargetsOld (CyNode cyNode){
        List<CyEdge> edgeTargets = outgoingEdges.get(cyNode);
        return new ArrayList<>(edgeTargets);
    }
    private List<CyEdge> getSourcesOld (CyNode cyNode){
        List<CyEdge> edgeSources = incomingEdges.get(cyNode);

        return new ArrayList<>(edgeSources);
    }
    private CyEdge makeEdge (CyNode source, CyNode target){

        String edgeID = source.getSUID().toString().concat("-".concat(target.getSUID().toString()));

        if(!edgeIDs.contains(edgeID)) {

            edgeIDs.add(edgeID);
            CyEdge newEdge = newNetwork.addEdge(source, target, true);
            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Source", newNetwork.getDefaultNodeTable().getRow(source.getSUID()).get("shared Name", String.class));
            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Target", newNetwork.getDefaultNodeTable().getRow(target.getSUID()).get("shared Name", String.class));
            return newEdge;
        }else {
            CyEdge existingEdge = newNetwork.getConnectingEdgeList(source, target, CyEdge.Type.DIRECTED).get(0);
            return existingEdge;
        }
    }
    private void edgeTributes(CyEdge currentEdge, CyNode oldSource, CyNode oldTarget){

        List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
        String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
        String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
        Double stoichiometry = 0.0;
        for (CyEdge oldEdge : oldEdges) {
            System.out.println(stoichiometry);
            Double stoich = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
            if(stoich !=null){
                stoichiometry += stoich;
            }
        }
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("stoichiometry", stoichiometry);
    }
}


