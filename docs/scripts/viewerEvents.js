
/* ______________________Viewer Events________________________________________________________________________ */
/* These functions are called by the viewer to update you of various events */

/* Called when viewer loads, provide a custom width here (aspect ratio is always preserved). */
window.viewerStatsWidth = function(){
    setSizes(.9,.66,512); //defined in common.js
	return viewerWidth;
}

/* called when the viewer resets and is usable again (is not called the first time it is loaded) */
window.viewerEventResetFinished=function(){
    this.console.log("viewer reset size");
}


/* Called when any input was received (not just clicks)*/
// window.viewerListenerInput = function(){
//     var lastClick=viewerGetLastClick();
//     if(lastClick=="0,0,0,0")
// 		console.log("received non click input for slide set "+viewerGetCurrentMode()+ " on slide "+viewerGetCurrentSlide())
// 	else{
// 		console.log("received click "+viewerGetLastClick()+" for slide set "+viewerGetCurrentMode());
// 		viewerResetLastClick();
// 	}
// }

/* Called when a click is added to the viewer */
// window.viewerListenerClickAdded = function(click){
//     console.log("added click "+click);
// }

/* Called when a click is removed from the viewer */
// window.viewerListenerClickRemoved = function(click){
//     console.log("removed click "+click);
// }

/* Called when the viewer scrolls successfully */
// window.viewerListenerScrollSuccess = function(slideIndex){
//     console.log("scroll success "+slideIndex);
// }

/* Called when the viewer tries to scroll past the image bounds */
// window.viewerListenerScrollFail = function(slideIndex){
//     console.log("scroll fail "+slideIndex);
// }



/* Called on every cycle of the renderer even when it isn't showing */
// window.viewerCoroutine = function(){
//     console.log("coroutine ");
// }



/* Called when the viewer makes progress on image processing
    This will not fire more than the milliseconds specified in the "yield"
    variable of the config file
    remaining - the remaining tasks (including the current)
    progress - the percentage (between 0 and 1) of work on the current image */
window.viewerListenerProcessing = function(remaining,progress){
    let t=19;
    $("blockerConsole").innerHTML="Processing: "+(t-remaining)+"/"+t+" "+(progress*100)+"%";
}


/* Called when the viewer reaches a loading milestone (both on initial start and restart)*/
window.viewerListenerLoadingStateChanged = function(state){
	if(state == FLAGviewerFailedLoad)//error
        alert("Please refresh page.\n\nCT-Viewer did not load properly.\n\
        This is likely due to an interruption in the network.");
    else if(state==FLAGviewerLoaded){ //loaded, but still might have lengthy processing to do
        //all viewer functions are now available, so maybe go ahead and set the slide to your first one
        //so the "no slide" gray screen is never shown.
        //viewerSetSlide(3);
    }
	else if(state == FLAGviewerReady){ //loaded and processed, ready to display
        block=false;  //"turn off" blocking page
        setFeed("");
        $("blockerConsole").innerHTML="";
    }

}

/* Called when the viewer prints to console, use to debug when a console isn't available, or when you
   wish to pipe console output from users to yourself (this only catches output from the viewer itself
    not exceptions, or messages from external javascript). */
// window.viewerConsoleOut = function(msg){
//     console.log("Custom console out: "+msg);
// }

/* Called at the very beginning, if you wish to make your own loading panel do so here 
    Make sure you either create a meter bar with the id "viewer-meter-inner-bar" or
	override the loading behavior with the function "window.viewerLoadingBarAdvance(percent)
	shown below, otherwise the default loading behavior crashes because it tries to
	advance a bar which doesn't exist.*/
// window.viewerCustomLoadingBar = function(panel){
//     console.log("making panel");
//     panel.innerHTML="<br><br><h3>HI, I'm A custom loading panel</h3><br><br>\
//                     <div id='viewer-meter-outer-bar'><div id='viewer-meter-inner-bar'></div></div>";
// }

/* Called whenever the loading panel increases progress */
// window.viewerLoadingBarAdvance = function(percent){
//     $("viewer-meter-inner-bar").style.width=""+(percent*100)+"%";
//     console.log("percent "+percent);
// }


/* ______________________System Events________________________________________________________________________ */

//directly pipe keyboard input to the viewer (necessary for some browsers/html widgits)
// make sure to turn "useWASD" to false if the user needs to input text.
window.addEventListener('keydown', function(e) {
    
    if (e.code !== undefined) {
        if(e.code == 'ArrowUp'||(useWASD && e.code == 'KeyW')){
            viewerPipeInput(FLAGviewerKeyScrollUp, FLAGviewerKeyPressDown);
            e.preventDefault();
        }
        else if(e.code == 'ArrowDown'||(useWASD && e.code == 'KeyS')){
            viewerPipeInput(FLAGviewerKeyScrollDown, FLAGviewerKeyPressDown);
            e.preventDefault();
        }
    } 
    else if (e.keyCode !== undefined) {
        if(e.keyCode == 40 ||e.keyCode == 38||(useWASD && (e.keyCode == 87||e.keyCode == 83))) {
            viewerPipeInput(e.keyCode,FLAGviewerKeyPressDown);
            e.preventDefault();
        }
    }
  });
  window.addEventListener('keyup', function(e) {

    if (e.code !== undefined) {
        if(e.code == 'ArrowUp'||(useWASD && e.code == 'KeyW')){
            viewerPipeInput(FLAGviewerKeyScrollUp, FLAGviewerKeyPressRelease);
            e.preventDefault();
        }
        else if(e.code == 'ArrowDown'||(useWASD && e.code == 'KeyS')){
            viewerPipeInput(FLAGviewerKeyScrollDown, FLAGviewerKeyPressRelease);
            e.preventDefault();
        }
    } 
    else if (e.keyCode !== undefined) {
        if(e.keyCode == 40 ||e.keyCode == 38||(useWASD && (e.keyCode == 87||e.keyCode == 83))) {
            viewerPipeInput(e.keyCode,FLAGviewerKeyPressRelease);
            e.preventDefault();
        }
    }
  });