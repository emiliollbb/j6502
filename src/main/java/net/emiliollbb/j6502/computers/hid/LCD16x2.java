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
		private int x;
		private int y;
		
		public Character(int x, int y) {
			this.x=x;
			this.y=y;
		}
		
		public void paint(Graphics g) {
			g.setColor(Color.decode("#7b9d31"));
			g.fillRect(x, y, 29, 47);
		}
	}
}
