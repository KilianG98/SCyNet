package org.cytoscape.sample.internal;


import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import java.util.*;


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

	public boolean check_external(String compartment) {
		if(compartment.length() > 2) {
			return compartment.charAt(2) == 'e';
		}
		return false;
	}

	public void from_old_to_new(CyNode old_node, CyNetwork old_network, CyNetwork new_network) {
		CyNode new_node = new_network.addNode();
		String name = old_network.getDefaultNodeTable().getRow(old_node.getSUID()).get("name", String.class);
		new_network.getDefaultNodeTable().getRow(new_node.getSUID()).set("name", name);
		// System.out.println(name);
	}

	public HashMap<CyNode, List<CyNode>> node_suid_edge_map(CyNetwork old_network) {
		HashMap<CyNode, List<CyNode>> edge_map = new HashMap<CyNode, List<CyNode>>();
		List<CyEdge> old_edge_list = old_network.getEdgeList();

		for (CyEdge current_edge : old_edge_list) {	// THIS IS TO FIND THE and/or FROM ON SIDE
			CyNode first_node = current_edge.getSource();
			CyNode key_node = current_edge.getTarget();
			List<CyNode> value_node_list = new ArrayList<CyNode>();

			for (CyEdge next_edge : old_edge_list) { // AND FROM THE OTHER SIDE
				CyNode second_node = next_edge.getTarget();
				if (Objects.equals(first_node, second_node)) {
					//MAKE CONNECTION!
					value_node_list.add(next_edge.getSource());
				}

			edge_map.put(key_node, value_node_list);
			}
		}
		return edge_map;
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
			newNetwork.getDefaultNetworkTable().getRow(newNetwork.getSUID()).set("name", cyNetworkNaming.getSuggestedNetworkTitle("Newly created Network-view"));
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