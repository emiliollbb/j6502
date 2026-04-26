package net.emiliollbb.j6502.computers.trainer;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import net.emiliollbb.j6502.chips.Cpu6502;
import net.emiliollbb.j6502.chips.RamChip;
import net.emiliollbb.j6502.chips.RomChip;
import net.emiliollbb.j6502.chips.ScreenDriver;
import net.emiliollbb.j6502.computers.hid.ConsoleOutput;
import net.emiliollbb.j6502.computers.hid.VirtualScreen;

public class TrainerBench {
	private RamChip ram;
	private RomChip rom;
	private Cpu6502 cpu;

	public TrainerBench() throws Exception{
		// 48K RAM $0000 - $7FFF 
		ram = new RamChip(0x0000, 0xc000);
		/* 16k ROM $C000 - $FFFFF */
		rom = new RomChip(0xc000, 0x4000, new File("/home/emilio/proyectos/j6502/workspace/j6502/src/main/asm/trainer_test.bin"));
		
		cpu = new Cpu6502(1000, Arrays.asList(ram, rom));
		cpu.setVerbose(10);
		cpu.listDevices();
		
		cpu.reset();
		//new Thread(cpu).start();
		long cycles = 0;
		cpu.setVerbose(0);
		Instant start = Instant.now();
		for(int i=0; i<1000000; i++) {
			cycles+=cpu.step();
		}
		Instant end = Instant.now();
		System.out.println("Cycles: "+cycles+". millis; "+start.until(end, ChronoUnit.MILLIS));
		
	}

	public static void main(String[] args) throws Exception {
		 new TrainerBench();
	}
}
