package com.radiogramviewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

import java.nio.IntBuffer;
import java.util.HashMap;


/**
 * Slide set drawer and navigator, it ain't simple
 */
public class SlideManager implements Disposable{
    public final static int up=1, down=-1, stay=0;

    private int dir;
    private float holdTime=0, holdFor;
    private boolean buttons, discardDelta=false;
    private Drawer drawer;

    private static HashMap<String,DistributedRegions> textureCache;

    public SlideManager(SlideDimensions.Node node, ScrollFollower scrollLog, Config c){
        dir=stay;
        holdFor=c.input.holdTime;
        buttons=c.input.wasd||c.input.arrow;

        try{
            //try loading the image, if it can't be found, but wasn't mentioned in the slide Dimensions
            // then it is expected to not be important.
            // otherwise, throw an  GDBS exception.
            if(isCached(node.file)){
                drawer=new NormalDrawer(node,scrollLog,getCached(node.file));
                MainViewer.println("using cached slides: "+node.file,Constants.d);
            }
            else {
                Pixmap p;
                p = new Pixmap(Gdx.files.internal(node.file));
                int maxSize = maxSize();
                drawer = new NormalDrawer(node, scrollLog, c, p, maxSize);
                cache(node.file,drawer);
            }
        }
        catch (Exception e){
            if(node.necessary)
                throw new FailToLoadException("Failed to load necessary image "+e.getMessage());
            else
                drawer=new DummyDrawer();
        }


    }

    private boolean isCached(String filename){
        if(textureCache==null)
            return false;
        return textureCache.containsKey(filename);
    }

    private DistributedRegions getCached(String filename){
        return textureCache.get(filename);
    }

    private void cache(String filename, Drawer d){
        if(textureCache==null)
            textureCache=new HashMap<String, DistributedRegions>();
        textureCache.put(filename,d.getTextures());
    }


    //used for calculating how to fit the slideset into the GPU
    private int largestDimension(Pixmap p){
        int h=p.getHeight();
        int w=p.getWidth();
        if(h>w)
            return h;
        else return w;
    }

