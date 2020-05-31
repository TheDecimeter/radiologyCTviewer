package com.radiogramviewer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.config.SlideDimensions;
import com.radiogramviewer.graphics.Bar;
import com.radiogramviewer.graphics.DrawShape;
import com.radiogramviewer.graphics.SlideManager;
import com.radiogramviewer.graphics.shaders.ShaderManager;
import com.radiogramviewer.logging.ClickFollower;
import com.radiogramviewer.logging.ScrollFollower;
import com.radiogramviewer.logging.Timing;
import com.radiogramviewer.coroutine.CoroutineRunner;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;
import java.util.HashMap;

public class MainViewer extends ApplicationAdapter {

	public static final int none=0, dont=-1; //flags for when no slide is shown, and when scrollpos shouldn't be set
	private static int SlideMode, SlideIndex, LastSlideMode; //state keeping variables
	private static boolean updateSlides; //triggering variable
	 //keep track of width and height, it's not always constant



	private static SpriteBatch batch;
	private static SpriteBatch windowBatch;
	private Texture img;
	private SlideManager slideManager;
	private ArrayList<SlideManager> slideManagers;
	private CoroutineRunner slideProcessor;
	private Controls controller;
	private Bar scroll;


	private static Constants constants; //used to interface with javascript (or a dummy if compiled for PC)

	private static Config c; //the config file state
	public static Config getConfig(){return c;}

	private Texture clickImg, clickHighlightImg; //the circle to be drawn when the slide is clicked, and a highlight option
	private static ArrayList<ClickFollower> click; //all click trackers for each slide
	private static ClickFollower slideClick; //the current slide's click follower

	private static ArrayList<ScrollFollower> scrollTimes; //all scroll trackers
 	private ShaderManager shaderManager;
 	private Relay relay;

	public MainViewer(Constants constants){
		MainViewer.constants=constants;
		Timing.start(constants.getTime());
	}

	/**
	 * System specific printing. Use this instead of console printing.
	 * This also allows console printing to a function in HTML if desired.
	 * @param msg The string to print
	 * @param code The type of message, eg, error, warning, or debug (Use Constants.e, Constants.w, or Constants.d)
	 */
	public static void println(String msg, int code){
		constants.print(msg, code);
	}

	//Simple interfaces to forward input event from controls to Javascript


	//Called when program loads, performs initial setup
	@Override
	public void create () {
		try {
			Gdx.graphics.setContinuousRendering(false); //this keeps the processor from constantly cycling on PC, does nothing in HTML
			SlideIndex = dont;

			//Get config file state
			c = new Config(constants);

			slideProcessor=new CoroutineRunner(c.global.yieldMillis,constants);

			//Get perferred dimensions (incase dynamically specified in JavaScript)
			int width = c.window.width;
			int height = c.window.height;
			//println("using fixed pxl: width:" + width + " height:" + height, Constants.d);

			controller = new Controls(null, c);
			constants.addControls(controller);
			Gdx.input.setInputProcessor(controller);

			Gdx.graphics.setWindowedMode(width, height);
			batch = new SpriteBatch();
			windowBatch=new SpriteBatch();

			//Generate the images for indicating clicks and scroll bar
			clickImg = DrawShape.ring(c.click.color, c.click.radius, c.click.thickness);
			clickHighlightImg = DrawShape.ring(c.click.highlightColor, c.click.highlightRadius, c.click.highlightThickness);
			scroll = new Bar(c);

			slideManagers = new ArrayList<SlideManager>(20);
			setupSlideManagers(c);
			relay=new Relay(click,scrollTimes,constants,width,height);

			LastSlideMode = SlideMode = constants.getMode();
			updateSlides = true;
			updateSlideMode();

			shaderManager=new ShaderManager(batch);

			Relay.changeLoadingState(Relay.loaded);
		}
		catch (Exception e){
			Relay.changeLoadingState(Relay.error,constants);
			println(e.getMessage(),Constants.e);
		}

	}



