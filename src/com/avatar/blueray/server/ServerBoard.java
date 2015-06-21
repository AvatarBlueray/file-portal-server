package com.avatar.blueray.server;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;


import java.awt.image.BufferedImage;

import java.io.File;



import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;


import java.util.ArrayList;
import java.util.Arrays;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane ;
import javax.swing.ImageIcon;

import javax.swing.ImageIcon;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;





import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
 
//import java.awt.*;
public class ServerBoard extends JFrame {

    private TCPServer mServer; 
    private TCPServerData mServerData;
    private String workdir;
    
    
    private ArrayList<String> fileList;
    private ArrayList<String> dirList;
    private int serverstate = 0;
    
    public String localip = "";
    public String localport = "";
    private boolean isRecived = false;
    
    private long filelength = 0;
    
    private long fileslength = 0;
  
    private static Object[] appendValue(Object[] obj, Object newObj) {
    	 
    	ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
    	temp.add(newObj);
    	return temp.toArray();
     
      }
    
    public static String getLocalIpAddress() {
    	 String ret = null;
    	 
    	 Object[] possibilities = {};
    	 
    	 
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        ret =  inetAddress.getHostAddress();
                        possibilities =   appendValue(possibilities,ret);
                        System.out.println("ip adress  "+ ret);
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        
        if (possibilities.length > 1) {
        JFrame frame = new JFrame("InputDialog Example #1");
        
        //Object[] possibilities = {"ham", "spam", "yam"};
        String s = (String)JOptionPane.showInputDialog(
                            frame,
                            "",
                            "Select server IP",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            possibilities,
                            ret);

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
        	ret = s;
        	 System.out.println("ip adress  "+ s );
        }
        }
        
