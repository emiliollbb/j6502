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
//			case 0xEC:
//				cmp(x, peek(am_a()));
//				if (ver > 3) System.out.println("[CPXa]");
//				cycles = 4;
//				break;
//			case 0xE4:
//				cmp(x, peek(peek(pc++)));
//				if (ver > 3) System.out.println("[CPXz]");
//				cycles = 3;
//				break;
//	/* *** CPY: Compare Memory And Index Y *** */
//			case 0xC0:
//				cmp(y, peek(pc++));
//				if (ver > 3) System.out.println("[CPY#]");
//				break;
//			case 0xCC:
//				cmp(y, peek(am_a()));
//				if (ver > 3) System.out.println("[CPYa]");
//				cycles = 4;
//				break;
//			case 0xC4:
//				cmp(y, peek(peek(pc++)));
//				if (ver > 3) System.out.println("[CPYz]");
//				cycles = 3;
//				break;






			

//	/* *** ASL: Shift Left one Bit (Memory or Accumulator) *** */
//			case 0x0E:
//				adr = am_a();
//				temp = peek(adr);
//				asl(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ASLa]");
//				cycles = 6;
//				break;
//			case 0x06:
//				temp = peek(peek(pc));
//				asl(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[ASLz]");
//				cycles = 5;
//				break;
//			case 0x0A:
//				asl(&a);
//				if (ver > 3) System.out.println("[ASL]");
//				break;
//			case 0x16:
//				adr = am_zx();
//				temp = peek(adr);
//				asl(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ASLzx]");
//				cycles = 6;
//				break;
//			case 0x1E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				asl(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ASLx]");
//				cycles = 6 + page;	// 7 on NMOS
//				break;
//	/* *** Bxx: Branch on flag condition *** */
//			case 0x90:
//				if(!(p & 0b00000001)) {
//					rel(&page);
//					cycles = 3 + page;
//					if (ver > 2) System.out.println("[BCC]");
//				} else pc++;	// must skip offset if not done EEEEEK
//				break;
//			case 0xB0:
//				if(p & 0b00000001) {
//					rel(&page);
//					cycles = 3 + page;
//					if (ver > 2) System.out.println("[BCS]");
//				} else pc++;	// must skip offset if not done EEEEEK
//				break;
//			case 0xF0:
//				if(p & 0b00000010) {
//					rel(&page);
//					cycles = 3 + page;
//					if (ver > 2) System.out.println("[BEQ]");
//				} else pc++;	// must skip offset if not done EEEEEK
//				break;
//	/* *** BIT: Test Bits in Memory with Accumulator *** */
//			case 0x2C:
//				temp = peek(am_a());
//				p &= 0b00111101;			// pre-clear N, V & Z
//				p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BITa]");
//				cycles = 4;
//				break;
//			case 0x24:
//				temp = peek(peek(pc++));
//				p &= 0b00111101;			// pre-clear N, V & Z
//				p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BITz]");
//				cycles = 3;
//				break;
//			case 0x89:			// CMOS only
//				temp = peek(pc++);
//				p &= 0b11111101;			// pre-clear Z only, is this OK?
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BIT#]");
//				break;
//			case 0x3C:			// CMOS only
//				temp = peek(am_ax(&page));
//				p &= 0b00111101;			// pre-clear N, V & Z
//				p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BITx]");
//				cycles = 4 + page;
//				break;
//			case 0x34:			// CMOS only
//				temp = peek(am_zx());
//				p &= 0b00111101;			// pre-clear N, V & Z
//				p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BITzx]");
//				cycles = 4;
//				break;
//	/* *** Bxx: Branch on flag condition *** */
//			case 0x30:
//				if(p & 0b10000000) {
//					rel(&page);
//					cycles = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BMI]");
//				break;
//			case 0xD0:
//				if(!(p & 0b00000010)) {
//					rel(&page);
//					cycles = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BNE]");
//				break;
//			case 0x10:
//				if(!(p & 0b10000000)) {
//					rel(&page);
//					cycles = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BPL]");
//				break;

//	/* *** Bxx: Branch on flag condition *** */
//			case 0x50:
//				if(!(p & 0b01000000)) {
//					rel(&page);
//					cycles = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BVC]");
//				break;
//			case 0x70:
//				if(p & 0b01000000) {
//					rel(&page);
//					cycles = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BVS]");
//				break;

