package com.ivan.usbhostdemo;

public class Tools {	  
	public  static  byte[] string2Bytes(String src,int byte_len){  
        byte[] b1 = new byte[byte_len];  
            StringBuffer tempb = new StringBuffer();     // ��ʱ�������  
            int temp_len = 0;                            // ��ʱ�ַ��Ĵ�   
            int src_len = src.length();                  // ��Ҫת�����ַ����ĳ���  
            char zero = '\0';  
            if(src_len<byte_len){  
                temp_len = byte_len - src_len;           // �������λ�Ĵ�С  
                for(int i=0;i<temp_len;i++){             // ѭ�����ռλ��  
                    tempb.append(zero);  
                }   
                b1 = (src+tempb.toString()).getBytes();   
//             tempb.delete(0, temp_len);               // �����ʱ�ַ�  
            }else{                                       // ������ڴ˹涨�ĳ��ȣ���ֱ��תΪbtye����  
                b1 = src.getBytes();  
            }    
            return b1;  
    } 
	
	 // byte[] --> String  
    public static String bytes2String(byte b[]){  
        String result_str = new String(b);  
        return result_str;  
    }
}


