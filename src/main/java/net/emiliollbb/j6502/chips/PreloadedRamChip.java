package net.emiliollbb.j6502.chips;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PreloadedRamChip extends RamChip {

	public PreloadedRamChip(int startAddress, int size, File rom) throws IOException {
		super(startAddress, size);
		load(0x4000, Files.readAllBytes(rom.toPath()));
	}
	
	private void load(int startAddress, byte[] data) {
		for(int i=0; i<data.length; i++) {
			poke(startAddress+i, data[i]);
		}
	}

}