    //used for seeing how large a texture can be on a specific platform's GPU
    private int maxSize(){
        IntBuffer buf = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
        return buf.get(0);
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

    private class FailToLoadException extends RuntimeException{ FailToLoadException(String s){super(s);}}

    private interface Drawer  extends Disposable{
        boolean draw(SpriteBatch batch);
        void goToSlide(int slide);
        void advanceSlide(int howMuch);
        int getSlide();
        int getTotal();
        DistributedRegions getTextures();
    }


    /**
     * The Drawer for a usable slide set
     */
    private class NormalDrawer implements Drawer{
        int x, y, ih,iw,it;
        private ScrollFollower scrollLog;
        TextureRegion [][] img;
        private Texture [] texture;

        public NormalDrawer(SlideDimensions.Node node, ScrollFollower scrollLog, DistributedRegions d){
            ih= node.height;
            iw= node.width;
            it= node.total;
            x=0;
            y=0;
            this.scrollLog=scrollLog;
            this.img=d.img;
            this.texture=d.texture;
        }

        /**
         * Create a drawer
         * @param node the node containing all the slide specific information (how many slides high, wide, total, etc)
         * @param scrollLog the log to report all scrolling to
         * @param c the app config with basic constants
         * @param p the image of the slideset
         * @param maxTextureSize how large of a texture the GPU can hold
         */
        public NormalDrawer(SlideDimensions.Node node, ScrollFollower scrollLog, Config c, Pixmap p, int maxTextureSize){
            ih= node.height;
            iw= node.width;
            it= node.total;
            x=0;
            y=0;
            this.scrollLog=scrollLog;

            //The slideset comes in through the CPU first, so that we can see how to fit it in the GPU
            if(largestDimension(p)<maxTextureSize) { //if texture already fits in renderable space on GPU
                texture=new Texture[1];
                texture[0] = new Texture(p);
                img = parse(texture[0], ih, iw);
            }
            else if(c.window.scalableTexture){ //if it is desirable to scale texture to make it fit
                MainViewer.println("scaling "+node.file+" to fit in memory", Constants.w);
                p = scale(p, maxTextureSize, iw, ih);
                texture=new Texture[1];
                texture[0] = new Texture(p);
                img = parse(texture[0], ih, iw);
            }
            else{ //if dividing the texture across multiple textures is desirable for making it fit
                int size=canFit(p,maxTextureSize,iw,ih);
                if(size<=0) {
                    size=1;
                    MainViewer.println("dividing up and scaling"+node.file+" to fit in memory", Constants.w);

                }
                else
                    MainViewer.println("dividing up "+node.file+" to fit in memory", Constants.w);
                DistributedRegions d=distribute(p,maxTextureSize,iw,ih,it,size);
                texture=d.texture;
                img=d.img;
            }
            p.dispose();
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
        private DistributedRegions distribute(Pixmap orig, int maxTextureSize, int w, int h, int totalSlides,int size){
            int textureCount=totalSlides/(size*size); //see how many textures are needed to hold the slideset

            Texture[] rt=new Texture[textureCount+1]; //set up return structure
            TextureRegion [][] rr=new TextureRegion[h][w];
            DistributedRegions r=new DistributedRegions(rr,rt);

            //get individual slide dimensions
            int eachW=orig.getWidth()/w;
            int eachH=orig.getHeight()/h;
            //get coordinate counters
            Coord oc1=new Coord(w); //oc is the coordinates for the original image's grid
            Coord oc2=new Coord(w);
            Coord pc=new Coord(size); //pc is the coordinates for the smaller image with fewer slides (that can fit in the GPU)

            MainViewer.println("individual dimensions: w"+eachW+", h"+eachH+" s"+size+" textures used"+r.texture.length, Constants.w);

            int textureIndex=0;
            int incr=size*size;
            for(int i=0; i<totalSlides; i+=incr){
                Pixmap p=new Pixmap(size*eachW,size*eachH,orig.getFormat());

                //map as many slides from the old image as you can onto a new pixmap
                pc.reset();
                for(int j=0; j<incr && i+j<totalSlides; ++j){
                    p.drawPixmap(orig,oc1.x*eachW,oc1.y*eachH,eachW,eachH,pc.x*eachW,pc.y*eachH,eachW,eachH);
                    oc1.advance();
                    pc.advance();
                }

                //save the new pixmap to a texture, scale it if necessary.
                p=scale(p, maxTextureSize, size, size);
                Texture t=new Texture(p);
                r.texture[textureIndex++]=t;
                p.dispose();

                //get newly scaled dimensions for accurate texture regions
                int H=t.getHeight()/size;
                int W=t.getWidth()/size;

                //map the new texture's slides onto the original image's grid.
                pc.reset();
                for(int j=0; j<incr && i+j<totalSlides; ++j){
                    r.img[oc2.y][oc2.x]=new TextureRegion(t,pc.x*W,pc.y*H,W,H);
                    oc2.advance();
                    pc.advance();
                }
            }


            return r;
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
            batch.draw(img[y][x],0,0,MainViewer.getWidth(),MainViewer.getHeight());
            return true;
        }

        @Override
        public void goToSlide(int slide){
            slide=clamp(slide,0,it-1);
            int nx=getX(slide);
            int ny=getY(slide);
            if(nx!=x||ny!=y) {
                MainViewer.inputOccured();
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
                MainViewer.inputOccured();
                for (int i = 0; i < howMuch; ++i)
                    nextSlide();
            }
            else if(howMuch<0) {
                MainViewer.inputOccured();
                for (int i = 0; i < -howMuch; ++i)
                    prevSlide();
            }
//        Gdx.graphics.requestRendering();
            int nextIndex=getSlide();
            if(oldIndex==nextIndex){
                if(howFar!=0)
                    MainViewer.scrollStuck(nextIndex);
            }
            else{
                MainViewer.scrollMove(nextIndex);
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

        @Override
        public DistributedRegions getTextures() {
            return new DistributedRegions(img,texture);
        }


        @Override
        public void dispose() {
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
        public boolean draw(SpriteBatch batch) {
            return true;
        }
        @Override
        public void goToSlide(int slide) {}
        @Override
        public void advanceSlide(int howMuch) {}
        @Override
        public int getSlide() {return 0;}
        @Override
        public int getTotal() {return 0;}

        @Override
        public DistributedRegions getTextures() { return null; }

        @Override
        public void dispose() {}
    }


    /**
     * simple node for holding textures and regions
     */
    private class DistributedRegions{
        final TextureRegion [][] img;
        final Texture [] texture;
        DistributedRegions(TextureRegion [][] img, Texture[] texture){
            this.img=img;
            this.texture=texture;
        }
    }
}
