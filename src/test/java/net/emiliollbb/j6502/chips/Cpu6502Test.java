package net.emiliollbb.j6502.chips;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.emiliollbb.j6502.interfaces.IBusDevice;

@ExtendWith(MockitoExtension.class)
public class Cpu6502Test {
	private Cpu6502 cpu;
	@Mock
	private IBusDevice device;
	
	@BeforeEach
	void init() {
		cpu = new Cpu6502(0, Arrays.asList(device));
		cpu.setVerbose(10);
		Mockito.when(device.isInRange(Mockito.anyInt())).thenReturn(true);
	}
	
	private void loadProgram(int begin, int[] data) {
		//pc = peek(0xFFFC) | peek(0xFFFD)<<8 & 0x0000FFFF;
		Mockito.when(device.peek(0xFFFC)).thenReturn((byte)(begin & 0x000000FF));
		Mockito.when(device.peek(0xFFFD)).thenReturn((byte)((begin & 0x0000FF00) >>8));
		for(int i=0; i<data.length; i++) {
			Mockito.when(device.peek(begin+i)).thenReturn((byte)data[i]);
		}
	}
	
	@Test
	void testReset() {
		loadProgram(0xC000, new int[] {});
		cpu.reset();
		printByte(cpu.getPc());
		Assertions.assertEquals(0xC000, cpu.getPc());
	}
	
	@Test
	void testReset2() {
		loadProgram(0xC001, new int[] {});
		cpu.reset();
		printByte(cpu.getPc());
		Assertions.assertEquals(0xC001, cpu.getPc());
	}
	
	@Test
	void testLDXInmediate() {
		loadProgram(0x0200, new int[] {0xA2, 0x55});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(2, cycles);
	}
	
	@Test
	void testLDXAbsolute() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xAE, 0x05, 0x03});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDXZeroPage() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xA6, 0x05});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testLDXZeroPageY() {
		Mockito.when(device.peek(0x008F)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDY #0F
				0xA0, 0x0F,
				// LDX $80,y
				0xB6, 0x80});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDXAbsoluteY() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDY #04
				0xA0, 0x04,
				// LDX $0301,Y
				0xBE, 0x01, 0x03});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(4, cycles);
	}
	
	@Test
	void testBRA() {
		loadProgram(0x0200, new int[] {0x80, 0x01});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x203, cpu.getPc());
		Assertions.assertEquals(3, cycles);
	}
	
	@Test
	void testBRA_page() {
		loadProgram(0x02F0, new int[] {0x80, 20});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x306, cpu.getPc());
		Assertions.assertEquals(4, cycles);
	}
	
	private String printByte(byte b) {
		return String.format("0x%02X", b)+ "("+b+")";
	}
	private String printByte(int b) {
		return String.format("0x%02X", b)+ "("+b+")";
	}
}
