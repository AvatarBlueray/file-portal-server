package com.avatar.blueray.server;
//import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * The class extends the Thread class so we can receive and send messages at the same time
 */
public class TCPServerData extends Thread {

    public int SERVERPORT = 0;
    private boolean running = false;
  
    private OnMessageReceived messageListener;
    private FileOutputStream fop = null;
    
    public long filerecived = 0;
    
    public String workdir;
    
   
    BufferedOutputStream bos = null;


    /**
     * Constructor of the class
     * @param messageListener listens for the messages
     */
    public TCPServerData(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }


    /**
     * Method to send the messages from server to client
     * @param message the message sent by the server
     */

    
    public  void file_close(){
    	System.out.println("try to close ");
    	if ( fop != null ){
    		
    		
    		try {
				fop.flush();
				fop.close();
				System.out.println("file closed ");
			} catch (IOException e) {
			
				e.printStackTrace();
			}
    		
    		fop = null;
    	}
    }    
    
    public void filesend(String filepath){
    	  BufferedInputStream bin = null;
          FileInputStream fileInputStream = null;



          try {
              fileInputStream = new FileInputStream(filepath);
              bin = new BufferedInputStream(fileInputStream);
              
              byte[] buf = new byte[1024*32];
              int len;

              try {
                  while((len = bin.read(buf)) > 0) {
                  	
                    if (bos != null ) {
                        try {
                            bos.write(buf,0,len);
                            
                        } catch (IOException e) {
                        	e.printStackTrace();
                        	System.out.println("error data send");
                        	 messageListener.messageReceived("error");
                        }

                    }
                  }
                  bos.flush();
              } catch (IOException e) {
                  e.printStackTrace();
              }

          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
          
          messageListener.messageReceived("filesended");

    }

    public void file_open( String path){
    	
    	//isRecived = true;
    	
    	String filepath = "";
    	String dirpath = "";
    	File file;
    	File dir;
    	System.out.println(path);
    	
    	
    	
    	filerecived = 0;

    	String[] parts = path.split("/");
    	filepath = workdir;
    	dirpath = workdir;
        for (int i = 0; i< parts.length; i++){
        	if (i < parts.length - 1) {
        		filepath = filepath.concat(parts[i]).concat(File.separator);
        		dirpath = dirpath.concat(parts[i]).concat(File.separator);
        	}else{
        		filepath = filepath.concat(parts[i]);
        	}
        	        	
        }
        




        
        System.out.println(Charset.defaultCharset());
        
        file = new File(filepath);
        
        dir = new File(dirpath);
       
       
        dir.mkdirs();
        try {
			file.createNewFile();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
        
        try {
        	System.out.println("file opened "+filepath);
			fop = new FileOutputStream(file,false);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
        
        
    }
    @Override
    public void run() {
        super.run();

        running = true;

        try {
            System.out.println("Data: DAta Connecting...");

            //create a server socket. A server socket waits for requests to come in over the network.
            ServerSocket serverSocket = new ServerSocket(0);
            SERVERPORT = serverSocket.getLocalPort();
            System.out.println("Data: Port ... " + Integer.toString(SERVERPORT) ); 
            

            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
            Socket client = serverSocket.accept();
            System.out.println("data: data Receiving...");
            

            try {

                //sends the message to the client
               // mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            	client.setSendBufferSize(1024*64);
            	client.setReceiveBufferSize(1024*64);
            	bos = new BufferedOutputStream( client.getOutputStream() );
            	
               // os = client.getOutputStream();


                //read the message received from client
               // BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                InputStream is =  new BufferedInputStream( client.getInputStream());

                //in this while we wait to receive messages from client (it's an infinite loop)
                //this while it's like a listener for messages
                
                while (running) {
                   // String message = in.readLine();
                	int bytesRead;
                	byte[] aByte = new byte[1024*64];
                	bytesRead = is.read(aByte);
                	

                    if (bytesRead != -1 && messageListener != null) {
                        //call the method messageReceived from ServerBoard class
                    	
                    	if	(fop != null ){
        					try {
        						if(bytesRead != -1){
        							fop.write(aByte,0,bytesRead);
        							filerecived += bytesRead;
        						}
        					} catch (IOException e) {
        						
        						e.printStackTrace();
        					}
        					
        				}
                    	
                      //  messageListener.messageReceived(aByte,bytesRead);
                    }
                }

            } catch (Exception e) {
                System.out.println("data: Error");
                e.printStackTrace();
            } finally {
                client.close();
                serverSocket.close();
                System.out.println("data: Done.");
            }

        } catch (Exception e) {
            System.out.println("data: Error2");
            e.printStackTrace();
        }

    }


    //Declare the interface. The method messageReceived(String message) will must be implemented in the ServerBoard
    //class at on startServer button click
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}