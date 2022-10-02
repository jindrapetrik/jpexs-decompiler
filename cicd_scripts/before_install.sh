#!/usr/bin/env bash

# Exit immediately on first error
set -e
sudo apt-get -qq update    
#NSIS needs these
sudo apt-get install -y zlib1g-dev
sudo apt-get install -y lib32ncurses5 lib32z1
#For deploying
sudo apt-get install -y jq
sudo apt-get install -y curl
#For parsing changelog
#sudo apt-get install -y php7.0-cli
#commented out: assuming cicd tool already has php cli
# test php installed
php --version

tools_dir=$CICD_TEMP/tools

if [ ! -f "$tools_dir/cached.txt" ]; then    
    sudo apt-get install -y scons
    #For unpacking unzip :-)
    sudo apt-get install -y unzip

    # create directory where tools will be downloaded and installed
    mkdir -p $tools_dir

    echo "cached">$tools_dir/cached.txt

    cp ./cicd_scripts/tools/nsis-3.0-src.tar.bz2 ./
    #Unpack NSIS sources - Tool for making windows installers
    bzip2 -d nsis-3.0-src.tar.bz2
    tar xvf nsis-3.0-src.tar -C $tools_dir >/dev/null  

    #Compile NSIS
    cd $tools_dir/nsis-3.0-src/  
    scons UNICODE=yes SKIPSTUBS=all SKIPPLUGINS=all SKIPUTILS=all SKIPMISC=all NSIS_CONFIG_CONST_DATA=no PREFIX=$tools_dir/nsis-3.0-src/ install-compiler >/dev/null
    mkdir share
    #Make this symbolic link, otherwise it does not work
    ln -s $tools_dir/nsis-3.0-src share/nsis
    cd -  

    #Extract some binary additional sources which NSIS needs and are part of Windows ZIP file
    cp ./cicd_scripts/tools/nsis-3.0-addon.zip ./
    unzip -u nsis-3.0-addon.zip -d $tools_dir/nsis-3.0-src

    #Extract launch4j - tool for creating EXE file from Java
    cp ./cicd_scripts/tools/launch4j-3.12-linux.tgz ./
    tar zxvf launch4j-3.12-linux.tgz -C $tools_dir >/dev/null
fi

#Create tools.properties with paths to NSIS and launch4j
echo "nsis.path = $tools_dir/nsis-3.0-src/bin" > tools.properties
echo "launch4j.path = $tools_dir/launch4j" >> tools.properties

# Secure variable $website_password is not available from outside 
# of jpexs repository (e.g pull requests from other users on GitHub)
if ! [ -z ${website_password+x} ]; then 
  # Store username and password for uploading releases to jpexs server
  echo "username=$website_user" > jpexs_website.properties
  echo "password=$website_password" >> jpexs_website.properties
fi
