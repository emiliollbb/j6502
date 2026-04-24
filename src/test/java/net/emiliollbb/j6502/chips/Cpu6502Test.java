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
		Mockito.when(device.peek(0xFFFC)).thenReturn((byte)(begin & 0x0000FFFF));
		Mockito.when(device.peek(0xFFFD)).thenReturn((byte)(begin >>8 & 0x0000FFFF));
		for(int i=0; i<data.length; i++) {
			Mockito.when(device.peek(0x0200+i)).thenReturn((byte)data[i]);
		}
	}
	
	@Test
	void testLDX() {
		loadProgram(0x200, new int[] {0xA2, 0x55});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(2, cycles);
	}
	
	@Test
	void testBRA() {
		Mockito.when(device.peek(0xFFFC)).thenReturn((byte)0x00);
		Mockito.when(device.peek(0xFFFD)).thenReturn((byte)0x02);
		Mockito.when(device.peek(0x0200)).thenReturn((byte)0x80);
		Mockito.when(device.peek(0x0201)).thenReturn((byte)0x01);
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x203, cpu.getPc());
		Assertions.assertEquals(3, cycles);
	}
}
