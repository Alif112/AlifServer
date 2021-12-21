/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseudpserver.config;

import baseudpserver.util.Functions;
import baseudpserver.util.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author alif
 */
public class Config {
    
    
    public static int protocolType;
    public static int protocolNumber;
    public static String protocolName;
    public static String ipDataSend;
    public static int serverSocketPort;
    public static int dataLen=0,offset=0;
    public static byte[] header;
    public static String headerToSend;
    public static int headerLen;
    public static int loadConfigFromServer;
    public static String serverConfigIP;
    public static int serverConfigPort;
        
    public static int delay;
    public static int socketType;
    public static int numberOfPacketsPerSocket;
    
    
    public static void showConfig(HashMap<String, String> config){
        config.entrySet().forEach(entry -> {
            System.out.println(entry.getKey() + " " + entry.getValue());
        });
    }
    
    
    public static HashMap configuration=new HashMap<String,String> ();
    public static boolean readConfiguration(){
        File file = new File(Constants.filename);
        if(!file.exists())return false;
        try{
            FileReader fileReader=new FileReader(file);
            BufferedReader br=new BufferedReader(fileReader);
            String line;
            while((line = br.readLine()) != null) {
                if(line.startsWith("#"))continue;
                String [] str = line.split("=");
                if(str.length < 2)continue;
                configuration.put(str[0].toLowerCase(), str[1]);
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
        
    
    public static void loadConfig(HashMap<String, String> config) {
        String str=config.get(Constants.protocolNumber.toLowerCase());
        if(str!=null) protocolNumber=Integer.parseInt(str.trim());
        str = config.get(Constants.protocolType.toLowerCase());
        if(str != null){
            protocolType=Integer.parseInt(str.trim());
            if(protocolType==0) protocolName=Constants.protocolNameListUDP[protocolNumber-1000];
            else protocolName=Constants.protocolNameListTCP[protocolNumber-2000];
        }
        str=config.get(Constants.ipDataSend.toLowerCase());
        if(str!=null) ipDataSend=str.trim();
        
        
        str=config.get(Constants.fixedClientPort.toLowerCase());
        if(str!=null) serverSocketPort=Integer.parseInt(str.trim());
        str=config.get(Constants.dataLen.toLowerCase());
        if(str!=null) dataLen=Integer.parseInt(str.trim());
        

        
        str=config.get(Constants.rtpHeader.toLowerCase());
        if(str!=null) {
            headerToSend=str;
            header=Utility.hexStringToByteArray(str.trim());
            headerLen=header.length;
        }
        str=config.get(Constants.loadConfigFromServer.toLowerCase());
        if(str!=null) loadConfigFromServer=Integer.parseInt(str.trim());
        
        str=config.get(Constants.serverConfigIP.toLowerCase());
        if(str!=null) serverConfigIP=str.trim();
        str=config.get(Constants.serverConfigPort.toLowerCase());
        if(str!=null) serverConfigPort=Integer.parseInt(str.trim());
        
        str=config.get(Constants.delay.toLowerCase());
        if(str!=null) delay=Integer.parseInt(str.trim());
        str=config.get(Constants.socketType.toLowerCase());
        if(str!=null) socketType=Integer.parseInt(str.trim());
        str=config.get(Constants.numberOfPacketsPerSocket.toLowerCase());
        if(str!=null) numberOfPacketsPerSocket=Integer.parseInt(str.trim());
        
    }
     
    
    public static void showConfigAfter(){
        System.out.println("-------------------------------------------------------");
        if(protocolType==0) System.out.println("protocol Type udp");
        else System.out.println("protocol Type tcp");
        
        System.out.println("protocol number = "+protocolNumber);
        System.out.println("protocol name = "+protocolName);
        System.out.println("IP = "+ipDataSend);
        System.out.println("Port = "+serverSocketPort);
        System.out.println("data len = "+dataLen);
        System.out.println("Header = "+header);
        System.out.println("HeaderLen = "+headerLen);
        System.out.println("LoadConfigfromServer = "+loadConfigFromServer);
        System.out.println("serverConfigIP = "+serverConfigIP);
        System.out.println("Server Config Port = "+serverConfigPort);
        
        System.out.println("-------------------------------------------------------\n\n");
    }
    static Socket socket;
    static ServerSocket ss;
    public static void sendConfigToClient() throws IOException {
        InetAddress addr = InetAddress.getByName(Config.serverConfigIP);

        ss= new ServerSocket(Config.serverConfigPort,50, addr);
        System.out.println("Config Socket created");
        byte[] data=new byte[2048];
        while(true){
            System.out.println("Before Socket accept");
            socket=ss.accept();
            System.out.println("After Socket accept");

            InputStream is=socket.getInputStream();
            OutputStream os=socket.getOutputStream();
            
            String readData=Functions.readLine(is, data,offset);
            System.err.println(""+readData);
            int len=createConfigPacket(data);
            os.write(data,0,len);
            os.flush();
            
        }
        
    }

    private static int createConfigPacket(byte[] data) {
        int index=0;
        String configPacket=Constants.loadConfigFromServer+"="+loadConfigFromServer+"\n"+
                Constants.protocolType+"="+protocolType+"\n"+
                Constants.protocolNumber+"="+protocolNumber+"\n"+
                Constants.ipDataSend+"="+ipDataSend+"\n"+
                Constants.fixedClientPort+"="+serverSocketPort+"\n"+
                Constants.dataLen+"="+dataLen+"\n"+
                Constants.delay+"="+delay+"\n"+
                Constants.socketType+"="+socketType+"\n"+
                Constants.numberOfPacketsPerSocket+"="+numberOfPacketsPerSocket+"\n"+
                Constants.rtpHeader+"="+headerToSend+"\n";
        
        byte[] send=configPacket.getBytes();
        Functions.putShort2(data, index, (short) send.length);
        index+=2;
        System.arraycopy(send, 0, data, index, send.length);
        index+=send.length;
        return index;
    }
    
        
}
