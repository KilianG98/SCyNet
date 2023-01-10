package org.cytoscape.sample.internal;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import javax.swing.*;
import java.util.HashMap;

public class CreateNetworkViewTaskFactory extends AbstractTaskFactory {

	private final CyNetworkFactory cnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkNaming cyNetworkNaming;
	private final DataSourceManager dataSourceManager;
	private final CyNetwork currentNetwork;

	public CreateNetworkViewTaskFactory(CyNetworkNaming cyNetworkNaming, CyNetworkFactory cnf, CyNetworkManager networkManager, CyNetworkViewFactory cnvf,
										final CyNetworkViewManager networkViewManager, DataSourceManager dataSourceManager, CyNetwork currentNetwork){

		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.networkManager = networkManager;
		this.cyNetworkNaming = cyNetworkNaming;
		this.dataSourceManager = dataSourceManager;
		this.currentNetwork = currentNetwork;
	}

	public TaskIterator createTaskIterator(){
		FileChoosing newChooser = new FileChoosing();
		System.out.println(newChooser.giveFile().getName());
		HashMap<String, Float> csvMap = newChooser.makeMap();

		return new TaskIterator(new CreateNetworkViewTask(cyNetworkNaming, cnf,networkManager, cnvf, networkViewManager, dataSourceManager, currentNetwork, csvMap));
	}
}