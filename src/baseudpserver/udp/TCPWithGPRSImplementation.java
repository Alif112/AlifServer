/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package baseudpserver.udp;

import baseudpserver.util.Functions;
import baseudpserver.util.Utility;

/**
 *
 * @author alif
 */
public class TCPWithGPRSImplementation {
    
    private static final byte[] tlliID2={0x4a,0x6a,0x25,0x42,0x43,0x48};
    private static final byte[] qos={0x31,0x34};
    private static final byte[] CI={0x32,0x4e,(byte)0xcc};
    public static final byte protocol=0x06; //TCP
    private static final byte[] optionsAfter=Utility.hexStringToByteArray("010303000101080a000000000000000001010402");
    
    public static byte bvciFirstByte;
    private short locAreaCode,unAcknowledgedMode,ipv4Len,ipv4Identification,headerChecksum;
    private short srcPort,destPort;
    private int seqNumber,ackNumber;
    private short windowSize,checkSum,maximumPacketSize;
    private byte[] fcsData=new byte[3];
    
    
    public TCPWithGPRSImplementation(){
        srcPort=80; 
        destPort=3287;
        maximumPacketSize=1460;
        bvciFirstByte=(byte) Utility.random.nextInt();
        locAreaCode=(short) Utility.random.nextInt();
        unAcknowledgedMode=(short) Utility.random.nextInt();
        ipv4Identification=(short) Utility.random.nextInt();
        headerChecksum = (short) Utility.random.nextInt();
        seqNumber=Utility.random.nextInt();
        ackNumber=Utility.random.nextInt();
        windowSize=(short) Utility.random.nextInt();
        checkSum=(short) Utility.random.nextInt();
        for(int i=0;i<3;i++) fcsData[i]=(byte) Utility.random.nextInt();
    }
    
    public int createPacket(byte [] data, int offset, int len){
        if(data.length <= offset + len + 100)
            return len;
        for(int i = offset + len - 1; i >=offset; i--)
            data[i + 97] = data[i];
        
        int index=offset;
//        GPRS network service
        data[index++]=0x00; //data bits
        data[index++]=0x00; //control bits
        data[index++]=bvciFirstByte; data[index++]=0x32;  //BVCI
//        Base station subsystem GPRS protocol
        index=baseStationGPRSProtocol(data,index);  
//        MS SGSN
        index=MS_SGSN_LLC(data,index);
//        Subnetwork Dependent convergence protocol
        index=startSDCPProtocol(data,index,len);
//        Internet protocol version 4


/**Your desired application layer protocol 
 * can build tcp, udp plus other application layer protocol over tcp/udp
 **/
        index+=len;
        System.arraycopy(fcsData, 0, data, index, 3);
        index+=3;
        String s=Utility.bytesToHex(fcsData);
        System.err.println(""+s);
        bvciFirstByte=(byte) Utility.random.nextInt();
        unAcknowledgedMode=(short) Utility.random.nextInt();
        ipv4Identification=(short) Utility.random.nextInt();
        seqNumber=Utility.random.nextInt();
        ackNumber=Utility.random.nextInt();
        windowSize=(short) Utility.random.nextInt();
        checkSum=(short) Utility.random.nextInt();
        for(int i=0;i<3;i++) fcsData[i]=(byte) Utility.random.nextInt();
        
        return len+100;
    }

    public int decodePacket(byte [] data, int offset, int len){
        srcPort=Functions.getShort2(data, offset+55);
        destPort=Functions.getShort2(data, offset+53);
        bvciFirstByte=data[offset+2];
        locAreaCode=Functions.getShort2(data, offset+18);
        unAcknowledgedMode=Functions.getShort2(data, offset+31);
        ipv4Identification=Functions.getShort2(data, offset+37);
        headerChecksum=Functions.getShort2(data, offset+43);
        seqNumber=Functions.getInt4(data, offset+57);
        ackNumber=Functions.getInt4(data, offset+61);
        windowSize=Functions.getShort2(data, offset+67);
        checkSum=Functions.getShort2(data, offset+69);
        System.arraycopy(data, offset+len-3, fcsData, 0, 3);
        
        System.arraycopy(data, offset+97, data, offset, len-100);

        return len-100;
    }

