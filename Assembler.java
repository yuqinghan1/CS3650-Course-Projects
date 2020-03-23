import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.io.File;
import java.io.PrintWriter;

public class Assembler {


	public static void main(String args[]) throws IOException {
		Scanner sc = new Scanner(new File(args[0]));
		LinkedList<String> ll = new LinkedList<String>();
		// maps all the symbols to their values
		Map<String, String> symbols = new HashMap<String, String>();
		Assembler assembler = new Assembler();
		assembler.readFile(sc, ll);
		int llLength = ll.size();
		assembler.createSymbolTable(ll, symbols, llLength);
		llLength = ll.size();
		assembler.replaceSymbols(ll, symbols, llLength);
		String binaryCode = new String(assembler.assemblerToBinary(ll));
		String output = new String(args[0].replace(".asm", ".hack"));
		System.out.println(symbols.toString());
		try {
			PrintWriter out = new PrintWriter(new File(output));
			out.println(binaryCode);
			out.close();
		}
		catch(Exception e) {
		}
	}

	//read file line by line and add it to a linked list
	//delete any empty line or commented line
	private void readFile(Scanner sc, LinkedList<String> ll) {
		try {
			String line = new String();
			while(sc.hasNextLine()) {
				line = new String(sc.nextLine().replaceAll("\\s",""));
				if(!line.isEmpty() && !line.startsWith("//")) {
					ll.add(line);
				}
			}
			sc.close();
		}
		catch(Exception e) {
		}
	}

	private void createSymbolTable(LinkedList<String> ll, Map<String, String> symbols, int llLength) {
		// stores the symbol to be placed in the map
		String key = new String();
		// location for the created symbol variables
		int variableLocation = 16;
		// iterates through the linked list to find the symbols
		symbols.put("SP", "0");
		symbols.put("LCL", "1");
		symbols.put("ARG", "2");
		symbols.put("THIS", "3");
		symbols.put("THAT", "4");
		for(int i = 0; i < 16; i++) {
			symbols.put("R" + Integer.toString(i), Integer.toString(i));
		}
		symbols.put("SCREEN", "16384");
		symbols.put("KBD", "24576");
		for(int i = 0; i < llLength; i++) {
			String line = new String(ll.get(i));
			//looks for loops
			if(line.charAt(0) == '(') {
				key = new String(line.substring(1, line.length() - 1));
				symbols.put(key, Integer.toString(i));
				ll.remove(i);
				i--;
				llLength--;
			}
		}
		System.out.println(ll.size() + " llLenght: " + llLength);
		//looks for loops
		for(int i = 0; i < llLength; i++) {
			String line = new String(ll.get(i));
			if(line.charAt(0) == '@' && !Character.isDigit(line.charAt(1))) {
				key = new String(line.substring(1));
				if(!symbols.containsKey(key)) {
					symbols.put(key, Integer.toString(variableLocation));
					variableLocation++;
				}
			}
		}
	}

	private void replaceSymbols(LinkedList<String> ll, Map<String, String> symbols, int llLength) {
		System.out.println(ll.size() + " llLenght: " + llLength);
		StringBuilder replace = new StringBuilder("@");
		for(int i = 0; i < llLength; i++) {
			String line = new String(ll.get(i));
			//replaces all symbols
			if(line.charAt(0) == '@' && !Character.isDigit(line.charAt(1))) {
				replace = new StringBuilder("@");
				String find = new String(line.substring(1));
				replace.append(symbols.get(find));
				line = new String(replace.toString());
				ll.set(i, line);
			}
		}
	}

	private String assemblerToBinary(LinkedList<String> ll) {
		StringBuffer sb = new StringBuffer();
		while(!ll.isEmpty()) {
			String line = new String(ll.pop());
			if(!line.isEmpty() && line.charAt(0) == '@') {
				sb.append(Assembler.aInstructionToBinary(line));
				sb.append("\n");
			}
			else if (!line.isEmpty()) {
				sb.append(Assembler.cInstructionToBinary(line));
				sb.append("\n");
			}
		}
		return sb.toString().trim();	
	}

	private static String aInstructionToBinary(String line) {
		line = line.substring(1);
		int temp = Integer.parseInt(line);
		StringBuilder aReg = new StringBuilder(Integer.toBinaryString(temp));
		while(aReg.length() < 16) {
			aReg.insert(0, '0');
		}
		aReg.setCharAt(0, '0');
		return aReg.toString().replaceAll("\\s","");
	}

