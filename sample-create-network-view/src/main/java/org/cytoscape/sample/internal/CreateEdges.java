package org.cytoscape.sample.internal;

import jdk.internal.math.FloatingDecimal;
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
    private final List<CyNode> oldExchangeNodes;
    private final HashMap<CyNode, Set<CyNode>> sourceToTargets = new HashMap<>();
    private final HashMap <CyNode, Set<CyNode>> targetToSources = new HashMap<>();
    private  HashMap<String, Float> csvMap;

    public CreateEdges(CyNetwork oldNetwork, CyNetwork newNetwork, CreateNodes createNodes, HashMap<String, Float> csvMap) {
        this.edgeIDs = new ArrayList<>();
        this.csvMap = csvMap;
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
        this.createNodes = createNodes;
        this.cyNodeList = oldNetwork.getNodeList();
        this.oldExternalNodes = createNodes.getExtNodes();
        this.oldExchangeNodes = createNodes.getExchgNodes();
        makeAllEdges();
    }

    private void makeAllEdges() {
        // here we add the columns needed in the edge-table and then we create all the edges
        newNetwork.getDefaultEdgeTable().createColumn("source", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("target", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("edgeID", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("flux", Double.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("stoichiometry", Double.class, true);
        //makeEdgesToNode();
        //makeEdgesFromNode();
        newMakeEdgesFromNode();
    }

    private void newMakeEdgesFromNode() {
        for (CyNode exchgNode : oldExchangeNodes) {
            List<CyEdge> outgoingEdges = oldNetwork.getAdjacentEdgeList(exchgNode, CyEdge.Type.OUTGOING);
            List<CyEdge> incomingEdges = oldNetwork.getAdjacentEdgeList(exchgNode, CyEdge.Type.INCOMING);
            for (CyEdge outgoingEdge : outgoingEdges) {
                CyNode targetNode = outgoingEdge.getTarget();
                CyNode newTargetNode = createNodes.getIntCompNodeForAnyNode(targetNode);
                CyEdge newEdge = newMakeEdge(createNodes.getNewNode(exchgNode), newTargetNode);
            }
            for (CyEdge incomingEdge : incomingEdges) {
                CyNode sourceNode = incomingEdge.getSource();
                CyNode newSourceNode = createNodes.getIntCompNodeForAnyNode(sourceNode);
                System.out.println("NEW SOURCE");
                System.out.println(newSourceNode);
                if (newSourceNode == null) {
                    newSourceNode = createNodes.getCompNodeFromName("exchg");
                }
                CyEdge newEdge = newMakeEdge(newSourceNode, createNodes.getNewNode(exchgNode));
                edgeTributesComp(newEdge, sourceNode, exchgNode, true);
                edgeTributes(newEdge, sourceNode, exchgNode);
            }

        }
    }

    private CyEdge newMakeEdge(CyNode source, CyNode target) {
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
            Float fluxFloatValue = getFlux(oldSource);
            Double fluxValue = fluxFloatValue.doubleValue();
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "EXPORT");
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("flux", fluxValue);
        } else {
            String targetName = createNodes.getCompNameFromNode(createNodes.getIntCompNodeForAnyNode(oldTarget));
            String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
            String sharedName = sourceName.concat(" - Import");
            Float fluxFloatValue = getFlux(oldTarget);
            Double fluxValue = fluxFloatValue.doubleValue();
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "IMPORT");
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("flux", fluxValue);
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

    private Float getFlux(CyNode oldNode) {
        String oldType = oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("sbml type", String.class);
        String oldID = oldNetwork.getDefaultNodeTable().getRow(oldNode.getSUID()).get("sbml id", String.class);
        String[] splitID = oldID.split("_", 0);
        if (Objects.equals(oldType, "reaction") && Objects.equals(splitID[0], "R")) {
            String key = "";
            if (splitID[2].length() == 2) {
                key = splitID[1].concat(splitID[3]);
            } else {
                key = splitID[1].concat(splitID[2]);
            }
            return csvMap.get(key);
        }
        return 0.0f;
    }
}


