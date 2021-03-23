Android apps are subject to malicious reverse engineering and code tampering for many reasons, like premium features unlocking and malware piggybacking. Scientific literature and practitioners proposed several **Anti-Debugging** and **Anti-Tampering** protections, readily implementable by app developers, to empower Android apps to react against malicious reverse engineering actively. However, the extent to which Android app developers deploy these protections is not known.  

In this paper, we describe a large-scale study on Android apps to quantify the practical adoption of Anti-Debugging and Anti-Tampering protections. We analyzed 14,173 apps from 2015 and 23,610 apps from 2019 from the Google Play Store. Our analysis shows that **59%** of these apps implement neither Anti-Debugging nor Anti-Tampering protections. Moreover, half of the remaining apps deploy **only one protection**, not exploiting the variety of available protections. We also observe that app developers **prefer Java to Native** protections by a ratio of 99 to 1. Finally, we note that **apps in 2019 employ more protections** against reverse engineering than apps in 2015.
<hr />

To run ATADetector:
1. clone the repository;
2. enter in the "ATADetector/Tool" folder;
3. run "java -jar out/artifacts/ATADetector_jar/ATADetector.jar" for the usage;
4. to test ATADetector works, run "java -jar out/artifacts/ATADetector_jar/ATADetector.jar -p ../apks/ -r ../output/" (remember to mark the file "resources/dex2jar-2.0/d2j-dex2jar.sh" as executable). This will run the analysis on a sample application and report the results in the "output" directory;
5. add your APKs in the input folder ("-p") for the complete analysis.

<hr />

The paper was published by the **Journal of Information Security and Applications** in the volume 52, June 2020. A PDF version of the pre-print can be found [**in my website**](https://stefanoberlato.it/publications/)
