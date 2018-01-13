<?php

function get_string_between($x,$before,$after)
{
  $pos_before=strpos($x,$before);
  if($pos_before===false){
    return false;
  }
  $x=substr($x,$pos_before+strlen($before));
  $pos_after=strpos($x,$after); 
  if($pos_after===false){
    return false;
  }
  $x=substr($x,0,$pos_after);
  return $x;
}

function get_string_between_multiple_after($x,$before,array $afters)
{
     $ret = false;
     foreach($afters as $after)
     {
        $ret = get_string_between($x,$before,$after);
        if($ret !== false){
          break;
        }
     }
     return $ret;
}

if($argc < 2){
 fwrite(STDERR, "Invalid arguments - version required\n");
 exit(1);
}

$version = $argv[1];

$txt=file_get_contents("CHANGELOG.md");
$txt=str_replace("\r\n","\n",$txt)."--EOF--";

$afters = ["\n## [","\n[","--EOF--"];
$x=get_string_between_multiple_after($txt,"\n## [$version]",$afters);
if($x===false){
  //nothing found
  exit;
}
$x=substr($x,strpos($x,"\n")+1); //from start of the line

//remove [] from issue names  [#1234] => Issue #1234
$x=preg_replace('/\[(#[0-9]+)\]/','Issue $1',$x);

//remove markdown headers
$x=preg_replace("/### ([^\n]+)\n/","$1:\n",$x);
$x=str_replace("\n- ","\n",$x); 

echo $x;


