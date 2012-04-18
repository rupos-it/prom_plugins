package org.processmining.datamining.PlugIn;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import weka.classifiers.Classifier;



@Plugin(name = "Import Weka Classifier model file", parameterLabels = { "Filename" }, returnLabels = { "Classifier" }, returnTypes = { Classifier.class })
@UIImportPlugin(description = "Weka Classifier model file", extensions = { "model" })

public class ImportWekaModel  extends AbstractImportPlugin {
	
	/*protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("Weka Classifier file model", "model");
	}*/

	/*http://weka.wikispaces.com/Serialization*/
	protected Classifier importFromStream(PluginContext context, InputStream input, String filename,
			long fileSizeInBytes) throws Exception {
		
		 Classifier classifier = (Classifier) weka.core.SerializationHelper.read(input);

		 
		return classifier;
	}
}
