package com.radiogramviewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.config.ShapeMaker;
import com.radiogramviewer.config.SlideDimensions;
import com.radiogramviewer.coroutine.CoroutineConstantRunner;
import com.radiogramviewer.coroutine.CoroutineRunner;
import com.radiogramviewer.graphics.Bar;
import com.radiogramviewer.graphics.DrawShape;
import com.radiogramviewer.graphics.SlideManager;
import com.radiogramviewer.graphics.TextManager;
import com.radiogramviewer.graphics.shaders.ShaderManager;
import com.radiogramviewer.logging.ClickFollower;
import com.radiogramviewer.logging.ScrollFollower;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;

public class SubViewer {

    public static final int none=0, dont=-1; //flags for when no slide is shown, and when scrollpos shouldn't be set
    //keep track of width and height, it's not always constant



    private static SpriteBatch slideBatch;
    private static TextManager textManager;
    private SlideManager slideManager;
    private ArrayList<SlideManager> slideManagers;
    private CoroutineRunner slideProcessor;
    private Controls controller;
    private Bar scroll;
    private CoroutineConstantRunner coroutineRunner;



    private static int SlideMode, SlideIndex, LastSlideMode; //state keeping variables
    private static boolean updateSlides; //triggering variable

    private Constants constants; //used to interface with javascript (or a dummy if compiled for PC)

    private static Config c; //the config file state
    public static Config getConfig(){return c;}

    private Texture clickImg, clickHighlightImg; //the circle to be drawn when the slide is clicked, and a highlight option
    private static ArrayList<ClickFollower> click; //all click trackers for each slide
    private static ClickFollower slideClick; //the current slide's click follower
    private ShapeMaker shapes;


    private static ArrayList<ScrollFollower> scrollTimes; //all scroll trackers

    public SubViewer(Constants constants){
        coroutineRunner=new CoroutineConstantRunner(constants);
        P.init(constants);
        this.constants=constants;




        try {
            //Gdx.graphics.setContinuousRendering(false); //this keeps the processor from constantly cycling on PC, does nothing in HTML

            if(click==null) {
                LastSlideMode = SlideMode = constants.getMode();
                SlideIndex = dont;
            }
            else{
                LastSlideMode=SlideMode;
            }

            //Get config file state
            c = new Config(constants);

            slideProcessor=new CoroutineRunner(c.global.yieldMillis,constants);

            //Get perferred dimensions (incase dynamically specified in JavaScript)
            int width = c.window.width;
            int height = c.window.height;

            Relay.initMain(constants,width,height);

            controller = new Controls(null, c);
            constants.addControls(controller);
            Gdx.input.setInputProcessor(controller);

            Gdx.graphics.setWindowedMode(width, height);
            slideBatch=new SpriteBatch();

            //Generate the images for indicating clicks and scroll bar
            shapes=new ShapeMaker(c);
            clickImg = DrawShape.ring(c.click.color, c.click.radius, c.click.thickness);
            clickHighlightImg = DrawShape.ring(c.click.highlightColor, c.click.highlightRadius, c.click.highlightThickness);
            scroll = new Bar(c);

            slideManagers = new ArrayList<SlideManager>(20);
            setupSlideManagers(c);

            if(textManager==null)
                textManager=new TextManager(c);
            else
                textManager=new TextManager(c, textManager);

            Relay.initLogs(click, scrollTimes, shapes, textManager);

            updateSlides = true;
            updateSlideMode();

            ShaderManager.init(slideBatch);



            Relay.changeLoadingState(Relay.loaded);

            if(slideProcessor.done()){
                constants.processingState(0,1);
                Relay.changeLoadingState(Relay.ready);
            }
            else{
                constants.processingState(slideProcessor.remainingTasks(),0);
            }
        }
        catch (Exception e){
            Relay.changeLoadingState(Relay.error);
            P.e(e.getMessage()+e);
        }

    }
    public void render(){
        if(slideProcessor.runOne()) {
            Gdx.graphics.requestRendering();
            if(slideProcessor.done()) {
                constants.processingState(0,1);
                Relay.changeLoadingState(Relay.ready);
            }
            return;
        }



        //clear the last rendered image
        Gdx.gl.glClearColor(.5f, .5f, .5f, 1);
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

            //note that I use individual batches here, using only one and changing the shader
            // causes a crash in html5, not sure why.

            ShaderManager.apply(slideBatch);
            slideBatch.begin();
            slideManager.draw(slideBatch);  //draw slide
            slideClick.drawImageShapes(slideBatch,currentSlide);
            slideBatch.end();

            ShaderManager.remove(slideBatch);
            slideBatch.begin();
            slideClick.drawUIshapes(slideBatch,currentSlide);
            slideClick.drawClicks(slideBatch, currentSlide);    //draw any relevant clicks
            slideClick.drawHighlights(slideBatch, currentSlide);    //draw any highlightedAreas
            if (totalSlides > 1 && !SlideManager.scrollLock)
                scroll.draw(slideBatch);                    //draw scroll bar, if more than 1 slide

            textManager.draw(slideBatch);

            slideBatch.end();
        }
        coroutineRunner.run(); //note that in pc this will only run when responding to input
    }
    public void dispose(){
        slideBatch.dispose();

        if(SlideMode!=none)
            SlideIndex=slideManagers.get(SlideMode-1).getSlide();

        scroll.dispose();
        for(SlideManager m : slideManagers)
            m.dispose();

        clickImg.dispose();
        clickHighlightImg.dispose();
        shapes.dispose();

    }

    public void disposeCompletely(){
        SlideManager.disposeCompletely();
        ShaderManager.dispose();
        textManager.dispose();
    }



    public static int getSlideMode(){
        return SlideMode;
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





    /**
     * Link slide managers to their respective scroll and click trackers
     * also load their images and partition them for viewing.
     * @param c the config file
     */
    private void setupSlideManagers(Config c){
        boolean newScrollTimes=true;
        ArrayList<ClickFollower> newclick=new ArrayList<ClickFollower>();

        if(scrollTimes==null) {
            scrollTimes = new ArrayList<ScrollFollower>();
            newScrollTimes=false;
        }
        SlideDimensions d=new SlideDimensions();
        int loadingState=0;
        Relay.changeLoadingState(loadingState);
        for(SlideDimensions.Node n : d.dims()){
            ScrollFollower sf;
            if(newScrollTimes) {
                sf = scrollTimes.get(loadingState);
            }
            else{
                sf = new ScrollFollower(c, n.total);
                scrollTimes.add(sf);
            }
            SlideManager s=new SlideManager(n,sf,c);
            slideProcessor.add(s);
            slideManagers.add(s);
            if(newScrollTimes)
                newclick.add(new ClickFollower(n.total,n.markClicks,clickImg,clickHighlightImg, c, shapes, click.get(loadingState)));
            else
                newclick.add(new ClickFollower(n.total,n.markClicks,clickImg,clickHighlightImg, c,shapes,null));

            Relay.changeLoadingState(++loadingState);
        }
        click=newclick;
        slideProcessor.invertWork();

        if(!newScrollTimes){
            if(SlideMode!=none)
                slideManager=slideManagers.get(SlideMode-1);
        }
    }

    /**
     * trigger method to change slide set (because the change is triggered through a static
     * method)
     */
    private void updateSlideMode(){
        updateSlides=false;

        //Record focus lost
        if(LastSlideMode!=SlideMode&&LastSlideMode!=none) {
            scrollTimes.get(LastSlideMode - 1).logClose(slideManager.getSlide());
        }

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
}
