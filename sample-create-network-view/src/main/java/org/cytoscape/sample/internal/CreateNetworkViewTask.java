package org.cytoscape.sample.internal;


import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.io.*;
import java.util.*;


public class CreateNetworkViewTask extends AbstractTask {

	private final CyNetworkFactory cnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkNaming cyNetworkNaming;
	private final DataSourceManager dataSourceManager;
	private final CyNetwork currentNetwork;
	private final HashMap<String, Double> csvMap;

	public CreateNetworkViewTask(CyNetworkNaming cyNetworkNaming, CyNetworkFactory cnf, CyNetworkManager networkManager,
								 CyNetworkViewFactory cnvf, final CyNetworkViewManager networkViewManager,
								 final DataSourceManager dataSourceManager, CyNetwork currentNetwork, HashMap<String, Double> csvMap) {
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.networkManager = networkManager;
		this.cyNetworkNaming = cyNetworkNaming;
		this.dataSourceManager = dataSourceManager;
		this.currentNetwork = currentNetwork;
		this.csvMap = csvMap;
	}

	public void run(TaskMonitor monitor) throws FileNotFoundException {
		// HERE I CREATE THE NEW NETWORK WHICH WE FILL WITH NEW STUFF
		CyNetwork newNetwork = this.cnf.createNetwork();

		// My Code goes here
		CreateNodes createNodes = new CreateNodes(currentNetwork, newNetwork);
		CreateEdges createEdges = new CreateEdges(currentNetwork, newNetwork, createNodes, csvMap);

		// Here I add a name to my Network
		newNetwork.getDefaultNetworkTable().getRow(newNetwork.getSUID()).set("name", cyNetworkNaming.getSuggestedNetworkTitle("Simplified Network-view"));
		this.networkManager.addNetwork(newNetwork);

		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(newNetwork);
		CyNetworkView myView = null;
		if (views.size() != 0)
			myView = views.iterator().next();

		if (myView == null) {
			// create a new View for my network
			myView = cnvf.createNetworkView(newNetwork);
			networkViewManager.addNetworkView(myView);
		} else {
			System.out.println("This Network View already existed.");
		}
		// Here the color/size/label etc. of the Nodes and Edges is changed
		Aesthetics aesthetics = new Aesthetics(createNodes, createEdges.getFLuxMap(), newNetwork, myView);
	}
}