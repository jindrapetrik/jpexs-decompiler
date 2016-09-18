#!/bin/bash
set -e
git add . -u
git commit -m "Saving files before refreshing line endings"
git rm --cached -r .
git reset --hard
git add .
set +e
git commit -m "Normalize all the line endings"