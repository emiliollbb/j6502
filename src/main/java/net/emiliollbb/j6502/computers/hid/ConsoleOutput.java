package net.emiliollbb.j6502.computers.hid;

import net.emiliollbb.j6502.chips.AbstractBusDevice;

public class ConsoleOutput extends AbstractBusDevice {
	
	public ConsoleOutput(int startAddress) {
		super("CONSOLE", startAddress, 1);
	}

	@Override
	protected byte ioRead(int addr) {
		return 0;
	}

	@Override
	protected void ioWrite(int addr, byte data) {
		System.out.println("OUTPUT: "+String.format("0x%02X", data));
	}

	
}
