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

protectionsSpecific = [
#"SignatureChecking_NATIVE",
"SignatureChecking_JAVA_1",
"SignatureChecking_JAVA_2",
"CodeIntegrityChecking_JAVA_1",
"CodeIntegrityChecking_JAVA_2",
"InstallerVerification_JAVA_1",
"InstallerVerification_JAVA_2",
"SafetyNetAttestation_JAVA_1",
"SafetyNetAttestation_JAVA_2",
#"EmulatorDetection_NATIVE",
"EmulatorDetection_JAVA_1",
"EmulatorDetection_JAVA_2",
#"DynamicAnalysisFrameworkDetection_NATIVE",
"DynamicAnalysisFrameworkDetection_JAVA_1",
"DynamicAnalysisFrameworkDetection_JAVA_2",
#"DebuggerDetection_NATIVE",
"DebuggerDetection_JAVA_1",
"DebuggerDetection_JAVA_2",
#"DebuggableStatusDetection_NATIVE",
"DebuggableStatusDetection_JAVA_1",
"DebuggableStatusDetection_JAVA_2"]
#"AlteringDebuggerMemoryStructure_NATIVE"]


protectionNumbersOnlyUserCode = {}
for i in range (0,10):
	protectionNumbersOnlyUserCode[i] = 0

c = 0



pathlist = Path('./2019').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str):
		c = c + 1
		with open(path_in_str) as json_file:
			data = json.load(json_file)
			#print(path_in_str)
			counterForApp = 0
			for protectionSpecific in protectionsSpecific:
				if (protectionSpecific in data):
					if (data[protectionSpecific] > 0):
						if ("2" in protectionSpecific):
							counterForApp = counterForApp - 1
						if ("1" in protectionSpecific):
							counterForApp = counterForApp + 1
			if (counterForApp < 0):
				counterForApp = 0
				print(path_in_str)
			protectionNumbersOnlyUserCode[counterForApp] = protectionNumbersOnlyUserCode[counterForApp] + 1


csvProtectionsUserCode = "numberOfProtections, numberOfApps\n"

for i in range(0,10):
	csvProtectionsUserCode = csvProtectionsUserCode + str(i) + "," + str(protectionNumbersOnlyUserCode[i]) + "\n"

csvProtectionsFile = open("protectionsNumberLibrariesOnly.csv", "w")
csvProtectionsFile.write(csvProtectionsUserCode)
csvProtectionsFile.close()


print("counter is: " + str(c))

