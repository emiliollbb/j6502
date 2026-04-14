package net.emiliollbb.j6502.chips;

import java.util.LinkedList;
import java.util.List;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public class Cpu6502 {
	private byte a; 
	private byte x;
	private byte y;
	private byte s;
	private byte p;			// 8-bit registers
	private int pc;					// program counter
	private int ver;
	private int dec;
	
	private List<IBusDevice> busDevices;
	
	public Cpu6502() {
		pc=0;
		busDevices=new LinkedList<>();
	}
	
	public Cpu6502(List<IBusDevice> devices) {
		super();
		busDevices.addAll(devices);
	}
	
	byte peek(int addr) {
		return busDevices.stream().filter(d -> d.isInRange(addr)).findFirst().get().peek(addr);
	}
	void poke(int addr, byte data) {
		busDevices.stream().filter(d -> d.isInRange(addr)).findFirst().get().poke(addr, data);
	}
	
	/* execute a single opcode, returning cycle count */
	public int step() {
		int per = 2;			// base cycle count
		int page = 0;			// page boundary flag, for speed penalties
		byte opcode, temp;
		short adr;

		opcode = peek(pc++);	// get opcode and point to next one (or operand)

		switch(opcode) {
//	/* *** ADC: Add Memory to Accumulator with Carry *** */
//			case 0x69:
//				adc(peek(pc++));
//				if (ver > 3) System.out.println("[ADC#]");
//				per += dec;
//				break;
//			case 0x6D:
//				adc(peek(am_a()));
//				if (ver > 3) System.out.println("[ADCa]");
//				per = 4 + dec;
//				break;
//			case 0x65:
//				adc(peek(peek(pc++)));
//				if (ver > 3) System.out.println("[ADCz]");
//				per = 3 + dec;
//				break;
//			case 0x61:
//				adc(peek(am_ix()));
//				if (ver > 3) System.out.println("[ADC(x)]");
//				per = 6 + dec;
//				break;
//			case 0x71:
//				adc(peek(am_iy(&page)));
//				if (ver > 3) System.out.println("[ADC(y)]");
//				per = 5 + dec + page;
//				break;
//			case 0x75:
//				adc(peek(am_zx()));
//				if (ver > 3) System.out.println("[ADCzx]");
//				per = 4 + dec;
//				break;
//			case 0x7D:
//				adc(peek(am_ax(&page)));
//				if (ver > 3) System.out.println("[ADCx]");
//				per = 4 + dec + page;
//				break;
//			case 0x79:
//				adc(peek(am_ay(&page)));
//				if (ver > 3) System.out.println("[ADCy]");
//				per = 4 + dec + page;
//				break;
//			case 0x72:			// CMOS only
//				adc(peek(am_iz()));
//				if (ver > 3) System.out.println("[ADC(z)]");
//				per = 5 + dec;
//				break;
//	/* *** AND: "And" Memory with Accumulator *** */
//			case 0x29:
//				a &= peek(pc++);
//				bits_nz(a);
//				if (ver > 3) System.out.println("[AND#]");
//				break;
//			case 0x2D:
//				a &= peek(am_a());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ANDa]");
//				per = 4;
//				break;
//			case 0x25:
//				a &= peek(peek(pc++));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ANDz]");
//				per = 3;
//				break;
//			case 0x21:
//				a &= peek(am_ix());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[AND(x)]");
//				per = 6;
//				break;
//			case 0x31:
//				a &= peek(am_iy(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[AND(y)]");
//				per = 5 + page;
//				break;
//			case 0x35:
//				a &= peek(am_zx());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ANDzx]");
//				per = 4;
//				break;
//			case 0x3D:
//				a &= peek(am_ax(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ANDx]");
//				per = 4 + page;
//				break;
//			case 0x39:
//				a &= peek(am_ay(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ANDy]");
//				per = 4 + page;
//				break;
//			case 0x32:			// CMOS only
//				a &= peek(am_iz());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[AND(z)]");
//				per = 5;
//				break;
//	/* *** ASL: Shift Left one Bit (Memory or Accumulator) *** */
//			case 0x0E:
//				adr = am_a();
//				temp = peek(adr);
//				asl(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ASLa]");
//				per = 6;
//				break;
//			case 0x06:
//				temp = peek(peek(pc));
//				asl(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[ASLz]");
//				per = 5;
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
//				per = 6;
//				break;
//			case 0x1E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				asl(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ASLx]");
//				per = 6 + page;	// 7 on NMOS
//				break;
//	/* *** Bxx: Branch on flag condition *** */
//			case 0x90:
//				if(!(p & 0b00000001)) {
//					rel(&page);
//					per = 3 + page;
//					if (ver > 2) System.out.println("[BCC]");
//				} else pc++;	// must skip offset if not done EEEEEK
//				break;
//			case 0xB0:
//				if(p & 0b00000001) {
//					rel(&page);
//					per = 3 + page;
//					if (ver > 2) System.out.println("[BCS]");
//				} else pc++;	// must skip offset if not done EEEEEK
//				break;
//			case 0xF0:
//				if(p & 0b00000010) {
//					rel(&page);
//					per = 3 + page;
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
//				per = 4;
//				break;
//			case 0x24:
//				temp = peek(peek(pc++));
//				p &= 0b00111101;			// pre-clear N, V & Z
//				p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BITz]");
//				per = 3;
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
//				per = 4 + page;
//				break;
//			case 0x34:			// CMOS only
//				temp = peek(am_zx());
//				p &= 0b00111101;			// pre-clear N, V & Z
//				p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//				p |= (a & temp)?0:2;		// set Z accordingly
//				if (ver > 3) System.out.println("[BITzx]");
//				per = 4;
//				break;
//	/* *** Bxx: Branch on flag condition *** */
//			case 0x30:
//				if(p & 0b10000000) {
//					rel(&page);
//					per = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BMI]");
//				break;
//			case 0xD0:
//				if(!(p & 0b00000010)) {
//					rel(&page);
//					per = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BNE]");
//				break;
//			case 0x10:
//				if(!(p & 0b10000000)) {
//					rel(&page);
//					per = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BPL]");
//				break;
//			case 0x80:			// CMOS only
//				rel(&page);
//				per = 3 + page;
//				if (ver > 2) System.out.println("[BRA]");
//				break;
//	/* *** BRK: force break *** */
//			case 0x00:
//				pc++;
//				if (ver > 1) System.out.println("[BRK]");
//				if (safe)	run = 0;
//				else {
//					p |= 0b00010000;		// set B, just in case
//					intack();
//					p &= 0b11101111;		// clear B, just in case
//					pc = peek(0xFFFE) | peek(0xFFFF)<<8;	// IRQ/BRK vector
//					if (ver > 1) System.out.println("\b PC=>%04X]", pc);
//				}
//				break;
//	/* *** Bxx: Branch on flag condition *** */
//			case 0x50:
//				if(!(p & 0b01000000)) {
//					rel(&page);
//					per = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BVC]");
//				break;
//			case 0x70:
//				if(p & 0b01000000) {
//					rel(&page);
//					per = 3 + page;
//				} else pc++;	// must skip offset if not done EEEEEK
//				if (ver > 2) System.out.println("[BVS]");
//				break;
//	/* *** CLx: Clear flags *** */
//			case 0x18:
//				p &= 0b11111110;
//				if (ver > 3) System.out.println("[CLC]");
//				break;
//			case 0xD8:
//				p &= 0b11110111;
//				dec = 0;
//				if (ver > 3) System.out.println("[CLD]");
//				break;
//			case 0x58:
//				p &= 0b11111011;
//				if (ver > 3) System.out.println("[CLI]");
//				break;
//			case 0xB8:
//				p &= 0b10111111;
//				if (ver > 3) System.out.println("[CLV]");
//				break;
//	/* *** CMP: Compare Memory And Accumulator *** */
//			case 0xC9:
//				cmp(a, peek(pc++));
//				if (ver > 3) System.out.println("[CMP#]");
//				break;
//			case 0xCD:
//				cmp(a, peek(am_a()));
//				if (ver > 3) System.out.println("[CMPa]");
//				per = 4;
//				break;
//			case 0xC5:
//				cmp(a, peek(peek(pc++)));
//				if (ver > 3) System.out.println("[CMPz]");
//				per = 3;
//				break;
//			case 0xC1:
//				cmp(a, peek(am_ix()));
//				if (ver > 3) System.out.println("[CMP(x)]");
//				per = 6;
//				break;
//			case 0xD1:
//				cmp(a, peek(am_iy(&page)));
//				if (ver > 3) System.out.println("[CMP(y)]");
//				per = 5 + page;
//				break;
//			case 0xD5:
//				cmp(a, peek(am_zx()));
//				if (ver > 3) System.out.println("[CMPzx]");
//				per = 4;
//				break;
//			case 0xDD:
//				cmp(a, peek(am_ax(&page)));
//				if (ver > 3) System.out.println("[CMPx]");
//				per = 4 + page;
//				break;
//			case 0xD9:
//				cmp(a, peek(am_ay(&page)));
//				if (ver > 3) System.out.println("[CMPy]");
//				per = 4 + page;
//				break;
//			case 0xD2:			// CMOS only
//				cmp(a, peek(am_iz()));
//				if (ver > 3) System.out.println("[CMP(z)]");
//				per = 5;
//				break;
//	/* *** CPX: Compare Memory And Index X *** */
//			case 0xE0:
//				cmp(x, peek(pc++));
//				if (ver > 3) System.out.println("[CPX#]");
//				break;
//			case 0xEC:
//				cmp(x, peek(am_a()));
//				if (ver > 3) System.out.println("[CPXa]");
//				per = 4;
//				break;
//			case 0xE4:
//				cmp(x, peek(peek(pc++)));
//				if (ver > 3) System.out.println("[CPXz]");
//				per = 3;
//				break;
//	/* *** CPY: Compare Memory And Index Y *** */
//			case 0xC0:
//				cmp(y, peek(pc++));
//				if (ver > 3) System.out.println("[CPY#]");
//				break;
//			case 0xCC:
//				cmp(y, peek(am_a()));
//				if (ver > 3) System.out.println("[CPYa]");
//				per = 4;
//				break;
//			case 0xC4:
//				cmp(y, peek(peek(pc++)));
//				if (ver > 3) System.out.println("[CPYz]");
//				per = 3;
//				break;
//	/* *** DEC: Decrement Memory (or Accumulator) by One *** */
//			case 0xCE:
//				adr = am_a();	// EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEK
//				temp = peek(adr);
//				temp--;
//				poke(adr, temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[DECa]");
//				per = 6;
//				break;
//			case 0xC6:
//				temp = peek(peek(pc));
//				temp--;
//				poke(peek(pc++), temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[DECz]");
//				per = 5;
//				break;
//			case 0xD6:
//				adr = am_zx();	// EEEEEEEEEEK
//				temp = peek(adr);
//				temp--;
//				poke(adr, temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[DECzx]");
//				per = 6;
//				break;
//			case 0xDE:
//				adr = am_ax(&page);	// EEEEEEEEK
//				temp = peek(adr);
//				temp--;
//				poke(adr, temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[DECx]");
//				per = 7;		// 6+page for WDC?
//				break;
//			case 0x3A:			// CMOS only (OK)
//				a--;
//				bits_nz(a);
//				if (ver > 3) System.out.println("[DEC]");
//				break;
//	/* *** DEX: Decrement Index X by One *** */
//			case 0xCA:
//				x--;
//				bits_nz(x);
//				if (ver > 3) System.out.println("[DEX]");
//				break;
//	/* *** DEY: Decrement Index Y by One *** */
//			case 0x88:
//				y--;
//				bits_nz(y);
//				if (ver > 3) System.out.println("[DEY]");
//				break;
//	/* *** EOR: "Exclusive Or" Memory with Accumulator *** */
//			case 0x49:
//				a ^= peek(pc++);
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EOR#]");
//				break;
//			case 0x4D:
//				a ^= peek(am_a());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EORa]");
//				per = 4;
//				break;
//			case 0x45:
//				a ^= peek(peek(pc++));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EORz]");
//				per = 3;
//				break;
//			case 0x41:
//				a ^= peek(am_ix());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EOR(x)]");
//				per = 6;
//				break;
//			case 0x51:
//				a ^= peek(am_iy(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EOR(y)]");
//				per = 5 + page;
//				break;
//			case 0x55:
//				a ^= peek(am_zx());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EORzx]");
//				per = 4;
//				break;
//			case 0x5D:
//				a ^= peek(am_ax(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EORx]");
//				per = 4 + page;
//				break;
//			case 0x59:
//				a ^= peek(am_ay(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EORy]");
//				per = 4 + page;
//				break;
//			case 0x52:			// CMOS only
//				a ^= peek(am_iz());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[EOR(z)]");
//				per = 5;
//				break;
//	/* *** INC: Increment Memory (or Accumulator) by One *** */
//			case 0xEE:
//				adr = am_a();	// EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEK
//				temp = peek(adr);
//				temp++;
//				poke(adr, temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[INCa]");
//				per = 6;
//				break;
//			case 0xE6:
//				temp = peek(peek(pc));
//				temp++;
//				poke(peek(pc++), temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[INCz]");
//				per = 5;
//				break;
//			case 0xF6:
//				adr = am_zx();	// EEEEEEEEEEK
//				temp = peek(adr);
//				temp++;
//				poke(adr, temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[INCzx]");
//				per = 6;
//				break;
//			case 0xFE:
//				adr = am_ax(&page);	// EEEEEEEEEEK
//				temp = peek(adr);
//				temp++;
//				poke(adr, temp);
//				bits_nz(temp);
//				if (ver > 3) System.out.println("[INCx]");
//				per = 7;		// 6+page for WDC?
//				break;
//			case 0x1A:			// CMOS only
//				a++;
//				bits_nz(a);
//				if (ver > 3) System.out.println("[INC]");
//				break;
//	/* *** INX: Increment Index X by One *** */
//			case 0xE8:
//				x++;
//				bits_nz(x);
//				if (ver > 3) System.out.println("[INX]");
//				break;
//	/* *** INY: Increment Index Y by One *** */
//			case 0xC8:
//				y++;
//				bits_nz(y);
//				if (ver > 3) System.out.println("[INY]");
//				break;
//	/* *** JMP: Jump to New Location *** */
//			case 0x4C:
//				pc = am_a();
//				if (ver > 2)	System.out.println("[JMP]");
//				per = 3;
//				break;
//			case 0x6C:
//				pc = am_ai();
//				if (ver > 2)	System.out.println("[JMP()]");
//				per = 6;		// 5 for NMOS!
//				break;
//			case 0x7C:			// CMOS only
//				pc = am_aix();
//				if (ver > 2)	System.out.println("[JMP(x)]");
//				per = 6;
//				break;
//	/* *** JSR: Jump to New Location Saving Return Address *** */
//			case 0x20:
//				push((pc+1)>>8);		// stack one byte before return address, right at MSB
//				push((pc+1)&255);
//				pc = am_a();			// get operand
//				if (ver > 2)	System.out.println("[JSR]");
//				per = 6;
//				break;
	/* *** LDA: Load Accumulator with Memory *** */
			case (byte) 0xA9:
				a = peek(pc++);
				bits_nz(a);
				if (ver > 3) System.out.println("[LDA#]");
				break;
//			case (byte) 0xAD:
//				a = peek(am_a());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDAa]");
//				per = 4;
//				break;
//			case (byte) 0xA5:
//				a = peek(peek(pc++));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDAz]");
//				per = 3;
//				break;
//			case (byte) 0xA1:
//				a = peek(am_ix());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDA(x)]");
//				per = 6;
//				break;
//			case (byte) 0xB1:
//				a = peek(am_iy(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDA(y)]");
//				per = 5 + page;
//				break;
//			case (byte) 0xB5:
//				a = peek(am_zx());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDAzx]");
//				per = 4;
//				break;
//			case (byte) 0xBD:
//				a = peek(am_ax(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDAx]");
//				per = 4 + page;
//				break;
//			case (byte) 0xB9:
//				a = peek(am_ay(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDAy]");
//				per = 4 + page;
//				break;
//			case (byte) 0xB2:			// CMOS only
//				a = peek(am_iz());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[LDA(z)]");
//				per = 5;
//				break;
	/* *** LDX: Load Index X with Memory *** */
			case (byte) 0xA2:
				x = peek(pc++);
				bits_nz(x);
				if (ver > 3) System.out.println("[LDX#]");
				break;
//			case 0xAE:
//				x = peek(am_a());
//				bits_nz(x);
//				if (ver > 3) System.out.println("[LDXa]");
//				per = 4;
//				break;
//			case 0xA6:
//				x = peek(peek(pc++));
//				bits_nz(x);
//				if (ver > 3) System.out.println("[LDXz]");
//				per = 3;
//				break;
//			case 0xB6:
//				x = peek(am_zy());
//				bits_nz(x);
//				if (ver > 3) System.out.println("[LDXzy]");
//				per = 4;
//				break;
//			case 0xBE:
//				x = peek(am_ay(&page));
//				bits_nz(x);
//				if (ver > 3) System.out.println("[LDXy]");
//				per = 4 + page;
//				break;
//	/* *** LDY: Load Index Y with Memory *** */
//			case 0xA0:
//				y = peek(pc++);
//				bits_nz(y);
//				if (ver > 3) System.out.println("[LDY#]");
//				break;
//			case 0xAC:
//				y = peek(am_a());
//				bits_nz(y);
//				if (ver > 3) System.out.println("[LDYa]");
//				per = 4;
//				break;
//			case 0xA4:
//				y = peek(peek(pc++));
//				bits_nz(y);
//				if (ver > 3) System.out.println("[LDYz]");
//				per = 3;
//				break;
//			case 0xB4:
//				y = peek(am_zx());
//				bits_nz(y);
//				if (ver > 3) System.out.println("[LDYzx]");
//				per = 4;
//				break;
//			case 0xBC:
//				y = peek(am_ax(&page));
//				bits_nz(y);
//				if (ver > 3) System.out.println("[LDYx]");
//				per = 4 + page;
//				break;
//	/* *** LSR: Shift One Bit Right (Memory or Accumulator) *** */
//			case 0x4E:
//				adr=am_a();
//				temp = peek(adr);
//				lsr(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[LSRa]");
//				per = 6;
//				break;
//			case 0x46:
//				temp = peek(peek(pc));
//				lsr(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[LSRz]");
//				per = 5;
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
//				per = 6;
//				break;
//			case 0x5E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				lsr(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[LSRx]");
//				per = 6 + page;	// 7 for NMOS
//				break;
//	/* *** NOP: No Operation *** */
//			case 0xEA:
//				if (ver > 3) System.out.println("[NOP]");
//				break;
//	/* *** ORA: "Or" Memory with Accumulator *** */
//			case 0x09:
//				a |= peek(pc++);
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORA#]");
//				break;
//			case 0x0D:
//				a |= peek(am_a());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORAa]");
//				per = 4;
//				break;
//			case 0x05:
//				a |= peek(peek(pc++));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORAz]");
//				per = 3;
//				break;
//			case 0x01:
//				a |= peek(am_ix());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORA(x)]");
//				per = 6;
//				break;
//			case 0x11:
//				a |= peek(am_iy(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORA(y)]");
//				per = 5 + page;
//				break;
//			case 0x15:
//				a |= peek(am_zx());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORAzx]");
//				per = 4;
//				break;
//			case 0x1D:
//				a |= peek(am_ax(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORAx]");
//				per = 4 + page;
//				break;
//			case 0x19:
//				a |= peek(am_ay(&page));
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORAy]");
//				per = 4 + page;
//				break;
//			case 0x12:			// CMOS only
//				a |= peek(am_iz());
//				bits_nz(a);
//				if (ver > 3) System.out.println("[ORA(z)]");
//				per = 5;
//				break;
//	/* *** PHA: Push Accumulator on Stack *** */
//			case 0x48:
//				push(a);
//				if (ver > 3) System.out.println("[PHA]");
//				per = 3;
//				break;
//	/* *** PHP: Push Processor Status on Stack *** */
//			case 0x08:
//				push(p);
//				if (ver > 3) System.out.println("[PHP]");
//				per = 3;
//				break;
//	/* *** PHX: Push Index X on Stack *** */
//			case 0xDA:			// CMOS only
//				push(x);
//				if (ver > 3) System.out.println("[PHX]");
//				per = 3;
//				break;
//	/* *** PHY: Push Index Y on Stack *** */
//			case 0x5A:			// CMOS only
//				push(y);
//				if (ver > 3) System.out.println("[PHY]");
//				per = 3;
//				break;
//	/* *** PLA: Pull Accumulator from Stack *** */
//			case 0x68:
//				a = pop();
//				if (ver > 3) System.out.println("[PLA]");
//				bits_nz(a);
//				per = 4;
//				break;
//	/* *** PLP: Pull Processor Status from Stack *** */
//			case 0x28:
//				p = pop();
//				if (p & 0b00001000)	dec = 1;	// check for decimal flag
//				else				dec = 0;
//				if (ver > 3) System.out.println("[PLP]");
//				per = 4;
//				break;
//	/* *** PLX: Pull Index X from Stack *** */
//			case 0xFA:			// CMOS only
//				x = pop();
//				if (ver > 3) System.out.println("[PLX]");
//				bits_nz(x);		// EEEEEEEEEEEEEEEEEEEEK
//				per = 4;
//				break;
//	/* *** PLX: Pull Index X from Stack *** */
//			case 0x7A:			// CMOS only
//				y = pop();
//				if (ver > 3) System.out.println("[PLY]");
//				bits_nz(y);		// EEEEEEEEEEEEEEEEEEEEK
//				per = 4;
//				break;
//	/* *** ROL: Rotate One Bit Left (Memory or Accumulator) *** */
//			case 0x2E:
//				adr = am_a();
//				temp = peek(adr);
//				rol(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ROLa]");
//				per = 6;
//				break;
//			case 0x26:
//				temp = peek(peek(pc));
//				rol(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[ROLz]");
//				per = 5;
//				break;
//			case 0x36:
//				adr = am_zx();
//				temp = peek(adr);
//				rol(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ROLzx]");
//				per = 6;
//				break;
//			case 0x3E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				rol(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[ROLx]");
//				per = 6 + page;	// 7 for NMOS
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
//				per = 6;
//				break;
//			case 0x66:
//				temp = peek(peek(pc));
//				ror(&temp);
//				poke(peek(pc++), temp);
//				if (ver > 3) System.out.println("[RORz]");
//				per = 5;
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
//				per = 6;
//				break;
//			case 0x7E:
//				adr = am_ax(&page);
//				temp = peek(adr);
//				ror(&temp);
//				poke(adr, temp);
//				if (ver > 3) System.out.println("[RORx]");
//				per = 6 + page;	// 7 for NMOS
//				break;
//	/* *** RTI: Return from Interrupt *** */
//			case 0x40:
//				p = pop();					// retrieve status
//				p |= 0b00010000;			// forget possible B flag
//				pc = pop();					// extract LSB...
//				pc |= (pop() << 8);			// ...and MSB, address is correct
//				if (ver > 2)	System.out.println("[RTI]");
//				per = 6;
//				break;
//	/* *** RTS: Return from Subroutine *** */
//			case 0x60:
//				pc = pop();					// extract LSB...
//				pc |= (pop() << 8);			// ...and MSB, but is one byte off
//				pc++;						// return instruction address
//				if (ver > 2)	System.out.println("[RTS]");
//				per = 6;
//				break;
//	/* *** SBC: Subtract Memory from Accumulator with Borrow *** */
//			case 0xE9:
//				sbc(peek(pc++));
//				if (ver > 3) System.out.println("[SBC#]");
//				per += dec;
//				break;
//			case 0xED:
//				sbc(peek(am_a()));
//				if (ver > 3) System.out.println("[SBCa]");
//				per = 4 + dec;
//				break;
//			case 0xE5:
//				sbc(peek(peek(pc++)));
//				if (ver > 3) System.out.println("[SBCz]");
//				per = 3 + dec;
//				break;
//			case 0xE1:
//				sbc(peek(am_ix()));
//				if (ver > 3) System.out.println("[SBC(x)]");
//				per = 6 + dec;
//				break;
//			case 0xF1:
//				sbc(peek(am_iy(&page)));
//				if (ver > 3) System.out.println("[SBC(y)]");
//				per = 5 + dec + page;
//				break;
//			case 0xF5:
//				sbc(peek(am_zx()));
//				if (ver > 3) System.out.println("[SBCzx]");
//				per = 4 + dec;
//				break;
//			case 0xFD:
//				sbc(peek(am_ax(&page)));
//				if (ver > 3) System.out.println("[SBCx]");
//				per = 4 + dec + page;
//				break;
//			case 0xF9:
//				sbc(peek(am_ay(&page)));
//				if (ver > 3) System.out.println("[SBCy]");
//				per = 4 + dec + page;
//				break;
//			case 0xF2:			// CMOS only
//				sbc(peek(am_iz()));
//				if (ver > 3) System.out.println("[SBC(z)]");
//				per = 5 + dec;
//				break;
//	// *** SEx: Set Flags *** */
//			case 0x38:
//				p |= 0b00000001;
//				if (ver > 3) System.out.println("[SEC]");
//				break;
//			case 0xF8:
//				p |= 0b00001000;
//				dec = 1;
//				if (ver > 3) System.out.println("[SED]");
//				break;
//			case 0x78:
//				p |= 0b00000100;
//				if (ver > 3) System.out.println("[SEI]");
//				break;
//	/* *** STA: Store Accumulator in Memory *** */
//			case 0x8D:
//				poke(am_a(), a);
//				if (ver > 3) System.out.println("[STAa]");
//				per = 4;
//				break;
//			case 0x85:
//				poke(peek(pc++), a);
//				if (ver > 3) System.out.println("[STAz]");
//				per = 3;
//				break;
//			case 0x81:
//				poke(am_ix(), a);
//				if (ver > 3) System.out.println("[STA(x)]");
//				per = 6;
//				break;
//			case 0x91:
//				poke(am_iy(&page), a);
//				if (ver > 3) System.out.println("[STA(y)]");
//				per = 6;		// ...and not 5, as expected
//				break;
//			case 0x95:
//				poke(am_zx(), a);
//				if (ver > 3) System.out.println("[STAzx]");
//				per = 4;
//				break;
//			case 0x9D:
//				poke(am_ax(&page), a);
//				if (ver > 3) System.out.println("[STAx]");
//				per = 5;		// ...and not 4, as expected
//				break;
//			case 0x99:
//				poke(am_ay(&page), a);
//				if (ver > 3) System.out.println("[STAy]");
//				per = 5;		// ...and not 4, as expected
//				break;
//			case 0x92:			// CMOS only
//				poke(am_iz(), a);
//				if (ver > 3) System.out.println("[STA(z)]");
//				per = 5;
//				break;
	/* *** STX: Store Index X in Memory *** */
			case (byte) 0x8E:
				poke(am_a(), x);
				if (ver > 3) System.out.println("[STXa]");
				per = 4;
				break;
//			case 0x86:
//				poke(peek(pc++), x);
//				if (ver > 3) System.out.println("[STXz]");
//				per = 3;
//				break;
//			case 0x96:
//				poke(am_zy(), x);
//				if (ver > 3) System.out.println("[STXzy]");
//				per = 4;
//				break;
//	/* *** STY: Store Index Y in Memory *** */
//			case 0x8C:
//				poke(am_a(), y);
//				if (ver > 3) System.out.println("[STYa]");
//				per = 4;
//				break;
//			case 0x84:
//				poke(peek(pc++), y);
//				if (ver > 3) System.out.println("[STYz]");
//				per = 3;
//				break;
//			case 0x94:
//				poke(am_zx(), y);
//				if (ver > 3) System.out.println("[STYzx]");
//				per = 4;
//				break;
//	// *** STZ: Store Zero in Memory, CMOS only ***
//			case 0x9C:
//				poke(am_a(), 0);
//				if (ver > 3) System.out.println("[STZa]");
//				per = 4;
//				break;
//			case 0x64:
//				poke(peek(pc++), 0);
//				if (ver > 3) System.out.println("[STZz]");
//				per = 3;
//				break;
//			case 0x74:
//				poke(am_zx(), 0);
//				if (ver > 3) System.out.println("[STZzx]");
//				per = 4;
//				break;
//			case 0x9E:
//				poke(am_ax(&page), 0);
//				if (ver > 3) System.out.println("[STZx]");
//				per = 5;		// ...and not 4, as expected
//				break;
//	/* *** TAX: Transfer Accumulator to Index X *** */
//			case 0xAA:
//				x = a;
//				bits_nz(x);
//				if (ver > 3) System.out.println("[TAX]");
//				break;
//	/* *** TAY: Transfer Accumulator to Index Y *** */
//			case 0xA8:
//				y = a;
//				bits_nz(y);
//				if (ver > 3) System.out.println("[TAY]");
//				break;
//	/* *** TRB: Test and Reset Bits, CMOS only *** */
//			case 0x1C:
//				adr = am_a();
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp & ~a);
//				if (ver > 3) System.out.println("[TRBa]");
//				per = 6;
//				break;
//			case 0x14:
//				adr = peek(pc++);
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp & ~a);
//				if (ver > 3) System.out.println("[TRBz]");
//				per = 5;
//				break;
//	/* *** TSB: Test and Set Bits, CMOS only *** */
//			case 0x0C:
//				adr = am_a();
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp | a);
//				if (ver > 3) System.out.println("[TSBa]");
//				per = 6;
//				break;
//			case 0x04:
//				adr = peek(pc++);
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp | a);
//				if (ver > 3) System.out.println("[TSBz]");
//				per = 5;
//				break;
//	/* *** TSX: Transfer Stack Pointer to Index X *** */
//			case 0xBA:
//				x = s;
//				bits_nz(x);
//				if (ver > 3) System.out.println("[TSX]");
//				break;
//	/* *** TXA: Transfer Index X to Accumulator *** */
//			case 0x8A:
//				a = x;
//				bits_nz(a);
//				if (ver > 3) System.out.println("[TXA]");
//				break;
//	/* *** TXS: Transfer Index X to Stack Pointer *** */
//			case 0x9A:
//				s = x;
//				bits_nz(s);
//				if (ver > 3) System.out.println("[TXS]");
//				break;
//	/* *** TYA: Transfer Index Y to Accumulator *** */
//			case 0x98:
//				a = y;
//				bits_nz(a);
//				if (ver > 3) System.out.println("[TYA]");
//				break;
//	/* *** *** special control 'opcodes' *** *** */
//	/* *** Emulator Breakpoint  (WAI on WDC) *** */
//			case 0xCB:
////				if (ver)	System.out.println(" Status @ $%x04:", pc-1);	// must allow warnings to display status request
////				stat();
//				run = 1;		// pause execution
//				break;
//	/* *** Graceful Halt (STP on WDC) *** */
//			case 0xDB:
//				System.out.println(" ...HALT!");
//				run = per = 0;	// definitively stop execution
//				break;
//	/* *** *** unused (illegal?) opcodes *** *** */
//	/* *** remaining opcodes (illegal on NMOS) executed as pseudoNOPs, according to 65C02 byte and cycle usage *** */
//			case 0x03:
//			case 0x13:
//			case 0x23:
//			case 0x33:
//			case 0x43:
//			case 0x53:
//			case 0x63:
//			case 0x73:
//			case 0x83:
//			case 0x93:
//			case 0xA3:
//			case 0xB3:
//			case 0xC3:
//			case 0xD3:
//			case 0xE3:
//			case 0xF3:
//			case 0x0B:
//			case 0x1B:
//			case 0x2B:
//			case 0x3B:
//			case 0x4B:
//			case 0x5B:
//			case 0x6B:
//			case 0x7B:
//			case 0x8B:
//			case 0x9B:
//			case 0xAB:
//			case 0xBB:
//			case 0xEB:
//			case 0xFB:	// minus WDC opcodes, used for emulator control
//			case 0x07:
//			case 0x17:
//			case 0x27:
//			case 0x37:
//			case 0x47:
//			case 0x57:
//			case 0x67:
//			case 0x77:
//			case 0x87:
//			case 0x97:
//			case 0xA7:
//			case 0xB7:
//			case 0xC7:
//			case 0xD7:
//			case 0xE7:
//			case 0xF7:	// Rockwell RMB/SMB opcodes
//			case 0x0F:
//			case 0x1F:
//			case 0x2F:
//			case 0x3F:
//			case 0x4F:
//			case 0x5F:
//			case 0x6F:
//			case 0x7F:
//			case 0x8F:
//			case 0x9F:
//			case 0xAF:
//			case 0xBF:
//			case 0xCF:
//			case 0xDF:
//			case 0xEF:
//			case 0xFF:	// Rockwell BBR/BBS opcodes
//				per = 1;		// ultra-fast 1 byte NOPs!
//				if (ver)	System.out.println("[NOP!]");
//				if (safe)	illegal(1, opcode);
//				break;
//			case 0x02:
//			case 0x22:
//			case 0x42:
//			case 0x62:
//			case 0x82:
//			case 0xC2:
//			case 0xE2:
//				pc++;			// 2-byte, 2-cycle NOPs
//				if (ver)	System.out.println("[NOP#]");
//				if (safe)	illegal(2, opcode);
//				break;
//			case 0x44:
//				pc++;
//				per++;			// only case of 2-byte, 3-cycle NOP
//				if (ver)	System.out.println("[NOPz]");
//				if (safe)	illegal(2, opcode);
//				break;
//			case 0x54:
//			case 0xD4:
//			case 0xF4:
//				pc++;
//				per = 4;		// only cases of 2-byte, 4-cycle NOP
//				if (ver)	System.out.println("[NOPzx]");
//				if (safe)	illegal(2, opcode);
//				break;
//			case 0xDC:
//			case 0xFC:
//				pc += 2;
//				per = 4;		// only cases of 3-byte, 4-cycle NOP
//				if (ver)	System.out.println("[NOPa]");
//				if (safe)	illegal(3, opcode);
//				break;
//			case 0x5C:
//				pc += 2;
//				per = 8;		// extremely slow 8-cycle NOP
//				if (ver)	System.out.println("[NOP?]");
//				if (safe)	illegal(3, opcode);
//				break;			// not needed as it's the last one, but just in case
		}

		return per;
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
//	/* ADC, add with carry */
//	void adc(byte d) {
//		byte old = a;
//		word big = a, high;
//
//		big += d;				// basic add... but check for Decimal mode!
//		big += (p & 1);			// add with Carry (A was computer just after this)
//
//		if (p & 0b00001000) {						// Decimal mode!
//			high = (old & 0x0F)+(d & 0x0F)+(p & 1);			// compute carry-less LSN eeeeeek
//			if (((big & 0x0F) > 9)||(high & 0x10)) {		// LSN overflow? was 'a' instead of 'big'
//				big += 6;											// get into next decade
//			}
//			if (((big & 0xF0) > 0x90)||(big & 256)) {				// MSN overflow?
//				big += 0x60;						// correct it
//			}
//		}
//		a = big & 255;			// placed here trying to correct Carry in BCD mode
//
//		if (big & 256)			p |= 0b00000001;	// set Carry if needed
//		else					p &= 0b11111110;
//		if ((a&128)^(old&128))	p |= 0b01000000;	// set oVerflow if needed
//		else					p &= 0b10111111;
//		bits_nz(a);									// set N & Z as usual
//	}
//
//	/* SBC, subtract with borrow */ //EEEEEEEEEEEEEEEEK
//	void sbc(byte d) {
//		byte old = a;
//		word big = a;
//
//		big += ~d;				// basic subtract, 6502-style... but check for Decimal mode!
//		big += (p & 1);			// add with Carry
//		
//		if (p & 0b00001000) {						// Decimal mode!
//			if ((big & 0x0F) > 9) {					// LSN overflow?
//				big -= 6;								// get into next decade *** check
//			}
//			if ((big & 0xF0) > 0x90) {				// MSN overflow?
//				big -= 0x60;							// correct it
//			}
//		}
//		a = big & 255;			// same as ADC
//
//		if (big & 256)			p &= 0b11111110;	// set Carry if needed EEEEEEEEEEEEK, is this OK?
//		else					p |= 0b00000001;
//		if ((a&128)^(old&128))	p |= 0b01000000;	// set oVerflow if needed
//		else					p &= 0b10111111;
//		bits_nz(a);									// set N & Z as usual
//	}
//
//	/* CMP/CPX/CPY compare register to memory */
//	void cmp(byte reg, byte d) {
//		word big = reg;
//
//		big -= d;				// apparent subtract, always binary
//
//		if (big & 256)			p &= 0b11111110;	// set Carry if needed (note inversion)
//		else					p |= 0b00000001;
//		bits_nz(reg - d);							// set N & Z as usual
//	}
//	
	
	
	
	
	
	/* *** addressing modes *** */
	/* absolute */
	private int am_a() {
		int pt = peek(pc) | (peek(pc+1) <<8);
		pc += 2;

		return pt;
	}

