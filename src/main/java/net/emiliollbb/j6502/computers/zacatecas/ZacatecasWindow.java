package net.emiliollbb.j6502.computers.zacatecas;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.emiliollbb.j6502.computers.hid.LCD16x2;

// Driver Class
public class ZacatecasWindow {
	
    // main function
    public static void main(String[] args)
    {
        // Declaring a Frame and Label
        Frame frame = new Frame("Zacatecas");
        LCD16x2 lcd = new LCD16x2();

        // Adding Label and Setting the Size of the Frame
        frame.add(lcd);
        frame.setSize(lcd.getWidth(), lcd.getHeight());

        // Making the Frame visible
        frame.setVisible(true);
        lcd.setText1("Hello!");
        lcd.setText2("World!");

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