	public static void setSlideMode(int slideMode, int index){
		if(index==dont && slideMode==SlideMode)
			return;
		if(slideMode>20)
			slideMode=20;
		if(slideMode<0)
			slideMode=0;
		updateSlides=true;
		LastSlideMode=SlideMode;
		SlideMode=slideMode;
		SlideIndex=index;
		Gdx.graphics.requestRendering();
	}




	public static int getSlideMode(){
		return SlideMode;
	}


	/**
	 * Link slide managers to their respective scroll and click trackers
	 * also load their images and partition them for viewing.
	 * @param c the config file
	 */
	private void setupSlideManagers(Config c){
		click=new ArrayList<ClickFollower>();
		scrollTimes=new ArrayList<ScrollFollower>();
		SlideDimensions d=new SlideDimensions();
		int loadingState=0;
		Relay.changeLoadingState(loadingState,constants);
		for(SlideDimensions.Node n : d.dims()){
			ScrollFollower sf= new ScrollFollower(c,n.total);
			scrollTimes.add(sf);
			SlideManager s=new SlideManager(n,sf,c);
			slideProcessor.add(s);
			slideManagers.add(s);
			click.add(new ClickFollower(n.total,c.click.radius,c.click.depth,n.markClicks,clickImg,clickHighlightImg, c));

			Relay.changeLoadingState(++loadingState,constants);
		}
		slideProcessor.invertWork();
	}

	/**
	 * Called to update the screen
	 */
	@Override
	public void render () {
        if(slideProcessor.runOne()) {
            Gdx.graphics.requestRendering();
            if(slideProcessor.done()) {
				Relay.changeLoadingState(Relay.ready);
            }
            return;
        }



		//clear the last rendered image
		Gdx.gl.glClearColor(1f, .5f, .5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//if changing to a new slide set, do that
		if(updateSlides){
			updateSlideMode();
		}

		//if there is a valid slide set active, draw it
		if(SlideMode!=none) {
			//set scroll bar to correct position
			int currentSlide=slideManager.getSlide();
			int totalSlides=slideManager.getTotal();
			scroll.setHeight(totalSlides - currentSlide, totalSlides);

			batch.begin();
			slideManager.draw(batch);  //draw slide
			batch.end();

			windowBatch.begin();
			slideClick.drawClicks(windowBatch,currentSlide);	//draw any relevant clicks
			slideClick.drawHighlights(windowBatch,currentSlide);	//draw any highlightedAreas
			if(totalSlides>1)
				scroll.draw(windowBatch);					//draw scroll bar, if more than 1 slide
			windowBatch.end();
		}
	}

	/**
	 * trigger method to change slide set (because the change is triggered through a static
	 * method)
	 */
	private void updateSlideMode(){
		updateSlides=false;

		//Record focus lost
		if(LastSlideMode!=SlideMode&&LastSlideMode!=none)
			scrollTimes.get(LastSlideMode-1).logClose(slideManager.getSlide());

		//if empty slide, don't do anything
		if(SlideMode==none){
			return;
		}

		//go to the next slide set

		slideManager=slideManagers.get(SlideMode-1);
		//change to specified index if necessary
		if(SlideIndex!=dont)
			slideManager.goToSlide(SlideIndex);
		//select new click follower
		slideClick=click.get(SlideMode-1);
		//update the input to controll the new slide set
		controller.setSlideManager(slideManager,slideClick);

		//Record focus gained
        if(LastSlideMode!=SlideMode)
		    scrollTimes.get(SlideMode-1).logOpen(slideManager.getSlide());
	}

	//release memory back into the wild
	@Override
	public void dispose () {
		batch.dispose();
		windowBatch.dispose();

		scroll.dispose();
		for(SlideManager m : slideManagers)
			m.dispose();

		if(slideManager!=null)
			shaderManager.dispose();

		clickImg.dispose();
		clickHighlightImg.dispose();
	}
	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			dispose();
		}
		finally
		{
			super.finalize();
		}
	}
}
