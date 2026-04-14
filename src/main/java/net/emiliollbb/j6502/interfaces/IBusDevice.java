package net.emiliollbb.j6502.interfaces;

public interface IBusDevice {
	byte peek(int addr);
	void poke(int addr, byte data);
	boolean isInRange(int addr);
}
