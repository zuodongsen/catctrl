package com.zds.carctrl;

import java.io.*;
import java.net.*;

import android.R.string;
import android.location.Address;
import android.os.Handler;
import android.os.Message;

@SuppressWarnings("unused")
public class SocketService extends Thread
{

	private Handler handerFrom;

	private Socket socket;
	private InetSocketAddress ip;
	public boolean isConnected;

	public SocketService(String ipAdd, int port, Handler handler)
	{
		socket = new Socket();
		ip = new InetSocketAddress(ipAdd, port);
		this.handerFrom = handler;
	}

	@Override
	public void run()
	{
		try
		{
			socket.connect(ip);
			isConnected = true;
			if(socket.isConnected())
			{
				Message meg = new Message();
				meg.what = 10;
				meg.obj = "";
				if(handerFrom!=null)
				{
					handerFrom.sendMessage(meg);
				}
			}
		}
		catch (IOException e)
		{
			isConnected = false;
			e.printStackTrace();
		}
	}



	public String sendMess(String mess)
	{
		try
		{
			BufferedReader inner = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			out.write(mess);
			out.flush();
			return inner.readLine();
		}
		catch(Exception e)

		{
			return "";
		}
	}



}
