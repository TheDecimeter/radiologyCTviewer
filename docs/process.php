<?php

$all="";

$all.= "User IP,".$_SERVER['REMOTE_ADDR']."\n";
$all.="Server Date,".date("Y-m-d")."\n";
$all.="Server Time,".date("H:i:s")."\n";

/* get server time zone, if possible */
if (ini_get('date.timezone')) {
    $all.="Server Timezone,".ini_get('date.timezone') . "\n";
}else{
    date_default_timezone_set('America/Denver');
	$serverTimezone=ini_get('date.timezone');
	if(strlen($serverTimezone>0))
    		$all.="Server Timezone,".$serverTimezone . "\n";
	else
    		$all.="Server Timezone,Unknown\n";
}


$all.="\n";

$all.=addLine('PageVersion','Page Code Version');
$all.=addLine('UserVersion');
$all.="\n";

$all.=addLine('PageStartTime');
$all.=addLine('ViewerStartTime');
$all.=addLine('ViewerLoadDelay');
$all.=addLine('TotalTime');
$all.=addLine('SentTime');
$all.="\n";

$all.=addLine('ViewerInch');
$all.=addLine('ViewerPix');
$all.="\n";

$all.=pDem();
$all.=pWndTrl();
$all.=pSmpTrl();
$all.=pFinal();

$all.="\n\n";
$all.=addLists("click",3,3);
$all.=addLists("scroll",1,3);
$all.=addLine('shaderLog',"#### Windowing Log ####",false);

$all.="\n\n";

//debugging, print everything we got
// foreach ($_POST as $key => $value) {
//     $all.=htmlspecialchars($key)." --- ".htmlspecialchars($value)."\n";
// }

$dataID = getName("test");
$all = "File,".$dataID."\n".$all;
/* save the data on the server */
$fdw = fopen($dataID, "w+");
fwrite($fdw, $all);
fclose($fdw);



function pDem(){
    $ret="-----Demographics------\n";
    $ret.=addLine('demAge');
    $ret.=addLine('demBrd', 'board certified');
    $ret.=combineOrderedValues('demExp',5,'Expertise');
    $ret.=addLine('demExpOtherTxt',"Other Expertise");
    $ret.="\n";
    return $ret;
}

function pWndTrl(){
    $ret="-----Windowing trial------\n";

    $ret.=addLine('pWndTrl_Start');
    $ret.=addLine('pWndTrl_End');
    $ret.=addLine('pWndTrl_Duration');

    $ret.=addLine('wndTrlNone',"Indicated no abnormalities");
    $ret.=combineAllOrderedValues('wndTrlDgnsVals','Abnormality Diagnoses');
    $ret.="\n";
    return $ret;
}

function pSmpTrl(){
    $ret="-------Simple trial-------\n";

    $ret.=addLine('pSmpTrl_Start');
    $ret.=addLine('pSmpTrl_End');
    $ret.=addLine('pSmpTrl_Duration');

    $ret.=combineOrderedValues('pSmpTrlAb',5,'Abnormalities Found');
    $ret.=addLine('pSmpTrlAbOtherTxt',"Other Abnormality");
    $ret.="\n";
    return $ret;
}
function pFinal(){
    $ret="-------Final page-------\n";
    $ret.=addLine('commentBox');
    $ret.="\n";
    return $ret;
}

/**
 * Don't call, use <code>randNum</code> instead.
 */
function initRand ()
{
    static $randCalled = false;
    if (!$randCalled){
	$num=ip2long($_SERVER['REMOTE_ADDR']);
	if($num)
        	srand((double) microtime() * 1000000 + $num);
	else
        	srand((double) microtime() * 1000000);

        $randCalled = false;
    }
}
function randNum()
{
    initRand();
    $rNum = rand();
    return $rNum;
}

function getName($experimentName){
    $num=randNum();
    $experimentName = "data/".$experimentName."_".date("Y-m-d")."_".date("H:i:s")."_";
    while(file_exists($experimentName.strval($num).".csv"))
       $num++;
    return $experimentName.strval($num).".csv";
}

function addLists($listName, $from, $to, $newName=""){
    if($newName=="")
        $newName=$listName;

    $ret="####" . $newName."####\n";
    for ($i = $from; $i <= $to; $i++) {
        if(isset($_POST[$listName . strval($i)]))
            $ret.="\n __". $newName . strval($i). "__\n" . $_POST[$listName . strval($i)] . "\n";
        else
            $ret.="\n __". $newName . strval($i). "__\n unset\n";
    } 
    return $ret;
}

/**
 * Set all values with <code>$name</code> plus an index
 * until you reach the maximum index.
 * Use for check boxes
 */
function combineOrderedValues($name, $till, $newName=""){
    if($newName=="")
        $newName=$name;

	$vals="";
	for ($i = 1; $i<=$till; $i++){
        if(isset($_POST[$name . strval($i)]))
            $vals.=$_POST[$name . strval($i)] . ";";
    }
    
	if($vals=="")
        return $newName . ", unset\n";
    if(strpos($vals, ',')!==false)
            return $newName . ",\"" . $vals . "\"\n";
        else
            return $newName . "," . $vals . "\n";
}
/**
 * Set all values with <code>$name</code> plus an index
 * until you come to the first unset value.
 * DO NOT USE THIS WITH CHECK BOXES
 */
function combineAllOrderedValues($name,$newName=""){
    if($newName=="")
        $newName=$name;

	$vals="";
	for ($i = 1;; $i++){
        if(isset($_POST[$name . strval($i)]))
            $vals.=$_POST[$name . strval($i)] . ";";
        else
            break;
    }
    
	if($vals=="")
        return $newName . ", unset\n";
    if(strpos($vals, ',')!==false)
            return $newName . ",\"" . $vals . "\"\n";
        else
            return $newName . "," . $vals . "\n";
}
/**
 * Combine all values with a key which contains <code>$name</code>.
 * This checks every incomming value, so it is more expensive 
 * than <code>combineAllOrderedValues</code> or
 * <code>combineOrderedValues</code>
 */
function combineAllValuesWith($name,$newName=""){
    if($newName=="")
        $newName=$name;

    $vals="";
    foreach ($_POST as $key => $val) {
        if(strpos($key, $name)!==false)
            $vals.=$val . ";";
    }

    if($vals=="")
        return $newName . ", unset\n";
    if(strpos($vals, ',')!==false)
            return $newName . ",\"" . $vals . "\"\n";
        else
            return $newName . "," . $vals . "\n";
}

/**
 * return a new line of information found at $name
 * if a $newName is specified, rename it.
 * if $sameRow is false, insert a line break between
 *    the name and value, otherwise, separate them with
 *    a comma.
 */
function addLine($name, $newName="", $sameRow = true){
    if($newName=="")
        $newName=$name;
    
    $sep="\n";
    if($sameRow)
        $sep=",";

    $vals="";
	if(isset($_POST[$name])){
		$vals=$_POST[$name];
    }
    if($vals=="")
        return $newName . ", unset\n";
    if(strpos($vals, ',')!==false && $sameRow)
            return $newName . $sep . "\"" . $vals . "\"\n";
        else
            return $newName . $sep . $vals . "\n";
}
?>