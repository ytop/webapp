@echo off
echo Starting build process...

rem Install client dependencies
echo Installing client dependencies...
cd client
call npm install

rem Build client
echo Building client application...
call npm run build:prod

rem Return to root directory
cd ..

echo Build process completed!
