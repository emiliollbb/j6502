package net.emiliollbb.j6502.computers.durango;

import java.io.File;
import java.util.Arrays;

import net.emiliollbb.j6502.chips.Cpu65C02;
import net.emiliollbb.j6502.chips.RamChip;

public class Durango {
	/* 16K RAM $0000 - $7FFF */
	private RamChip ram;
	private DurangoScreen screen;
	/* 32k ROM $8000 - $FFFFF */
	private DurangoRom rom;
	private Cpu65C02 cpu;

	public Durango() throws Exception{
		// 32K RAM
		ram = new RamChip(0x0000, 0x8000);
		// 32K ROM
		rom = new DurangoRom(0x8000, 0x8000, new File("/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/durango/test.bin"));
		// Durango screen		
		screen = new DurangoScreen(4, ram, rom);
		
		cpu = new Cpu65C02(10, Arrays.asList(ram, rom));
		//cpu.setSpeed(1);
		cpu.setVerbose(10);
		cpu.listDevices();
		
		cpu.reset();
		for(int i=0; i<10; i++) {
			cpu.step();
		}
		
	}
	
	public Cpu65C02 getCpu() {
		return cpu;
	}

	public DurangoScreen getScreen() {
		return screen;
	}
	
	public static void main(String[] args) throws Exception {
		 new Durango();
	}
}
