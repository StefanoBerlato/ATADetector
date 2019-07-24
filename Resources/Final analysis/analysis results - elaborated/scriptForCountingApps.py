import json
from pathlib import Path

c = 0

totalPerCategory = {}


pathlist = Path('./2015').glob('**/*short.json')
for path in pathlist:
	path_in_str = str(path)
	if ("finalReport_short.json" not in path_in_str):
		c = c+ 1
		category = (path_in_str.replace("2015/", "").split("/")[0])
		if (category not in totalPerCategory):
			totalPerCategory[category] = 0
		with open(path_in_str) as json_file:
			totalPerCategory[category] = totalPerCategory[category] + 1


csvTotal = "category, apps\n"

for category in totalPerCategory:
	csvTotal = csvTotal + category + "," + str(totalPerCategory[category]) + "\n"

csvADFile = open("totalAppPerCategory2015.csv", "w")
csvADFile.write(csvTotal)
csvADFile.close()

print("counter is: " + str(c))


