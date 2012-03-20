package org.processmining.plugins.bpmn;

import java.awt.event.*;
import java.awt.GridLayout;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;

import org.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;

import org.processmining.models.graphbased.directed.petrinet.elements.ExpandableSubNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.semantics.petrinet.Marking;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class BPMNtoPNUI extends BPMNtoPN{

	private ExpandableSubNet subNet = null;

	@Plugin(name = "BPMN to PetriNet UI",
			parameterLabels = { "BPMNDiagram" },
			returnLabels = {"Petri Net", "Marking",  "Error Log" },
			returnTypes = { Petrinet.class, Marking.class, String.class},userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "GOS", email = "Di.unipi", pack = "BPMN")
	@PluginVariant(requiredParameterLabels = {0}, variantLabel = "Trasform BPMN to PN")
	public Object BPMN2PN(UIPluginContext c ,BPMNDiagram bpmn) {

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
		Map<Activity, boolean[]> lifeCycle = new HashMap<Activity, boolean[]>();

		final SlickerFactory factory = SlickerFactory.instance();

		final JPanel panel = new JPanel();

		// tabella delle JCheckBox
		final Map<String, JCheckBox> boxes = new HashMap <String, JCheckBox>();

		// i 4 possibili sottinsiemi
		final String[] cycles = { "creation", "assignment", "pause/resume", "skip" };
		final int n_cycles = cycles.length;

		// array delle selezioni
		boolean sel[] = new boolean[n_cycles];

		GridLayout experimentLayout = new GridLayout(0, n_cycles+1);
		panel.setLayout(experimentLayout);

//		checkbox per ogni task
		int tasks=0;
		for (Activity act : bpmn.getActivities()) {

			String task = act.getLabel();
			panel.add(factory.createLabel(task + ":"));

			for (String cycle : cycles) {
				JCheckBox ch = factory.createCheckBox(cycle, false);
				boxes.put(task + "+" + cycle, ch);
				panel.add(ch);
			}

			tasks++;
		}

		// checkbox per selezionare un sottoinsieme di lifecycle per tutti i task
		if(tasks>1) {
			panel.add(factory.createLabel("ALL:"));

			for (final String cycle : cycles) {
				final JCheckBox ch = factory.createCheckBox(cycle + " ALL", false);

				final BPMNDiagram BPMN = bpmn;
//			gestore dell'evento selezione sulla checkbox
				ItemListener i = new ItemListener() {
					public void itemStateChanged(ItemEvent e) {

						for (Activity act : BPMN.getActivities())
							boxes.get(act.getLabel() + "+" + cycle).setSelected(ch.isSelected());
					}

				};

				panel.add(ch);
				ch.addItemListener(i);
			}
		}

		JComponent netBPMNView = ProMJGraphVisualizer.instance().visualizeGraph(c, bpmn.getGraph());

		InteractionResult result = c.showWizard(bpmn.getLabel(), true, false, netBPMNView);

		boolean flag = true;

		while(flag) {

			switch (result) {

				case PREV:
					result = c.showWizard(bpmn.getLabel(), true, false, netBPMNView);
					break;

				case NEXT:
					result = c.showWizard("Select task lifecycle", false, true, panel);
					break;

				case FINISHED:
					for (Activity act : bpmn.getActivities()) {

						String task = act.getLabel();

						//crea l'array delle selezioni per il task act
						for (int i=0; i<n_cycles; i++)
							sel[i]=boxes.get(task + "+" + cycles[i]).isSelected();

						//mette in tabella l'array con il rispettivo task
						lifeCycle.put(act, sel);
					}

					flag = false;
					break;

				default:
					return objects;

				}
			}

		translateTask(bpmn, flowMap, net, lifeCycle);

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
			PetrinetGraph net, Map<Activity, boolean[]> lifeCycle) {

		for (Activity c : bpmn.getActivities()) {
			String id = c.getLabel();

			//array delle JCheckBox
			boolean selected[] = lifeCycle.get(c);

			//Transizioni
			Map<String, Transition> t = new HashMap<String, Transition>();

			//Mappe
			Map<String, Place> p = new HashMap<String, Place>();

			//costanti stringa
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
}
