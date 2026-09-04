package net.emiliollbb.j6502.chips;

public class ZacaVia extends AbstractBusDevice {
	private static final int ver=10;
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
	private byte ier;
	private byte t1cl;
	private byte t1ch;
	private byte acr;
	private byte pcr;

	@Override
	protected void ioWrite(int addr, byte data) {
		switch(addr) {
		case 0:
			if (ver > 3) System.out.println("DATA B: "+String.format("0x%02X", data));
			dataB=data;
			break;
		case 1:
			if (ver > 3) System.out.println("DATA A: "+String.format("0x%02X", data));
			dataA=data;
			break;
		case 2:
			if (ver > 3) System.out.println("DATA DIR B: "+String.format("0x%02X", data));
			dataDirB=data;
			break;
		case 3:
			if (ver > 3) System.out.println("DATA DIR B: "+String.format("0x%02X", data));
			dataDirA=data;
			break;
			
		case 4:
			if (ver > 3) System.out.println("T1C-L: "+String.format("0x%02X", data));
			t1cl=data;
			break;
		case 5:
			if (ver > 3) System.out.println("T1C-H: "+String.format("0x%02X", data));
			t1ch=data;
			break;
		case 11:
			if (ver > 3) System.out.println("ACR: "+String.format("0x%02X", data));
			acr=data;
			break;	
		case 12:
			if (ver > 3) System.out.println("PCR: "+String.format("0x%02X", data));
			pcr=data;
			break;
		case 14:
			if (ver > 3) System.out.println("IER: "+String.format("0x%02X", data));
			ier=data;
			break;	
		}
	}
	
	@Override
	protected byte ioRead(int addr) {
		return (byte)0x80;
	}
}
