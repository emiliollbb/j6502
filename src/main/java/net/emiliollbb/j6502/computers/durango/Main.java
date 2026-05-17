package net.emiliollbb.j6502.computers.durango;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        frame.setLayout(new BorderLayout());
        Durango durango = new Durango();
        frame.add(durango.getScreen(), BorderLayout.NORTH);
        durango.getScreen().setPreferredSize(new Dimension(durango.getScreen().getWidth(), durango.getScreen().getHeight()));
        Button button = new Button("STEP");
        button.setPreferredSize(new Dimension(100, 30));
        frame.add(button, BorderLayout.SOUTH);
        frame.pack();
        button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				durango.getCpu().step();
				durango.getScreen().repaint();
			}
		});

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
        //worker.execute();

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
