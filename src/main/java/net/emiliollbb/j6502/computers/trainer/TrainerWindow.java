package net.emiliollbb.j6502.computers.trainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.emiliollbb.j6502.chips.ScreenDriver;
import net.emiliollbb.j6502.computers.hid.VirtualScreen;

// Driver Class
public class TrainerWindow {
	
    // main function
    public static void main(String[] args) throws Exception
    {
        // Declaring a Frame and Label
        Frame frame = new Frame("Trainer");
        Trainer trainer = new Trainer();
        VirtualScreen screen = trainer.getScreen();

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