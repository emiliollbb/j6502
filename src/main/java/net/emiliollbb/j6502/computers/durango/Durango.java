package net.emiliollbb.j6502.computers.durango;

import java.io.File;
import java.util.Arrays;

import net.emiliollbb.j6502.chips.Cpu65C02;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.chips.ScreenDriver;
import net.emiliollbb.j6502.computers.hid.ConsoleOutput;
import net.emiliollbb.j6502.computers.hid.VirtualScreen;

public class Durango {
	/* 16K RAM $0000 - $7FFF */
	private RamChip ram;
	private DurangoScreen screen;
	/* 32k ROM $8000 - $FFFFF */
	private RomChip rom;
	private Cpu65C02 cpu;

	public Durango() throws Exception{
		// 32K RAM
		ram = new RamChip(0x0000, 0x8000);
		screen = new DurangoScreen(8, ram);
		// 32K ROM
		System.out.println("Size: "+0x4000);
		rom = new RomChip(0x8000, 0x8000, new File("/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/durango/test.bin"));
		
		cpu = new Cpu65C02(10, Arrays.asList(ram, rom));
		cpu.setVerbose(10);
		cpu.listDevices();
		
		cpu.reset();
		//new Thread(cpu).start();
		
	}

	public DurangoScreen getScreen() {
		return screen;
	}
	
	public static void main(String[] args) throws Exception {
		 new Durango();
	}
}
