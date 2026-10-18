package net.emiliollbb.j6502.computers.zacatecas;

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
        //zacatecas.getCpu().runUntilBrk();

        // Adding Label and Setting the Size of the Frame
        frame.add(zacatecas.getLcd());
        frame.setSize(zacatecas.getLcd().getWidth(), zacatecas.getLcd().getHeight());

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
      	  @Override
      	  protected Void doInBackground() throws Exception {
      		  while(true) {
      			  long wait = 0;
      			  wait = zacatecas.getCpu().timedStep();
      			  if(wait>0) {
	        			Thread.sleep(Duration.ofMillis(wait));
	        			zacatecas.getLcd().repaint();
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