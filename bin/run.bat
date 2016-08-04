@ECHO OFF

ECHO Running Java program to download the spreadsheet and convert to MSE file...
java -jar shadowfist-magicsetedit.jar

REM Set the location where MSE is installed and the mse.exe can be found.
REM For example, SET MSE_HOME=C:\Program Files\Magic Set Editor 2
SET MSE_HOME="D:\Apps\Magic Set Editor 2\"

ECHO Opening the mse-set file with the MSE command line interface...
%MSE_HOME%\mse --cli better-tomorrow-playtest1.mse-set

ECHO Exporting the card images...
write_image_file(set.cards[0], file:"sample_writefile.png")