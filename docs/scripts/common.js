/**
 * This file contains common functions which interface with the ct-viewer,
 *  as well as common types of input validation and transmission.
 */

 /* ______________________Common variables________________________________________________________________________ */

 const pageVersion=1;

 //tracking
 const pageLoadTime=Date.now(); //see when the page was first loaded
 var currentPage='pConsent'; //set to the first page *****************************************EDIT ME
 const totalStages=6; //how many pieces you want the user's work divided into**********************EDIT ME
 var currentStage=1;
 var dataNotSubmitted=false; //used to prompt user if they have unsubmitted changes.
 var block=true; //blocks form progression until viewer loads successfully
 const blocker='blocker';
 const blockerText='blockerText';
 var sentCount=1; //attempts to send data to server
 const formID='questions';
 
 //transmission status pages
 const processingSite='process.php'; //where are the results sent
 const successPage='pSuccess';
 const failPage='pFail';
 
 const multifailPage='pMultiFail';
 const multifailReturnAddressFullID='pMultiFailReturnFull';
 const multifailReturnAddressID='pMultiFailReturn';
 const mutlifailDataBoxID='pMultiFailData';
 
 const mailTo='foo@bar.eggs'; //if the server goes down, where can data be sent********************EDIT ME
 const subject='new data';
 
 const attemptCounterID='Attempts';//appended to make failPageAttempts and multifailPageAttempts
 const failCodeID='Code';//appended to make failPageCode and multifailPageCode
 
  //error highlight
  var errorColor = '#990000';
 
  //regex
 const word= new RegExp("^[A-Za-z]+$");
 const number= new RegExp("^\\d+$");
 const range= new RegExp("-");
 
 //layout
 const feed='feedbackConsole';
 const stageCounter='stageCounter';
 const sidePadding='sidePadding';
 const viewer='viewerWindow';
 const form='formDiv';
 const feedbackArea='feedbackArea';
 var viewerWidth=800;
 var formWidth=400;
 
 var useWASD=true; //should W and S be piped directly to viewer, good if using WASD to scroll, bad if text box needs those characters, control dynamically with onfocus and onblur
 
 /* ______________________Common functions________________________________________________________________________ */
 
 // ######################## shorthand functions ################
 function $(x){if(typeof x === "string") return document.getElementById(x); return x;} //Get dom element by id or itself
 function ID(x){if(typeof x === "string") return x; else return x.id;} //Get id, either from the dom element or itself
 function B(x,d){if(typeof x === "boolean") return x; else return d;} //get a potential bool, default to 'd' if impossible
 function N(x,d){if(typeof x === "number") return x; else return d;} //get a potential number, default to 'd' if impossible
 function S(x,d){if(typeof x === "string") return x; else return d;} //get a potential string, default to 'd' if impossible
 function px(x){return ""+x+"px";} //turn number into pixel size
 
 // ######################## utility ############################
 function addData(key,value){
	 //console.log("adding data "+key+", "+value);
	 let x = document.createElement("INPUT")
	 x.setAttribute("type", "hidden");
	 x.setAttribute("name", key);
	 x.setAttribute("value",value);
	 $(formID).appendChild(x);
 }
 
 function distance(x1,y1,x2,y2){
	 return Math.hypot(x2-x1, y2-y1)
	 // let x=x1-x2;
	 // let y=y1-y2;
	 // // return Math.sqrt(x*x+y*y);
	 // return Math.hypot(x,y);
 }
 
 function viewerAvailable(){
	 return typeof viewerSetSlide === "function" && !block;
 }
 function bkgd(e){
	 e=$(e);
	 if(e.bkgd == null)
		 return e.parentElement;
	 return e.bkgd;
 }
 
 function shuffle(array) {
	 let counter = array.length;
	 while (counter > 0) {
		 let index = Math.floor(Math.random() * counter);
		 counter--;
		 let temp = array[counter];
		 array[counter] = array[index];
		 array[index] = temp;
	 }
 }
 
 //convert from HU windowing units to 0 - 1 values for shaders.
 function convertHUwidth(w){
	 return w/2000.0;
 }
 function convertHUlevel(l){
	 return (l+1000)/2000.0;
 }
 function makeShader(name,l,w){
	 let L=Number($(l).value);
	 if(Number.isNaN(L))
		 return;
	 let W=Number($(w).value);
	 if(Number.isNaN(W))
		 return;
	 L=convertHUlevel(L);
	 W=convertHUwidth(W);
	 viewerAddWindowingShaderGray(name,L,W);
 }
 
 
 // ######################## form manipulation ##################
 function shuffleInputs(id){
	 let elem = $(id);
	 for (let i = elem.children.length; i >= 0; i--) {
		 elem.appendChild(elem.children[Math.random() * i | 0]);
	 }
 }
 function clearChecks(id, from, to){
	 for (let i=from; i<=to; i++){
		 $(id+i).checked=false;
	 }
 }
 
 // ######################## basic form validation ##############
 //----meant for internal usage-----
 //go through checkboxes or radio buttons with shared id "id" between 
 // numbers from and to (inclusive) and see if at least one was selected
 function verifyChecked(id, from, to){
	 for (let i=from; i<=to; i++){
		 if($(id+i).checked==true){
			 return true;
		 }
	 }
	 return false;
 }
 //highlight the elements "ids" with "color" (probably used for error highlighting)
 // also show the error message if there was an error
 function highlightElements(color,errorMessage,ids){
	 let errorMessageID = errorMessage.replace(/\s/g, '');
	 for (let i=2; i<arguments.length; i++){
		 if(color==""){
			 let e =$(arguments[i].id+errorMessageID);
			 if(e!=null)
				 $(arguments[i]).removeChild($(arguments[i].id+errorMessageID));
		 }
		 else{
			 if(errorMessage!="" && $(arguments[i].id+errorMessageID)==null){
				 let e=document.createElement("SPAN");
				 e.id=arguments[i].id+errorMessageID;
				 e.className="errorMsg";
				 e.style.display="inline";
				e.innerHTML="<br>"+errorMessage;
				 $(arguments[i]).appendChild(e);
			 }
		 }
		 $(arguments[i]).style.backgroundColor = color;
	 }
 }
 //see if a text box "id" conforms to a specific "regex", if not, flag it's "background"
 // with an error highlight (if so, clear the background)
 function checkText(text,regex,background,message,showError=true){
	 if(regex.exec(text)){
		 highlightElements("",message,background);
		 return true;
	 }
	 if(showError){
		 highlightElements(errorColor,message,background);
	 }
	 return false;
 }
 
 //----meant for external usage-----
 // sweep radio or check boxes with the shared name "id" from indices "from" to "to" (inclusive).
 // highlight "background" with the error color if none are checked.
 function isChecked(id,from,to,showError=true){
	 let background = $(id);
	 id=ID(id);
	 if(!verifyChecked(id,from,to)&&showError){
		 highlightElements(errorColor,"Required",background);
		 return false;
	 }
	 highlightElements("","Required",background);
	 return true;
 }
 
 function cTrue(id){
	 id=$(id);
	 return id.checked;
 }
 
 // specific check on "other" styled check boxes to ensure if it is checked, the text box
 // isn't empty. Error flagging is slightly different, instead of highlighting a background,
 // a custom error message is displayed.
 function isValidOther(otherCheckId,otherTextId,showError=true,errorMsgId){
	 let background=bkgd(otherTextId);
	 if($(otherCheckId).checked==true){
		 if($(otherTextId).value.length==0&&showError){
			 let e=$(errorMsgId);
			 if(e!=null)e.style.display="block";
			 else highlightElements(errorColor,"Please Specify Other",background);
			 return false;
		 }
	 }
	 let e=$(errorMsgId);
	 if(e!=null)e.style.display="none";
	 highlightElements("","Please Specify Other",background);
	 return true;
 }
 //check the "other" check box if words have been added or removed
 // from it's text box.
 function updateCheckOther(textElem, checkID){
	 textElem=$(textElem);
	 if(textElem.value.length!=0){
		 $(checkID).checked=true;
		 return true;
	 }
	 else{
		 $(checkID).checked=false;
		 return false;
	 }
 }
 
 function isFilled(id,showError=true){
	 e=$(id);
	 let background = bkgd(e);
	 if(e.value.length>0){
		 highlightElements("","Required",background);
		 return true;
	 }
	 if(showError)
		 highlightElements(errorColor,"Required",background);
	 return !showError;
 }
 function isWord(id,showError=true){
	 let background = bkgd(id);
	 return checkText($(id).value,word,background,"One Word Required",showError);
 }
 function isNum(id,showError=true,partial=false){
	 let s=$(id).value;
	 let background = bkgd(id);
	 if(s.charAt(0)=='-')
		 if(s.length==1&&partial)
			 return checkText('1',number,background,"Number Required",showError);
		 else
			 return checkText(s.substring(1,s.length),number,background,"Number Required",showError);
	 return checkText(s,number,background,"Number Required",showError);
 }
 function isRange(id){
	 minSize=2;
	 let s=$(id).value;
	 if(s.length<minSize)
		 return false;
	 if(s.charAt(0)=='-')
		 return range.exec(s.substring(1,s.length));
	 return range.exec(s);
 }
 
 //show the error message if failed is true
 //there are two ways of showing an error message
 // - either make a custom div with a message and specify it's ID as the "errorMessage"
 // - or supply a custom text message as "errorMessage" and an element ID as "e" where 
 //   it is to be displayed.
 function err(failed,errorMessage,showError=true,e=this){
	 if($(errorMessage)==null){
		 let errorMessageID = errorMessage.replace(/\s/g, '');
		 let background = bkgd(e);
		 if($(background.id+errorMessageID)==null){
			 let e=document.createElement("SPAN");
			 e.id=background.id+errorMessageID;
			 e.className="errorMsg";
			 let t=document.createTextNode(errorMessage);
			 e.appendChild(document.createElement('BR'));
			 e.appendChild(t);
			 background.appendChild(e);
		 }
		 errorMessage=$(background.id+errorMessageID)
	 }
	 if(!failed){
		 $(errorMessage).style.display="none";
		 return false;
	 }
	 if(!showError)
		 return false;
	 e= $(errorMessage);
	 if(e.tagName=="SPAN")
		 e.style.display="inline";
	 else
		 e.style.display="block";
	 return true;
 }
 
 // ######################## navigation #########################
 function goToPage(page){
	 $(currentPage).style.display="none";
	 currentPage=page;
	 $(currentPage).style.display="block";
	 $(currentPage).scrollTop=0;
	 $(form).scrollTop=0;
 }
 function goToPageWithViewer(page,slideSet,slide, callback=null){
	 if(viewerAvailable()){
		 initViewerValues();
		 showViewerAt(slideSet,slide);
		 goToPage(page);
		 if(callback!=null)
			 callback();
	 }
	 else{
		 showViewer();
		 goToPage(blocker);
		 setTimeout(function() {
			 goToPageWithViewer(page,slideSet,slide,callback);
		 }, 200)
	 }
 }
 function showViewerAt(slideSet, at){
	 viewerSetSlide(slideSet,at);
	 showViewer();
 }
 function showViewer(){
	 $(viewer).style.display="block";
	 $(form).style.width=px(formWidth);
	 $(viewer).style.width=px(viewerWidth);
 }
 function hideViewer(){
	 if(typeof viewerSetSlide === "function")
		 viewerSetSlide(0,0);
	 $(viewer).style.display="none";
	 $(form).style.width=px(formWidth+viewerWidth);
	 $(viewer).style.width="0px";
 }
 function silentClose(flag){
	 dataNotSubmitted=!flag;
 }
 window.onload = function() {
	 //set up warning if user tries to leave page without submiting data
	 window.addEventListener("beforeunload", function (e) {
		 if (dataNotSubmitted) {
			 setFeed("Please complete the trial and submit the data.");
			 let confirmationMessage = 'You have unsubmitted data, Are you sure you wish to leave?';
			 (e || window.event).returnValue = confirmationMessage; //Gecko + IE
			 return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
		 }
		 return undefined;
	 });
	 $(formID).reset();
 };
 window.onresize = function(){ //resizing the viewe recreates all click/highlight/shape images and rescales their positions
	 if(viewerAvailable())
         setFeed('Resize Viewer? <button onclick="viewerResetSize();" type="button">Yes</button><button onclick="setFeed(\'\');" type="button">No</button>');
    else
        setSizes(.9,.66,512);
 }
 
 
 
 
 /* ______________________Data Transmission________________________________________________________________________ */
 
 function copyData() {
	 let copyText = $(mutlifailDataBoxID);
	 copyText.select();
	 copyText.setSelectionRange(0, 99999);
	 document.execCommand("copy");
	 alert("Copied the text: " + copyText.innerHTML);
 } 
   
 function dataString(formData){
	 let object = {};
	 formData.forEach((value, key) => {
		 if(!Reflect.has(object, key)){
			 object[key] = value;
			 return;
		 }
		 if(!Array.isArray(object[key])){
			 object[key] = [object[key]];    
		 }
		 object[key].push(value);
	 });
	 return JSON.stringify(object);
 }
 
 function sendData(){
	 console.log("send Data");
	 $(blockerText).innerHTML="<b>Please keep the window open</b><br><br>Submitting Data";
	 goToPage(blocker);
 
 
	 const request = new XMLHttpRequest();
	 request.onreadystatechange = function() {
		 if (this.readyState == 4){
			 if(this.status == 200) {
				 console.log("successfully transmitted data "+this.statusText);
				 silentClose(true);
				 advanceCounter();
				 goToPage(successPage);
				 reward();
			 }
			 else if(this.status==413 && sentCount==1){
				 console.log("server connection fine, but upload too big "+this.statusText);
				 sendData(request,getMinimalData(formID));
				 sentCount++;
			 }
			 else{
				 if(sentCount<3)
					 populateFailPage(failPage,request);
				 else{
					 silentClose(true);
					 populateFailPage(multifailPage,request);
				 }
				 sentCount++;
			 }
		 }
	 };
 
	 transmit(request,getAllData(formID));
 }
 
 function populateFailPage(pageID, request){
	 $(pageID+failCodeID).innerHTML=request.status+" "+request.statusText;
	 $(pageID+attemptCounterID).innerHTML=""+sentCount;
 
	 let form=getMinimalData(formID);
	 let minStr=dataString(form);
	 form=getAllData(formID);
	 let fullStr=dataString(form);
	 setLink(fullStr);
	 $(mutlifailDataBoxID).innerHTML=minStr;
	 goToPage(pageID);
 }
 
 function transmit(request, data){
	 request.open('POST', processingSite, true);
	 request.send(data);
 }
 function getMinimalData(id){
 
	 let now=Date.now();
	 addData("SentTime",now); //append multiples for every time the user tries to resend
 
	 let form = new FormData($(id));
 
	 form.append("PageVersion",pageVersion);
	 form.append("UserVersion",versionKey);
 
	 form.append("PageStartTime",pageLoadTime);
	 form.append("ViewerStartTime",viewerGetStartTime());
	 form.append("ViewerLoadDelay",viewerGetStartTime()-pageLoadTime);
	 form.append("TotalTime",now-pageLoadTime);
 
	 form.append("ViewerInch",viewerGetWidthInches());
	 form.append("ViewerPix",viewerGetWidth());
	 addClickData(form,3,3);
	 return form;
 }
 function getAllData(id){
	 let form=getMinimalData(id);
	 addScrollData(form,1,3);
	 form.append("shaderLog",viewerGetShaderLog(',','\n'));
	 //addScrollData(form,1,2);
	 //addClickData(form,1,2);
	 return form;
 }
 
 function addScrollData(formData, from, to){
	 for (let i=from; i<=to; i++)
		 formData.append('scroll'+i,viewerGetScrollTimesFor(i,',','\n'));
 }
 function addClickData(formData, from, to){
	 for (let i=from; i<=to; i++)
		 formData.append('click'+i,viewerGetClicksFor(i,',','\n'));
 }
 
 function setLink(body){
	 body=encodeURIComponent(body);
	 let e=mailTo;
	 let s='Data'
	 let textFull=$(multifailReturnAddressFullID).innerHTML;
	 let textAddr=$(multifailReturnAddressID).innerHTML;
	 $(multifailReturnAddressID).innerHTML="<a href=\"mailto:"+e+"\">"+textAddr+"</a>";
	 $(multifailReturnAddressFullID).innerHTML="<a href=\"mailto:"+e+"?subject="+s+"&body="+body+"\">"+textFull+"</a>";
 }
 
 
 
 /* ______________________Init________________________________________________________________________ */
 
 // ######################## sizes #########################
 
 //called when page loads
 function initCommonValues(){
	 setSizes(.9,.66,512);
	 setFeed("");
	 setCounter(currentStage);
 }
 //called when viewer loads (if loading pages properly with 'goToPageWithViewer')
 var viewerValuesInitiated=false;
 function initViewerValues(){
	 if(viewerValuesInitiated)
		 return;
	 viewerValuesInitiated=true;
 
	 createShaders(); //defined in custom file
 }
 
 /* Set the viewer and question box sizes
	mgn - is the margin around the screen
	vewr - is the percentage of available size the viewer wants
	min - is the smallest pixel size for the viewer
	 */
 function setSizes(mgn, vewr, min){
	 let avlW = window.innerWidth * mgn;
	 let avlH = window.innerHeight * mgn; 
 
	 let tmpV=avlW*vewr;
	 if(tmpV<avlH){ //scale to available width if height is no issue
		 viewerWidth=tmpV;
		 formWidth=tmpV/2;
	 }
	 else{ //scale to available height if that is the constraint
		 viewerWidth=avlH;
		 formWidth=avlH/2;
	 }
 
	 if(viewerWidth<min){ //make sure the min size is observed
		 viewerWidth=min;
		 formWidth=min/2;
	 }
	 
	 viewerWidth=Math.ceil(viewerWidth);
	 formWidth=Math.ceil(formWidth);
 
	 initSizes();
 }
 
 function initSizes(){
	 if($(viewer).style.display=="none"){
		 $(form).style.width=px(formWidth+viewerWidth);
		 $(viewer).style.width="0px";
		 $(viewer).style.height=px(viewerWidth);
		 $(form).style.height=px(viewerWidth);
	 }
	 else{
		 $(viewer).style.width=px(viewerWidth);
		 $(form).style.width=px(formWidth);
		 $(viewer).style.height=px(viewerWidth);
		 $(form).style.height=px(viewerWidth);
	 }
	 $(sidePadding).style.width=px(Math.ceil((window.innerWidth-(viewerWidth+formWidth))/2));
 }
 
 function setFeed(msg){
	 let e=$(feed)
	 if(e!=null){
		 e.innerHTML=msg;
		 if(msg.length==0){
			 e.style.width=px(0);
			 $(stageCounter).style.width=px(viewerWidth+formWidth);
		 }
		 else{
			 $(stageCounter).style.width=px(formWidth);
			 e.style.width=px(viewerWidth);
		 }
	 
	 }
 }
 
 function advanceCounter(){setCounter(++currentStage);}
 function setCounter(stage){
	 let i=1;
	 let s=""
	 if(stage>totalStages){
		 s+='<span style="color:green;">';
		 for(;i<=totalStages; ++i)
			 s+="&nbsp;"+i;
		 s+='</span>';
	 }
	 else{
		 if(stage>1){
			 s+='<span style="color:green;">';
			 for(;i<stage; ++i)
				 s+="&nbsp;"+i;
			 s+='</span>';
		 }
		 s+="&nbsp;<b>"+i+"</b>";
		 if(i<totalStages){
			 i++;
			 s+='<span style="color:red;">';
			 for(;i<=totalStages;++i)
				 s+="&nbsp;"+i;
			 s+='&nbsp;</span>';
		 }
	 }
	 let e=$(stageCounter);
	 e.innerHTML='<span style="font-size:120%;">'+s+'</span>';
 
	 if($(feed).innerHTML.length==0)
		 e.style.width=px(viewerWidth+formWidth);
	 else
		 e.style.width=px(formWidth);
	 e.style.textAlign="center";
	 
 }
 
 