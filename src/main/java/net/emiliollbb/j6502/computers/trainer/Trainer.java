package net.emiliollbb.j6502.computers.trainer;

import java.io.File;

import net.emiliollbb.j6502.chips.Cpu6502;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.chips.ScreenDriver;
import net.emiliollbb.j6502.computers.hid.ConsoleOutput;
import net.emiliollbb.j6502.computers.hid.VirtualScreen;

public class Trainer {
	/* 16K RAM $0000 - $3FFF */
	private RamChip ram;
	/* 16K VIDEO RAM $4000 - $7FFF */
	private VirtualScreen screen;
	private ScreenDriver screenDriver;
	/* 16K Devices $8000 - BFFF */
	private ConsoleOutput console;
	/* 16k ROM $C000 - $FFFFF */
	private RomChip rom;
	private Cpu6502 cpu;

	public Trainer() throws Exception{
		// 16K RAM
		ram = new RamChip(0x0000, 0x4000);
		// 16k SCREEN
		screen = new VirtualScreen(5);
		screenDriver = new ScreenDriver(0x4000, 0x4000);
		screenDriver.setVirtualScreen(screen);
		// 16k DEVICES
		console = new ConsoleOutput(0x8000);
		// 16K ROM
		System.out.println("Size: "+0x4000);
		rom = new RomChip(0xc000, 0x4000, new File("/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/trainer_test.bin"));
		
		cpu = new Cpu6502();
		cpu.setVerbose(10);
		cpu.getBusDevices().add(ram);
		cpu.getBusDevices().add(screenDriver);
		cpu.getBusDevices().add(console);
		cpu.getBusDevices().add(rom);
		cpu.listDevices();
		
		cpu.reset();
		System.out.println(cpu.step());
		System.out.println(cpu.step());
		//System.out.println(cpu.step());
		//System.out.println(cpu.step());
	}

	public VirtualScreen getScreen() {
		return screen;
	}
	
	public static void main(String[] args) throws Exception {
		 new Trainer();
	}
}
