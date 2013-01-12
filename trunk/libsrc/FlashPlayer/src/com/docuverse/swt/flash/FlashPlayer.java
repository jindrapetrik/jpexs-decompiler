/*
 * Copyright (c) 2002 Docuverse.  All Rights Reserved.
 */
package com.docuverse.swt.flash;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.*;

import com.docuverse.swt.ole.OleObject;

/**
 * Adobe Flash Player ActiveX wrapper for Eclipse SWT.
 * This control depends only on SWT and can be used independent
 * of FlashPlugin and FlashPlayerView.
 *
 * BUGS: ActiveX events are not firing.
 * 
 * @author Don Park
 * @see FlashPlayerView
 * @see FlashPlugin
 */
public class FlashPlayer extends Composite
{
	// Dispatch IDs
	final static int DISPID_READYSTATE = 0xfffffdf3;
	final static int DISPID_TOTALFRAMES = 0x0000007c;
	final static int DISPID_PLAYING = 0x0000007d;
	final static int DISPID_QUALITY = 0x00000069;
	final static int DISPID_SCALEMODE = 0x00000078;
	final static int DISPID_ALIGNMODE = 0x00000079;
	final static int DISPID_BACKGROUNDCOLOR = 0x0000007b;
	final static int DISPID_LOOP = 0x0000006a;
	final static int DISPID_MOVIE = 0x00000066;
	final static int DISPID_FRAMENUM = 0x0000006b;
	final static int DISPID_SETZOOMRECT = 0x0000006d;
	final static int DISPID_ZOOM = 0x00000076;
	final static int DISPID_PAN = 0x00000077;
	final static int DISPID_PLAY = 0x00000070;
	final static int DISPID_STOP = 0x00000071;
	final static int DISPID_BACK = 0x00000072;
	final static int DISPID_FORWARD = 0x00000073;
	final static int DISPID_REWIND = 0x00000074;
	final static int DISPID_STOPPLAY = 0x0000007e;
	final static int DISPID_GOTOFRAME = 0x0000007f;
	final static int DISPID_CURRENTFRAME = 0x00000080;
	final static int DISPID_ISPLAYING = 0x00000081;
	final static int DISPID_PERCENTLOADED = 0x00000082;
	final static int DISPID_FRAMELOADED = 0x00000083;
	final static int DISPID_FLASHVERSION = 0x00000084;
	final static int DISPID_WMODE = 0x00000085;
	final static int DISPID_SALIGN = 0x00000086;
	final static int DISPID_MENU = 0x00000087;
	final static int DISPID_BASE = 0x00000088;
	final static int DISPID_SCALE = 0x00000089;
	final static int DISPID_DEVICEFONT = 0x0000008a;
	final static int DISPID_EMBEDMOVIE = 0x0000008b;
	final static int DISPID_BGCOLOR = 0x0000008c;
	final static int DISPID_QUALITY2 = 0x0000008d;
	final static int DISPID_LOADMOVIE = 0x0000008e;
	final static int DISPID_TGOTOFRAME = 0x0000008f;
	final static int DISPID_TGOTOLABEL = 0x00000090;
	final static int DISPID_TCURRENTFRAME = 0x00000091;
	final static int DISPID_TCURRENTLABEL = 0x00000092;
	final static int DISPID_TPLAY = 0x00000093;
	final static int DISPID_TSTOPPLAY = 0x00000094;
	final static int DISPID_SETVARIABLE = 0x00000097;
	final static int DISPID_GETVARIABLE = 0x00000098;
	final static int DISPID_TSETPROPERTY = 0x00000099;
	final static int DISPID_TGETPROPERTY = 0x0000009a;
	final static int DISPID_TCALLFRAME = 0x0000009b;
	final static int DISPID_TCALLLABEL = 0x0000009c;
	final static int DISPID_TSETPROPERTYNUM = 0x0000009d;
	final static int DISPID_TGETPROPERTYNUM = 0x0000009e;
	final static int DISPID_SWREMOTE = 0x0000009f;
	final static int DISPID_FLASHVARS = 0x000000aa;

