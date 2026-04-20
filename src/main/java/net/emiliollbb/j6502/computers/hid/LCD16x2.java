package net.emiliollbb.j6502.computers.hid;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class LCD16x2 extends Canvas {
	private static final long serialVersionUID = -8173391079739071510L;

	private Character[] characters;
	
	public LCD16x2() {
		characters = new Character[32];
		int x=30;
		int y=30;
		for(int i=0; i<characters.length; i++) {
			characters[i]=new Character(x,y);
			x+=32;
			if(i==15) {
				y+=49;
				x=30;
			}
		}
	}
	
	@Override
    public void paint(Graphics g) {
		g.setColor(Color.decode("#87ad34"));
		g.fillRect(0, 0, 569, 186);
		for(int i=0; i<characters.length; i++) {
			characters[i].paint(g);
		}
	}
	
	public int getWidth() {
		return 569;
	}
	
	public int getHeight() {
		return 186;
	}
	
	private class Character {
		private final Color PIXEL_OFF = Color.decode("#70902C");
		private final Color PIXEL_ON = Color.decode("#4A5E1D");
		private int x;
		private int y;
		
		public Character(int x, int y) {
			this.x=x;
			this.y=y;
		}
		
		public void paint(Graphics g) {
			g.setColor(Color.decode("#7b9d31"));
			g.fillRect(x, y, 29, 47);
			int xx=x;
			int yy=y;
			g.setColor(PIXEL_OFF);
			for(int f=0; f<8; f++) {
				for(int c=0; c<5; c++) {
					g.fillRect(xx, yy, 5, 5);
					xx+=6;
				}
				yy+=6;
				xx=x;
			}
		}
	}
}
