/**
 * silly setup to draw firework like particles on the viewer using simulated clicks.
 * Make sure to save the click log first, this will decimate it.
 * 
 * Another optional method is below which autoscrolls the slide for a dynamic background
 * to the firework show :P
 */


/**
 * Specify a crap ton of ranges to launch fireworks at. 1 firework will always fire immediately, the next will fire within finalLaunchTime millisecons
 */
function launchRandomFireworks(minCount,maxCount,minThrust,maxThrust,minAlt,maxAlt,minX,maxX,spawn,gravity,millisPerFrame,ttl,finalLaunchTime,fireworkStopEvent=null){
    let range=function(min,max){let r=max-min; return Math.floor(min+Math.random()*r);}
    let count=range(minCount,maxCount);
    if(count>=1)
        launchFirework(range(minX,maxX),range(minAlt,maxAlt),spawn,ttl,minThrust,maxThrust,gravity,millisPerFrame,fireworkStopEvent);
    for(let i=1; i<count; ++i){
        let time=range(0,finalLaunchTime);
        fireworkScheduledLaunches.push(setTimeout(function(){launchFirework(range(minX,maxX),range(minAlt,maxAlt),spawn,ttl,minThrust,maxThrust,gravity,millisPerFrame,fireworkStopEvent);},time));
    }
}

/**
 * launch a firework and add it to the existing ones
 */
function launchFirework(x,alt,spawn,ttl,minThrust,maxThrust,gravity,millisPerFrame,fireworkStopEvent=null){
    FireworkStopEvent=fireworkStopEvent;
    fireworkAnimCount=ttl;
    if(FireworkControllerAnimator==null){
        fireworkObjects= [new FireworkController(alt,spawn,x,minThrust,maxThrust,gravity,ttl)];
        FireworkControllerAnimator = setInterval(fireworkFrame, millisPerFrame);
    }
    else{
        fireworkObjects.push(new FireworkController(alt,spawn,x,minThrust,maxThrust,gravity,ttl));
    }
}
/**
 * Stop any current and upcomming firework launches
 */
function cancelFireworks(){
    if(fireworkAnimCount<=0)
        return;
    fireworkAnimCount=-1;
    fireworkFrame();
    for(let i=0; i<fireworkScheduledLaunches.length; ++i)
        clearTimeout(fireworkScheduledLaunches[i]);
    fireworkScheduledLaunches=[];
}


/**
 * single particle class to keep track of current position and heading
 */
class FireworkParticle{
    constructor(x,y,slide,hx,hy,set,clickType){
        this.x=x;
        this.y=y;
        this.slide=slide;
        this.hx=hx;
        this.hy=hy;
        this.set=set;
        this.clickType=clickType;
    }
    advance(slide,gravity){
        this.hy-=gravity;
        this.x+=this.hx;
        this.y+=this.hy;
        this.slide=slide;
        this.clickType(this.set,this.x,this.y,slide)
        //viewerSimulateClick(this.set,this.x,this.y,slide);
    }
}
/**
 * class to manage the particles of a single firework during launch, split, and freefall.
 */
