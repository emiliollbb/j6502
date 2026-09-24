package net.emiliollbb.j6502.chips;

import net.emiliollbb.j6502.computers.hid.LCD16x2;

public class ZacaVia extends AbstractBusDevice {
	protected int verbose;
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
	private byte acr; //$B 11
	private byte pcr; //$C 12
	private byte ifr; //$D 13
	private byte ier; //$E 14
	private LCD16x2 lcd;

	@Override
	protected void ioWrite(int addr, byte data) {
		switch(addr) {
		case 0:
			if (verbose > 3) System.out.println("DATA B: "+Integer.toString(data&0x000000FF, 2)+" ("+String.format("0x%02X", data)+")");
			dataB=data;
			lcd.setE((dataB&0x000000FF&0x10)!=0);
			lcd.setLight((dataB&0x000000FF&0x20)!=0);
			//System.out.println("E's LED: "+((dataB&0x000000FF&0x40)!=0));
			break;
		case 1:
			if (verbose > 3) System.out.println("DATA A: "+String.format("0x%02X", data));
			dataA=data;
			lcd.setData((byte)(dataA&0x000000F0));
			lcd.setRS((dataA&0x00000001)==1);
			break;
		case 2:
			if (verbose > 3) System.out.println("DATA DIR B: "+String.format("0x%02X", data));
			dataDirB=data;
			break;
		case 3:
			if (verbose > 3) System.out.println("DATA DIR A: "+String.format("0x%02X", data));
			dataDirA=data;
			break;
		case 4:
			if (verbose > 3) System.out.println("T1C-L: "+String.format("0x%02X", data));
			// Escribir en T1CL ($BFF4), que en realidad guardará el valor en T1LL ($BFF6)
			t1ll=data;
			break;
		case 5:
			if (verbose > 3) System.out.println("T1C-H: "+String.format("0x%02X", data));
			// Escribir en T1CH ($BFF5), que iniciará la cuenta y copiará el valor en T1LH ($BFF7)
			t1lh=data;
			t1ch=t1lh;
			t1cl=t1ll;
			break;
		case 6:
			t1ll=data;
			break;
		case 7:
			t1lh=data;
			break;
		case 11:
			if (verbose > 3) System.out.println("ACR: "+String.format("0x%02X", data));
			acr=data;
			break;	
		case 12:
			if (verbose > 3) System.out.println("PCR: "+String.format("0x%02X", data));
			pcr=data;
			break;
		case 13:
			//ifr = ifr AND (NOT (x AND $7f))
			ifr = (byte)((ifr & (~(data & 0x7F)))&0x000000FF);
			if (verbose > 3) System.out.println("IFR: "+String.format("0x%02X", ifr));
			break;
		case 14:
			 // x<$80 ? ier = ier AND (NOT (x AND $7f)) : ier = ier OR x
			if((data&0x000000FF)<0x80) {
				ier = (byte)((ier & (~(data & 0x7f)))&0x000000FF);
			}
			else {
				ier = (byte)((ier | data)&0x000000FF);
			}
			if (verbose > 3) System.out.println("IER: "+String.format("0x%02X", ier));
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
				// RETURN (data_a AND ddra) OR (NOT ddra)
				return (byte)(((dataA & dataDirA) | ~dataDirA)&0x000000FF);
			case 2:
				return dataDirB;
			case 3:
				return dataDirA;
			case 6:
				return t1ll;
			case 7:
				return t1lh;
			case 11:
				return acr;
			case 12:
				return pcr;
			case 14:
				return (byte)((ier | 0x80)&0x000000FF);
		}
		return (byte)0xFF;
	}

	public LCD16x2 getLcd() {
		return lcd;
	}
	public void setLcd(LCD16x2 lcd) {
		this.lcd = lcd;
	}
	public int getVerbose() {
		return verbose;
	}
	public void setVerbose(int ver) {
		this.verbose = ver;
	}
	
}
