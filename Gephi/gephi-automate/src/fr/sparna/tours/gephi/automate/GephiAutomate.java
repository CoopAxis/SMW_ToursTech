package fr.sparna.tours.gephi.automate;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.ImportController;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.labelAdjust.LabelAdjust;
import org.gephi.layout.plugin.labelAdjust.LabelAdjustBuilder;
import org.gephi.layout.plugin.scale.Expand;
import org.gephi.layout.plugin.scale.ScaleLayout;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.EdgeColorTransformer;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

import fr.inria.edelweiss.semantic.importer.SemanticWebImportParser;
import fr.inria.edelweiss.sparql.restdriver.SparqlRestEndPointDriver;
import fr.inria.edelweiss.sparql.restdriver.SparqlRestEndPointDriverParameters;
import fr.sparna.commons.io.ReadWriteTextFile;

public class GephiAutomate {

	protected String endpointUrl;
	protected String sparqlPath;
	

	public GephiAutomate(String endpointUrl, String sparqlPath) {
		super();
		this.endpointUrl = endpointUrl;
		this.sparqlPath = sparqlPath;
	}

	public void run(String outputFilePath, String gexfFilePath) throws Exception {
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		 
		//Get models and controllers for this new workspace - will be useful later
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
		 
		//Import file       
//		Container container;
//		try {
//		    // File file = new File(getClass().getResource("/home/thomas/workspace/01-Projets/Tours-Tourisme/SMW_ToursTech/Gephi/gephi-automate/gephi-toolkit-0.8.7-all/gephi-toolkit-demos/src/org/gephi/toolkit/demos/resources/polblogs.gml").toURI());
//			File file = new File("/home/thomas/workspace/01-Projets/Tours-Tourisme/SMW_ToursTech/Gephi/gephi-automate/gephi-toolkit-0.8.7-all/gephi-toolkit-demos/src/org/gephi/toolkit/demos/resources/polblogs.gml");
//		    container = importController.importFile(file);
//		    container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
//		} catch (Exception ex) {
//		    ex.printStackTrace();
//		    return;
//		}
		

		/*
		 * LOAD FROM SPARQL ENDPOINT
		 */
		
		String sparql = ReadWriteTextFile.getContents(new File(this.sparqlPath));
		
		System.out.println("Will execute SPARQL : "+"\n"+sparql);
		System.out.println("Will run SPARQL against : "+this.endpointUrl);
		System.out.println("System property file.encoding : "+System.getProperty("file.encoding"));
		System.out.println("System default Charset : "+Charset.defaultCharset());
		System.out.println("Default Charset in Use : "+getDefaultCharSet());
		
		SparqlRestEndPointDriver driver = new SparqlRestEndPointDriver();
		SparqlRestEndPointDriverParameters driverParameters = new SparqlRestEndPointDriverParameters();
		driverParameters.setEndPointUrl(this.endpointUrl);
		driver.setParameters(driverParameters);
		
//		String rdfXmlResult = driver.sparqlQuery(sparql);
//		System.out.println(rdfXmlResult);
		
		SemanticWebImportParser parser = new SemanticWebImportParser();
        parser.getParameters().setRdfRequest(sparql);
        parser.populateRDFGraph(driver, new Properties(), null);
        parser.waitEndpopulateRDFGraph();
        
        /*
         * END - LOAD FROM SPARQL ENDPOINT
         */
        
//		 
//		//Append imported data to GraphAPI
//		importController.process(container, new DefaultProcessor(), workspace);
		 
		//See if graph is well imported
		DirectedGraph graph = graphModel.getDirectedGraph();
		System.out.println("Nodes: " + graph.getNodeCount());
		System.out.println("Edges: " + graph.getEdgeCount());
		 
		//Filter      
//		DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
//		degreeFilter.init(graph);
//		degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));     //Remove nodes with degree < 30
//		Query query = filterController.createQuery(degreeFilter);
//		GraphView view = filterController.filter(query);
//		graphModel.setVisibleView(view);    //Set the filter result as the visible view
		 
		//See visible graph stats
		UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
		System.out.println("Nodes: " + graphVisible.getNodeCount());
		System.out.println("Edges: " + graphVisible.getEdgeCount());
		
		// run layout for 100 passes
		ForceAtlas2Builder builder = new ForceAtlas2Builder();
		ForceAtlas2 fa2Layout = new ForceAtlas2(builder);
		fa2Layout.setGraphModel(graphModel);
		fa2Layout.resetPropertiesValues();
		fa2Layout.setEdgeWeightInfluence(1.0);
		fa2Layout.setGravity(1.0);
		fa2Layout.setScalingRatio(2.0);
		fa2Layout.setBarnesHutTheta(1.2);
		fa2Layout.setJitterTolerance(0.1);
		fa2Layout.initAlgo();
		
		
		for (int i = 0; i < 200 && fa2Layout.canAlgo(); i++) {
			fa2Layout.goAlgo();
		}
		fa2Layout.endAlgo();
		
		System.out.println("End forceAtlas layout");
		
//		ScaleLayout scaleLayout = new ScaleLayout(new Expand(), 1.2d);
//		scaleLayout.setGraphModel(graphModel);
//		
//		scaleLayout.initAlgo();		
//		for (int i = 0; i < 10 && scaleLayout.canAlgo(); i++) {
//			scaleLayout.goAlgo();
//		}
//		scaleLayout.endAlgo();
//		System.out.println("End Scale layout");
		
		// @see https://github.com/gephi/gephi/issues/564
//		Node[] nodes = graphModel.getGraph().getNodes().toArray();
//		for (int i = 0; i < nodes.length; i++)
//		{
//			//Get the TextDataImpl object
//			TextDataImpl td=(TextDataImpl) nodes[i].getNodeData().getTextData();
//			String labelText=nodes[i].getNodeData().getLabel();
//			td.setText(labelText);
//			//Could perhaps used getFontMetrics here to be more accurate but
//			// this heuristic seems to work for me:
//			Rectangle2D bounds=new Rectangle(labelText.length()*10,20);
//			//Use reflection to set the protected Bounds data to non-zero sizes.
//			Field protectedLineField = TextDataImpl.class.getDeclaredField("line");
//			protectedLineField.setAccessible(true);        
//			TextLine line = (TextLine) protectedLineField.get(td);
//			line.setBounds(bounds);
//		}
		
		LabelAdjust ladjustLayout = new LabelAdjust(new LabelAdjustBuilder());
		ladjustLayout.setGraphModel(graphModel);
		
		ladjustLayout.initAlgo();		
		for (int i = 0; i < 1000 && ladjustLayout.canAlgo(); i++) {
			ladjustLayout.goAlgo();
		}
		ladjustLayout.endAlgo();
		System.out.println("End label adjust layout");
		
		 
		//Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);
		 
		//Rank color by Degree
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
		// du beige au rouge :
		colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
		// du bleu au rouge :
		// colorTransformer.setColors(new Color[]{new Color(0x146BFF), new Color(0xB30000)});
		rankingController.transform(degreeRanking,colorTransformer);
		 
		//Rank size by centrality
		AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(1);
		sizeTransformer.setMaxSize(5);
		rankingController.transform(centralityRanking,sizeTransformer);
		 
		// change edge color according to their type
//		Partition p = partitionController.buildPartition(attributeModel.getEdgeTable().getColumn("Label"), graph);
//		System.out.println(p.getPartsCount() + " edge partitions found");
//		EdgeColorTransformer edgeColorTransformer = new EdgeColorTransformer();
//		edgeColorTransformer.randomizeColors(p);
//		partitionController.transform(p, edgeColorTransformer);
//		
		
		// Run modularity algorithm - community detection
//		Modularity modularity = new Modularity();
//        modularity.execute(graphModel, attributeModel);
//        // Change node and edge color according to modularity
//        AttributeColumn modColumn = attributeModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
//        Partition p2 = partitionController.buildPartition(modColumn, graph);
//        System.out.println(p2.getPartsCount() + " modularity partitions found");
//        NodeColorTransformer nodeColorTransformer2 = new NodeColorTransformer();
//        nodeColorTransformer2.randomizeColors(p2);
//        partitionController.transform(p2, nodeColorTransformer2);

		
		
		//Preview
		model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		// model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
		model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.3f));
		model.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH, new Float(0.2f));
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
		
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
	    // ec.exportFile(new File("smw-coopaxis.png"));
		// ec.exportFile(new File("headless_simple.pdf"));
	    PNGExporter exporter = (PNGExporter) ec.getExporter("png");
	    exporter.setHeight(3072);
	    exporter.setWidth(3072);
	    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ec.exportFile(new File(outputFilePath), exporter);
	    
	    // export gefx
	    if(gexfFilePath != null && !gexfFilePath.equals("")) {
	    	System.out.println("Exporting GEXF in "+gexfFilePath);
	    	// get GEXF Exporter
	    	GraphExporter gexfExporter = (GraphExporter) ec.getExporter("gexf");
	    	// Only exports the visible (filtered) graph
	    	gexfExporter.setExportVisible(true);
	    	exporter.setWorkspace(workspace);
	    	ec.exportFile(new File(gexfFilePath), exporter);
	    }
	}
	
	
	
	private static String getDefaultCharSet() {
    	OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
    	String enc = writer.getEncoding();
    	return enc;
    }
	
	/**
	 * args[0] : SPARQL endpoint
	 * args[1] : Path to SPARQL query file
	 * args[2] : PNG output file
	 * args[3] : (optional) : path to gexf output file
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String...args) throws Exception {
		GephiAutomate automate = new GephiAutomate(args[0], args[1]);
		if(args.length > 3) {
			automate.run(args[2], args[3]);
		} else {
			automate.run(args[2], null);
		}
	}
	
}
