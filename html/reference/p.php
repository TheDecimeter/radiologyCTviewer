<html>
<body>

Welcome <?php echo $_POST["name"]; ?><br>

Your email address is: <?php echo $_POST["email"]; ?><br>

from: <?php echo $_SERVER['REMOTE_ADDR']; ?>
<br><br>
<?php echo $_POST["key"]; ?>
<br><br>
<?php echo $_POST["clicks1"]; ?>


<?php

function initRand ()
{
    static $randCalled = FALSE;
    if (!$randCalled)
    {
	$num=ip2long($_SERVER['REMOTE_ADDR']);
	if($num)
        	srand((double) microtime() * 1000000 + $num);
	else
        	srand((double) microtime() * 1000000);

        $randCalled = TRUE;
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
    $experimentName .= "_".$_SERVER['REMOTE_ADDR']."_".date("Y-m-d")."_".date("H:i:s")."_";
    while(file_exists($experimentName.strval($num).".txt"))
       $num+=1;
    return $experimentName.strval($num).".txt";
}


/*$clicks=str_replace(";", "\n", $_POST["clicks1"]);*/
$clicks=$_POST["clicks1"];

$email = "";
$email .= $clicks ."\n". $_SERVER['REMOTE_ADDR'] ."\n". $_POST["name"];


$dataID = getName("sdkfjs");

/* Saves the data into a file */
$fileName=$dataID;
$fdw = fopen($fileName, "w+");
fwrite($fdw, $email);
fclose($fdw);

/* Email the data */
mail('spawnofthepilgrims@gmail.com','New Data'.$dataID,$email);

?>

</body>
</html> 
