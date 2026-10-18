package net.emiliollbb.j6502.computers.durango;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

public class Main {
	// main function
    public static void main(String[] args) throws Exception
    {
        // Declaring a Frame and Label
    	JFrame frame = new JFrame("DURANGO-X");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        Durango durango = new Durango();
        frame.add(durango.getScreen(), BorderLayout.CENTER);
        durango.getScreen().setPreferredSize(new Dimension(durango.getScreen().getWidth(), durango.getScreen().getHeight()));
        Panel buttons = new Panel();
        buttons.setLayout(new BorderLayout());
        frame.add(buttons, BorderLayout.PAGE_END);
        frame.pack();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        	  @Override
        	  protected Void doInBackground() throws Exception {
        		  while(true) {
        			  long wait = 0;
        			  wait = durango.getCpu().timedStep();
        			  if(wait>0) {
	        			  Thread.sleep(Duration.ofMillis(wait));
	        			  durango.getScreen().repaint();
        			  }
        			  else {
        				  Thread.sleep(Duration.ofMillis(1000));
        			  }
        		  }
        	  }
        	};

        	
        // Using WindowListener for closing the window
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
