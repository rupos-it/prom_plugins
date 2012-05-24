package org.processmining.plugins.bpmn.exporting;

import java.awt.Color;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

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

import java.util.Collection;

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
		
		//tempo di attesa prima dell'attivazione di un task BPMN
		Map<Activity, Float> MapWait = new HashMap<Activity, Float>();
		//tempo di esecuzione di un task BPMN
		Map<Activity, Float> MapExc = new HashMap<Activity, Float>();
		//tempo totale di un task BPMN
		Map<Activity, Float> MapTot = new HashMap<Activity, Float>();
		//task attivati
		Map<Activity, Integer> TaskAttivati = new HashMap<Activity, Integer>();

		for (Place p : net.getPlaces()) {
			String task;
			try {
				task = (String) p.getLabel().subSequence(0, p.getLabel().indexOf("+"));
			}
			catch (StringIndexOutOfBoundsException e) {
				task = null;
			}
			Activity activity = null;
			// cerco l'attività bpmn a cui collegare l'artifacts
			for (Activity a : bpmn.getActivities()) {
				if (task != null && a.getLabel().equals(task)) {
					activity = a;
					break;
				}
			}
			
			PerformanceData ps = getPerfResult(p, Performanceresult.getList());
			if (ps != null) {
				if(activity!=null) {
					if(MapTot.containsKey(activity))
						MapTot.put(activity, MapTot.get(activity)+ps.getTime());
					else
						MapTot.put(activity, ps.getTime());
					// il tempo di esecuzione è nella piazza running
					if(p.getLabel().endsWith(BPMNtoPNUI.run)) {
						//	esaminiamo gli archi della stella entrante della piazza
						Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
							edges = p.getGraph().getInEdges(p);
						//numero esecuzioni (numero attivazioni dell'arco start -> running)
						int count = 1;
						for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : 
								edges) {
							Arc a = (Arc) edge;
							if (maparc.containsKey(a) && a.getSource().getLabel().
									endsWith(BPMNtoPNUI.st))
								count = maparc.get(a);
						}
						//si considera il tempo medio di esecuzione
						MapExc.put(activity,ps.getTime()/count);
					}
				}
				// tempo di attesa attivazione task:
				// una piazza è di attivazione per un task T quando non appartiene a T
				// e almeno una transizione del suo postset appartiene a T
				else { 
					Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
					outedges = p.getGraph().getOutEdges(p);
					for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outedge : 
							outedges) {
						Arc arc = (Arc)outedge;
						String next = (arc.getTarget().getLabel());
						try {
							task = (String) next.subSequence(0, next.indexOf("+"));
						}
						catch (StringIndexOutOfBoundsException e) {
							task = null;
						}
						// cerco l'attività bpmn a cui collegare l'artifacts
						for (Activity a : bpmn.getActivities()) {
							if (task != null && a.getLabel().equals(task)) {
								activity = a;
								break;
							}
						}
						if(activity!=null) {
							// calcolo numero di attivazioni del task
							if(maparc.containsKey(arc)) {
								if(TaskAttivati.containsKey(activity))
									TaskAttivati.put(activity, TaskAttivati.get(activity) + maparc.get(arc));
								else
									TaskAttivati.put(activity, maparc.get(arc));
							}
							if(!MapWait.containsKey(activity)) {
								//	esaminiamo gli archi della stella entrante della piazza
								Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
									edges = p.getGraph().getInEdges(p);
								/* totale attivazioni degli archi entranti in p */
								int act = 0;
								for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> 
									edge : edges) {
									Arc a = (Arc) edge;
									if (maparc.containsKey(a))
										act += maparc.get(a);
								}
								if(act!=0)
									MapWait.put(activity, ps.getTime()/act);
							}
						}
					}
				}
			}		
		}
		for (Activity a : bpmn.getActivities()) {
			float actTime = 0, excTime = 0, totTime = 0;
			if(MapWait.containsKey(a))
				actTime = MapWait.get(a);
			if(MapExc.containsKey(a))
				excTime = MapExc.get(a);
			if(ArchiAttivatiBPMN.containsKey(a.getLabel()))
				ArchiAttivatiBPMN.get(a.getLabel());
			if(MapTot.containsKey(a))
				totTime = MapTot.get(a)/TaskAttivati.get(a);
			String text = "Average Activation Time: " + secondsToString((long)actTime) + "<br/>Average Execution Time: " + 
					secondsToString((long)excTime) + "<br/>Average Total Time: " + secondsToString((long)totTime) + "<br/>";
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


		// remaining tokens
		Marking remaining = conformanceresult.getRemainingMarking();
		// transizioni che non fittano
		Map<Transition, Integer> transnotfit = conformanceresult.getMapTransition();
		// archi attivati
		Map<Arc, Integer> attivazionearchi = conformanceresult.getMapArc();
		// piazze esaminate
		Set<Place> examined = new HashSet<Place>();

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
		String ret = "<br/>";
	
		for (Transition t : net.getTransitions()) {
			String unsoundallert = "";					
			String fullname = t.getLabel(), task , tname;
			try {
				task = (String) fullname.subSequence(0, fullname.indexOf("+"));
			}
			catch (StringIndexOutOfBoundsException e) {
				task = null;
			}
			if(task==null)
				tname=null;
			else
				tname = (String) fullname.substring(fullname.indexOf("+")+1);
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = t
					.getGraph().getInEdges(t);

			Activity activity = null;
			// cerco l'attività bpmn a cui collegare l'artifacts
			for (Activity a : bpmn.getActivities()) {
				if (task!= null && a.getLabel().equals(task)) {
					activity = a;
					break;
				}
			}
			
			if(activity!=null && tname!=null) {
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					Arc a = (Arc) edge;

					/* riporto missing token */
					if (transnotfit.containsKey(t)) {
						double perc = trunc((double)transnotfit.get(t) * 100 / (double)attivazionearchi.get(a), 2);
						unsoundallert += ret + perc + "% Unsound \"" + tname +  "\"\n";							
					}
				}

				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					Arc a = (Arc) edge;
					/* riporto remaining token */

					/* è sempre una piazza, perché si sta esaminando il preset di una transizione */
					Place p  = (Place)a.getSource();
					String pname = (String) p.getLabel().substring(p.getLabel().indexOf("+")+1);
					if(pname.equals(p.getLabel()))
						pname = null;
					if (remaining.contains(p) && !examined.contains(p)) {
						Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> 
							inedges = p.getGraph().getInEdges(p);
						/* totale attivazioni degli archi entranti in p */
						float act = 0;
						for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> 
								inedge : inedges)
							if(attivazionearchi.containsKey((Arc)inedge))
								act += attivazionearchi.get((Arc)inedge);

						double perc = trunc((double)remaining.occurrences(p) * 100 / (double)act, 2);
						
						unsoundallert += ret + perc + "% Interrupted flow ";
						if(pname==null)
							unsoundallert += "before \"" + tname + "\"\n";
						else
							unsoundallert += "while " + pname + "\n";
						examined.add(p);
					}
				}
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
					art.getAttributeMap().put(AttributeMap.LABEL, label);					}
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

	private static double trunc (double number, int cifre) {
		int mult = (int)Math.pow(10, cifre);
		int num2 = (int)(number * mult);
		return (double)((double)num2/(double)mult);
		
	}

	private static String secondsToString(long elapsedTime){
        System.out.println(""+elapsedTime);
        String format = String.format("%%0%dd", 2);  
        elapsedTime = elapsedTime / 1000;  
        String seconds = String.format(format, elapsedTime % 60);  
        String minutes = String.format(format, (elapsedTime % 3600) / 60);  
        String hours = String.format(format, (elapsedTime / 3600)%24); 

        String day = String.format(format, (elapsedTime / 86400));  // % 30
        
        String stime =  hours + ":" + minutes + ":" + seconds; 
        if(!day.equals("00"))
                 stime = "Day: "+ day +" "+ stime;
        
        return stime;  
        
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