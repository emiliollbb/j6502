package net.emiliollbb.j6502.computers.trainer;

import java.io.File;
import java.util.Arrays;

import net.emiliollbb.j6502.chips.Cpu65C02;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.computers.hid.CLIDevice;

public class CliTrainer {
	private Cpu65C02 cpu;

	public CliTrainer(String img) throws Exception{
		
		cpu = new Cpu65C02(10, Arrays.asList(
				/* 32K RAM $0000 - $7FFF */
				new RamChip(0x0000, 0x8000), 
				/* 16K Devices $8000 - BFFF */
				new CLIDevice(0x8000, System.out, System.in), 
				/* 16k ROM $C000 - $FFFFF */
				new RomChip(0xC000, 0x4000, 
						new File(img))));
		//cpu.setVerbose(10);
	}
	
	public void run() {
		cpu.reset();
		while(cpu.step()>0);
	}

	public static void main(String[] args) throws Exception {
		new CliTrainer(args[0]).run();
	}
}
