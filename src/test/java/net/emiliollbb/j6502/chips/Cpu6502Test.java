package net.emiliollbb.j6502.chips;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.LenientStubber;

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
		loadProgram(begin, data, false);
	}
	
	private void loadProgram(int begin, int[] data, boolean lenient) {
		//pc = peek(0xFFFC) | peek(0xFFFD)<<8 & 0x0000FFFF;
		Mockito.when(device.peek(0xFFFC)).thenReturn((byte)(begin & 0x000000FF));
		Mockito.when(device.peek(0xFFFD)).thenReturn((byte)((begin & 0x0000FF00) >>8));
		for(int i=0; i<data.length; i++) {
			if(lenient) {
				Mockito.lenient().when(device.peek(begin+i)).thenReturn((byte)data[i]);
			}
			else {
				Mockito.when(device.peek(begin+i)).thenReturn((byte)data[i]);
			}
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
	void testNOP() {
		loadProgram(0xC000, new int[] {0xEA});
		cpu.reset();
		cpu.step();
		Mockito.verifyNoMoreInteractions(device);
	}
	
	@ParameterizedTest
	@CsvSource({
		"0x55,0x00",
		"0x00,0x02",
		"0xA5,0x80",
		})
	void testLDXInmediate(int value, int p) {
		loadProgram(0x0200, new int[] {0xA2, value});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)value, cpu.getX());
		Assertions.assertEquals((byte)p, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	
	@Test
	void testLDXInmediateNegative() {
		loadProgram(0x0200, new int[] {
				// LDX #-96
				0xA2, 0xA0});
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
				// STY $A0
				0x84, 0xA0
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
				0X94, 0x09
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
				// STY $A3A1
				0X8C, 0xA1, 0xA3
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
	@ParameterizedTest
	@CsvSource({
		"0x55,0x00",
		"0x00,0x02",
		"-91,-128",
		"-84,-128"
		})
	void testLDAInmediate(byte value, byte p) {
		loadProgram(0x0200, new int[] {0xA9, value});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)value, cpu.getA());
		Assertions.assertEquals((byte)p, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testLDAZeroPage() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xA5, 0x05});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testLDAZeroPageX() {
		Mockito.when(device.peek(0x00AA)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// LDA $09,X
				0XB5, 0X09
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDAAbsolute() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {0xAD, 0x05, 0x03});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDAAbsoluteX() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$02
				0xA2, 0x04,
				0xBD, 0x01, 0x03});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testLDAAbsoluteY() {
		Mockito.when(device.peek(0x0305)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDY #$02
				0xA0, 0x04,
				0xB9, 0x01, 0x03});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	
	@ParameterizedTest
	@CsvSource({
		"0x55,0x34,0x14,0x00",
		})
	void testANDInmediate(byte acumulator, byte operand,
			byte expectedAcumulator, byte expectedFlags) {
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, acumulator,
				// AND #$34
				0X29, operand
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedAcumulator, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
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
	void testLDAIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA ($03,X)
				0xA1, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testLDAIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA ($05),Y
				0xB1, 0x05
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x55, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(5, cycles);
	}
	@Test
	void testSTAZeroPage() {
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				0X85, 0XA0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(3, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0x55);
	}
	@Test
	void testSTAZeroPageX() {
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// STA #$03
				0x95, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
		Mockito.verify(device).poke(0x0012, (byte)0x55);
	}
	@Test
	void testSTAAbsolute() {
		loadProgram(0x0200, new int[] {
				// LDa #$55
				0xA9, 0x55,
				// STA $A301
				0x8D, 0xA1, 0xA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(4, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0x55);
	}
	@Test
	void testSTAAbsoluteX() {
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// STA $A301,X
				0x9D, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0x55);
	}
	@Test
	void testSTAAbsoluteY() {
		loadProgram(0x0200, new int[] {
				// LDY #0F
				0xA0, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// STA $A301,Y
				0x99, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0x55);
	}
	@Test
	void testSTAIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// STA ($03,X)
				0x81, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xF003, (byte)0x55);
	}
	@Test
	void testSTAIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// STA ($05),Y
				0x91, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xF003, (byte)0x55);
	}
	
	@Test
	void testINCZeroPage() {
		Mockito.when(device.peek(0xA0)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// INC $A0
				0xE6, 0xA0
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0x56);
	}
	@Test
	void testINCZeroPageX() {
		Mockito.when(device.peek(0xAA)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// INC $09,X
				0xF6, 0x09
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0x56);
	}
	@Test
	void testINCAbsolute() {
		Mockito.when(device.peek(0xA3A1)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// INC $A3A1
				0xEE, 0xA1, 0xA3,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0x56);
	}
	@Test
	void testINCAbsoluteX() {
		Mockito.when(device.peek(0xA312)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// INC $A301,X
				0xFE, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(7, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0x56);
	}
	
	@Test
	void testDECZeroPage() {
		Mockito.when(device.peek(0xA0)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// INC $A0
				0xC6, 0xA0
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0x54);
	}
	@Test
	void testDECZeroPageX() {
		Mockito.when(device.peek(0xAA)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// INC $09,X
				0xD6, 0x09
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0x54);
	}
	@Test
	void testDECAbsolute() {
		Mockito.when(device.peek(0xA3A1)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// INC $A3A1
				0xCE, 0xA1, 0xA3,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0x54);
	}
	@Test
	void testDECAbsoluteX() {
		Mockito.when(device.peek(0xA312)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// INC $A301,X
				0xDE, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(7, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0x54);
	}
	
	@Test
	void testORAInmediate() {
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// ORA #$34
				0X09, 0x34
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testORAZeroPage() {
		Mockito.when(device.peek(0x0003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// OR #$03
				0X05, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testORAZeroPageX() {
		Mockito.when(device.peek(0x0012)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// OR #$03
				0x15, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testORAAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// OR $F003
				0X0D, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testORAAbsoluteX() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// OR $01,X
				0x1D, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testORAAbsoluteY() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// OR $01,Y
				0x19, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testORAIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// OR ($03,X)
				0x01, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testORAIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// OR ($05),Y
				0x11, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x75, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(5, cycles);
	}
	
	@Test
	void testEORInmediate() {
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// EOR #$34
				0X49, 0x34
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testEORZeroPage() {
		Mockito.when(device.peek(0x0003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// EOR #$03
				0X45, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testEORZeroPageX() {
		Mockito.when(device.peek(0x0012)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// EOR #$03
				0x55, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testEORAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// EOR $F003
				0X4D, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testEORAbsoluteX() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// EOR $01,X
				0x5D, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testEORAbsoluteY() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// EOR $01,Y
				0x59, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testEORIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// EOR ($03,X)
				0x41, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testEORIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// EOR ($05),Y
				0x51, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x61, cpu.getA());
		Assertions.assertEquals(0x00, cpu.getP());
		Assertions.assertEquals(5, cycles);
	}
	@Test
	void testSECandCLC() {
		loadProgram(0x0200, new int[] {
				// SEC CLC
				0x38, 0x18,
				});
		cpu.reset();
		cpu.step();
		Assertions.assertEquals(0x01, cpu.getP());
		cpu.step();
		Assertions.assertEquals(0x00, cpu.getP());
	}
	@Test
	void testSEIandCLI() {
		loadProgram(0x0200, new int[] {
				// SEI CLI
				0x78, 0x58,
				});
		cpu.reset();
		cpu.step();
		Assertions.assertEquals(0x04, cpu.getP());
		cpu.step();
		Assertions.assertEquals(0x00, cpu.getP());
	}
	@Test
	void testSEDandCLD() {
		loadProgram(0x0200, new int[] {
				// SED CLD
				0xF8, 0xD8,
				});
		cpu.reset();
		cpu.step();
		Assertions.assertEquals(0x08, cpu.getP());
		cpu.step();
		Assertions.assertEquals(0x00, cpu.getP());
	}
	
	@ParameterizedTest
	@CsvSource({
		"85,17,false,102,0,2",
		"85,17,true,103,0,2",
		"-100,10,false,-90,-128,2",
		"120,8,false,-128,-64,2",
		})
	void testADCInmediate(byte acumulator, byte operand, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// CLC/SEC,       LDA acumulator
				carry?0x38:0x18, 0xA9, acumulator, 
				// ADC operand
				0x69, operand
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testADCZeroPage() {
		//0x55 + 0x34
		Mockito.when(device.peek(0x0003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// ADC #$03
				0X65, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testADCZeroPageX() {
		Mockito.when(device.peek(0x0012)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// ADC #$03
				0x75, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testADCAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// ADC $F003
				0X6D, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testADCAbsoluteX() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		// 85+52=137
		// 0x55+0x34=0x89 // C=0
		// 85+52=-119 // V=1 N=1
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// ADC $01,X
				0x7D, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testADCAbsoluteY() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// ADC $01,Y
				0x79, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testADCIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// ADC ($03,X)
				0x61, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testADCIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// ADC ($05),Y
				0x71, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x89, cpu.getA());
		Assertions.assertEquals((byte)0xC0, cpu.getP());
		Assertions.assertEquals(5, cycles);
	}
	@ParameterizedTest
	@CsvSource({
		// 23+12=35
		"35,18,false,53,0,3",
		})
	void testADCInmediateDEC(byte acumulator, byte operand, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				//SED
				0xF8,
				// CLC/SEC,       LDA acumulator
				carry?0x38:0x18, 0xA9, acumulator, 
				// ADC operand
				0x69, operand,
				//CLD
				0xD8
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	
	@ParameterizedTest
	@CsvSource({
		"85,17,true,68,1,2",
		})
	void testSBCInmediate(byte acumulator, byte operand, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// CLC/SEC,       LDA acumulator
				carry?0x38:0x18, 0xA9, acumulator, 
				// SBC operand
				0xE9, operand
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testSBCZeroPage() {
		//0x55 - 0x34
		Mockito.when(device.peek(0x0003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC #$03
				0XE5, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testSBCZeroPageX() {
		Mockito.when(device.peek(0x0012)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC #$03
				0xF5, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testSBCAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC $F003
				0XED, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testSBCAbsoluteX() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC $01,X
				0xFD, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testSBCAbsoluteY() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC $01,Y
				0xF9, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testSBCIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC ($03,X)
				0xE1, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testSBCIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x34);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// SEC
				0x38,
				// SBC ($05),Y
				0xF1, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x21, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(5, cycles);
	}	
	@ParameterizedTest
	@CsvSource({
		// 23-13=10; C=1 V=0 N=0
		"35,19,true,16,1,3",
		})
	void testSBCInmediateDEC(byte acumulator, byte operand, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				//SED
				0xF8,
				// CLC/SEC,       LDA acumulator
				carry?0x38:0x18, 0xA9, acumulator, 
				// SBC operand
				0xE9, operand,
				//CLD
				0xD8
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	
	/*
	 * 
    UNSIGNED COMPARISON
    NVBDIZC
    If X != operand then Z = 0 
    If X == operand then Z = 1  
    If X < operand then C = 0  
    If X >= operand then C = 1  
	 */
	/*
	 If X == OPERAND => Z=1; C=1;
	 If X < OPERAND  => Z=0; C=0;
	 If X > OPERAND  => Z=0; C=1;
	 */
	/*
	 X == OPERAND => Z=1
	 X != OPERAND => Z=0
	 X <  OPERAND => C=0;
	 X >= OPERAND => C=1;
	 */
	@ParameterizedTest
	@CsvSource({
		"0x55,0x55,0x03,2", // X=OP
		"0x55,0x54,0x01,2", // X>OP
		"0x55,0x56,-128,2", // X<OP
		})
	void testCPXInmediate(byte xreg, byte operand,
			byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// LDX value
				0xA2, xreg,
				// CPX n});
				0xE0, operand});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)xreg, cpu.getX());
		Assertions.assertEquals((byte)expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testCPXZeroPage() {
		Mockito.when(device.peek(0x0023)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX
				0xA2, 0x56,
				// CPX $23
				0xE4, 0x23
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x56, cpu.getX());
		Assertions.assertEquals((byte) 0x01, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testCPXAbsolute() {
		Mockito.when(device.peek(0x0523)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX
				0xA2, 0x56,
				// CPX $0523
				0xEC, 0x23, 0x05
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x56, cpu.getX());
		Assertions.assertEquals((byte) 0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	
	@Test
	void testCPYInmediate() {
		loadProgram(0x0200, new int[] {
				// LDX
				0xA0, 0x56,
				// CPY $23
				0xC0, 0x55
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x56, cpu.getY());
		Assertions.assertEquals((byte) 0x01, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testCPYZeroPage() {
		Mockito.when(device.peek(0x0023)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX
				0xA0, 0x56,
				// CPY $23
				0xC4, 0x23
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x56, cpu.getY());
		Assertions.assertEquals((byte) 0x01, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testCPYAbsolute() {
		Mockito.when(device.peek(0x0523)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX
				0xA0, 0x56,
				// CPY $0523
				0xCC, 0x23, 0x05
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x56, cpu.getY());
		Assertions.assertEquals((byte) 0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	
	@Test
	void testCMPInmediate() {
		loadProgram(0x0200, new int[] {
				// LDA acumulator
				0xA9, 0x56, 
				// CMP operand
				0xC9, 0x55
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x56, cpu.getA());
		Assertions.assertEquals(0x01, cpu.getP());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testCMPZeroPage() {
		//0x55 + 0x34
		Mockito.when(device.peek(0x0003)).thenReturn((byte)0x54);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// CMP #$03
				0XC5, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testCMPZeroPageX() {
		Mockito.when(device.peek(0x0012)).thenReturn((byte)0x54);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LDA #$55
				0xA9, 0x55,
				// CMP #$03
				0xD5, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testCMPAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x54);
		loadProgram(0x0200, new int[] {
				// LDA #$55
				0xA9, 0x55,
				// CMP $F003
				0XCD, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testCMPAbsoluteX() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x54);
		// 85+52=137
		// 0x55+0x34=0x89 // C=0
		// 85+52=-119 // V=1 N=1
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// CMP $01,X
				0xDD, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testCMPAbsoluteY() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x54);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// CMP $01,Y
				0xD9, 0x01, 0XF0
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	@Test
	void testCMPIndirectX() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x03);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x54);
		loadProgram(0x0200, new int[] {
				// LDX #02
				0xA2, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// CMP ($03,X)
				0xC1, 0x03
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testCMPIndirectY() {
		Mockito.when(device.peek(0x0005)).thenReturn((byte)0x01);
		Mockito.when(device.peek(0x0006)).thenReturn((byte)0xF0);
		Mockito.when(device.peek(0xF003)).thenReturn((byte)0x54);
		loadProgram(0x0200, new int[] {
				// LDY #02
				0xA0, 0x02,
				// LDA #$55
				0xA9, 0x55,
				// CMP ($05),Y
				0xD1, 0x05
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte)0x55, cpu.getA());
		Assertions.assertEquals((byte)0x01, cpu.getP());
		Assertions.assertEquals(5, cycles);
	}
	
	@ParameterizedTest
	@CsvSource({
		"-84,true,88,1,2", //-84=10101100 88=01011000
		"-84,false,88,1,2",
		})
	void testASLInmediate(byte acumulator, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// CLC/SEC
				carry?0x38:0x18, 
				// LDA acumulator
				0xA9, acumulator, 
				// ASL
				0x0A
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testASLZeroPage() {
		Mockito.when(device.peek(0xA0)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// ASL 0xA0
				0x06, 0xA0
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0xAA);
	}
	@Test
	void testASLZeroPageX() {
		Mockito.when(device.peek(0xAA)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// ASL
				0x16, 0x09
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0xAA);
	}
	@Test
	void testASLAbsolute() {
		Mockito.when(device.peek(0xA3A1)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// ASL
				0x0E, 0xA1, 0xA3,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0xAA);
	}
	@Test
	void testASLAbsoluteX() {
		Mockito.when(device.peek(0xA312)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// ASL
				0x1E, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(7, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0xAA);
	}
	
	
	@ParameterizedTest
	@CsvSource({
		"-84,true,86,0,2", //-84=10101100 44=01010110
		"-84,false,86,0,2",
		})
	void testLSRInmediate(byte acumulator, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// CLC/SEC
				carry?0x38:0x18, 
				// LDA acumulator
				0xA9, acumulator, 
				// LSR
				0x4A
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testLSRZeroPage() {
		// 01010101
		Mockito.when(device.peek(0xA0)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LSR 0xA0
				0x46, 0xA0
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		// 00101010
		Mockito.verify(device).poke(0xA0, (byte)0x2A);
	}
	@Test
	void testLSRZeroPageX() {
		Mockito.when(device.peek(0xAA)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// LSR
				0x56, 0x09
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0x2A);
	}
	@Test
	void testLSRAbsolute() {
		Mockito.when(device.peek(0xA3A1)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LSR
				0x4E, 0xA1, 0xA3,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0x2A);
	}
	@Test
	void testLSRAbsoluteX() {
		Mockito.when(device.peek(0xA312)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// LSR
				0x5E, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(7, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0x2A);
	}
	
	@ParameterizedTest
	@CsvSource({
		"-84,true,89,1,2", //-84=10101100 89=01011001
		"-84,false,88,1,2",
		})
	void testROLInmediate(byte acumulator, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// CLC/SEC
				carry?0x38:0x18, 
				// LDA acumulator
				0xA9, acumulator, 
				// ROL
				0x2A
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testROLZeroPage() {
		// 0x55 = 01010101
		Mockito.when(device.peek(0xA0)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// SEC
				0x38,
				// ROL 0xA0
				0x26, 0xA0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA0, (byte)0xAB);
	}
	@Test
	void testROLZeroPageX() {
		Mockito.when(device.peek(0xAA)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// SEC
				0x38,
				// ROL
				0x36, 0x09
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xAA, (byte)0xAB);
	}
	@Test
	void testROLAbsolute() {
		Mockito.when(device.peek(0xA3A1)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// SEC
				0x38,
				// ROL
				0x2E, 0xA1, 0xA3,
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)0xAB);
	}
	@Test
	void testROLAbsoluteX() {
		Mockito.when(device.peek(0xA312)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// SEC
				0x38,
				// ROL
				0x3E, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(7, cycles);
		Mockito.verify(device).poke(0xA312, (byte)0xAB);
	}
	
	@ParameterizedTest
	@CsvSource({
		"-84,true,-42,-128,2", //-84=10101100 -42=11010110
		"-84,false,86,0,2", //-84=10101100 86=01010110   11010110
		})
	void testRORInmediate(byte acumulator, boolean carry, 
			byte expectedResult, byte expectedFlags, int expectedCycles) {
		loadProgram(0x0200, new int[] {
				// CLC/SEC
				carry?0x38:0x18, 
				// LDA acumulator
				0xA9, acumulator, 
				// ROR
				0x6A
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(expectedResult, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(expectedCycles, cycles);
	}
	@Test
	void testRORZeroPage() {
		Mockito.when(device.peek(0xA0)).thenReturn((byte)0x57);
		loadProgram(0x0200, new int[] {
				// SEC
				0x38,
				// ROR 0xA0
				0x66, 0xA0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(5, cycles);
		Mockito.verify(device).poke(0xA0, (byte)-85);
	}
	@Test
	void testRORZeroPageX() {
		Mockito.when(device.peek(0xAA)).thenReturn((byte)0x57);
		loadProgram(0x0200, new int[] {
				// LDX #$A1
				0xA2, 0xA1,
				// SEC
				0x38,
				// ROR
				0x76, 0x09
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xAA, (byte)-85);
	}
	@Test
	void testRORAbsolute() {
		Mockito.when(device.peek(0xA3A1)).thenReturn((byte)0x57);
		loadProgram(0x0200, new int[] {
				// SEC
				0x38,
				// ROR
				0x6E, 0xA1, 0xA3,
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(6, cycles);
		Mockito.verify(device).poke(0xA3A1, (byte)-85);
	}
	@Test
	void testRORAbsoluteX() {
		// 0x55 = 01010111 -> 10101011
		Mockito.when(device.peek(0xA312)).thenReturn((byte)0x57);
		loadProgram(0x0200, new int[] {
				// LDX #0F
				0xA2, 0x0F,
				// SEC
				0x38,
				// ROR
				0x7E, 0x03, 0xA3
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(7, cycles);
		Mockito.verify(device).poke(0xA312, (byte)-85);
	}
	
	@ParameterizedTest
	@CsvSource({
		"0x55,0x34,0x00",
		"0,-64,-62"
		})
	void testBITZeroPage(byte acumulator, byte operand, byte expectedFlags) {
		Mockito.when(device.peek(0x0003)).thenReturn(operand);
		loadProgram(0x0200, new int[] {
				// LDA
				0xA9, acumulator,
				// BIT #$03
				0X24, 0x03
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(acumulator, cpu.getA());
		Assertions.assertEquals(expectedFlags, cpu.getP());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testBITAbsolute() {
		Mockito.when(device.peek(0xF003)).thenReturn((byte)-64);
		loadProgram(0x0200, new int[] {
				// LDA
				0xA9, 0,
				// AND $F003
				0X2C, 0x03, 0XF0
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0, cpu.getA());
		Assertions.assertEquals(-62, cpu.getP());
		Assertions.assertEquals(4, cycles);
	}
	
	@Test
	void testJMPAbsolute() {
		loadProgram(0x0200, new int[] {
				// LDA
				0x4C, 0X02, 0XC0,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0xC002, cpu.getPc());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testJMPZeroPage() {
		Mockito.when(device.peek(0xC0A0)).thenReturn((byte)0x57);
		Mockito.when(device.peek(0xC0A1)).thenReturn((byte)0xB0);
		loadProgram(0x0200, new int[] {
				// JMP 0xA0
				0x6C, 0xA0, 0XC0,
				});
		cpu.reset();
		int cycles = cpu.step();
		Assertions.assertEquals(0xB057, cpu.getPc());
		Assertions.assertEquals(5, cycles);
	}
	
	@Test
	void testBCS() {
		loadProgram(0x0200, new int[] {
				// BCS
				0xB0,10,
				// SEC
				0x38, 
				// BCS
				0xB0,10
				}, true);
		cpu.reset();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x020F, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	@Test
	void testBCC() {
		loadProgram(0x0200, new int[] {
				// SEC
				0x38,
				// BCC
				0x90,10,
				// CLC
				0x18, 
				// BCC
				0x90,10
				}, true);
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0210, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	
	@Test
	void testBEQ() {
		loadProgram(0x0200, new int[] {
				// LDA 1
				0xA9, 1,
				// BEQ
				0xF0,10,
				// LDA 0
				0xA9, 0, 
				// BEQ
				0xF0,10
				}, true);
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0212, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	@Test
	void testBNE() {
		loadProgram(0x0200, new int[] {
				// LDA 0
				0xA9, 0,
				// BNE
				0xD0,10,
				// LDA 1
				0xA9, 1, 
				// BNE
				0xD0,10
				}, true);
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0212, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	
	@Test
	void testBMI() {
		loadProgram(0x0200, new int[] {
				// LDA 0
				0xA9, 0,
				// BMI
				0x30,10,
				// LDA -1
				0xA9, -1, 
				// BMI
				0x30,10
				}, true);
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0212, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	@Test
	void testBPL() {
		loadProgram(0x0200, new int[] {
				// LDA -1
				0xA9, -1,
				// BPL
				0x10,10,
				// LDA 0
				0xA9, 0, 
				// BPL
				0x10,10
				}, true);
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0212, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	
	@Test
	void testBVS() {
		loadProgram(0x0200, new int[] {
				// LDA 100
				0xA9, 100,
				// BVS
				0x70,10,
				// ADC 100
				0x18,0x69, 100, 
				// BVS
				0x70,10
				}, true);
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0213, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	@Test
	void testBVC() {
		loadProgram(0x0200, new int[] {
				// LDA 100
				0xA9, 100,
				// ADC 100
				0x18,0x69, 100, 
				// BVC
				0x50,10,
				// CLV
				0xB8,
				// BVC
				0x50,10
				}, true);
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		cpu.step();
		cpu.step();
		int cycles2 = cpu.step();
		Assertions.assertEquals(0x0214, cpu.getPc());
		Assertions.assertEquals(2, cycles);
		Assertions.assertEquals(3, cycles2);
	}
	@Test
	void testTXS() {
		loadProgram(0x0200, new int[] {
				// LDX 0XFF
				0xA2, 0xFF,
				// TXS
				0x9A
				});
		cpu.reset();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0xFF, cpu.getS());
		Assertions.assertEquals(2, cycles);
	}
	@Test
	void testSTX() {
		loadProgram(0x0200, new int[] {
				// LDX 0XFF
				0xA2, 0xFF,
				// TXS
				0x9A,
				0xA2, 0x00,
				0xBA
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0xFF, cpu.getX());
		Assertions.assertEquals(2, cycles);
	}
	
	@Test
	void testPHA() {
		loadProgram(0x0200, new int[] {
				// LDX 0XFF
				0xA2, 0xFF,
				// TXS
				0x9A,
				// LDA
				0xA9, 0x55,
				// PHA
				0x48
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Mockito.verify(device).poke(0x01FF, (byte)0x55);
		Assertions.assertEquals((byte) 0xFE, cpu.getS());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testPLA() {
		Mockito.when(device.peek(0x01FF)).thenReturn((byte)0x55);
		loadProgram(0x0200, new int[] {
				// LDX 0XFF
				0xA2, 0xFE,
				// TXS
				0x9A,
				// PLA
				0x68
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x55, cpu.getA());
		Assertions.assertEquals((byte) 0xFF, cpu.getS());
		Assertions.assertEquals(4, cycles);
	}
	
	@Test
	void testPHP() {
		loadProgram(0x0200, new int[] {
				// LDX 0XFF
				0xA2, 0xFF,
				// TXS
				0x9A,
				// LDX 0X00
				0xA2, 0x00,
				// SEC
				0x38,
				// PHP
				0x08
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Mockito.verify(device).poke(0x01FF, (byte)0x03);
		Assertions.assertEquals((byte) 0xFE, cpu.getS());
		Assertions.assertEquals(3, cycles);
	}
	@Test
	void testPLP() {
		Mockito.when(device.peek(0x01FF)).thenReturn((byte)0x03);
		loadProgram(0x0200, new int[] {
				// LDX 0XFF
				0xA2, 0xFE,
				// TXS
				0x9A,
				// PLP
				0x28
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals((byte) 0x03, cpu.getP());
		Assertions.assertEquals((byte) 0xFF, cpu.getS());
		Assertions.assertEquals(4, cycles);
	}
	
	@Test
	void testJSR() {
		loadProgram(0x0200, new int[] {
				// LDX 0xFF, TXS
				0xA2, 0xFF,	0x9A,
				// JSR $1103
				0x20, 0x03, 0x11});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x1103, cpu.getPc());
		Mockito.verify(device).poke(0x01FF, (byte)0x02);
		Mockito.verify(device).poke(0x01FE, (byte)0x05);
		Assertions.assertEquals(6, cycles);
	}
	@Test
	void testRTS() {
		Mockito.when(device.peek(0x01FF)).thenReturn((byte)0x02);
		Mockito.when(device.peek(0x01FE)).thenReturn((byte)0x0A);
		loadProgram(0x0200, new int[] {
				// LDX 0XFD, TXS
				0xA2, 0xFD,	0x9A,
				// RTS
				0x60
				});
		cpu.reset();
		cpu.step();
		cpu.step();
		int cycles = cpu.step();
		Assertions.assertEquals(0x020B, cpu.getPc());
		Assertions.assertEquals((byte) 0xFF, cpu.getS());
		Assertions.assertEquals(6, cycles);
	}
	
	private String printByte(byte b) {
		return String.format("0x%02X", b)+ "("+b+")";
	}
	private String printByte(int b) {
		return String.format("0x%02X", b)+ "("+b+")";
	}
}
