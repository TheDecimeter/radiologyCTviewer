/* custom functions which are less likely to be shared between viewer implementations */


/* ______________________ event listening ________________________________________________________________________ */


var clickCount=0;
var notscrolled=true;
var currentShader='off';
var initiatedShadersOnce=false;
//one viewer requires clicks or acknowledgement of nothing to click on
window.viewerListenerClickAdded = function(click){
    clickCount++;
    if(viewerGetCurrentSet()==3){
        validPageWndTrl(false);//if they click, verify they also haven't checked that there was nothing to find and show error
        let a=click.split(',');
        this.addInput(  "pWndTrlD_"+click,
                        "wndTrlDgns",
                        "Diagnosis for "+viewerPixelsToInches(a[1]).toFixed(2)+", "+viewerPixelsToInches(a[2]).toFixed(2)+", "+a[3],"wndTrlDgnsSubClass",
                        "highlightClick(3,"+a[1]+", "+a[2]+", "+a[3]+", \""+currentShader+"\");useWASD=false;",
                        "validPageWndTrl(false);",
                        "useWASD=true;");
    }
}
window.viewerListenerClickRemoved = function(click){
    clickCount--;
    if(viewerGetCurrentSet()==3){
        validPageWndTrl(false);//if they remove clicks, remove potential errors this fixes, but don't show any new ones.
        this.removeElement("pWndTrlD_"+click);
        if(rulerInactive())
            viewerResetHighlights(3);
    }
}

//make sure they scroll at least once (in case they missed the part about how to do it, or are just trying to blow through it)
window.viewerListenerScrollSuccess = function(slideIndex){
    notscrolled=false;
    if(viewerGetCurrentSet()==3){
        validPageWndTrl(false);//if they scroll, remove potential errors this fixes, but don't show any new ones.
        if(rulerInactive())
            viewerResetHighlights(3);
    }
    if(viewerGetCurrentSet()==1){
        validPageSmpTrl(false);//if they scroll, remove potential errors this fixes, but don't show any new ones.
    }
}

//for ruler
window.viewerListenerInput = function(){
    this.rulerClick();
}

/* ______________________ page checking ________________________________________________________________________ */

function validPageConsent(showError=true){
    let allGood=true;
    
	if(!isChecked("consentContinue",1,2,showError))
        allGood=false;
        
    err(!allGood,'pagpConsentErr',showError,'pConsent');
    
	return allGood;
}

let validPageDemDoneOnce=false;
function validPageDemRedo(){
    if(validPageDemDoneOnce)
        validPageDem(false);
}
function validPageDem(showError=true){
    let allGood=true;
    
    if(!isNum("demAge"))
        allGood=false;

    if(!isChecked("demBrd",1,2))
        allGood=false;

    if(!isChecked("demExp",1,5))
        allGood=false;
    if(!isValidOther("demExp4","demExpOtherTxt",showError))
        allGood=false;
        
    err(!allGood,'pagpDemErr');

    validPageDemDoneOnce=true;
	return allGood;
}



//if showError is false, error messages will only be cleared
//this is good for not causing a lot of distractions as a participant
//navigates an image.
function validPageWndTrl(showError=true){
    let allGood=true;
    
    if(clickCount==0){
        if($('wndTrlNone').checked==false){ //no clicks and didn't indicate no tumors found, bad
            allGood=false;
            err(true,'winTrlClkNone',showError);
        }
        else{ //no clicks but indicated no turmors were found, good
            err(false,'winTrlClkNone',showError);
            err(false,'winTrlClkBoth',showError);
        }
    }
    else{
        if($('wndTrlNone').checked==false){ //some clicks and didn't indicate no tumors found, good
            err(false,'winTrlClkNone',showError);
            err(false,'winTrlClkBoth',showError);
        }
        else{ //some clicks and indicated no tumors were found, bad
            allGood=false;
            err(true,'winTrlClkBoth');
        }
    }


    if(!checkInFilled("wndTrlDgns","wndTrlDgnsSubClass",showError)){
        err(true,'winTrlDgns');
        allGood=false;
    }
    else
        err(false,'winTrlDgns');


    if(notscrolled){
        allGood=false;
        err(true,'winTrlScrl');
    }
    else
        err(false,'winTrlScrl');
        

    return allGood;
}

function validPageSmpTrl(showError=true){
    let allGood=true;

    if(!isChecked("pSmpTrlAb",1,5,showError))
        allGood=false;
    if(!isValidOther('pSmpTrlAb4','pSmpTrlAbOtherTxt',showError))
        allGood=false;

    err(!allGood,'smpTrlErr', showError);
    
    if(notscrolled){
        allGood=false;
        err(true,'smpTrlScrl');
    }
    else
        err(false,'smpTrlScrl');
        

    return allGood;
}




/* ______________________ page helpers ________________________________________________________________________ */

//function to go to windowing debrief page
function pdbf0(){
    cancelFireworks();
    viewerClickLock(true);
    viewerResetHighlights(3);
    goToPageWithViewer("pWndDbf",3,1);
    setShader("off");
    let marginOfError=1.5;
    markFound(2*marginOfError,24*marginOfError,'pWndDbfFnd',[[256,223,19],[342,290,19],[578,228,23]],3);
}

