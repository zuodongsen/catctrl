package com.zds.carctrl;

import java.io.PrintWriter;
import java.net.Socket;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class ButtonDownUpListener implements  OnTouchListener
{
	public Socket client;
	public PrintWriter clientWriter;
	

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if(clientWriter == null)
		{
			return false;
		}
		Button btn = (Button) v;
		if(event.getAction()== MotionEvent.ACTION_DOWN)
		{
			if(btn.getText().toString().contains("向前"))
			{
				clientWriter.print("FFforvardEE");
				clientWriter.flush();
			}
			else if(btn.getText().toString().contains("向后"))
			{
				clientWriter.print("FFbackEE");
				clientWriter.flush();
			}
			else if(btn.getText().toString().contains("向左"))
			{
				clientWriter.print("FFleftEE");
				clientWriter.flush();
			}
			else if(btn.getText().toString().contains("向右"))
			{
				clientWriter.print("FFrightEE");
				clientWriter.flush();
			}
			else if(btn.getText().toString().contains("停止"))
			{
				clientWriter.print("FFstopEE");
				clientWriter.flush();
			}
			btn.setBackgroundColor(R.color.black);
		}
		else if(event.getAction()== MotionEvent.ACTION_UP)
		{
			
				clientWriter.print("FFstopEE");
				clientWriter.flush();
			
		}
		
		
		
		return true;
	}

}
