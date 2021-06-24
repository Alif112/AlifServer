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
public class DNPImplementation {
    private byte seqNumber;
    private static final byte[] sampleData=Utility.hexStringToByteArray("c48180000102000101011e01000101");
    private static final byte[] sampleData_2=Utility.hexStringToByteArray("01a9f6ffff");
    private int srcPort;
    private int destPort;
    
    
    public int createPacket(byte[] data, int offset, int len){
        if(data.length<=offset+len+35) return len;
        for(int i=offset+len-1;i>=offset;i--) data[i+33]=data[i];
        
        int index=offset;
        
//        initiator
        data[index++]=0x05; data[index++]=0x64; 
        data[index++]=0x1a; //data length 
        data[index++]=(byte) 0x44;
        
        Functions.putShort2(data, index, (short) destPort);
        data[index] = (byte) (data[index] ^ data[index+1]);
        data[index+1] = (byte) (data[index] ^ data[index+1]);
        data[index] = (byte) (data[index] ^ data[index+1]);
        index+=2;
        
        Functions.putShort2(data, index, (short) srcPort);
        data[index] = (byte) (data[index] ^ data[index+1]);
        data[index+1] = (byte) (data[index] ^ data[index+1]);
        data[index] = (byte) (data[index] ^ data[index+1]);
        index+=2;
        
        //checksum
        data[index++]=(byte) 0xf0; data[index++]=(byte) 0x9d;
        //seq
        data[index++]=seqNumber;
        System.arraycopy(sampleData,0, data, index, sampleData.length);
        index+=sampleData.length; //15
        data[index++]=(byte) 0xb1; data[index++]=(byte) 0xcc;
        
        System.arraycopy(sampleData_2,0, data, index, sampleData_2.length);
        index+=sampleData_2.length; //5
        
        
        index+=len;
        data[index++]=(byte) 0x3a; data[index++]=0x38;
        
        return len+35;
    }
    
    
    public int decodePacket(byte[] data,int offset, int len){
        data[offset+4] = (byte) (data[offset+4] ^ data[offset+5]);
        data[offset+5] = (byte) (data[offset+4] ^ data[offset+5]);
        data[offset+4] = (byte) (data[offset+4] ^ data[offset+5]);
        srcPort=Functions.getShort2(data, offset+4);
        
        data[offset+6] = (byte) (data[offset+6] ^ data[offset+7]);
        data[offset+7] = (byte) (data[offset+6] ^ data[offset+7]);
        data[offset+6] = (byte) (data[offset+6] ^ data[offset+7]);
        destPort=Functions.getShort2(data, offset+6);
        
        seqNumber=data[offset+10];
        System.arraycopy(data, offset+23, data, offset, len-25);
        
        return len-25;
    }
    
    
    
}
