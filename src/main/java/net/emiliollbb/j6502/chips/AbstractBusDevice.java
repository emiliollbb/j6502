package net.emiliollbb.j6502.chips;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public abstract class AbstractBusDevice implements IBusDevice {
	protected String name;
	protected int startAddress;
	protected int size;
	protected int endAddress;
	public AbstractBusDevice(String name, int startAddress, int size) {
		this.name=name;
		this.startAddress=startAddress;
		this.size=size;
		this.endAddress=this.startAddress+this.size-1;
	}
	
	@Override
	public boolean isInRange(int address) {
		return address>=startAddress && address <= endAddress;
	}
	
	@Override
	public String toString() {
		return name+" -> "+printAddress(startAddress)+ " - "+printAddress(endAddress);
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
	
	public String getName() {
		return name;
	}
	
	private String printAddress(int address) {
		return String.format("0x%02X", address)+ "("+address+")";
	}

	protected abstract byte ioRead(int addr);
	protected abstract void ioWrite(int addr, byte data);
}
