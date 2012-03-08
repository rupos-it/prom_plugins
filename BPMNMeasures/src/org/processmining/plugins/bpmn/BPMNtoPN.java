package org.processmining.plugins.bpmn;

import java.awt.event.*;
import java.awt.GridLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.AttributeMap.SerializablePoint2D;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.AttributeMap;

import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Event;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventType;
import org.processmining.models.graphbased.directed.bpmn.elements.Event.EventTrigger;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway.GatewayType;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;

import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphPort;
import org.processmining.models.semantics.petrinet.Marking;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;


public class BPMNtoPN {

	private ExpandableSubNet subNet = null;
	private BPMNDiagram BPMN;
	
	@Plugin(name = "BPMN to PetriNet",
			parameterLabels = { "BPMNDiagram" },
			returnLabels = {"Petri Net", "Marking",  "Error Log" },
			returnTypes = { Petrinet.class, Marking.class, String.class},userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "GOS", email = "Di.unipi", pack = "BPMN")
	@PluginVariant(requiredParameterLabels = {0}, variantLabel = "Trasform BPMN to PN")
	public Object BPMN2PN(UIPluginContext c ,BPMNDiagram bpmn) {
		BPMN = bpmn;
		Collection<String> error = this.isWellFormed(bpmn);


		// final	LinkedHashMap<Place, Flow> placeMap = new LinkedHashMap<Place, Flow>();
		LinkedHashMap<Flow, Place> flowMap = new LinkedHashMap<Flow, Place>();


		PetrinetGraph net = PetrinetFactory.newPetrinet(bpmn.getLabel());
		Marking marking = new Marking();


		//gli argchi del diagramma BPMN diventano piazze della rete di Petri
		for (Flow g : bpmn.getFlows()) {
			String f = g.getSource().getLabel();
			String z = g.getTarget().getLabel();

			Place p = net.addPlace(f + z, this.subNet);

			flowMap.put(g, p);


		}

		Object[] objects = new Object[3];
		objects[0]=objects[1]=objects[2]=null;
		
		//tabella dei lifecycle selezionati per ogni task
		BPMN.getGraph();
		
		final Map<String, JCheckBox> boxes;
		Map<Activity, boolean[]> tab = new HashMap<Activity, boolean[]>();
		
			final SlickerFactory factory = SlickerFactory.instance();

			final JPanel panel = new JPanel();
			
			// tabella delle JCheckBox
			boxes = new HashMap <String, JCheckBox>();
			final String[] cycles = { "creation", "assignment", "pause and resume", "skip" };
			final int n_cycles = cycles.length;
			boolean sel[] = new boolean[n_cycles];

			GridLayout experimentLayout = new GridLayout(0, 1);
			panel.setLayout(experimentLayout);
			
//			checkbox per ogni task
			int tasks=0;
			for (Activity act : bpmn.getActivities()) {

				String task = act.getLabel();
				panel.add(factory.createLabel(task + ":"));

				for (String cy : cycles) {
					JCheckBox ch = factory.createCheckBox(cy, false);
					boxes.put(task + "+" + cy, ch);
					panel.add(ch);
				}
				
				tasks++;
			}

			// checkbox per selezionare un sottoinsieme di lifecycle per tutti i task
			if(tasks>1) {
				panel.add(factory.createLabel("ALL:"));
			
				for (final String cy : cycles) {
					final JCheckBox ch = factory.createCheckBox(cy + " ALL", false);
					boxes.put(cy,ch);
					panel.add(ch);

//				gestore dell'evento sulla checkbox
					ItemListener i = new ItemListener() {
						public void itemStateChanged(ItemEvent e) {

							for (Activity act : BPMN.getActivities()) {
								String task = act.getLabel();
								boxes.get(task + "+" + cy).setSelected(ch.isSelected());
							}
						}

					};

					panel.add(ch);
					ch.addItemListener(i);
				}
			}
			
			JComponent netBPMNView = ProMJGraphVisualizer.instance().visualizeGraph(c, BPMN.getGraph());
					
			InteractionResult result = ((UIPluginContext)c).showWizard(BPMN.getLabel(), true, false, netBPMNView);

			boolean flag = true;
			
			while(flag) {

				switch (result) {

				case PREV:
					result = ((UIPluginContext)c).showWizard(BPMN.getLabel(), true, false, netBPMNView);
					break;

				case NEXT:
					result = ((UIPluginContext)c).showWizard("Select task lifecycle", false, true, panel);
					break;

				case FINISHED :
					for (Activity act : bpmn.getActivities()) {

						String task = act.getLabel();
					
						//crea l'array delle selezioni per il task act
						for (int i=0; i<n_cycles; i++)
							sel[i]=boxes.get(task + "+" + cycles[i]).isSelected();
						
						//mette in tabella l'array con il rispettivo task
						tab.put(act, sel);
					}
					
					flag = false;
					break;
					
				default :
					return objects;
			
				}
			}
		
		translateTask(bpmn, flowMap, net, tab);

		translateGateway(bpmn, flowMap, net);

		translateEvent(bpmn, flowMap, net, marking);

		layoutcreate(c,net);

		String errorLog = error.toString();

		objects[0] = net;
		objects[1] = marking;


		c.addConnection(new BPMNtoPNConnection(bpmn, net, errorLog, flowMap.values()));

		c.addConnection(new InitialMarkingConnection(net, marking));



		return objects;
	}


