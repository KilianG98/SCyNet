package org.cytoscape.sample.internal;

import jdk.internal.math.FloatingDecimal;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.sql.SQLOutput;
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
    private final HashMap<CyNode, Set<CyNode>> targetToSources = new HashMap<>();
    private HashMap<String, Float> csvMap;

    public CreateEdges(CyNetwork oldNetwork, CyNetwork newNetwork, CreateNodes createNodes, HashMap<String, Float> csvMap) {
        this.edgeIDs = new ArrayList<>();
        this.csvMap = csvMap;
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
        newNetwork.getDefaultEdgeTable().createColumn("flux", Double.class, true);
        newNetwork.getDefaultEdgeTable().createColumn("stoichiometry", Double.class, true);
        makeEdgesToNode();
        makeEdgesFromNode();
    }


    private void makeEdgesToNode() {
        // here we loop through all external Nodes and get their Sources, using these we make edges the external Nodes
        // or compartment Nodes if the Source is in a compartment

        for (CyNode oldExchgNode : createNodes.getExchgNodes()) {
            //I added the exchg Compartemnt Node of the old Network to the list of exchg Nodes, because there are edges going to this Node.
            // right now it is a different Node than the exchg comp Node in the new network, since they are created in different methods
            // I refer to them as EXCHG node and EXCHG-COMP node
            // They should be merged into one Node.
            List<CyNode> oldSources = getAllNeighbors(oldExchgNode, "Sources");

            System.out.println(oldNetwork.getDefaultNodeTable().getRow(oldExchgNode.getSUID()).get("shared name", String.class));
            for (CyNode oSource : oldSources) {

                if (oldExternalNodes.contains(oSource)) {
                    // this happens for the EXCHG node ALL the exchg metabolites are imported to the compartment.
                    // leads to BLACK edges to the EXCHG node
                    System.out.println("externalNode");
                    CyEdge edge = makeEdge(createNodes.getNewNode(oSource), createNodes.getNewNode(oldExchgNode));
                    edgeTributes(edge, oSource, oldExchgNode);
                } else {
                    //all the metabolites are here
                    //their sources are Transport reactions, coming from the organism(BLUE edges from organisms to Metabolites)
                    //But also TP reactions from the exchg comp, those are import reactions from outside the model.
                    //Therefore there are GREEN edges beetween all metabolites and the EXCHG-COMP node
                    System.out.println(oldNetwork.getDefaultNodeTable().getRow(oSource.getSUID()).get("shared name", String.class));
                    System.out.println(oldNetwork.getDefaultNodeTable().getRow(oSource.getSUID()).get("sbml id", String.class));
                    CyNode comp = createNodes.getIntCompNodeForAnyNode(oSource);
                    System.out.println(comp);
                    CyEdge edge = makeEdge(comp, createNodes.getNewNode(oldExchgNode));
                    edgeTributesComp(edge, oSource, oldExchgNode, true);

                }
            }
        }
    }
        private void makeEdgesFromNode () {
            // here we loop through all external Nodes and get their Targets, using these we make edges the external Nodes
            // or compartment Nodes if the Target is in a compartment
            for (CyNode oldExchgNode : createNodes.getExchgNodes()) {
                List<CyNode> oldTargets = getAllNeighbors(oldExchgNode, "Targets");
                for (CyNode oTarget : oldTargets) {

                    System.out.println(oTarget);//this is always 1532516 in the old networK->  EXCHG Node of the old Network
                    CyNode comp = createNodes.getIntCompNodeForAnyNode(oTarget);
                    //Compartment Nodes have no SBML ID, therefore EXCHG-COMP is returned by default
                    // -> BLUE Edges betweeen exchg Comp Node and metabolites
                    System.out.println(createNodes.getCompNameFromNode(comp));
                    System.out.println(oldNetwork.getDefaultNodeTable().getRow(oTarget.getSUID()).get("sbml type", String.class));
                    System.out.println(oldNetwork.getDefaultNodeTable().getRow(oldExchgNode.getSUID()).get("shared name", String.class));
                    CyEdge edge = makeEdge(createNodes.getNewNode(oldExchgNode), comp);
                    edgeTributesComp(edge, oldExchgNode, oTarget, false);
                    //There dont appear to be edges going from The EXCHG node to anywhere(which was the reason to add it in the first place)
                    //There are also no edges going from the exchg-metabolites to anywhere but the EXCHG-Comp Node
                    //There dont seem to be import reactions in either network.

                }
            }
        }


        private HashMap<CyNode, List<CyEdge>> mkMapOfOutEdges () {
            // this method is used to create the Map which maps a Node to its outgoing Edges
            HashMap<CyNode, List<CyEdge>> outEdges = new HashMap<>();
            for (CyNode cyNode : cyNodeList) {
                outEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.OUTGOING));
            }
            return outEdges;
        }

        private HashMap<CyNode, List<CyEdge>> mkMapOfInEdges () {
            // this method is used to create the Map which maps a Node to its incoming Edges
            HashMap<CyNode, List<CyEdge>> inEdges = new HashMap<>();
            for (CyNode cyNode : cyNodeList) {
                inEdges.put(cyNode, oldNetwork.getAdjacentEdgeList(cyNode, CyEdge.Type.INCOMING));
            }
            return inEdges;
        }

        private List<CyNode> getAllNeighbors (CyNode oldExchgNode, String direction ){

            List<CyNode> oldNeighbors = new ArrayList<>();

            if (Objects.equals(direction, "Sources")) {
                List<CyEdge> edges = incomingEdges.get(oldExchgNode);
                for (CyEdge edge : edges) {
                    oldNeighbors.add(edge.getSource());
                }
            } else {
                List<CyEdge> edges = outgoingEdges.get(oldExchgNode);
                for (CyEdge edge : edges) {
                    oldNeighbors.add(edge.getTarget());
                }
            }

            return oldNeighbors;
        }

        private CyEdge makeEdge (CyNode source, CyNode target){
            // here an Edge is created if it does not already exist, which is checked by its individual edgeID
            // otherwise the already created Edge is returned

            String edgeID = source.getSUID().toString().concat("-".concat(target.getSUID().toString()));
            System.out.println(edgeID);
            if (!edgeIDs.contains(edgeID)) {

                edgeIDs.add(edgeID);
                CyEdge newEdge = newNetwork.addEdge(source, target, true);
                newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Source", newNetwork.getDefaultNodeTable().getRow(source.getSUID()).get("shared Name", String.class));
                newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).set("Target", newNetwork.getDefaultNodeTable().getRow(target.getSUID()).get("shared Name", String.class));
                return newEdge;
            } else {
                // I AM NOT SURE WHY THIS IS NEEDED, BUT IT IS NEEDED
                // WHY ARE THERE MULTIPLE TIMES THE SAME EDGE (at least same edgeID) IN THE OLD NETWORK???
                return newNetwork.getConnectingEdgeList(source, target, CyEdge.Type.DIRECTED).get(0);
            }
        }

        private void edgeTributes (CyEdge currentEdge, CyNode oldSource, CyNode oldTarget){
            // here all the attributes of an Edge are added to its entry in the edge-table (external Node to external Node)
            List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
            String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
            String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);

            Double stoichiometry = 0.0;
            for (CyEdge oldEdge : oldEdges) {
                Double stoich = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
                if (stoich != null) {
                    stoichiometry += stoich;
                }
            }
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("stoichiometry", stoichiometry);
        }

        private void edgeTributesComp (CyEdge currentEdge, CyNode oldSource, CyNode oldTarget,boolean sourceIsComp){
            // here all the attributes of an Edge are added to its entry in the edge-table (external Node to comp Node)
            if (sourceIsComp) {
                String sourceName = createNodes.getCompNameFromNode(createNodes.getIntCompNodeForAnyNode(oldSource));
                String targetName = oldNetwork.getDefaultNodeTable().getRow(oldTarget.getSUID()).get("shared name", String.class);
                String sharedName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
                //Float fluxFloatValue = getFlux(oldSource);
                //Double fluxValue = fluxFloatValue.doubleValue();
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "EXPORT");
                //newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("flux", fluxValue);
            } else {
                String targetName = createNodes.getCompNameFromNode(createNodes.getIntCompNodeForAnyNode(oldTarget));
                String sourceName = oldNetwork.getDefaultNodeTable().getRow(oldSource.getSUID()).get("shared name", String.class);
                String sharedName = sourceName.concat(" - Import");
                //Float fluxFloatValue = getFlux(oldTarget);
                //Double fluxValue = fluxFloatValue.doubleValue();
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("source", sourceName);
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("target", targetName);
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared name", sharedName);
                newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("shared interaction", "IMPORT");
                //newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("flux", fluxValue);
            }

            List<CyEdge> oldEdges = oldNetwork.getConnectingEdgeList(oldSource, oldTarget, CyEdge.Type.ANY);
            Double stoichiometry = 0.0;
            for (CyEdge oldEdge : oldEdges) {
                Double stoich = oldNetwork.getDefaultEdgeTable().getRow(oldEdge.getSUID()).get("stoichiometry", Double.class);
                if (stoich != null) {
                    stoichiometry += stoich;
                }
            }
            newNetwork.getDefaultEdgeTable().getRow(currentEdge.getSUID()).set("stoichiometry", stoichiometry);
        }

        private Float getFlux (CyNode oldNode){
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




