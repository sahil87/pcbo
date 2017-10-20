/* A simple applet.
Aim : To take in a mysql result from a servlet and display it in the applet
Bug - hangs sometimes when TperDiv is changed.
Author : Sahil Ahuja
*/
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
//import java.awt.image.BufferedImage.*;
//import java.awt.Image.*;
public class SimpleApplet extends java.applet.Applet implements Runnable, ChangeListener, ActionListener
    {
    	static final long serialVersionUID=1;
    	Thread t = null;
    	boolean stopFlag;
	String msg = ".";
	Manager m;
	Byte bigByteBox[]=new Byte[15000];
	int cFN,tFN;//current frame number,total frame nos
	int bigByteBoxFrequency,bigByteBoxFrequencyOld;//float causing a lot of problems !!!!!
//the applet interface : 
	ButtonGroup VperDiv;
	ButtonGroup TperDiv;		
	JToggleButton VperDiv5,VperDiv2,VperDiv1,VperDiv500m,VperDiv200m,VperDiv100m;
	JToggleButton TperDiv2,TperDiv5,TperDiv10,TperDiv20,TperDiv50,TperDiv100;
	JToggleButton togglePause,toggleTrigger,toggleVector,toggleGround;
	Panel panelRight, panelBottom,panelTimeBox,panelVoltBox,panelCenter,panelCenterDisplay,panelCenterDisplayBottom;
	GraphCanvas display;
	JSlider hScroll,vScroll;
	Label Vpp,Freq;
	int VppMid;
	float VppVal;
	long currentTimeNew,currentTimeOld;

	public void init() 
	{
		currentTimeOld=System.currentTimeMillis();
		currentTimeNew=currentTimeOld+2000;
		m = new Manager();
		m.codebase=getCodeBase();
		m.booleanStart=true;
		for(int i=0;i<15000;++i)		bigByteBox[i]=new Byte((byte) 20);
		VppMid=0;

//applet interface :
		setBackground(new Color(214,224,234));
		setLayout(new BorderLayout(20,0));
		panelRight=new Panel();
		panelBottom=new Panel();
		panelTimeBox=new Panel();
		panelVoltBox=new Panel();
		panelCenter=new Panel();
		panelCenterDisplay=new Panel();
		panelCenterDisplayBottom=new Panel();
		add(panelBottom,BorderLayout.PAGE_END);
		add(panelRight, BorderLayout.LINE_END);
		panelRight.setLayout(new BoxLayout(panelRight,BoxLayout.Y_AXIS));
		//panelRight.setBorder(BorderFactory.createLineBorder(Color.black));
		panelRight.setFont(new Font("Serif",Font.BOLD,18));
		panelBottom.setLayout(new FlowLayout(FlowLayout.LEADING));
		panelTimeBox.setLayout(new GridLayout(3,2,20,25));
		panelVoltBox.setLayout(new GridLayout(3,2,20,30));
		panelCenter.setLayout(new BorderLayout());
		panelCenter.setBackground(new Color(43,42,83));
       		//panelCenterDisplay.setLayout(new BoxLayout(panelCenterDisplay,BoxLayout.Y_AXIS));
		panelCenterDisplay.setLayout(new FlowLayout());
hScroll=new JSlider(JSlider.HORIZONTAL,-175,175,0);//dealt with in percentage
hScroll.setMajorTickSpacing(35);
hScroll.setMinorTickSpacing(7);
hScroll.setPaintTicks(true);
hScroll.addChangeListener(this);
//hScroll.setPaintLabels(true);
panelCenter.add(hScroll, BorderLayout.PAGE_END);
vScroll=new JSlider(JSlider.VERTICAL,-140,140,0);
vScroll.setMajorTickSpacing(35);
vScroll.setMinorTickSpacing(7);
vScroll.setPaintTicks(true); 
vScroll.addChangeListener(this);
//vScroll.setPaintLabels(true);
panelCenter.add(vScroll, BorderLayout.LINE_END);

display = new GraphCanvas();
Dimension displayDimension = new Dimension(351,281);
display.setSize(displayDimension);
//*****************************************************
display.queryInterval=1500;//from servlet (milliseconds)
//*****************************************************
//display.setBounds(0,0,350,280);
panelCenterDisplay.add(display);
panelCenterDisplayBottom.setForeground(new Color(87,189,39));
panelCenterDisplayBottom.add(new Label("Vpp (V): "));
Vpp=new Label("----",Label.CENTER);
panelCenterDisplayBottom.add(Vpp);
panelCenterDisplayBottom.add(new Label("Frequency (Hz): ",Label.CENTER));
Freq=new Label("------",Label.CENTER);
panelCenterDisplayBottom.add(Freq);
panelCenterDisplay.add(panelCenterDisplayBottom);
panelCenter.add(panelCenterDisplay,BorderLayout.CENTER);
	//	add(new Button("This is across the top."),BorderLayout.PAGE_START);
togglePause = new JToggleButton("Run/Stop",false);togglePause.addActionListener(this);
toggleTrigger = new JToggleButton("Auto Trigger",false);toggleTrigger.addActionListener(this);
toggleVector = new JToggleButton("Vector Mode",true);toggleVector.addActionListener(this);
toggleGround = new JToggleButton("Ground",false);toggleGround.addActionListener(this);
panelBottom.add(togglePause);
panelBottom.add(toggleTrigger);
panelBottom.add(toggleVector);
panelBottom.add(toggleGround);

VperDiv = new ButtonGroup();
VperDiv5 = new JToggleButton("5V", false);VperDiv5.setActionCommand("5000");VperDiv5.addActionListener(this);
VperDiv2 = new JToggleButton("2V", false);VperDiv2.setActionCommand("2000");VperDiv2.addActionListener(this);
VperDiv1 = new JToggleButton("1V", true); VperDiv1.setActionCommand("1000");VperDiv1.addActionListener(this);
VperDiv500m = new JToggleButton("500mV", false);VperDiv500m.setActionCommand("500");VperDiv500m.addActionListener(this);
VperDiv200m = new JToggleButton("200mV", false);VperDiv200m.setActionCommand("200");VperDiv200m.addActionListener(this);
VperDiv100m = new JToggleButton("100mV", false);VperDiv100m.setActionCommand("100");VperDiv100m.addActionListener(this);
VperDiv.add(VperDiv100m);
VperDiv.add(VperDiv200m);
VperDiv.add(VperDiv500m);
VperDiv.add(VperDiv1);
VperDiv.add(VperDiv2);
VperDiv.add(VperDiv5);

TperDiv = new ButtonGroup();
TperDiv100 = new JToggleButton("100ms", false);TperDiv100.setActionCommand("100");TperDiv100.addActionListener(this);
TperDiv50 = new JToggleButton("50ms", false);TperDiv50.setActionCommand("50");TperDiv50.addActionListener(this);
TperDiv20 = new JToggleButton("20ms", true);TperDiv20.setActionCommand("20");TperDiv20.addActionListener(this);
TperDiv10 = new JToggleButton("10ms", false);TperDiv10.setActionCommand("10");TperDiv10.addActionListener(this);
TperDiv5 = new JToggleButton("5ms", false);TperDiv5.setActionCommand("5");TperDiv5.addActionListener(this);
TperDiv2 = new JToggleButton("2ms", false);TperDiv2.setActionCommand("2");TperDiv2.addActionListener(this);
TperDiv.add(TperDiv2);
TperDiv.add(TperDiv5);
TperDiv.add(TperDiv10);
TperDiv.add(TperDiv20);
TperDiv.add(TperDiv50);
TperDiv.add(TperDiv100);

//Label labelTimeBase=new Label("TIME BASE",Label.CENTER);
Label labelTimePerDiv=new Label("Time/DIV",Label.CENTER);
//labelTimeBase.setSize(new Dimension(30,20));
Label labelVoltPerDiv=new Label("Volt/DIV",Label.CENTER);
labelVoltPerDiv.setSize(new Dimension(30,15));

//panelRight.add(labelTimeBase);
panelRight.add(labelTimePerDiv);
panelTimeBox.add(TperDiv2);
panelTimeBox.add(TperDiv5);
panelTimeBox.add(TperDiv10);
panelTimeBox.add(TperDiv20);
panelTimeBox.add(TperDiv50);
panelTimeBox.add(TperDiv100);
panelRight.add(panelTimeBox);

panelRight.add(labelVoltPerDiv);
panelVoltBox.add(VperDiv100m);
panelVoltBox.add(VperDiv200m);
panelVoltBox.add(VperDiv500m);
panelVoltBox.add(VperDiv1);
panelVoltBox.add(VperDiv2);
panelVoltBox.add(VperDiv5);
panelRight.add(panelVoltBox);

		add(panelCenter,BorderLayout.CENTER);
//repaint();
    	}
	public Insets getInsets() 
	{
		return new Insets(13, 10, 0, 20);
    	}
	//Start thread
    	public void start() 
	{
		t = new Thread(this,"SimpleAppletThread");
		stopFlag = false;
		t.start();
    	}
	//listen to slider :
	public void stateChanged(ChangeEvent evt)
	{
		display.Yoffset=(int) vScroll.getValue();
		display.Xoffset=(int) hScroll.getValue();
		if(togglePause.isSelected())	display.repaint();
   	}
	//listen to buttons : (VperDiv / TperDiv) 
	public void actionPerformed(ActionEvent e) 
	{
        	//Check everything -- much much simpler for now
		display.mVperDiv=Integer.parseInt(VperDiv.getSelection().getActionCommand());
		display.TperDiv=Integer.parseInt(TperDiv.getSelection().getActionCommand());
		display.toggleVector=toggleVector.isSelected();
		display.toggleGround=toggleGround.isSelected();
		float Xmultiplier=((float) (35*display.queryInterval))/((float) (display.bigByteBoxSize*display.TperDiv));
		int hScrollMax=(int) (Xmultiplier*display.PXperFrame);
		int a=hScrollMax%175;
		if(a!=0)	hScrollMax+= (175-a);
		hScroll.setMaximum(hScrollMax);
		hScroll.setMinimum(-hScrollMax);
		hScroll.setMajorTickSpacing(hScrollMax/5);
		hScroll.setMinorTickSpacing(hScrollMax/25);
		int vScrollMax=(int) ((128*1000)/Integer.parseInt(VperDiv.getSelection().getActionCommand()));
		a=vScrollMax%140;
		if(a!=0)	vScrollMax+= (140-a);
		vScroll.setMaximum(vScrollMax);
		vScroll.setMinimum(-vScrollMax);
		vScroll.setMajorTickSpacing(vScrollMax/5);
		vScroll.setMinorTickSpacing(vScrollMax/20);
		if(togglePause.isSelected()) {	m.booleanStart=true; display.repaint(); }
		//display.mVperDiv=Integer.parseInt(e.getActionCommand());
    	}
	// Entry point for the thread that runs the graph.
	public void run()
	{
	try {
		Thread.sleep(2000);
		int bigByteBoxNOZ,TperFrame;
		TperDiv20.doClick();VperDiv1.doClick();
		while(!stopFlag)
		{    
			bigByteBox=m.bigByteBox;
			bigByteBoxNOZ=m.bigByteBoxNOZ;
			m.notifyManager();
			display.bigByteBoxSize=(bigByteBox[0].intValue()+128)*256+(bigByteBox[1].intValue()+128);
			if(display.bigByteBoxSize>15000) display.bigByteBoxSize=15000;
			TperFrame=display.TperDiv*10;
			if(TperFrame>=display.queryInterval)
			{	TperDiv20.doClick();
				TperFrame=display.TperDiv*10;
			}
			tFN=display.queryInterval/TperFrame;
			display.PXperFrame=(display.bigByteBoxSize*TperFrame)/display.queryInterval;//display.bigByteBoxSize/tFN;
			if(display.PXperFrame>3000) 	display.PXperFrame=3000;
		//-----------------To adjust the slider scalings :	
		float Xmultiplier=((float) (35*display.queryInterval))/((float) (display.bigByteBoxSize*display.TperDiv));
		int hScrollMax=(int) (Xmultiplier*display.PXperFrame);
		int a=hScrollMax%175;
		if(a!=0)	hScrollMax+= (175-a);
		hScroll.setMaximum(hScrollMax);
		hScroll.setMinimum(-hScrollMax);
		hScroll.setMajorTickSpacing(hScrollMax/5);
		hScroll.setMinorTickSpacing(hScrollMax/25);
		int vScrollMax=(int) ((128*1000)/Integer.parseInt(VperDiv.getSelection().getActionCommand()));
		a=vScrollMax%140;
		if(a!=0)	vScrollMax+= (140-a);
		vScroll.setMaximum(vScrollMax);
		vScroll.setMinimum(-vScrollMax);
		vScroll.setMajorTickSpacing(vScrollMax/5);
		vScroll.setMinorTickSpacing(vScrollMax/20);
		//-------------------slider scaling over.
			
			long currentTimeNew=System.currentTimeMillis();
			bigByteBoxFrequency=bigByteBoxFrequencyOld;
			bigByteBoxFrequencyOld=(int) ( (500*bigByteBoxNOZ))/((int) (currentTimeNew-currentTimeOld) );
			currentTimeOld=currentTimeNew;
			if(bigByteBoxFrequencyOld==0)	bigByteBoxFrequency=0;
			else
			bigByteBoxFrequency=(bigByteBoxFrequency+bigByteBoxFrequencyOld)/2;
			
			VppVal=m.Vpp;
			VppMid=m.VppMid;
			// 1/T = 1/(total time/total zero in full time period/2) 
			//	= total zero/2 / total time/1000
			//	= 500 * total zero / total time  
			if((!toggleTrigger.isSelected())||(bigByteBoxFrequencyOld==0))
			for(cFN=0;cFN<tFN;++cFN)
			{
				Thread.sleep(TperFrame);
				for(int i=0;i<display.PXperFrame;++i) 
					display.yArr[i]=-((int) bigByteBox[cFN*display.PXperFrame + i + 2].intValue());
				display.repaint();
				if(togglePause.isSelected())
				{
					m.booleanStart=true;
					while(togglePause.isSelected())	Thread.sleep(50);	
					m.notifyManager(); 
					Thread.sleep(100);
				}
			}
			else   // trigger ON : 
			{
				boolean passed=false;int PXno=2,mod=0,missedFrames=0;
				while((PXno+display.PXperFrame)<display.bigByteBoxSize+2)
				{
					passed=false;
					if(bigByteBox[PXno].intValue()<0)	mod=-1;
					else if(bigByteBox[PXno].intValue()>0)	mod=1;
					else	mod=0;
					Thread.sleep((TperFrame*(display.PXperFrame+missedFrames))/display.PXperFrame);
					missedFrames=0;
					for(int i=0;(i<display.PXperFrame)&&(PXno+1<display.bigByteBoxSize);++PXno)
					{
						if(!passed)
						{
							if(bigByteBox[PXno].intValue()>VppMid) 
								{if(mod!=1)	if(bigByteBox[PXno+1].intValue()>VppMid)	passed=true;}
							else if(bigByteBox[PXno].intValue()<VppMid)
								mod=-1;
							else //if(bigByteBox[PXno].intValue()==0)
								{if(mod==-1)	{if(bigByteBox[PXno+1].intValue()>VppMid) passed=true;}	else	mod=0;}
							if(!passed)	{	missedFrames++;	continue;	}
						}
						display.yArr[i]=-((int) bigByteBox[PXno].intValue());
						++i;
					}
					if((PXno)>=display.bigByteBoxSize-1)	break;
					display.msg="Hi";
					display.repaint();//Vpp.setText("Hi");
					if(togglePause.isSelected())
					{
						m.booleanStart=true;
						while(togglePause.isSelected())	Thread.sleep(50);	
						m.notifyManager(); 
						Thread.sleep(100);
					}
				}
			}
			Vpp.setText(""+(Float.toString( (VppVal*5)/128) ).substring(0,3));
			Freq.setText(""+bigByteBoxFrequency);
		      	if(stopFlag)	break;
	    	}
	    }
	    catch(InterruptedException e) {}
	    catch (Exception e) 
	    {	msg="Exception: " + e.getMessage();
		repaint();
	    }  
     	}
   	 public void stop() 
	{                
		stopFlag = true;
		t = null;
    	}
}

