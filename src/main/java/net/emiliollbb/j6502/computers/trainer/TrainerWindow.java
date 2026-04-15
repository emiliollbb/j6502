package net.emiliollbb.j6502.computers.trainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Driver Class
public class TrainerWindow {
	
    // main function
    public static void main(String[] args)
    {
        // Declaring a Frame and Label
        Frame frame = new Frame("Basic Program");
        VirtualScreen screen = new VirtualScreen(6);

        // Adding Label and Setting the Size of the Frame
        frame.add(screen);
        frame.setSize(screen.getWidth(), screen.getHeight());

        // Making the Frame visible
        frame.setVisible(true);

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