//	/* *** CMP: Compare Memory And Accumulator *** */
//			case 0xC9:
//				cmp(a, peek(pc++));
//				if (ver > 3) System.out.println("[CMP#]");
//				break;
//			case 0xCD:
//				cmp(a, peek(am_a()));
//				if (ver > 3) System.out.println("[CMPa]");
//				cycles = 4;
//				break;
//			case 0xC5:
//				cmp(a, peek(peek(pc++)));
//				if (ver > 3) System.out.println("[CMPz]");
//				cycles = 3;
//				break;
//			case 0xC1:
//				cmp(a, peek(am_ix()));
//				if (ver > 3) System.out.println("[CMP(x)]");
//				cycles = 6;
//				break;
//			case 0xD1:
//				cmp(a, peek(am_iy(&page)));
//				if (ver > 3) System.out.println("[CMP(y)]");
//				cycles = 5 + page;
//				break;
//			case 0xD5:
//				cmp(a, peek(am_zx()));
//				if (ver > 3) System.out.println("[CMPzx]");
//				cycles = 4;
//				break;
//			case 0xDD:
//				cmp(a, peek(am_ax(&page)));
//				if (ver > 3) System.out.println("[CMPx]");
//				cycles = 4 + page;
//				break;
//			case 0xD9:
//				cmp(a, peek(am_ay(&page)));
//				if (ver > 3) System.out.println("[CMPy]");
//				cycles = 4 + page;
//				break;
//			case 0xD2:			// CMOS only
//				cmp(a, peek(am_iz()));
//				if (ver > 3) System.out.println("[CMP(z)]");
//				cycles = 5;
//				break;





//	/* *** JMP: Jump to New Location *** */
//			case 0x4C:
//				pc = am_a();
//				if (ver > 2)	System.out.println("[JMP]");
//				cycles = 3;
//				break;
//			case 0x6C:
//				pc = am_ai();
//				if (ver > 2)	System.out.println("[JMP()]");
//				cycles = 6;		// 5 for NMOS!
//				break;
//			case 0x7C:			// CMOS only
//				pc = am_aix();
//				if (ver > 2)	System.out.println("[JMP(x)]");
//				cycles = 6;
//				break;
//	/* *** JSR: Jump to New Location Saving Return Address *** */
//			case 0x20:
//				push((pc+1)>>8);		// stack one byte before return address, right at MSB
//				push((pc+1)&255);
//				pc = am_a();			// get ocyclesand
//				if (ver > 2)	System.out.println("[JSR]");
//				cycles = 6;
//				break;
	
	

//	/* *** LSR: Shift One Bit Right (Memory or Accumulator) *** */
//			case 0x4E:
//				adr=am_a();
//				temp = peek(adr);
//				lsr(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[LSRa]");
//				cycles = 6;
//				break;
//			case 0x46:
//				temp = peek(peek(pc));
//				lsr(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[LSRz]");
//				cycles = 5;
//				break;
//			case 0x4A:
//				lsr(&a);
//				if (ver > 3) System.out.println("[LSR]");
//				break;
//			case 0x56:
//				adr = am_zx();
//				temp = peek(adr);
//				lsr(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[LSRzx]");
//				cycles = 6;
//				break;
//			case 0x5E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				lsr(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[LSRx]");
//				cycles = 6 + page;	// 7 for NMOS
//				break;


