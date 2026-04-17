package net.emiliollbb.j6502.computers.trainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.emiliollbb.j6502.chips.ScreenDriver;
import net.emiliollbb.j6502.computers.hid.VirtualScreen;

// Driver Class
public class TrainerWindow {
	
    // main function
    public static void main(String[] args)
    {
        // Declaring a Frame and Label
        Frame frame = new Frame("Trainer");
        VirtualScreen screen = new VirtualScreen(6);
        ScreenDriver driver = new ScreenDriver(0x4000, 128*128);
        driver.setVirtualScreen(screen);
        int addr=0x4000;
        for(int j=0; j<128; j++) {
        	for(int i=0; i<128; i++) {
	        	driver.poke(addr, (byte)i);
	        	addr++;
	        }
        }

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