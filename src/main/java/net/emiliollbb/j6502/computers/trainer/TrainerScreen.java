package net.emiliollbb.j6502.computers.trainer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
 
public class TrainerScreen  extends Canvas {
    private static final long serialVersionUID = 6458825515816507486L;
    
    private int zoom;
    private Color[][] pic;
    
    public TrainerScreen(int zoom) {
    	super();
    	this.zoom=zoom;
    	pic = new Color[128][128];
    	for(int i=0; i<128; i++) {
    		pic[i] = new Color[128];
    	}
    	
    	for(int i=0; i<128; i++) {
    		for(int j=0; j<128; j++) {
    			pic[i][j]=(j+i)%2==0?Color.RED:Color.GREEN;
    		}
    	}
    }
 
    void drawLines(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
 
        g2d.drawLine(120, 50, 360, 50);
 
        g2d.draw(new Line2D.Double(59.2d, 99.8d, 419.1d, 99.8d));
 
        g2d.draw(new Line2D.Float(21.50f, 132.50f, 459.50f, 132.50f));
 
    }
    
    protected void drawPixel(Graphics g, int x, int y, Color color) {
    	g.setColor(color);
    	g.fillRect(x*zoom, y*zoom, zoom, zoom);
    }
 
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawLines(g);
        
        for(int i=0; i<128; i++) {
    		for(int j=0; j<128; j++) {
    			drawPixel(g, i, j, pic[i][j]);
    		}
    	}
    }

	public int getWidth() {
		return 128*zoom;
	}
	
	public int getHeight() {
		return 128*zoom;
	}

    
    
}