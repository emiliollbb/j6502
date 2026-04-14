package net.emiliollbb.j6502.chips;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public class RamChip extends AbstractBusDevice implements IBusDevice {
	private byte[] sram;
	
	public RamChip(int startAddress, int size) {
		super(startAddress, size);
		sram=new byte[size];
	}

	@Override
	protected byte ioRead(int addr) {
		return sram[addr];
	}

	@Override
	protected void ioWrite(int addr, byte data) {
		sram[addr]=data;
	}

}
