<!DOCTYPE html>
<html>
       <head>
              <title>ct-viewer</title>
    	      <link rel="shortcut icon" href="favicon.ico">
              <meta name="robots" content="noindex">
              <meta http-equiv="content-type" content="text/html; charset=UTF-8">
              <meta id="gameViewport" name="viewport" content="width=device-width initial-scale=1">
              <link href="styles.css" rel="stylesheet" type="text/css">
              <script src="soundmanager2-setup.js"></script>
  		<script src="soundmanager2-jsmin.js"></script>

              <!--classes-->
  		<script src="scripts/stack.js"></script>
  		<script src="scripts/firework.js"></script>
  		<script src="scripts/ruler.js"></script>

              <!--other scripts and functions and whatnot-->
  		<script src="scripts/common.js"></script>
  		<script src="scripts/viewerEvents.js"></script>
              <script src="scripts/custom.js"></script>

       </head>

       <body>
              <!--This won't show if javascript is enabled -->
              <noscript style="font-size: 300%;">Please Enable JavaScript for this page to work.<br><a href="https://www.enable-javascript.com/">Click here to learn how.</a></noscript>

              <table> 
                     <tr><td rowspan="3" id="sidePadding"></td></tr><!--some reason the usual centering method wasn't working so this is expanded as needed at load time-->
                     <tr>
                            <td>
                                   <table>
                                          <tr>
                                                 <td>
                                                        <div class="center" id='feedbackConsole'></div>
                                                 </td>
                                                 <td>
                                                        <div class="center" id='stageCounter'></div>
                                                 </td>
                                          </tr>
                                   </table>
                            </td>
                     </tr>
                     <tr>
                            <td>
                                   <table>
                                          <tr>
                                                 <td>
                                                        <div id="viewerWindow" style="display:none;">
                                                               <div id="embed-html"></div><!--viewer area-->
                                                        </div>
                                                 </td>
                                                 <td class="formOuter">
                                                        <div id="formDiv" class="formHousing"><!--viewer area-->
                                                               <form id="questions" action="p.php" method="post">

                                                                      <!--CONSENT PAGE -->
                                                                      <div id="pConsent"    style="display:block;" class="formPage">
                                                                             <button onclick="enterTrial();" type="button">Skip to<br>viewer page</button><br>
                                                                             <br>
                                                                             This could be a consent page.
                                                                             <br>
                                                                             <h3>BACKGROUND</h3>
                                                                             Before you decide to participate it is important for you to understand why the research is being done and what it will involve. Please take time to read the following information carefully so that you can decide whether or not you would like to volunteer to take part in this rese...
                                                                             <br><br>
                                                                             ...
                                                                             <br><br>
                                                                             Outside of the normal consent stuff you may also wish to include whether or not information is secure (without https, it definitely isn't) so include warnings not to put anything private in the text fields.
                                                                      
                                                                             <br><br>
                                                                             <!-- Consent Question -->
                                                                             <div id="consentContinue" oninput="validPageConsent(false);" class="formQst" style="margin-left:50px;">
                                                                                    I agree to participate<br>
                                                                                    <span class="formRadio">
                                                                                           <label>
                                                                                                  <input type="radio"  id="consentContinue1" name="consentContinue" value="Yes">
                                                                                                  Yes
                                                                                           </label>
                                                                                    </span><br>
                                                                                    <span class="formRadio">
                                                                                           <label>
                                                                                                  <input type="radio" id="consentContinue2" name="consentContinue" value="No">
                                                                                                  No
                                                                                           </label>
                                                                                    </span>
                                                                             </div>

                                                                             <br>
                                                                             <div id="pagpConsentErr" class="errorMsg"> Please check highlighted questions.</div>

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button class="arrowFont" onclick="if(validPageConsent()){if(cTrue('consentContinue1')){silentClose(false);goToPage('pDem');advanceCounter();}else{silentClose(true);goToPage('pThanks');}}" type="button">&rarr;</button></td></tr></table>
                                                                      </div>

                                                                      <!--BLOCKER PAGE if the user needs the viewer before it is ready, this lets them know it's comming 
                                                                             also used for submitting data-->
                                                                      <div id="blocker"    class="formPage">
                                                                             <span id="blockerText">Loading CT-Viewer</span>
                                                                             <br><br>
                                                                             <div id="blockerConsole"></div>
                                                                             <table><tr><td style="width:40%;"></td><td>
                                                                                    <div class="loader"></div>
                                                                             </td><td style="width:40%;"></td></tr></table>
                                                                      </div>

                                                                      <!--EMPTY THANKYOU PAGE there are several thankyou pages, this one is if they click no on the consent form-->
                                                                      <div id="pThanks"    class="formPage"> 
                                                                             Thank you for your time.
                                                                      </div>

                                                                      <!--DEMOGRAPHICS PAGE -->
                                                                      <div id="pDem"    class="formPage" oninput="validPageDemRedo();">

                                                                             
                                                                             <h3>Demographics</h3>

                                                                             <br><br><br>
                                                                             <div id="demAgeOuter" class="formQst">
                                                                                    What is your age (in years)?<br>
                                                                                    <input type="number" min="1" max="150" id="demAge" name="demAge" oninput="err(isRange(this),'Exact Values Please',true,this);isNum(this);">
                                                                             </div>

                                                                             <br><br><br><br>
                                                                             <!-- Board Certification Question -->
                                                                             <div id="demBrd" oninput="isChecked(this,1,2);" class="formQst">
                                                                                    Are your board certified (or your country's equivilent)?<br>
                                                                                    <span class="formRadio">
                                                                                           <label>
                                                                                                  <input type="radio"  id="demBrd1" name="demBrd" value="Yes">
                                                                                                  Yes
                                                                                           </label>
                                                                                    </span><br>
                                                                                    <span class="formRadio">
                                                                                           <label>
                                                                                                  <input type="radio" id="demBrd2" name="demBrd" value="No">
                                                                                                  No
                                                                                           </label>
                                                                                    </span>
                                                                             </div>

                                                                             <br><br><br><br>

                                                                             <!-- Expertise Question -->
                                                                             <div id="demExp" oninput="isChecked(this,1,5);" class="formQst">
                                                                                    What is your area of expertise (select all which apply)?<br>
                                                                                    <span class="formCheckBox">
                                                                                           <label> Abdominal
                                                                                                  <input type="checkbox" id="demExp1" name="demExp1" value="Abdominal" onclick="clearChecks('demExp',5,5);isValidOther('demExp4','demExpOtherTxt');">
                                                                                                  <span class="checkmark"> </span>
                                                                                           </label>
                                                                                    </span><br>
                                                                                    <span class="formCheckBox">
                                                                                           <label> Breast
                                                                                                  <input type="checkbox" id="demExp2" name="demExp2" value="Breast" onclick="clearChecks('demExp',5,5);isValidOther('demExp4','demExpOtherTxt');">
                                                                                                  <span class="checkmark"> </span>
                                                                                           </label>
                                                                                    </span><br>
                                                                                    <span class="formCheckBox">
                                                                                           <label> Chest
                                                                                                  <input type="checkbox" id="demExp3" name="demExp3" value="Chest" onclick="clearChecks('demExp',5,5);isValidOther('demExp4','demExpOtherTxt');">
                                                                                                  <span class="checkmark"> </span>
                                                                                           </label>
                                                                                    </span><br>
                                                                                    <span id="demExpOther" class="formCheckBox">
                                                                                           <label> Other
                                                                                                  <input type="checkbox" id="demExp4" name="demExp4" value="Other"onclick="clearChecks('demExp',5,5);isValidOther('demExp4','demExpOtherTxt',false);">
                                                                                                  <span class="checkmark"></span>
                                                                                           </label>

                                                                                           <input type="text" id="demExpOtherTxt" name="demExpOtherTxt" 
                                                                                                  oninput="if(updateCheckOther(this,'demExp4'))clearChecks('demExp',5,5);isValidOther('demExp4','demExpOtherTxt');">
                                                                                    </span><br>
                                                                                    <span class="formCheckBox">
                                                                                           <label> None
                                                                                                  <input type="checkbox" id="demExp5" name="demExp5" value="None" onclick="clearChecks('demExp',1,4);">
                                                                                                  <span class="checkmark"> </span>
                                                                                           </label>
                                                                                    </span>
                                                                                    
                                                                             </div>
                                                                             <br>
                                                                             <div id="pagpDemErr" class="errorMsg"> Please check highlighted questions.</div>

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button class="arrowFont" onclick="if(validPageDem())enterTrial();" type="button">&rarr;</button></td></tr></table>
                                                                      </div>

                                                                      <!--WINDOWING TRIAL PAGE -->
                                                                      <div id="pWndTrl"    class="formPage">
                                                                             <br><br>
                                                                             <!-- these shaders are made in custom.js when the viewer loads -->
                                                                             <label>Windowing:
                                                                                    <select id="windows1" oninput="setShader(this.value);">
                                                                                           <optgroup label="Common">
                                                                                                  <option value="ab">Abdomen</option>
                                                                                                  <option value="bone">Bone</option>
                                                                                                  <option value="felsenbein">Felsenbein</option>
                                                                                                  <option value="fullDynamic">Full Dynamic</option>
                                                                                                  <option value="lung">Lung</option>
                                                                                                  <option value="mediastinum">Mediastinum</option>
                                                                                                  <option value="postmyelo">Post Myelogram</option>
                                                                                                  <option value="skull">Skull</option>
                                                                                           </optgroup>
                                                                                           <optgroup label="Custom Demo">
                                                                                                  <option value="bl">Blue</option>
                                                                                                  <option value="sq">Square</option>
                                                                                                  <option value="rt">Square Root</option>
                                                                                           </optgroup>
                                                                                           <optgroup label="Other">
                                                                                                  <option value="user1">Extra Slot</option>
                                                                                                  <option value="off" selected="selected">None</option>
                                                                                           </optgroup>
                                                                                    </select> 
                                                                             </label>
                                                                             <br><br>
                                                                             <div>
                                                                                    <div id="winTrlSetNone" class="errorMsg">
                                                                                           Select a value from the drop down to be reset.<br><br>
                                                                                           <q>None</q> can't be reset.
                                                                                    </div>
                                                                                    Change (in HU)
                                                                                    <button onclick="
                                                                                           if(!err($('windows1').value == 'off','winTrlSetNone') && isNum('customWindowingTextW') && isNum('customWindowingTextL')){
                                                                                                         makeShader($('windows1').value,'customWindowingTextL','customWindowingTextW');}" 
                                                                                           type="button"> Set</button>
                                                                                    <div id="customWindowingNoRange" class="errorMsg">
                                                                                           Exact values please
                                                                                    </div>
                                                                                    <br>
                                                                                    <div id="customWindowShaderMaker" style="padding:1%;">
                                                                                           L <input type="text" style="width:4em;" id="customWindowingTextL" oninput="err(isRange(this),'customWindowingNoRange');isNum(this,true,true);">
                                                                                           W <input type="text" style="width:4em;" id="customWindowingTextW" oninput="err(isRange(this),'customWindowingNoRange');isNum(this,true,true);">
                                                                                    </div>
                                                                             </div>
                                                                             <br>
                                                                             <div>
                                                                                    Click Setting:<br>
                                                                                    <span class="formRadio">
                                                                                           <label>
                                                                                                  <input type="radio" name="wndTrlClickSetting" onclick="rulerStart('wndTrlRlrOut',false);" checked>
                                                                                                  Selector
                                                                                           </label>
                                                                                    </span>
                                                                                    <span class="formRadio">
                                                                                           <label>
                                                                                                  <input type="radio" name="wndTrlClickSetting" onclick="rulerStart('wndTrlRlrOut',true);">
                                                                                                  Ruler
                                                                                           </label>
                                                                                    </span>
                                                                             
                                                                                    <div id="wndTrlRlrOut"></div>
                                                                             </div>
                                                                             <br><br>
                                                                             <div id="winTrlInst" class="formQst" style="text-align:center;line-height:1.5em;">
                                                                                    Either<br>
                                                                                    <b>Click on an abnormality<br>to diagnose it</b><br>
                                                                                    or<br> <b>Click:</b> 
                                                                                    <span class="formCheckBox">
                                                                                           <label> <b>None Found</b>
                                                                                                  <input type="checkbox" id="wndTrlNone" name="wndTrlNone" value="true" onclick="validPageWndTrl(this.checked);">
                                                                                                  <span class="checkmark"> </span>
                                                                                           </label>
                                                                                    </span><br>
                                                                             </div>
                                                                             <br>          
                                                                             <div id="wndTrlDgns"></div>

                                                                             <br>
                                                                             <div id="winTrlClkNone" class="errorMsg">
                                                                                    Either click on cancer in the image, or select <q>None Found</q> above.
                                                                             </div>
                                                                             <div id="winTrlDgns" class="errorMsg">
                                                                                    Please add a diagnosis for each click.
                                                                             </div>
                                                                             <div id="winTrlClkBoth" class="errorMsg">
                                                                                    You selected <q>None Found</q> <b><u>and also</u></b> clicked potential tumors in the image.<br>
                                                                                    <button onclick="$('wndTrlNone').checked=false;validPageWndTrl(false);" type="button">Uncheck <q>None Found</q></button> or
                                                                                    <button onclick="resetClicksFor(3);" type="button"> Remove Clicks from image</button>
                                                                             </div>
                                                                             <div id="winTrlScrl" class="directionMsg">
                                                                                    You can use the scroll wheel, arrows, or drag over the image to scroll through it.
                                                                             </div>

                                                                             <br>
                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button class="arrowFont" onclick="if(validPageWndTrl()){addAll('wndTrlDgns','wndTrlDgnsVals','wndTrlDgnsSubClass');exitTrial();setShader('off');}" type="button">&rarr;</button></td></tr></table>
                                                                      </div>

                                                                      <!--WINDOWING TRIAL PAGE FINISHED -->
                                                                      <div id="pWndTrlFin"    class="formPage">
                                                                             You have completed trial <span id="pWndTrlDone"></span>, click the arrow to proceed.

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button class="arrowFont" onclick="enterTrial();" type="button">&rarr;</button></td></tr></table>
                                                                      </div>

                                                                      <!--SIMPLE TRIAL PAGE -->
                                                                      <div id="pSmpTrl"    class="formPage">


                                                                             <!-- Abnormality Question -->
                                                                             <div id="pSmpTrlAb" oninput="isChecked(this,1,5);validPageSmpTrl(false);" class="formQst">
                                                                                    <div style="line-height:1.2em;">
                                                                                           <b>Which abnormalities are present in this case (check all that apply)?</b><br>
                                                                                    </div>
                                                                                    <div id="pSmpTrlAbShuffle"><!--shuffle the inputs (leave "Other" and "None" in their proper order at the bottom)-->
                                                                                           <span class="formCheckBox">
                                                                                                  <label> Lung Nodule(s)
                                                                                                         <input type="checkbox" id="pSmpTrlAb1" name="pSmpTrlAb1" value="LungNodule(s)" onclick="clearChecks('pSmpTrlAb',5,5);isValidOther('pSmpTrlAb4','pSmpTrlAbOtherTxt');">
                                                                                                         <span class="checkmark"> </span>
                                                                                                  </label>
                                                                                                  <br>
                                                                                           </span>
                                                                                           <span class="formCheckBox">
                                                                                                  <label> Lymphadenopathy
                                                                                                         <input type="checkbox" id="pSmpTrlAb2" name="pSmpTrlAb2" value="Lymphadenopathy" onclick="clearChecks('pSmpTrlAb',5,5);isValidOther('pSmpTrlAb4','pSmpTrlAbOtherTxt');">
                                                                                                         <span class="checkmark"> </span>
                                                                                                  </label>
                                                                                                  <br>
                                                                                           </span>
                                                                                           <span class="formCheckBox">
                                                                                                  <label> Breast Mass
                                                                                                         <input type="checkbox" id="pSmpTrlAb3" name="pSmpTrlAb3" value="BreastMass" onclick="clearChecks('pSmpTrlAb',5,5);isValidOther('pSmpTrlAb4','pSmpTrlAbOtherTxt');">
                                                                                                         <span class="checkmark"> </span>
                                                                                                  </label>
                                                                                                  <br>
                                                                                           </span>
                                                                                    </div>
                                                                                    <span>
                                                                                           <span id="pSmpTrlAbOtherUnspecified" class="errorMsg">
                                                                                                  Please Specify Other Abnormalities <br>
                                                                                           </span>
                                                                                           <span id="pSmpTrlAbOther" class="formCheckBox">
                                                                                                         <label> Other
                                                                                                                <input type="checkbox" id="pSmpTrlAb4" name="pSmpTrlAb4" value="Other"onclick="clearChecks('pSmpTrlAb',5,5);isValidOther('pSmpTrlAb4','pSmpTrlAbOtherTxt',false);">
                                                                                                                <span class="checkmark"></span>
                                                                                                         </label>

                                                                                                         <input type="text" id="pSmpTrlAbOtherTxt" name="pSmpTrlAbOtherTxt" 
                                                                                                                oninput="if(updateCheckOther(this,'pSmpTrlAb4'))clearChecks('pSmpTrlAb',5,5);isValidOther('pSmpTrlAb4','pSmpTrlAbOtherTxt');"
                                                                                                                onfocus="useWASD=false;"
                                                                                                                onblur="useWASD=true;">
                                                                                                  
                                                                                           </span>
                                                                                           <br>
                                                                                    </span>
                                                                                    <span class="formCheckBox">
                                                                                           <label> None
                                                                                                  <input type="checkbox" id="pSmpTrlAb5" name="pSmpTrlAb5" value="None" onclick="clearChecks('pSmpTrlAb',1,4);">
                                                                                                  <span class="checkmark"> </span>
                                                                                           </label>
                                                                                    </span>
                                                                                    
                                                                             </div>
                                                                             <br>
                                                                             <div id="smpTrlScrl" class="directionMsg">
                                                                                    You can use the scroll wheel, arrows, or drag over the image to scroll through it.
                                                                             </div>
                                                                             <div id="smpTrlErr" class="errorMsg"> Please check highlighted questions.</div>

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button class="arrowFont" onclick="if(validPageSmpTrl())exitTrial();" type="button">&rarr;</button></td></tr></table>
                                                                      </div>

                                                                      <!--SIMPLE TRIAL PAGE FINISHED -->
                                                                      <div id="pSmpTrlFin"    class="formPage">
                                                                             You have completed trial <span id="pSmpTrlDone"></span>, click the arrow to proceed.

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button class="arrowFont" onclick="enterTrial();" type="button">&rarr;</button></td></tr></table>
                                                                      </div>

                                                                      <!--FINAL FINISH PAGE -->
                                                                      <div id="pFinal"    class="formPage">
                                                                      <br>
                                                                             Please enter any questions, comments, or concerns about the study here.
                                                                             <br><br>
                                                                             <label>
                                                                                    Comments:<br>
                                                                                    <textarea rows="10" id="commentsBox" name="commentBox" style="width:98%" onfocus="useWASD=false;" onblur="useWASD=true;"></textarea>
                                                                             </label>
                                                                             <br><br><br><br>
                                                                             <div style="text-align:center;"><h3>Press <q>Submit</q> when finished.</h3></div>

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button onclick="sendData();" type="button">Submit</button></td></tr></table>
                                                                      </div>

                                                                      <!--SUBMIT FAIL PAGE -->
                                                                      <div id="pFail"    class="formPage">
                                                                             <br><br>
                                                                             <h1>Submission Failed</h2>
                                                                             <br>
                                                                             Please <b>keep this page open to retain your data</b> and try submitting again later.
                                                                             <br><br>
                                                                             This is normally caused by a temporary interruption in the network.
                                                                             <br><br>
                                                                             Attempts: <span id="pFailAttempts"></span>
                                                                             <br><br>
                                                                             Error Code: <span id="pFailCode"></span>

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button onclick="sendData();" type="button">Try&nbsp;Again</button></td></tr></table>
                                                                      </div>

                                                                      <!--SUBMIT MULTIFAIL PAGE -->
                                                                      <div id="pMultiFail"    class="formPage">
                                                                             <br><br>
                                                                             <h1>Submission Failed</h2>
                                                                             <br>
                                                                             The server has consistantly failed to respond. You are welcome to keep trying, or 
                                                                             simply <span id="pMultiFailReturn">email the following data to me.</span>
                                                                             <br><br>
                                                                             Thankyou for participating, sorry for the inconvenience.
                                                                             <br><br>
                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button onclick="sendData();" type="button">Try&nbsp;Again</button></td></tr></table>
                                                                             <br>
                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button onclick="copyData();" type="button">Copy</button></td></tr></table>
                                                                             <label>Data:<br>
                                                                                    <textarea readonly rows="10" id="pMultiFailData" style="width:100%"></textarea>
                                                                             </label>
                                                                             <br><br>
                                                                             <span id="pMultiFailReturnFull">You may also click here to load all the data into your email client.</span>

                                                                             <br><br>
                                                                             Attempts: <span id="pMultiFailAttempts"></span>
                                                                             <br><br>
                                                                             Error Code: <span id="pMultiFailCode"></span>
                                                                      </div>

                                                                      <!--SUBMISSION SUCCESS PAGE with minor debrief-->
                                                                      <div id="pSuccess"    class="formPage">
                                                                             <br>
                                                                             <h1>Success</h2>
                                                                             Thankyou for participating.
                                                                             <br>
                                                                             <h3>Debrief</h3>
                                                                             The goal of this study was to demonstrate simple and complex use of an online study with a ct viewer.
                                                                             <br>
                                                                             <h3>Feedback</h3>
                                                                             <br>
                                                                             Trial 1 <span id="dbf0"></span>
                                                                             <br><br>
                                                                             Trial 2 <span id="dbf1"></span>
                                                                             <br><br><br>

                                                                             <table style="width:99%"><tr><td style="width:99%"></td><td><button onclick="moreReward();" type="button">This&nbsp;is&nbsp;all&nbsp;the thanks&nbsp;I&nbsp;get?!</button></td></tr></table>
                                                                      </div>

                                                                      <!--DEBRIEF PAGE FOR THE WINDOWING TRIAL -->
                                                                      <div id="pWndDbf"    class="formPage">
                                                                             <button class="arrowFont" onclick="goToPageWithViewer(successPage,4,0);viewerClickLock(false);setShader('off');" type="button">&larr;</button>
                                                                             <br><br>
                                                                             <h2>Full Debrief:</h2>
                                                                             <br>

                                                                             This set contains 3 tumors.. maybe, I'm not a doctor.
                                                                             <br><br>
                                                                             <button onclick="highlightClick(3,256,223,19,'lung');" type="button">Tumor A (slide 19)</button> <span id="pWndDbfFnd1"></span>
                                                                             <br><br>
                                                                             <button onclick="highlightClick(3,342,290,19,'lung');" type="button">Tumor B (slide 19)</button> <span id="pWndDbfFnd2"></span>
                                                                             <br><br>
                                                                             <button onclick="highlightClick(3,578,228,23,'lung');" type="button">Tumor C (slide 23)</button> <span id="pWndDbfFnd3"></span>
                                                                             <br><br>
                                                                             <span id="pWndDbfFnd4"></span>
                                                                             <br><br>
                                                                             Some disclaimer about how the above results aren't always accurate because it's a cheap means of checking and it's
                                                                             always possible something new was found that I didn't know about.
                                                                      </div>

                                                               </form>
                                                        </div>
                                                 </td>
                                          </tr>
                                   </table>
                            </td>
                     </tr>
              </table>

              <script type="text/javascript" src="html/html.nocache.js"></script>
       </body>

       <script>
              shuffleInputs('pSmpTrlAbShuffle'); //shuffle the check boxes on the simple slide


<?php
//version queries with length <= 6 are hard coded to a particular combination
//  even length: complex windowing trial first, odd: simple first.
//  no query or query longer than 6 are alternating, or random.
$v="nothing"; //Note that this is a 7 character word on purpose.
if(isset($_GET["v"]))
	$v=trim($_GET["v"],"\"");

$orderIndex= -1;
$key="_".$v;

$t1="['pWndTrl',3,'contained 3 tumors <button onclick=\"pdbf0();\" type=\"button\">View</button>']";
$t2="['pSmpTrl',1,'contained 0 abnormalities.']";

if(strlen($v)>6){
       $fn = "use.txt";
       $fp=fopen($fn,'r+');
       if ($fp && flock($fp, LOCK_EX|LOCK_NB)) {
              //opened, and locked, alternate between trial types.
              $fc = fread($fp,filesize($fn));
              $orderIndex = intval($fc);
              $key.='_'.$fc;

              $nextOrder =$orderIndex+1;
              if($nextOrder>=2)
                     $nextOrder=0;
              $fc=strval($nextOrder);

              rewind($fp);         //note this will only overwrite the value completely
              fwrite($fp,$fc);     // if it is 1 byte long
              flock($fp,LOCK_UN);
       }
       else{
              //failed to open or lock file, pick randomly in javascript
              $key.='_x';
       }
       fclose($fp);
}
else if(strlen($v)%2==1){//odd
       $orderIndex=1;
}
else{
       $orderIndex=0;
}
echo "let key = '".$key."';\n";
switch($orderIndex){
       case -1:
              echo "let trials = [", $t1, ",", $t2, "];\n";
              echo "shuffle(trials);\n";
       break;
       case 0:
              echo "let trials = [", $t1, ",", $t2, "];\n";
       break;
       case 1:
              echo "let trials = [", $t2, ",", $t1, "];\n";
       break;

}


?>

              let trialStartTime=0, trialIndex=0, finalPage='pFinal';
              // let trials=[['pWndTrl',3,'contained 3 tumors <button onclick="pdbf0();" type="button">View</button>']
              //            ,['pSmpTrl',1,'contained 0 abnormalities.']]; //list of tiral pages and associated viewer slide sets
              // shuffle(trials);

              let trialOrder="";
              for(let i=0; i<trials.length;++i){ //(the last page never gets seen)
                     $(trials[i][0]+'Done').innerHTML=""+(i+1)+" of "+trials.length;
                     $('dbf'+i).innerHTML=trials[i][2];
                     trialOrder+=trials[i][0]+";";
              }

              const versionKey=trialOrder+key;
              
              function enterTrial(){
                     let t=trials[trialIndex];
                     goToPageWithViewer(t[0],t[1],0);
                     trialStartTime=viewerGetUpTime();
                     advanceCounter();
              }
              function exitTrial(){
                     hideViewer();
                     let trialEndTime=viewerGetUpTime();

                     let t = trials[trialIndex];

                     addData(t[0]+"_Start",trialStartTime);
                     addData(t[0]+"_End",trialEndTime);
                     addData(t[0]+"_Duration",trialEndTime-trialStartTime);

                     if(trialIndex==trials.length-1){
                            goToPage(finalPage);
                            advanceCounter();
                     }
                     else{
                            goToPage(t[0]+"Fin");
                            trialIndex++;
                     }
              }

              //set up page and common values
              initCommonValues();

              document.getElementById('gameViewport').setAttribute('content',
                 'width=device-width initial-scale=' + 1/window.devicePixelRatio);

              function handleMouseDown(evt) {
                evt.preventDefault();
                evt.stopPropagation();
                evt.target.style.cursor = 'default';
                window.focus();
              }

              function handleMouseUp(evt) {
                evt.preventDefault();
                evt.stopPropagation();
                evt.target.style.cursor = '';
              }

              

              document.getElementById('embed-html').addEventListener('mousedown', handleMouseDown, false);
              document.getElementById('embed-html').addEventListener('mouseup', handleMouseUp, false);

              errorColor = getComputedStyle(document.body).getPropertyValue('--error-color');
       </script>
</html>