	final static int DISPID_ONREADYSTATECHANGE = 0xfffffd9f;
	final static int DISPID_ONPROGRESS = 0x000007a6;
	final static int DISPID_FSCOMMAND = 0x00000096;

	// Constants for ReadyState
	public static final int READYSTATE_UNINITIALIZED = 0;
	public static final int READYSTATE_LOADING = 1;
	public static final int READYSTATE_LOADED = 2;
	public static final int READYSTATE_INTERACTIVE = 3;
	public static final int READYSTATE_COMPLETE = 4;

	public static final int PROP_X_POS = 0;
	public static final int PROP_Y_POS = 1;
	public static final int PROP_X_SCALE = 2;
	public static final int PROP_Y_SCALE = 3;
	public static final int PROP_CURRENT_FRAME = 4;
	public static final int PROP_TOTAL_FRAMES = 5;
	public static final int PROP_ALPHA = 6;
	public static final int PROP_VISIBLE = 7;
	public static final int PROP_WIDTH = 8;
	public static final int PROP_HEIGHT = 9;
	public static final int PROP_ROTATE = 10;
	public static final int PROP_TARGET = 11;
	public static final int PROP_FRAMES_LOADED = 12;
	public static final int PROP_NAME = 13;
	public static final int PROP_DROP_TARGET = 14;
	public static final int PROP_URL = 15;

	public static final int PROP_HIGH_QUALITY = 16;
	public static final int PROP_FOCUS_RECT = 17;
	public static final int PROP_SOUND_BUF_TIME = 18;

	// Flash Player Control Events
//	public static final int OnReadyStateChange = DISPID_ONREADYSTATECHANGE;
//	public static final int OnProgress = DISPID_ONPROGRESS;
//	public static final int FSCommand = DISPID_FSCOMMAND;

	protected FlashPlayerListener flashListener;
	protected OleFrame oleFrame;
	protected OleControlSite oleControlSite;
	protected OleObject flashObject;
	protected boolean created = false;
	protected boolean activated = false;

	public FlashPlayer(Composite parent)
	{
		this(parent, SWT.NO_BACKGROUND, null);
	}

	public FlashPlayer(Composite parent, int style)
	{
		this(parent, style, null);
	}

	public FlashPlayer(Composite parent, int style, FlashPlayerListener listener)
	{
		super(parent, style | SWT.NO_BACKGROUND);

		flashListener = listener;

		this.setLayout(new FillLayout());

		oleFrame = new OleFrame(this, SWT.NONE);

		try
		{
			// Create an Automation object for access to extended capabilities
			oleControlSite =
				new OleControlSite(
					oleFrame,
					SWT.NONE,
					"ShockwaveFlash.ShockwaveFlash");
			OleAutomation oleAutomation = new OleAutomation(oleControlSite);
			flashObject = new OleObject(oleAutomation);
			created = true;
		}
		catch (SWTException ex)
		{
			Label label = new Label(oleFrame, SWT.BORDER);
			label.setText("Unable to create Flash Player");
			return;
		}

		// ReadyStateChange event 
		oleControlSite
			.addPropertyListener(DISPID_ONREADYSTATECHANGE, new OleListener()
		{
			public void handleEvent(OleEvent event)
			{
				if (event.detail == OLE.PROPERTY_CHANGING)
					return;
				if (flashListener != null)
					flashListener.onReadyStateChange(getReadyState());
			}
		});

		// OnProgress event
		oleControlSite.addEventListener(DISPID_ONPROGRESS, new OleListener()
		{
			public void handleEvent(OleEvent event)
			{
				Variant percentDone = event.arguments[0];
				if (percentDone != null)
				{
					if (flashListener != null)
						flashListener.onProgress(percentDone.getInt());
				}
			}
		});

		// FSCommand event
		oleControlSite.addEventListener(DISPID_FSCOMMAND, new OleListener()
		{
			public void handleEvent(OleEvent event)
			{
				Variant command = event.arguments[0];
				Variant args = event.arguments[1];
				if (command != null && args != null)
				{
					if (flashListener != null)
						flashListener.onFSCommand(
							command.getString(),
							args.getString());
				}
			}
		});
	}

