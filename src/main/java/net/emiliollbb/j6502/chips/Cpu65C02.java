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
	public int step() {
		int cycles = 2;			// base cycle count
		page = 0;			// page boundary flag, for speed penalties
		byte opcode, temp;
		short adr;

		opcode = peek(pc++);	// get opcode and point to next one (or operand)
		if(ver>5) System.out.println("OPCODE: "+printByte(opcode));
		switch(opcode) {
		case (byte)0x80:			// CMOS only
			if (ver > 2) System.out.println("[BRA]");
			page=rel(page);
			cycles = 3 + page;
			break;
//		case (byte) 0xB2:			// CMOS only
//		a = peek(am_iz());
//		bits_nz(a);
//		if (ver > 3) System.out.println("[LDA(z)]");
//		cycles = 5;
//		break;		
		default:
			pc--;
			cycles=super.step();
		}
		return cycles;
		
	}
}
