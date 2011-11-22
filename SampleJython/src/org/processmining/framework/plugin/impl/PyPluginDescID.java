package org.processmining.framework.plugin.impl;

import org.processmining.framework.plugin.PluginDescriptorID;

public class PyPluginDescID implements PluginDescriptorID {
	@Override
	public int compareTo(PluginDescriptorID arg0) {
		if (arg0 == this)
			return 0;
		return 1;
	}

}