//	/* *** PHA: Push Accumulator on Stack *** */
//			case 0x48:
//				push(a);
//				if (ver > 3) System.out.println("[PHA]");
//				cycles = 3;
//				break;
//	/* *** PHP: Push Processor Status on Stack *** */
//			case 0x08:
//				push(p);
//				if (ver > 3) System.out.println("[PHP]");
//				cycles = 3;
//				break;
//	/* *** PHX: Push Index X on Stack *** */
//			case 0xDA:			// CMOS only
//				push(x);
//				if (ver > 3) System.out.println("[PHX]");
//				cycles = 3;
//				break;
//	/* *** PHY: Push Index Y on Stack *** */
//			case 0x5A:			// CMOS only
//				push(y);
//				if (ver > 3) System.out.println("[PHY]");
//				cycles = 3;
//				break;
//	/* *** PLA: Pull Accumulator from Stack *** */
//			case 0x68:
//				a = pop();
//				if (ver > 3) System.out.println("[PLA]");
//				bits_nz(a);
//				cycles = 4;
//				break;
//	/* *** PLP: Pull Processor Status from Stack *** */
//			case 0x28:
//				p = pop();
//				if (p & 0b00001000)	dec = 1;	// check for decimal flag
//				else				dec = 0;
//				if (ver > 3) System.out.println("[PLP]");
//				cycles = 4;
//				break;
//	/* *** PLX: Pull Index X from Stack *** */
//			case 0xFA:			// CMOS only
//				x = pop();
//				if (ver > 3) System.out.println("[PLX]");
//				bits_nz(x);		// EEEEEEEEEEEEEEEEEEEEK
//				cycles = 4;
//				break;
//	/* *** PLX: Pull Index X from Stack *** */
//			case 0x7A:			// CMOS only
//				y = pop();
//				if (ver > 3) System.out.println("[PLY]");
//				bits_nz(y);		// EEEEEEEEEEEEEEEEEEEEK
//				cycles = 4;
//				break;
//	/* *** ROL: Rotate One Bit Left (Memory or Accumulator) *** */
//			case 0x2E:
//				adr = am_a();
//				temp = peek(adr);
//				rol(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ROLa]");
//				cycles = 6;
//				break;
//			case 0x26:
//				temp = peek(peek(pc));
//				rol(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[ROLz]");
//				cycles = 5;
//				break;
//			case 0x36:
//				adr = am_zx();
//				temp = peek(adr);
//				rol(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ROLzx]");
//				cycles = 6;
//				break;
//			case 0x3E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				rol(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ROLx]");
//				cycles = 6 + page;	// 7 for NMOS
//				break;
//			case 0x2A:
//				rol(&a);
//				if (ver > 3) System.out.println("[ROL]");
//				break;
//	/* *** ROR: Rotate One Bit Right (Memory or Accumulator) *** */
//			case 0x6E:
//				adr = am_a();
//				temp = peek(adr);
//				ror(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[RORa]");
//				cycles = 6;
//				break;
//			case 0x66:
//				temp = peek(peek(pc));
//				ror(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[RORz]");
//				cycles = 5;
//				break;
//			case 0x6A:
//				ror(&a);
//				if (ver > 3) System.out.println("[ROR]");
//				break;
//			case 0x76:
//				adr = am_zx();
//				temp = peek(adr);
//				ror(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[RORzx]");
//				cycles = 6;
//				break;
//			case 0x7E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				ror(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[RORx]");
//				cycles = 6 + page;	// 7 for NMOS
//				break;
//	/* *** RTI: Return from Interrupt *** */
//			case 0x40:
//				p = pop();					// retrieve status
//				p |= 0b00010000;			// forget possible B flag
//				pc = pop();					// extract LSB...
//				pc |= (pop() << 8);			// ...and MSB, address is correct
//				if (ver > 2)	System.out.println("[RTI]");
//				cycles = 6;
//				break;
//	/* *** RTS: Return from Subroutine *** */
//			case 0x60:
//				pc = pop();					// extract LSB...
//				pc |= (pop() << 8);			// ...and MSB, but is one byte off
//				pc++;						// return instruction address
//				if (ver > 2)	System.out.println("[RTS]");
//				cycles = 6;
//				break;




//	/* *** TRB: Test and Reset Bits, CMOS only *** */
//			case 0x1C:
//				adr = am_a();
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp & ~a);
//				if (ver > 3) System.out.println("[TRBa]");
//				cycles = 6;
//				break;
//			case 0x14:
//				adr = peek(pc++);
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp & ~a);
//				if (ver > 3) System.out.println("[TRBz]");
//				cycles = 5;
//				break;
//	/* *** TSB: Test and Set Bits, CMOS only *** */
//			case 0x0C:
//				adr = am_a();
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp | a);
//				if (ver > 3) System.out.println("[TSBa]");
//				cycles = 6;
//				break;
//			case 0x04:
//				adr = peek(pc++);
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp | a);
//				if (ver > 3) System.out.println("[TSBz]");
//				cycles = 5;
//				break;
//	/* *** TSX: Transfer Stack Pointer to Index X *** */
//			case 0xBA:
//				x = s;
//				bits_nz(x);
//				if (ver > 3) System.out.println("[TSX]");
//				break;

//	/* *** TXS: Transfer Index X to Stack Pointer *** */
//			case 0x9A:
//				s = x;
//				bits_nz(s);
//				if (ver > 3) System.out.println("[TXS]");
//				break;

