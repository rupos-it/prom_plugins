package org.processmining.plugins.petrinet.addtransition;

import java.awt.Color;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


import com.fluxicon.slickerbox.factory.SlickerFactory;

public class AddEndTransitionPanel extends JPanel {

	/**
	 * 
	 */

  private static final long serialVersionUID = -2523136421962943318L;
	
  private JPanel panel;

	private JLabel title;
	private JLabel  lStop;
	
	private JTextField  tStop;

	public AddEndTransitionPanel() {
		this.init();
	}

	private void init() {
		SlickerFactory factory = SlickerFactory.instance();
		
		panel = factory.createRoundedPanel(25, Color.gray);

		this.title = factory.createLabel("Add artificial Transition");
		this.title.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		this.title.setForeground(new Color(40, 40, 40));
		this.title.setSize(50, 50);
		
		this.lStop = factory.createLabel("Add artificial end Transition");
		this.lStop.setForeground(new Color(40, 40, 40));

		
		

		
		this.tStop = new JTextField(80);
		this.tStop.setText("ArtificialEnd");
		this.tStop.setForeground(new Color(40, 40, 40));

		this.panel.setLayout(null);
		this.panel.add(this.title);
		
		this.panel.add(this.lStop);
		
		this.panel.add(this.tStop);

		this.panel.setBounds(0, 0, 700, 200);
		this.title.setBounds(10, 10, 200, 30);
		
		this.lStop.setBounds(20, 80, 150, 20);
		
		this.tStop.setBounds(200, 80, 200, 20);

		this.setLayout(null);
		this.add(panel);
		this.validate();
		this.repaint();
	}

	

	

	public String getStopTaskName() {
		return tStop.getText();
	}

}
