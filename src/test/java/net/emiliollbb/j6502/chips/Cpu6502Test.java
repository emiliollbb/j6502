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
	
	@Test
	void testLDX() {
		Mockito.when(device.peek(0xFFFC)).thenReturn((byte)0x00);
		Mockito.when(device.peek(0xFFFD)).thenReturn((byte)0x02);
		Mockito.when(device.peek(0x0200)).thenReturn((byte)0xA2);
		Mockito.when(device.peek(0x0201)).thenReturn((byte)0x55);
		cpu.reset();
		cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
	}
	
	@Test
	void testBRA() {
		cpu.reset();
		cpu.step();
	}
}
