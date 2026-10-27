package net.emiliollbb.j6502.computers.zacatecas;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

// Driver Class
public class ZacatecasWindow {

	// main function
	public static void main(String[] args) throws Exception
	{
		// Declaring a Frame and Label
		JFrame frame = new JFrame("Zacatecas");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Zacatecas zacatecas = new Zacatecas();

		// Adding Label and Setting the Size of the Frame
		frame.add(zacatecas.getLcd(), BorderLayout.CENTER);
		zacatecas.getLcd().setPreferredSize(new Dimension(zacatecas.getLcd().getWidth(), zacatecas.getLcd().getHeight()));
		frame.setSize(zacatecas.getLcd().getWidth(), zacatecas.getLcd().getHeight());
		Panel buttonsPanel = new Panel();
		String[] bLabels = new String[] {"Left","Down","Up","Right","SELECT","START","B","A"};
		Button[] buttons = new Button[8];
		for(int i=0; i<8; i++) {
			buttons[i] = new Button(bLabels[i]);
			buttons[i].setPreferredSize(new Dimension(Math.max(15*bLabels[i].length(),30), 30));
			buttonsPanel.add(buttons[i]);
		}
		frame.add(buttonsPanel, BorderLayout.PAGE_END);
		frame.pack();

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				int i=0;
				while(true) {
//					long wait = 0;
//					wait = zacatecas.getCpu().timedStep();
//					if(wait>0) {
//						Thread.sleep(Duration.ofMillis(wait));
//					}
					zacatecas.getCpu().step();
					if(i++%100==0) {
						zacatecas.getLcd().repaint();
					}
				}
			}
		};

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				worker.execute();
			}
		});

		// Making the Frame visible
		frame.setVisible(true);
	}
}