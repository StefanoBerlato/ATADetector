# how to access a range of rows
# rq1AtLeastOne2015[["percentageOfAppsWithAtLeastOneProtectionOnAllApps"]][1:29]

# clear the environment
rm(list=ls())

# modify here the path
path = "/home/stefano/Desktop/paperAndroid/ATADetector/Resources/Final analysis/analysis results - elaborated/"






# columns
al1 = "percentageOfAppsWithAtLeastOneProtectionOnAllApps"
al1jwl = "percentageOfAppsWithAtLeastOneProtectionWithLibrariesOnAllApps"
al1jwol = "percentageOfAppsWithAtLeastOneProtectionWithoutLibrariesOnAllApps"
al1n = "percentageOfAppsWithAtLeastOneProtectionNativeOnAllApps"
anpwl = "averageNumberOfProtectionPerAppsWithLibrariesOnAllApps"
anpwol = "averageNumberOfProtectionPerAppsWithoutLibrariesOnAllApps"
anpn = "averageNumberOfNativeProtectionPerAppsOnAllApps"



allCategory = "AVERAGE"
category = "Category"

# colors
blue = "#2980b9"
red = "#e74c3c"
grey = "#95a5a6"
white = "#ffffff"


# ===== Total app per category =====

cat = "category"
apps = "apps"

totalAppPerCategory2015 <- read.csv(paste(path, "appPerCategory2015.csv", sep=""))
totalAppPerCategory2019 <- read.csv(paste(path, "appPerCategory2019.csv", sep=""))

pdf(file = "AppPerCategory2015", height = 4)
tapc2015 <- totalAppPerCategory2015[order(totalAppPerCategory2015[apps],
                             decreasing=TRUE),]
