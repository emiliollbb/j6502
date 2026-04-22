package net.emiliollbb.j6502.computers.trainer;

import net.emiliollbb.j6502.chips.Cpu6502;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.chips.ScreenDriver;
import net.emiliollbb.j6502.computers.hid.ConsoleOutput;
import net.emiliollbb.j6502.computers.hid.VirtualScreen;

public class Trainer {
	private RamChip ram;
	private VirtualScreen screen;
	private ScreenDriver screenDriver;
	private ConsoleOutput console;
	private RomChip rom;
	private Cpu6502 cpu6502;

	public Trainer() {
		// 16K RAM
		ram = new RamChip(0x0000, 0x4000);
		// 16k SCREEN
		screen = new VirtualScreen(5);
		screenDriver = new ScreenDriver(0x4000, 0x4000);
		screenDriver.setVirtualScreen(screen);
		// 16k DEVICES
		console = new ConsoleOutput(0x8000);
		// 16K ROM
		rom = new RomChip(0xc000, 0x8000, new byte[0x8000]);
		
		cpu6502 = new Cpu6502();
	}
}
