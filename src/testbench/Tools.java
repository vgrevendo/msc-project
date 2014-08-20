package testbench;

import java.io.File;

/**
 * Some misc testing and utility tools
 * @author vincent
 *
 */
public class Tools {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	public static void println(String s, String color) {
		System.out.println(color + s + ANSI_RESET);
	}
	
	public static String chooseFileName(String pathRoot, String extension) {
		int id = 0;
		String fullExtension = "." + extension;
		
		while((new File(pathRoot + id + fullExtension)).exists()) {
			id ++;
		}
		
		return pathRoot + id + fullExtension;
	}
}
