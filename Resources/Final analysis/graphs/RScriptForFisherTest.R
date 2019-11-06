# how to access a range of rows
# rq1AtLeastOne2015[["percentageOfAppsWithAtLeastOneProtectionOnAllApps"]][1:29]

# clear the environment
rm(list=ls())

# modify here the path
path = "/home/stefano/Desktop/paperAndroid/ATADetector/Resources/Final analysis/analysis results - elaborated/"



# ===== fisher test for RQ3: how frequent are AT and AT protections applied to developers' code and to third-party libraries? =====

protectionsNumberUserCode <- read.csv(paste(path, "protectionsNumberUserCodeOnly.csv", sep=""))
protectionsNumberLibraries <- read.csv(paste(path, "protectionsNumberLibrariesOnly.csv", sep=""))

nop = "numberOfProtections"
noa = "numberOfApps"

rq3N <- protectionsNumberUserCode[1:8,]
rq3L <- protectionsNumberLibraries[1:8,]

matRQ3 <- cbind(userCode=rq3N[[noa]], libraryCode=rq3L[[noa]])
rownames(matRQ3) <- rq3N[[1]]

RQ3Result <- fisher.test(matRQ3,simulate.p.value=T)




# ===== fisher test for RQ4: how frequent are AT and AT protections implemented at Java and at Native Level? =====


protectionsNumberJava <- read.csv(paste(path, "protectionsNumberJava.csv", sep=""))
protectionsNumberNative <- read.csv(paste(path, "protectionsNumberNative.csv", sep=""))

nop = "numberOfProtections"
noa = "numberOfApps"

rq4J <- protectionsNumberJava[1:8,]
rq4N <- protectionsNumberNative[1:8,]

matRQ4 <- cbind(java=rq4J[[noa]], native=rq4N[[noa]])
rownames(matRQ4) <- rq4J[[1]]

RQ4Result <- fisher.test(matRQ4,simulate.p.value=T)




# ===== fisher test for RQ5: what is the evolution in the adoption of AD and AT protections in apps? =====


protectionsAdoptionCommon2015 <- read.csv(paste(path, "protectionsAdoptionCommon2015.csv", sep=""))
protectionsAdoptionCommon2019 <- read.csv(paste(path, "protectionsAdoptionCommon2019.csv", sep=""))

paip= "percentageAppImplementingProtection"

rq52015 <- protectionsAdoptionCommon2015[order(protectionsAdoptionCommon2015[paip],
                                               decreasing=TRUE),]
rq52019 <- protectionsAdoptionCommon2019[order(protectionsAdoptionCommon2019[paip],
                                               decreasing=TRUE),]

matRQ5 <- cbind(a2015=rq52015[[paip]], a2019=rq52019[[paip]])

rownames(matRQ5) <- rq52015[["protection"]]

RQ5Result <- fisher.test(matRQ5,simulate.p.value=T)


# ===== printing results

print(RQ3Result->p->value)
print(RQ4Result->p->value)
print(RQ5Result->p->value)


