<?php

function print_usage()
{
   fwrite(STDERR,
    "USAGE:\n"
         . " php format_release_info.php -filever filever_tag changelog_section, version_tag, changelog_path, github_repo\n"
         . " OR \n"
         . " php format_release_info.php -json jsondatafile changelog_section, version_tag, changelog_path, github_repo\n"
         );         
}

if ($argc < 7) {
   fwrite(STDERR, "Invalid arguments:\n");
   print_usage();
   exit(1);
}

$load_from = $argv[1];
if(!in_array($load_from,["-filever","-json"]))
{
   print_usage();
   exit(1);
}
if($load_from == "-filever")
{
   $filever_tag = $argv[2]; //10.0.0 or 10.0.0_nightly123
   
   $files = [
               [
                   "file_name" => "ffdec_${filever_tag}_setup.exe",
                   "ossupport" => ["windows"],
                   "type_name" => "Installer (Windows)",
                   "type_icon" => "setup"
               ],
               [
                   "file_name" => "ffdec_${filever_tag}.zip",
                   "ossupport" => ["windows", "linux", "macosx"],
                   "type_name" => "ZIP (Windows, Linux, Mac OS)",
                   "type_icon" => "zip"
               ],
               [
                   "file_name" => "ffdec_${filever_tag}.deb",
                   "ossupport" => ["linux"],
                   "type_name" => "DEB package (Linux)",
                   "type_icon" => "deb"
               ],
               [
                   "file_name" => "ffdec_${filever_tag}.pkg",
                   "ossupport" => ["macosx"],
                   "type_name" => "Mac OS X Installer (pkg)",
                   "type_icon" => "osx"
               ],
               [
                   "file_name" => "ffdec_${filever_tag}_macosx.zip",
                   "ossupport" => ["macosx"],
                   "type_name" => "Mac OS X Application (zipped)",
                   "type_icon" => "zip"
               ],
               /*[
                   "file_name" => "ffdec_${filever_tag}_lang.zip",
                   "ossupport" => ["java"],
                   "type_name" => "Language pack for translators (zipped)",
                   "type_icon" => "zip"
               ],*/
               [
                   "file_name" => "ffdec_lib_${filever_tag}.zip",
                   "ossupport" => ["java"],
                   "type_name" => "Library only (Java SE) - Zipped",
                   "type_icon" => "zip"
               ],
               [
                   "file_name" => "ffdec_lib_javadoc_${filever_tag}.zip",
                   "ossupport" => [],
                   "type_name" => "Library documentation (HTML Javadoc) - Zipped",
                   "type_icon" => "zip"
               ],
           ];
}
else //-json
{
   $json_file = $argv[2];
   if(!file_exists($json_file)){
      fwrite(STDERR,"Datafile $json_file does not exist\n");
      exit(1);
   }
   $files = json_decode(file_get_contents($json_file),true);
   if($files === null)
   {
      fwrite(STDERR,"Cannot load data from $json_file\n");
      exit(1);
   }
}
$changelog_section = $argv[3]; //10.0.0 or "alpha 1" or "Unreleased" or "1.5.0 update 1"
$version_tag = $argv[4]; //"version10.0.0" or "nightly165" or "alpha1" or "version1.8.8u2"
$changelog_path = $argv[5]; // ./CHANGELOG.md
$github_repo = $argv[6]; // "jindrapetrik/jpexs-decompiler"



function get_string_between($x, $before, $after) {
   $pos_before = strpos($x, $before);
   if ($pos_before === false) {
      return false;
   }
   $x = substr($x, $pos_before + strlen($before));
   $pos_after = strpos($x, $after);
   if ($pos_after === false) {
      return false;
   }
   $x = substr($x, 0, $pos_after);
   return $x;
}

function get_string_between_multiple_after($x, $before, array $afters) {
   $ret = false;
   foreach ($afters as $after) {
      $ret = get_string_between($x, $before, $after);
      if ($ret !== false) {
         break;
      }
   }
   return $ret;
}

