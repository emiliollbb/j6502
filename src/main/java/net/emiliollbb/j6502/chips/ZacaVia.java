package net.emiliollbb.j6502.chips;

public class ZacaVia extends AbstractBusDevice {
	private static final int ver=10;
	public ZacaVia() {
		super("Zacatecas VIA", 0xBFF0, 16);
	}
	
	/**
	 * Register 0.Data of port B
	 */
	private byte dataB; //$0
	/**
	 * Register 1.Data of port A
	 */
	private byte dataA; //$1
	/**
	 * Register 2.Data direction of each pin of port B
	 */
	private byte dataDirB; //$2
	/**
	 * Register 3.Data direction of each pin of port A
	 */
	private byte dataDirA; //$3
	private byte t1cl; //$4
	private byte t1ch; //$5
	private byte t1ll; //$6
	private byte t1lh; //$7
	private byte acr; //$B
	private byte pcr; //$C
	private byte ier; //$D

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
			if (ver > 3) System.out.println("DATA DIR A: "+String.format("0x%02X", data));
			dataDirA=data;
			break;
		case 4:
			if (ver > 3) System.out.println("T1C-L: "+String.format("0x%02X", data));
			// Escribir en T1CL ($BFF4), que en realidad guardará el valor en T1LL ($BFF6)
			t1ll=data;
			break;
		case 5:
			if (ver > 3) System.out.println("T1C-H: "+String.format("0x%02X", data));
			// Escribir en T1CH ($BFF5), que iniciará la cuenta y copiará el valor en T1LH ($BFF7)
			t1lh=data;
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
		switch(addr) {
			case 0:
				// Con el puerto B no ocurre, si escribiste "1" se leerá "1", aunque la patilla esté echando humo.
				return dataB;
			case 1:
				// En el caso del puerto A, el valor leído es siempre el nivel presente en el pin
				return 0x00;
			case 2:
				return dataDirB;
			case 3:
				return dataDirA;
			case 14:
				return (byte)0x80;
		}
		return (byte)0x80;
	}
}
