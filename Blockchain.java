/* 

Author: Andrew Richards



The web sources:

https://mkyong.com/java/how-to-parse-json-with-gson/
http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object
https://www.java67.com/2014/10/how-to-pad-numbers-with-leading-zeroes-in-Java-example.html

One version of the JSON jar file here:
https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.2/

Need to download gson-2.8.2.jar into your classpath / compiling directory.

To compile and run:

javac -cp "gson-2.8.2.jar" Blockchain.java
java -cp ".;gson-2.8.2.jar" Blockchain

-----------------------------------------------------------------------------------------------------*/
  

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.lang.reflect.Type;
import java.security.*;

import java.time.Instant;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

//don't touch
class Ports {
	public static int KeyServerPortBase = 4710;
	public static int UnverifiedBlockServerPortBase = 4820;
	public static int BlockchainServerPortBase = 4930;

	public static int KeyServerPort;
	public static int UnverifiedBlockServerPort;
	public static int BlockchainServerPort;

	public void setPorts(){
		KeyServerPort = KeyServerPortBase + Blockchain.PID;
		UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + Blockchain.PID;
		BlockchainServerPort = BlockchainServerPortBase + Blockchain.PID;
	}
}
//don't touch
class PublicKeyWorker extends Thread {
	Socket sock;
	PublicKeyWorker (Socket s) {
		sock = s;
	}

