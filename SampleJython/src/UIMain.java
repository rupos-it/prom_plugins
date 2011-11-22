import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.deckfour.uitopia.api.event.TaskListener;
import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.uitopia.api.model.Action;
import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.main.MainView;
import org.deckfour.uitopia.ui.main.Overlayable;
import org.deckfour.uitopia.ui.overlay.TwoButtonOverlayDialog;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.model.ProMAction;
import org.processmining.contexts.uitopia.model.ProMPOResource;
import org.processmining.contexts.uitopia.packagemanager.PMFrame;
import org.processmining.contexts.uitopia.packagemanager.PMMainView;
import org.processmining.contexts.uitopia.packagemanager.PMPackage;
import org.processmining.contexts.uitopia.packagemanager.PMPackage.PMStatus;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.events.PackageManagerListener;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.util.CommandLineArgumentList;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class UIMain {

	@Plugin(name = "UITopia", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) {
		final UIContext globalContext = new UIContext();
		globalContext.initialize();
		final UITopiaController controller = new UITopiaController(globalContext);
		globalContext.setController(controller);
		globalContext.setFrame(controller.getFrame());
		controller.getFrame().setIconImage(ImageLoader.load("prom_icon_32x32.png"));
		controller.getFrame().setVisible(true);
		controller.getMainView().showWorkspaceView();
		controller.getMainView().getWorkspaceView().showFavorites();

		globalContext.startup();

		for (String cmd : commandlineArguments) {
			File f = new File(cmd);
			if (f.exists() && f.isFile()) {
				globalContext.getResourceManager().importResource(null, f);
			}
		}


		MainView mainView = controller.getMainView();
		JComponent parent = (JComponent) controller.getMainView().getParent();
		parent.remove(mainView);
		
		JSplitPane split = new JSplitPane();
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(mainView);
		

		PySystemState.initialize();
        PythonInterpreter pyi = new PythonInterpreter();   
        pyi.exec("import sys");    

        // you can pass the python.path to java to avoid hardcoding this
        // java -Dpython.path=/path/to/jythonconsole-0.0.6 EmbedExample
        pyi.exec("sys.path.append(r'jythonconsole-0.0.7/')");
        pyi.exec("from console import Console,main");


        
        pyi.set("globalContext", globalContext);

        PluginDescriptor importLogToFind = null;
		final PluginDescriptor importLog = importLogToFind;

		for (PluginDescriptor plugin : globalContext.getPluginManager().getAllPlugins()) {
			if ("Open XES Log File".equals(plugin.getName())) {
			}

			String pluginName = plugin.getName().
					replace(' ', '_').
					replace('-', '_').
					replace('/', '_').
					replaceAll("\\(", "").
					replaceAll("\\)", "").
					replaceAll("\\[", "").
					replaceAll("\\]", "").
					replaceAll("\\.", "").
					toLowerCase();
			
			System.out.println(pluginName);
			if ("".equals(pluginName.trim()))
				continue;
			
			
			pyi.set("pl_" + pluginName, plugin);

			String code = "";
			code += "def " + pluginName + "(p):\n";
			code += "   c = globalContext.getMainPluginContext()\n";
			code += "   c1 = c.createChildContext('Result of Import Log Error')\n";
			code += "   pl_"+pluginName+".invoke(0, c1, p)\n";
			code += "   c1.getResult().synchronize()\n";
			code += "   c.getProvidedObjectManager().createProvidedObjects(c1)\n";
			code += "   return c1.getResult().getResults()\n";
			
			System.out.println(pluginName);
			
			pyi.exec(code);
			importLogToFind = plugin;
		}

		pyi.set("manager", globalContext.getTaskManager());
        
		//split.setBottomComponent(pyScrollPane);
		
		pyi.set("split", split);

		PyObject localvars = pyi.getLocals();
		pyi.set("localvars", localvars);
		pyi.set("sample_file", "../examples/log3.mxml");
        
		pyi.exec("from javax.swing import JScrollPane\nconsole = Console(localvars)\nsplit.setBottomComponent(JScrollPane(console.text_pane))\n");
        //PyObject main = pyi.get("main");
        //main.__call__(pyi.getLocals());


/*			
		
        

		pyConsole.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				if ('\n' == e.getKeyChar()) {
					System.out.println("INVIO");
					String[] lines = pyConsole.getText().split("\n");
					
	                String codeString = lines[lines.length-1];
	                if (codeString.equals("exit"))
	                {
	                    System.exit(0);
	                }
	                interp.exec(codeString);
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
	*/	
		parent.add(split);
		controller.getFrame().repaint();
		controller.getFrame().validate();
		
		return controller;
	}

	public static void main(String[] args) throws Exception {
		Boot.boot(UIMain.class, UIPluginContext.class, args);
	}
}

class FirstTimeOverlay extends TwoButtonOverlayDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 494237962617678531L;

	public FirstTimeOverlay(Overlayable mainView) {
		super(mainView, "Starting ProM", "Cancel", "  OK  ",//
				new JLabel("<html>All packages have been downloaded.<BR>"
						+ "Please wait while starting ProM<BR>for the first time.<BR><BR>"
						+ "If this is the first time you run ProM on this computer, please be patient.</html>"));

		getCancelButton().setEnabled(true);
		getOKButton().setEnabled(false);

	}

	@Override
	public void close(boolean okayed) {
		if (!okayed) {
			System.exit(0);
		}
		super.close(okayed);
	}

}

class UIPackageManagerListener implements PackageManagerListener {

	private final String[] args;
	private final PMFrame frame;
	private boolean done = false;

	public UIPackageManagerListener(PMFrame frame, String[] args) {
		this.frame = frame;
		this.args = args;
	}

	public void exception(Throwable t) {
	}

	public void exception(String exception) {
	}

	public void finishedInstall(String packageName, File folder, PackageDescriptor pack) {
	}

	public void sessionComplete(boolean error) {
		synchronized (this) {
			done = true;
			this.notifyAll();
		}
		PackageManager.getInstance().removeListener(this);
		showOverlayAfterInstall();
	}

	public void sessionStart() {
	}

	public void startDownload(String packageName, URL url, PackageDescriptor pack) {
	}

	public void startInstall(String packageName, File folder, PackageDescriptor pack) {
	}

	public boolean isDone() {
		return done;
	}

	private void showOverlayAfterInstall() {
		PMMainView overlayable = frame.getController().getMainView();

		FirstTimeOverlay dialog = new FirstTimeOverlay(overlayable);

		overlayable.showOverlay(dialog);
		frame.saveConfig();

		try {
			Boot.boot(UIMain.class, UIPluginContext.class, args);
			Boot.setLatestReleaseInstalled();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		} finally {
			frame.setVisible(false);
		}

	}

}
