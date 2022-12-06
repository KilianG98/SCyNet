package org.cytoscape.sample.internal;


import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.*;
import java.util.List;


public class CreateNetworkViewTask extends AbstractTask {


	private NodeStuff nodeStuff;
	private final CyNetworkFactory cnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkNaming cyNetworkNaming;
	private final DataSourceManager dataSourceManager;

	public CreateNetworkViewTask(CyNetworkNaming cyNetworkNaming, CyNetworkFactory cnf, CyNetworkManager networkManager,
								 CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager, final DataSourceManager dataSourceManager) {
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.networkManager = networkManager;
		this.cyNetworkNaming = cyNetworkNaming;
		this.dataSourceManager = dataSourceManager;
	}

	public void run(TaskMonitor monitor) {
		// I look at existing networks before creating my own network
		Set<CyNetwork> set_of_networks = networkManager.getNetworkSet();

		int network_number = 1;

		for (CyNetwork original_network : set_of_networks) {
			System.out.println("Network: ".concat(Integer.toString(network_number)));
			network_number += 1;

			// HERE I CREATE THE NEW NETWORK WHICH WE FILL WITH NEW STUFF
			CyNetwork newNetwork = this.cnf.createNetwork();

			// My Code goes here
			NodeStuff nodeStuff = new NodeStuff(original_network, newNetwork);
			EdgeStuff edgeStuff = new EdgeStuff(original_network, newNetwork, nodeStuff);

			// Here I add a name to my Network
			newNetwork.getDefaultNetworkTable().getRow(newNetwork.getSUID()).set("name", cyNetworkNaming.getSuggestedNetworkTitle("Simplified Network-view"));
			this.networkManager.addNetwork(newNetwork);

			final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(newNetwork);
			CyNetworkView myView = null;
			if (views.size() != 0)
				myView = views.iterator().next();

			if (myView == null) {
				// create a new view for my network
				myView = cnvf.createNetworkView(newNetwork);
				networkViewManager.addNetworkView(myView);
			} else {
				System.out.println("networkView already existed.");
			}
			// Here I change the color/size etc. of the Nodes
			List<String> compList = nodeStuff.getIntComps();

			for (String compartment: compList) {
				View<CyNode> nodeView = myView.getNodeView(nodeStuff.getCompNodeFromName(compartment));
				double nodeSize = nodeView.getVisualProperty(BasicVisualLexicon.NODE_SIZE) + 100;
				Paint nodeColor = new ColorUIResource(Color.blue);
				//nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH, lineWidth);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, nodeColor);
				nodeView.setLockedValue(BasicVisualLexicon.NODE_SIZE, nodeSize);
			}

			// Set the variable destroyView to true, the following snippet of code
			// will destroy a view
			boolean destroyView = false;
			if (destroyView) {
				networkViewManager.destroyNetworkView(myView);
			}

			// I ADDED THIS SO THE ALGORITHM JUST GOES OVER THE FIRST NETWORK
			break;

		}


	}
}