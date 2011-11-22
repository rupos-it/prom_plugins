import hacker.Lilly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.processmining.contexts.uitopia.UI;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;
import org.processmining.framework.plugin.impl.PyPluginDescriptor;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;


public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		UI.main(args);
		
		PluginManagerImpl plugins = (PluginManagerImpl) PluginManagerImpl.getInstance();
		plugins.addPlugin(new PyPluginDescriptor());
		
		
		BufferedReader terminal;
        PythonInterpreter interp;
        terminal = new BufferedReader(new InputStreamReader(System.in));
        System.out.println ("Hello");
        interp = new PythonInterpreter();
        /*
        interp.exec("import sys");
        interp.exec("print sys");
        interp.set("a", new PyInteger(42));
        interp.exec("print a");
        interp.exec("x = 2+2");
        PyObject x = interp.get("x");
        System.out.println("x: " + x);
        */
        PyObject localvars = interp.getLocals();
        interp.set("localvars", localvars);
        String codeString = "";
        String prompt = ">> ";
        while (true)
        {
            System.out.print (prompt);
            try
            {
                codeString = terminal.readLine();
                if (codeString.equals("exit"))
                {
                    System.exit(0);
                    break;
                }
                interp.exec(codeString);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception e) {
            	e.printStackTrace();
            }
        }
        System.out.println("Goodbye");
	}
}
