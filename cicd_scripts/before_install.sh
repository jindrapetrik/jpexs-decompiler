#!/usr/bin/env bash

# Exit immediately on first error
set -e
apt-get -qq update    
apt-get install -y jq
apt-get install -y curl

tools_dir=$CICD_TEMP/tools

#Create tools.properties with paths to NSIS and launch4j

MAKENSIS_FULLPATH=`which makensis`
MAKENSIS_PATH=`dirname "$MAKENSIS_FULLPATH"`

echo "nsis.path = $MAKENSIS_PATH" > tools.properties
echo "launch4j.path = -" >> tools.properties

cat tools.properties

# Secure variable $website_password is not available from outside 
# of jpexs repository (e.g pull requests from other users on GitHub)
if ! [ -z ${website_password+x} ]; then 
  # Store username and password for uploading releases to jpexs server
  echo "username=$website_user" > jpexs_website.properties
  echo "password=$website_password" >> jpexs_website.properties
fi