	public void run() {
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			String data = in.readLine();
			System.out.println("Public Key Server Got Key: " + data + "\n");
			sock.close();
		} 	
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class PublicKeyServer implements Runnable {
  //public ProcessBlock[] PBlock = new ProcessBlock[3]; // One block to store info for each process.

	public void run() {
		
		Socket sock;
		System.out.println("Starting Key Server input thread using: " + Ports.KeyServerPort);
		try {
			ServerSocket servsock = new ServerSocket(Ports.KeyServerPort, Blockchain.q_len);
			while (true){
				sock = servsock.accept();
				new PublicKeyWorker (sock).start();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class BlockRecord implements Comparable {
  
  //check this
  private String BlockData;//??
  
  //-----------------------------------
  private String Timestamp;
  private String BlockID;
  private String VerificationProcessID;
  private String PreviousHash;
  private UUID uuid;
  private String SignedSHA256;
  private String SHA256String;
  private String CreatingProcess;
  private String Fname;
  private String Lname;
  private String SSNum;
  private String DOB;
  private String Diag;
  private String Treat;
  private String Rx;
  private String WinningHash;//make getter/ setter

  //-----accessor methods-----
  public String getPreviousHash() {return this.PreviousHash;}
  public void setPreviousHash(String previousHash){this.PreviousHash = previousHash;}
  
  public UUID getUUID() {return uuid;}
  public void setUUID (UUID ud){this.uuid = ud;}

  public String getBlockData() {return this.BlockData;}
  public void setBlockData(String blockData){this.BlockData = blockData;}

  public String getTimestamp() {return Timestamp;}
  public void setTimestamp(String TS){this.Timestamp = TS;}

  public String getSHA256String() {return SHA256String;}
  public void setSHA256String(String SH){this.SHA256String = SH;}

  public String getSignedSHA256() {return SignedSHA256;}
  public void setSignedSHA256(String SH){this.SignedSHA256 = SH;}

  public String getCreatingProcess() {return CreatingProcess;}
  public void setCreatingProcess(String CP){this.CreatingProcess = CP;}

  public String getVerificationProcessID() {return VerificationProcessID;}
  public void setVerificationProcessID(String VID){this.VerificationProcessID = VID;}

  public String getBlockID() {return BlockID;}
  public void setBlockID(String BID){this.BlockID = BID;}

  public String getSSNum() {return SSNum;}
  public void setSSNum(String SS){this.SSNum = SS;}

  public String getFname() {return this.Fname;}
  public void setFname(String FN){this.Fname = FN;}

  public String getLname() {return Lname;}
  public void setLname(String LN){this.Lname = LN;}

  public String getFDOB() {return DOB;}
  public void setFDOB(String DOB){this.DOB = DOB;}

  public String getGDiag() {return Diag;}
  public void setGDiag(String D){this.Diag = D;}

  public String getGTreat() {return Treat;}
  public void setGTreat(String D){this.Treat = D;}

  public String getGRx() {return Rx;}
  public void setGRx(String D){this.Rx = D;}
  
  public String getWinningHash() {return WinningHash;}
  public void setWinningHash (String WH){this.WinningHash = WH;}

  

  @Override
  public int compareTo(Object o) {

    BlockRecord other = (BlockRecord) o;

    Instant otherInstant = Instant.parse(other.getTimestamp());
    Instant thisInstant = Instant.parse(this.getTimestamp());

    return thisInstant.compareTo(otherInstant);
  }
}

//change collection
class BlockInput {

	private static String FILENAME;

	/* Token indexes for input: */
	private static final int iFNAME = 0;
	private static final int iLNAME = 1;
	private static final int iDOB = 2;
	private static final int iSSNUM = 3;
	private static final int iDIAG = 4;
	private static final int iTREAT = 5;
	private static final int iRX = 6;

	//replace JSON
	
	public static LinkedList<String> GetJsonListString(int pid) throws Exception {

		String result;

		// CDE: Process numbers and port numbers to be used: 
		int pnum = pid;
		int UnverifiedBlockPort;
		int BlockChainPort;
		
		UnverifiedBlockPort = 4820 + pnum;
		BlockChainPort = 4930 + pnum;

		System.out.println("Process number: " + pnum + " Unverified Server Port: " + UnverifiedBlockPort + " Blockchain Server Port: " + BlockChainPort + "\n");

		switch(pnum) {
			case 1: FILENAME = "BlockInput1.txt"; break;
			case 2: FILENAME = "BlockInput2.txt"; break;
			default: FILENAME= "BlockInput0.txt"; break;
		}

		System.out.println("Using input file: " + FILENAME);

		try{
			BufferedReader br = new BufferedReader(new FileReader(FILENAME));
			String[] tokens = new String[10];
			String InputLineStr;
			String suuid;
			UUID idA;

			//------------create blockchain list from data input file---------
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			LinkedList<String> BlockList = new LinkedList<String>();
			
			while ((InputLineStr = br.readLine()) != null) {
				
				BlockRecord BR = new BlockRecord();
				String TS = Instant.now().toString();
				BR.setTimestamp(TS);
				BR.setSHA256String("");
				BR.setSignedSHA256("");
				
				//idA = UUID.randomUUID();
				suuid = UUID.randomUUID().toString();
				BR.setBlockID(suuid);
				BR.setCreatingProcess("Process" + pnum);
				BR.setVerificationProcessID("To be set later...");
				tokens = InputLineStr.split(" +"); // Tokenize the input
				BR.setSSNum(tokens[iSSNUM]);
				BR.setFname(tokens[iFNAME]);
				BR.setLname(tokens[iLNAME]);
				BR.setFDOB(tokens[iDOB]);
				BR.setGDiag(tokens[iDIAG]);
				BR.setGTreat(tokens[iTREAT]);
				BR.setGRx(tokens[iRX]);
				
				String jsonRecord = gson.toJson(BR);
				BlockList.add(jsonRecord);
				//BlockList.add(BR);
			}
			System.out.println(BlockList.size() + " records read.\n");
			//System.out.println("Names read into a linked list from the data input file:\n");
			
			//-------print blockchain list names--------
			//Iterator<BlockRecord> iterator = BlockList.iterator();
			//BlockRecord current;
			//while(iterator.hasNext()){
				//current = iterator.next();
				//System.out.println(current.getFname() + " " + current.getLname());
			//}
			//System.out.println("\n\n");

			//-------create JSON string from list-----------
			//Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
			// Convert the Java object to a JSON String:
			//String json = gson.toJson(BlockList);
			//System.out.println("\nJSON marshaled list is:\n" + json);

			return BlockList;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}

//adjust
class WorkB {

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	static String someText = "one two three";
	static String randString;

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	//adjust - refactor
	private static String signSHA256(String input) throws Exception {

		MessageDigest MD = MessageDigest.getInstance("SHA-256");
		byte[] byteData = MD.digest(input.getBytes("UTF-8"));

		// Turn into a string of hex values
		
		String SHA256String = "";
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		
		SHA256String = sb.toString();
		
		return SHA256String;
	}

	//adjust - refactor 
	public static BlockRecord verifyBlock(BlockRecord inputBlock, int PID, String previousHash) throws Exception {
		String concatString;  // Random seed string concatenated with the existing data
		String StringOut; // Will contain the new SHA256 string converted to HEX and printable.
		int workNumber;

		inputBlock.setPreviousHash(previousHash);

		try {
			for(int i=1; i<20; i++) { // Limit how long we try for this example.
				randString = randomAlphaNumeric(8);

				inputBlock.setBlockData(randString);
				concatString = inputBlock.toString().replace("\n", "").replace(" ", "");

				// Get the hash value
				//MessageDigest MD = MessageDigest.getInstance("SHA-256");
				//byte[] bytesHash = MD.digest(concatString.getBytes("UTF-8"));

				// Turn into a string of hex values
				//StringOut = DatatypeConverter.printHexBinary(bytesHash);
				StringOut = signSHA256(concatString);
				
				
				System.out.println("Hash is: " + StringOut);

				// Between 0000 (0) and FFFF (65535)
				workNumber = Integer.parseInt(StringOut.substring(0,4),16);

				System.out.println("First 16 bits in Hex and Decimal: " + StringOut.substring(0,4) +" and " + workNumber);

				if (!(workNumber < 20000)) {  // lower number = more work.
					System.out.format("%d is not less than 20,000 so we did not solve the puzzle\n\n", workNumber);
				}
				if (workNumber < 20000) {
					System.out.format("%d IS less than 20,000 so puzzle solved!\n", workNumber);
					System.out.println("The seed (puzzle answer) was: " + randString);
					inputBlock.setSHA256String(StringOut);
					
					String hashInput = inputBlock.getSHA256String() + String.valueOf(PID);
					
					inputBlock.setSignedSHA256(signSHA256(hashInput));
					return inputBlock;
				}
				// Here is where you would periodically check to see if the blockchain has been updated
				// ...if so, then abandon this verification effort and start over.
				// Here is where you will sleep if you want to extend the time up to a second or two.
			}
		} 
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}


class UnverifiedBlockServer implements Runnable {
	BlockingQueue<BlockRecord> queue;
	
	UnverifiedBlockServer(BlockingQueue<BlockRecord> queue){
		this.queue = queue;
	}

  /* Inner class to share priority queue. We are going to place the unverified blocks into this queue in the order we get
     them, but they will be retrieved by a consumer process sorted by blockID. */

	class UnverifiedBlockWorker extends Thread {
		Socket sock;
		UnverifiedBlockWorker(Socket s){
			sock = s;
		}

		public void run(){
			try{
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String data = in.readLine();
				data = data.substring(6);
				data = data.replace("--linebreak--", "\n");
				
				BlockRecord blockRecord = this.UnMarshallJSON(data);

				System.out.println("Unverified Block Worker Put Block in priority queue: " + data + "\n");
				queue.put(blockRecord);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private BlockRecord UnMarshallJSON(String data){
			Gson gson = new Gson();
			BlockRecord blockRecordIn = null;
			
			System.out.println("Unverified Block Server Incoming Block\n");
			// Read and convert JSON File to a Java Object:
			blockRecordIn = gson.fromJson(data, BlockRecord.class);
	  
			// Print the blockRecord:
			System.out.println(blockRecordIn);
			System.out.println("Name is: " + blockRecordIn.getFname() + " " + blockRecordIn.getLname());
			System.out.println("String UUID: " + blockRecordIn.getBlockID() + " Stored-binaryUUID: " + blockRecordIn.getUUID());
			
			return blockRecordIn;
		}
	}
	
	public void run(){
		
		Socket sock;
		System.out.println("Starting the Unverified Block Server input thread using " + Ports.UnverifiedBlockServerPort);
		try {
			ServerSocket servsock = new ServerSocket(Ports.UnverifiedBlockServerPort, Blockchain.q_len);
			while (true) {
				sock = servsock.accept(); // Got a new unverified block
				new UnverifiedBlockWorker(sock).start(); // So start a thread to process it.
			}
		} 
		catch (IOException e) {
			System.out.println(e);
		}
	}
}

//Adjust
class UnverifiedBlockConsumer implements Runnable {
	private BlockingQueue<BlockRecord> queue;
	private int PID;
	UnverifiedBlockConsumer(BlockingQueue<BlockRecord> queue, int PID){
		this.queue = queue;
		this.PID = PID;
	}
	
	private BlockRecord UnMarshallJSON(String data){
		Gson gson = new Gson();
		BlockRecord blockRecordIn = null;

		
		System.out.println("Unverified Block Server Incoming Block\n");
		// Read and convert JSON File to a Java Object:
		blockRecordIn = gson.fromJson(data, BlockRecord.class);
			
	  
		// Print the blockRecord:
		System.out.println(blockRecordIn);
		System.out.println("Name is: " + blockRecordIn.getFname() + " " + blockRecordIn.getLname());

		System.out.println("String UUID: " + blockRecordIn.getBlockID() + " Stored-binaryUUID: " + blockRecordIn.getUUID());
		
		return blockRecordIn;
	}
	
	/*private LinkedList<BlockRecord> ConvertJsonToList(String blockchain) {
		LinkedList<BlockRecord> BlockList = new LinkedList<BlockRecord>();
		if(blockchain != null){
			Gson gson = new Gson();
			Type ConversionType = new TypeToken<LinkedList<BlockRecord>>(){}.getType();
			BlockList = gson.fromJson(blockchain, ConversionType);
		}
		return BlockList;
	}	*/

	public void run(){
		BlockRecord data;
		PrintStream toServer;
		Socket sock;
		String pPrevHash;
		BlockRecord VerifiedBlock;
		String NewBlockchain;

		System.out.println("Starting the Unverified Block Priority Queue Consumer thread.\n");
		try{
			while(true){ // Consume from the incoming queue. Do the work to verify. Mulitcast new blockchain
				data = queue.take(); // Will blocked-wait on empty queue
				System.out.println("Consumer got unverified: " + data + "\n");
				//System.out.println("TEST " + Blockchain.blockchain + "\n");
				
				//LinkedList<BlockRecord> BlockList = ConvertJsonToList(Blockchain.blockchain);
				LinkedList<BlockRecord> BlockList = new LinkedList<BlockRecord>();
				Gson gson = new Gson();
				
				if(Blockchain.blockchain.length() > 0){
					Type ConversionType = new TypeToken<LinkedList<BlockRecord>>(){}.getType();
					BlockList = gson.fromJson(Blockchain.blockchain, ConversionType);
					
					pPrevHash = BlockList.get(0).getSHA256String();
				}
				else{
					pPrevHash = "Head Block";	
				}
				
				//Do work
				VerifiedBlock = WorkB.verifyBlock(data, this.PID, pPrevHash);
				VerifiedBlock.setVerificationProcessID(Integer.toString(this.PID));
				//Puzzle solzed

				//Add new block to front of blockchain
				gson = new GsonBuilder().setPrettyPrinting().create();
				NewBlockchain = gson.toJson(VerifiedBlock) + Blockchain.blockchain;
				//NewBlockchain = VerifiedBlock.toString().replace(" ", "") + Blockchain.blockchain;
				
				for(int i = 0; i < Blockchain.numProcesses; i++){ // send to each process in group, including us:
					sock = new Socket(Blockchain.serverName, Ports.BlockchainServerPortBase + i);
					toServer = new PrintStream(sock.getOutputStream());

					//Multicast new Blockchain
					toServer.println(NewBlockchain); 
					toServer.flush();
					sock.close();
				}
				Thread.sleep(1500); //Wait for our blockchain to be updated before processing a new block
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

//Adjust
// Incoming proposed replacement blockchains. Compare to existing. Replace if winner:
class BlockchainWorker extends Thread {
	private Socket sock;
	private int PID;
	
	BlockchainWorker (Socket s, int PID) {
		this.sock = s;
		this.PID = PID;
	}

	public void run(){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String data = "";
			String data2;
			while((data2 = in.readLine()) != null){
				data = data + data2;
			}
			
			Blockchain.blockchain = data; // Would normally have to check first for winner before replacing.
			System.out.println("         --NEW BLOCKCHAIN--\n" + Blockchain.blockchain + "\n\n");
			sock.close();
			
			//if first process, write JSON to disk
			if(this.PID == 0) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				// Write the JSON object to a file:
				try (FileWriter writer = new FileWriter("blockRecord.json")) {
					gson.toJson(data, writer);
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		} 
		catch (IOException x){
			x.printStackTrace();
		}
	}
}

class BlockchainServer implements Runnable {
	private int PID;
	
	BlockchainServer(int PID) {
		this.PID = PID;
	}
	
	public void run(){
		
		Socket sock;
		System.out.println("Starting the blockchain server input thread using " + Ports.BlockchainServerPort);
		try{
			ServerSocket servsock = new ServerSocket(Ports.BlockchainServerPort, Blockchain.q_len);
			while (true) {
				sock = servsock.accept();
				new BlockchainWorker (sock, this.PID).start();
			}
		}
		catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
}

// Main
public class Blockchain {

	public static final int q_len = 6;
	static String serverName = "localhost";
	static String blockchain = "";//"[First block]";
	static final int numProcesses = 1; // Set this to match your batch execution file that starts N processes with args 0,1,2,...N
	static int PID;

	//maybe refactor this
	//Multicast to each process
	public void MultiSend(){
		try{
			//-------MultiCast Keys----------
			System.out.println("BlockFramework multicasting process key to public key servers.\n\n");
			String fakeKey = ("FakeKeyProcess: " + Blockchain.PID);
			Multicast(Ports.KeyServerPort, fakeKey);

			Thread.sleep(1000); // wait for keys to settle, normally would wait for an ack

			//-------MultiCast JSON BlockChain---------
			System.out.println("BlockFramework multicasting blockchain string to blockchain servers.\n\n");
			Multicast(Ports.BlockchainServerPort, blockchain);

			//-------MultiCast UV Blocks----------
			LinkedList<String> JsonRecord = BlockInput.GetJsonListString(PID);
			Iterator<String> iterator = JsonRecord.iterator();
			String current;
			while(iterator.hasNext()){
				current = iterator.next();
				System.out.println("BlockFramework multicasting unverified block string to unverified block servers.\n" + current + "\n");
				current = current.replace("\n", "--linebreak--");
				Multicast(Ports.UnverifiedBlockServerPort, "PID: " + Blockchain.PID + current);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void Multicast(int port, String output) throws IOException {
		Socket sock;
		PrintStream toServer;
		for(int i=0; i< numProcesses; i++){// Send a sample unverified block to each server
			sock = new Socket(serverName, port + (i));
			toServer = new PrintStream(sock.getOutputStream());
			toServer.println(output);
			toServer.flush();
			sock.close();
		}
	}

	public static void main(String args[]){

		PID = (args.length < 1) ? 0 : Integer.parseInt(args[0]); // Process ID
		System.out.println("Andrew's BlockFramework control-c to quit.\n");
		System.out.println("Using processID " + PID + "\n");

		final BlockingQueue<BlockRecord> queue = new PriorityBlockingQueue<>(); // Concurrent queue for unverified blocks
		new Ports().setPorts(); // Establish OUR port number scheme, based on PID

		new Thread(new PublicKeyServer()).start();// New thread to process incoming public keys
		new Thread(new UnverifiedBlockServer(queue)).start();// New thread to process incoming unverified blocks
		new Thread(new BlockchainServer(PID)).start();// New thread to process incoming new blockchains
		
		try{
			Thread.sleep(1000);// Wait for servers to start.
		}catch(Exception e){}
		
		new Blockchain().MultiSend();// Multicast some new unverified blocks out to all servers as data
		
		try{
			Thread.sleep(1000);// Wait for multicast to fill incoming queue for our example.
		}catch(Exception e){}

		new Thread(new UnverifiedBlockConsumer(queue, PID)).start();// Start consuming the queued-up unverified blocks
	}
}



