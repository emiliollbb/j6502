package net.emiliollbb.j6502.computers.hid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.emiliollbb.j6502.chips.AbstractBusDevice;

public class CLIDevice  extends AbstractBusDevice {
	protected int controlAddr;
	protected int dataAddr;
	protected BufferedReader in;
	protected PrintWriter out;
	protected char[] buffer;
	protected int index;
	public CLIDevice(int startAddr, OutputStream out, InputStream in) {
		super("CLI Device", startAddr, 2);
		controlAddr=startAddr;
		dataAddr=startAddr+1;
		this.in=new BufferedReader(new InputStreamReader(in));
		this.out=new PrintWriter(out);
		buffer=new char[0];
		index=0;
	}

	@Override
	protected byte ioRead(int addr) {
		if(addr==controlAddr) {
			return index==0?(byte)1:0;
		}
		else if(addr==dataAddr) {
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
		if(addr==controlAddr) {
			try {
				buffer = in.readLine().toCharArray();
				index = 0;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if(addr==dataAddr) {
			out.print((char)data);
		} 
	}

}
