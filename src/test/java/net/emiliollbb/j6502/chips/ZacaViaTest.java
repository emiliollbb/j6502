package net.emiliollbb.j6502.chips;

import static org.mockito.Mockito.mockitoSession;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.emiliollbb.j6502.computers.hid.LCD16x2;

@ExtendWith(MockitoExtension.class)
public class ZacaViaTest {
	private static final int DATAB=0xBFF0;
	private static final int DATAA=0xBFF1;
	private static final int DDRA=0xBFF3;
	private static final int DDRB=0xBFF2;
	private static final int ACR=0xBFFB;
	private static final int PCR=0xBFFC;
	private static final int T1LL=0xBFF6;
	private static final int T1LH=0xBFF7;
	private static final int IER=0xBFFE;
	
	@Mock
	private LCD16x2 lcd;
	@InjectMocks
	private ZacaVia via;
	
	@ParameterizedTest
	@CsvSource({
		DDRA+",0x00",
		DDRA+",0xFF",
		DDRB+",0x00",
		DDRB+",0xFF",
		ACR+",0x00",
		ACR+",0xFF",
		PCR+",0x00",
		PCR+",0xFF",
		T1LL+",0x00",
		T1LL+",0xFF",
		T1LH+",0x00",
		T1LH+",0xFF",
		})
	void testPlainIO(int addr, int value) {
		via.poke(addr, (byte)value);
		Assertions.assertEquals((byte)value, via.peek(addr));
	}
	
	/**
	 Un ejemplo: empezando con la comprobación típica (escribe $7F, lee $80) se puede hacer la siguiente secuencia:
		escribe $81, lee $81
		escribe $82, lee $83
		escribe $84, lee $87
		escribe $88, lee $8F
		escribe $90, lee $9F
		escribe $A0, lee $BF
		escribe $C0, lee $FF
		y puedes completar con escribe $7F, lee $80
	 */
	@Test
	void testIER() {
		via.poke(IER, (byte)0x7F);
		Assertions.assertEquals((byte)0x80, via.peek(IER));
		via.poke(IER, (byte)0x81);
		Assertions.assertEquals((byte)0x81, via.peek(IER));
		via.poke(IER, (byte)0x82);
		Assertions.assertEquals((byte)0x83, via.peek(IER));
		via.poke(IER, (byte)0x84);
		Assertions.assertEquals((byte)0x87, via.peek(IER));
		via.poke(IER, (byte)0x88);
		Assertions.assertEquals((byte)0x8F, via.peek(IER));
		via.poke(IER, (byte)0x90);
		Assertions.assertEquals((byte)0x9F, via.peek(IER));
		via.poke(IER, (byte)0xA0);
		Assertions.assertEquals((byte)0xBF, via.peek(IER));
		via.poke(IER, (byte)0xC0);
		Assertions.assertEquals((byte)0xFF, via.peek(IER));
	}
	
	@ParameterizedTest
	@CsvSource({
		"0x10,0x10,false",
		"0x11,0x10,true",
		"0xF1,0xF0,true",
	})
	void testLCD(int pa, int lcdData, boolean rs) {
		via.poke(DDRA, (byte)0xFF);
		via.poke(DATAA, (byte)pa);
		Mockito.verify(lcd).setData(Mockito.eq((byte)lcdData));
	}
}
