#!/bin/bash
# If we've got website password, we can upload nightly builds.
# Travis secure variable $website_password is not available from outside 
# of jpexs repository (e.g pull requests from other users on GitHub)
if [ -z ${website_password+x} ]; then 
    # create nightly build
    ant new-version-nightlybuild
else
    # just make private release without publishing result    
    ant all
fi