//	/* *** *** special control 'opcodes' *** *** */
//	/* *** Emulator Breakpoint  (WAI on WDC) *** */
//			case 0xCB:
////				if (ver)	System.out.println(" Status @ $%x04:", pc-1);	// must allow warnings to display status request
////				stat();
//				run = 1;		// pause execution
//				break;
	/* *** Graceful Halt (STP on WDC) *** */
			case (byte)0xDB:
				System.out.println(" ...HALT!");
				cycles = 0;	// definitively stop execution
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
	void bits_nz(byte b) {
		p &= 0b01111101;		// pre-clear N & Z
		p |= (b & 128);			// set N as bit 7
		p |= (b==0)?2:0;		// set Z accordingly
	}
//
//	/* ASL, shift left */
//	void asl(byte *d) {
//		p &= 0b11111110;		// clear C
//		p |= ((*d) & 128) >> 7;	// will take previous bit 7
//		(*d) <<= 1;				// EEEEEEEEK
//		bits_nz(*d);
//	}
//
//	/* LSR, shift right */
//	void lsr(byte *d) {
//		p &= 0b11111110;		// clear C
//		p |= (*d) & 1;			// will take previous bit 0
//		(*d) >>= 1;				// eeeek
//		bits_nz(*d);
//	}
//
//	/* ROL, rotate left */
//	void rol(byte *d) {
//		byte tmp = (p & 1);		// keep previous C
//
//		p &= 0b11111110;		// clear C
//		p |= ((*d) & 128) >> 7;	// will take previous bit 7
//		(*d) <<= 1;				// eeeeeek
//		(*d) |= tmp;			// rotate C
//		bits_nz(*d);
//	}
//
//	/* ROR, rotate right */
//	void ror(byte *d) {
//		byte tmp = (p & 1)<<7;	// keep previous C (shifted)
//
//		p &= 0b11111110;		// clear C
//		p |= (*d) & 1;			// will take previous bit 0
//		(*d) >>= 1;				// eeeek
//		(*d) |= tmp;			// rotate C
//		bits_nz(*d);
//	}
//
	/* ADC, add with carry */
	void adc(byte d) {
		int unsignedAcumulator = a & 0x000000FF;
		int unsignedOperand = d & 0x000000FF;
		int unsignedResult = unsignedAcumulator+unsignedOperand + (p&0x01);
		
		int signedAcumulator = (int) a;
		int signedOperand = (int) d;
		int signedResult = signedAcumulator + signedOperand + (p&0x01);

		a = (byte)(unsignedResult & 0x000000FF);
		
		// Carry
		p=unsignedResult>255? (byte)(p|0x01) : (byte)(p&0xFE);
		// Negative
		p=signedResult<0? (byte)(p|0x80):(byte)(p&0x7F);
		// Zero
		p=unsignedResult==0? (byte)(p|0x02):(byte)(p&0xFD);
		// Overflow
		p=signedResult>127 || signedResult<-128? (byte)(p|0x40):(byte)(p&0xBF);
		
		// Decimal mode
		if ((p&0x08)==1) {
			int bcdAcumulator = a & 0x000000F0*10 + a & 0x0000000F;
			int bcdOperand = d & 0x000000F0*10 + d & 0x0000000F;
			unsignedResult = bcdAcumulator+bcdOperand;
			String strResult = String.format("%02d", unsignedResult);
			a = (byte)((Integer.parseInt(String.valueOf(strResult.charAt(0)))&0x0000000F)<<8);
			a |= (byte)(Integer.parseInt(String.valueOf(strResult.charAt(0)))&0x0000000F);
			// Carry
			p=unsignedResult>99? (byte)(p|0x01) : (byte)(p&0xFE);
		}
	}
	void sbc(byte d) {
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
		else {
			adc((byte)~d);
		}
	}


	/* CMP/CPX/CPY compare register to memory */
	void cmp(byte reg, byte d) {
		int unsignedAcumulator = a & 0x000000FF;
		int unsignedOperand = ~(d & 0x000000FF);
		int unsignedResult = unsignedAcumulator+unsignedOperand + (p&0x01);

		// Carry
		p=unsignedResult>255? (byte)(p|0x01) : (byte)(p&0xFE);
		// Negative
		p=unsignedResult>=0x80? (byte)(p|0x80):(byte)(p&0x7F);
		// Zero
		p=unsignedResult==0? (byte)(p|0x02):(byte)(p&0xFD);
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

	
	
}
