# HCIPlayAndRecord
Final Project for HCI Class

The Android app is ran only on Android Studio 3.4 with Affectiva 3.2
Take a look at the build.Gradle (Module: app) file - the dependencies are absolutely necessary in that way, and break otherwise 
(git issues were raised, and I left a link to the documentation for that if there are questions)
Once installed on your phone, you can record video and analyze any videos from your library. The generated text files contain the 
sad and joy data in their respective files in a folder documented as "myDirectory". This is done for easy access to these files by 
going straight from File Explorer on your computer when plugging in your phone. Also, I am leaving in MetricsPanel code for
building a view that would show all the Affectiva metrics (emotions, facial points, etc) and their relative scores, as
that is an unnecessary but cosmetically appealing feature to be added on after the project is due.


statsAnalysis.py
Python file in this is ran on Python 3.7 with latest version of numpy, scipy, os, and pandas packages.
Takes the txt files created by the app and compares them. This file first splits a reader's video into 10 sections, 
then splits the participant's video into 10 sections, and compares the corresponding sections to each other to see if they have
statistically equal means. If more than 5 sections are equivalent, then I state that as "synchronous" behavior.





Steps to implement Affectiva's video analysis in your own personalized app (will need to be updated for versions later than 3.2):

- go to https://github.com/Affectiva/android-sdk-samples
- open VideoDetectorDemo
- copy VideoDetectorThread.java to your folder
- alter your res/values/strings file to copy the strings from the demo strings.xml file
- do the previous step for res/values.styles.xml file as well











