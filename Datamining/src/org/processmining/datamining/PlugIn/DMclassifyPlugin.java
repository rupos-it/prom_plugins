package org.processmining.datamining.PlugIn;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class DMclassifyPlugin {
	@Plugin(name = "Classify Istances to", 
	        parameterLabels = { "Istances", "Classifier" }, 
	        returnLabels = { "Weka Instances classified" }, 
	        returnTypes = { Instances.class }  )
	@UITopiaVariant(
			uiLabel="Weka classify Instances",
			affiliation = "University of Pisa",
			author = "Hind",
			email = "hind")		
			
	public Instances classifyIstances(PluginContext context, Instances istanze, Classifier classifier) throws Exception{
		
		double clsVal = 0;
		Instances instances = new Instances(istanze);
		instances.setClassIndex(instances.numAttributes() - 1);
		
		for( int i=0; i<instances.numInstances(); i++){
				clsVal = classifier.classifyInstance(instances.get(i));
				instances.get(i).setClassValue(clsVal);
		}
		
		return instances;
	}
}