class GraphCanvas extends Canvas 
{
	static final long serialVersionUID=2;
	private Color colorGraphCanvas,colorGraphGrid,colorGraphBoundary,colorGraphPlot,colorGraphMajorGrid;
	public int xArr[],yArr[];//for the repaint(); j is the frame no.
	public int Yoffset,Xoffset,mVperDiv,TperDiv,PXperFrame,bigByteBoxSize,queryInterval;
      	Image backbuffer=null;
	float Xmultiplier,Ymultiplier;
	boolean toggleVector,toggleGround;
	String msg;
   	//Graphics Backg;

	public GraphCanvas() 
	{
		colorGraphCanvas=new Color(43,69,126);
		colorGraphGrid=new Color(30,46,80);
		colorGraphMajorGrid=new Color(7,15,35);
		colorGraphBoundary=new Color(100,93,251);
		colorGraphPlot=new Color(150,249,46);
		setBackground(colorGraphCanvas);		
		xArr = new int[3000];
		yArr = new int[3000];
		for(int i=0;i<3000;i++){  xArr[i]=i; }
		Yoffset=0;Xoffset=0;mVperDiv=1000;TperDiv=20;PXperFrame=500;bigByteBoxSize=15000;
		backbuffer=createImage(getSize().width,getSize().height);
		toggleVector=true;toggleGround=false;
		msg="Hello";
		//Backg = backbuffer.getGraphics();
	}

