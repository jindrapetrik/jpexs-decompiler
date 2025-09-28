#!/usr/bin/env bash
#stop on error
set -e

VERSION_PROP_FILE="version.properties"

# If we've got website password, we can upload nightly builds.
# CICD secure variable $website_password is not available from outside 
# of jpexs repository (e.g pull requests from other users on GitHub)

if [ -z ${GITHUB_ACCESS_TOKEN+x} ]; then
  # password not set,  just make private release without publishing result
  echo "No password set, making private release..."
  JAVA_HOME=$JAVA_HOME_8_X64
  ant all
  # Javadoc generation is buggy with Java 8, lets generate it with Java 21
  JAVA_HOME=$JAVA_HOME_21_X64
  ant javadoc
else
  # if tag set
  if [ $CICD_REFTYPE = "tag" ]; then
    #tag starts with "version" prefix
    if [[ $CICD_REFNAME =~ ^version.* ]] ; then
      echo "Version tag, creating new version..."
    
      #generate prop file
      VERSION_NUMBER=`echo $CICD_REFNAME|sed 's/version//'`
      
      VERSION_MAJOR=`echo $VERSION_NUMBER|cut -d '.' -f 1`
      VERSION_MINOR=`echo $VERSION_NUMBER|cut -d '.' -f 2`
      VERSION_RELEASE=`echo $VERSION_NUMBER|cut -d '.' -f 3`        
      
      echo "">$VERSION_PROP_FILE
      echo "major=$VERSION_MAJOR">>$VERSION_PROP_FILE
      echo "minor=$VERSION_MINOR">>$VERSION_PROP_FILE
      echo "release=$VERSION_RELEASE">>$VERSION_PROP_FILE
      echo "build=0">>$VERSION_PROP_FILE
      echo "revision=$CICD_COMMIT">>$VERSION_PROP_FILE
      echo "debug=false">>$VERSION_PROP_FILE
           
      JAVA_HOME=$JAVA_HOME_8_X64

      #compile, build, create files
      ant new-version

      # Javadoc generation is buggy with Java 8, lets generate it with Java 21
      JAVA_HOME=$JAVA_HOME_21_X64
      ant release_lib_javadoc
            
      # release standard version based on tag
      export DEPLOY_TAG_NAME=$CICD_REFNAME
      export DEPLOY_VERSION_NAME="version $VERSION_NUMBER"
      export DEPLOY_DESCRIPTION=`php ./cicd_scripts/format_release_info.php -filever $VERSION_NUMBER $VERSION_NUMBER $DEPLOY_TAG_NAME ./CHANGELOG.md "$CICD_REPO_SLUG"`
      export DEPLOY_COMMITISH="master"
      export DEPLOY_PRERELEASE=false
      export DEPLOY_FILEVER_TAG="$VERSION_NUMBER"          
      export DO_DEPLOY=1
    else
      # regular build
      echo "Other tag, regular build..."
      ant all            
    fi
  else        
    #if we are on $NIGHTLY_BRANCH branch and it's not a pull request
    if [ "$CICD_REFNAME" == "$NIGHTLY_BRANCH" ] && [ "$CICD_EVENTNAME" != "pull_request" ]; then             
      echo "On $NIGHTLY_BRANCH branch and no pull request, creating nightly..."
      # create nightly build...
      
      TAGGER_NAME=$CICD_NAME
      TAGGER_EMAIL=$CICD_EMAIL       
              
      TAG_COMMIT_HASH=$CICD_COMMIT
      GITHUB_REPO=$CICD_REPO_SLUG
      echo "Getting new version tag and name..."
      RELEASES_JSON=`curl --silent --request GET --header "Accept: application/vnd.github.manifold-preview" --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases`
      LAST_NIGHTLY_VER=`echo $RELEASES_JSON|jq --raw-output '.[].tag_name'|grep 'nightly'|sed 's/nightly//'|head -n 1`
      LAST_STABLE_VER=`echo $RELEASES_JSON|jq --raw-output '.[].tag_name'|grep 'version'|sed 's/version//'|head -n 1`          
      NEXT_NIGHTLY_VER=$(($LAST_NIGHTLY_VER+1))
      LAST_NIGHTLY_TAG=nightly$LAST_NIGHTLY_VER
      NEXT_NIGHTLY_TAG=nightly$NEXT_NIGHTLY_VER
      
      
      #generate prop file
      VERSION_NUMBER=$LAST_STABLE_VER     
      VERSION_MAJOR=`echo $VERSION_NUMBER|cut -d '.' -f 1`
      VERSION_MINOR=`echo $VERSION_NUMBER|cut -d '.' -f 2`
      VERSION_RELEASE=`echo $VERSION_NUMBER|cut -d '.' -f 3`                    
      
      echo "">$VERSION_PROP_FILE
      echo "major=$VERSION_MAJOR">>$VERSION_PROP_FILE
      echo "minor=$VERSION_MINOR">>$VERSION_PROP_FILE
      echo "release=$VERSION_RELEASE">>$VERSION_PROP_FILE
      echo "build=$NEXT_NIGHTLY_VER">>$VERSION_PROP_FILE
      echo "revision=$CICD_COMMIT">>$VERSION_PROP_FILE
      echo "debug=true">>$VERSION_PROP_FILE
      
      JAVA_HOME=$JAVA_HOME_8_X64
      #compile, build, create files
      ant new-version

      # Javadoc generation is buggy with Java 8, lets generate it with Java 21
      JAVA_HOME=$JAVA_HOME_21_X64
      ant release_lib_javadoc
                        
      CURRENT_DATE=`date +%Y-%m-%dT%H:%M:%SZ`

      ESC_TAGGER_NAME=`echo $TAGGER_NAME|jq --raw-input --ascii-output '.'`        
      TAG_NAME=$NEXT_NIGHTLY_TAG
      VERSION_PRERELEASE=true
              
      #Create tag
      echo "Creating tag $TAG_NAME..."
      echo '{"tag":"'$TAG_NAME'","message":"","object":"'$TAG_COMMIT_HASH'","type":"commit","tagger":{"name":'$ESC_TAGGER_NAME',"email":"'$TAGGER_EMAIL'","date":"'$CURRENT_DATE'"}}'>json.bin
      curl --silent --request POST --data-binary @json.bin --header "Content-Type: application/json" --header "Accept: application/vnd.github.manifold-preview" --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/git/tags>/dev/null
      
      echo "Tag created"            
      export DEPLOY_FILEVER_TAG="${VERSION_NUMBER}_nightly${NEXT_NIGHTLY_VER}"                          
      export DEPLOY_RELEASE_TO_REMOVE=$LAST_NIGHTLY_TAG                                 
      export DEPLOY_TAG_NAME=$NEXT_NIGHTLY_TAG
      export DEPLOY_VERSION_NAME="(PREVIEW) version $LAST_STABLE_VER nightly $NEXT_NIGHTLY_VER"
      export DEPLOY_DESCRIPTION=`php ./cicd_scripts/format_release_info.php -filever $DEPLOY_FILEVER_TAG Unreleased $DEPLOY_TAG_NAME ./CHANGELOG.md "$CICD_REPO_SLUG"`
      export DEPLOY_COMMITISH=$NIGHTLY_BRANCH
      export DEPLOY_PRERELEASE=true
      export DO_DEPLOY=1      
    else
      #tag not set - regular build
      echo "Other branch or pull request, regular build..."
      ant all                    
    fi  
  fi    