	/**
	 * Cleanup
	 */
	public void dispose()
	{
		if (activated)
		{
			oleControlSite.deactivateInPlaceClient();
			activated = false;
		}

		if (flashObject != null)
			flashObject.dispose();

		flashObject = null;
		super.dispose();
	}

	public boolean activate()
	{
		if (!created)
			return false;
			
		// in place activate the ActiveX control		
		activated =
			(oleControlSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE) == OLE.S_OK);

		return activated;
	}

	public boolean isActivated()
	{
		return activated;
	}

	public OleFrame getOleFrame()
	{
		return oleFrame;
	}

	public OleControlSite getOleControlSite()
	{
		return oleControlSite;
	}

	public OleObject getOleObject()
	{
		return flashObject;
	}

	/*
	 * Interact with the Control via OLE Automation
	 * 
	 * Note: You can hard code the DISPIDs if you know them beforehand
	 *       this is of course the fastest way, but you increase coupling
	 *       to the control.
	 */

	/**
	 * Returns the current state of the control.
	 * 
	 * @return the current state of the control, one of:
	 *         READYSTATE_UNINITIALIZED;
	 *         READYSTATE_LOADING;
	 *         READYSTATE_LOADED;
	 *         READYSTATE_INTERACTIVE;
	 *         READYSTATE_COMPLETE.
	 */
	public int getReadyState()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_READYSTATE);
		else
			return READYSTATE_UNINITIALIZED;
	}

	/**
	 * Returns the total number of frames in the movie.
	 */
	public int getTotalFrames()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_TOTALFRAMES);
		else
			return 0;
	}

	public boolean getPlaying()
	{
		if (created)
			return flashObject.getBooleanProperty(DISPID_PLAYING);
		else
			return false;
	}

	public boolean setPlaying(boolean value)
	{
		if (created)
			return flashObject.setBooleanProperty(DISPID_PLAYING, value);
		else
			return false;
	}

	public int getQuality()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_QUALITY);
		else
			return 0;
	}

	public boolean setQuality(int value)
	{
		if (created)
			return flashObject.setIntegerProperty(DISPID_QUALITY, value);
		else
			return false;
	}

	public int getScaleMode()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_SCALEMODE);
		else
			return 0;
	}

	public boolean setScaleMode(int value)
	{
		if (created)
			return flashObject.setIntegerProperty(DISPID_SCALEMODE, value);
		else
			return false;
	}

	public int getAlignMode()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_ALIGNMODE);
		else
			return 0;
	}

	public boolean setAlignMode(int value)
	{
		if (created)
			return flashObject.setIntegerProperty(DISPID_ALIGNMODE, value);
		else
			return false;
	}

	public int getBackgroundColor()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_BACKGROUNDCOLOR);
		else
			return 0;
	}

	public boolean setBackgroundColor(int value)
	{
		if (created)
			return flashObject.setIntegerProperty(DISPID_BACKGROUNDCOLOR, value);
		else
			return false;
	}

	public boolean getLoop()
	{
		if (created)
			return flashObject.getBooleanProperty(DISPID_LOOP);
		else
			return false;
	}

	public boolean setLoop(boolean value)
	{
		if (created)
			return flashObject.setBooleanProperty(DISPID_LOOP, value);
		else
			return false;
	}

	public String getMovie()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_MOVIE);
		else
			return null;
	}

	public boolean setMovie(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_MOVIE, value);
		else
			return false;
	}

	public int getFrameNum()
	{
		if (created)
			return flashObject.getIntegerProperty(DISPID_FRAMENUM);
		else
			return 0;
	}

	public boolean setFrameNum(int value)
	{
		if (created)
			return flashObject.setIntegerProperty(DISPID_FRAMENUM, value);
		else
			return false;
	}

	public String getWMode()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_WMODE);
		else
			return null;
	}

	public boolean setWMode(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_WMODE, value);
		else
			return false;
	}

	public String getSAlign()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_SALIGN);
		else
			return null;
	}

	public boolean setSAlign(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_SALIGN, value);
		else
			return false;
	}

	public boolean getMenuEnable()
	{
		if (created)
			return flashObject.getBooleanProperty(DISPID_MENU);
		else
			return false;
	}

	public boolean setMenuEnable(boolean value)
	{
		if (created)
			return flashObject.setBooleanProperty(DISPID_MENU, value);
		else
			return false;
	}

	public String getBase()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_BASE);
		else
			return null;
	}

	public boolean setBase(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_BASE, value);
		else
			return false;
	}

	public String getScale()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_SCALE);
		else
			return null;
	}

	public boolean setScale(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_SCALE, value);
		else
			return false;
	}

	public boolean getDeviceFont()
	{
		if (created)
			return flashObject.getBooleanProperty(DISPID_DEVICEFONT);
		else
			return false;
	}

	public boolean setDeviceFont(boolean value)
	{
		if (created)
			return flashObject.setBooleanProperty(DISPID_DEVICEFONT, value);
		else
			return false;
	}

	public boolean getEmbedMovie()
	{
		if (created)
			return flashObject.getBooleanProperty(DISPID_EMBEDMOVIE);
		else
			return false;
	}

	public boolean setEmbedMovie(boolean value)
	{
		if (created)
			return flashObject.setBooleanProperty(DISPID_EMBEDMOVIE, value);
		else
			return false;
	}

	public String getBGColor()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_BGCOLOR);
		else
			return null;
	}

	public boolean setBGColor(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_BGCOLOR, value);
		else
			return false;
	}

	public String getQuality2()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_QUALITY2);
		else
			return null;
	}

	public boolean setQuality2(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_QUALITY2, value);
		else
			return false;
	}

	public String getSWRemote()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_SWREMOTE);
		else
			return null;
	}

	public boolean setSWRemote(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_SWREMOTE, value);
		else
			return false;
	}

	public String getFlashVars()
	{
		if (created)
			return flashObject.getStringProperty(DISPID_FLASHVARS);
		else
			return null;
	}

	public boolean setFlashVars(String value)
	{
		if (created)
			return flashObject.setStringProperty(DISPID_FLASHVARS, value);
		else
			return false;
	}

	// Flash Player Methods

	/**
	 * <p>
	 * Zooms in on a rectangular area of the movie.
	 * </p>
	 * <p>
	 * The units of the coordinates are in twips (1440 units per inch).
	 * To calculate a rectangle in Flash, set the ruler units to Points
	 * and multiply the coordinates by 20 to get twips.  There are 72
	 * points per inch.
	 * </p>
	 */
	public void setZoomRect(int left, int top, int right, int bottom)
	{
		if (!created)
			return;

		Variant[] args = new Variant[4];
		args[0] = new Variant(left);
		args[1] = new Variant(top);
		args[2] = new Variant(right);
		args[3] = new Variant(bottom);

		flashObject.invokeNoReply(DISPID_SETZOOMRECT, args);
	}

	/**
	 * <p>
	 * Zooms the view by a relative scale factor specified by percent.
	 * </p>
	 * <p>
	 * You cannot specify a reduction in the size of objects in the
	 * view when the current view is already 100%.
	 * </p>
	 * <pre>
	 * player.zoom(0);   // resets the view to 100%
	 * player.zoom(50);  // doubles the size of the objects in the view
	 * player.zoom(200); // reduces the size of objects in the view by one half
	 * </pre>
	 * @param factor zoom factor
	 */ 
	public void zoom(int factor)
	{
		if (!created)
			return;

		Variant[] args = new Variant[1];
		args[0] = new Variant(factor);

		flashObject.invokeNoReply(DISPID_ZOOM, args);
	}

	/**
	 * <p>
	 * Pans a zoomed-in movie to the coordinates specified by x and y.
	 * Pan does not pan beyond the boundaries of the zoomed-in movie.
	 * </p>
	 * 
	 * @param mode specifies whether the values for x and y are pixels (0)
	 * or percents of window size (1).
	 */
	public void pan(int x, int y, int mode)
	{
		if (!created)
			return;

		Variant[] args = new Variant[3];
		args[0] = new Variant(x);
		args[1] = new Variant(y);
		args[2] = new Variant(mode);

		flashObject.invokeNoReply(DISPID_PAN, args);
	}

	/**
	 * <p>
	 * Start playing the movie.
	 * </p>
	 */
	public void play()
	{
		if (!created)
			return;

		flashObject.invokeNoReply(DISPID_PLAY);
	}

	/**
	 * <p>
	 * Stops playing the movie.
	 * </p>
	 */
	public void stop()
	{
		if (!created)
			return;

		flashObject.invokeNoReply(DISPID_STOP);
	}

	/**
	 * <p>
	 * Rewinds the movie by a frame.
	 * </p>
	 */
	public void back()
	{
		if (!created)
			return;

		flashObject.invokeNoReply(DISPID_BACK);
	}

	/**
	 * <p>
	 * Advance the movie by a frame.
	 * </p>
	 */
	public void forward()
	{
		if (!created)
			return;

		flashObject.invokeNoReply(DISPID_FORWARD);
	}

	/**
	 * <p>
	 * Goes to the first frame.  Same as gotoFrame(0).
	 * </p>
	 */
	public void rewind()
	{
		if (!created)
			return;

		flashObject.invokeNoReply(DISPID_REWIND);
	}

	/**
	 * <p>
	 * Stop playing the movie.
	 * </p>
	 */
	public void stopPlay()
	{
		if (!created)
			return;

		flashObject.invokeNoReply(DISPID_STOPPLAY);
	}

	/**
	 * <p>
	 * Activates the frame number specified by frameNumber in the
	 * current movie. If the data for a requested frame is not yet
	 * available, the player goes to the last frame available and
	 * stops, causing unexpected results during playback.
	 * </p>
	 * <p>
	 * Use the getPercentLoaded() method to determine if enough of
	 * the movie is available to execute the GotoFrame() method.
	 * </p>
	 * <pre>
	 * flashPlayer.gotoFrame(0); // goto first frame
	 * </pre>
	 * @param frameNum zero-based frame number to go to.
	 */
	public void gotoFrame(int frameNum)
	{
		if (!created)
			return;

		Variant[] args = new Variant[1];
		args[0] = new Variant(frameNum);

		flashObject.invokeNoReply(DISPID_GOTOFRAME, args);
	}

	public int getCurrentFrame()
	{
		if (!created)
			return 0;

		Variant result = flashObject.invoke(DISPID_CURRENTFRAME);
		return result != null ? result.getInt() : 0;
	}

	/**
	 * <p>
	 * Returns true if the movie is currently playing.
	 * </p>
	 */
	public boolean isPlaying()
	{
		if (!created)
			return false;

		Variant result = flashObject.invoke(DISPID_ISPLAYING);
		return result != null ? result.getBoolean() : false;
	}

	/**
	 * <p>
	 * Returns the percent (0 to 100) of the Flash Player movie
	 * that has streamed into the browser so far.
	 * </p>
	 */
	public int getPercentLoaded()
	{
		if (!created)
			return 0;

		Variant result = flashObject.invoke(DISPID_PERCENTLOADED);
		return result != null ? result.getInt() : 0;
	}

	public boolean isFrameLoaded(int frameNum)
	{
		if (!created)
			return false;

		Variant[] args = new Variant[1];
		args[0] = new Variant(frameNum);

		Variant result = flashObject.invoke(DISPID_FRAMELOADED, args);
		return (result != null) ? result.getBoolean() : false;
	}

	public int getFlashVersion()
	{
		if (!created)
			return 0;

		Variant result = flashObject.invoke(DISPID_FLASHVERSION);
		return result != null ? result.getInt() : 0;
	}

	/**
	 * <p>
	 * Loads the movie identified by url to the layer specified by
	 * layerNumber.
	 * </p>
	 * @param layer target layer to play the movie in.
	 * @param url URL or file path to movie.
	 */
	public void loadMovie(int layer, String url)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(layer);
		args[1] = new Variant(url);

		flashObject.invokeNoReply(DISPID_LOADMOVIE, args);
	}

	/**
	 * <p>
	 * For the timeline indicated by target, goes to the frame number
	 * specified by frameNumber.
	 * </p>
	 */
	public void gotoFrame(String timeline, int frameNum)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(frameNum);

		flashObject.invokeNoReply(DISPID_TGOTOFRAME, args);
	}

	/**
	 * <p>
	 * For the timeline indicated by target, goes to the frame label
	 * specified by label.
	 * </p>
	 */
	public void gotoLabel(String timeline, String label)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(label);

		flashObject.invokeNoReply(DISPID_TGOTOLABEL, args);
	}

	/**
	 * <p>
	 * Returns the number of the current frame for the timeline
	 * specified by target.  Returned number is zero-based.
	 * </p>
	 * <pre>
	 * if (player.getCurrentFrame("/MovieClip") == 0)
	 *     ; // first frame
	 * </pre>
	 */
	public int getCurrentFrame(String timeline)
	{
		if (!created)
			return 0;

		Variant[] args = new Variant[1];
		args[0] = new Variant(timeline);

		Variant result = flashObject.invoke(DISPID_TCURRENTFRAME, args);
		return (result != null) ? result.getInt() : 0;
	}

	/**
	 * <p>
	 * Returns the label of the current frame of the timeline specified
	 * by target.  If there is no current frame label, an empty string
	 * is returned.
	 * </p>
	 */
	public String getCurrentLabel(String timeline)
	{
		if (!created)
			return null;

		Variant[] args = new Variant[1];
		args[0] = new Variant(timeline);

		Variant result = flashObject.invoke(DISPID_TCURRENTLABEL, args);
		return (result != null) ? result.getString() : "";
	}

	/**
	 * <p>
	 * Plays the timeline specified by target.
	 * </p>
	 */
	public void play(String timeline)
	{
		if (!created)
			return;

		Variant[] args = new Variant[1];
		args[0] = new Variant(timeline);

		flashObject.invokeNoReply(DISPID_TPLAY, args);
	}

	/**
	 * <p>
	 * Plays the timeline specified by target.
	 * </p>
	 */
	public void stopPlay(String timeline)
	{
		if (!created)
			return;

		Variant[] args = new Variant[1];
		args[0] = new Variant(timeline);

		flashObject.invokeNoReply(DISPID_TSTOPPLAY, args);
	}

	/**
	 * Sets the value of the Flash variable specified by name to
	 * the value specified by value.
	 */
	public void setVariable(String name, String value)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(name);
		args[1] = new Variant(value);

		flashObject.invokeNoReply(DISPID_SETVARIABLE, args);
	}

	/**
	 * Returns the value of the Flash variable specified by varName.
	 * <pre>
	 * String value = flashPlayer.getVariable("myvar");
	 * </pre>
	 * @return null if the variable does not exist, string value otherwise.
	 */
	public String getVariable(String name)
	{
		if (!created)
			return null;

		Variant[] args = new Variant[1];
		args[0] = new Variant(name);

		Variant result = flashObject.invoke(DISPID_GETVARIABLE, args);
		return (result != null) ? result.getString() : "";
	}

	/**
	 * <p>
	 * For the timeline indicated by target, set string value of
	 * a property.
	 * </p>
	 * @param property one of following:
	 * PROP_X_POS, PROP_Y_POS, PROP_X_SCALE, PROP_Y_SCALE,
	 * PROP_ALPHA, PROP_VISIBLE,
	 * PROP_ROTATE, PROP_NAME,
	 * PROP_HIGH_QUALITY, PROP_FOCUS_RECT, PROP_SOUND_BUF_TIME
	 */
	public void setProperty(String timeline, int property, String value)
	{
		if (!created)
			return;

		Variant[] args = new Variant[3];
		args[0] = new Variant(timeline);
		args[1] = new Variant(property);
		args[2] = new Variant(value);

		flashObject.invokeNoReply(DISPID_TSETPROPERTY, args);
	}

	/**
	 * <p>
	 * For the timeline indicated by target, returns string value of
	 * a property.
	 * </p>
	 * @param property one of following:
	 * PROP_X_POS, PROP_Y_POS, PROP_X_SCALE, PROP_Y_SCALE,
	 * PROP_CURRENT_FRAME, PROP_TOTAL_FRAMES,
	 * PROP_ALPHA, PROP_VISIBLE,
	 * PROP_WIDTH, PROP_HEIGHT, PROP_ROTATE,
	 * PROP_TARGET, PROP_FRAMES_LOADED,
	 * PROP_NAME,  PROP_DROP_TARGET, PROP_URL,
	 * PROP_HIGH_QUALITY, PROP_FOCUS_RECT, PROP_SOUND_BUF_TIME
	 */
	public String getProperty(String timeline, int property)
	{
		if (!created)
			return null;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(property);

		Variant result = flashObject.invoke(DISPID_TGETPROPERTY, args);
		return (result != null) ? result.getString() : "";
	}

	/**
	 * <p>
	 * In the timeline specified by target, executes the action in the
	 * frame specified by frameNum.
	 * </p>
	 * <pre>
	 * // call action in fifth frame of the main timeline 
	 * player.callFrame("/", 4);
	 * </pre>
	 */
	public void callFrame(String timeline, int frameNum)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(frameNum);

		flashObject.invokeNoReply(DISPID_TCALLFRAME, args);
	}

	/**
	 * <p>
	 * In the Timeline indicated by target, executes the action in the
	 * frame specified by the label frame label.
	 * </p>
	 */
	public void callLabel(String timeline, String label)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(label);

		flashObject.invokeNoReply(DISPID_TCALLLABEL, args);
	}

	/**
	 *
	 * @param property one of following:
	 * PROP_X_POS, PROP_Y_POS, PROP_X_SCALE, PROP_Y_SCALE,
	 * PROP_ALPHA, PROP_VISIBLE,
	 * PROP_ROTATE, PROP_NAME,
	 * PROP_HIGH_QUALITY, PROP_FOCUS_RECT, PROP_SOUND_BUF_TIME
	 */
	public void setPropertyNum(String timeline, int property, double value)
	{
		if (!created)
			return;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(property);
		args[2] = new Variant((float) value);

		flashObject.invokeNoReply(DISPID_TSETPROPERTYNUM, args);
	}

	/**
	 * <p>
	 * For the timeline indicated by target, returns numberic value of
	 * a property.
	 * </p>
	 * @param property one of following:
	 * PROP_X_POS, PROP_Y_POS, PROP_X_SCALE, PROP_Y_SCALE,
	 * PROP_CURRENT_FRAME, PROP_TOTAL_FRAMES,
	 * PROP_ALPHA, PROP_VISIBLE,
	 * PROP_WIDTH, PROP_HEIGHT, PROP_ROTATE,
	 * PROP_TARGET, PROP_FRAMES_LOADED,
	 * PROP_NAME,  PROP_DROP_TARGET, PROP_URL,
	 * PROP_HIGH_QUALITY, PROP_FOCUS_RECT, PROP_SOUND_BUF_TIME
	 */
	public double getPropertyNum(String timeline, int property)
	{
		if (!created)
			return 0.0;

		Variant[] args = new Variant[2];
		args[0] = new Variant(timeline);
		args[1] = new Variant(property);

		Variant result = flashObject.invoke(DISPID_TGETPROPERTYNUM, args);
		return (result != null) ? result.getFloat() : 0.0;
	}
}
