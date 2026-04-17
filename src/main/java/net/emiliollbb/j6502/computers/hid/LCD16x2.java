package net.emiliollbb.j6502.computers.hid;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;

public class LCD16x2 extends Canvas {
	private static final long serialVersionUID = -8173391079739071510L;

	private Character[] characters;
	
	public LCD16x2() {
		characters = new Character[32];
	}
	
	@Override
    public void paint(Graphics g) {
		g.setColor(Color.decode("#87ad34"));
		g.fillRect(0, 0, 600, 150);
	}
	
	public int getWidth() {
		return 600;
	}
	
	public int getHeight() {
		return 150;
	}
	
	private class Character {
		
	}
}