  	public void paint(Graphics g) 
	{
		if(backbuffer==null)
		{	
			backbuffer = createImage(getSize().width,getSize().height);
		}//backbuffer need to recreated each time because of some bug... if not checked for null the applet hangs..
		Graphics backg=backbuffer.getGraphics();
		backg.setColor(colorGraphCanvas);
		backg.fillRect(0, 0, getSize().width, getSize().height);
		backg.setColor(colorGraphBoundary);
		backg.drawRect(0,0,350,280);
		backg.setColor(colorGraphGrid);
		for(int i=1;i<=7;++i) backg.drawLine(0,i*35,350,i*35);
		for(int i=1;i<=9;++i) backg.drawLine(i*35,0,i*35,280);
		backg.setColor(colorGraphMajorGrid);
		backg.drawLine(0,140,350,140);
		backg.drawLine(175,0,175,280);
		backg.setColor(colorGraphPlot);
		backg.translate(0+Xoffset,140-Yoffset);
		
		Xmultiplier=((float) (35*queryInterval))/((float) (bigByteBoxSize*TperDiv));//xArr[i]*350/PXperFrame
		Ymultiplier= ((float) (5000*35))/((float) 128*mVperDiv);	//(5000/128)*(35/mVperDiv)
		//g.drawString(msg,50,50);
		if(!toggleGround)
		{if(toggleVector)
		for(int i=0;i<PXperFrame-1;++i) 
			backg.drawLine((int) (xArr[i]*Xmultiplier),(int) (yArr[i]*Ymultiplier),(int) (xArr[i+1]*Xmultiplier),(int) (yArr[i+1]*Ymultiplier));
		else
		for(int i=0;i<PXperFrame;++i) 
			backg.drawLine((int) (xArr[i]*Xmultiplier),(int) (yArr[i]*Ymultiplier),(int) (xArr[i]*Xmultiplier),(int) (yArr[i]*Ymultiplier));
		backg.dispose();
		}
		else
			backg.drawLine(0,0,350,0);
		g.drawImage( backbuffer, 0, 0, this );//copying from buffer and not directly drawing on the canvas (to prevent flickering)
	}
	public void update(Graphics g) //used this update to prevent it from executing setColor as that is done in paint(g)
	{
          	paint(g);
       	}
/* Actual update : --
public void update (Graphics g)
{
  g.setColor(getBackground());
  g.fillRect(0, 0, getSize().width, getSize().height);
  g.setColor(getForeground());
  paint(g);
}
  */
} 
