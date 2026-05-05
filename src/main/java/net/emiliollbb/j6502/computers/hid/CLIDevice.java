package net.emiliollbb.j6502.computers.hid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import net.emiliollbb.j6502.chips.AbstractBusDevice;

public class CLIDevice  extends AbstractBusDevice {
	protected static final int CONTROL_ADDR=0;
	protected static final int DATA_ADDR=1;
	protected BufferedReader in;
	protected PrintStream out;
	protected char[] buffer;
	protected int index;
	public CLIDevice(int startAddr, PrintStream out, InputStream in) {
		super("CLI Device", startAddr, 2);
		this.in=new BufferedReader(new InputStreamReader(in));
		this.out=out;
		buffer=new char[0];
		index=0;
	}

	@Override
	protected byte ioRead(int addr) {
		if(addr==CONTROL_ADDR) {
			return index==0?(byte)1:0;
		}
		else if(addr==DATA_ADDR) {
			if(index<buffer.length) {
				return (byte)buffer[index++];
			} else {
				return 0;
			}
		}
		throw new RuntimeException("wrong address");
	}

	@Override
	protected void ioWrite(int addr, byte data) {
		if(addr==CONTROL_ADDR) {
			try {
				buffer = in.readLine().toCharArray();
				index = 0;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if(addr==DATA_ADDR) {
			out.print((char)data);
		} 
	}

}
