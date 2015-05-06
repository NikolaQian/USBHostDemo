package com.ivan.usbhostdemo;

public class Tools {	  
	public  static  byte[] string2Bytes(String src,int byte_len){  
        byte[] b1 = new byte[byte_len];  
            StringBuffer tempb = new StringBuffer();     // 临时填充数据  
            int temp_len = 0;                            // 临时字符的大   
            int src_len = src.length();                  // 需要转换的字符串的长度  
            char zero = '\0';  
            if(src_len<byte_len){  
                temp_len = byte_len - src_len;           // 计算填充位的大小  
                for(int i=0;i<temp_len;i++){             // 循环填充占位符  
                    tempb.append(zero);  
                }   
                b1 = (src+tempb.toString()).getBytes();   
//             tempb.delete(0, temp_len);               // 清空临时字符  
            }else{                                       // 如果大于此规定的长度，则直接转为btye类型  
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