cols <- c(blue, blue)[((tapc2015[["category"]] == allCategory)) + 1]
par(mar=c(6,4,2,1))
barplot(tapc2015[[apps]], 
        names.arg=gsub("_", " ", sapply(tapc2015[["category"]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        xpd=FALSE,
        ylab="Number",
        yaxt="n",
        ylim=c(0,540), 
        cex.names=0.6)
#title("Apps per Category - 2015", line = 1)
axis(2, at = seq(0, 540, 90), las = 1)
abline(h = c(90,180,270,360,450,540), col = grey)
abline(v = c(36.16))
dev.off()


pdf(file = "AppPerCategory2019", height = 4)
tapc2019 <- totalAppPerCategory2019[order(totalAppPerCategory2019[apps],
                                          decreasing=TRUE),]
cols <- c(blue, blue)[((tapc2019[["category"]] == allCategory)) + 1]
par(mar=c(6,4,2,1))
barplot(tapc2019[[apps]], 
        names.arg=gsub("_", " ", sapply(tapc2019[["category"]], tolower)), 
        las=2, 
        col=cols, 
        border=NA,
        xpd=FALSE,
        yaxt="n",
        ylab="Number", 
        ylim=c(0,540), 
        cex.names=0.6)
#title("Apps per Category - 2019", line = 1)
axis(2, at = seq(0, 540, 90), las = 1)
abline(h = c(90,180,270,360,450,540), col = grey)
abline(v = c(71.07))
dev.off()





# ===== RQ1 How frequent do apps implement AT and AD protections? =====

paiad = "percentageAppImplementingAD"
paiat = "percentageAppImplementingAT"
paip= "percentageAppImplementingProtection"

antiTampering <- read.csv(paste(path, "antiTampering.csv", sep=""))
antiDebugging <- read.csv(paste(path, "antiDebugging.csv", sep=""))
protectionsAdoption <- read.csv(paste(path, "protectionsAdoption.csv", sep=""))

# AD 2019. X axis categories in 2019. Y axis % number of apps implementing at least one AD protection
pdf(file = "rq1AD", height = 4)
rq1AD <- antiDebugging[order(antiDebugging[paiad],
                     decreasing=TRUE),]
cols <- c(blue, red)[((rq1AD[["category"]] == allCategory)) + 1]
par(mar=c(6,4,2,1))
barplot(rq1AD[[paiad]], 
        names.arg=gsub("_", " ", sapply(rq1AD[["category"]], tolower)), 
        las=2, 
        col=cols, 
        border=NA,
        xpd=FALSE,
        yaxt="n",
        ylab="Percentage", 
        ylim=c(0,100), 
        cex.names=0.6)
#title("Percentage of Apps adopting at least one AD protection", line = 1)
axis(2, at = seq(0, 100, 10), las = 1)
abline(h = c(10,20,30,40,50,60,70,80,90,100), col = grey)
abline(v = c(72.31))
dev.off()


# AT 2019. X axis categories in 2019. Y axis % number of apps implementing at least one AT protection
pdf(file = "rq1AT", height = 4)
rq1AT <- antiTampering[order(antiTampering[paiat],
                             decreasing=TRUE),]
cols <- c(blue, red)[((rq1AT[["category"]] == allCategory)) + 1]
par(mar=c(6,4,2,1))
barplot(rq1AT[[paiat]], 
        names.arg=gsub("_", " ", sapply(rq1AT[["category"]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        xpd=FALSE,
        yaxt="n",
        ylab="Percentage", 
        ylim=c(0,100), 
        cex.names=0.6)
#title("Percentage of Apps adopting at least one AT protection", line = 1)
axis(2, at = seq(0, 100, 10), las = 1)
abline(h = c(10,20,30,40,50,60,70,80,90,100), col = grey)
abline(v = c(72.31))
dev.off()


# 2019. X axis protections. Y axis number of apps implementing the protection
pdf(file = "rq1Protections", height = 4)
rq1P <- protectionsAdoption[order(protectionsAdoption[paip],
                             decreasing=TRUE),]
cols <- c(blue, blue)[((rq1P[["protection"]] == allCategory)) + 1]
par(mar=c(10,5,2,1))
barplot(rq1P[[paip]], 
        names.arg=gsub("_", " ", rq1P[["protection"]]), 
        las=2, 
        col=cols, 
        border=NA, 
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,25000), 
        cex.names=0.6)
#title("Apps adopting the related protection", line = 1)
title(ylab="Number of Apps", line=4, cex.lab=1.2)
axis(2, at = seq(0, 25000, 5000), las = 1, labels=formatC(axTicks(2), format="d", big.mark=','))
abline(h = c(5000,10000,15000,20000,25000), col = grey)
abline(v = c(11.22))
dev.off()




        



# ===== RQ2 How frequent do AT and AT protections integrate each other? =====

protectionsNumber <- read.csv(paste(path, "protectionsNumber.csv", sep=""))

nop = "numberOfProtections"
aip = "appImplementingProtection"


# 2019. X axis number of protections adopted Y axis number of apps implementing that number of protections
pdf(file = "rq2NumberOfProtections", height = 4)
rq2N <- protectionsNumber
cols <- c(blue, blue)[((rq2N[[nop]] == allCategory)) + 1]
par(mar=c(4,5,2,1))
barplot(rq2N[[aip]], 
        names.arg=gsub("_", " ", rq2N[[nop]]), 
        las=1, 
        col=cols, 
        border=NA, 
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,8000), 
        cex.names=0.6)
#title("Apps per Number of Protections Implemented", line = 1)
title(ylab="Number of Apps", line=4, cex.lab=1.2)
title(xlab="Number of Protections", line=2.5, cex.lab=1.2)
axis(2, at = seq(0, 8000, 2000), las = 1, labels=formatC(axTicks(2), format="d", big.mark=','))
abline(h = c(1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000), col = grey)
abline(v = c(12.461))
dev.off()








# ===== RQ3 How frequent are AT and AT protections applied to developers' code and to third-party libraries? =====

protectionsNumberUserCode <- read.csv(paste(path, "protectionsNumberUserCodeOnly.csv", sep=""))
protectionsNumberLibraries <- read.csv(paste(path, "protectionsNumberLibrariesOnly.csv", sep=""))

nop = "numberOfProtections"
noa = "numberOfApps"
paip3 = "LibrariesInAppImplementingProtection"

protectionsAdoptionLibraries <- read.csv(paste(path, "protectionsAdoptionLibraries.csv", sep=""))

# 2019. X axis number of protections adopted in apps code and libraries only Y axis number of apps implementing that number of protections
cols <- c(blue,red)
pdf(file = "rq3NumberOfProtectionsLibrariesAndCode", height = 4)
rq3N <- protectionsNumberUserCode
rq3L <- protectionsNumberLibraries
final = rbind(
        rq3N[[noa]],
        rq3L[[noa]]
)
par(mar=c(4,5,2,1))
barplot(final, 
        names.arg=gsub("_", " ", rq3N[[nop]]), 
        las=1, 
        beside=T,
        col=cols, 
        border=NA,
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,14000), 
        cex.names=0.6)
#title("Apps per Number of Protections Implemented", line = 1)
title(ylab="Number of Apps", line=4, cex.lab=1.2)
title(xlab="Number of Protections", line=2.5, cex.lab=1.2)
axis(2, at = seq(0, 14000, 2000), las = 1, labels=formatC(axTicks(2), format="d", big.mark=','))
abline(h = c(2000, 4000, 6000, 8000, 10000, 12000, 14000), col = grey)
abline(v = c(31.14))
legend(bg=white,'topright', fill=cols, legend=c("App Code Only", "Third-Party Libraries Only"))
dev.off()


# 2019. X axis protections. Y axis number of apps implementing the protection in the libraries
pdf(file = "rq3ProtectionsLibraries.pdf", height = 4)
rq3LA <- protectionsAdoptionLibraries[order(protectionsAdoptionLibraries[paip3],
                                  decreasing=TRUE),]
cols <- c(blue, blue)[((rq3LA[["protection"]] == allCategory)) + 1]
par(mar=c(10,5,2,1))
barplot(rq3LA[[paip3]], 
        names.arg=gsub("_", " ", rq3LA[["protection"]]), 
        las=2, 
        col=cols, 
        border=NA, 
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,25000), 
        cex.names=0.6)
#title("Apps adopting the related protection in library", line = 1)
title(ylab="Number of Apps", line=4, cex.lab=1.2)
axis(2, at = seq(0, 25000, 5000), las = 1, labels=formatC(axTicks(2), format="d", big.mark=','))
abline(h = c(5000,10000,15000,20000,25000), col = grey)
abline(v = c(11.22))
dev.off()







# ===== RQ4 How frequent are AT and AT protections implemented at Java and at Native Level? =====


protectionsNumberJava <- read.csv(paste(path, "protectionsNumberJava.csv", sep=""))
protectionsNumberNative <- read.csv(paste(path, "protectionsNumberNative.csv", sep=""))

nop = "numberOfProtections"
noa = "numberOfApps"





# 2019. X axis number of protections adopted at Java and Native level only Y axis number of apps implementing that number of protections
cols <- c(blue,red)
pdf(file = "rq4NumberOfProtectionsJavaAndNative", height = 4)
rq4J <- protectionsNumberJava
rq4N <- protectionsNumberNative
final = rbind(
        rq4J[[noa]],
        rq4N[[noa]]
)
par(mar=c(4,5,2,1))
barplot(final, 
        names.arg=gsub("_", " ", rq4J[[nop]]), 
        las=1, 
        beside=T,
        col=cols, 
        border=NA,
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,25000), 
        cex.names=0.6)
#title("Apps per Number of Protections Implemented", line = 1)
title(ylab="Number of Apps", line=4, cex.lab=1.2)
title(xlab="Number of Protections", line=2.5, cex.lab=1.2)
axis(2, at = seq(0, 25000, 5000), las = 1, labels=formatC(axTicks(2), format="d", big.mark=','))
abline(h = c(2500, 5000, 7500, 10000, 12500, 15000, 17500, 20000, 22500, 25000), col = grey)
abline(v = c(31.14))
legend(bg=white,'topright', fill=cols, legend=c("Java Level Only", "Native Level Only"))
dev.off()





# ===== RQ5 What is the evolution in the adoption of AD and AT protections in apps? =====

protectionsAdoptionCommon2015 <- read.csv(paste(path, "protectionsAdoptionCommon2015.csv", sep=""))
protectionsAdoptionCommon2019 <- read.csv(paste(path, "protectionsAdoptionCommon2019.csv", sep=""))
protectionsAdoption2015 <- read.csv(paste(path, "protectionsAdoption2015.csv", sep=""))
protectionsAdoption2019 <- read.csv(paste(path, "protectionsAdoption2019.csv", sep=""))


# X axis protections. Y axis number of COMMON apps implementing the protection in 2015 and 2019
cols <- c(blue,red)
pdf(file = "rq5ProtectionsCommon", height = 4)
rq52015 <- protectionsAdoptionCommon2015[order(protectionsAdoptionCommon2015[paip],
                                            decreasing=TRUE),]
rq52019 <- protectionsAdoptionCommon2019[order(protectionsAdoptionCommon2019[paip],
                                            decreasing=TRUE),]
final = rbind(
        rq52015[[paip]],
        rq52019[[paip]]
)
par(mar=c(10,5,2,1))
barplot(final, 
        names.arg=gsub("_", " ", rq52015[["protection"]]), 
        las=2, 
        beside=T,
        col=cols, 
        border=NA,
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,4000), 
        cex.names=0.6)
