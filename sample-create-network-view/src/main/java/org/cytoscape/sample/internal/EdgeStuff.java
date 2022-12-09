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

    public EdgeStuff(CyNetwork oldNetwork, CyNetwork newNetwork, NodeStuff nodeStuff) {
        this.edgeIDs = new ArrayList<>();
        this.newNetwork = newNetwork;
        this.oldNetwork = oldNetwork;
        this.nodeStuff = nodeStuff;
        this.cyNodeList = oldNetwork.getNodeList();
        this.oldExternalNodes = nodeStuff.getExtNodes();
        this.outgoingEdges = mkOutEdges();
        this.incomingEdges = mkInEdges();
        makeAllEdges();
    }

    private void makeAllEdges() {
        newNetwork.getDefaultEdgeTable().createColumn("source", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("target", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("edgeID", String.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("stoichiometry", Double.class, true);
        makeEdgesToNode();
        //makeEdgesFromNode();
    }

    private void makeEdgesToNode() {
        //oldExternalNodes only contains Metabolite nodes!!
        for (CyNode oldExtNode : oldExternalNodes) {
            //get all similar Nodes
            //get All Sources for all Nodes -> create function for this, which accepts direction as well, so it can be reused for targets
            //loop through all sources and get their new Nodes/compartments
            //make corresponding edges, once again in separate, reusable function

            List<CyNode> oldSources = getAllNeighbors(oldExtNode, "Sources");

            List<CyNode> oldTargets = getAllNeighbors(oldExtNode, "Targets");

            for (CyNode oSource : oldSources) {
                String edgeID;
                if (oldExternalNodes.contains(oSource)) {
                    edgeID = oSource.getSUID().toString().concat("-".concat(oldExtNode.getSUID().toString()));
                    if (!edgeIDs.contains(edgeID)) {
                        edgeIDs.add(edgeID);
                        makeEdge(nodeStuff.getNewNode(oSource), nodeStuff.getNewNode(oldExtNode));
                    }
                } else {
                    CyNode comp = nodeStuff.getIntCompNodeForAnyNode(oSource);
                }
            }
        }
    }
        /*private void makeEdgesFromNode() {
            int counterID = 0;
            for (CyNode oldExternalNode : oldExternalNodes) {
                CyNode newExternalNode = nodeStuff.getNewNode(oldExternalNode);
                List<CyNode> oldTargets = new ArrayList<>();
                HashMap<CyNode, CyEdge> targetMap = new HashMap<>();

                List<CyEdge> oldEdges = getTargetsOld(oldExternalNode);
                List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExternalNode));
                for (CyNode similarNode : similarNodes) {
                    oldEdges.addAll(getTargetsOld(similarNode));
                }
                for (CyEdge oldEdge : oldEdges) {
                    targetMap.put(oldEdge.getTarget(), oldEdge);
                    oldTargets.add(oldEdge.getTarget());
                }

            }
                for (CyNode oldTarget : oldTargets) {
                    Double stoich = oldNetwork.getDefaultEdgeTable().getRow(targetMap.get(oldTarget).getSUID()).get("stoichiometry", Double.class);
                    if (this.oldExternalNodes.contains(oldTarget)) {//this happens
                        String edgeID = Long.toString(oldExternalNode.getSUID()).concat("_".concat(Long.toString(oldTarget.getSUID())));

                        if (!edgeIDs.contains(edgeID)) {
                            edgeIDs.add(edgeID);
                            CyEdge newEdge = newNetwork.addEdge(newExternalNode, oldTarget, true);

                            counterID += 1;
                            String temp = String.format("%07d", counterID).concat("-");
                            String edgeName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class).concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class)));
                            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                            String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class);
                            String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("name", String.class);

                            if (stoich != null) {
                                newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("stoichiometry", stoich);
                            }
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

                        //add the edge only if it is not already part of the new network
                        if (!edgeIDs.contains(edgeID)) {
                            edgeIDs.add(edgeID);
                            CyEdge newEdge = newNetwork.addEdge(newExternalNode, compartmentNode, true);

                            counterID += 1;
                            String temp = String.format("%07d", counterID).concat("-");
                            String edgeName = imp.concat(temp.concat(oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class)));
                            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("shared name", edgeName);

                            String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldExternalNode.getSUID()).get("name", String.class);
                            String targetName = nodeStuff.getCompNameFromNode(compartmentNode);

                            if (stoich != null) {
                                newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("stoichiometry", stoich);
                            }
                            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("edgeID", edgeID);
                            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("source", sourceName);
                            newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("target", targetName);
                        } else {
                            System.out.println("duplicate");
                        }

                    }
                }
            }
        }*/
        private HashMap<CyNode, List<CyEdge>> mkOutEdges () {
            HashMap<CyNode, List<CyEdge>> outEdges = new HashMap<>();
            for (CyNode cyNode : cyNodeList) {
                outEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.OUTGOING));
            }
            return outEdges;
        }
        private HashMap<CyNode, List<CyEdge>> mkInEdges () {
            HashMap<CyNode, List<CyEdge>> inEdges = new HashMap<>();
            for (CyNode cyNode : cyNodeList) {
                inEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.INCOMING));
            }
            return inEdges;
        }
        private List<CyNode> getAllNeighbors (CyNode oldExtNode, String direction ){
            List<CyNode> similarNodes = nodeStuff.getExtNodesFromName(nodeStuff.getNodeSharedName(oldExtNode));
            List<CyNode> oldNeighbors = new ArrayList<>();

            for (CyNode sNode : similarNodes) {
                if (direction.equals("Sources")) {
                    for (CyEdge oldEdge : getSourcesOld(sNode))
                        oldNeighbors.add(oldEdge.getSource());
                } else {
                    for (CyEdge oldEdge : getTargetsOld(sNode))
                        oldNeighbors.add(oldEdge.getTarget());
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
        private void makeEdge (CyNode source, CyNode target){

            CyEdge newEdge = newNetwork.addEdge(source, target, true);

        }
    }