class FireworkController{
    constructor(alt,spawn,x,minThrust,maxThrust,gravity,ttl){
        this.ttl=ttl;
        this.igniteAlt=alt;
        this.spawn=spawn;
        this.minThrust=minThrust;
        this.thrustMargin=maxThrust-minThrust;
        this.gravity=gravity;
        this.set=viewerGetCurrentSet();

        let y=Math.floor(this.minThrust+Math.random() * this.thrustMargin);
        this.p=new FireworkParticle(x,0,viewerGetCurrentSlide(),0,y,this.set,viewerSimulateClick);
    }
    advance(){
        if(this.igniteAlt>=0){
            this.stage1();
            if(this.p.y>=this.igniteAlt){
                this.split();
                this.igniteAlt=-1;
            }
        }
        else{
            this.stage2();
        }
        this.ttl--;
    }
    stage1(){//launch stage
        let set=viewerGetCurrentSet();
        let slide=viewerGetCurrentSlide();
        this.p.advance(slide,0);
    }
    stage2(){//plume stage
        let set=viewerGetCurrentSet();
        let slide=viewerGetCurrentSlide();
        for(let i=0; i<this.p.length;++i)
            this.p[i].advance(slide,this.gravity);
    }
    split(){
        let px=this.p.x;
        let py=this.p.y;
        this.p.remove;
        let set=viewerGetCurrentSet();
        let slide=viewerGetCurrentSlide();
        this.p=[];
        for(let i=0; i<this.spawn;++i){
            let t=this.minThrust+Math.random() * this.thrustMargin;
            let x=Math.random();
            let y=1-x;
            x=Math.floor(x*t);
            y=Math.floor(y*t);
            if(Math.random()<.5) x= -x;
            if(Math.random()<.5) y= -y;

            if(Math.random()<.5) this.p.push(new FireworkParticle(px+x,py+y,slide,x,y,set,viewerSimulateClick));
            else                 this.p.push(new FireworkParticle(px+x,py+y,slide,x,y,set,viewerAddHighlight));
        }
    }
}

//variables to hold all of the active fireworks, as well as keeping track of their animation loop, and time to live count
var fireworkObjects,fireworkAnimCount=0,FireworkControllerAnimator=null,fireworkScheduledLaunches=[],FireworkStopEvent;



/**
 * never call, this runs the frames of animation, call launchFirework, or launchRandomFireworks instead.
 */
function fireworkFrame(){
    viewerResetClicks(viewerGetCurrentSet());
    viewerResetHighlights(viewerGetCurrentSet());
    for(let i=0; i<fireworkObjects.length; ++i)
        fireworkObjects[i].advance();
    
    for(let i=0; i<fireworkObjects.length; ++i)
        if(fireworkObjects[i].ttl<0)
            fireworkObjects.shift();
    fireworkAnimCount--;
    if(fireworkAnimCount<=0){
        clearInterval(FireworkControllerAnimator);
        viewerResetClicks(viewerGetCurrentSet());
        viewerResetHighlights(viewerGetCurrentSet());
        FireworkControllerAnimator=null;
        fireworkObjects=null;
        if(FireworkStopEvent!=null)
            FireworkStopEvent();
    }
}




/* ______________________ auto scroller ________________________________________________________________________ */

/**
 * call this to start the autoscroller
 */
function rewardAutoScrollStart(){
    if(RewardAutoScrollerObject.stopped){
        RewardAutoScrollerObject.start(setInterval(rewardAutoScrollFrame, 50));
    }
}
/**
 * call this to stop the auto scroller
 * Note: this was made with the idea of being fed into the FireworkStopEvent
 * in those various launch functions above.
 */
function rewardAutoScrollStop(){
    RewardAutoScrollerObject.stop();
}

class RewardAutoScroller{
    constructor(){
        this.stopped=true;
        this.dir=1;
    }
    start(autoscroller){
        this.slide=viewerGetCurrentSlide();
        if(this.slide==1) this.slide++;
        else this.slide--;
        this.stopped=false;
        this.failed=0;
        this.autoscroller=autoscroller;
    }
    update(){
        // console.log('slide '+this.slide+" dir"+this.dir);
        if(viewerGetCurrentSlide()==this.slide){
            this.failed++;
            if(this.failed>5){
                this.failed=0;
                this.dir*=-1;
                viewerSetSlide(viewerGetCurrentSet(),this.slide+this.dir);
            }
        }
        this.slide=viewerGetCurrentSlide();
        viewerSetSlide(viewerGetCurrentSet(),this.slide+this.dir);
    }
    stop(){
        clearInterval(this.autoscroller);
        this.stopped=true;
    }
}
var RewardAutoScrollerObject=new RewardAutoScroller();


function rewardAutoScrollFrame(){
    RewardAutoScrollerObject.update();
}