//wrapper to only set shader when it is something new
function setShader(shader){
    if(currentShader==shader)
        return false;
    viewerSetShader(shader);
    currentShader=shader;
    return true;
}

function markFound(depth,radius,ID,coords,set){
    let clicks=viewerGetClicksFor(set,',',';');
    let successes=0;
    clicks=clicks.split(';');
    for(let c=0; c<coords.length; ++c){
        let coord=coords[c];
        let e=$(ID+(1+c));
        e.innerHTML="Missed";
        e.style.color="#F88";
        for(let i=0; i<clicks.length-1;i++){
            let click=clicks[i].split(',');
            if(click[2]<=coord[2]+depth && click[2] >=coord[2]-depth){
                if(distance(click[0],click[1],coord[0],coord[1])<=radius){
                    e.innerHTML="Found";
                    e.style.color="#8F8";
                    successes++;
                    break;
                }
            }
        }
    }
    
    let falseAlarmsE=$(ID+(1+coords.length));
    let falseAlarms=clicks.length-successes-1; //clicks will have an emtpy item at the end
    
    if(falseAlarmsE){
        if(falseAlarms==1)
            falseAlarmsE.innerHTML="There was 1 false alarm.";
        else if(falseAlarms>1)
            falseAlarmsE.innerHTML="There were "+falseAlarms+" false alarms.";
    }
        

}


function highlightClick(at,x,y,z,shaderName=""){
    if(shaderName!=""){
        if(currentPage=='pWndTrl') //make sure any time you change a shader, you also change the appropriate dropdown
            $('windows1').value=shaderName;
        if(setShader(shaderName))
            viewerLogShaderMessage("auto change to "+shaderName);
    }
    viewerResetHighlights(at);
    viewerAddHighlight(at,x,y,z);
    viewerLogScrollMessage(at,'auto scroll to '+z);
    viewerSetSlide(at,z);
}

function checkInFilled(at, className,showError=true){
    at=$(at);
    if(at==null){
        console.log("at is null");
        return true;
    }
        
    let l=at.querySelectorAll('.'+className);
    let allGood=true;
    for(let i=0; i<l.length; ++i)
        if(!isFilled(l[i],showError)){
            allGood=false;
        }
    return allGood;
}

function addAll(at, key, valsClass){
    at=$(at);
    key=ID(key);
        
    let l=at.querySelectorAll('.'+valsClass);
    for(let i=0; i<l.length; ++i)
        addData(key+(i+1),l[i].value+" "+l[i].parentNode.id);
}


function viewerPixelsToInches(x){
    //pixel coordinates are given according to the original config file's dimesions
    // not the actual pixel dimensions of the one displayed so we need to scale
    // them to pixels which match the current screen.
    let scl=1/viewerGetViewerDensityFactor();
    //now we need to compare the true pixels to the dpi of the physical screen
    return (x*scl)*(viewerGetWidthInches()/viewerGetWidth());
    //return viewerUtilityPixelsToInches(x*scl);
}

function resetClicksFor(slideSet){
    viewerResetClicks(slideSet);
    clickCount=0;
    if(slideSet==3){
        removeAllFrom('wndTrlDgns');
        validPageWndTrl();
    }
}


function addInput(elemID, toID, hint="", className="", onfocus="", oninput="", onblur="") {
    let text = document.createElement('div');
    text.id = elemID;
    text.style.padding="2px";
    text.innerHTML = "<input type='text' style='width:90%;' class='"+className+"' placeholder='"+hint+"' onfocus='"+onfocus+"' onclick='"+onfocus+"' oninput='"+oninput+"' onblur='"+onblur+"'/>";

    $(toID).appendChild(text);
}

function removeElement(elemID){
    let e=$(elemID)
    e.parentNode.removeChild(e);
}
function removeAllFrom(id){
    $(id).innerHTML="";
}


/* ______________________ example shader fragments ________________________________________________________________________ */

function createShaders(){
    if(initiatedShadersOnce)
        return;
    initiatedShadersOnce=true;
    /* simple custom shaders for demo purposes (defined below) */
    viewerAddCustomShader('bl','d',blueFragment,false);
    viewerAddCustomShader('sq','d',squareFragment,false);
    viewerAddCustomShader('rt','d',sqrtFragment,false);

    /* hot metal dynamic windowed shaders (defined below) */
    viewerAddCustomShader('hotFull','d',generateHotMetalShader('z','0.0','1.0'),false);
    viewerAddCustomShader('hotLung','d',generateHotMetalShader('z','-0.1','0.8'),false);
    viewerAddCustomShader('hotAb','d',generateHotMetalShader('z','0.405','0.2'),false);

    /* common windowing levels according to values on microdicom
    Note that very small windows will amplify jpg's compression defects*/
    viewerAddWindowingShaderGray('fullDynamic',.5285,1.0815,false);
    viewerAddWindowingShaderGray('skull',.5125,.0475,false);
    viewerAddWindowingShaderGray('lung',.3,.8,false);
    viewerAddWindowingShaderGray('ab',.505,.2,false);
    viewerAddWindowingShaderGray('mediastinum',.505,.225,false);
    viewerAddWindowingShaderGray('bone',.65,1.25,false);
    viewerAddWindowingShaderGray('spine',.51,.15,false);
    viewerAddWindowingShaderGray('postmyelo',.6,.5,false);
    viewerAddWindowingShaderGray('felsenbein',.75,2,false);
    viewerAddWindowingShaderGray('user1',.5,-1,false);


    //instead of logging the creation of each message to send back to the server, just make an initial statement with and ID to easily find what the shaders were.
    viewerLogShaderMessage("added initial 4JUN2020");
}




