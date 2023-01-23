package org.cytoscape.sample.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.abs;

public class Aesthetics {

    private final CreateNodes nodes;
    private final CyNetwork newNetwork;
    private final CyNetworkView newView;
    private final List<String> compList;

    public Aesthetics(CreateNodes nodes, CyNetwork newNetwork, CyNetworkView newView) {
        this.nodes = nodes;
        this.newNetwork = newNetwork;
        this.newView = newView;
        this.compList = nodes.getIntComps();
        compNodes();
        exchgNodes();
        edges();
        newView.updateView();
    }

    private void compNodes() {
        // Here we change the appearance of the Compartment Nodes
        ListIterator<String> compListIterator = compList.listIterator();
        Color[] colors = getColors(compList.size());
        while (compListIterator.hasNext()) {
            String compartment = compListIterator.next();
            int idx = compListIterator.nextIndex();
            View<CyNode> compNodeView = newView.getNodeView(nodes.getCompNodeFromName(compartment));
            if (compNodeView == null) {continue;}

            String compNodeName = newNetwork.getDefaultNodeTable().getRow(nodes.getCompNodeFromName(compartment).getSUID()).get("shared name", String.class);
            double compNodeSize = compNodeView.getVisualProperty(BasicVisualLexicon.NODE_SIZE) + 100;
            Paint compNodeColor = new ColorUIResource(colors[idx-1]);

            compNodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, compNodeColor);
            compNodeView.setLockedValue(BasicVisualLexicon.NODE_SIZE, compNodeSize);
            compNodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL, compNodeName);
        }
    }

    private void exchgNodes() {
        // Here we change the appearance of the external Nodes
        List<CyNode> extNodesList = nodes.getExchgNodes();
        for (CyNode exchgNode : extNodesList) {
            View<CyNode> exchgNodeView = newView.getNodeView(nodes.getNewNode(exchgNode));
            if (exchgNodeView == null) {continue;}

            String exchgNodeName = newNetwork.getDefaultNodeTable().getRow(nodes.getNewNode(exchgNode).getSUID()).get("shared name", String.class);
            Integer size = exchgNodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);

            exchgNodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, size/2);
            exchgNodeView.setLockedValue(BasicVisualLexicon.NODE_LABEL, exchgNodeName);
        }
    }

    private void edges() {
        // Here we change the appearance of the Edges
        for (CyEdge newEdge: newNetwork.getEdgeList()){
            String edgeSourceName = newNetwork.getDefaultNodeTable().getRow(newEdge.getSource().getSUID()).get("shared name", String.class);
            String edgeTargetName = newNetwork.getDefaultNodeTable().getRow(newEdge.getTarget().getSUID()).get("shared name", String.class);
            Double edgeFlux = newNetwork.getDefaultEdgeTable().getRow(newEdge.getSUID()).get("flux", Double.class);
            View<CyEdge> edgeView = newView.getEdgeView(newEdge);
            // If Fluxes were added or not
            if (edgeFlux != null) {
                // Color of the Edges is selected based on Fluxes
                Paint edgeColor;
                if (edgeFlux >= 0.0d) {edgeColor = new ColorUIResource(Color.blue);}
                else {edgeColor = new ColorUIResource(Color.green);}
                edgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, edgeColor);

                // Transparency and Width of the Edges is also based on Fluxes
                if (edgeFlux != 0.0d) {
                    // Transparency is removed because very small fluxes are almost not visible
                    // edgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, (abs(edgeFlux.intValue()) + 2) * 50);
                    edgeView.setLockedValue(BasicVisualLexicon.EDGE_WIDTH, abs(edgeFlux)+1);
                } else {edgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 0);}
            } else {
                // Otherwise we just chose the Color based on their direction
                if (compList.contains(edgeSourceName)){
                    Paint edgeColor = new ColorUIResource(Color.blue);
                    edgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, edgeColor);
                }
                if (compList.contains(edgeTargetName)) {
                    Paint edgeColor = new ColorUIResource(Color.green);
                    edgeView.setLockedValue(BasicVisualLexicon.EDGE_PAINT, edgeColor);
                }
            }
        }
    }

    public static Color[] getColors(int n) {
        Color[] colors = new Color[n];
        for (int i = 0; i < n; i++) {
            float hue = (float) i / n;
            colors[i] = Color.getHSBColor(hue, 1.0f, 1.0f);
        }
        return colors;
    }


}