fi

if [ "$DO_DEPLOY" == 1 ]; then
  echo "Deploying..."
  GITHUB_REPO=$CICD_REPO_SLUG

  
  DEPLOY_ATTACH_FILES_JSON='[
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'_setup.exe","content_type":"application/exe"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'.zip","content_type":"application/zip"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'.deb","content_type":"application/vnd.debian.binary-package"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'.pkg","content_type":"application/x-newton-compatible-pkg"},
  {"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'_macosx.zip","content_type":"application/zip"}, 
  {"file_name":"ffdec_lib_'$DEPLOY_FILEVER_TAG'.zip","content_type":"application/zip"},
  {"file_name":"ffdec_lib_javadoc_'$DEPLOY_FILEVER_TAG'.zip","content_type":"application/zip"}
  ]';

  #{"file_name":"ffdec_'$DEPLOY_FILEVER_TAG'_lang.zip","content_type":"application/zip"},
    
  echo "Creating release..."
  ESC_VERSION_NAME=`echo $DEPLOY_VERSION_NAME|jq --raw-input --ascii-output '.'`
  ESC_VERSION_DESCRIPTION=`echo "$DEPLOY_DESCRIPTION"|jq --raw-input --slurp --ascii-output '.'`
            
  echo '{"tag_name":"'$DEPLOY_TAG_NAME'","target_commitish":"'$DEPLOY_COMMITISH'","name":'$ESC_VERSION_NAME',"body":'"$ESC_VERSION_DESCRIPTION"',"draft":false,"prerelease":'$DEPLOY_PRERELEASE'}'>json.bin
  json=`curl --silent --request POST --data-binary @json.bin --header "Content-Type: application/json" --header "Accept: application/vnd.github.manifold-preview" --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases`
  RELEASE_ID=`echo "$json"|jq '.id'`
  
  

  #Attaching files...
  echo "Attaching files..."
  NUM_FILES=`echo "$DEPLOY_ATTACH_FILES_JSON"|jq ".|length"`
  
  set +e
  MAX_RETRY=10;
  NUM_RETRY=0
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
    CURL_STATUS=$?
    if [ "$CURL_STATUS" != 0 ]; then
        echo "UPLOAD FAILED on CURL Error ${CURL_STATUS}";
        if [ "$CURL_STATUS" != 55 ] && [ "$CURL_STATUS" != 56 ]; then
            echo "Status ${CURL_STATUS} is other than ignored 55 or 56, aborting..." 1>&2
            exit 1;
        fi
        if [ "$NUM_RETRY" == "$MAX_RETRY" ]; then
            echo "Max retry reached, aborting" 1>&2
            exit 1;
        fi
        i=$((i-1))
        NUM_RETRY=$((NUM_RETRY+1))
        echo "..retrying again (retry ${NUM_RETRY})"
    else
        NUM_RETRY=0
    fi
    #wait few seconds to not DDOS GitHub
    sleep 2
  done
        
  if [ -n "$DEPLOY_RELEASE_TO_REMOVE" ]; then
    #Remove old nightly
    echo "Removing old release $DEPLOY_RELEASE_TO_REMOVE..."
    #-remove release
    TAG_INFO=`curl --silent --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases/tags/$DEPLOY_RELEASE_TO_REMOVE`
    RELEASE_ID=`echo $TAG_INFO|jq '.id'`    
    curl --silent --request DELETE --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/releases/$RELEASE_ID>/dev/null
    # wait few seconds before DELETE properly propagates so we can delete tag then
    sleep 5
    #delete tag
    curl --silent --request DELETE --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/git/refs/tags/$DEPLOY_RELEASE_TO_REMOVE>/dev/null        
  fi  
  echo "FINISHED"
  exit 0
fi
