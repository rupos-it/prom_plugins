package org.processmining.plugins.log.logadd;

import java.util.Date;
import java.util.Iterator;


import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;


public class LogAddEvent {

	@Plugin(name = "Add End Artificial Events", parameterLabels = { "Log" }, returnLabels = { "Altered log" }, returnTypes = { XLog.class }, userAccessible = true, help = "Adds an artificial  end task to every trace in the log file")
	@UITopiaVariant(affiliation = "Department of Computer Science University of Pisa", author = "R.Guanciale,G.Spagnolo et al.", email = "spagnolo@di.unipi.it", pack = "PetriNetReplayAnalysis")
	public XLog addEvents(PluginContext context, XLog oldLog) {


		context.getFutureResult(0).setLabel(XConceptExtension.instance().extractName(oldLog));

		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(oldLog.size());
		context.getProgress().setIndeterminate(false);
		context.getProgress().setValue(0);

		XAttributeMap logattlist = copyAttMap(oldLog.getAttributes());
		XLog newLog = new XLogImpl(logattlist);
		for (int i = 0; i < oldLog.size(); i++) {
			XTrace oldTrace = oldLog.get(i);
			XTrace newTrace = new XTraceImpl(copyAttMap(oldTrace.getAttributes()));
			for (int j = 0; j < oldTrace.size(); j++) {
				XEvent oldEvent = oldTrace.get(j);
				XEvent newEvent = new XEventImpl(copyAttMap(oldEvent.getAttributes()));
				newTrace.add(newEvent);
			}

			Date time = new Date();
			try {
				time = ((XAttributeTimestampImpl) oldTrace.get(oldTrace.size() - 1).getAttributes()
						.get("time:timestamp")).getValue();
				time.setTime(time.getTime() + 1);
			} catch (Exception ex) {}

			newTrace.add(makeEvent("ArtificialEnd", time));

			newLog.add(newTrace);
			context.getProgress().inc();
		}
		return newLog;
	}

	private XEvent makeEvent(String name, Date time) {
		XAttributeMap attMap = new XAttributeMapImpl();
		putLiteral(attMap, "concept:name", name);
		putLiteral(attMap, "lifecycle:transition", "complete");
		putLiteral(attMap, "org:resource", "artificial");
		putTimestamp(attMap, "time:timestamp", time);
		XEvent newEvent = new XEventImpl(attMap);
		return newEvent;
	}

	private void putLiteral(XAttributeMap attMap, String key, String value) {
		attMap.put(key, new XAttributeLiteralImpl(key, value));
	}

	private void putTimestamp(XAttributeMap attMap, String key, Date value) {
		attMap.put(key, new XAttributeTimestampImpl(key, value));
	}

	private static XAttributeMap copyAttMap(XAttributeMap srcAttMap) {
		XAttributeMap destAttMap = new XAttributeMapImpl();
		Iterator<XAttribute> attit = srcAttMap.values().iterator();
		while (attit.hasNext()) {
			XAttribute att = attit.next();
			String key = att.getKey();
			att = (XAttribute) att.clone();
			destAttMap.put(key, att);
		}
		return destAttMap;
	}


}
