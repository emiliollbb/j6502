package net.emiliollbb.j6502.computers.durango;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.emiliollbb.j6502.chips.RomChip;

public class DurangoRom extends RomChip{
	public DurangoRom(int startAddress, int size, byte[] data) {
		super(startAddress, size, data);
	}
	public DurangoRom(int startAddress, int size, File rom) throws FileNotFoundException, IOException {
		super(startAddress, size, rom);
	}
	
	@Override
	protected void ioWrite(int addr, byte data) {
		// IO space
		if(addr+0x8000==0XDF80) {
			rom[addr]=data;
		}
		else {
			// Do nothing. Rom can NOT be modified
		}
	}

}