        return ret;
    }
    
    public void listFilesForFolder(final File folder, String directory) {

        if (folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
              
            	System.out.println("List folder 1 "+ fileEntry.getPath());
            	System.out.println("List folder 1 "+ fileEntry.getParent());
                listFilesForFolder(fileEntry, directory);
               
            }
        } else {

           
            String path = folder.getPath();
            
            fileslength = fileslength + folder.length();
         
            System.out.println("List folder 2 "+ path);
            System.out.println("List folder 2 "+ directory);
            fileList.add(path);
            dirList.add(directory);
        }

    }    
    
    public void filesend(){
    	String filepath;
    	
    	if ( fileList.size() == 0){
    		mServer.sendMessage("off");
    		if ( isRecived )
				try {
					Desktop.getDesktop().open(new File(workdir));
				} catch (IOException e) {
					// 
					//e.printStackTrace();
				}
    		System.exit(0);
        }else{
        	if (serverstate == 0){
                serverstate = 1;
                File file = new File(fileList.get(0));
               
                filepath = fileList.get(0).replace(dirList.get(0), "");
                filepath = filepath.replace("\\", "/");
                System.out.println("file:"+filepath+":"+Long.toString(file.length()));
                mServer.sendMessage("file:"+filepath+":"+Long.toString(file.length()));
            }else{
                serverstate = 0;
                
                

                filepath = fileList.get(0);
                fileList.remove(0);
                dirList.remove(0);
                
                mServerData.filesend(filepath);

              /*
                if (fileList.size()>0){
                    serverstate = 1;
                    File file = new File(fileList.get(0));
                    filepath = fileList.get(0).replace(dirList.get(0), "");
                    filepath = filepath.replace("\\", "/");
                    System.out.println("file:"+filepath+":"+Long.toString(file.length()));
                    mServer.sendMessage("file:"+filepath+":"+Long.toString(file.length()));
                }else {
                	mServer.sendMessage("off");
                	System.exit(0);
                }
*/

            }
        }
    }

    

    


    public static void main(String[] args) {
    	System.out.println("S: 1...");
    	
    	if (args.length == 0) {
            System.out.println("no arguments were given.");
        }
        else {
            for (String a : args) {
                System.out.println(a);
            }
        }

        //opens the window where the messages will be received and sent
        ServerBoard frame = new ServerBoard();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
 

    }
    
    

    public ServerBoard() {
    	

        super("ServerBoard");
        System.out.println("S: 2...");

        JPanel panelFields = new JPanel();
        panelFields.setLayout(new BoxLayout(panelFields,BoxLayout.X_AXIS));

        JPanel panelFields2 = new JPanel();
        panelFields2.setLayout(new BoxLayout(panelFields2,BoxLayout.X_AXIS));

        fileList = new ArrayList<String>();
        dirList = new ArrayList<String>();
        
        workdir = ServerBoard.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String[] parts = workdir.split("/");
        workdir = "";
        for (int i = 1; i< parts.length-1; i++){
        	workdir = workdir.concat(parts[i]).concat(File.separator);
        }
        workdir = workdir.concat("Download").concat(File.separator); 

              JPanel panel3 = new JPanel();
              
       
        
        new FileDrop( panel3, new FileDrop.Listener()
              {   public void filesDropped( java.io.File[] files )
                  {   
                      // handle file drop
            	  for( int i = 0; i < files.length; i++ )
                  {   listFilesForFolder(files[i],files[i].getParent());
				  //	messagesArea.append("\n"+files[i].getCanonicalPath());
                  }   // end for: through each dropped file
                  }   // end filesDropped
              }); // end FileDrop.Listener
        

        getContentPane().add(panel3);
       // getContentPane().add(panelFields2);
       

     

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        
       // getContentPane().setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2) ;

        setSize(300, 170);
        setLocation((dim.width - 240 )/2,(dim.height-260)/2);
        setTitle("FilePortal Server");
        
       // URL url = ClassLoader.getSystemClassLoader().getResource("icons/ic_launcher.png");
       // Icon icon = new ImageIcon(url);
       // System.out.println(ClassLoader.getSystemClassLoader().getResource("icons/ic_launcher.png"));
        
        //ImageIcon img = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/ic_launcher.png"));
        
        Toolkit kit = Toolkit.getDefaultToolkit();
        ;
        this.setIconImage(kit.getImage(getClass().getResource("icon.png")));
        
        setVisible(true);
        System.out.println("S: 3...");
        
       // startServer.setEnabled(false);

        //creates the object OnMessageReceived asked by the TCPServer constructor
        mServer = new TCPServer(new TCPServer.OnMessageReceived() {
            @Override
            //this method declared in the interface from TCPServer class is implemented here
            //this method is actually a callback method, because it will run every time when it will be called from
            //TCPServer class (at while)
            public void messageReceived(String message) {
            	System.out.println(message);
            	if	(  "port".equals(message) ) {
            		System.out.println("S: port.......");
            		mServer.sendMessage(Integer.toString(mServerData.SERVERPORT));
            	}
            	if ( message.startsWith("filelength!")){
            		/*String[] parts;
            		parts = message.split("!");
            		totalfileslength = fileslength + Long.parseLong(parts[1]);*/
            		mServer.sendMessage("filelength!"+Long.toString(fileslength));
            	}
            	
            	if	(  "connected".equals(message) ) {
            		System.out.println("S: connected.......");
            		mServer.sendMessage("ok");
            	}
            	if	(  "turn".equals(message) ) {
            		            		
            		while (mServerData.filerecived  < filelength){
            			try {
            				System.out.println(Long.toString(mServerData.filerecived )+" "+Long.toString(filelength ));
        					Thread.sleep(100);
        				} catch (InterruptedException e) {
        					// 
        					e.printStackTrace();
        				}
            		}
            		
            		mServerData.file_close();
            		System.out.println("S: turn.......");
            		filesend();
            		
            	}
            	
            	if ( "ok".equals(message)){
                    filesend();
                   // mTcpClient.sendMessage("turn");
                }
            	if (message.contains(":")){
            		while (mServerData.filerecived  < filelength){
            			try {
        					Thread.sleep(100);
        				} catch (InterruptedException e) {
        					// 
        					e.printStackTrace();
        				}
            		}
            		mServerData.file_close();
            		
            		String[] parts;
            		parts = message.split(":");
            		for (int i = 0; i < parts.length;i++){
            		//	messagesArea.append("\n parts-"+parts[i]);
            		}
            		isRecived = true;
            		filelength = Long.parseLong(parts[2]);
            		mServerData.file_open(parts[1]);
            		mServer.sendMessage("ok");
            	}
               // messagesArea.append("\n "+message);
            }
        });
        mServer.start();
        
         mServerData = new TCPServerData( new TCPServerData.OnMessageReceived() {
			
			@Override
			public void messageReceived(String message) {
				if ("filesended".equals(message)){
					filesend();
				}
				if ("error".equals(message)){
					mServer.sendMessage("off");
		    		
		    		System.exit(0);
				}
				// 
				//System.out.println("S: Data received...");
				//System.out.println("S: Data received..."+ Integer.toString(message.length));
				
				
			}
		});
         
         mServerData.workdir = workdir;
         
         mServerData.start();
         
       /*  while ( mServer.SERVERPORT == 0) ;
         {
        	 
        	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         };
        	 */

         while (mServer.SERVERPORT == 0){
        	 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	 System.out.println("sleep");
         }
         System.out.println("S: server port: "+Integer.toString(mServer.SERVERPORT));    
         
         String myCodeText = getLocalIpAddress()+":"+Integer.toString(mServer.SERVERPORT);
       //	 String filePath = workdir.concat(File.separator).concat("QR.png");
       	 int size = 55;
          //  String fileType = "png";
        //    File myFile = new File(filePath);
          //  try {
                Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix byteMatrix = null;
    			try {
    				byteMatrix = qrCodeWriter.encode(myCodeText,BarcodeFormat.QR_CODE, size, size, hintMap);
    			} catch (WriterException e1) {
    				// 
    				e1.printStackTrace();
    			}
    			
    			 int offset = 0;
    			 int CrunchifyWidth = byteMatrix.getWidth();
    			 for (int i = 0; i < CrunchifyWidth;i++){
    				 if ( byteMatrix.get(i, i)){
    					 offset = i - 1;
    					 break;
    				 }
    			 }
    			 int pixelsize = 10;
               
                BufferedImage image = new BufferedImage((CrunchifyWidth-offset*2)*pixelsize, (CrunchifyWidth-offset*2)*pixelsize,
                        BufferedImage.TYPE_INT_RGB);
                image.createGraphics();
     
                Graphics2D graphics = (Graphics2D) image.getGraphics();
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, CrunchifyWidth*pixelsize, CrunchifyWidth*pixelsize);
                graphics.setColor(Color.BLACK);
     
                for (int i = offset; i < CrunchifyWidth - offset; i++) {
                    for (int j = offset; j < CrunchifyWidth - offset; j++) {
                        if (byteMatrix.get(i, j)) {
                            graphics.fillRect((i-offset)*pixelsize, (j-offset)*pixelsize, pixelsize, pixelsize);
                        }
                    }
                }
                ImageIcon background = new ImageIcon(image);
               	 
               	 JLabel label = new JLabel();
               	 label.setBounds(0, 0, CrunchifyWidth, CrunchifyWidth);
               	 label.setIcon(background);
               	
               	 panel3.setLayout(new BoxLayout(panel3,BoxLayout.X_AXIS));
               	 panel3.add(label);
               	 getContentPane().add(panel3);
    				//ImageIO.write(image, fileType, myFile);         
         
         

        System.out.println("S: server port: "+Integer.toString(mServer.SERVERPORT));    
        
    }
    
}
 