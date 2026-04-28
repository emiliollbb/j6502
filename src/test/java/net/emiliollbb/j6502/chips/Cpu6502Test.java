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
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testLDXInmediateZero() {
		loadProgram(0x0200, new int[] {0xA2, 0x00});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x00, cpu.getX());
		Assertions.assertEquals(0x02, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testLDXInmediateNegative() {
		loadProgram(0x0200, new int[] {0xA2, 0xA0});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0xA0, cpu.getX());
		Assertions.assertEquals((byte) 0x80, cpu.getP());
		Assertions.assertEquals(2, cycles);
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
	void testLDXAbsolute() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xAE, 0x05, 0x03});
		cpu.reset();
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
	void testLDYInmediate() {
		loadProgram(0x0200, new int[] {
				// LDY #$55
				0xA0, 0x55,
				// LDY #$00
				0xA0, 0x00,
				// LDY #$F0
				0xA0, 0xF0,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getY());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(2, cycles);
		// Zero
		cycles = cpu.step();
		Assertions.assertEquals(0x00, cpu.getY());
		Assertions.assertEquals(0x02, cpu.getP());
		Assertions.assertEquals(2, cycles);
		// Negative
		cycles = cpu.step();
		Assertions.assertEquals((byte) 0xF0, cpu.getY());
		Assertions.assertEquals((byte) 0x80, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testLDYZeroPage() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xA4, 0x05});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getY());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testLDYZeroPageX() {
		Mockito.when(device.peek(0x008F)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDY $80,X
				0xB4, 0x80});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getY());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDYAbsolute() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xAC, 0x05, 0x03});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getY());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDYAbsoluteY() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #04
				0xA2, 0x04,
				// LDY $0301,X
				0xBC, 0x01, 0x03});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getY());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testSTXZeroPage() {
		loadProgram(0x0200, new int[] {
				// LDX #$55
				0xA2, 0x55,
				0X86, 0XA0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(3, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0x55);
	}
	@Test
	void testSTXZeroPageY() {
		loadProgram(0x0200, new int[] {
				// LDX #$55
				0xA2, 0x55,
				// LDY #$A1
				0xA0, 0xA1,
				// STX $09,Y
				0X96, 0X09
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0x55);
	}
	@Test
	void testSTXAbsolute() {
		loadProgram(0x0200, new int[] {
				// LDX #$55
				0xA2, 0x55,
				// STX $0301
				0X8E, 0XA1, 0XA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0x55);
	}
	

	@Test
	void testSTYZeroPage() {
		loadProgram(0x0200, new int[] {
				// LDY #$55
				0xA0, 0x55,
				0X84, 0XA0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(3, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0x55);
	}
	@Test
	void testSTYZeroPageX() {
		loadProgram(0x0200, new int[] {
				// LDY #$55
				0xA0, 0x55,
				// LDX #$A1
				0xA2, 0xA1,
				// STY $09,X
				0X94, 0X09
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0x55);
	}
	@Test
	void testSTYAbsolute() {
		loadProgram(0x0200, new int[] {
				// LDY #$55
				0xA0, 0x55,
				// STY $0301
				0X8C, 0XA1, 0XA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0x55);
	}
	
	
	
	@Test
	void tesTAX() {
		loadProgram(0x0200, new int[] {
				// LDA #04
				0xA9, 0x54,
				// TAX
				0xAA});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x54, cpu.getX());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesTXA() {
		loadProgram(0x0200, new int[] {
				// LDX #54
				0xA2, 0x54,
				// TXA
				0x8A});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x54, cpu.getA());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesDEX() {
		loadProgram(0x0200, new int[] {
				// LDX #54
				0xA2, 0x54,
				// DEX
				0xCA});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x53, cpu.getX());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesINX() {
		loadProgram(0x0200, new int[] {
				// LDX #54
				0xA2, 0x54,
				// INX
				0xE8});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getX());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesTAY() {
		loadProgram(0x0200, new int[] {
				// LDA #54
				0xA9, 0x54,
				// TAY
				0xA8});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x54, cpu.getY());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesTYA() {
		loadProgram(0x0200, new int[] {
				// LDY #54
				0xA0, 0x54,
				// TYA
				0x98});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x54, cpu.getA());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesDEY() {
		loadProgram(0x0200, new int[] {
				// LDY #54
				0xA0, 0x54,
				// DEY
				0x88});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x53, cpu.getY());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void tesINY() {
		loadProgram(0x0200, new int[] {
				// LDY #54
				0xA0, 0x54,
				// INY
				0xC8});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getY());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testLDAInmediate() {
		loadProgram(0x0200, new int[] {0xA9, 0x55});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testANDInmediate() {
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// AND #$34
				0X29, 0x34
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testANDZeroPage() {
		Mockito.when(device.peek(0x0003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// AND #$03
				0X25, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testANDZeroPageX() {
		Mockito.when(device.peek(0x0012)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// AND #$03
				0x35, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testANDAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// AND $F003
				0X2D, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testANDAbsoluteX() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// AND $01,X
				0x3D, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testANDAbsoluteY() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// AND $01,Y
				0x39, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testANDIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// AND ($03,X)
				0x21, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testANDIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// AND ($05),Y
				0x31, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x14, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(5, cycles);
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
