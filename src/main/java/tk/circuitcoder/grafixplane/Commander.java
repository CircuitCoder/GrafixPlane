package tk.circuitcoder.grafixplane;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import tk.circuitcoder.grafixplane.user.User;
import tk.circuitcoder.grafixplane.user.User.AccessLevel;
import static tk.circuitcoder.grafixplane.GrafixPlane.*;
import jline.console.ConsoleReader;

/**
 * Reads from console input and execute specified commands or call other components to do the job
 * @author CircuitCoder
 */
public class Commander {
	boolean jline;
	ConsoleReader reader;
	BufferedReader is;
	BufferedWriter os;
	
	public Commander(boolean useJline) throws IOException {
		this(useJline, System.in ,System.out);
	}
	
	public Commander(boolean useJline,InputStream input,OutputStream output) throws IOException {
		jline=useJline;
		is=new BufferedReader(new InputStreamReader(input));
		os=new BufferedWriter(new OutputStreamWriter(output));;
		if(jline) reader=new ConsoleReader("GrafixPlane", input, output,null,null);
	}
	
	public void startLoop() {
		String input;
		while((input=read())!=null) {
			getGP().getLogger().debug("Console issuing: "+input);
			String[] args=input.split(" ");
			//TODO: Other commands
			try {
				if(args[0].toLowerCase().equals("?")) getGP().getLogger().info("No help right now!");
				else if(args[0].toLowerCase().equals("useradd")) {
					if(User.newUser(args[1], args[2], AccessLevel.ADMIN)!=null)
						getGP().getLogger().info("Added!");
					else getGP().getLogger().info("Failed!");
				}
				else if(args[0].toLowerCase().equals("stop")) {
					getGP().shutdown();
					System.exit(0);
				}
				else if(args[0].toLowerCase().equals("userauth"))
					getGP().getLogger().info(String.valueOf(User.verify(args[1], args[2])));
				else
					getGP().getLogger().info("Command Not Found. Type '?' to get help");
			} catch(ArrayIndexOutOfBoundsException e) {
				getGP().getLogger().error("Not Enough Arguments");
			} catch(Exception e) {
				getGP().getLogger().error("Unknow error. Type '?' to get help");
			}
		}
		//Ctrl-D!
		getGP().getLogger().info("EOF detected");
		getGP().shutdown();
		System.exit(0);
	}
	
	private String read() {
		try {
			if(jline) return reader.readLine("GrafixPlane> ");
			else return is.readLine(); 
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
