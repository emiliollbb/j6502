package net.emiliollbb.j6502.chips;

import java.util.List;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public class Cpu65C02 extends Cpu6502 {
	
	
	public Cpu65C02(int speed) {
		super(speed);
	}
	public Cpu65C02(int speed, List<IBusDevice> devices) {
		super(speed, devices);
	}

	@Override
	protected int runOpcode(byte opcode) {
		page = 0;			// page boundary flag, for speed penalties
		int cycles = 2;			// base cycle count
		byte temp;
		int adr;
		switch(opcode) {
		case (byte)0x80:			// CMOS only
			if (ver > 2) System.out.println("[BRA]");
			page=rel(page);
			cycles = 3 + page;
			break;
//			case (byte) 0x12:			// CMOS only
//			if (ver > 3) System.out.println("[ORA(z)]");
//			a |= peek(am_iz());
//			bits_nz(a);
//			cycles = 5;
//			break;
//		case (byte) 0xB2:			// CMOS only
//		a = peek(am_iz());
//		bits_nz(a);
//		if (ver > 3) System.out.println("[LDA(z)]");
//		cycles = 5;
//		break;
//			
//			case (byte) 0x92:			// CMOS only
//				poke(am_iz(), a);
//				if (ver > 3) System.out.println("[STA(z)]");
//				cycles = 5;
//				break;

//	// *** STZ: Store Zero in Memory, CMOS only ***
//			case (byte) 0x9C:
//				poke(am_a(), 0);
//				if (ver > 3) System.out.println("[STZa]");
//				cycles = 4;
//				break;
//			case (byte) 0x64:
//				poke(peek(pc++), 0);
//				if (ver > 3) System.out.println("[STZz]");
//				cycles = 3;
//				break;
//			case (byte) 0x74:
//				poke(am_zx(), 0);
//				if (ver > 3) System.out.println("[STZzx]");
//				cycles = 4;
//				break;
//			case (byte) 0x9E:
//				poke(am_ax(&page), 0);
//				if (ver > 3) System.out.println("[STZx]");
//				cycles = 5;		// ...and not 4, as expected
//				break;			
						
//			case 0x32:			// CMOS only
//			a &= peek(am_iz());
//			bits_nz(a);
//			if (ver > 3) System.out.println("[AND(z)]");
//			cycles = 5;
//			break;			
			
//			case (byte) 0x52:			// CMOS only
//			if (ver > 3) System.out.println("[EOR(z)]");
//			a ^= peek(am_iz());
//			bits_nz(a);
//			cycles = 5;
//			break;
			
//			case 0x72:			// CMOS only
//			adc(peek(am_iz()));
//			if (ver > 3) System.out.println("[ADC(z)]");
//			cycles = 5 + dec;
//			break;

			//		case (byte) 0x1A:			// CMOS only
//			a++;
//			bits_nz(a);
//			if (ver > 3) System.out.println("[INC]");
//			break;	
//		case (byte) 0x3A:			// CMOS only (OK)
//			if (ver > 3) System.out.println("[DEC]");
//			a--;
//			bits_nz(a);
//			break;			
			
//		case (byte) 0xD2:			// CMOS only
//			cmp(a, peek(am_iz()));
//			if (ver > 3) System.out.println("[CMP(z)]");
//			cycles = 5;
//			break;
//		case (byte) 0x1E:
//			super.step();
//			cycles = 6 + page;
//		case (byte) 0x5E:
//			super.step();
//			cycles = 6 + page;
//		case (byte) 0x3E:
//			super.step();
//			cycles = 6 + page;	// 7 for NMOS
//			break;
//		case (byte) 0x7E:
//			super.step();
//			cycles = 6 + page;	// 7 for NMOS
//			break;

//			/* *** TRB: Test and Reset Bits, CMOS only *** */
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
			

//			/* *** *** special control 'opcodes' *** *** */
//			/* *** Emulator Breakpoint  (WAI on WDC) *** */
//					case 0xCB:
////						if (ver)	System.out.println(" Status @ $%x04:", pc-1);	// must allow warnings to display status request
////						stat();
//						run = 1;		// pause execution
//						break;
//			case 0x89:			// CMOS only
//			temp = peek(pc++);
//			p &= 0b11111101;			// pre-clear Z only, is this OK?
//			p |= (a & temp)?0:2;		// set Z accordingly
//			if (ver > 3) System.out.println("[BIT#]");
//			break;
//		case 0x3C:			// CMOS only
//			temp = peek(am_ax(&page));
//			p &= 0b00111101;			// pre-clear N, V & Z
//			p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//			p |= (a & temp)?0:2;		// set Z accordingly
//			if (ver > 3) System.out.println("[BITx]");
//			cycles = 4 + page;
//			break;
//		case 0x34:			// CMOS only
//			temp = peek(am_zx());
//			p &= 0b00111101;			// pre-clear N, V & Z
//			p |= (temp & 0b11000000);	// copy bits 7 & 6 as N & Z
//			p |= (a & temp)?0:2;		// set Z accordingly
//			if (ver > 3) System.out.println("[BITzx]");
//			cycles = 4;
//			break;
//			case 0x7C:			// CMOS only
//			pc = am_aix();
//			if (ver > 2)	System.out.println("[JMP(x)]");
//			cycles = 6;
//			break;
//		case (byte) 0x6C:
//			if (ver > 2)	System.out.println("[JMP()]");
//			pc = am_ai();
//			cycles = 6;		// 5 for NMOS!
//			break;
			/* *** PHX: Push Index X on Stack *** */
//		case (byte) 0xDA:			// CMOS only
//			push(x);
//			if (ver > 3) System.out.println("[PHX]");
//			cycles = 3;
//			break;
//		/* *** PHY: Push Index Y on Stack *** */
//		case (byte) 0x5A:			// CMOS only
//			push(y);
//			if (ver > 3) System.out.println("[PHY]");
//			cycles = 3;
//			break;

//			/* *** PLX: Pull Index X from Stack *** */
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

			
			
			
			/* *** Graceful Halt (STP on WDC) *** */
					case (byte)0xDB:
						System.out.println(" ...HALT!");
						cycles = 0;	// definitively stop execution
						break;
			
		default:
			cycles=super.runOpcode(opcode);
		}
		return cycles;
		
	}
}
