package org.processmining.plugins.bpmn.exporting;

import java.awt.Color;

import java.util.Collection;
import java.util.HashMap;

import java.util.Map;
import java.util.Vector;


import org.processmining.models.graphbased.AttributeMap;

import org.processmining.models.graphbased.directed.ContainingDirectedGraphNode;

import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramExt;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagramExtFactory;

import org.processmining.models.graphbased.directed.bpmn.elements.Activity;
import org.processmining.models.graphbased.directed.bpmn.elements.Artifacts;
import org.processmining.models.graphbased.directed.bpmn.elements.Flow;

import org.processmining.models.graphbased.directed.bpmn.elements.Artifacts.ArtifactType;
import org.processmining.models.graphbased.directed.bpmn.elements.SubProcess;
import org.processmining.models.graphbased.directed.bpmn.elements.Swimlane;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import org.processmining.models.semantics.petrinet.Marking;

import org.processmining.plugins.petrinet.replay.conformance.ConformanceResult;
import org.processmining.plugins.petrinet.replay.performance.PerformanceData;
import org.processmining.plugins.petrinet.replay.performance.PerformanceResult;

import org.processmining.plugins.bpmn.BPMNtoPNUI;


public class BPMNDecorateUtil {

	public BPMNDecorateUtil() {

	}

	public static BPMNDiagramExt exportPerformancetoBPMN(
			BPMNDiagram bpmnoriginal,
			PerformanceResult Performanceresult, Collection< Place> placeFlowCollection,
			Petrinet net) {

		// clona bpmn
		BPMNDiagramExt bpmn = BPMNDiagramExtFactory.cloneBPMNDiagram(bpmnoriginal);

		Map<Arc, Integer> maparc = Performanceresult.getMaparc();

		Map<String, Integer> ArchiAttivatiBPMN = new HashMap<String, Integer>();
		Map<String, String> archibpmnwithsyncperformance = new HashMap<String, String>();

		

		// ogni piazza che attraversiamo in performance result conta i token
		// passati sulla pizza
		/*
		 * for(Place p : Performanceresult.keySet()){
		 * 
		 * if(MapArc2Place.containsKey(p.getLabel())){
		 * 
		 * PerformanceData rs= Performanceresult.get(p); int i =
		 * rs.getTokenCount(); ArchiAttivatiBPMN.put(p.getLabel(), i); } }
		 */

		for (Place p : placeFlowCollection) {

			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = p
			.getGraph().getOutEdges(p);
			int count = 0;
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				Arc a = (Arc) edge;
				if (maparc.containsKey(a)) {
					Integer i = maparc.get(a);
					count += i;
				}
			}

			ArchiAttivatiBPMN.put(p.getLabel(), count);
		}

		//tempo di esecuzione di un task BPMN
		Map<Activity, Float> MapExc = new HashMap<Activity, Float>();
		//tempo totale di un task BPMN
		Map<Activity, Float> MapTot = new HashMap<Activity, Float>();

