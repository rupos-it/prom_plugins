package org.processmining.datamining.PlugIn;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode1;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

@Visualizer
@Plugin(name = "Visualizer Weka Desicion Tree", parameterLabels = "Visualizze decision tree Weka", returnLabels = "Decision Tree Weka Visualized", returnTypes = JComponent.class)

public class VisualizeDecisionTree {
	@PluginVariant(requiredParameterLabels = { 0 },variantLabel="Visualizer Instances Weka")
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Hind", email = "di.unipi.it")
	public JPanel visualize(UIPluginContext context, Classifier classifier) throws Exception {

		final javax.swing.JPanel jf = new javax.swing.JPanel(new BorderLayout());
		     //jf.setSize(500,400);
		     //jf.getContentPane().setLayout(new BorderLayout());
		     TreeVisualizer tv = new TreeVisualizer(null,((J48)classifier).graph(), new PlaceNode1());
		     //jf.getContentPane().add(tv, BorderLayout.CENTER);
		     
		     //jf.addWindowListener(new java.awt.event.WindowAdapter() {
		       //public void windowClosing(java.awt.event.WindowEvent e) {
		       //  jf.dispose();
		       //}
		     //});
		     
		    
		     tv.setSize(800,800);
		     jf.validate();
		     jf.add(tv,BorderLayout.CENTER);
		     jf.setVisible(true);
		     tv.fitToScreen();
		     
		return jf;
		
	}
}
