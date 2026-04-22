package net.emiliollbb.j6502.chips;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public abstract class AbstractBusDevice implements IBusDevice {
	int startAddress;
	int size;
	int endAddress;
	public AbstractBusDevice(int startAddress, int size) {
		this.startAddress=startAddress;
		this.size=size;
		this.endAddress=this.startAddress+this.size;
	}
	
	@Override
	public boolean isInRange(int address) {
		return address>=startAddress && address <= endAddress;
	}
	
	public byte peek(int addr) {
		if(!isInRange(addr)) {
			throw new IndexOutOfBoundsException(addr);
		}
		return ioRead(addr-startAddress);
	}
	
	public void poke(int addr, byte data) {
		if(!isInRange(addr)) {
			throw new IndexOutOfBoundsException(addr);
		}
		ioWrite(addr-startAddress, data);
	}
	
	protected abstract byte ioRead(int addr);
	protected abstract void ioWrite(int addr, byte data);
}
