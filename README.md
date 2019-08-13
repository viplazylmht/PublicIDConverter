# Public ID Converter

This is a tool for Android that can convert public IDs when porting smali files.

  

## Features

- **Search** all smali files on a smali directory for all IDs contained in smali files

- **Convert** all IDs within smali files to new IDs

- Implement guilde port (for convert smali step)

  

## First initializing

- Give _storage_ permission

- This tool use _OI File Manager_ as **file picker**, if device have problem with in-app install, please install this file manager normally.

  

For more about _OI File Manager_, visit http://www.openintents.org/filemanager/

  

## How to use

- Make sure you have enough free space memory on **Internal Storage**

- Get **Source Public** from guide port

- Decompile your apk that will be port

- Follow guide port, add resources, recompile, decompile again to get **Port Public**

- Parsing **smali Directory** from guide as **SMALI DIR**, do not use smali from your apk because it may be _very **big**_.
  
- Enter **Find IDs** and see the Log

- Compare Log with guide to make sure that will be correct.

- Enter **Convert** and checkout Log again 

- **Smali-NEW** may be output to _/sdcard/PublicIDConverter/$DATE-TIME/_ but you can see full path in Log too.

- If this tool convert _more/less_ than you need, or incorrect, just go to **Smali-NEW** and fix normally.

- Put **Smali-NEW** to _apk smali directory_ and recompile, sign, done.