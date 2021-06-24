package baseudpserver;

import baseudpserver.util.Utility;
import baseudpserver.config.Config;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import baseudpserver.config.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlifServer {
    static ServerSocket ss;
    static Socket s;
    
    public static InetAddress ia,sia;
    
    public static int countsend=0;
    public static int countreceive=0,sequenceNumber;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException {
            try{
        boolean isread=Config.readConfiguration();
        Config.showConfig(Config.configuration);
        if(isread) Config.loadConfig(Config.configuration);
        Config.showConfigAfter();
        initializeConfig();
        
        ia=InetAddress.getByName(Config.ipDataSend);
        sia=InetAddress.getByName("10.0.0.2");
        }catch(Exception e){e.printStackTrace();}
        
        
        System.out.println(Config.protocolName+" server version "+Constants.serverVersion+" Started Successfully.... ");
        
        Thread myConfigThread=new MyConfigureThread();
        myConfigThread.start();
        
        
        
        switch(Config.protocolType){
            case 0:
                Thread t=new MyThread();
                t.start();
                break;
            case 1:
                try{
                    InetAddress addr = InetAddress.getByName(Config.ipDataSend);

                    ss= new ServerSocket(Config.serverSocketPort,50, addr);
                    while(true){
                        s=ss.accept();
                        Thread myThread=new MyTCPThread(s);
                        myThread.start();

                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
        }
        
        
        

    }

    private static void initializeConfig() {
        
        
    }

    private static class MyThread extends Thread {
        
        public MyThread() {
        }

        @Override
        public void run() {
            try{
//                DatagramSocket ds=new DatagramSocket(ServerPort, InetAddress.getByName("localhost"));
                DatagramSocket ds=new DatagramSocket(Config.serverSocketPort, InetAddress.getByName(Config.ipDataSend));
                byte[] b1=new byte[2048];

                DatagramPacket dp1=new DatagramPacket(b1, b1.length);
                DatagramPacket dp2 = new DatagramPacket(b1, b1.length);
                int i=0;
                
                while(true){
                    try {    
                    ds.receive(dp1);
//                    String message=new String(dp1.getData(),0,dp1.getLength());
//                    System.out.println(message);

                    int len1=0,len2=0,sendDataLen;
                    
                    byte[] newdata=new byte[Config.offset+Config.dataLen+1000];
                    sendDataLen=Utility.getRandomData(newdata, Config.offset, Config.dataLen);
                    
                    if(sequenceNumber==256) sequenceNumber=0;
                    newdata[sendDataLen]=(byte) sequenceNumber++;
                    
                    String m1=Utility.bytesToHex(newdata,Config.offset,sendDataLen+1);
//                    System.out.println("--------------> ");
//                    System.out.println(m1);
                    
                    switch(Config.protocolNumber){
                        case Constants.UDP100:
//                            len1=udp100.decodePacket(b1, 0, dp1.getLength(),headerLen);
                            len2=Constants.udp100.createPacket(newdata, Config.offset, sendDataLen,Config.header,Config.headerLen);
                            break;
                        case Constants.UFTP:
                            len1=Constants.uftp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.uftp.createPacket(newdata, Config.offset, sendDataLen+1,ia,sia);
                            break;
                        case Constants.CIGI:
                            len1=Constants.cigi.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.cigi.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.NFS:
                            len1=Constants.nfs.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.nfs.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.NTP:
                            len1=Constants.ntp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.ntp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.SMTP:
                            len1=Constants.snmp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.snmp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.CLDAP:
                            len1=Constants.cldap.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.cldap.createPacket(newdata, Config.offset, sendDataLen+1);
                        case Constants.L2TP:
                            len1=Constants.l2tp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.l2tp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.BFD:
                            len1=Constants.bfd.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.bfd.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.WSP:
                            len1=Constants.wsp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.wsp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;   
                        case Constants.MOUNT:
                            len1=Constants.mount.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.mount.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.STAT:
                            len1=Constants.stat.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.stat.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.ICMPV6:
                            len1=Constants.icmpv6.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.icmpv6.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.LOWPAN:
                            len1=Constants.lowpan.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.lowpan.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.DSPV2:
                            len1=Constants.dspv2.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.dspv2.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.TEPV1:
                            len1=Constants.tepv1.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.tepv1.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.DPPV2:
                            len1=Constants.dppv2.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.dppv2.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.COAP:
                            len1=Constants.coap.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.coap.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.TFTP2:
                            len1=Constants.tftp2.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.tftp2.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.IPV6:
                            len1=Constants.ipv6.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.ipv6.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.LTP:
                            len1=Constants.ltp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.ltp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.XTACACS:
                            len1=Constants.xtacacs.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.xtacacs.createPacket(newdata, Config.offset, sendDataLen+1, (Inet4Address) ia,Config.serverSocketPort);
                            break;
                        case Constants.ISAKMP:
                            len1=Constants.isakmp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.isakmp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.BVLC:
                            len1=Constants.bvlc.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.bvlc.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.MMSE:
                            len1=Constants.mmse.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.mmse.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.SLIMP3:
                            len1=Constants.slimp3.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.slimp3.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.AUTORP:
                            len1=Constants.autorp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.autorp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.MIOP:
                            len1=Constants.miop.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.miop.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.EDONKEY:
                            len1=Constants.edonkey.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.edonkey.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.UAUDP:
                            len1=Constants.uaudp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.uaudp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.DROPBOX:
                            len1=Constants.dropbox.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.dropbox.createPacket(newdata, Config.offset, sendDataLen+1,Config.serverSocketPort);
                            break;
                        case Constants.RDT:
                            len1=Constants.rdt.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.rdt.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.MAC_TELNET:
//                            len1=Constants.macTelnet.decodePacket(b1, 0, dp1.getLength());
//                            len2=Constants.macTelnet.createPacket(newdata, offset, sendDataLen+1);
                            break;
                        case Constants.DCP_PFT:
                            len1=Constants.dcp_pft.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.dcp_pft.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.WIREGUARD:
                            len1=Constants.wireGuard.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.wireGuard.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                        case Constants.DNP:
                            len1=Constants.dnp.decodePacket(b1, 0, dp1.getLength());
                            len2=Constants.dnp.createPacket(newdata, Config.offset, sendDataLen+1);
                            break;
                                
                                
                    }
                    
                    /**show decoded message**/
                    System.out.println(Config.protocolName+" Received ---------------> "+len1);
//                    String ack=Utility.bytesToHex(b1, 0, len1);                   
//                    System.out.println(ack);

                  
                    /**show new encoded message**/
                    System.out.println("Sending from server ----->   "+ (sendDataLen+1));
                    String m=Utility.bytesToHex(newdata,Config.offset,len2);
//                    System.out.println(m);
                    
                    byte[] b2=Utility.hexStringToByteArray(m);
                    

                    dp2.setData(b2);
                    dp2.setAddress(dp1.getAddress());
                    dp2.setPort(dp1.getPort());
                    ds.send(dp2);

//                    System.out.println("------sending from server to ip:port: "+dp.getAddress()+":"+ServerToClientPort);
                    countsend+=1;
                    System.out.println(ds+"Total Count ------------------->  "+countsend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        
    }
    
    
    public static class MyTCPThread extends Thread {
        Socket s;
        public MyTCPThread(Socket s) {
            this.s=s;
        }

        @Override
        public void run() {
            try{
                int len2=0;
                s.setTcpNoDelay(true);
//                isr=new InputStreamReader(s.getInputStream());
                boolean isHandShake=false,handShakeReq=false;
                switch (Config. protocolNumber){
                    case Constants.IMAP:
                        isHandShake=Constants.imap.imapHandshakeAtServer(s);
                        handShakeReq=true;
                        break;
                    case Constants.SMTP:
                        isHandShake=Constants.smtp.smtpHandshakeAtServer(s);
                        handShakeReq=true;
                        break;
                    case Constants.IPA:
                        isHandShake=Constants.ipa.ipaHandshakeAtServer(s);
                        handShakeReq=true;
                        break;
                    case Constants.CQL:
                        isHandShake=Constants.cql.cqlHandshakeAtServer(s);
                        handShakeReq=true;
                        break;
                }

                if(handShakeReq && !isHandShake) throw new Exception("HandShaking Failed!!!");
                
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();
                byte [] data = new byte[Config.offset+Config.dataLen+2048];
//                br=new BufferedReader(isr);
                
                while(true){
                    switch(Config.protocolNumber){
                    case Constants.NINEP2000:
                        len2=Constants.nineP2000.decodePacket(data, Config.offset, is);
                        break;
                    case Constants.COPS:
                        len2=Constants.cops.decodePacket(data, Config.offset, is);
                        break;
                    case Constants.EXEC:
                        len2=Constants.exec.decodePacket(data, Config.offset, is);
                        break;
                    case Constants.TCP:
                        len2=Constants.tcp.decodePacket(data, Config.offset, is);
                        break;
                    case Constants.IMAP:
                        len2=Constants.imap.decodePacketAtServer(data, Config.offset, is);
                        break;
                    case Constants.SMTP:
                        len2=Constants.smtp.decodePacket(data, Config.offset, is);
                        break;
                    case Constants.IPA:
                        len2=Constants.ipa.decodePacketAtServer(data, Config.offset, is);
                        break;
                    case Constants.CQL:
                        len2=Constants.cql.decodePacketAtServer(data, Config.offset, is);
                        break;
                    case Constants.BGP:
                        len2=Constants.bgp.decodePacket(data, Config.offset, is);
                        break;
                        
                            
                }
                    
                    if(len2<0){
                        System.out.println("---------------------------------------> "+len2);
                        break;
                    }
                    String msg=Utility.bytesToHex(data, Config.offset, len2);
//                    System.out.println(msg);
                    System.out.println("Received at Server=================> "+len2);

                    byte[] newdata=new byte[Config.offset+Config.dataLen+500];
                    int sendDataLen=Utility.getRandomData(newdata, Config.offset, Config.dataLen);
                    if(sequenceNumber==256) sequenceNumber=0;
                    newdata[sendDataLen]=(byte) sequenceNumber++;
                    
                    String m1=Utility.bytesToHex(newdata,Config.offset,sendDataLen+1);
//                    System.out.println("--------------> ");
//                    System.out.println(m1);
                    
                    
                    switch(Config.protocolNumber){
                    case Constants.NINEP2000:
                        len2=Constants.nineP2000.createPacket(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.COPS:
                        len2=Constants.cops.createPacket(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.EXEC:
                        len2=Constants.exec.createPacket(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.TCP:
                        len2=Constants.tcp.createPacket(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.IMAP:
                        len2=Constants.imap.createPacketAtServer(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.SMTP:
                        len2=Constants.smtp.createPacket(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.IPA:
                        len2=Constants.ipa.createPacketAtServer(newdata, Config.offset, sendDataLen+1);
                        break;
                    case Constants.CQL:
                        len2=Constants.cql.createPacketAtServer(newdata, Config.offset, sendDataLen+1);
                        break;    
                    case Constants.BGP:
                        len2=Constants.bgp.createPacket(newdata, Config.offset, sendDataLen+1);
                        break;
                        
                }
                    
                    
                    String m=Utility.bytesToHex(newdata,Config.offset,len2);
//                    System.out.println("================================>          "+ len2);
//                    System.out.println(m);
                    byte[] senddata=Utility.hexStringToByteArray(m); 
                    
//                    os.write(message);
                    os.write(senddata);
                    
                    System.out.println((sendDataLen+1)+" Bytes of "+Config.protocolName+" data sending -->number of packet is "+ countsend++);
                }
                
            
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }

    private static class MyConfigureThread extends Thread {

        public MyConfigureThread() {
        }

        @Override
        public void run() {
            try {
                Config.sendConfigToClient();
            } catch (IOException ex) {
                Logger.getLogger(AlifServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }

    
}