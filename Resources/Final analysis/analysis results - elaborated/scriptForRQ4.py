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


protectionNumbersJava = {}
protectionNumbersNative = {}
for i in range (0,10):
	protectionNumbersJava[i] = 0
	protectionNumbersNative[i] = 0

c = 0



pathlist = Path('./2019').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str):
		c = c + 1
		with open(path_in_str) as json_file:
			data = json.load(json_file)
			counterForAppJava = 0
			counterForAppNative = 0
			for protectionSpecific in protectionsSpecific:
				if (protectionSpecific in data):
					if (data[protectionSpecific] > 0):
						if ("JAVA" in protectionSpecific):
							counterForAppJava = counterForAppJava + 1
						else:
							counterForAppNative = counterForAppNative + 1
			protectionNumbersJava[counterForAppJava] = protectionNumbersJava[counterForAppJava] + 1
			protectionNumbersNative[counterForAppNative] = protectionNumbersNative[counterForAppNative] + 1


csvProtectionsJava = "numberOfProtections, numberOfApps\n"
csvProtectionsNative = "numberOfProtections, numberOfApps\n"

for i in range(0,10):
	csvProtectionsJava = csvProtectionsJava + str(i) + "," + str(protectionNumbersJava[i]) + "\n"
	csvProtectionsNative = csvProtectionsNative + str(i) + "," + str(protectionNumbersNative[i]) + "\n"

csvProtectionsFileJava = open("protectionsNumberJava.csv", "w")
csvProtectionsFileJava.write(csvProtectionsJava)
csvProtectionsFileJava.close()

csvProtectionsFileNative = open("protectionsNumberNative.csv", "w")
csvProtectionsFileNative.write(csvProtectionsNative)
csvProtectionsFileNative.close()



print("counter is: " + str(c))