//	/* absolute indexed X */
//	word am_ax(int *bound) {
//		word ba = am_a();		// pick base address and skip operand
//		word pt = ba + x;		// add offset
//		*bound = ((pt & 0xFF00)==(ba & 0xFF00))?0:1;	// check page crossing
//
//		return pt;
//	}
//
//	/* absolute indexed Y */
//	word am_ay(int *bound) {
//		word ba = am_a();		// pick base address and skip operand
//		word pt = ba + y;		// add offset
//		*bound = ((pt & 0xFF00)==(ba & 0xFF00))?0:1;	// check page crossing
//
//		return pt;
//	}
//
//	/* indirect */
//	word am_iz(void) {
//		word pt = peek(peek(pc)) | (peek((peek(pc)+1)&255)<<8);	// EEEEEEEK
//		pc++;
//
//		return pt;
//	}
//
//	/* indirect post-indexed */
//	word am_iy(int *bound) {
//		word ba = am_iz();		// pick base address and skip operand
//		word pt = ba + y;		// add offset
//		*bound = ((pt & 0xFF00)==(ba & 0xFF00))?0:1;	// check page crossing
//
//		return pt;
//	}
//
//	/* pre-indexed indirect */
//	word am_ix(void) {
//		word pt = (peek((peek(pc)+x)&255)|(peek((peek(pc)+x+1)&255)<<8));	// EEEEEEK
//		pc++;
//
//		return pt;
//	}
//
//	/* relative branch */
//	void rel(int *bound) {
//		byte off = peek(pc++);	// read offset and skip operand
//		word old = pc;
//
//		pc += off;
//		pc -= (off & 128)?256:0;						// check negative displacement
//
//		*bound = ((old & 0xFF00)==(pc & 0xFF00))?0:1;	// check page crossing
//	}
	
	
	
	
}
