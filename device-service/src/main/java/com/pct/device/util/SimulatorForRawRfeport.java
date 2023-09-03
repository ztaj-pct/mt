package com.pct.device.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


public class SimulatorForRawRfeport {


	    public static void main(String[] args) {
	        ConfigurableApplicationContext context = SpringApplication.run(SimulatorForRawRfeport.class,
	                args);
	        Test bean = context.getBean(Test.class);
	        bean.getFromDB();
	    }


	    public class Test {
	        
	        public  byte[] Hex2Byte(String str) {
	            return Hex2Byte(str, 0, str.length());
	        }

	        /**
	         * Convert Hex sub-string to byte array
	         * @param str - String for source
	         * @param offset - int for starting location of substring
	         * @param len - int for length of substring
	         * @return - byte[] for result
	         */
	        public  byte[] Hex2Byte(String str, int offset, int len)
	        {
	            byte[] bytes = new byte[len / 2];
	            for (int i = offset, j=0; i < (offset + bytes.length); i++, j++) {
	                bytes[j] = (byte) Integer.parseInt(str.substring(2*i,2*i + 2), 16);
	            }
	            return bytes;
	        }
	        public void getFromDB() {
	            try {
	                Instant start = Instant.now();
	                System.out.println(start);
                Instant end = Instant.now();
                Duration res = Duration.between(start, end);
                System.out.println("time gap in milli  " + res.toMillis());
                System.out.println("time gap in sec  " + res.getSeconds());
              //DatagramSocket socket = new DatagramSocket();
    			DatagramSocket socket = null;
    			byte[] buf = Hex2Byte("7d01001511500422690100b12a289a7f2270011bc32a289aa20725c94f052152d33f00a2ffd1060e3d5380000459001121007f04001f001a0000000001e40000000000000000000915000043ffb7000d0007e1000d0040310004030d003002068200e1021193d7424850485f4c5f544c565f56313100e21005003022104850020032");
    			InetAddress inetAddress = InetAddress.getByName("3.239.204.16");
    			DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, inetAddress, 15020);
    			if (socket == null) {
    				socket = new DatagramSocket();
    				socket.setReceiveBufferSize(100000000);

    				
    			}
    			for(int i=0;i<1;i++)
    			{
    				socket.send(datagramPacket);
    				System.out.println("datapacket sent on socket  ");
    				try
    				{
    					Thread.sleep(3);
    				}
    				catch(Exception e)
    				{
    					e.printStackTrace();
    				}
    			}
    		//	logger.info("Datagram packet sent to UDP Listener");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                   // selectStmt.close();
//                insertStmt.close();
                    //connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
	    }
}
