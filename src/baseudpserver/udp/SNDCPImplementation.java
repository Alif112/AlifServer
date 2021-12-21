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
public class SNDCPImplementation {
    private static final byte[] tlliID2={0x4a,0x6a,0x25,0x42,0x43,0x48};
    private static final byte[] qos={0x31,0x34};
    private static final byte[] CI={0x32,0x4e,(byte)0xcc};
    
    
    public static byte bvciFirstByte;
    private short locAreaCode,unAcknowledgedMode;
    private byte[] fcsData=new byte[3];
    
    
    public SNDCPImplementation(){
        bvciFirstByte=(byte) Utility.random.nextInt();
        locAreaCode=(short) Utility.random.nextInt();
        unAcknowledgedMode=(short) Utility.random.nextInt();
        for(int i=0;i<3;i++) fcsData[i]=(byte) Utility.random.nextInt();
    }
    
    public int createPacket(byte [] data, int offset, int len){
        if(data.length <= offset + len + 36)
            return len;
        for(int i = offset + len - 1; i >=offset; i--)
            data[i + 33] = data[i];
        
        int index=offset;
//        GPRS network service
        data[index++]=0x00; //data bits
        data[index++]=0x00; //control bits
        data[index++]=bvciFirstByte; data[index++]=0x32;  //BVCI
//        Base station subsystem GPRS protocol
        index=baseStationGPRSProtocol(data,index,len);  //22bytes
//        MS SGSN
        index=MS_SGSN_LLC(data,index); //3bytes
//        Subnetwork Dependent convergence protocol
        index=startSDCPProtocol(data,index,len); //4bytes
//        Internet protocol version 4


/**Your desired application layer protocol 
 * can build tcp, udp plus other application layer protocol over tcp/udp
 **/
        index+=len;
        System.arraycopy(fcsData, 0, data, index, 3);
        index+=3;
//        String s=Utility.bytesToHex(fcsData);
//        System.err.println(""+s);
        bvciFirstByte=(byte) Utility.random.nextInt();
        unAcknowledgedMode=(short) Utility.random.nextInt();
        for(int i=0;i<3;i++) fcsData[i]=(byte) Utility.random.nextInt();
        
        return len+36;
    }

    public int decodePacket(byte [] data, int offset, int len){
        bvciFirstByte=data[offset+2];
        locAreaCode=Functions.getShort2(data, offset+18);
        unAcknowledgedMode=Functions.getShort2(data, offset+31);
        System.arraycopy(data, offset+len-3, fcsData, 0, 3);
        System.arraycopy(data, offset+33, data, offset, len-36);

        return len-36;
    }

    private int baseStationGPRSProtocol(byte[] data, int index,int len) {
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
        data[index++]=0x10; //Data compression
        Functions.putShort2(data, index, unAcknowledgedMode);
        index+=2;
        
        return index;
    }

}