	private static String cInstructionToBinary(String line) {
		StringBuilder c = new StringBuilder();
		c.append("111");
		StringBuilder dest = Assembler.getDest(line);
		StringBuilder comp = Assembler.getComp(line);
		StringBuilder jump = Assembler.getJump(line);
		c.append(comp);
		c.append(dest); 
		c.append(jump);
		//System.out.println(c.toString());
		return c.toString();
	}

	private static StringBuilder getJump(String line) {
		int j = line.indexOf(";");
		String subLine = new String();
		StringBuilder str = new StringBuilder();
		if(j != -1) {
			subLine = new String(line.substring(j));
			if(subLine.startsWith(";null")) {
				str.append("000");
			}
			else if(subLine.startsWith(";JGT")) {
				str.append("001");
			}
			else if(subLine.startsWith(";JEQ")) {
				str.append("010");
			}
			else if(subLine.startsWith(";JGE")) {
				str.append("011");
			}
			else if(subLine.startsWith(";JLT")) {
				str.append("100");
			}
			else if(subLine.startsWith(";JNE")) {
				str.append("101");
			}
			else if(subLine.startsWith(";JLE")) {
				str.append("110");
			}
			else if(subLine.startsWith(";JMP")) {
				str.append("111");
			}
		}
		else {
			str.append("000");
		}
		return str;
	}

	private static StringBuilder getDest(String line) {
		int i = 0;
		char[] dest = new char[3];
		dest[0]='0';
		dest[1]='0';
		dest[2]='0';
		if(line.charAt(i+1) != ';') {
			while(i < 3) {
				if(line.charAt(i) == 'A') {
					dest[2] = '1';
				}
				else if(line.charAt(i) == 'D') {
					dest[1] = '1';
				}
				else if(line.charAt(i) == 'M') {
					dest[0] = '1';
				}
				else {
					break;
				}
				i++;
			}
		}
		StringBuilder sdest = new StringBuilder();
		sdest.append(dest[2]);
		sdest.append(dest[1]);
		sdest.append(dest[0]);
		//System.out.println(sdest.toString());
		return sdest;
	} 

	private static StringBuilder getComp(String line) {
		StringBuilder str = new StringBuilder();
		int i = line.indexOf("=") + 1;
		if(line.indexOf("=") == -1) {
			i=0;
		}
		String subLine = new String(line.substring(i));
		if(subLine.startsWith("0"))
			str.append("0101010");
		else if(subLine.startsWith("1"))
			str.append("0111111");
		else if(subLine.startsWith("-1"))
			str.append("0111010");
		else if(subLine.startsWith("D+1"))
			str.append("0011111");
		else if(subLine.startsWith("A+1"))
			str.append("0110111");
		else if(subLine.startsWith("M+1"))
			str.append("1110111");
		else if(subLine.startsWith("D-1"))
			str.append("0001110");
		else if(subLine.startsWith("A-1"))
			str.append("0110010");
		else if(subLine.startsWith("M-1"))
			str.append("1110010");
		else if(subLine.startsWith("D+A") || subLine.startsWith("A+D"))
			str.append("0000010");
		else if(subLine.startsWith("D+M") || subLine.startsWith("M+D"))
			str.append("1000010");
		else if(subLine.startsWith("D-A"))
			str.append("0010011");
		else if(subLine.startsWith("D-M"))
			str.append("1010011");
		else if(subLine.startsWith("A-D"))
			str.append("0000111");
		else if(subLine.startsWith("M-D"))
			str.append("1000111");
		else if(subLine.startsWith("D&A"))
			str.append("0000000");
		else if(subLine.startsWith("D&M"))
			str.append("1000000");
		else if(subLine.startsWith("D|A"))
			str.append("0010101");
		else if(subLine.startsWith("D|M"))
			str.append("1010101");
		else if(subLine.startsWith("D"))
			str.append("0001100");
		else if(subLine.startsWith("A"))
			str.append("0110000");
		else if(subLine.startsWith("M"))
			str.append("1110000");
		else if(subLine.startsWith("!D"))
			str.append("0001101");
		else if(subLine.startsWith("!A"))
			str.append("0110001");
		else if(subLine.startsWith("!M"))
			str.append("1110001");
		else if(subLine.startsWith("-D"))
			str.append("0001111");
		else if(subLine.startsWith("-A"))
			str.append("0110011");
		else if(subLine.startsWith("-M"))
			str.append("1110011");
		//System.out.println(str);
		return str;

	}

}