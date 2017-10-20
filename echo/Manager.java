//trying to remove byteBox and Queue and directly passing the array.

import java.io.*;
import java.net.*;
import java.lang.Byte;
/*A Queue for bytes*/
class Manager implements Runnable
{
	Thread t;
	boolean stopFlag;
	public Byte bigByteBox[]=new Byte[15000];
	public int bigByteBoxNOZ,bigByteBoxSize;//no of zeros per time period
	public URL codebase;
	public int Vpp,VppMid=0;
	//int limitnumber=9523;
	boolean booleanStart;

	Manager()
	{
		for(int i=0;i<15000;++i)		bigByteBox[i]=new Byte((byte) 20);
		t=new Thread(this,"ManagerThread");
		stopFlag = false;
		booleanStart=true;
		t.start();
	}
	
	synchronized public void run()
	{
		int mod=1,VppMin=0,VppMax=0;
		while(!stopFlag)
		{
			try
			{
				//limitstart++;
				URLConnection con = getServletConnection();
				InputStream instr = con.getInputStream();
				ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
				bigByteBox = (Byte[]) inputFromServlet.readObject();
				inputFromServlet.close();
				instr.close();
				bigByteBoxSize=(bigByteBox[0].intValue()+128)*256+(bigByteBox[1].intValue()+128);
				bigByteBoxNOZ=0;
				mod=1;
				VppMin=bigByteBox[2].intValue();VppMax=bigByteBox[2].intValue();
				for(int i=2;i<=bigByteBoxSize+1;i++)
				{	
					if(bigByteBox[i].intValue()<VppMin)	VppMin=bigByteBox[i].intValue();
					if(bigByteBox[i].intValue()>VppMax)	VppMax=bigByteBox[i].intValue();
				}
				Vpp=VppMax-VppMin;
				VppMid=(VppMax+VppMin)/2;
				for(int i=2;i<=bigByteBoxSize+1;i++)
				{	if(bigByteBox[i].intValue()>VppMid) 
						{if(mod!=1)
						{if(mod==-1)	{bigByteBoxNOZ++;mod=1;}
						/*else if(mod==0)		mod=1;*/}}
					else if(bigByteBox[i].intValue()<VppMid)
						{if(mod!=-1)
						{if(mod==1)		{bigByteBoxNOZ++;mod=-1;}
						/*else if(mod==0)		mod=-1;*/}}
					//else if(mod==0)
					//	bigByteBoxNOZ++;
				}
				wait();
			}
			catch(Exception ex)
			{
			}
		}
	}
	
	synchronized void notifyManager()
	{
		notify();
	}
	private URLConnection getServletConnection()
	throws MalformedURLException, IOException
	{
		URL urlServlet;
		if(booleanStart==true)
		{	
			urlServlet = new URL(codebase, "echo"+"?querypoint=start");
			booleanStart=false;
		}
		else	
		{	urlServlet = new URL(codebase, "echo"+"?querypoint=continue");
		}
		URLConnection con = urlServlet.openConnection();
		// configuration
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty(
			"Content-Type",
			"application/x-java-serialized-object");

		return con;
	}

}