#title("Common apps adopting the related protection", line = 1)
title(ylab="Number of Apps", line=4, cex.lab=1.2)
title(xlab="Protections", line=8, cex.lab=1.2)
axis(2, at = seq(0, 4000, 1000), las = 1, labels=formatC(axTicks(2), format="d", big.mark=','))
abline(h = c(500,1000,1500,2000,2500,3000,3500,4000), col = grey)
abline(v = c(31.14))
legend(bg=white,'topright', fill=cols, legend=c("2015", "2019"))
dev.off()





# X axis protections. Y axis percentage of ALL apps implementing the protection in 2015 and 2019
cols <- c(blue,red)
pdf(file = "rq5ProtectionsAll", height = 4)
rq52015All <- protectionsAdoption2015
rq52019All <- protectionsAdoption2019
final = rbind(
        rq52015All[[paip]],
        rq52019All[[paip]]
)
par(mar=c(10,5,2,1))
barplot(final, 
        names.arg=gsub("_", " ", rq52015All[["protection"]]), 
        las=2, 
        beside=T,
        col=cols, 
        border=NA,
        xpd=FALSE,
        yaxt="n",
        ylab="", 
        ylim=c(0,100), 
        cex.names=0.6)
#title("Common apps adopting the related protection", line = 1)
title(ylab="Percentage of Apps", line=4, cex.lab=1.2)
title(xlab="Protections", line=8, cex.lab=1.2)
axis(2, at = seq(0, 100, 10), las = 1)
abline(h = c(10,20,30,40,50,60,70,80,90,100), col = grey)
abline(v = c(72.31))
legend(bg=white,'topright', fill=cols, legend=c("2015", "2019"))
dev.off()

        