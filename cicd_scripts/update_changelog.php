<?php

/**
 * This script generates changelog from commit messages
 * and increases version number.
 */

function print_usage()
{
   fwrite(STDERR,
    "USAGE:\n"
         . " php update_changelog.php true/false\n"
         . "\n"  
         . " true = is stable, false = is nightly\n"
         . " When it is stable it prints new version name on output."
         );         
}

if ($argc < 2) {
   fwrite(STDERR, "Invalid arguments:\n");
   print_usage();
   exit(1);
}

if (!in_array($argv[1], ["true", "false"], true)) {
   fwrite(STDERR, "Invalid arguments:\n");
   print_usage();
   exit(1);
}

$nightly = $argv[1] === "false";

$result = 0;
exec('git tag -l "version*" --sort=-v:refname', $output, $result);
$version1 = trim($output[0]);
$version2 = "HEAD";

if ($result !== 0) {
    echo "cannot git tag";
    exit(1);
}

$output = [];
$result = 0;
exec('git log ' . $version1 . '..' . $version2 . ' --pretty=format:"%s%n%b%n---SPLIT---" --reverse', $output, $result);
if ($result !== 0) {
    echo "cannot git log";
    exit(1);
}


$messages = [];
$message_lines = [];
foreach ($output as $line) {
    $line = trim($line);        
    if ($line === "---SPLIT---") {
        $messages[] = $message_lines;
        $message_lines = [];    
    } else {
        $message_lines[] = $line;
    }
}


$changelog_types = [
    "feat" => "Features",
    "fix" => "Bug Fixes",
    "perf" => "Performance Improvements"
];                

$changelog = [

];

foreach (array_keys($changelog_types) as $type) {
    $changelog[$type] = [];
}

$has_breaking_change = false;

foreach ($messages as $message) {
    $first_line = $message[0];


    if (preg_match('/^(?<type>feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\((?<scope>.+)\))?(?<breaking>!)?: (?<desc>.+)$/', $first_line, $matches)) {
        $type = $matches["type"];
        $scope = $matches["scope"];
        $desc = $matches["desc"];
        $breaking = $matches["breaking"] === "!";
        
        if ($breaking) {
            $has_breaking_change = true;
        }

        if ($scope !== "") {
            $desc = $scope . ": " . $desc;
        }

        if (array_key_exists($type, $changelog_types)) {
            $changelog[$type][] = $desc;
        }
    }       
}       

$result = "";
$empty_changelog = true;
foreach ($changelog as $type => $descs) {
    if (count($descs) === 0) {
        continue;
    }
    $empty_changelog = false;
    $result .= "\r\n";
    $result .= "### " . $changelog_types[$type] . "\r\n";
    foreach ($descs as $desc) {
        $result .= "- " . $desc . "\r\n";
    }
}
$result = trim($result) . "\r\n";

$changelog_file = "CHANGELOG.md";

$changelog_data = file_get_contents($changelog_file);

$changelog_data = preg_replace('/\r?\n/', "\r\n", $changelog_data);

$after = "\r\n## [";

preg_match('/^version(?<major>[0-9]+)\.(?<minor>[0-9]+)\.(?<patch>[0-9]+)$/', $version1, $matches);

$major = (int) $matches["major"];
$minor = (int) $matches["minor"];
$patch = (int) $matches["patch"];

if ($has_breaking_change) {
    $major++;
    $minor = 0;
    $patch = 0;
} else if (count($changelog["feat"]) > 0) {
    $minor++;
    $patch = 0;
} else if (count($changelog["fix"]) > 0 || count($changelog["perf"]) > 0) {
    $patch++;
} else {
    $nightly = true;
}

$new_version = "$major.$minor.$patch";

if ($nightly) {
    $version_title = "Unreleased";
    $version_date_add = "";
} else {
    $version_title = $new_version;
    $version_date_add = " - " . date("Y-m-d");
}

$insert = "\r\n## [$version_title]$version_date_add\r\n" . $result;

$after_pos = mb_strpos($changelog_data, $after);
if ($after_pos !== false) {
    $changelog_data = mb_substr($changelog_data, 0, $after_pos) . $insert . mb_substr($changelog_data, $after_pos);
}

file_put_contents($changelog_file, $changelog_data);    

if ($empty_changelog) {
    echo "empty";
    exit;
}

if (!$nightly) {
    echo $version_title;
    exit;
}

echo "nightly";