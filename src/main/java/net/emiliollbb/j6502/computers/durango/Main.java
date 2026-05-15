package net.emiliollbb.j6502.computers.durango;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;

import javax.swing.SwingWorker;

public class Main {
	// main function
    public static void main(String[] args) throws Exception
    {
        // Declaring a Frame and Label
        Frame frame = new Frame("DURANGO-X");
        Durango durango = new Durango();
        frame.add(durango.getScreen());
        frame.setSize(durango.getScreen().getWidth(), durango.getScreen().getHeight());

        // Making the Frame visible
        frame.setVisible(true);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        	  @Override
        	  protected Void doInBackground() throws Exception {
        		  while(true) {
        			  durango.getCpu().step();
        			  Thread.sleep(Duration.ofMillis(1000));
        		  }
        	  }
        	};
        worker.execute();

        // Using WindowListener for closing the window
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
    }
}
