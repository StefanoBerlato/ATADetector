import json
from pathlib import Path

c = 0
paths = []

files2019 = []
files2015 = []

len2019 = 0
len2015 = 0

pathlist2019 = Path('./2019').glob('**/*short.json')
for path2019 in pathlist2019:
        files2019.append(str(path2019).split("/")[3])
files2019.sort()
len2019 = len(files2019)
print("acquired 2019: " + str(len2019) + " files")


pathlist2015 = Path('./2015').glob('**/*short.json')
for path2015 in pathlist2015:
        files2015.append(str(path2015).split("/")[3])
files2015.sort()
len2015 = len(files2015)
print("aquired 2015: " + str(len2015) + " files")

index2019 = 0
index2015 = 0

finished = False

while (not finished):
        current2019 = files2019[index2019]
        current2015 = files2015[index2015]
        #print(current2019 + "=====" + current2015 +"====>" + min(current2019, current2015) +"=====" + str(current2015 == current2019))
        if (current2019 == current2015):
                paths.append(current2019)
                index2019 = index2019 + 1
                index2015 = index2015 + 1
                c = c + 1
        elif (min(current2019, current2015) == current2019):
                index2019 = index2019 + 1
        else:
                index2015 = index2015 + 1
        if (index2019 == len2019 or index2015 == len2015):
                finished = True

print("there are " + str(c) + " common apps")



protections = [
"SignatureChecking",
"CodeIntegrityChecking",
"InstallerVerification",
"SafetyNetAttestation",
"EmulatorDetection",
"DynamicAnalysisFrameworkDetection",
"DebuggerDetection",
"DebuggableStatusDetection",
"AlteringDebuggerMemoryStructure"
]

protectionsSpecific = [
"SignatureChecking_NATIVE",
"SignatureChecking_JAVA_1",
#"SignatureChecking_JAVA_2",
"CodeIntegrityChecking_JAVA_1",
#"CodeIntegrityChecking_JAVA_2",
"InstallerVerification_JAVA_1",
#"InstallerVerification_JAVA_2",
"SafetyNetAttestation_JAVA_1",
#"SafetyNetAttestation_JAVA_2",
"EmulatorDetection_NATIVE",
"EmulatorDetection_JAVA_1",
#"EmulatorDetection_JAVA_2",
"DynamicAnalysisFrameworkDetection_NATIVE",
"DynamicAnalysisFrameworkDetection_JAVA_1",
#"DynamicAnalysisFrameworkDetection_JAVA_2",
"DebuggerDetection_NATIVE",
"DebuggerDetection_JAVA_1",
#"DebuggerDetection_JAVA_2",
"DebuggableStatusDetection_NATIVE",
"DebuggableStatusDetection_JAVA_1",
#"DebuggableStatusDetection_JAVA_2",
"AlteringDebuggerMemoryStructure_NATIVE"]


c = 0


protectionsAdoption = {}
for protection in protections:
	protectionsAdoption[protection] = 0

pathlist = Path('./2015').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str and path_in_str.split("/")[3] in paths):
		c = c + 1
		with open(path_in_str) as json_file:
			data = json.load(json_file)
			alreadyDone = []
			for protection in protectionsSpecific:
				if (protection in data and protection not in alreadyDone):
					if (data[protection] > 0):
						alreadyDone.append(protection.split("_")[0])
						protectionsAdoption[protection.split("_")[0]] = protectionsAdoption[protection.split("_")[0]] + 1

csvProtectionsAdoption = "protection, percentageAppImplementingProtection\n"

for protection in protectionsAdoption:
        csvProtectionsAdoption = csvProtectionsAdoption + protection + "," + str(protectionsAdoption[protection]) + "\n"

csvProtectionsFile = open("protectionsAdoptionCommon2015.csv", "w")
csvProtectionsFile.write(csvProtectionsAdoption)
csvProtectionsFile.close()

print("counter is: " + str(c))
