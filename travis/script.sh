#!/bin/bash
#stop on error
set -e

VERSION_PROP_FILE="version.properties"

# If we've got website password, we can upload nightly builds.
# Travis secure variable $website_password is not available from outside 
# of jpexs repository (e.g pull requests from other users on GitHub)

if [ -z ${GITHUB_ACCESS_TOKEN+x} ]; then
  # password not set,  just make private release without publishing result
  ant all
else
  # if tag set
  if [ -n "$TRAVIS_TAG" ]; then
    #tag starts with "version" prefix
    if [[ $TRAVIS_TAG =~ ^version.* ]] ; then
    
      #generate prop file
      VERSION_NUMBER=`echo $TRAVIS_TAG|sed 's/version//'`
      
      VERSION_MAJOR=`echo $VERSION_NUMBER|cut -d '.' -f 1`
      VERSION_MINOR=`echo $VERSION_NUMBER|cut -d '.' -f 2`
      VERSION_RELEASE=`echo $VERSION_NUMBER|cut -d '.' -f 3`        
      
      echo "">$VERSION_PROP_FILE
      echo "major=$VERSION_MAJOR">>$VERSION_PROP_FILE
      echo "minor=$VERSION_MINOR">>$VERSION_PROP_FILE
      echo "release=$VERSION_RELEASE">>$VERSION_PROP_FILE
      echo "build=0">>$VERSION_PROP_FILE
      echo "revision=$TRAVIS_COMMIT">>$VERSION_PROP_FILE
      echo "debug=false">>$VERSION_PROP_FILE
             
      #compile, build, create files
      ant new-version
            
      # release standard version based on tag
      export DEPLOY_TAG_NAME=$TRAVIS_TAG
      export DEPLOY_VERSION_NAME="version $VERSION_NUMBER"
      export DEPLOY_DESCRIPTION=`php ./travis/format_release_info.php -filever $VERSION_NUMBER $VERSION_NUMBER $DEPLOY_TAG_NAME ./CHANGELOG.md "$TRAVIS_REPO_SLUG"`
      export DEPLOY_COMMITISH="master"
      export DEPLOY_PRERELEASE=false
      export DEPLOY_FILEVER_TAG="$VERSION_NUMBER"          
      export DO_DEPLOY=1
    else
      # regular build
      ant all            
    fi
  else
    #if we are on dev branch and it's not a pull request
    if [ $TRAVIS_BRANCH = "dev" ] && [ $TRAVIS_PULL_REQUEST = "false" ]; then       
      # create nightly build...
      
      TAGGER_NAME="Travis CI"
      TAGGER_EMAIL=travis@travis-ci.org          
              
      TAG_COMMIT_HASH=$TRAVIS_COMMIT
      GITHUB_REPO=$TRAVIS_REPO_SLUG
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
      echo "revision=$TRAVIS_COMMIT">>$VERSION_PROP_FILE
      echo "debug=true">>$VERSION_PROP_FILE
      
      #compile, build, create files
      ant new-version
                        
      CURRENT_DATE=`date +%Y-%m-%dT%H:%M:%SZ`

      ESC_TAGGER_NAME=`echo $TAGGER_NAME|jq --raw-input --ascii-output '.'`        
      TAG_NAME=$NEXT_NIGHTLY_TAG
      VERSION_PRERELEASE=true
              
      #Create tag
      echo "Creating tag $TAG_NAME..."
      echo '{"tag":"'$TAG_NAME'","message":"","object":"'$TAG_COMMIT_HASH'","type":"commit","tagger":{"name":'$ESC_TAGGER_NAME',"email":"'$TAGGER_EMAIL'","date":"'$CURRENT_DATE'"}}'>json.bin
      curl --silent --request POST --data-binary @json.bin --header "Content-Type: application/json" --header "Accept: application/vnd.github.manifold-preview" --user $GITHUB_USER:$GITHUB_ACCESS_TOKEN https://api.github.com/repos/$GITHUB_REPO/git/tags>/dev/null
      
      export DEPLOY_FILEVER_TAG="${VERSION_NUMBER}_nightly${NEXT_NIGHTLY_VER}"                          
      export DEPLOY_RELEASE_TO_REMOVE=$LAST_NIGHTLY_TAG                                 
      export DEPLOY_TAG_NAME=$NEXT_NIGHTLY_TAG
      export DEPLOY_VERSION_NAME="(PREVIEW) version $LAST_STABLE_VER nightly $NEXT_NIGHTLY_VER"
      export DEPLOY_DESCRIPTION=`php ./travis/format_release_info.php -filever $DEPLOY_FILEVER_TAG Unreleased $DEPLOY_TAG_NAME ./CHANGELOG.md "$TRAVIS_REPO_SLUG"`
      export DEPLOY_COMMITISH="dev"
      export DEPLOY_PRERELEASE=true
      export DO_DEPLOY=1
    else
      #tag not set - regular build
      ant all                    
    fi  
  fi    
fi
