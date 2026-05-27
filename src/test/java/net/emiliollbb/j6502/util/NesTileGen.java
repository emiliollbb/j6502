package net.emiliollbb.j6502.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NesTileGen {
	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		StringBuffer sb=new StringBuffer(100);
		StringBuffer line1=new StringBuffer(100);
		StringBuffer line2=new StringBuffer(100);
		System.out.println("Introduce colores");
		for(int i=0; i<8; i++) {
			sb.append(reader.readLine());
		}
		if(sb.length()!=64) {
			throw new Exception("Wrong size");
		}
		for(char c: sb.toString().toCharArray()) {
			byte b = (byte) Integer.parseInt(""+c);
			switch(b) {
				case 0:
					line1.append('0');
					line2.append('0');
					break;
				case 1:
					line1.append('1');
					line2.append('0');
					break;
				case 2:
					line1.append('0');
					line2.append('1');
					break;
				case 3:
					line1.append('1');
					line2.append('1');
					break;
			}
		}
		
		String full = line1.toString()+line2.toString();
		System.out.print(".byt ");
		for(int i=0; i<full.length(); i+=8) {
			System.out.print(String.format("$%02X,", Integer.parseInt(full.substring(i,i+8),2)));
		}
		System.out.println();
	}
}