/* custom shader to square each pixel (gives more of the color spectrum to brighter values, roughly the top quarter gets half of the spectrum) */
var squareFragment= "#ifdef GL_ES\n\
#define LOWP lowp\n\
    precision mediump float;\n\
#else\n\
    #define LOWP\n\
#endif\n\
varying LOWP vec4 v_color;\n\
varying vec2 v_texCoords;\n\
uniform sampler2D u_texture;\n\
\n\
vec3 sq(vec3 c) {\n\
    return vec3(c.r*c.r,c.g*c.g,c.b*c.b); \n\
  }\n\
  \n\
void main()\n\
{\n\
    vec4 c = texture2D(u_texture, v_texCoords).rgba;\n\
    gl_FragColor = vec4(sq(c.rgb), c.a);\n\
}";

/* custom shader to square root each pixel (gives more of the color spectrum to darker values, roughly the lower quarter gets half of the spectrum) */
var sqrtFragment= "#ifdef GL_ES\n\
#define LOWP lowp\n\
    precision mediump float;\n\
#else\n\
    #define LOWP\n\
#endif\n\
varying LOWP vec4 v_color;\n\
varying vec2 v_texCoords;\n\
uniform sampler2D u_texture;\n\
\n\
vec3 rt(vec3 c) {\n\
    return vec3(sqrt(c.r),sqrt(c.g),sqrt(c.b)); \n\
  }\n\
  \n\
void main()\n\
{\n\
    vec4 c = texture2D(u_texture, v_texCoords).rgba;\n\
    gl_FragColor = vec4(rt(c.rgb), c.a);\n\
}";

/* custom shader to better illustrate diffrence between UI shapes and image shapes */
var blueFragment= "#ifdef GL_ES\n\
#define LOWP lowp\n\
    precision mediump float;\n\
#else\n\
    #define LOWP\n\
#endif\n\
varying LOWP vec4 v_color;\n\
varying vec2 v_texCoords;\n\
uniform sampler2D u_texture;\n\
\n\
vec3 bl(vec3 c) {\n\
    return vec3(c.rg*0.5,c.b*2.0); \n\
  }\n\
  \n\
void main()\n\
{\n\
    vec4 c = texture2D(u_texture, v_texCoords).rgba;\n\
    gl_FragColor = vec4(bl(c.rgb), c.a);\n\
}";


let hot2= "#ifdef GL_ES\n\
#define LOWP lowp\n\
    precision mediump float;\n\
#else\n\
    #define LOWP\n\
#endif\n\
varying LOWP vec4 v_color;\n\
varying vec2 v_texCoords;\n\
uniform sampler2D u_texture;\n\
\n\
float R(float c) {\n\
    return 1.4117556*c; \n\
}\n\
\n\
float G(float c) {\n\
    return 2.74*c-1.36541176471; \n\
}\n\
\n\
float B(float c) {\n\
    return 3.89285714284*c-2.92170868346; \n\
}\n\
\n\
vec3 h(float c) {\n\
    return vec3(R(c),G(c),B(c)); \n\
}\n\
\n\
float C(vec2 c) {\n\
    return (c.r*256.0+c.g)*0.1275; \n\
}\n\
\n\
float O(float c) {\n\
    return c*2.0475; \n\
}\n\
\n\
float W(float c) {\n\
    return (c-(";

let hot3="))/";

let hot4="; \n\
}\n\
\n\
void main()\n\
{\n\
    vec4 c = texture2D(u_texture, v_texCoords).rgba;\n\
    gl_FragColor = vec4(h(W(";

let hot5=")), c.a);\n\
}";

function generateHotMetalShader(t,lo, width){
    if(t=='z')
        return hot2+lo+hot3+width+hot4+"c.r"+hot5;
    if(t=='f')
        return hot2+lo+hot3+width+hot4+"O(c.r)"+hot5;
    if(t=='h')
        return hot2+lo+hot3+width+hot4+"C(c.rg)"+hot5;
}

/* ______________________ post trial rewards ________________________________________________________________________ */
function reward(){
    showViewerAt(4,0);
    viewerClickLock(false);
    launchRandomFireworks(2,2,30,50,300,500,150,750,10,2,100,80,2000);
}
function moreReward(){
    setShader('sq');
    rewardAutoScrollStart();
    launchRandomFireworks(1,5,30,50,300,500,150,750,10,2,100,80,4000,rewardAutoScrollStop);
}