	private void translateTask(BPMNDiagram bpmn, LinkedHashMap<Flow,Place> flowMap,
			PetrinetGraph net, Map<Activity, boolean[]> tab) {

		for (Activity c : bpmn.getActivities()) {
			String id = c.getLabel();

			//array delle JCheckBox
			boolean selected[] = tab.get(c);

			Map<String, Transition> t = new HashMap<String, Transition>();
			Map<String, Place> p = new HashMap<String, Place>();

			//costanti
			final String crt="create", ass="assign", rvk="revoke", rea="reassign", st="start", pau="pause", rsm="resume", cpl="complete", skd="skipped";
			final String A="alfa", B="beta", G="gamma", D="delta", S="sigma", L="lambda", E="epsilon";
			final String ctd="created", asd="assigned", rvg="revoking", rvd="revoked", run="running", spd="suspended", skg="skipping";

			
			//aggiungo transizioni e piazze alle hashmap
			insertTransition(net,id,st,t,false);
			insertPlace(net,id,run,p);
			insertTransition(net,id,cpl,t,false);
			if(selected[0]) {
				insertTransition(net, id, crt, t, false);
				insertPlace(net,id,ctd,p);
			}
			if(selected[1]) {
				insertTransition(net,id,ass,t,false);
				insertPlace(net,id,asd,p);
				insertTransition(net,id,G,t,true);
				insertPlace(net,id,rvg,p);
				insertTransition(net,id,rvk,t,false);
				insertPlace(net,id,rvd,p);
				insertTransition(net,id,rea,t,false);
				insertTransition(net,id,S,t,true);
				if(selected[3]) {
					insertTransition(net,id,B,t,true);
					insertTransition(net,id,D,t,true);				
				}
			}
			if(selected[2]) {
				insertTransition(net,id,pau,t,false);
				insertPlace(net,id,spd,p);
				insertTransition(net,id,rsm,t,false);				
				if(selected[3])
					insertTransition(net,id,L,t,true);
			}
			if(selected[3]) {
				insertTransition(net,id,A,t,true);
				insertTransition(net,id,E,t,true);								
				insertPlace(net,id,skg,p);
				insertTransition(net,id,skd,t,false);				
			}
			
			
			//archi
			net.addArc (t.get(st), p.get(run));
			net.addArc (p.get(run), t.get(cpl));
			if(selected[0]) {
				net.addArc (t.get(crt), p.get(ctd));
				if(selected[1])
					net.addArc (p.get(ctd), t.get(ass));
				else
					net.addArc (p.get(ctd), t.get(st));
				if(selected[3])
					net.addArc (p.get(ctd), t.get(A));
			}
			if(selected[1]) {
				net.addArc (t.get(ass), p.get(asd));
				net.addArc (p.get(asd), t.get(G));
				net.addArc (t.get(G), p.get(rvg));
				net.addArc (t.get(S), p.get(rvg));
				net.addArc (p.get(rvg), t.get(rvk));
				net.addArc (t.get(rvk), p.get(rvd));
				net.addArc (p.get(rvd), t.get(rea));
				net.addArc (t.get(rea), p.get(asd));
				net.addArc (p.get(asd), t.get(st));
				if(selected[3]) {				
					net.addArc (p.get(asd), t.get(B));
					net.addArc (t.get(B), p.get(skg));
					net.addArc (p.get(rvd), t.get(D));
					net.addArc (t.get(D), p.get(skg));
				}
			}
			if(selected[2]) {
				net.addArc (p.get(run), t.get(pau));
				net.addArc (t.get(pau), p.get(spd));
				net.addArc (p.get(spd), t.get(rsm));
				net.addArc (t.get(rsm), p.get(run));
				if(selected[1])
					net.addArc (p.get(spd), t.get(S));
				if(selected[3]) {
					net.addArc (p.get(spd), t.get(L));
					net.addArc (t.get(L), p.get(skg));
				}
			}
			else if(selected[1])
				net.addArc (p.get(run), t.get(S));
			if(selected[3]) {
				net.addArc (t.get(A), p.get(skg));
				net.addArc (p.get(run), t.get(E));
				net.addArc (t.get(E), p.get(skg));
				net.addArc (p.get(skg), t.get(skd));
			}
			
	
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : c
					.getGraph().getInEdges(c)) {
				if(s instanceof Flow)	{

					Place pst = flowMap.get(s);
					if(selected[0])
						net.addArc(pst, t.get(crt), 1, this.subNet);
					else {
						if(selected[1])
							net.addArc(pst, t.get(ass), 1, this.subNet);
						else
							net.addArc(pst, t.get(st), 1, this.subNet);
						if(selected[3])
							net.addArc(pst, t.get(A), 1, this.subNet);						
						}
					}

				}
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : c
					.getGraph().getOutEdges(c)) {
				if(s instanceof Flow){

					Place pst = flowMap.get(s);

					// complete -> p_end
					net.addArc(t.get(cpl), pst, 1, this.subNet);

					// skipped -> p_end
					if(selected[3])
						net.addArc(t.get(skd), pst, 1, this.subNet);
				}
			}

		}
	}

	private void insertTransition(PetrinetGraph net, String id, String s, Map<String, Transition> t, boolean vs) {
		String trsName = id + "+" + s;
		Transition trs = net.addTransition(trsName);
		trs.setInvisible(vs);
		t.put(s, trs);
	}

	private void insertPlace(PetrinetGraph net, String id, String s, Map<String, Place> p) {
		String placeName = id + "+" + s;
		p.put(s, net.addPlace(placeName));
	}

	private void translateGateway(BPMNDiagram bpmn,	LinkedHashMap<Flow, Place> flowMap, PetrinetGraph net) {
		for (Gateway g : bpmn.getGateways()) {
			//gateway data-based
			if (g.getGatewayType().equals(GatewayType.DATABASED)) {
				int i = 0;
				Map<String, Transition> tranMap = new HashMap<String, Transition>();
				//gateway data-based if branch 
				if (g.getGraph().getOutEdges(g).size()>1 && g.getGraph().getInEdges(g).size()==1 ){
					for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : g.getGraph().getOutEdges(g)) {
						String source = s.getSource().getLabel();
						String target = s.getTarget().getLabel();

						Transition t = net.addTransition(g.getLabel() + "_" + i++,
								this.subNet);
						t.setInvisible(true);
						tranMap.put(target + source, t);

						Place pst = flowMap.get(s);

						net.addArc(t, pst, 1, this.subNet);

					}
					for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : g.getGraph().getInEdges(g)) {
						String source = s.getSource().getLabel();
						String target = s.getTarget().getLabel();

						Place pst = flowMap.get(s);

						for (Transition t : tranMap.values()) {

							net.addArc(pst, t, 1, this.subNet);

						}
					}
				}else{
					//gateway merge
					if (g.getGraph().getOutEdges(g).size()==1 && g.getGraph().getInEdges(g).size()>1 ){

						Place ps =null;
						for(BPMNEdge<? extends BPMNNode, ? extends BPMNNode> out : g.getGraph().getOutEdges(g)) {
							ps= flowMap.get(out);
						}
						i=0;
						for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : g.getGraph().getInEdges(g)){



							Place pst = flowMap.get(s);

							Transition t = net.addTransition(g.getLabel() + "_" + i++,this.subNet );
							t.setInvisible(true);
							net.addArc( pst,t, 1, this.subNet);

							net.addArc(t, ps, this.subNet);

						}
					}
				}

			}else{
				if (g.getGatewayType().equals(GatewayType.PARALLEL)) {
					//gateway parallel fork 
					if (g.getGraph().getOutEdges(g).size()>1 && g.getGraph().getInEdges(g).size()==1 ){
						BPMNEdge<? extends BPMNNode, ? extends BPMNNode> so = g.getGraph().getInEdges(g).iterator().next();

						Place ps = flowMap.get(so);
						Transition t = net.addTransition(g.getLabel() + "_fork",this.subNet );
						t.setInvisible(true);
						net.addArc( ps,t, 1, this.subNet);
						for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : g.getGraph().getOutEdges(g)){


							Place pst = flowMap.get(s);
							net.addArc(t, pst, 1, this.subNet);

						}


					}else{
						//gateway parallel Join 
						if (g.getGraph().getOutEdges(g).size()==1 && g.getGraph().getInEdges(g).size()>1 ){
							BPMNEdge<? extends BPMNNode, ? extends BPMNNode> so = g.getGraph().getOutEdges(g).iterator().next();

							Place ps = flowMap.get(so);
							Transition t = net.addTransition(g.getLabel() + "_join",this.subNet );
							t.setInvisible(true);
							net.addArc( t,ps, 1, this.subNet);
							for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : g.getGraph().getInEdges(g)){

								Place pst = flowMap.get(s);
								net.addArc(pst,t, 1, this.subNet);

							}


						}
					}
				}else{
					//gateway event-based
					if (g.getGatewayType().equals(GatewayType.EVENTBASED)) {
						//Exclusive event gateway 
						if (g.getGraph().getOutEdges(g).size()>1 && g.getGraph().getInEdges(g).size()==1 ){
							BPMNEdge<? extends BPMNNode, ? extends BPMNNode> so = g.getGraph().getInEdges(g).iterator().next();

							Place ps = flowMap.get(so);
							int i=0;
							for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : g.getGraph().getOutEdges(g)){

								Place pst = flowMap.get(s);

								Transition t = net.addTransition(g.getLabel() + "_" + i++,this.subNet );
								t.setInvisible(true);
								net.addArc( t,pst, 1, this.subNet);

								net.addArc( ps,t , this.subNet);

							}


						}


					}
				}
			}
		}
	}

	private void translateEvent(BPMNDiagram bpmn, LinkedHashMap<Flow, Place> flowMap, PetrinetGraph net, Marking marking){
		for (Event e : bpmn.getEvents()) {
			if (e.getEventType().equals(EventType.START) && e.getEventTrigger().equals(EventTrigger.NONE)) {

				// Place p = new Place(e.getLabel(), net);
				Place p = net.addPlace("p"+e.getLabel(), this.subNet);

				Transition t = net.addTransition("t_"+e.getLabel(), this.subNet);
				t.setInvisible(true);
				net.addArc(p, t, 1, this.subNet);
				marking.add(p, 1);

				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : e.getGraph().getOutEdges(e)) {


					Place pst = flowMap.get(s);

					net.addArc(t, pst, 1, this.subNet);

				}


			}
			if (e.getEventType().equals(EventType.END) && e.getEventTrigger().equals(EventTrigger.NONE)) {


				Place p = net.addPlace("p"+e.getLabel(), this.subNet);

				Transition t = net.addTransition("t_"+e.getLabel(), this.subNet);

				t.setInvisible(true);
				net.addArc(t, p, 1, this.subNet);

				for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> s : e.getGraph().getInEdges(e)) {

					Place pst = flowMap.get(s);

					net.addArc( pst, t, 1, this.subNet);

				}

			}
			if (e.getEventType().equals(EventType.INTERMEDIATE) && !e.getEventTrigger().equals(EventTrigger.NONE)) {



				Transition t = net.addTransition(e.getLabel(), this.subNet);			


				if(e.getBoundingNode()==null){
					BPMNEdge<? extends BPMNNode, ? extends BPMNNode> g = e.getGraph().getInEdges(e).iterator().next();
					if(g instanceof Flow && g!=null){
						Place ps_pre = flowMap.get(g);


						g  = e.getGraph().getOutEdges(e).iterator().next();;
						if(g instanceof Flow && g!=null){
							Place ps_post = flowMap.get(g);

							net.addArc(ps_pre,t, 1, this.subNet);
							net.addArc(t,ps_post, 1, this.subNet);
						}

					}
				}else{
					//è un evento di confine
					
				}

			}

		}


	}






	private void layoutcreate(PluginContext c, PetrinetGraph net){

		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		try {
			layout = c.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, c, net);
		} catch (ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			/*
			 * Get a jgraph for this graph.
			 */

			ProMJGraph jgraph = ProMJGraphVisualizer.instance().visualizeGraph(c, net).getGraph();
			/*
			 * Layout this jgraph.
			 */
			JGraphFacade facade = new JGraphFacade(jgraph);
			layOutFMJGraph(net, jgraph, facade);
			System.out.print("Creata Layout della PetriNet");
		}
	}
	private void layOutFMJGraph(
			DirectedGraph<? extends AbstractDirectedGraphNode, ? extends AbstractDirectedGraphEdge<?, ?>> graph,
					ProMJGraph jgraph, JGraphFacade facade) {
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(20);
		layout.setOrientation(graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

		facade.setOrdered(true);
		facade.setEdgePromotion(true);
		facade.setIgnoresCellsInGroups(false);
		facade.setIgnoresHiddenCells(false);
		facade.setIgnoresUnconnectedCells(false);
		facade.setDirected(false);
		facade.resetControlPoints();

		facade.run(layout, false);

		fixParallelTransitions(facade, 15);

		Map<?, ?> nested = facade.createNestedMap(true, false);

		jgraph.getGraphLayoutCache().edit(nested);
		jgraph.setUpdateLayout(layout);
	}
	private void fixParallelTransitions(JGraphFacade facade, double spacing) {
		ArrayList<Object> edges = getEdges(facade);
		for (Object edge : edges) {
			List<Object> points = getPoints(facade, edge);
			if (points.size() != 2) {
				continue;
			}
			Object sourceCell = facade.getSource(edge);
			Object targetCell = facade.getTarget(edge);
			Object sourcePort = facade.getSourcePort(edge);
			Object targetPort = facade.getTargetPort(edge);
			Object[] between = facade.getEdgesBetween(sourcePort, targetPort, false);
			if ((between.length == 1) && !(sourcePort == targetPort)) {
				continue;
			}
			Rectangle2D sCP = facade.getBounds(sourceCell);
			Rectangle2D tCP = facade.getBounds(targetCell);
			Point2D sPP = GraphConstants.getOffset(((ProMGraphPort) sourcePort).getAttributes());

			if (sPP == null) {
				sPP = new Point2D.Double(sCP.getCenterX(), sCP.getCenterY());
			}
			Point2D tPP = GraphConstants.getOffset(((ProMGraphPort) targetPort).getAttributes());
			// facade.getBounds(sourcePort);

			if (tPP == null) {
				tPP = new Point2D.Double(tCP.getCenterX(), tCP.getCenterY());
			}

			if (sourcePort == targetPort) {
				assert (sPP.equals(tPP));
				double x = sPP.getX();
				double y = sPP.getY();
				for (int i = 2; i < between.length + 2; i++) {
					List<Point2D> newPoints = new ArrayList<Point2D>(5);
					newPoints.add(new Point2D.Double(x - (spacing + i * spacing), y));
					newPoints.add(new Point2D.Double(x - (spacing + i * spacing), y - (spacing + i * spacing)));
					newPoints.add(new Point2D.Double(x, y - (2 * spacing + i * spacing)));
					newPoints.add(new Point2D.Double(x + (spacing + i * spacing), y - (spacing + i * spacing)));
					newPoints.add(new Point2D.Double(x + (spacing), y - (spacing / 2 + i * spacing)));
					facade.setPoints(between[i - 2], newPoints);
				}

				continue;
			}

			double dx = (sPP.getX()) - (tPP.getX());
			double dy = (sPP.getY()) - (tPP.getY());
			double mx = (tPP.getX()) + dx / 2.0;
			double my = (tPP.getY()) + dy / 2.0;
			double slope = Math.sqrt(dx * dx + dy * dy);
			for (int i = 0; i < between.length; i++) {
				List<Point2D> newPoints = new ArrayList<Point2D>(3);
				double pos = 2 * i - (between.length - 1);
				if (facade.getSourcePort(between[i]) == sourcePort) {
					newPoints.add(sPP);
					newPoints.add(tPP);
				} else {
					newPoints.add(tPP);
					newPoints.add(sPP);
				}
				if (pos != 0) {
					pos = pos / 2;
					double x = mx + pos * spacing * dy / slope;
					double y = my - pos * spacing * dx / slope;
					newPoints.add(1, new SerializablePoint2D.Double(x, y));
				}
				facade.setPoints(between[i], newPoints);
			}
		}
	}
	@SuppressWarnings("unchecked")
	private ArrayList<Object> getEdges(JGraphFacade facade) {
		return new ArrayList<Object>(facade.getEdges());
	}

	@SuppressWarnings("unchecked")
	private List<Object> getPoints(JGraphFacade facade, Object edge) {
		return facade.getPoints(edge);
	}

	private Collection<String> isWellFormed(BPMNDiagram bpmn){
		if (true)
			return new Vector<String>();
		
		Collection<String> maperror = new Vector<String>();
		//  Elementi BPMN che non possono essere mappati:
		//  Event-Start  End != NONE
		//  IntermediateCompensation-Event 
		//  EndCompensation-Event 
		//  EndLink-Event 
		//  StartLink-Event 

		GatewayWellFormed(bpmn,maperror);
		EventWellFormed(bpmn,maperror);

		ActivityWellFormed(bpmn,maperror);

		//every object is on a path from a start event or an exception event to an end event
		pathFromStartToEnd(bpmn,maperror);
		//if Q is a set of well-formed core BPMN processes and the relation HR is a Direct Acyclic Graph, HR* is a connected graph
		//Controlliamo che nn ci siano sotto processi che vengono invocati da più attività o che un sotto processo figlio invochi un sottoprocesso padre
		acyclicSubProcess(bpmn,maperror);


		// Nessuna SequenceFlow deve essere collegata allo stesso elemento
		for(Flow g : bpmn.getFlows()) {
			BPMNNode s = g.getSource();
			BPMNNode t = g.getTarget();
			if(s.equals(t)){
				maperror.add(s.getLabel()+"SequenceFlow connessa allo stesso elemento+flow_s->t");
			}
		}



		return maperror;



	}

	private void acyclicSubProcess(BPMNDiagram bpmn, Collection<String> maperror) {
		Collection<SubProcess> subs = bpmn.getSubProcesses();
		for(SubProcess s : subs){
			System.out.print(s);
			isacyclicsubprocess( s , maperror);
		}

	}

	private void isacyclicsubprocess(SubProcess s, Collection<String> maperror) {
		/*if(){

		}*/

	}

	private void pathFromStartToEnd(BPMNDiagram bpmn,
			Collection<String> maperror) {
		//every object is on a path from a start event or an exception event to an end event
		for(BPMNNode a : bpmn.getNodes()){
			if(!( a instanceof Swimlane)){
				pathFromNodeToEnd(a,maperror,a);
				pathFromNodeToStart(a,maperror,a);
			}

		}

	}

	private void pathFromNodeToStart(BPMNNode a, Collection<String> maperror,
			BPMNNode c) {
		Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> edges = a.getGraph().getInEdges(a);
		if(!edges.isEmpty()){
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : edges){
				BPMNNode b = edge.getSource();
				if(b instanceof Event){
					Event isend = (Event) b;
					if(isend.getEventTrigger()==null){isend.setEventTrigger(EventTrigger.NONE);}
					if((isend.getEventType()==EventType.START && isend.getEventTrigger()==EventTrigger.NONE)||(isend.getEventType()==EventType.INTERMEDIATE&& isend.getEventTrigger()==EventTrigger.ERROR)){
						break;
					}
				}
				if(b==null){
					maperror.add("The path of element [["+c.getLabel()+"]] don't contain start event element");
				}
				pathFromNodeToStart(b,maperror, c); ///vero??
			}

		}else{
			if(a instanceof Event){
				Event isstart = (Event) a;
				if(isstart.getEventTrigger()==null){isstart.setEventTrigger(EventTrigger.NONE);}
				if(isstart.getEventType()!=EventType.START && isstart.getEventTrigger()!=EventTrigger.NONE){
					maperror.add("The path of element [["+c.getLabel()+"]] don't contain start event  element");
				}
			}else maperror.add("The path of element [["+c.getLabel()+"]] don't contain start event element");

		}	

	}

	private void pathFromNodeToEnd(BPMNNode a, Collection<String> maperror, BPMNNode c) {
		Collection<BPMNEdge<? extends BPMNNode, ? extends BPMNNode>> edges = a.getGraph().getOutEdges(a);
		if(!edges.isEmpty()){
			for (BPMNEdge<? extends BPMNNode, ? extends BPMNNode> edge : edges){
				BPMNNode b = edge.getTarget();
				if(b instanceof Event){
					Event isend = (Event) b;
					if(isend.getEventTrigger()==null){isend.setEventTrigger(EventTrigger.NONE);}
					if(isend.getEventType()==EventType.END && isend.getEventTrigger()==EventTrigger.NONE){
						break;
					}
				}
				if(b==null){
					maperror.add("The path of element [["+c.getLabel()+"]] don't contain end event element");
				}
				pathFromNodeToEnd(b,maperror, c);
			}
		}else{
			if(a instanceof Event){
				Event isend = (Event) a;
				if(isend.getEventTrigger()==null){isend.setEventTrigger(EventTrigger.NONE);}
				if(isend.getEventType()!=EventType.END && isend.getEventTrigger()!=EventTrigger.NONE){
					maperror.add("The path of element [["+c.getLabel()+"]] don't contain end event element");
				}
			}else maperror.add("The path of element [["+c.getLabel()+"]] don't contain end event element");

		}		

	}

	private void ActivityWellFormed(BPMNDiagram bpmn, Collection<String> maperror) {

		for (Activity c : bpmn.getActivities()) {

			if(c.isBCompensation() || c.isBMultiinstance()){
				maperror.add("Activity not valid [["+c.getLabel()+"]]");
			}
			// activities  events have an in-degree of one and an out-degree of one
			if (c.getGraph().getInEdges(c).size()!=1 || c.getGraph().getOutEdges(c).size()!=1){
				maperror.add("Activities events don't have an in-degree of one and an out-degree of one [["+c.getLabel()+"]]");
			}
			if(c.getLabel().isEmpty()){
				maperror.add("Activities without label [["+c.getLabel()+"]]"); 
			}
		}
	}

	private void GatewayWellFormed(BPMNDiagram bpmn,Collection<String> maperror ){
		for (Gateway g : bpmn.getGateways()) {
			//DATABASED, EVENTBASED, INCLUSIVE, COMPLEX, PARALLEL
			GatewayType gtype = g.getGatewayType();

			switch (gtype) {
			case  INCLUSIVE :   maperror.add("Gateway not valid [["+g.getLabel()+"]]");    break;
			case COMPLEX : maperror.add("Gateway not valid [["+g.getLabel()+"]]"); break;
			}
			if(g.getLabel().isEmpty()){
				maperror.add("Gateway without [[ "+g.getLabel()+"]]"); 
			}

			//fork or decision gateways have an in-degree of one and an out-degree of more than one,
			//join or merge gateways have an out-degree of one and an in-degree of more than one
			if(gtype.equals(GatewayType.DATABASED) || gtype.equals(GatewayType.PARALLEL)){
				if(!(g.getGraph().getInEdges(g).size()==1 && g.getGraph().getOutEdges(g).size()>1 )){

					if(!(g.getGraph().getInEdges(g).size()>1 && g.getGraph().getOutEdges(g).size()==1 )){

						maperror.add(" Gateway don't have an out-degree of one and an in-degree of more than one [["+g.getLabel()+"]]"); 

					}
				}

			}
		}
	}

	private void EventWellFormed(BPMNDiagram bpmn,Collection<String> maperror ){
		for (Event e : bpmn.getEvents()){
			//START, INTERMEDIATE, END;
			EventType type = e.getEventType();
			//MESSAGE, TIMER, ERROR, CANCEL, COMPENSATION, CONDITIONAL, LINK, SIGNAL, TERMINATE, MULTIPLE
			EventTrigger trigger = e.getEventTrigger();

			if(trigger==null)
				trigger=EventTrigger.NONE;
			switch (trigger) {
			case COMPENSATION :   maperror.add("Event not valid [["+e.getLabel()+"]]");    break;
			case LINK : maperror.add("Evento not valid [["+e.getLabel()+"]]"); break;
			case CONDITIONAL : maperror.add("Evento not valid [["+e.getLabel()+"]]"); break;
			case SIGNAL : maperror.add("Evento not valid [["+e.getLabel()+"]]"); break;
			case MESSAGE : maperror.add("Evento not valid [["+e.getLabel()+"]]"); break;

			}

			if(e.getLabel().isEmpty()){
				maperror.add("Event without label [["+e.getLabel()+"]]"); 
			}
			//se trovo start o end che non sono di tipo NONE
			if(type.equals(EventType.START) || type.equals(EventType.END)){
				if (!trigger.equals(EventTrigger.NONE)){
					maperror.add("Evento not valid [["+e.getLabel()+"]]");
				}
			}

			// start events and exception events have an in-degree of zero and an out-degree of one
			if(type.equals(EventType.START) && ( trigger.equals(EventTrigger.ERROR) || trigger.equals(EventTrigger.NONE) )){
				if(e.getGraph().getInEdges(e).size()!=0){
					maperror.add("start events and exception events don't have an in-degree of zero [["+e.getLabel()+"]]");
				}
				if(e.getGraph().getOutEdges(e).size()!=1){
					maperror.add("start events and exception events don't have an out-degree of one [["+e.getLabel()+"]]");
				}
			}

			//end events have an out-degree of zero and an in-degree of one
			if(type.equals(EventType.END) &&  trigger.equals(EventTrigger.NONE)){
				if(e.getGraph().getInEdges(e).size()!=1){
					maperror.add("end events don't have  an in-degree of one [["+e.getLabel()+"]]");
				}
				if(e.getGraph().getOutEdges(e).size()!=0){
					maperror.add("end events don't  have an out-degree of zero [["+e.getLabel()+"]]");
				}
			}
			//  non-exception intermediate events have an in-degree of one and an out-degree of one
			if(type.equals(EventType.INTERMEDIATE) &&  !trigger.equals(EventTrigger.ERROR)){
				if(e.getGraph().getInEdges(e).size()!=1 && e.getGraph().getOutEdges(e).size()!=1){
					maperror.add("non-exception intermediate events don't have an in-degree of one and an out-degree of one [["+e.getLabel()+"]]");
				}
			}

		}
	}


}
