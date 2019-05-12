# -*- coding: utf-8 -*-
"""
Created on Tue May  7 16:10:55 2019

@author: User
"""

#%%
import scipy.stats as stats
import numpy as np
import pandas as pd
import os

#%%

#os.chdir("C:\\Jareds_Stuff\\2_SeniorYear\\2_Spring2019\\3_HCI\\finalProject\\newProject\\recordedVideo\\videoData\\sad\\1")
# My version of Anaconda is not recognizing files in different directeries outside of
#  my current folder, so I use the os package to change direct to each folder
#  Also have structure the directories like this because they sort awkwardly and I want
#  to keep file names the same throughout my project (in phone and on computer)
os.chdir("C:\\Jareds_Stuff\\2_SeniorYear\\2_Spring2019\\3_HCI\\finalProject\\newProject\\recordedVideo\\videoData\\sad\\1")
readerSamples = []
participantSamples = []


sadReader1 = np.genfromtxt("20190504_224335mp4AnalyzeSad.txt",delimiter=',',dtype=float)#ashly
sadParticipant1 = np.genfromtxt("20190507_115106mp4AnalyzeSad.txt",delimiter=',',dtype=float)#jared
readerSamples.append(sadReader1)
participantSamples.append(sadParticipant1)

os.chdir("C:\\Jareds_Stuff\\2_SeniorYear\\2_Spring2019\\3_HCI\\finalProject\\newProject\\recordedVideo\\videoData\\sad\\2")
sadReader2 = np.genfromtxt("20190504_223657mp4AnalyzeSad.txt",delimiter=',',dtype=float)#ashly
sadParticipant2 = np.genfromtxt("20190510_161725mp4AnalyzeSad.txt",delimiter=',',dtype=float)#jared
readerSamples.append(sadReader2)
participantSamples.append(sadParticipant2)

os.chdir("C:\\Jareds_Stuff\\2_SeniorYear\\2_Spring2019\\3_HCI\\finalProject\\newProject\\recordedVideo\\videoData\\funny\\1")
happyReader1 = np.genfromtxt("20190504_224931mp4AnalyzeHappy.txt",delimiter=',',dtype=float)#ashly
happyParticipant1 =np.genfromtxt("20190507_115433mp4AnalyzeHappy.txt",delimiter=',',dtype=float)#jared 
readerSamples.append(happyReader1)
participantSamples.append(happyParticipant1)

os.chdir("C:\\Jareds_Stuff\\2_SeniorYear\\2_Spring2019\\3_HCI\\finalProject\\newProject\\recordedVideo\\videoData\\funny\\2")
happyReader2 = np.genfromtxt("20190504_224718mp4AnalyzeHappy.txt",delimiter=',',dtype=float)#ashly
happyParticipant2 =np.genfromtxt("20190511_152854mp4AnalyzeHappy.txt",delimiter=',',dtype=float)#jared 
readerSamples.append(happyReader2)
participantSamples.append(happyParticipant2)

#
#sample1 = pd.read_csv("20190504_224335mp4AnalyzeSad.txt", header = None) #ashly
#sample2 = pd.read_csv("20190507_115106mp4AnalyzeSad.txt", header = None) #jared
#%%

# Compute the statistics of the relevant sections between the two samples
def computeStats(readerSamples, participantSamples):

  # loop through all samples, which will be appended to the array in same order
  # so the first funny and sad tests will be equivalent
  
  synchronousList = []
  for i in range(len(readerSamples)):
    readerSample = readerSamples[i]
    participantSample = readerSamples[i]
    
    #split samples into 10 sections to compare in windows
    ind1 = int(len(readerSample) / 10) #index of reader sample to split
    splitReaders = np.split(readerSample, [ind1, ind1*2,ind1*3,ind1*4,ind1*5,ind1*6,ind1*7,ind1*8,ind1*9])
    ind2 = int(len(participantSample) / 10) #index of reader sample to split
    splitParticipant = np.split(readerSample, [ind2, ind2*2,ind2*3,ind2*4,ind2*5,ind2*6,ind2*7,ind2*8,ind2*9])
    
    failRejectHo = 0
    # segment into 10 sections, count how many sections are statistically equivalent
    # testing Ho: sample distribution segments are similar
    for n in range(len(splitReaders)):
      rsample = splitReaders[n]  
      
      # Compute the descriptive statistics of reader and participant
      sam1bar = rsample.mean()
      sam1var = rsample.var(ddof=1)
      sam1n = rsample.size
      sam1dof = sam1n - 1
      
      psample = splitParticipant[1]
      sam2bar = psample.mean()
      sam2var = psample.var(ddof=1)
      sam2b = psample.size
      sam2bdof = sam2b - 1
      
      tvalue, pvalue = stats.ttest_ind(psample, rsample, equal_var = False)
      
      # if the pvalue is greater than the chosen alpha = 0.05, fail to reject Ho
      print("Pvalue: ", pvalue)
      if pvalue > 0.05:
        failRejectHo += 1
    print("Times we failed to reject Ho(the corresponding segments of the samples are equal): ", failRejectHo)
    # if the number of times we fail to reject Ho > 5, then the reader/participant "synchronized"
    if failRejectHo > 5:
      synchronousList.append(1)
    else:
      synchronousList.append(0)
  return synchronousList

#%%

#synchronous list represents the following array:
  # [sadStoryPreTest, sadStoryPostTest, funnyStoryPreTest, funnyStoryPostTest]
synchList = computeStats(readerSamples, participantSamples)
