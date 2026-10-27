package net.emiliollbb.j6502.computers.zacatecas;

import java.io.File;
import java.util.Arrays;

import net.emiliollbb.j6502.chips.Cpu65C02;
import net.emiliollbb.j6502.chips.PreloadedRamChip;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.chips.ZacaVia;
import net.emiliollbb.j6502.computers.hid.LCD16x2;

public class Zacatecas {
	/* 32K RAM $0000 - $7FFF */
	private RamChip ram;
	private ZacaVia via;
	/* 16k ROM $C000 - $FFFFF */
	private RomChip rom;
	private Cpu65C02 cpu;
	private LCD16x2 lcd;

	public Zacatecas() throws Exception{
		// 32K RAM
		//ram = new RamChip(0x0000, 0x8000);
		ram = new PreloadedRamChip(0x0000, 0x8000, 
				new File("/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/zacatecas/hello.bin"));
		lcd = new LCD16x2();
		via= new ZacaVia();
		via.setLcd(lcd);
		// 16K ROM
		// proyectos/j6502/workspace/j6502/src/main/asm/zacatecas.rom
		rom = new RomChip(0xC000, 0x4000, new File(
				"/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/zacatecas.rom"));
		
		cpu = new Cpu65C02(1000, Arrays.asList(ram, via, rom));
		cpu.setVerbose(3);
		via.setVerbose(0);
		lcd.setVerbose(0);
		cpu.listDevices();
		
		cpu.reset();
	}
	
	public Cpu65C02 getCpu() {
		return cpu;
	}
	
	public LCD16x2 getLcd() {
		return lcd;
	}

	public static void main(String[] args) throws Exception {
		Zacatecas zacatecas = new Zacatecas();
		zacatecas.getCpu().setVerbose(3);
		zacatecas.getCpu().runUntilBrk();
		System.out.println(String.format("0x%04X", zacatecas.getCpu().getPc()));
	}
}
