package org.processmining.datamining.PlugIn;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

@Plugin(name = "Import Weka instances file Arff", parameterLabels = { "Filename" }, returnLabels = { "Instances" }, returnTypes = { Instances.class })
@UIImportPlugin(description = "Weka instances file Arff", extensions = { "arff" })
public class ImportWekaInstances  extends AbstractImportPlugin {

	/*protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("Weka instances file Arff", "arff");
	}*/
	
	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Import Weka instances file Arff")
	public Instances importFromStream(PluginContext context, InputStream input, String filename,
			long fileSizeInBytes) throws Exception {
	
		DataSource source = new DataSource(input);
		
		Instances data = source.getDataSet();
		return data;
	}
}



	