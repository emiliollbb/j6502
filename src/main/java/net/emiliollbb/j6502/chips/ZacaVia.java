package net.emiliollbb.j6502.chips;

public class ZacaVia extends AbstractBusDevice {
	public ZacaVia() {
		super("Zacatecas VIA", 0xBFF0, 16);
	}
	
	/**
	 * Register 0.Data of port B
	 */
	private byte dataB;
	/**
	 * Register 1.Data of port A
	 */
	private byte dataA;
	/**
	 * Register 2.Data direction of each pin of port B
	 */
	private byte dataDirB;
	/**
	 * Register 3.Data direction of each pin of port A
	 */
	private byte dataDirA;

	@Override
	protected void ioWrite(int addr, byte data) {
		switch(addr) {
		case 0xBFF0:
			dataB=data;
			break;
		case 0xBFF1:
			dataA=data;
			break;
		case 0xBFF2:
			dataDirB=data;
			break;
		case 0xBFF3:
			dataDirA=data;
			break;
		}
	}
	
	@Override
	protected byte ioRead(int addr) {
		return (byte)0x80;
	}
}
