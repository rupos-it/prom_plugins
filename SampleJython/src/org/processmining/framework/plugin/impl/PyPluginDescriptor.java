package org.processmining.framework.plugin.impl;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginDescriptorID;
import org.processmining.framework.plugin.impl.AbstractPluginDescriptor;


public class PyPluginDescriptor extends AbstractPluginDescriptor {

	private PluginDescriptorID desc = new PyPluginDescID();

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
		return true;
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotationClass,
			int methodIndex) {
		return true;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (annotationClass == UITopiaVariant.class)
			return (T)new UITopiaVariant() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return UITopiaVariant.class;
				}
				
				@Override
				public String website() {
					return "www.rupos.it";
				}
				
				@Override
				public String uiLabel() {
					return "PyPlugin";
				}
				
				@Override
				public String pack() {
					return "Non lo so";
				}
				
				@Override
				public String email() {
					return "guancio@gmail.com";
				}
				
				@Override
				public String author() {
					return "Roberto guanciale";
				}
				
				@Override
				public String affiliation() {
					return "unipi";
				}
			};
		
		return null;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass,
			int methodIndex) {
		if (annotationClass == UITopiaVariant.class)
			return (T)new UITopiaVariant() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return UITopiaVariant.class;
				}
				
				@Override
				public String website() {
					return "www.rupos.it";
				}
				
				@Override
				public String uiLabel() {
					return "PyPlugin";
				}
				
				@Override
				public String pack() {
					return "Non lo so";
				}
				
				@Override
				public String email() {
					return "guancio@gmail.com";
				}
				
				@Override
				public String author() {
					return "Roberto guanciale";
				}
				
				@Override
				public String affiliation() {
					return "unipi";
				}
			};
		
		return null;
	}

	@Override
	public PackageDescriptor getPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "Python Script";
	}

	@Override
	public int getNumberOfMethods() {
		return 2;
	}

	@Override
	public List<Class<?>> getReturnTypes() {
		Vector<Class<?>> types = new Vector<Class<?>>();
		types.add(String.class);
		return types;
	}

	@Override
	public List<String> getReturnNames() {
		Vector<String> names = new Vector<String>();
		names.add("RITORNO");
		return names;
	}

	@Override
	public List<List<Class<?>>> getParameterTypes() {
		Vector<List<Class<?>>> params = new Vector<List<Class<?>>>();
		params.add(this.getParameterTypes(0));
		params.add(this.getParameterTypes(1));
		return params;
	}

	@Override
	public List<Class<?>> getParameterTypes(int methodIndex) {
		Vector<Class<?>> mparams = new Vector<Class<?>>();
		if (methodIndex == 0)
			return mparams;
		mparams.add(String.class);
		return mparams;
	}

	@Override
	public List<String> getParameterNames() {
		return this.getParameterNames(0);
	}

	@Override
	public List<String> getParameterNames(int methodIndex) {
		Vector<String> names = new Vector<String>();
		if (methodIndex == 1)
			names.add("PAR01");
		return names;
	}

	@Override
	public Class<?> getPluginParameterType(int methodIndex, int parameterIndex) {
		return this.getParameterTypes(methodIndex).get(parameterIndex);
	}

	@Override
	public String getPluginParameterName(int methodIndex, int parameterIndex) {
		return this.getParameterNames(methodIndex).get(parameterIndex);
	}

	@Override
	public PluginDescriptorID getID() {
		return this.desc ;
	}

	@Override
	public Class<? extends PluginContext> getContextType(int methodIndex) {
		return PluginContext.class;
	}

	@Override
	public Set<Class<?>> getTypesAtParameterIndex(int globalParameterIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIndexInParameterNames(int methodIndex,
			int methodParameterIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIndexInMethod(int methodIndex, int globalParameterIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMethodLabel(int methodIndex) {
		return "Python script methos label " + methodIndex;
	}

	@Override
	public int compareTo(PluginDescriptor plugin) {
		if (plugin == this)
			return 0;
		return 1;
	}

	@Override
	public boolean isUserAccessible() {
		return true;
	}

	@Override
	public boolean handlesCancel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMostSignificantResult() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	protected Object[] execute(PluginContext context, int methodIndex,
			Object... allArgs) throws Exception {
		System.out.println("Io Sono Guancio");
		if (methodIndex == 0)
			return new Object[]{"Guancio"};
		return new Object[]{"Guancio" + allArgs[0]};
	}

}
