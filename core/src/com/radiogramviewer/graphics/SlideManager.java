package com.radiogramviewer.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.config.SlideDimensions;
import com.radiogramviewer.logging.ScrollFollower;
import com.radiogramviewer.coroutine.Coroutine;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Slide set drawer and navigator, it ain't simple
 */
public class SlideManager implements Disposable, Coroutine{

    public final static int up=1, down=-1, stay=0;

    private int dir;
    private float holdTime=0, holdFor;
    private boolean buttons, discardDelta=false;
    private Drawer drawer;

    private static HashMap<String,NormalDrawer> textureCache;

    public SlideManager(SlideDimensions.Node node, ScrollFollower scrollLog, Config c){

        dir=stay;
        holdFor=c.input.holdTime;
        buttons=c.input.wasd||c.input.arrow;

        try{
            //try loading the image, if it can't be found, but wasn't mentioned in the slide Dimensions
            // then it is expected to not be important.
            // otherwise, throw an exception.
            if(isCached(node.file)){
                NormalDrawer d=new NormalDrawer(scrollLog,c);
                drawer=d;
                requestCache(node.file,d);
                MainViewer.println("using cached slides: "+node.file, Constants.d);
            }
            else {
                if(Gdx.files.internal(node.file).exists()){

                    NormalDrawer d=new NormalDrawer(node, scrollLog, c);
                    drawer=d;
                    cache(node.file,d);
                }
                else{
                    if(node.necessary) {
                        drawer=new DummyDrawer();
                        throw new FailToLoadException("Failed to load necessary image "+node.file);
                    }
                    else
                        drawer=new DummyDrawer();
                }
            }
        }
        catch (Exception e){
            if(node.necessary) {
                drawer=new DummyDrawer();
                throw new FailToLoadException("Failed to load necessary image " + e.getMessage());
            }
            else
                drawer=new DummyDrawer();
        }


    }


    private boolean isCached(String filename){
        if(textureCache==null)
            return false;
        return textureCache.containsKey(filename);
    }

    private void cache(String filename, NormalDrawer s){
        if(textureCache==null)
            textureCache=new HashMap<String, NormalDrawer>();
        textureCache.put(filename,s);
    }

    private void requestCache(String filename, NormalDrawer thisDrawer){
        NormalDrawer d=textureCache.get(filename);
        if(d.workAvailable())
            d.requestCache(thisDrawer);
        else{
            thisDrawer.texture=d.texture;
            thisDrawer.img=d.img;
        }

    }


    //used for calculating how to fit the slideset into the GPU
    private int largestDimension(Pixmap p){
        int h=p.getHeight();
        int w=p.getWidth();
        if(h>w)
            return h;
        else return w;
    }


    /**
     * @return the current slide index
     */
    public int getSlide(){
        return drawer.getSlide();
    }

    /**
     * @return get the total slides in the set
     */
    public int getTotal(){
        return drawer.getTotal();
    }

    /**
     * Go to a specific slide
     * @param slide the slide to go to
     */
    public void goToSlide(int slide){
        drawer.goToSlide(slide);
    }


    //simple clamp function, to make sure I don't go out of bounds
    private int clamp(int val, int min, int max){
        if(val > max)
            return max;
        if(val<min)
            return min;
        return val;
    }

    /**
     * Draw the current slide
     * @param batch used for efficiency
     * @return unused
     */
    public boolean draw(SpriteBatch batch) {
        buttonCheck();
        return drawer.draw(batch);
    }

    /**
     * manage button timings for scrolling purposes
     */
    private void buttonCheck(){
        if(!buttons)
            return;

        if(dir!=stay) {
            if(!discardDelta)
                holdTime += Gdx.graphics.getDeltaTime();
            discardDelta=false;
            if (holdTime > holdFor) {
                int d=(int)(holdTime/holdFor);
                holdTime-=holdFor*d;
                if(dir==up)
                    advanceSlide(-d);
                else
                    advanceSlide(d);
            }
        }
    }

