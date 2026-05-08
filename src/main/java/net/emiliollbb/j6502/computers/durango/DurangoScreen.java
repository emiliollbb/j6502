package net.emiliollbb.j6502.computers.durango;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

import net.emiliollbb.j6502.chips.RamChip;

public class DurangoScreen extends Canvas{
	private static final long serialVersionUID = -1643011527112910725L;
	private int zoom;
	private RamChip ram;
	
	public DurangoScreen(int zoom, RamChip ram) {
    	super();
    	this.zoom=zoom;
    	this.ram=ram;
    }
	
	protected void drawPixel(Graphics g, int x, int y, Color color) {
    	g.setColor(color);
    	g.fillRect(x*zoom, y*zoom, zoom, zoom);
    }
	
	protected void drawHiResPixel(Graphics g, int x, int y, Color color) {
    	g.setColor(color);
    	g.fillRect(x*zoom/2, y*zoom/2, zoom/2, zoom/2);
    }
	
	@Override
    public void paint(Graphics g) {
        super.paint(g);
        for(int i=0x6000; i<0x8000; i++) {
        	updatePixel(g,i);
        }        
    }
	
	/**
     * Draw supplied memory address in screen.
     */
    private void updatePixel(Graphics g, int addr) {
      // Read video mode [HiRes Invert S1 S0    RGB LED NC NC]
      byte videoModeReg = ram.peek(0xdf80);
      
      // Flags
      var hiRes = (videoModeReg & 0x80)>>7;      
      
      if(hiRes == 0) {
        drawColorPixel(g, addr);
      }
      else if(hiRes == 1) {
        drawHiResPixel(g, addr);
      }      
    }
    
    private String toHexValue(int value) {
    	return String.format("%02X", value);
    }
    
    /** 
     * Get rgb color to display pixel.
     */
    private Color getColor(int index) {
      // Color components
      int red=0, green=0, blue=0;

      // Durango palette
      switch(index) {
        case 0x00: red = 0x00; green = 0x00; blue = 0x00; break; // 0
        case 0x01: red = 0x00; green = 0xaa; blue = 0x00; break; // 1
        case 0x02: red = 0xff; green = 0x00; blue = 0x00; break; // 2
        case 0x03: red = 0xff; green = 0xaa; blue = 0x00; break; // 3
        case 0x04: red = 0x00; green = 0x55; blue = 0x00; break; // 4
        case 0x05: red = 0x00; green = 0xff; blue = 0x00; break; // 5
        case 0x06: red = 0xff; green = 0x55; blue = 0x00; break; // 6
        case 0x07: red = 0xff; green = 0xff; blue = 0x00; break; // 7
        case 0x08: red = 0x00; green = 0x00; blue = 0xff; break; // 8
        case 0x09: red = 0x00; green = 0xaa; blue = 0xff; break; // 9
        case 0x0a: red = 0xff; green = 0x00; blue = 0xff; break; // 10
        case 0x0b: red = 0xff; green = 0xaa; blue = 0xff; break; // 11
        case 0x0c: red = 0x00; green = 0x55; blue = 0xff; break; // 12
        case 0x0d: red = 0x00; green = 0xff; blue = 0xff; break; // 13
        case 0x0e: red = 0xff; green = 0x55; blue = 0xff; break; // 14
        case 0x0f: red = 0xff; green = 0xff; blue = 0xff; break; // 15
      }
	
      // Process invert flag
      if((ram.peek(0xdf80) & 0x40)>>6 == 1) {
        red = 0xff-red;
        green = 0xff - green;
        blue = 0xff - blue;
      }
	
      // Process RGB flag
      if((ram.peek(0xdf80) & 0x08)>>3 == 0) {
        red = (red + green + blue) / 3;
        green = red;
        blue = green;
      }
    
      return Color.decode('#'+toHexValue(red)+toHexValue(green)+toHexValue(blue));
    }
    
    /* Set current color in SDL HiRes mode */
    private Color getHiresColor(int color_index) {
      int color = color_index == 0 ? 0x00 : 0xff;

      // Process invert flag
      if((ram.peek(0xdf80) & 0x40)>>6 == 1) {
        color = 0xff-color;
      }
      
      String htmlColor = toHexValue(color);
      htmlColor = '#'+htmlColor+htmlColor+htmlColor;

      return Color.decode(htmlColor);
    }
    
    /**
     * Draw memory address in screen.
     */
    private void drawColorPixel(Graphics g, int addr) {
      // Calculate screen address
      int screenAddress = ((ram.peek(0xdf80) & 0x30)>>4)*0x2000;
      // Calculate screen y coord
      int y = (int)Math.floor((addr - screenAddress) * 2 / 128);
      // Calculate screen x coord
      int x = ((addr - screenAddress) *2) % 128;
      // Draw Left Pixel
      Color leftColor = getColor((ram.peek(addr) & 0xf0)>>4);
      drawPixel(g, x, y, leftColor);
      // Draw Right Pixel
      Color rightColor = getColor(ram.peek(addr) & 0x0f);
      drawPixel(g, x+1, y, rightColor);
    }
    
    private void drawHiResPixel(Graphics g, int addr) {
      // Calculate screen address
      int screenAddress = ((ram.peek(0xdf80) & 0x30)>>4)*0x2000;
      // Calculate screen y coord
      int y = (int) Math.floor((addr - screenAddress) * 8 / 256);
      // Calculate screen x coord
      int x = ((addr - screenAddress) *8) % 256;
      // 8 pixels to paint
      int b = ram.peek(addr);
      for(int i=0; i<8; i++) {
        // paint first pixel
    	drawPixel(g, x+i, y, getHiresColor(b & 0x80));  
        // Move to next pixel
        b <<= 1;
      }      
    }
    
    public int getWidth() {
		return 128*zoom;
	}
	
	public int getHeight() {
		return 128*zoom;
	}
}
