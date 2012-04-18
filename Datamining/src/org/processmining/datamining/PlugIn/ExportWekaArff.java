package org.processmining.datamining.PlugIn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import weka.core.Instances;


@Plugin(name = "Export Instance to Arff File", parameterLabels = { "Instaces", "File" }, returnLabels = {}, returnTypes = {}, userAccessible = true)
@UIExportPlugin(description = "Arff files", extension = "arff")
public class ExportWekaArff {



	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Hind", email = "Hind")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export Instace to Arff File")
	public void export(PluginContext context, Instances instanceweka, File file) throws IOException {


		FileOutputStream out = new FileOutputStream(file);
		out.write(instanceweka.toString().getBytes());
		out.close();
	}


}
