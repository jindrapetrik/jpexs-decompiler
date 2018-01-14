#!/bin/bash
#stop on error
set -e

if [ "$DO_DEPLOY" = 1 ]; then
  echo "Deploying..."
  GITHUB_REPO=$TRAVIS_REPO_SLUG

  echo "Constructing release body message..."
  
  DEPLOY_ATTACH_FILES_JSON='[
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'_setup.exe","content_type":"application/exe","works_with":["windows"],"type_name":"Installer (Windows)","type_icon":"setup"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'.zip","content_type":"application/zip","works_with":["windows","linux","macosx"],"type_name":"ZIP (Windows, Linux, Mac OS)","type_icon":"zip"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'.deb","content_type":"application/vnd.debian.binary-package","works_with":["linux"],"type_name":"DEB package (Linux)","type_icon":"deb"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'.pkg","content_type":"application/x-newton-compatible-pkg","works_with":["macosx"],"type_name":"Mac OS X Installer (pkg)","type_icon":"osx"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'_macosx.zip","content_type":"application/zip","works_with":["macosx"],"type_name":"Mac OS X Application (zipped)","type_icon":"zip"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'_lang.zip","content_type":"application/zip","works_with":["java"],"type_name":"Language pack for translators (zipped)","type_icon":"zip"},
  {"file_name":"ffdec_lib_'$DEPLOY_FILEVER_TAG'.zip","content_type":"application/zip","works_with":["java"],"type_name":"Library only (Java SE) - Zipped","type_icon":"jar"}
  ]';
  
  ALL_TYPE_ICONS=(setup zip deb osx jar)
  
  ICONS_URL=https://github.com/jindrapetrik/jpexs-decompiler/wiki/images
  
  
  FOOTER_TYPE_ICONS=""
  for i in ${ALL_TYPE_ICONS[@]}; do
    FOOTER_TYPE_ICONS=$FOOTER_TYPE_ICONS'['${i}'_icon]: '$ICONS_URL'/downloads/16/'${i}'.png
'
  done
  
  DOWNLOAD_PREFIX='https://github.com/'$GITHUB_REPO'/releases/download/'$DEPLOY_TAG_NAME'/'
  
  FOOTER=''
  BODY='## Downloads:

| Name | File | OS |
|---|---|---|
'
  
  NUM_FILES=`echo "$DEPLOY_ATTACH_FILES_JSON"|jq ".|length"`
  
  WORKS_MAP='{
  "windows":"Works on Windows",
  "linux":"Works on Linux",
  "macosx":"Works with Mac OSX",
  "java":"Works on java"
  }'
  
  WORKS_KEYS=`echo "$WORKS_MAP"|jq --raw-output 'keys[]'`
  
  
  FOOTER_WORK_ICONS=''
      for WORKS in ${WORKS_KEYS[@]}; do     
         FOOTER_WORK_ICONS="$FOOTER_WORK_ICONS[${WORKS}_icon]: $ICONS_URL/images/os/24/${WORKS}.png
"
       done
  
    
  for (( i=0; i<$NUM_FILES; i++ ))
    do              
      ITEM_JSON=`echo "$DEPLOY_ATTACH_FILES_JSON"|jq '.['$i']'`                                  
      FILE_NAME=`echo "$ITEM_JSON"|jq --raw-output '.file_name'`
      TYPE_ICON=`echo "$ITEM_JSON"|jq --raw-output '.type_icon'`
      TYPE_NAME=`echo "$ITEM_JSON"|jq --raw-output '.type_name'`
      BODY=$BODY'| **'$TYPE_NAME'** | !['$TYPE_NAME']['$TYPE_ICON'_icon] ['$FILE_NAME'] | '    
      NUM_WORKS=`echo "$ITEM_JSON"|jq --raw-output '.works_with | length'`
      for (( j=0; j<$NUM_WORKS; j++ ))
       do
         WORKS=`echo "$ITEM_JSON"|jq --raw-output '.works_with['$j']'`
         WORKS_DESCRIPTION=`echo "$WORKS_MAP"|jq --raw-output '.'$WORKS`
         BODY=$BODY"![$WORKS_DESCRIPTION][${WORKS}_icon]"
       done
      BODY=$BODY'
'
      FOOTER=$FOOTER'['$FILE_NAME']: '${DOWNLOAD_PREFIX}${FILE_NAME}'
'
    done
  
  FOOTER=$FOOTER$FOOTER_TYPE_ICONS$FOOTER_WORK_ICONS
  
  FULL_MSG="$BODY
## What's new:
$DEPLOY_DESCRIPTION
$FOOTER"  
  
  
  
  
  echo "Creating release..."
  ESC_VERSION_NAME=`echo $DEPLOY_VERSION_NAME|jq --raw-input --ascii-output '.'`
  ESC_VERSION_DESCRIPTION=`printf "$FULL_MSG"|jq --raw-input --slurp --ascii-output '.'`
            
  echo '{"tag_name":"'$DEPLOY_TAG_NAME'","target_commitish":"'$DEPLOY_COMMITISH'","name":'$ESC_VERSION_NAME',"body":'$ESC_VERSION_DESCRIPTION',"draft":false,"prerelease":'$DEPLOY_PRERELEASE'}'>json.bin
  json=`curl --silent --request POST --data-binary @json.bin --header "Content-Type: application/json" --header "Accept: application/vnd.github.manifold-preview" --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases`
  RELEASE_ID=`echo "$json"|jq '.id'`
  
  

  #Attaching files...
  echo "Attaching files..."
  NUM_FILES=`echo "$DEPLOY_ATTACH_FILES_JSON"|jq ".|length"`
  
  for (( i=0; i<$NUM_FILES; i++ ))
  do              
    ITEM_JSON=`echo "$DEPLOY_ATTACH_FILES_JSON"|jq '.['$i']'`                                  
    CONTENT_TYPE=`echo "$ITEM_JSON"|jq --raw-output '.content_type'`
    FILE_NAME=`echo "$ITEM_JSON"|jq --raw-output '.file_name'`
    FILE_PATH=releases/$FILE_NAME
    echo "Attaching $FILE_PATH ..."
    if [ ! -f $FILE_PATH ]; then
      echo "WARNING: File $FILE_PATH does not exist!"
    fi
    
    curl --silent --request POST --data-binary @$FILE_PATH --header "Content-Type: $CONTENT_TYPE" --header "Accept: application/vnd.github.manifold-preview" --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://uploads.github.com/repos/$GITHUB_REPO/releases/$RELEASE_ID/assets?name=$FILE_NAME>/dev/null
  done
        
  if [ -n "$DEPLOY_RELEASE_TO_REMOVE" ]; then
    #Remove old nightly
    echo "Removing old release $DEPLOY_RELEASE_TO_REMOVE..."
    #-remove release
    TAG_INFO=`curl --silent --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases/tags/$DEPLOY_RELEASE_TO_REMOVE`
    RELEASE_ID=`echo $TAG_INFO|jq '.id'`
    curl --silent --request DELETE --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases/$RELEASE_ID>/dev/null
    #-delete tag
    git config --local user.email "travis@travis-ci.org"
    git config --local user.name "Travis CI"
    git remote add myorigin https://${GITHUB_ACCESS_TOKEN}@github.com/$TRAVIS_REPO_SLUG.git > /dev/null 2>&1
    git tag -d $DEPLOY_RELEASE_TO_REMOVE
    git push --quiet myorigin :refs/tags/$DEPLOY_RELEASE_TO_REMOVE > /dev/null 2>&1        
  fi  
  echo "FINISHED"
fi