    /**
     * Get button movement updates from user input
     * @param dir
     */
    public void move(int dir){
        if(this.dir==dir)
            return;

        Gdx.graphics.setContinuousRendering(dir!=stay);
        if(this.dir==stay){
            discardDelta=true;
            holdTime=0;
        }
        this.dir=dir;
    }

    /**
     * scroll to another slide
     * @param howMuch direction and magnitude of scroll
     */
    public void advanceSlide(int howMuch){
        drawer.advanceSlide(howMuch);
    }

    /**
     * release memory
     */
    @Override
    public void dispose() {
        textureCache=null;
        drawer.dispose();
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


    //simple helper for dividing up a slide set if it can fit in the GPU as a single texture.
    static TextureRegion[][] parse(Texture image, int rows, int columns){

        return TextureRegion.split(image,image.getWidth()/columns,(image.getHeight()/rows));

    }

    @Override
    public boolean workAvailable() {
        return drawer.workAvailable();
    }

    @Override
    public float progress() {
        return drawer.progress();
    }

    @Override
    public void run() {
        drawer.run();
    }

    private class FailToLoadException extends RuntimeException{ FailToLoadException(String s){super(s);}}

    private interface Drawer  extends Disposable, Coroutine{
        boolean draw(SpriteBatch batch);
        void goToSlide(int slide);
        void advanceSlide(int howMuch);
        int getSlide();
        int getTotal();
    }


    /**
     * The Drawer for a usable slide set
     */
    private class NormalDrawer implements Drawer{
        final int overscan;
        int x, y, ih,iw,it;
        private ScrollFollower scrollLog;
        TextureRegion [][] img;
        private Texture [] texture;
        private Coroutine co=null;

        /**
         * Partial Constructor which doesn't initialize the textures themselves because it knows
         * they are being processed by another manager and it can get them from the cache when it
         * is done.
         * @param scrollLog the log to record scrolling
         * @param c the config object
         */
        public NormalDrawer(ScrollFollower scrollLog, Config c){
            y=x=0;
            this.overscan=c.global.overscan;
            this.scrollLog=scrollLog;
        }

        /**
         * Create a full drawer Note that this prepares a coroutine to acutally process the information, it is not ready
         * to be used until that coroutine runs to completion.
         * @param node the node containing all the slide specific information (how many slides high, wide, total, etc)
         * @param scrollLog the log to report all scrolling to
         * @param c the app config with basic constants
         */
        public NormalDrawer(SlideDimensions.Node node, ScrollFollower scrollLog, Config c){
            ih= node.height;
            iw= node.width;
            it= node.total;
            y=x=0;

            overscan=c.global.overscan;

            this.scrollLog=scrollLog;
            //The slideset comes in through the CPU first, so that we can see how to fit it in the GPU

            co=new Coroutine(node,c);

        }

        /**
         * how many slides can the GPU hold in one texture (in one dimension)
         * @param orig the original image with all the slides
         * @param maxTextureSize how large of a texture the GPU can hold
         * @param w how many slides wide the original image is
         * @param h how many slides tall the original image is
         * @return the number of slides across one dimension the GPU can hold (this can be 0)
         */
        private int canFit(Pixmap orig, int maxTextureSize, int w, int h){
            int eachW=orig.getWidth()/w;
            int eachH=orig.getHeight()/h;
            if(eachW>eachH){
                return maxTextureSize/eachW;
            }
            else{
                return maxTextureSize/eachH;
            }
        }

        /**
         * Get the new textures holding the image, and the new slide locations remapped across
         *  the new textures.
         * @param orig the original image with all the slides
         * @param maxTextureSize how large of a texture the GPU can hold
         * @param w how many slides wide the original image is
         * @param h how many slides tall the original image is
         * @param totalSlides how many slides exist on the original image
         * @param size how many slides we are going to fit in one dimension of the new textures (can't be 0)
         * @return The new textures holding the image, and the new locations of each slide
         */
        private void distribute(SlideDimensions.Node node, boolean scaled, Pixmap orig, int maxTextureSize, int w, int h, int totalSlides,int size){
            int textureCount=totalSlides/(size*size); //see how many textures are needed to hold the slideset

            //co=new Coroutine(node,cscaled,textureCount,h,w,totalSlides,size,maxTextureSize,orig);

            MainViewer.println("individual dimensions: w"+co.eachW+", h"+co.eachH+" s"+size+" textures used"+co.rt.length, Constants.w);

            co.textureIndex=0;
            co.i=0;
//            while(co!=null){
//                run();
//            }



            //return co.r;
        }

        @Override
        public boolean workAvailable() {
            return co!=null;
        }

        @Override
        public float progress() {
            if(co==null)
                return 1;
            return (float)(co.i + co.j)/co.totalSlides;
        }

        @Override
        public void run() {
            switch(co.stage) {
                case 0:
                    co.coStage0Init();
                    if(co==null)
                        return;
                    co.stage++;
                    break;
                case 1: //make a new smaller pixmap to hold some of the slides
                    co.p = new Pixmap(co.size * co.eachW, co.size * co.eachH, co.orig.getFormat());
                    co.pc.reset();
                    co.j = 0;
                    co.stage++;
                break;
                case 2: //move whatever slides that will fit onto the new pixmap
                    if (co.j < co.incr && co.i + co.j < co.totalSlides) {
                        co.coStage1MapFromOldToNew(); //take a slide from the old pixmap, and put it on the new one.
                        ++co.j;
                        return;
                    }
                    co.stage++;
                break;
                case 3: //set up the new texture regions for the smaller texture
                    co.coStage2MapNewToGrid();
                    co.p.dispose();
                    co.i += co.incr;
                    if(co.i<co.totalSlides)
                        co.stage=1;
                    else
                        co.stage=4;
                break;
                case 4: //set values and get rid of coroutine.
                    co.coStage3Finalize();
                    break;
            }
        }


        /**
         * Divides up the work of processing an image so that it can be more easily
         * interrupted... oh yield return, wherefore art thou yield return
         */
        private class Coroutine{
            Texture[] rt;
            TextureRegion [][] rr;
            int textureIndex,i,j,stage=0;
            int eachW,eachH,size,maxTextureSize,totalSlides,incr;
            Coord oc1,oc2,pc;
            Pixmap p,orig;
            Texture t;
            boolean scaled;
            final SlideDimensions.Node node;

            Config c;

            ArrayList<NormalDrawer> requests=new ArrayList<NormalDrawer>();

            public Coroutine(SlideDimensions.Node node, Config c){
                rr=new TextureRegion[ih][iw];


                //get individual slide dimensions
                //get coordinate counters
                oc1=new Coord(iw); //oc is the coordinates for the original image's grid
                oc2=new Coord(iw);

                this.node=node;
                this.c=c;
                textureIndex=0;
            }

            private void coStage0Init(){
                orig = new Pixmap(Gdx.files.internal(node.file));
                maxTextureSize = c.global.gpuMaxTextureSize;


                if(largestDimension(orig)<maxTextureSize) { //if texture already fits in renderable space on GPU
                    texture=new Texture[1];
                    texture[0] = new Texture(orig);
                    img = parse(texture[0], ih, iw);
                    wrapUp();
                }
                else if(c.global.downscaleTexture){ //if it is desirable to scale texture to make it fit
                    MainViewer.println("scaling "+node.file+" to fit in memory", Constants.w);
                    orig = scale(orig, maxTextureSize, iw, ih);
                    texture=new Texture[1];
                    texture[0] = new Texture(orig);
                    img = parse(texture[0], ih, iw);
                    Relay.addToTextureInfoPacket(node.file+",scaled,gpuMax:"+maxTextureSize+",x:"+texture[0].getWidth()+",y:"+texture[0].getHeight());
                    wrapUp();
                }
                else{ //if dividing the texture across multiple textures is desirable for making it fit
                    size=canFit(orig,maxTextureSize,iw,ih);
                    scaled=false;
                    if(size<=0) {
                        size=1;
                        scaled=true;
                        MainViewer.println("dividing up and scaling"+node.file+" to fit in memory", Constants.w);

                    }
                    else
                        MainViewer.println("dividing up "+node.file+" to fit in memory", Constants.w);
                    //distribute(node,scaled,orig,maxTextureSize,iw,ih,it,size);
//                texture=d.texture;
//                img=d.img;

                    totalSlides=it;
                    int textureCount=totalSlides/(size*size);
                    rt=new Texture[textureCount+1];
                    eachW=orig.getWidth()/iw;
                    eachH=orig.getHeight()/ih;



                    this.incr=size*size;
                    pc=new Coord(size); //pc is the coordinates for the smaller image with fewer slides (that can fit in the GPU)
                }
            }


            private void coStage1MapFromOldToNew(){
                p.drawPixmap(orig,oc1.x*eachW,oc1.y*eachH,eachW,eachH,pc.x*eachW,pc.y*eachH,eachW,eachH);
                oc1.advance();
                pc.advance();
            }


            private void coStage2MapNewToGrid(){
                //save the new pixmap to a texture, scale it if necessary.
                p=scale(p, maxTextureSize, size, size);
                t=new Texture(p);
                rt[textureIndex++]=t;

                //get newly scaled dimensions for accurate texture regions
                int H=t.getHeight()/size;
                int W=t.getWidth()/size;

                //map the new texture's slides onto the original image's grid.
                pc.reset();
                for(int j=0; j<incr && i+j<totalSlides; ++j){
                    rr[oc2.y][oc2.x]=new TextureRegion(t,pc.x*W,pc.y*H,W,H);
                    oc2.advance();
                    pc.advance();
                }
            }

            private void coStage3Finalize(){
                img=rr;
                texture=rt;

                if(scaled)
                    Relay.addToTextureInfoPacket(node.file+",divided&scaled,gpuMax:"+maxTextureSize+",x:"+texture[0].getWidth()+",y:"+texture[0].getHeight()+",count:"+texture.length);
                else
                    Relay.addToTextureInfoPacket(node.file+",divided,gpuMax:"+maxTextureSize+",x:"+texture[0].getWidth()+",y:"+texture[0].getHeight()+",count:"+texture.length);
                wrapUp();
            }

            private void wrapUp(){
                for(NormalDrawer d : requests){
                    d.img=img;
                    d.texture=texture;
                    d.ih=ih;
                    d.it=it;
                    d.iw=iw;
                }
                orig.dispose();
                co=null;
            }


            /**
             * incriment coordinates by emulating a grid of width 'r'
             */
            private class Coord{
                int x, y;
                private int r;

                Coord(int width){
                    r=width;
                    x=0;
                    y=0;
                }
                void advance(){
                    x++;
                    if(x>=r)
                    {
                        x=0;
                        y++;
                    }
                }
                void reset(){
                    x=0;
                    y=0;
                }
            }
        }



        /**
         * Scale the original image to fit in <code>maxTextureSize</code>. Note, this does not
         * necesicarily reproduce the image with maxTexture size, it instead makes sure each dimension
         * is equally divisible by the width and height of each image.
         * IMPORTANT: this will destroy the original pixmap.
         * @param orig the original image with all the slides
         * @param maxTextureSize how large of a texture the GPU can hold
         * @param w how many slides wide the original image is
         * @param h how many slides tall the original image is
         * @return a new Pixmap that fits within <code>maxTextureSize</code>
         */
        private Pixmap scale(Pixmap orig, int maxTextureSize, int w, int h){
            if(largestDimension(orig)<=maxTextureSize)
                return orig;


            int newW, newH;
            float scale;
            if(orig.getWidth()>orig.getHeight()){
                newW=evenDiv(w,maxTextureSize);
                scale=((float)newW)/orig.getWidth();
                newH=(int)((orig.getHeight()/h)*scale)*h;

            }
            else{
                newH=evenDiv(h,maxTextureSize);
                scale=((float)newH)/orig.getHeight();
                newW=(int)((orig.getWidth()/w)*scale)*w;

            }



            Pixmap ret = new Pixmap(newW, newH, orig.getFormat());
            ret.setFilter(Pixmap.Filter.BiLinear);
            ret.drawPixmap(orig, 0, 0, orig.getWidth(), orig.getHeight(), 0, 0, ret.getWidth(), ret.getHeight());

            orig.dispose();
            return ret;
        }

        //maximize the "space" we can use while still being evenly divisible by "num"
        private int evenDiv(int num, int space){
            int x=space/num;
            return x*num;
        }

        @Override
        public boolean draw(SpriteBatch batch) {
            //MainViewer.println("draw width:"+MainViewer.getWidth()+" draw height:"+MainViewer.getHeight(),Constants.d);
            batch.draw(img[y][x],-overscan,-overscan,Relay.getWidth()+overscan,Relay.getHeight()+overscan);
            return true;
        }

        @Override
        public void goToSlide(int slide){
            slide=clamp(slide,0,it-1);
            int nx=getX(slide);
            int ny=getY(slide);
            if(nx!=x||ny!=y) {
                Relay.inputOccured();
                x=nx;
                y=ny;
            }
        }

        @Override
        public void advanceSlide(int howFar){
            int howMuch=clamp(howFar,-it,it);

//        MainViewer.println("advance slide "+howMuch,Constants.d);
            int oldIndex=getSlide();
            if(howMuch>0) {
                Relay.inputOccured();
                for (int i = 0; i < howMuch; ++i)
                    nextSlide();
            }
            else if(howMuch<0) {
                Relay.inputOccured();
                for (int i = 0; i < -howMuch; ++i)
                    prevSlide();
            }
//        Gdx.graphics.requestRendering();
            int nextIndex=getSlide();
            if(oldIndex==nextIndex){
                if(howFar!=0)
                    Relay.scrollStuck(nextIndex);
            }
            else{
                Relay.scrollMove(nextIndex);
                scrollLog.logScroll(getSlide());
            }
        }


        private  void nextSlide(){
            int x = this.x+1;
            int y=this.y;
            if(x>=iw){
                x=0;
                y+=1;
            }

            if(getSlide(x,y)>=it)
                return;

            this.x=x;
            this.y=y;
        }
        private void prevSlide(){
            int x = this.x-1;
            int y=this.y;
            if(x<0){
                x=iw-1;
                y-=1;
            }

            if(y<0)
                return;

            this.x=x;
            this.y=y;
        }

        @Override
        public int getSlide() {
            return getSlide(x,y);
        }

        private int getSlide(int x, int y){
            return y*iw+x;
        }
        private int getX(int slide){
            return slide % iw;
        }
        private int getY(int slide){
            return slide / iw;
        }

        @Override
        public int getTotal(){
            return it;
        }

        public void requestCache(NormalDrawer d) {
            co.requests.add(d);
        }

//        @Override
//        public DistributedRegions getTextures() {
//            return new DistributedRegions(img,texture);
//        }


        @Override
        public void dispose() {
            if(texture==null)
                return;
            for(Texture t : texture)
                if(t!=null)
                    t.dispose();
        }
    }


    /**
     * if an image fails to load, but was deemed unnecessary, use a dummy drawer
     * so that the program doesn't crash if we activate that slideset (useful for debugging,
     * but this should never come up in trials)
     */
    public class DummyDrawer implements Drawer{
        @Override
        public boolean draw(SpriteBatch batch) {return true;}
        @Override
        public void goToSlide(int slide) {}
        @Override
        public void advanceSlide(int howMuch) {}
        @Override
        public int getSlide() {return 0;}
        @Override
        public int getTotal() {return 0;}
        @Override
        public void dispose() {}
        @Override
        public boolean workAvailable() {return false;}
        @Override
        public float progress() {return 1;}
        @Override
        public void run() {}
    }
}
