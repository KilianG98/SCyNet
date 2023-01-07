package org.cytoscape.sample.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import java.util.*;

public class CreateEdges {
    private final List<CyNode> cyNodeList;
    private final CyNetwork oldNetwork;
    private final CyNetwork newNetwork;
    private final CreateNodes createNodes;
    private final HashMap<CyNode, List<CyEdge>> outgoingEdges;
    private final HashMap<CyNode, List<CyEdge>> incomingEdges;
    private final List<String> edgeIDs;
    private final List<CyNode> oldExternalNodes;
    private final HashMap<CyNode, Set<CyNode>> sourceToTargets = new HashMap<>();
    private final HashMap <CyNode, Set<CyNode>> targetToSources = new HashMap<>();

    public CreateEdges(CyNetwork oldNetwork, CyNetwork newNetwork, CreateNodes createNodes) {
        this.edgeIDs = new ArrayList<>();
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
        this.createNodes = createNodes;
        this.cyNodeList = oldNetwork.getNodeList();
        this.oldExternalNodes = createNodes.getExtNodes();
        this.outgoingEdges = mkMapOfOutEdges();
        this.incomingEdges = mkMapOfInEdges();
        makeAllEdges();
    }

    private void makeAllEdges() {
        // here we add the columns needed in the edge-table and then we create all the edges
        newNetwork.getDefaultEdgeTable().createColumn("source", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("target", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("edgeID", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("stoichiometry", Double.class, true);
        makeEdgesToNode();
        makeEdgesFromNode();
    }

    private void makeEdgesToNode() {
        // here we loop through all external Nodes and get their Sources, using these we make edges the external Nodes
        // or compartment Nodes if the Source is in a compartment
        for (CyNode oldExtNode : oldExternalNodes) {
            List<CyNode> oldSources = getAllNeighbors(oldExtNode, "Sources");
            for (CyNode oSource : oldSources) {
                if (oldExternalNodes.contains(oSource)) {
                    CyEdge edge = makeEdge(createNodes.getNewNode(oSource), createNodes.getNewNode(oldExtNode));
                    edgeTributes(edge, oSource, oldExtNode);
                    } else {
                    CyNode comp = createNodes.getIntCompNodeForAnyNode(oSource);
                    CyEdge edge = makeEdge(comp, createNodes.getNewNode(oldExtNode));
                    edgeTributesComp(edge, oSource, oldExtNode, true);
                }
            }
        }
    }

    private void makeEdgesFromNode(){
        // here we loop through all external Nodes and get their Targets, using these we make edges the external Nodes
        // or compartment Nodes if the Target is in a compartment
        for (CyNode oldExtNode : oldExternalNodes) {
            List<CyNode> oldTargets = getAllNeighbors(oldExtNode, "Targets");
            for (CyNode oTarget : oldTargets) {
                if (oldExternalNodes.contains(oTarget)) {
                    CyEdge edge = makeEdge(createNodes.getNewNode(oldExtNode), createNodes.getNewNode(oTarget));
                    edgeTributes(edge,oldExtNode, oTarget);
                } else {
                    CyNode comp = createNodes.getIntCompNodeForAnyNode(oTarget);
                    CyEdge edge = makeEdge(createNodes.getNewNode(oldExtNode), comp);
                    edgeTributesComp(edge, oldExtNode, oTarget,false);
                }
            }
        }
    }

    private HashMap<CyNode, List<CyEdge>> mkMapOfOutEdges() {
        // this method is used to create the Map which maps a Node to its outgoing Edges
        HashMap<CyNode, List<CyEdge>> outEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            outEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.OUTGOING));
        }
        return outEdges;
    }

    private HashMap<CyNode, List<CyEdge>> mkMapOfInEdges() {
        // this method is used to create the Map which maps a Node to its incoming Edges
        HashMap<CyNode, List<CyEdge>> inEdges = new HashMap<>();
        for (CyNode cyNode : cyNodeList) {
            inEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.INCOMING));
        }
        return inEdges;
    }

    private List<CyNode> getAllNeighbors (CyNode oldExtNode, String direction ) {
        // here we get all neighbors of a Node, by looking at all the Sources/Targets of all the Nodes with the same name
        List<CyNode> similarNodes = createNodes.getExtNodesFromName(createNodes.getNodeSharedName(oldExtNode));
        List<CyNode> oldNeighbors = new ArrayList<>();

        for (CyNode sNode : similarNodes) {
            if (direction.equals("Sources")) {
                for (CyEdge oldEdge : incomingEdges.get(sNode)) {
                    oldNeighbors.add(oldEdge.getSource());
                    if (!sourceToTargets.containsKey(sNode)) {
                        sourceToTargets.put(sNode, new HashSet<CyNode>());
                    }
                    sourceToTargets.get(sNode).add(oldEdge.getSource());
                }
            } else {
                for (CyEdge oldEdge : outgoingEdges.get(sNode)) {
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

    private CyEdge makeEdge (CyNode source, CyNode target){
        // here an Edge is created if it does not already exist, which is checked by its individual edgeID
        // otherwise the already created Edge is returned
        String edgeID = source.getSUID().toString().concat("-".concat(target.getSUID().toString()));

        if(!edgeIDs.contains(edgeID)) {

            edgeIDs.add(edgeID);
            CyEdge newEdge = newNetwork.addEdge(source, target, true);
            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Source", newNetwork.getDefaultNodeTable().getRow(source.getSUID()).get("shared Name", String.class));
            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Target", newNetwork.getDefaultNodeTable().getRow(target.getSUID()).get("shared Name", String.class));
            return newEdge;
        }else {
            // I AM NOT SURE WHY THIS IS NEEDED, BUT IT IS NEEDED
            // WHY ARE THERE MULTIPLE TIMES THE SAME EDGE (at least same edgeID) IN THE OLD NETWORK???
            return newNetwork.getConnectingEdgeList(source, target, CyEdge.Type.DIRECTED).get(0);
        }
    }

    private void edgeTributes(CyEdge currentEdge, CyNode oldSource, CyNode oldTarget){
        // here all the attributes of an Edge are added to its entry in the edge-table (external Node to external Node)
        List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
        String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
        String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);

        Double stoichiometry = 0.0;
        for (CyEdge oldEdge : oldEdges) {
            Double stoich = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
            if(stoich !=null){
                stoichiometry += stoich;
            }
        }
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("stoichiometry", stoichiometry);
    }

    private void edgeTributesComp(CyEdge currentEdge, CyNode oldSource, CyNode oldTarget, boolean sourceIsComp){
        // here all the attributes of an Edge are added to its entry in the edge-table (external Node to comp Node)
        if (sourceIsComp){
            String sourceName = createNodes.getCompNameFromNode(createNodes.getIntCompNodeForAnyNode(oldSource));
            String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
            String sharedName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "EXPORT");
        } else {
            String targetName = createNodes.getCompNameFromNode(createNodes.getIntCompNodeForAnyNode(oldTarget));
            String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
            String sharedName = sourceName.concat(" - Import");
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "IMPORT");
        }

        List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
        Double stoichiometry = 0.0;
        for (CyEdge oldEdge : oldEdges) {
            Double stoich = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
            if(stoich !=null){
                stoichiometry += stoich;
            }
        }
        newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("stoichiometry", stoichiometry);
    }
}


