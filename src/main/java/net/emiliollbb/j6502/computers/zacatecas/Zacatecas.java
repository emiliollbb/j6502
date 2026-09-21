package net.emiliollbb.j6502.computers.zacatecas;

import java.io.File;
import java.util.Arrays;

import net.emiliollbb.j6502.chips.Cpu65C02;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.chips.ZacaVia;
import net.emiliollbb.j6502.computers.hid.LCD16x2;

public class Zacatecas {
	/* 32K RAM $0000 - $7FFF */
	private RamChip ram;
	private ZacaVia via;
	/* 32k ROM $8000 - $FFFFF */
	private RomChip rom;
	private Cpu65C02 cpu;

	public Zacatecas() throws Exception{
		// 32K RAM
		ram = new RamChip(0x0000, 0x8000);
		LCD16x2 lcd = new LCD16x2();
		via= new ZacaVia();
		via.setLcd(lcd);
		// 32K ROM
		rom = new RomChip(0xC000, 0x4000, new File("/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/zacatecas.rom"));
		
		cpu = new Cpu65C02(10, Arrays.asList(ram, via, rom));
		cpu.setSpeed(10);
		cpu.setVerbose(0);
		via.setVerbose(0);
		lcd.setVerbose(5);
		cpu.listDevices();
		
		cpu.reset();
	}
	
	public Cpu65C02 getCpu() {
		return cpu;
	}

	public static void main(String[] args) throws Exception {
		Zacatecas zacatecas = new Zacatecas();
		for(int i=0; i<200; i++) {
			zacatecas.getCpu().step();
		}
	}
}
