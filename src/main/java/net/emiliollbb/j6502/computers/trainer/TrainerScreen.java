package net.emiliollbb.j6502.computers.trainer;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
 
public class TrainerScreen  extends Canvas {
    private static final long serialVersionUID = 6458825515816507486L;
    
    private int zoom;
    
    public TrainerScreen(int zoom) {
    	super();
    	this.zoom=zoom;
    }
 
    void drawLines(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
 
        g2d.drawLine(120, 50, 360, 50);
 
        g2d.draw(new Line2D.Double(59.2d, 99.8d, 419.1d, 99.8d));
 
        g2d.draw(new Line2D.Float(21.50f, 132.50f, 459.50f, 132.50f));
 
    }
 
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawLines(g);
    }

	public int getWidth() {
		return 128*zoom;
	}
	
	public int getHeight() {
		return 128*zoom;
	}

    
    
}