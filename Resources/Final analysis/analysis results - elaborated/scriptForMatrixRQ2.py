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
#"SafetyNetAttestation_JAVA_2"]
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

matrix = {}
for protection in protections:
	matrix[protection] = {}
	for protection2 in protections:
		matrix[protection][protection2] = 0

pathlist = Path('./2019').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str):
		c = c + 1
		with open(path_in_str) as json_file:
			data = json.load(json_file)
			alreadyDone = []
			for protectionSpecific in protectionsSpecific:
				if (protectionSpecific.split("_")[0] not in alreadyDone):
					if (protectionSpecific in data):
						if (data[protectionSpecific] > 0):
							alreadyDone  = [protectionSpecific.split("_")[0]]
							alreadyDone2 = [protectionSpecific.split("_")[0]]
							for protectionSpecific2 in protectionsSpecific:
								if (protectionSpecific2.split("_")[0] not in alreadyDone2):
									if(protectionSpecific2 in data):
										if(data[protectionSpecific2] > 0):
											alreadyDone2.append(protectionSpecific2.split("_")[0])
											matrix[protectionSpecific.split("_")[0]][protectionSpecific2.split("_")[0]] = matrix[protectionSpecific.split("_")[0]][protectionSpecific2.split("_")[0]] + 1 


csvMatrix = "/,"

for protection in protections:
	csvMatrix = csvMatrix + protection + ","
csvMatrix = csvMatrix + "\n"

for protection in matrix:
	csvMatrix = csvMatrix + protection + ","
	for protection2 in matrix[protection]:
		csvMatrix = csvMatrix + str(matrix[protection][protection2]) + ","
	csvMatrix = csvMatrix + "\n"


csvProtectionsFile = open("matrix.csv", "w")
csvProtectionsFile.write(csvMatrix)
csvProtectionsFile.close()




print(matrix)
print("counter is: " + str(c))