function get_changelog_section($changelog_file, $section_name) {
   if (!file_exists($changelog_file)) {
      return false;
   }
   $txt = file_get_contents($changelog_file);
   $txt = str_replace("\r\n", "\n", $txt) . "--EOF--";

   $afters = ["\n## [", "\n[", "--EOF--"];
   $x = get_string_between_multiple_after($txt, "\n## [$section_name]", $afters);
   if ($x === false) {
      return false;
   }
   if(trim($x) === "") { //No Unreleased version data for example
      return "";
   }
   $x = substr($x, strpos($x, "\n") + 1); //from start of the line
   return $x;
}

$do_images = true;

if($do_images)
{
  $ossupport_map=[
  "windows" => "Works on Windows",
  "linux" => "Works on Linux",
  "macosx" => "Works with macOS",
  "java" => "Works on java"
  ];
}else{
$ossupport_map=[
  "windows" => "Win",
  "linux" => "Lin",
  "macosx" => "Mac",
  "java" => "Java"
  ];
}


$ICONS_URL = "https://github.com/jindrapetrik/jpexs-decompiler/wiki/images";

$body = "";

$is_prerelease = $changelog_section === "Unreleased";

if($is_prerelease) {
   $body .= "## Prerelease WARNING\n".
            "**This is prerelease nightly version. It should *NOT* be considered as stable.**\n\n";
}   

$body .= "## Downloads:\n".
      "\n".
      "| Name | File | OS |\n".
      "|---|---|---|\n";      



$footer_links = [];
foreach ($files as $f) {
   if($do_images) {      
      $footer_links[$f["type_icon"]."_icon"] = $ICONS_URL."/downloads/16/".$f["type_icon"].".png";   
   }
   $footer_links[$f["file_name"]] = 'https://github.com/'.$github_repo.'/releases/download/'.$version_tag.'/'.$f["file_name"];
   $body .= "| **".$f["type_name"]."** | ";
   if($do_images)
   {
      $body .= "![".$f["type_name"]."][".$f["type_icon"]."_icon]";   
   }
   $body .= " [".$f["file_name"]."] | ";
   $ossup_titles = [];
   foreach ($f["ossupport"] as $ossupport)
   {
      if($do_images)
      {
         $footer_links[$ossupport."_icon"] = $ICONS_URL."/os/24/".$ossupport.".png";         
      }
      $ossupport_title = $ossupport_map[$ossupport];
      $ossup_titles[] = $ossupport_title;
      if($do_images)
      {
         $body .= "![".$ossupport_title."][".$ossupport."_icon]";
      }
   }
   if(!$do_images)
   {
      $body .= implode(", ",$ossup_titles);
   }
   $body .= " |\n";
}

$all_ossupport = [];
foreach ($files as $f) {
   if (!in_array($f["ossupport"], $all_ossupport)) {
      $all_ossupport[] = $f["ossupport"];
   }
}

$changelog_data = get_changelog_section($changelog_path,$changelog_section);
if($changelog_data === false)
{
   if($is_prerelease)
   {
      //[Unreleased section may be missing]
      $changelog_data = "";
   }
   else
   {
      fwrite(STDERR, "Cannot load changelog data\n");
      exit(1);
   }   
}
$full_changelog = file_get_contents($changelog_path);
if(preg_match_all('/\[([^\]]+)\][^(]/', $changelog_data."\n",$m))      
{
   $references = $m[1];
   foreach($references as $r)
   {
      if(preg_match("/\[".preg_quote($r,"/")."\]:(.*)/", $full_changelog,$m2)){
         $referenced_link = trim($m2[1]);
         $footer_links[$r] = $referenced_link;         
      }
   }   
}


$body .= "\n";
if($is_prerelease)
{
   $body .= "## What's new since last stable version:\n";
}else{
   $body .= "## What's new:\n";
}
if($changelog_data === "")
{
   $body .= "No notable changes yet\n";
}

$body .= $changelog_data;
$body .= "\n";
foreach($footer_links as $title=>$link)
{
   $body .= "[$title]: $link\n";
}

echo $body;
