package org.processmining.datamining.PlugIn;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class GenerateClassifier {
	  @Plugin(name = "Istances to Classifier", 
	        parameterLabels = { "Istances" }, 
	        returnLabels = { "Weka Classifier" }, 
	        returnTypes = { Classifier.class }  )
	@UITopiaVariant(
			uiLabel="Generate Weka Classifier ",
			affiliation = "University of Pisa",
			author = "Hind",
			email = "hind")
			
    public Object generateclassifier(PluginContext contex, Instances instances) throws Exception{
		  
		  instances.setClassIndex(instances.numAttributes() - 1);
		  
		  Classifier classifier = new J48();
		  classifier.buildClassifier(instances);
		  
		  return classifier;
	  }
}
