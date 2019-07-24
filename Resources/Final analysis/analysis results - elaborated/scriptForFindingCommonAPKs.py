from pathlib import Path

c = 0
fatti = 0

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
	if (index2019 % 1000 == 0):
		print (str(index2019))
	if (index2019 == len2019 or index2015 == len2015):
		finished = True

print("counter is: " + str(c))
print("index2019: " + str(index2019) + ", index2015: " + str(index2015))

with open("common.txt", "w") as file:
	for path in paths:
		file.write("%s\n" % path)


