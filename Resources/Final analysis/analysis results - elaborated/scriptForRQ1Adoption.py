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


c = 0


protectionsAdoption = {}
for protection in protections:
	protectionsAdoption[protection] = 0

pathlist = Path('./2019').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str):
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

csvProtectionsFile = open("protectionsAdoption.csv", "w")
csvProtectionsFile.write(csvProtectionsAdoption)
csvProtectionsFile.close()

print("counter is: " + str(c))

