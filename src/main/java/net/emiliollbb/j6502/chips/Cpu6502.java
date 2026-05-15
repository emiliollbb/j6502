package net.emiliollbb.j6502.chips;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public class Cpu6502 implements Runnable {
	protected byte a; 
	protected byte x;
	protected byte y;
	protected byte s;
	protected byte p;			// 8-bit registers
	protected int pc;					// program counter
	protected int ver;
	protected int dec;
	protected int speed;
	protected int page;
	
	protected List<IBusDevice> busDevices;
	
	public Cpu6502(int speed) {
		pc=0;
		this.speed=speed;
		busDevices=new LinkedList<>();
	}
	
	public Cpu6502(int speed, List<IBusDevice> devices) {
		this(speed);
		busDevices.addAll(devices);
	}
	
	public List<IBusDevice> getBusDevices() {
		return busDevices;
	}

	public void setVerbose(int verbose) {
		this.ver = verbose;
	}
	
	protected String printByte(byte b) {
		return String.format("0x%02X", b)+ "("+b+")";
	}
	protected String printByte(int b) {
		return String.format("0x%02X", b)+ "("+b+")";
	}
	
	protected byte peek(int addr) {
		if(ver>5) System.out.print("peek "+printByte(addr));
		byte value= busDevices.stream().filter(d -> d.isInRange(addr)).findFirst().get().peek(addr);
		if(ver>5) System.out.println(" -> "+printByte(value));
		return value;
	}
	protected void poke(int addr, byte data) {
		if(ver>5) System.out.println("poke "+printByte(addr)+" -> "+printByte(data));
		busDevices.stream().filter(d -> d.isInRange(addr)).findFirst().get().poke(addr, data);
	}
	
	public void listDevices() {
		System.out.println("DEVICES\n----------------------------------------------");
		busDevices.stream().forEach(d -> System.out.println(d));
		System.out.println("----------------------------------------------");
	}
	
	public void reset() {
		pc = getWord(peek(0xFFFC), peek(0xFFFD));	// RESET vector
		if(ver>1) System.out.println("RESET! "+printByte(pc));
	}
	
	private int getWord(byte a, byte b) {
		return  a & 0x000000FF | (b & 0x0000FF)<<8;
	}
	
	public byte getA() {
		return a;
	}
	public byte getX() {
		return x;
	}
	public byte getY() {
		return y;
	}
	public int getPc() {
		return pc;
	}
	public byte getP() {
		return p;
	}
	public byte getS() {
		return s;
	}
	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@Override
	public void run() {
		int cycles;
		reset();
		do {
			Instant start = Instant.now();
			Instant end = Instant.now();
			cycles = step();
			long expectedTime=cycles*1000/speed;
			long actualTime=start.until(end, ChronoUnit.MILLIS);
			long sleepTime=expectedTime-actualTime;
			System.out.println("Sleep time: "+sleepTime);
			try {
				Thread.sleep(Duration.ofMillis(sleepTime));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}while(cycles>0);
	}

	/* execute a single opcode, returning cycle count */
	public int step() {
		int cycles = 2;			// base cycle count
		page = 0;			// page boundary flag, for speed penalties
		byte opcode, temp;
		int adr;

		opcode = peek(pc++);	// get opcode and point to next one (or operand)
		if(ver>5) System.out.println("OPCODE: "+printByte(opcode));
		switch(opcode) {
		/** INDEX REGISTERS MANIPULATION **/
		/* *** LDX: Load Index X with Memory *** */
		case (byte) 0xA2:
			if (ver > 3) System.out.println("[LDX#] "+printByte(x));
			x = peek(pc++);
			bits_nz(x);
			break;
		case (byte) 0xA6:
			if (ver > 3) System.out.println("[LDXz]");
			x = peek(peek(pc++));
			bits_nz(x);
			cycles = 3;
			break;
		case (byte) 0xB6:
			if (ver > 3) System.out.println("[LDXzy]");
			x = peek(am_zy());
			bits_nz(x);
			cycles = 4;
			break;	
		case (byte) 0xAE:
			if (ver > 3) System.out.println("[LDXa]");
			x = peek(am_a());
			bits_nz(x);
			cycles = 4;
			break;
		case (byte) 0xBE:
			if (ver > 3) System.out.println("[LDXy]");
			x = peek(am_ay());
			bits_nz(x);
			cycles = 4 + page;
			break;
		/* *** LDY: Load Index Y with Memory *** */
		case (byte) 0xA0:
			if (ver > 3) System.out.println("[LDY#]");
			y = peek(pc++);
			bits_nz(y);
			break;
		case (byte) 0xA4:
			if (ver > 3) System.out.println("[LDYz]");
			y = peek(peek(pc++));
			bits_nz(y);
			cycles = 3;
			break;
		case (byte) 0xB4:
			if (ver > 3) System.out.println("[LDYzx]");
			y = peek(am_zx());
			bits_nz(y);
			cycles = 4;
			break;
		case (byte) 0xAC:
			y = peek(am_a());
			bits_nz(y);
			if (ver > 3) System.out.println("[LDYa]");
			cycles = 4;
			break;
		case (byte) 0xBC:
			y = peek(am_ax());
			bits_nz(y);
			if (ver > 3) System.out.println("[LDYx]");
			cycles = 4 + page;
			break;
		/* *** TAX: Transfer Accumulator to Index X *** */
		case (byte) 0xAA:
			if (ver > 3) System.out.println("[TAX]");
			x = a;
			bits_nz(x);
			break;
		/* *** TXA: Transfer Index X to Accumulator *** */
		case (byte) 0x8A:
			if (ver > 3) System.out.println("[TXA]");
			a = x;
			bits_nz(a);
			break;
		/* *** DEX: Decrement Index X by One *** */
		case (byte) 0xCA:
			if (ver > 3) System.out.println("[DEX]");
			x--;
			bits_nz(x);
			break;
		/* *** INX: Increment Index X by One *** */
		case (byte) 0xE8:
			if (ver > 3) System.out.println("[INX]");
			x++;
			bits_nz(x);
			break;
		/* *** TAY: Transfer Accumulator to Index Y *** */
		case (byte) 0xA8:
			if (ver > 3) System.out.println("[TAY]");
			y = a;
			bits_nz(y);
			break;
		/* *** TYA: Transfer Index Y to Accumulator *** */
		case (byte) 0x98:
			if (ver > 3) System.out.println("[TYA]");
			a = y;
			bits_nz(a);
			break;
		/* *** DEY: Decrement Index Y by One *** */
		case (byte) 0x88:
			if (ver > 3) System.out.println("[DEY]");
			y--;
			bits_nz(y);
			break;
		/* *** INY: Increment Index Y by One *** */
		case (byte) 0xC8:
			if (ver > 3) System.out.println("[INY]");
			y++;
			bits_nz(y);
			break;
			/* *** STX: Store Index X in Memory *** */
		case (byte) 0x86:
			if (ver > 3) System.out.println("[STXz]");
			poke(peek(pc++) & 0X000000FF, x);
			cycles = 3;
			break;
		case (byte) 0x96:
			if (ver > 3) System.out.println("[STXzy]");
			poke(am_zy(), x);
			cycles = 4;
			break;	
		case (byte) 0x8E:
			if (ver > 3) System.out.println("[STXa]");
			poke(am_a(), x);
			cycles = 4;
			break;
			/* *** STY: Store Index Y in Memory *** */
		case (byte) 0x84:
			if (ver > 3) System.out.println("[STYz]");
			poke(peek(pc++) & 0X000000FF, y);
			cycles = 3;
			break;
		case (byte) 0x94:
			if (ver > 3) System.out.println("[STYzx]");
			poke(am_zx(), y);
			cycles = 4;
			break;
		case (byte) 0x8C:
			if (ver > 3) System.out.println("[STYa]");
			poke(am_a(), y);
			cycles = 4;
			break;
		/** ACUMULATOR OPERATIONS **/
			/* *** LDA: Load Accumulator with Memory *** */
		case (byte) 0xA9:
			if (ver > 3) System.out.println("[LDA#]");
			a = peek(pc++);
			bits_nz(a);
			break;
		case (byte) 0xA5:
			if (ver > 3) System.out.println("[LDAz]");
			a = peek(peek(pc++));
			bits_nz(a);
			cycles = 3;
			break;		
		case (byte) 0xB5:
			if (ver > 3) System.out.println("[LDAzx]");
			a = peek(am_zx());
			bits_nz(a);
			cycles = 4;
			break;			
		case (byte) 0xAD:
			if (ver > 3) System.out.println("[LDAa]");
			a = peek(am_a());
			bits_nz(a);
			cycles = 4;
			break;
		case (byte) 0xBD:
			a = peek(am_ax());
			bits_nz(a);
			if (ver > 3) System.out.println("[LDAx]");
			cycles = 4 + page;
			break;
		case (byte) 0xB9:
			a = peek(am_ay());
			bits_nz(a);
			if (ver > 3) System.out.println("[LDAy]");
			cycles = 4 + page;
			break;			
		case (byte) 0xA1:
			if (ver > 3) System.out.println("[LDA(x)]");
			a = peek(am_ix());
			bits_nz(a);
			cycles = 6;
			break;
		case (byte) 0xB1:
			if (ver > 3) System.out.println("[LDA(y)]");
			a = peek(am_iy());
			bits_nz(a);
			cycles = 5 + page;
			break;
			/* *** STA: Store Accumulator in Memory *** */
		case (byte) 0x85:
			if (ver > 3) System.out.println("[STAz]");
			poke(peek(pc++) & 0X000000FF, a);
			cycles = 3;
			break;
		case (byte) 0x95:
			if (ver > 3) System.out.println("[STAzx]");
			poke(am_zx(), a);
			cycles = 4;
			break;
		case (byte) 0x8D:
			if (ver > 3) System.out.println("[STAa]");
			poke(am_a(), a);
			cycles = 4;
			break;
		case (byte) 0x9D:
			if (ver > 3) System.out.println("[STAx]");
			poke(am_ax(), a);
			cycles = 5;		// ...and not 4, as expected
			break;
		case (byte) 0x99:
			if (ver > 3) System.out.println("[STAy]");
			poke(am_ay(), a);
			cycles = 5;		// ...and not 4, as expected
			break;
		case (byte) 0x81:
			if (ver > 3) System.out.println("[STA(x)]");
			poke(am_ix(), a);
			cycles = 6;
			break;			
		case (byte) 0x91:
			if (ver > 3) System.out.println("[STA(y)]");
			poke(am_iy(), a);
			cycles = 6;		// ...and not 5, as expected
			break;

		/* *** INC: Increment Memory (or Accumulator) by One *** */
		case (byte) 0xE6:
			if (ver > 3) System.out.println("[INCz]");
			temp = peek(peek(pc)&0x000000FF);
			temp++;
			poke(peek(pc++)&0x000000FF, temp);
			bits_nz(temp);
			cycles = 5;
			break;
		case (byte) 0xF6:
			if (ver > 3) System.out.println("[INCzx]");
			adr = am_zx();
			temp = peek(adr);
			temp++;
			poke(adr, temp);
			bits_nz(temp);
			cycles = 6;
			break;
		case (byte) 0xEE:
			if (ver > 3) System.out.println("[INCa]");
			adr = am_a();
			temp = peek(adr);
			temp++;
			poke(adr, temp);
			bits_nz(temp);
			cycles = 6;
			break;
		case (byte) 0xFE:
			if (ver > 3) System.out.println("[INCx]");
			adr = am_ax();
			temp = peek(adr);
			temp++;
			poke(adr, temp);
			bits_nz(temp);
			cycles = 7;
			break;

		/* *** DEC: Decrement Memory (or Accumulator) by One *** */
		case (byte) 0xC6:
			if (ver > 3) System.out.println("[DECz]");
			temp = peek(peek(pc)&0x000000FF);
			temp--;
			poke(peek(pc++)&0x000000FF, temp);
			bits_nz(temp);
			cycles = 5;
			break;
		case (byte) 0xD6:
			if (ver > 3) System.out.println("[DECzx]");
			adr = am_zx();
			temp = peek(adr);
			temp--;
			poke(adr, temp);
			bits_nz(temp);
			cycles = 6;
			break;			
		case (byte) 0xCE:
			if (ver > 3) System.out.println("[DECa]");
			adr = am_a();
			temp = peek(adr);
			temp--;
			poke(adr, temp);
			bits_nz(temp);
			cycles = 6;
			break;
		case (byte) 0xDE:
			if (ver > 3) System.out.println("[DECx]");
			adr = am_ax();
			temp = peek(adr);
			temp--;
			poke(adr, temp);
			bits_nz(temp);
			cycles = 7;
			break;

		/* *** NOP: No Ocyclesation *** */
		case (byte) 0xEA:
			if (ver > 3) System.out.println("[NOP]");
			break;				
			
		/* *** AND: "And" Memory with Accumulator *** */
		case (byte) 0x29:
			if (ver > 3) System.out.println("[AND#]");
			a &= peek(pc++);
			bits_nz(a);
			break;
		case (byte) 0x25:
			if (ver > 3) System.out.println("[ANDz]");
			a &= peek(peek(pc++));
			bits_nz(a);
			cycles = 3;
			break;
		case (byte) 0x35:
			if (ver > 3) System.out.println("[ANDzx]");
			a &= peek(am_zx());
			bits_nz(a);
			cycles = 4;
			break;			
		case (byte) 0x2D:
			if (ver > 3) System.out.println("[ANDa]");
			a &= peek(am_a());
			bits_nz(a);
			cycles = 4;
			break;
		case (byte) 0x3D:
			if (ver > 3) System.out.println("[ANDx]");
			a &= peek(am_ax());
			bits_nz(a);
			cycles = 4 + page;
			break;		
		case 0x39:
			if (ver > 3) System.out.println("[ANDy]");
			a &= peek(am_ay());
			bits_nz(a);
			cycles = 4 + page;
			break;	
		case (byte) 0x21:
			if (ver > 3) System.out.println("[AND(x)]");
			a &= peek(am_ix());
			bits_nz(a);
			cycles = 6;
			break;
		case (byte) 0x31:
			if (ver > 3) System.out.println("[AND(y)]");
			a &= peek(am_iy());
			bits_nz(a);
			cycles = 5 + page;
			break;
		/* *** ORA: "Or" Memory with Accumulator *** */
		case (byte) 0x09:
			a |= peek(pc++);
			bits_nz(a);
			if (ver > 3) System.out.println("[ORA#]");
			break;
		case (byte) 0x05:
			if (ver > 3) System.out.println("[ORAz]");
			a |= peek(peek(pc++));
			bits_nz(a);
			cycles = 3;
			break;
		case (byte) 0x15:
			if (ver > 3) System.out.println("[ORAzx]");
			a |= peek(am_zx());
			bits_nz(a);
			cycles = 4;
			break;			
		case (byte) 0x0D:
			if (ver > 3) System.out.println("[ORAa]");
			a |= peek(am_a());
			bits_nz(a);
			cycles = 4;
			break;
		case (byte) 0x1D:
			if (ver > 3) System.out.println("[ORAx]");
			a |= peek(am_ax());
			bits_nz(a);
			cycles = 4 + page;
			break;
		case (byte) 0x19:
			if (ver > 3) System.out.println("[ORAy]");
			a |= peek(am_ay());
			bits_nz(a);
			cycles = 4 + page;
			break;			
		case (byte) 0x01:
			if (ver > 3) System.out.println("[ORA(x)]");
			a |= peek(am_ix());
			bits_nz(a);
			cycles = 6;
			break;
		case (byte) 0x11:
			if (ver > 3) System.out.println("[ORA(y)]");
			a |= peek(am_iy());
			bits_nz(a);
			cycles = 5 + page;
			break;
			/* *** EOR: "Exclusive Or" Memory with Accumulator *** */
		case (byte) 0x49:
			if (ver > 3) System.out.println("[EOR#]");
			a ^= peek(pc++);
			bits_nz(a);
			break;
		case (byte) 0x45:
			if (ver > 3) System.out.println("[EORz]");
			a ^= peek(peek(pc++));
			bits_nz(a);
			cycles = 3;
			break;
		case (byte) 0x55:
			if (ver > 3) System.out.println("[EORzx]");
			a ^= peek(am_zx());
			bits_nz(a);
			cycles = 4;
			break;			
		case (byte) 0x4D:
			if (ver > 3) System.out.println("[EORa]");
			a ^= peek(am_a());
			bits_nz(a);
			cycles = 4;
			break;
		case (byte) 0x5D:
			if (ver > 3) System.out.println("[EORx]");
			a ^= peek(am_ax());
			bits_nz(a);
			cycles = 4 + page;
			break;
		case (byte) 0x59:
			if (ver > 3) System.out.println("[EORy]");
			a ^= peek(am_ay());
			bits_nz(a);
			cycles = 4 + page;
			break;			
		case (byte) 0x41:
			if (ver > 3) System.out.println("[EOR(x)]");
			a ^= peek(am_ix());
			bits_nz(a);
			cycles = 6;
			break;
		case (byte) 0x51:
			if (ver > 3) System.out.println("[EOR(y)]");
			a ^= peek(am_iy());
			bits_nz(a);
			cycles = 5 + page;
			break;
		/* *** Flags *** */
		case (byte) 0x18:
			if (ver > 3) System.out.println("[CLC]");
			p &= 0b11111110;
			break;
		case (byte) 0x38:
			if (ver > 3) System.out.println("[SEC]");
			p |= 0b00000001;
			break;
		case (byte) 0x58:
			if (ver > 3) System.out.println("[CLI]");
			p &= 0b11111011;
			break;
		case (byte) 0x78:
			if (ver > 3) System.out.println("[SEI]");
			p |= 0b00000100;
			break;
		case (byte) 0xB8:
			if (ver > 3) System.out.println("[CLV]");
			p &= 0b10111111;
			break;			
		case (byte) 0xD8:
			if (ver > 3) System.out.println("[CLD]");
			p &= 0b11110111;
			dec = 0;
			break;
		case (byte) 0xF8:
			if (ver > 3) System.out.println("[SED]");
			p |= 0b00001000;
			dec = 1;
			break;
		/* *** ADC: Add Memory to Accumulator with Carry *** */
		case (byte) 0x69:
			if (ver > 3) System.out.println("[ADC#]");
			adc(peek(pc++));
			cycles += dec;
			break;
		case (byte) 0x6D:
			if (ver > 3) System.out.println("[ADCa]");
			adc(peek(am_a()));
			cycles = 4 + dec;
			break;
		case (byte) 0x65:
			if (ver > 3) System.out.println("[ADCz]");
			adc(peek(peek(pc++)));
			cycles = 3 + dec;
			break;
		case (byte) 0x61:
			if (ver > 3) System.out.println("[ADC(x)]");
			adc(peek(am_ix()));
			cycles = 6 + dec;
			break;
		case (byte) 0x71:
			if (ver > 3) System.out.println("[ADC(y)]");
			adc(peek(am_iy()));
			cycles = 5 + dec + page;
			break;
		case (byte) 0x75:
			if (ver > 3) System.out.println("[ADCzx]");
			adc(peek(am_zx()));
			cycles = 4 + dec;
			break;
		case (byte) 0x7D:
			if (ver > 3) System.out.println("[ADCx]");
			adc(peek(am_ax()));
			cycles = 4 + dec + page;
			break;
		case (byte) 0x79:
			if (ver > 3) System.out.println("[ADCy]");
			adc(peek(am_ay()));
			cycles = 4 + dec + page;
			break;

		/* *** SBC: Subtract Memory from Accumulator with Borrow *** */
		case (byte) 0xE9:
			if (ver > 3) System.out.println("[SBC#]");
			sbc(peek(pc++));
			cycles += dec;
			break;
		case (byte) 0xED:
			if (ver > 3) System.out.println("[SBCa]");
			sbc(peek(am_a()));
			cycles = 4 + dec;
			break;
		case (byte) 0xE5:
			if (ver > 3) System.out.println("[SBCz]");
			sbc(peek(peek(pc++)));
			cycles = 3 + dec;
			break;
		case (byte) 0xE1:
			if (ver > 3) System.out.println("[SBC(x)]");
			sbc(peek(am_ix()));
			cycles = 6 + dec;
			break;
		case (byte) 0xF1:
			if (ver > 3) System.out.println("[SBC(y)]");
			sbc(peek(am_iy()));
			cycles = 5 + dec + page;
			break;
		case (byte) 0xF5:
			if (ver > 3) System.out.println("[SBCzx]");
			sbc(peek(am_zx()));
			cycles = 4 + dec;
			break;
		case (byte) 0xFD:
			if (ver > 3) System.out.println("[SBCx]");
			sbc(peek(am_ax()));
			cycles = 4 + dec + page;
			break;
		case (byte) 0xF9:
			if (ver > 3) System.out.println("[SBCy]");
			sbc(peek(am_ay()));
			cycles = 4 + dec + page;
			break;
		case (byte) 0xF2:			// CMOS only
			if (ver > 3) System.out.println("[SBC(z)]");
			sbc(peek(am_iz()));
			cycles = 5 + dec;
			break;
		/* *** CPX: Compare Memory And Index X *** */
		case (byte) 0xE0:
			if (ver > 3) System.out.println("[CPX#]");
			cmp(x, peek(pc++));
			break;
		case (byte) 0xE4:
			if (ver > 3) System.out.println("[CPXz]");
			cmp(x, peek(peek(pc++)));
			cycles = 3;
			break;
		case (byte) 0xEC:
			if (ver > 3) System.out.println("[CPXa]");
			cmp(x, peek(am_a()));
			cycles = 4;
			break;
	/* *** CPY: Compare Memory And Index Y *** */
		case (byte) 0xC0:
			if (ver > 3) System.out.println("[CPY#]");
			cmp(y, peek(pc++));
			break;
		case (byte) 0xC4:
			if (ver > 3) System.out.println("[CPYz]");
			cmp(y, peek(peek(pc++)));
			cycles = 3;
			break;			
		case (byte) 0xCC:
			if (ver > 3) System.out.println("[CPYa]");
			cmp(y, peek(am_a()));
			cycles = 4;
			break;

		/* *** CMP: Compare Memory And Accumulator *** */
		case (byte) 0xC9:
			if (ver > 3) System.out.println("[CMP#]");
			cmp(a, peek(pc++));
			break;
		case (byte) 0xC5:
			if (ver > 3) System.out.println("[CMPz]");
			cmp(a, peek(peek(pc++)));
			cycles = 3;
			break;
		case (byte) 0xD5:
			if (ver > 3) System.out.println("[CMPzx]");
			cmp(a, peek(am_zx()));
			cycles = 4;
			break;
		case (byte) 0xCD:
			if (ver > 3) System.out.println("[CMPa]");
			cmp(a, peek(am_a()));
			cycles = 4;
			break;
		case (byte) 0xD9:
			if (ver > 3) System.out.println("[CMPy]");
			cmp(a, peek(am_ay()));
			cycles = 4 + page;
			break;
		case (byte) 0xC1:
			if (ver > 3) System.out.println("[CMP(x)]");
			cmp(a, peek(am_ix()));
			cycles = 6;
			break;
		case (byte) 0xD1:
			if (ver > 3) System.out.println("[CMP(y)]");
			cmp(a, peek(am_iy()));
			cycles = 5 + page;
			break;
		
		case (byte) 0xDD:
			if (ver > 3) System.out.println("[CMPx]");
			cmp(a, peek(am_ax()));
			cycles = 4 + page;
			break;
			
		/* *** ASL: Shift Left one Bit (Memory or Accumulator) *** */
		case (byte) 0x0A:
			if (ver > 3) System.out.println("[ASL]");
			a=asl(a);
			break;
		case (byte) 0x06:
			if (ver > 3) System.out.println("[ASLz]");
			temp = peek(peek(pc)&0x000000FF);
			temp=asl(temp);
			poke(peek(pc++)&0x000000FF, temp);
			cycles = 5;
			break;
		case (byte) 0x16:
			if (ver > 3) System.out.println("[ASLzx]");
			adr = am_zx();
			temp = peek(adr);
			temp=asl(temp);
			poke(adr, temp);
			cycles = 6;
			break;	
		case (byte) 0x0E:
			if (ver > 3) System.out.println("[ASLa]");
			adr = am_a();
			temp = peek(adr);
			temp=asl(temp);
			poke(adr, temp);
			cycles = 6;
			break;
		case (byte) 0x1E:
			if (ver > 3) System.out.println("[ASLx]");
			adr = am_ax();
			temp = peek(adr);
			temp=asl(temp);
			poke(adr, temp);
			cycles = 7;
			break;			

		/* *** LSR: Shift One Bit Right (Memory or Accumulator) *** */
		case (byte) 0x4A:
			if (ver > 3) System.out.println("[LSR]");
			a=lsr(a);
			break;
		case (byte) 0x46:
			if (ver > 3) System.out.println("[LSRz]");
			temp = peek(peek(pc)&0x000000FF);
			temp=lsr(temp);
			poke(peek(pc++)&0x000000FF, temp);
			cycles = 5;
			break;
		case (byte) 0x56:
			if (ver > 3) System.out.println("[LSRzx]");
			adr = am_zx();
			temp = peek(adr);
			temp=lsr(temp);
			poke(adr, temp);
			cycles = 6;
			break;	
		case (byte) 0x4E:
			if (ver > 3) System.out.println("[LSRa]");
			adr=am_a();
			temp = peek(adr);
			temp=lsr(temp);
			poke(adr, temp);
			cycles = 6;
			break;
		case (byte) 0x5E:
			if (ver > 3) System.out.println("[LSRx]");
			adr = am_ax();
			temp = peek(adr);
			temp=lsr(temp);
			poke(adr, temp);
			cycles = 7;
			break;
			
		/* *** ROL: Rotate One Bit Left (Memory or Accumulator) *** */
		case (byte) 0x2A:
			if (ver > 3) System.out.println("[ROL]");
			a=rol(a);
			break;
		case (byte) 0x26:
			if (ver > 3) System.out.println("[ROLz]");
			temp = peek(peek(pc)&0x000000FF);
			temp=rol(temp);
			poke(peek(pc++)&0x000000FF, temp);
			cycles = 5;
			break;
		case (byte) 0x36:
			if (ver > 3) System.out.println("[ROLzx]");
			adr = am_zx();
			temp = peek(adr);
			temp=rol(temp);
			poke(adr, temp);
			cycles = 6;
			break;	
		case (byte) 0x2E:
			if (ver > 3) System.out.println("[ROLa]");
			adr = am_a();
			temp = peek(adr);
			temp=rol(temp);
			poke(adr, temp);
			cycles = 6;
			break;
		case (byte) 0x3E:
			if (ver > 3) System.out.println("[ROLx]");
			adr = am_ax();
			temp = peek(adr);
			temp=rol(temp);
			poke(adr, temp);
			cycles = 7;
			break;
		
		/* *** ROR: Rotate One Bit Right (Memory or Accumulator) *** */
		case (byte) 0x6A:
			if (ver > 3) System.out.println("[ROR]");
			a=ror(a);
			break;
		case (byte) 0x66:
			if (ver > 3) System.out.println("[RORz]");
			temp = peek(peek(pc)&0x000000FF);
			temp=ror(temp);
			poke(peek(pc++)&0x000000FF, temp);
			cycles = 5;
			break;
		case (byte) 0x76:
			if (ver > 3) System.out.println("[RORzx]");
			adr = am_zx();
			temp = peek(adr);
			temp=ror(temp);
			poke(adr, temp);
			cycles = 6;
			break;	
		case (byte) 0x6E:
			if (ver > 3) System.out.println("[RORa]");
			adr = am_a();
			temp = peek(adr);
			temp=ror(temp);
			poke(adr, temp);
			cycles = 6;
			break;
		case (byte) 0x7E:
			if (ver > 3) System.out.println("[RORx]");
			adr = am_ax();
			temp = peek(adr);
			temp=ror(temp);
			poke(adr, temp);
			cycles = 7;
			break;			
		
		/* *** BIT: Test Bits in Memory with Accumulator *** */
		case (byte) 0x24:
			if (ver > 3) System.out.println("[BITz]");
			temp = peek(peek(pc++));
			p = (byte)((p&0b00111101)&0x000000FF);			// pre-clear N, V & Z
			p = (byte)((p|(temp&0b11000000))&0x000000FF);	// copy bits 7 & 6 as N & Z
			p=(byte)((a&temp&0x000000FF)==0?p|2:p|0);
			cycles = 3;
			break;
		case (byte) 0x2C:
			if (ver > 3) System.out.println("[BITa]");
			temp = peek(am_a());
			p = (byte)((p&0b00111101)&0x000000FF);			// pre-clear N, V & Z
			p = (byte)((p|(temp&0b11000000))&0x000000FF);	// copy bits 7 & 6 as N & Z
			p=(byte)((a&temp&0x000000FF)==0?p|2:p|0);
			cycles = 4;
			break;
		/* *** JMP: Jump to New Location *** */
		case (byte) 0x4C:
			if (ver > 2)	System.out.println("[JMP]");
			pc = am_a();
			cycles = 3;
			break;
		case (byte) 0x6C:
			if (ver > 2)	System.out.println("[JMP()]");
			int j=am_a();
			pc= getWord(peek(j), peek(j+1));
			cycles = 5;
			break;

		/* *** Bxx: Branch on flag condition *** */
		case (byte) 0xB0:
			if (ver > 2) System.out.println("[BCS]");
			if((p & 0b00000001)!=0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;
		case (byte) 0x90:
			if (ver > 2) System.out.println("[BCC]");
			if((p & 0x01)==0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;
			break;
		case (byte) 0xF0:
			if (ver > 2) System.out.println("[BEQ]");
			if((p & 0b00000010)!=0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;
		case (byte) 0xD0:
			if (ver > 2) System.out.println("[BNE]");
			if((p & 0b00000010)==0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;
		case (byte) 0x30:
			if (ver > 2) System.out.println("[BMI]");
			if((p & 0b10000000)!=0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;
		case (byte) 0x10:
			if (ver > 2) System.out.println("[BPL]");
			if((p & 0b10000000)==0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;			
		case (byte) 0x70:
			if (ver > 2) System.out.println("[BVS]");
			if((p & 0b01000000)!=0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;			
		case (byte) 0x50:
			if (ver > 2) System.out.println("[BVC]");
			if((p & 0b01000000)==0) {
				page=rel(page);
				cycles = 3 + page;
			} else pc++;	// must skip offset if not done EEEEEK
			break;
			/* *** TXS: Transfer Index X to Stack Pointer *** */
		case (byte) 0x9A:
			if (ver > 3) System.out.println("[TXS]");
			s = x;
			bits_nz(s);
			break;
		/* *** TSX: Transfer Stack Pointer to Index X *** */
		case (byte) 0xBA:
			if (ver > 3) System.out.println("[TSX]");
			x = s;
			bits_nz(x);
			break;
		/* *** PHA: Push Accumulator on Stack *** */
		case (byte) 0x48:
			if (ver > 3) System.out.println("[PHA]");
			push(a);
			cycles = 3;
			break;
		/* *** PLA: Pull Accumulator from Stack *** */
		case (byte) 0x68:
			if (ver > 3) System.out.println("[PLA]");
			a = pop();
			bits_nz(a);
			cycles = 4;
			break;
				
		/* *** PHP: Push Processor Status on Stack *** */
		case (byte) 0x08:
			if (ver > 3) System.out.println("[PHP]");
			push(p);
			cycles = 3;
			break;
		/* *** PLP: Pull Processor Status from Stack *** */
		case (byte) 0x28:
			if (ver > 3) System.out.println("[PLP]");
			p = pop();
			if ((p & 0b00001000)!=0)	dec = 1;	// check for decimal flag
			else				dec = 0;
			cycles = 4;
			break;

		/* *** JSR: Jump to New Location Saving Return Address *** */
		case (byte) 0x20:
			if (ver > 2)	System.out.println("[JSR]");
			push((byte)(((pc+1)>>8)&0x000000FF));		// stack one byte before return address, right at MSB
			push((byte)((pc+1)&0x000000FF));
			pc = am_a();			// get ocyclesand
			cycles = 6;
			break;
		/* *** RTS: Return from Subroutine *** */
		case (byte) 0x60:
			if (ver > 2)	System.out.println("[RTS]");
			pc=getWord(pop(),pop());
			pc++;
			cycles = 6;
			break;
			
		/* *** RTI: Return from Interrupt *** */
		case 0x40:
			if (ver > 2)	System.out.println("[RTI]");
			p = pop();					// retrieve status
			p |= 0b00010000;			// forget possible B flag
			pc=getWord(pop(),pop());
			cycles = 6;
			break;

		/* *** BRK: force break *** */
		case 0x00:
			if (ver > 1) System.out.println("[BRK]");
			pc++;
			System.out.println("******************************************");
			System.out.println("ACUMULATOR: "+printByte(a));
			System.out.println("REGISTER X: "+printByte(x));
			System.out.println("REGISTER Y: "+printByte(y));
			System.out.println("******************************************");
			cycles=0;
			
			default:
				throw new RuntimeException("Opcode "+printByte(opcode)+" invalid!");
		}
		
		return cycles;
	}
	
	/* *** opcode assistants *** */
	/* compute usual N & Z flags from value */
	protected void bits_nz(byte b) {
		p &= 0b01111101;		// pre-clear N & Z
		p |= (b & 128);			// set N as bit 7
		p |= (b==0)?2:0;		// set Z accordingly
	}

	/* ASL, shift left */
	protected byte asl(byte d) {
		p &= 0b11111110;		// clear C
		p |= (d & 128) >> 7;	// will take previous bit 7
		d <<= 1;				// EEEEEEEEK
		bits_nz(d);
		return d;
	}

	/* LSR, shift right */
	protected byte lsr(byte d) {
		p &= 0b11111110;		// clear C
		p |= d & 1;			// will take previous bit 0
		d=(byte)((d&0x000000FF)>>1);				// eeeek
		bits_nz(d);
		return d;
	}

	/* ROL, rotate left */
	protected byte rol(byte d) {
		byte tmp = (byte)(p & 1);		// keep previous C

		p &= 0b11111110;		// clear C
		p |= (d &0x000000FF) >> 7;	// will take previous bit 7
		d <<= 1;				// eeeeeek
		d |= tmp;			// rotate C
		bits_nz(d);
		return d;
	}

	/* ROR, rotate right */
	protected byte ror(byte d) {
		byte tmp = (byte)(((p & 1)<<7)&0x000000FF);	// keep previous C (shifted)

		p &= 0b11111110;		// clear C
		p = (byte)((p|d&0x01)&0x000000FF);			// will take previous bit 0
		d = (byte)((d >> 1)&0x0000007F);				// eeeek
		d = (byte)((d|tmp)&0x000000FF);			// rotate C
		bits_nz(d);
		return d;
	}

	/* ADC, add with carry */
	protected void adc(byte d) {
		a=binaryAdd(a, d, true);		
		// Decimal mode
		if ((p&0x08)==1) {
			int bcdAcumulator = a & 0x000000F0*10 + a & 0x0000000F;
			int bcdOperand = d & 0x000000F0*10 + d & 0x0000000F;
			int unsignedResult = bcdAcumulator+bcdOperand;
			String strResult = String.format("%02d", unsignedResult);
			a = (byte)((Integer.parseInt(String.valueOf(strResult.charAt(0)))&0x0000000F)<<8);
			a |= (byte)(Integer.parseInt(String.valueOf(strResult.charAt(0)))&0x0000000F);
			// Carry
			p=unsignedResult>99? (byte)(p|0x01) : (byte)(p&0xFE);
		}
	}
	protected void sbc(byte d) {
		adc((byte)~d);
		// Decimal mode
		if ((p&0x08)==1) {
			int bcdAcumulator = a & 0x000000F0*10 + a & 0x0000000F;
			int bcdOperand = d & 0x000000F0*10 + d & 0x0000000F;
			int unsignedResult = bcdAcumulator-bcdOperand;
			String strResult = String.format("%02d", unsignedResult);
			a = (byte)((Integer.parseInt(String.valueOf(strResult.charAt(0)))&0x0000000F)<<8);
			a |= (byte)(Integer.parseInt(String.valueOf(strResult.charAt(0)))&0x0000000F);
			// Carry
			p=unsignedResult>99? (byte)(p|0xFE) : (byte)(p&0x01);
		}
	}
	
	protected byte binaryAdd(byte reg, byte operand, boolean setOverflow) {
		int unsignedReg = reg & 0x000000FF;
		int unsignedOperand = operand & 0x000000FF;
		int unsignedResult = unsignedReg+unsignedOperand + (p&0x01);
		// Carry
		p=unsignedResult>255? (byte)(p|0x01) : (byte)(p&0xFE);
		// Negative & zero
		bits_nz((byte)(unsignedResult&0x000000FF));
		// Overflow
		if(setOverflow) {
			p=unsignedReg>127==unsignedOperand>127 && unsignedResult > 127? (byte)(p|0x40):(byte)(p&0xBF);
		}
		return (byte)(unsignedResult & 0x000000FF);
	}


	/* CMP/CPX/CPY compare register to memory */
	protected void cmp(byte reg, byte d) {
		p|=0x01;
		binaryAdd((byte)reg,(byte)~d, false);
	}
	
	/* *** addressing modes *** */
	/* absolute */
	protected int am_a() {
		int pt = peek(pc) & 0x000000FF | (peek(pc+1) <<8 & 0x0000FFFF);
		pc += 2;

		return pt;
	}
	// Zero Page,Y
	protected int am_zy() { 
		return ((int)peek(pc++) & 0x000000FF) + ((int)y & 0x000000FF);
	}
	// Zero Page,X
	protected int am_zx() { 
		return ((int)peek(pc++) & 0x000000FF) + ((int)x & 0x000000FF);
	}


	/* absolute indexed Y */
	protected int am_ay() {
		int ba = am_a();		// pick base address and skip operand
		int pt = ba + y;		// add offset
		page = ((pt & 0x0000FF00)==(ba & 0x0000FF00))?0:1;	// check page crossing
		return pt;
	}
	/* absolute indexed X */
	protected int am_ax() {
		int ba = am_a();		// pick base address and skip operand
		int pt = ba + x;		// add offset
		page = ((pt & 0x0000FF00)==(ba & 0x0000FF00))?0:1;	// check page crossing
		return pt;
	}

	/* indirect post-indexed */
	protected int am_iy() {
		int ba = am_iz();		// pick base address and skip operand
		int pt = ba + y;		// add offset
		page = ((pt & 0x0000FF00)==(ba & 0x0000FF00))?0:1;	// check page crossing

		return pt;
	}
	
	/* indirect */
	protected int am_iz() {
		int pt = peek(peek(pc)) & 0x000000FF | (peek((peek(pc)+1)&255)<<8 & 0x0000FFFF);	// EEEEEEEK
		pc++;

		return pt;
	}


	/* pre-indexed indirect */
	protected int am_ix() {
		int pt = (peek((peek(pc)+x)&255)|(peek((peek(pc)+x+1)&255)<<8) & 0x0000FFFF);	// EEEEEEK
		pc++;

		return pt;
	}

	/* relative branch */
	protected int rel(int bound) {
		int off = peek(pc++);	// read offset and skip operand
		int old = pc;
		pc += off;
		// Old page == new page ?
		bound = off==-2?-3: ((old & 0x0000FF00)==(pc & 0x0000FF00))?0:1;	// check page crossing
		return bound;
	}
	
	protected void push(byte b)	{
		poke(getWord(s--, (byte)0x01), b); 
	}
	protected byte pop() {
		byte d =peek(getWord(++s, (byte)0x01));
		if(ver>6) System.out.println("pop "+printByte(s)+" -> "+printByte(d));
		return d;
	}
	
	/* emulate !NMI signal */
	protected void nmi() {
		intack();								// acknowledge and save

		pc = peek(0xFFFA) | peek(0xFFFB)<<8;	// NMI vector
		if (ver > 1)	System.out.println(" NMI: PC=>"+printByte(pc));
	}

	/* emulate !IRQ signal */
	protected void irq() {
		if ((p & 4)!=0) {								// if not masked...
			p &= 0b11101111;						// clear B, as this is IRQ!
			intack();								// acknowledge and save
			p |= 0b00010000;						// retrieve current status

			pc = peek(0xFFFE) | peek(0xFFFF)<<8;	// IRQ/BRK vector
			if (ver > 1)	System.out.println(" IRQ: PC=>"+printByte(pc));
		}
	}
	
	/* *** interrupt support *** */
	/* acknowledge interrupt and save status */
	protected int intack() {
		push((byte)((pc>>8)&0x000000FF));		// stack one byte before return address, right at MSB
		push((byte)(pc&0x000000FF));
		push(p);

		p |= 0b00000100;						// set interrupt mask
		p &= 0b11110111;						// and clear Decimal mode (CMOS only)
		dec = 0;
		return 7;								// interrupt acknowledge time
	}
}
