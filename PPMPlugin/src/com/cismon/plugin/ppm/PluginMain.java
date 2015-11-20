package com.cismon.plugin.ppm;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.cismon.core.CoreLoader;
import com.cismon.listener.CMDListener;
import com.cismon.plugins.PluginEgg;
import com.cismon.plugins.PluginManager;
import com.cismon.tts.Speaker;
import com.cismon.util.PropertiesManager;

public class PluginMain implements PluginEgg {
	private Properties pluPro = null; 
	private String ip = null;
	private String username = null;
	private String password = null;
	private int port = 22;
	public PluginMain()
	{
	}
	@Override
	public String getEggKey() {
		return "ppm-plugin";
	}
	@Override
	public boolean destoryEgg() {
		return true;
	}
	@Override
	public boolean initialize() {
		pluPro = (Properties) PluginManager.instance()
				.getPluginPros().get("ppm-plugin");
		ip = pluPro.getProperty("PPM_GW_IP");
		if(ip == null)
		{
			System.out.println("missing ip address of ppm testbed.");
		}
		username = pluPro.getProperty("SSH_USER");
		password = pluPro.getProperty("SSH_PWD");
		port = Integer.parseInt(pluPro.getProperty("SSH_PORT"));
		return true;
	}
	
	@Override
	public boolean accept(String cmd) {
		if(cmd.contains("p p m") || cmd.contains("cancel work"))
		{
			return true;
		}
		return false;
	}
	@Override
	public boolean processCMD(String cmd) {
		Speaker speaker = CoreLoader.getMainSpeaker();
		if(doubleCheck(cmd))
		{
			try
			{
				String command = null;
				if("p p m status".equals(cmd))
				{
				    command = "/opt/CSCOppm-gw/bin/ppm status";
				}
				else if("p p m usage".equals(cmd))
				{
					 command = "/opt/CSCOppm-gw/bin/ppm usage";
				}
				else if("p p m version".equals(cmd))
				{
					command = "/opt/CSCOppm-gw/bin/ppm version";
				}
				else if("p p m exception".equals(cmd))
				{
					command = "grep -i exception /opt/CSCOppm-gw/logs/*";
				}
				else if("p p m un install".equals(cmd))
				{
					command = "/opt/CSCOppm-gw/bin/ppm uninstall -n";
				}
				else if("p p m setup".equals(cmd))
				{
					Calendar cal = Calendar.getInstance();
					int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
					int month = cal.get(Calendar.MONTH);
					int year = cal.get(Calendar.YEAR);
					String dayStr = String.valueOf(dayOfMonth - 1);
					String monthStr = String.valueOf(month + 1);
					String  yearStr = String.valueOf(year);
					if(dayStr.length() == 1)
					{
						dayStr = "0" + dayStr;
					}
					if(monthStr.length() == 1)
					{
						monthStr = "0" + monthStr;
					}
					command = "/mount_83/.extracted/" 
							   + pluPro.getProperty("PPM_VERSION") 
							   +"-linux-" + yearStr 
							   + "-"+monthStr + dayStr +"-k9.old-0/setup.sh -n";
					System.out.println(command);
				}
				else if("cancel work".equals(cmd))
				{
					return false;
				}
				if(command != null)
				{
					speaker.speak("see console for details");
					SSHUtil.quickExec(command, ip, username, password, port);
				}
			} catch (Exception e) {
				e.printStackTrace();
				speaker.speak("Exception while execute " + cmd);
				speaker.speak(e.getMessage());
			}
			speaker.speak(cmd + " is done");
			return true;
		}
		else
		{
			speaker.speak(cmd + " is canceled");
			return false;
		}
	}
	
	private boolean doubleCheck(String cmd)
	{
		Speaker speaker = CoreLoader.getMainSpeaker();
		CMDListener listener = CoreLoader.getListener();
		speaker.speak("you said " + cmd);
		speaker.speak("are you sure I do it now");
		long curTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - curTime < 15000)
		{
			String confirm = listener.getMessage();
			if("yes".equals(confirm))
			{
				return true;
			}
			else if("no".equals(confirm))
			{
				return false;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		PropertiesManager.instance().setDevMode(true);
		CoreLoader.loadAll();
		Properties pro = PluginManager.getPluginPros().get("ppm-plugin");
		
	}
}
