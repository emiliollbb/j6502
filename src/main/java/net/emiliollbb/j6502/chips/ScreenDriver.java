package net.emiliollbb.j6502.chips;

import java.awt.Color;

import net.emiliollbb.j6502.computers.hid.VirtualScreen;

public class ScreenDriver extends AbstractBusDevice {
	// http://eastfarthing.com/blog/2016-05-06-palette/
	private static final Color[] palette = new Color[] {
			Color.decode("#000000"), // black
			Color.decode("#d6a090"), // pinkish tan
			Color.decode("#fe3b1e"), // orangey red
			Color.decode("#a12c32"), // rouge
			Color.decode("#fa2f7a"), // strong pink
			Color.decode("#fb9fda"), // bubblegum pink
			Color.decode("#e61cf7"), // pink/purple
			Color.decode("#992f7c"), // warm purple
			Color.decode("#47011f"), // burgundy
			Color.decode("#051155"), // navy blue
			Color.decode("#4f02ec"), // blue/purple
			Color.decode("#2d69cb"), // medium blue
			Color.decode("#00a6ee"), // azure
			Color.decode("#6febff"), // robin’s egg
			Color.decode("#08a29a"), // blue/green
			Color.decode("#2a666a"), // dark aqua
			Color.decode("#063619"), // dark forest green
			Color.decode("#4a4957"), // charcoal grey
			Color.decode("#8e7ba4"), // greyish purple
			Color.decode("#b7c0ff"), // light periwinkle
			Color.decode("#acbe9c"), // greenish grey
			Color.decode("#827c70"), // medium grey
			Color.decode("#5a3b1c"), // brown
			Color.decode("#ae6507"), // umber
			Color.decode("#f7aa30"), // yellowish orange
			Color.decode("#f4ea5c"), // yellowish
			Color.decode("#9b9500"), // pea soup
			Color.decode("#566204"), // mud green
			Color.decode("#11963b"), // kelley green
			Color.decode("#51e113"), // toxic green
			Color.decode("#08fdcc"), // bright teal
			Color.decode("#ffffff")}; // white
	
	private byte[] vram;
	private VirtualScreen virtualScreen;
	
	public ScreenDriver(int startAddress, int size) {
		super("SCREEN", startAddress, size);
		vram=new byte[128*128];
	}

	@Override
	protected byte ioRead(int addr) {
		return vram[addr-startAddress];
	}

	@Override
	protected void ioWrite(int addr, byte data) {
		int relativeAddr = addr-startAddress;
		vram[relativeAddr]=data;
		virtualScreen.setPixel(relativeAddr%128, (int)relativeAddr/128, palette[data%palette.length]);
		virtualScreen.repaint();
	}

	public VirtualScreen getVirtualScreen() {
		return virtualScreen;
	}

	public void setVirtualScreen(VirtualScreen virtualScreen) {
		this.virtualScreen = virtualScreen;
	}
	
	
}
