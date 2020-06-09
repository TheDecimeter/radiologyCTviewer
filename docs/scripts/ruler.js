/*Simple 2D ruler that uses highlights to mark clicks, them measures the distance
  between those clicks (and logs the two points to the scroll log).
  IMPORTANT: this assumes the config file is sets the width to 800px in the config
             if this is not the case, edit the value at the bottom of the page.
 */


/**
 * Start and stop the ruler, Note that this function must be called with true first.
 * @param {*} consoleID the html element to output instructions/measurements
 * @param {*} start true or false
 * @param {*} widthInches the width of the slide set in inches (default is 14)
 */
function rulerStart(consoleID,start,widthInches=14){ RULER_OBJECT.rulerStart(consoleID,widthInches,start); }

/**
 * Call this after input events so that it can look for the next click
 */
function rulerClick(){ RULER_OBJECT.rulerClick(); }

/**
 * Call this to check if the ruler is currently active
 */
function rulerInactive(){ return RULER_OBJECT.RulerStage==0; }

class Ruler{
    constructor(globalPxWidth){
        this.globalPxWidth=globalPxWidth;
        this.RulerStage=0;
    }
    rulerStart(consoleID, widthInches, start){
        if(start){
            if(this.RulerStage==0){
                this.widthInches=widthInches;
                this.RulerOutput=$(consoleID);
                this.RulerStage=1;
                this.RulerOutput.innerHTML="<br><b>Click Start Point</b>";
                viewerResetLastClick();
                viewerResetHighlights(viewerGetCurrentSet());
                viewerClickLock(true);
            }
            return;
        }
        if(this.RulerStage!=0){
            this.RulerStage=0;
            this.RulerOutput.innerHTML="";
            viewerResetHighlights(viewerGetCurrentSet());
            viewerClickLock(false);
        }
    }
    rulerClick(){
        if(this.RulerStage==0) //off
            return;
        let click=viewerGetLastClick();
        if(click=="0,0,0,0"){ //ignore non click events
            viewerResetLastClick();
            return;
        }
        viewerResetLastClick();
        if(this.RulerStage<=2){//store first click and highlight it
            if(this.RulerStage==2)
                viewerResetHighlights(viewerGetCurrentSet());
            let a=click.split(',');
            this.drawPoint(a[0],a[1],a[2]);
            this.RulerFirstClick=a;
            this.RulerOutput.innerHTML="<br><b>Click End Point</b>";
            this.RulerStage=3;
            return;
        }
        if(this.RulerStage==3){//highlight second click and print distance
            let p1=this.RulerFirstClick;
            let p2=click.split(',');
            this.drawPoint(p2[0],p2[1],p2[2]);
            let inch=this.distance(p1[0],p1[1],p2[0],p2[1])*(this.widthInches/this.globalPxWidth);
            let cm=inch*2.54;
            let mid="";
            if(p1[2]!=p2[2]) mid="<br>Note, this is in 2 dimensions."
            this.RulerOutput.innerHTML="<br><b>Distance: "+cm.toFixed(2)+"cm ("+inch.toFixed(2)+"in)"+mid+"<br>Click a new Start Point</b>";
            this.RulerStage=2;
            this.log(p1,p2);
        }
    }
    drawPoint(x,y,z){
        if(viewerAddShapeToUI('mark',viewerGetCurrentSet(),x,y,z)) return;
        viewerAddHighlight(viewerGetCurrentSet(),x,y,z);
    }
    distance(x1,y1,x2,y2){
        return Math.hypot(x2-x1, y2-y1)
        // let x=x1-x2;
        // let y=y1-y2;
        // // return Math.sqrt(x*x+y*y);
        // return Math.hypot(x,y);
    }
    log(p1,p2){
        viewerLogScrollMessage(viewerGetCurrentSet(),"Ruler("+p1[0]+" "+p1[1]+")-("+p2[0]+" "+p2[1]+")");
    }
}

//set config width here
const RULER_OBJECT = new Ruler(800);

