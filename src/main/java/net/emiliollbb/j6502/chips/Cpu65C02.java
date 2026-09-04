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
		int cycles = 2;			// base cycle count
		byte temp;
		int adr;
		switch(opcode) {
		case (byte)0x80:			// CMOS only
			if (ver > 2) System.out.println("["+printWord(pc)+"] [BRA]");
			page=rel(page);
			cycles = 3 + page;
			break;
		// (zp) addressing mode
		case 0x72:			// CMOS only
			adc(peek(am_iz()));
			if (ver > 3) System.out.println("["+printWord(pc)+"] [ADC(z)]");
			cycles = 5 + dec;
			break;
		case 0x32:			// CMOS only
			a &= peek(am_iz());
			bits_nz(a);
			if (ver > 3) System.out.println("["+printWord(pc)+"] [AND(z)]");
			cycles = 5;
			break;			
		case (byte) 0xD2:			// CMOS only
			cmp(a, peek(am_iz()));
			if (ver > 3) System.out.println("["+printWord(pc)+"] [CMP(z)]");
			cycles = 5;
			break;
		case (byte) 0x52:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [EOR(z)]");
			a ^= peek(am_iz());
			bits_nz(a);
			cycles = 5;
			break;
		case (byte) 0xB2:			// CMOS only
			a = peek(am_iz());
			bits_nz(a);
			if (ver > 3) System.out.println("["+printWord(pc)+"] [LDA(z)]");
			cycles = 5;
			break;
		case (byte) 0x12:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [ORA(z)]");
			a |= peek(am_iz());
			bits_nz(a);
			cycles = 5;
			break;
		case (byte) 0xF2:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [SBC(z)]");
			sbc(peek(am_iz()));
			cycles = 5 + dec;
			break;
		case (byte) 0x92:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [STA(z)]");
			poke(am_iz(), a);
			cycles = 5;
			break;
		// BIT
		case (byte) 0x89:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [BIT#]");
			temp = peek(pc++);
			p = (byte)((p&0b00111101)&0x000000FF);			// pre-clear N, V & Z
			p = (byte)((p|(temp&0b11000000))&0x000000FF);	// copy bits 7 & 6 as N & Z
			p=(byte)((a&temp&0x000000FF)==0?p|2:p|0);
			break;
		case (byte) 0x34:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [BITzx]");
			temp = peek(am_zx());
			p = (byte)((p&0b00111101)&0x000000FF);			// pre-clear N, V & Z
			p = (byte)((p|(temp&0b11000000))&0x000000FF);	// copy bits 7 & 6 as N & Z
			p=(byte)((a&temp&0x000000FF)==0?p|2:p|0);
			cycles = 4;
			break;	
		case (byte) 0x3C:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [BITx]");
			temp = peek(am_ax());
			p = (byte)((p&0b00111101)&0x000000FF);			// pre-clear N, V & Z
			p = (byte)((p|(temp&0b11000000))&0x000000FF);	// copy bits 7 & 6 as N & Z
			p=(byte)((a&temp&0x000000FF)==0?p|2:p|0);
			cycles = 4 + page;
			break;
		
		case (byte) 0x3A:			// CMOS only (OK)
			if (ver > 3) System.out.println("["+printWord(pc)+"] [DEC]");
			a--;
			bits_nz(a);
			break;	
		case (byte) 0x1A:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [INC]");
			a++;
			bits_nz(a);
			break;	
		
		case (byte) 0x7C:			// CMOS only
			if (ver > 2)	System.out.println("["+printWord(pc)+"] [JMP(x)]");
			int j=am_a();
			pc= getWord(peek(j+x), peek(j+x+1));
			cycles = 6;
			break;
			
		case (byte) 0xDA:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [PHX]");
			push(x);
			cycles = 3;
			break;
		/* *** PHY: Push Index Y on Stack *** */
		case (byte) 0x5A:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [PHY]");
			push(y);
			cycles = 3;
			break;
		/* *** PLX: Pull Index X from Stack *** */
		case (byte) 0xFA:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [PLX]");
			x = pop();
			bits_nz(x);		// EEEEEEEEEEEEEEEEEEEEK
			cycles = 4;
			break;
		/* *** PLX: Pull Index X from Stack *** */
		case (byte) 0x7A:			// CMOS only
			if (ver > 3) System.out.println("["+printWord(pc)+"] [PLY]");
			y = pop();
			bits_nz(y);		// EEEEEEEEEEEEEEEEEEEEK
			cycles = 4;
			break;

		// *** STZ: Store Zero in Memory, CMOS only ***
		case (byte) 0x64:
			poke(peek(pc++) & 0X000000FF, (byte)0x00);
			if (ver > 3) System.out.println("["+printWord(pc)+"] [STZz]");
			cycles = 3;
			break;
		case (byte) 0x9C:
			poke(am_a(), (byte)0x00);
			if (ver > 3) System.out.println("["+printWord(pc)+"] [STZa]");
			cycles = 4;
			break;
		case (byte) 0x74:
			poke(am_zx(), (byte)0x00);
			if (ver > 3) System.out.println("["+printWord(pc)+"] [STZzx]");
			cycles = 4;
			break;
		case (byte) 0x9E:
			poke(am_ax(), (byte)0x00);
			if (ver > 3) System.out.println("["+printWord(pc)+"] [STZx]");
			cycles = 5;		// ...and not 4, as expected
			break;			
						
		/* *** TRB: Test and Reset Bits, CMOS only *** */
//		case 0x14:
//			if (ver > 3) System.out.println("["+printWord(pc)+"] [TRBz]");
//			adr = peek(pc++);
//			temp = peek(adr);
//			if (temp & a)		p &= 0b11111101;	// set Z accordingly
//			else 				p |= 0b00000010;
//			poke(adr, temp & ~a);
//			cycles = 5;
//			break;	
//		case 0x1C:
//			if (ver > 3) System.out.println("["+printWord(pc)+"] [TRBa]");
//			adr = am_a();
//			temp = peek(adr);
//			if (temp & a)		p &= 0b11111101;	// set Z accordingly
//			else 				p |= 0b00000010;
//			poke(adr, temp & ~a);
//			cycles = 6;
//			break;
			
//			/* *** TSB: Test and Set Bits, CMOS only *** */
//			case 0x04:
//			adr = peek(pc++);
//			temp = peek(adr);
//			if (temp & a)		p &= 0b11111101;	// set Z accordingly
//			else 				p |= 0b00000010;
//			poke(adr, temp | a);
//			if (ver > 3) System.out.println("["+printWord(pc)+"] [TSBz]");
//			cycles = 5;
//			break;
//			case 0x0C:
//				adr = am_a();
//				temp = peek(adr);
//				if (temp & a)		p &= 0b11111101;	// set Z accordingly
//				else 				p |= 0b00000010;
//				poke(adr, temp | a);
//				if (ver > 3) System.out.println("["+printWord(pc)+"] [TSBa]");
//				cycles = 6;
//				break;			

		case (byte) 0x1E:
			super.step();
			cycles = 6 + page;
		case (byte) 0x5E:
			super.step();
			cycles = 6 + page;
		case (byte) 0x3E:
			super.step();
			cycles = 6 + page;	// 7 for NMOS
			break;
		case (byte) 0x7E:
			super.step();
			cycles = 6 + page;	// 7 for NMOS
			break;

//			/* *** *** special control 'opcodes' *** *** */
//			/* *** Emulator Breakpoint  (WAI on WDC) *** */
//					case 0xCB:
////						if (ver)	System.out.println(" Status @ $%x04:", pc-1);	// must allow warnings to display status request
////						stat();
//						run = 1;		// pause execution
//						break;


//		case (byte) 0x6C:
//			if (ver > 2)	System.out.println("["+printWord(pc)+"] [JMP()]");
//			pc = am_ai();
//			cycles = 6;		// 5 for NMOS!
//			break;
			/* *** PHX: Push Index X on Stack *** */

			
			
			
			/* *** Graceful Halt (STP on WDC) *** */
					case (byte)0xDB:
						if (ver > 3) System.out.println("["+printWord(pc)+"] [HALT]");
						System.out.println(" ...HALT!");
						cycles = 0;	// definitively stop execution
						break;
			
		default:
			cycles=super.runOpcode(opcode);
		}
		return cycles;
		
	}
}