		for (Place p : net.getPlaces()) {
			String pname = p.getLabel();
			String taskName;
			try {
				taskName = (String) pname.subSequence(0, pname.indexOf("+"));
			}
			catch (StringIndexOutOfBoundsException e) {
				taskName = null;	
			}
			Activity activity = null;
			// cerco l'attività bpmn a cui collegare l'artifacts
			for (Activity a : bpmn.getActivities()) {
				if (taskName != null && a.getLabel().equals(taskName)) {
					activity = a;
					break;
				}
			}

			if(activity!=null){
				PerformanceData ps = getPerfResult(p, Performanceresult.getList());
				if (ps != null) {
					if(MapTot.containsKey(activity))
						MapTot.put(activity, MapTot.get(activity)+ps.getTime());
					else
						MapTot.put(activity, ps.getTime());
					if(pname.endsWith(BPMNtoPNUI.run)) {
						//tempo totale delle esecuzioni su p
						float time = ps.getTime();
						//numero esecuzioni (numero attivazioni dell'arco start -> running)
						int count = 1;
						//	esaminiamo gli archi della stella entrante della piazza running
						Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = p
								.getGraph().getInEdges(p);
						for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
							Arc a = (Arc) edge;
							if (a.getSource().getLabel().endsWith(BPMNtoPNUI.st))
								count = maparc.get(a);
						}
						//si considera il tempo medio di esecuzione
						MapExc.put(activity,time/count);
					}
				}
			}
		}
		/*		for (Transition t : net.getTransitions()) {
			String text = "";
			if (!t.isInvisible()) {
				String tname = t.getLabel();
				String name = (String) tname.subSequence(0, tname.indexOf("+"));
				Activity activity = null;
				// cerco l'attività bpmn a cui collegare l'artifacts
				for (Activity a : bpmn.getActivities()) {
					if (a.getLabel().equals(name)) {
						activity = a;
						break;
					}
				}
				if(activity!=null){
					Place preplace = (Place) t.getGraph().getInEdges(t).iterator()
							.next().getSource();
			// Place postplace = (Place)
			// t.getGraph().getOutEdges(t).iterator().next().getTarget();
					PerformanceData ps = getPerfResult(preplace, Performanceresult.getList());
					if (ps != null) {
						if(preplace.getLabel().endsWith("running")) {
							excTime += ps.getWaitTime(); 
						}
				/*
						if (t.getLabel().endsWith("start")) {
							if (ps.getWaitTime() > 0) {
								text = "Activation Time: " + ps.getWaitTime()
										+ "<br/>";
				}
						} 
						else if (t.getLabel().endsWith("complete")) {
					if (ps.getWaitTime() > 0) {
						text = "Execution Time: " + ps.getWaitTime()
								+ "<br/>";

					}
				}
						if (MapActivity.containsKey(activity))
							text += MapActivity.get(activity);
						MapActivity.put(activity, text);
					}
				}
			} else {
				//t.getLabel().endsWith("_join")
				if (t.getGraph().getInEdges(t).size()>1) {

					// controlla la presenza di sync time e inserisce il souj
					// per ogni ramo parallelo
					addsoujandsynctime(Performanceresult.getList(), t,
							archibpmnwithsyncperformance);
					}
				}
		}*/

		for (Activity a : bpmn.getActivities()) {
			float totTime = MapTot.get(a)/ArchiAttivatiBPMN.get(a.getLabel());
			String text = "Execution Time: " + MapExc.get(a) + "<br/>Total Time: " + totTime + "<br/>";
			String label = "<html>" + text + "</html>";
			ContainingDirectedGraphNode parent = a.getParent();
			Artifacts art = null;
			if (parent instanceof Swimlane) {
				art = bpmn.addArtifacts(label, ArtifactType.TEXTANNOATION,
						a.getParentSwimlane());
				bpmn.addFlowAssociation(art, a,a.getParentSwimlane());
			}
			if (parent instanceof SubProcess) {
				art = bpmn.addArtifacts(label, ArtifactType.TEXTANNOATION,
						a.getParentSubProcess());
				bpmn.addFlowAssociation(art, a,a.getParentSubProcess());
			}
			if (parent == null) {
				art = bpmn.addArtifacts(label, ArtifactType.TEXTANNOATION);
				bpmn.addFlowAssociation(art, a);
			}


		}

		// i sync time sono sempre sulle piazze "arco", quindi cerco l'arco a
		// cui si riferisco i place con sync time ed aggiungo
		// il tooltip all'arco e lo coloro di rosso.
		for (Flow f : bpmn.getFlows()) {
			String from = f.getSource().getLabel();
			String to = f.getTarget().getLabel();
			if (archibpmnwithsyncperformance.containsKey(from + to)) {
				String flowsync = archibpmnwithsyncperformance.get(from + to);
				f.getAttributeMap().remove(AttributeMap.TOOLTIP);

				f.getAttributeMap().put(AttributeMap.TOOLTIP, flowsync);
				f.getAttributeMap().remove(AttributeMap.SHOWLABEL);
				f.getAttributeMap().put(AttributeMap.SHOWLABEL, false);
				f.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.RED);

			}
			if (ArchiAttivatiBPMN.containsKey(from + to)) {
				Integer i = ArchiAttivatiBPMN.get(from + to);
				f.getAttributeMap().remove(AttributeMap.SHOWLABEL);
				f.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
				f.getAttributeMap().put(AttributeMap.LABEL, i.toString());

			}
		}

		return bpmn;
	}

	private static void addsoujandsynctime(
			Map<Place, PerformanceData> performanceresult, Transition t,
			Map<String, String> archibpmnwithsyncperformance) {

		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inflows = t
		.getGraph().getInEdges(t);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : inflows) {
			Place source = (Place) edge.getSource();
			PerformanceData rs = performanceresult.get(source);
			if (rs.getSynchTime() > 0) {
				String sourjtime = calcolasojourntime(source, performanceresult);
				archibpmnwithsyncperformance.put(source.getLabel(),
						"Sync time: " + String.valueOf(rs.getSynchTime())
						+ "\n Souj time: " + sourjtime);
			} else {
				if (rs.getTime() >= 0) {
					String sourjtime = calcolasojourntime(source,
							performanceresult);
					archibpmnwithsyncperformance.put(source.getLabel(),
							"Souj time: " + sourjtime);
				}

			}

		}

	}

	private static String calcolasojourntime(PetrinetNode p,
			Map<Place, PerformanceData> performanceresult) {

		myFloat soujour = new myFloat();

		recursiveaddsoujourtime(soujour, performanceresult, p, 0);

		return String.valueOf(soujour.getFlo());
	}

	private static void recursiveaddsoujourtime(myFloat soujour,
			Map<Place, PerformanceData> performanceresult, PetrinetNode p, int i) {

		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> flow : p
				.getGraph().getInEdges(p)) {
			PetrinetNode sourcenode = flow.getSource();
			if (sourcenode instanceof Transition) {
				Transition source = (Transition) flow.getSource();
				//source.getLabel().endsWith("_join")
				if (source.getGraph().getInEdges(source).size()>1) {
					// prendo solo un ramo ora devo non mi devo fermare al primo
					// fork che incontro ma al successivo
					PetrinetNode newsource = source.getGraph()
					.getInEdges(source).iterator().next().getSource();
					recursiveaddsoujourtime(soujour, performanceresult,
							newsource, i++);
				} else {
					//!source.getLabel().endsWith("_fork")
					
					if (source.getGraph().getOutEdges(source).size()==1) {
						recursiveaddsoujourtime(soujour, performanceresult,
								source, i);
					} else {
						// se i è maggiore di 0 significa che sto calcolando un
						// tempo di soggiorno del branch che contiente almeno
						// un altro branch parallelo nel suo interno manca
						// ancora per i cicli
						if (i > 0) {
							recursiveaddsoujourtime(soujour, performanceresult,
									source, i--);
						}

					}

				}
			} else if (sourcenode instanceof Place) {
				Place source = (Place) flow.getSource();
				PerformanceData ps = performanceresult.get(source);
				if (ps != null) {
					if (ps.getTime() > 0) {
						soujour.add(ps.getTime());
					}
					recursiveaddsoujourtime(soujour, performanceresult, source,
							i);
				}

			}

		}

	}

	private static PerformanceData getPerfResult(Place preplace,
			Map<Place, PerformanceData> performanceresult) {
		for (Place p : performanceresult.keySet()) {
			if (p.getLabel().equals(preplace.getLabel())) {
				return performanceresult.get(p);
			}
		}

		return null;
	}

	public static BPMNDiagramExt exportConformancetoBPMN(
			BPMNDiagram bpmnoriginal, Petrinet net, ConformanceResult conformanceresult, Collection< Place> placeFlowCollection) {





		// clona bpmn
		BPMNDiagramExt bpmn = BPMNDiagramExtFactory.cloneBPMNDiagram(bpmnoriginal);



		Marking remaining = conformanceresult.getRemainingMarking();
		Map<Transition, Integer> transnotfit = conformanceresult.getMapTransition();
		Map<Arc, Integer> attivazionearchi = conformanceresult.getMapArc();

		Map<String, Integer> ArchiAttivatiBPMN = new HashMap<String, Integer>();
		Map<String, String> archibpmnwitherrorconformance = new HashMap<String, String>();

		// gli archi che attivo sul bpmn sono gli archi uscenti delle piazze
		// "arco"
		for (Place p : placeFlowCollection) {
			int att = 0;
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> egde : p
					.getGraph().getOutEdges(p)) {
				if (attivazionearchi.containsKey(egde)) {
					att += attivazionearchi.get(egde);

				}

			}
			ArchiAttivatiBPMN.put(p.getLabel(), att);
			
		
		}

		Map<Activity,Artifacts> mapActiArtic = new HashMap<Activity, Artifacts>();
		// transizioni che non fittano
		String ret = "<br/>";
	
		for (Transition t : net.getTransitions()) {
			if (!t.isInvisible()) {
				String fullname = t.getLabel();
				String task = (String) fullname.subSequence(0, fullname.indexOf("+"));
				String name = (String) fullname.substring(fullname.indexOf("+")+1);

				Activity activity = null;
				// cerco l'attività bpmn a cui collegare l'artifacts
				for (Activity a : bpmn.getActivities()) {
					if (a.getLabel().equals(task)) {
						activity = a;
						break;
					}
				}
				if(activity!=null) {
					String unsoundallert = "";
					Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = t
							.getGraph().getInEdges(t);
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
						Arc a = (Arc) edge;
						if (remaining.contains(a.getSource()))
							unsoundallert += ret + " Interrupted Flow\n";
						if (transnotfit.containsKey(t))
							unsoundallert += ret + "Unsound \"" + name + "\"\n";
					}
					if (activity != null && unsoundallert!="") {

						String label = "<html>"+ unsoundallert + "<html>";
						if(!mapActiArtic.containsKey(activity)){

							Artifacts art = null;
							if (activity.getParent() == null) {
								art = bpmn.addArtifacts(label,
										ArtifactType.TEXTANNOATION);
								bpmn.addFlowAssociation(art, activity);

							} 
							else {
								if (activity.getParent() instanceof SubProcess) {
									art = bpmn.addArtifacts(label,
											ArtifactType.TEXTANNOATION,
											activity.getParentSubProcess());
									bpmn.addFlowAssociation(art, activity,activity.getParentSubProcess());
								} 
								else {
									if (activity.getParent() instanceof Swimlane) {
										art = bpmn.addArtifacts(label,
												ArtifactType.TEXTANNOATION,
												activity.getParentSwimlane());
										bpmn.addFlowAssociation(art, activity,activity.getParentSwimlane());
									}
								}
							}

							mapActiArtic.put(activity, art);
						}
						else {
							Artifacts art = mapActiArtic.get(activity);
							label+=art.getLabel();
							art.getAttributeMap().remove(AttributeMap.LABEL);
							art.getAttributeMap().put(AttributeMap.LABEL, label);
						}
					}
				}
			}


			// cerco la transizione del fork
			//t.getLabel().endsWith("_fork")

			if (t.getGraph().getOutEdges(t).size()>1) {
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> p = t
						.getGraph().getOutEdges(t);

				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : p) {
					Place target = (Place) e.getTarget();
					if(remaining.contains(target)) {
						System.out.println(ret + " Fork internal failures");
						archibpmnwitherrorconformance.put(target.getLabel(),
								" Fork internal failures");
					}
				}
			}
		}

			// metto gli attraversamenti sugli archi bpmn
		for (Flow f : bpmn.getFlows()) {
			String from = f.getSource().getLabel();
			String to = f.getTarget().getLabel();
			if (ArchiAttivatiBPMN.containsKey(from + to)) {
				Integer i = ArchiAttivatiBPMN.get(from + to);
				if (i > 0) {
					f.getAttributeMap().put(AttributeMap.LABEL, i.toString());
					f.getAttributeMap().put(AttributeMap.TOOLTIP, i.toString());
					f.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
				}
			}
			// metto eventuali errore sul arco di fork
			if (archibpmnwitherrorconformance.containsKey(from + to)) {

				String flowerr = archibpmnwitherrorconformance.get(from + to);
				f.getAttributeMap().remove(AttributeMap.TOOLTIP);

				f.getAttributeMap().put(AttributeMap.TOOLTIP, flowerr);
				f.getAttributeMap().remove(AttributeMap.SHOWLABEL);
				f.getAttributeMap().put(AttributeMap.SHOWLABEL, true);
				f.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.RED);

			}
		}

		return bpmn;
	}

}

class myFloat {

	float flo = 0;

	public float getFlo() {
		return flo;
	}

	public void setFlo(float flo) {
		this.flo = flo;
		}

	public void add(float a) {
		flo += a;
	}

	myFloat() {
		flo = 0;
	}

}