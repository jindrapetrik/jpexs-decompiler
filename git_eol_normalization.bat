@echo This will normalize line endings in the GIT repository
@pause
git add . -u
@if %errorlevel% neq 0 goto failed
git commit -m "Saving files before refreshing line endings"
@if %errorlevel% neq 0 goto failed
git rm --cached -r .
@if %errorlevel% neq 0 goto failed
git reset --hard
@if %errorlevel% neq 0 goto failed
git add .
@if %errorlevel% neq 0 goto failed
git commit -m "Normalize all the line endings"
@rem No errorlevel check here - the commit can be empty
@goto okay
:failed
@echo ERROR: Something FAILED
@goto finish
:okay
@echo SUCCESS
:finish
@pause