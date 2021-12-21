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
public class IPv4withGPRSImplementation {
    
    private static final byte[] tlliID2={0x4a,0x6a,0x25,0x42,0x43,0x48};
    private static final byte[] qos={0x31,0x34};
    private static final byte[] CI={0x32,0x4e,(byte)0xcc};
    public static final byte protocol=0x06; //TCP
    private static final byte[] MS_RA_CAPABILITY1=Utility.hexStringToByteArray("3767022a85464200");
    private static final byte[] MS_RA_CAPABILITY2=Utility.hexStringToByteArray("2a6604321000");
    private static final byte[] IMSI=Utility.hexStringToByteArray("0d884906008301337070");
    
    public static short bvci;
    private short locAreaCode,unAcknowledgedMode,ipv4Len,ipv4Identification,headerChecksum;
    private byte[] fcsData=new byte[3];
    
    public static short delayValue;
    private byte[] destData;
    
    public IPv4withGPRSImplementation(){
        bvci= 50;
        locAreaCode=(short) Utility.random.nextInt();
        unAcknowledgedMode=(short) Utility.random.nextInt();
        ipv4Identification=(short) Utility.random.nextInt();
        headerChecksum = (short) Utility.random.nextInt();
        delayValue=(short) Utility.random.nextInt();
        destData=new byte[32];
        
        for(int i=0;i<3;i++) fcsData[i]=(byte) Utility.random.nextInt();
    }
    
    public int createPacket(byte [] data, int offset, int len){
        if(data.length <= offset + len + 80)
            return len;
        for(int i = offset + len - 1; i >=offset+32; i--)
            data[i + 80] = data[i];
        for(int i=offset+31,j=31;i>=offset;i--,j--) destData[j]=data[i];
        int index=offset;
//        GPRS network service
        data[index++]=0x00; //data bits
        data[index++]=0x00; //control bits
        Functions.putShort2(data, index, bvci);  //BVCI
        index+=2;
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
        System.arraycopy(destData, 0, data, index, destData.length);
        index+=destData.length;
        System.arraycopy(fcsData, 0, data, index, 3);
        index+=3;
        index+=len-32;
        String s=Utility.bytesToHex(fcsData);
        System.err.println(""+s);
        bvci=(short) Utility.random.nextInt();
        unAcknowledgedMode=(short) Utility.random.nextInt();
        ipv4Identification=(short) Utility.random.nextInt();
        delayValue=(short) Utility.random.nextInt();
        for(int i=0;i<3;i++) fcsData[i]=(byte) Utility.random.nextInt();
        return len+80;
    }

    public int decodePacket(byte [] data, int offset, int len){
        bvci=Functions.getShort2(data, offset+2);
        unAcknowledgedMode=Functions.getShort2(data, offset+55);
        ipv4Identification=Functions.getShort2(data, offset+61);
        
        System.arraycopy(data, offset+77, data, offset, 32);
        System.arraycopy(data, offset+112, data, offset+32, len-32);

        return len-80;
    }

    private int baseStationGPRSProtocol(byte[] data, int index) {
        data[index++]=0x00;
        //tlli
        data[index++]=(byte) 0xf5;
        data[index++]=tlliID2[Utility.random.nextInt(6)];
        data[index++]=0x66; 
        data[index++]=0x51;
//        qos profile
        data[index++]=0x00;data[index++]=0x00;
        data[index++]=qos[Utility.random.nextInt(2)];
        //--------------------PDU lifetime-------------
        data[index++]=0x16;
        data[index++]=(byte) 0x8a; // len 10
        Functions.putShort2(data, index, delayValue);
        index+=2;
        data[index++]=0x13;
        data[index++]=(byte) 0x90;  // length 16
        System.arraycopy(MS_RA_CAPABILITY1, 0, data, index, MS_RA_CAPABILITY1.length);
        index+=MS_RA_CAPABILITY1.length; //8bytes
        data[index++]=0x02;
        System.arraycopy(MS_RA_CAPABILITY2, 0, data, index, MS_RA_CAPABILITY2.length);
        index+=MS_RA_CAPABILITY2.length; //6 bytes
        data[index++]=0x00;
        //----------------------------DRX parameter------------------
        data[index++]=0x0a;
        data[index++]=(byte) 0x82; //len 2
        data[index++]=(byte) Utility.random.nextInt(10);
        data[index++]=0x03;
        //------------------------- DRX done-------------------------
        //--------------------- IMSI---------------------------------
        System.arraycopy(IMSI, 0, data, index, IMSI.length);
        index+=IMSI.length;
        // imsi done-----------------------------
        // LLC data
        data[index++]=0x0e; 
        data[index++]=(byte) 0xbe;   // llc len------
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
//        data[index++]=0x00; data[index++]=0x15; 
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
//       data[index++]=0x00; data[index++]=0x00; 
       Functions.putShort2(data, index, ipv4Identification);
       index+=2;
       data[index++]=0x25; data[index++]=0x00;  //flags------
       
       data[index++]=(byte) (10+Utility.random.nextInt(90));
       data[index++]=protocol;
//       Header checksum
//       Functions.putShort2(data, index, headerChecksum);
//       index+=2;
       data[index++]=0x32; data[index++]=0x41;
       //source
       data[index++]=0x01;
       data[index++]=0x01;
       data[index++]=0x01;
       data[index++]=0x01;
       //destination
       data[index++]=0x08;
       data[index++]=0x08;
       data[index++]=0x08;
       data[index++]=0x08;
       
//       index=startDesiredTCPProtocol(data,index,len);
       
        return index;
    }
    
}
