<?php

function print_usage()
{
   fwrite(STDERR,
    "USAGE:\n"
         . " php check_references.php mdfile\n"
         );         
}

if ($argc < 2) {
   fwrite(STDERR, "Invalid arguments:\n");
   print_usage();
   exit(1);
}

$mdfile = $argv[1];

$data = file_get_contents($mdfile);

if(preg_match_all('/\[([^\]]+)\][^(:]/', $data."\n",$m))      
{
   $references = $m[1];
   foreach($references as $r)
   {
      if(!preg_match("/\[".preg_quote($r,"/")."\]:(.*)/", $data,$m2)){
         echo "Missing reference for [$r] !\n";
      }
   }   
}
