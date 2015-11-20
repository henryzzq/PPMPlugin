package com.cismon.plugin.ppm;

import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
public class SSHUtil {
	private static JSch jsch = new JSch();
	
	public static void quickExec(String command,
								 String ip, 
								 String username, 
								 String password, 
								 int port) 
			throws Exception
	{
		Session session = jsch.getSession(username, ip, port);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.setPassword(password);
		session.connect();
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();

		channel.connect();
		
		byte[] tmp=new byte[1024];
	      while(true){
	        while(in.available()>0){
	          int i=in.read(tmp, 0, 1024);
	          if(i<0)break;
	          System.out.print(new String(tmp, 0, i));
	        }
	        if(channel.isClosed()){
	          System.out.println("exit-status: "+channel.getExitStatus());
	          break;
	        }
	      }
	      channel.disconnect();
	      session.disconnect();
	}
	
}