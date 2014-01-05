package tk.circuitcoder.grafixplane;

import java.io.IOException;
import java.util.Arrays;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * As it says, a Launcher</br>
 * Takes the console arguments, and starts the GrafixPlane instance
 * @author CircuitCoder
 * @since 0.0.1
 */
public class Launcher {

	public static void main(String[] args) {		
		OptionParser parser=new OptionParser();
		parser.acceptsAll(Arrays.asList("h","host"),"Host for this server")
			.withRequiredArg()
			.ofType(String.class);
		parser.acceptsAll(Arrays.asList("p","port"), "Port to use for the embedded web server")
			.withRequiredArg()
			.ofType(Integer.class)
			.defaultsTo(80);
		parser.acceptsAll(Arrays.asList("d","debug"), "Enable debug mode");
		parser.acceptsAll(Arrays.asList("?","help"), "Show the help text");
		parser.acceptsAll(Arrays.asList("v","version"), "Show the version");
		
		OptionSet options=null;
		try {
			options=parser.parse(args);
		} catch (OptionException ex) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(-1);
		}
		
		if(options.has("?")) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(options.has("v")) {
			System.out.println(GrafixPlane.VERSION_TEXT);
		}
		else {
			GrafixPlane.start(options);
		}
	}
}