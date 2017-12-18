import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import org.jpos.iso.ISOUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class send_HS_message {
	public static void main(String[] args){



		String MSG=null; 
		DataInputStream datain = null;
		DataOutputStream dataout = null;
		String serverName=null;
		int port;

		//Sample Message
		//MSG = "00101010000082017120410425300000000000000000000^2^santosh maharjan^tel:+94776501975^USD^10.0^198501^DEA^^^^";

		try{

			Scanner sc=new Scanner(System.in);
			System.out.println("Enter Message you want to proceed:");
			MSG=sc.next();

			serverName="192.168.1.64";

			port= 4003;
			
			System.out.println("Connecting to " + serverName + " on port " + port);
			Socket client = new Socket(serverName, port);

			byte request[] = MSG.getBytes();
			System.out.println("Request Byte : " + new String(request));


			int buffer_length = request.length;
			byte[] MH = ISOUtil.hex2byte(ISOUtil.zeropad(Integer.toHexString(buffer_length), 8));
			byte[] req=ISOUtil.concat(MH, request);

			dataout = new DataOutputStream(client.getOutputStream());
			datain = new DataInputStream(client.getInputStream());
			dataout.write(req);
			dataout.flush();


			//reading the respond from e-switch
			byte MESSAGEBUFFER[] = null;
			int relen = 0;
			byte HD_IN[]	= new byte[4];
			relen = 0;
			datain.readFully(HD_IN, 0, 4);
			relen =  Integer.parseInt(ISOUtil.hexString(HD_IN),16);
			byte totalReadBytes[]	= new byte[relen];
			datain.readFully(totalReadBytes,0,relen);

			MESSAGEBUFFER = ISOUtil.concat(HD_IN, totalReadBytes);
			String output = new String(totalReadBytes);

			String[] data = output.split("\\^");


			System.out.println("Response From e-switch : " +output );
			printJsonObject(data[2]);


			client.close();


		} catch(Exception e) {
			e.printStackTrace();
		}
	}



	public static void printJsonObject(String x) throws Exception {

		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(x);

		for (Object key : jsonObj.keySet()) {
			//based on you key types
			String keyStr = (String)key;
			Object keyvalue = jsonObj.get(keyStr);

			//Print key and value
			System.out.println(keyStr + " : " + keyvalue);

			//for nested objects iteration if required
			if (keyvalue instanceof JSONArray) {

				
				JSONArray interventionJsonArray = (JSONArray)keyvalue;
				
				System.out.println("\n================= "+ keyStr +" =======================");
				
				for(int i = 0; i < interventionJsonArray.size();i++) {
					System.out.println("----------------------------------------\n" );
					printJsonObject(interventionJsonArray.get(i).toString());
					
				}

			}
			else if (keyvalue instanceof JSONObject) {
				printJsonObject(keyvalue.toString());
				
			}
			else {
				// It's something else, like a string or number
			}

		}
		
		System.out.println("\n");
		

	}




}
