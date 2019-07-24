import json
from pathlib import Path

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

protections_AT = [
"SignatureChecking_NATIVE",
"SignatureChecking_JAVA_1",
"SignatureChecking_JAVA_2",
"CodeIntegrityChecking_JAVA_1",
"CodeIntegrityChecking_JAVA_2",
"InstallerVerification_JAVA_1",
"InstallerVerification_JAVA_2",
"SafetyNetAttestation_JAVA_1",
"SafetyNetAttestation_JAVA_2"]

protections_AD = [
"EmulatorDetection_NATIVE",
"EmulatorDetection_JAVA_1",
"EmulatorDetection_JAVA_2",
"DynamicAnalysisFrameworkDetection_NATIVE",
"DynamicAnalysisFrameworkDetection_JAVA_1",
"DynamicAnalysisFrameworkDetection_JAVA_2",
"DebuggerDetection_NATIVE",
"DebuggerDetection_JAVA_1",
"DebuggerDetection_JAVA_2",
"DebuggableStatusDetection_NATIVE",
"DebuggableStatusDetection_JAVA_1",
"DebuggableStatusDetection_JAVA_2",
"AlteringDebuggerMemoryStructure_NATIVE"]


c = 0

resultsForCategoryAT = {}
totalPerCategory = {}
resultsForCategoryAD = {}


pathlist = Path('./2019').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str):
		c = c+ 1
		category = (path_in_str.replace("2019/", "").split("/")[0])
		if (category not in resultsForCategoryAT):
			resultsForCategoryAT[category] = 0
		if (category not in resultsForCategoryAD):
			resultsForCategoryAD[category] = 0
		if (category not in totalPerCategory):
			totalPerCategory[category] = 0
		with open(path_in_str) as json_file:
			totalPerCategory[category] = totalPerCategory[category] + 1
			data = json.load(json_file)
			for stringAT in protections_AT:
				if (stringAT in data):
					if (data[stringAT] > 0):
						resultsForCategoryAT[category] = resultsForCategoryAT[category] + 1;
						break
			for stringAD in protections_AD:
               	        	if (stringAD in data):
               	                	if (data[stringAD] > 0):
               	                        	resultsForCategoryAD[category] = resultsForCategoryAD[category] + 1;
               	                        	break

csvAT = "category, percentageAppImplementingAT\n"
csvAD = "category, percentageAppImplementingAD\n"

for category in resultsForCategoryAT:
	csvAT = csvAT + category + "," + str(100*resultsForCategoryAT[category]/totalPerCategory[category]) + "\n"

csvATFile = open("antiTampering.csv", "w")
csvATFile.write(csvAT)
csvATFile.close()

for category in resultsForCategoryAD:
	csvAD = csvAD + category + "," + str(100*resultsForCategoryAD[category]/totalPerCategory[category]) + "\n"

csvADFile = open("antiDebugging.csv", "w")
csvADFile.write(csvAD)
csvADFile.close()


print("counter is: " + str(c))
print(resultsForCategoryAT)
print(resultsForCategoryAD)