    private int baseStationGPRSProtocol(byte[] data, int index) {
        data[index++]=0x01;
        //tlli
        data[index++]=(byte) 0xf5;
        data[index++]=tlliID2[Utility.random.nextInt(6)];
        data[index++]=0x66; 
        data[index++]=0x51;
//        qos profile
        data[index++]=0x00;data[index++]=0x00;
        data[index++]=qos[Utility.random.nextInt(2)];
        data[index++]=0x08; data[index++]=(byte) 0x88;
        
//        if(data[index-1]==0x31){
//            data[index++]=0x16;
//            data[index++]=(byte) 0x82;
//        }
//        else{
//            data[index++]=0x08;
//            data[index++]=(byte) 0x88;
//        } 
        data[index++]=0x64; data[index++]=(byte) 0xf0; // need to change, its only for china----------------------------------------------- 
        data[index++]=0x00; data[index++]=0x00;
        Functions.putShort2(data, index, locAreaCode);;
        index+=2;
//        data[index++]=0x01; //Routing area Code
        data[index++]=CI[Utility.random.nextInt(3)];
        data[index++]=(byte) 0x95;
//        ---------------- Allignment octets
        data[index++]=0x00; data[index++]=(byte) 0x80; 
        // LLC data
        data[index++]=0x0e; 
        data[index++]=(byte) 0xca;   // llc len------
        return index;
    }

    private int MS_SGSN_LLC(byte[] data, int index) {
        data[index++]=0x03; //SAPI user data
        data[index++]=(byte) (192+Utility.random.nextInt(9));
        data[index++]=(byte) 0xfd;

        return index;
    }


    private int startSDCPProtocol(byte[] data, int index,int len) {
        data[index++]=0x65; //NSAPI
        data[index++]=0x00; //No compression
        Functions.putShort2(data, index, unAcknowledgedMode);
        index+=2;
        index=startIPv4(data,index,len);
        
        return index;
    }

    private int startIPv4(byte[] data, int index,int len) {
       data[index++]=0x45; //ip version
       data[index++]=0x00;
       ipv4Len=(short) (52);
       Functions.putShort2(data, index, ipv4Len);
       index+=2;
       Functions.putShort2(data, index, ipv4Identification);
       index+=2;
//       fags
       data[index++]=0x40; data[index++]=0x00;
       data[index++]=(byte) (10+Utility.random.nextInt(90));
       data[index++]=protocol;
//       Header checksum
       Functions.putShort2(data, index, headerChecksum);
       index+=2;
       data[index++]=0x01;
       data[index++]=0x01;
       data[index++]=0x01;
       data[index++]=0x01;
       //source
       data[index++]=0x08;
       data[index++]=0x08;
       data[index++]=0x08;
       data[index++]=0x08;
       //destination
       
       index=startDesiredTCPProtocol(data,index,len);
       
        return index;
    }
    
    private int startDesiredTCPProtocol(byte[] data, int index, int len) {
        
        Functions.putShort2(data, index, srcPort);
        index+=2;
        Functions.putShort2(data, index, destPort);
        index+=2;
        Functions.putInt4(data, index, seqNumber);
        index+=4;
        Functions.putInt4(data, index, ackNumber);
        index+=4;
        data[index++]=(byte) (0xb0); //length of header
        data[index++]=(byte) 0x11;    //flag ack
        windowSize=(short) (windowSize<<1);
        windowSize=(short) (windowSize>>1);
        //experimental --------------------------------
        Functions.putShort2(data, index, windowSize);
        index+=2;
        Functions.putShort2(data, index, checkSum);
        index+=2;
        data[index++]=0x00;data[index++]=0x00; //urgent pointer
        //Options -------------------------details of this tcp packet
        data[index++]=0x02; data[index++]=0x04;
        Functions.putShort2(data, index, maximumPacketSize);
        index+=2;
        System.arraycopy(optionsAfter, 0, data, index, optionsAfter.length);
        index+=optionsAfter.length; //20bytes
        
        return index;
    }
}
