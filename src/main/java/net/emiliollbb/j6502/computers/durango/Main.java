package net.emiliollbb.j6502.computers.durango;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
