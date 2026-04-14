package net.emiliollbb.j6502.chips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import net.emiliollbb.j6502.interfaces.IBusDevice;

public class RomChip extends AbstractBusDevice implements IBusDevice {
	private byte[] rom;
	
	public RomChip(int startAddress, int size, byte[] data) {
		super(startAddress, size);
		rom=new byte[size];
		load(data);
	}
	
	public RomChip(int startAddress, int size, File rom) throws FileNotFoundException, IOException {
		this(startAddress, size, Files.readAllBytes(rom.toPath()));
	}
	
	private void load(byte[] data) {
		for(int i=0; i<size; i++) {
			rom[i]=data[i];
		}
	}

	@Override
	protected byte ioRead(int addr) {
		return rom[addr];
	}

	@Override
	protected void ioWrite(int addr, byte data) {
		// Do nothing. Rom can NOT be modified
	}

}
