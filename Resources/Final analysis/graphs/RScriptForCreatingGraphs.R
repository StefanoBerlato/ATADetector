# how to access a range of rows
# rq1AtLeastOne2015[["percentageOfAppsWithAtLeastOneProtectionOnAllApps"]][1:29]

# clear the environment
rm(list=ls())

# modify here the path
path = "~/Desktop/researchProject/Final results/output/"


# columns
al1 = "percentageOfAppsWithAtLeastOneProtectionOnAllApps"
al1jwl = "percentageOfAppsWithAtLeastOneProtectionWithLibrariesOnAllApps"
al1jwol = "percentageOfAppsWithAtLeastOneProtectionWithoutLibrariesOnAllApps"
al1n = "percentageOfAppsWithAtLeastOneProtectionNativeOnAllApps"
anpwl = "averageNumberOfProtectionPerAppsWithLibrariesOnAllApps"
anpwol = "averageNumberOfProtectionPerAppsWithoutLibrariesOnAllApps"
anpn = "averageNumberOfNativeProtectionPerAppsOnAllApps"

allCategory = "ALL"
category = "Category"

# colors
blue = "#2980b9"
red = "#c0392b"

# load data
finalResults2015 <- read.csv(paste(path, "finalResults2015.csv", sep=""))
finalResults2019 <- read.csv(paste(path, "finalResults2019.csv", sep=""))
finalResults2015Common <- read.csv(paste(path, "finalResults2015Common.csv", sep=""))
finalResults2019Common <- read.csv(paste(path, "finalResults2019Common.csv", sep=""))
topProtections2015 <- read.csv(paste(path, "topProtections2015.csv", sep=""))
topProtections2019 <- read.csv(paste(path, "topProtections2019.csv", sep=""))



# ===== RQ1 = how many applications adopt at least one AT or AD protection? =====

# at least one 2015
pdf(file = "rq1AtLeastOne2015", height = 4)
rq1AtLeastOne2015 <- finalResults2015[order(finalResults2015[al1],
                     decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOne2015[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq1AtLeastOne2015[[al1]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOne2015[[category]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one protection 2015", 
        ylab="Percentage", 
        ylim=c(0,100), 
        cex.names=0.8)
dev.off()

# at least one 2019
pdf(file = "rq1AtLeastOne2019", height = 4)
rq1AtLeastOne2019 <- finalResults2019[order(finalResults2019[al1],
                     decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOne2019[[category]] == allCategory)) + 1]
par(mar=c(7,5,2,0))
barplot(rq1AtLeastOne2019[[al1]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOne2019[[category]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one protection 2019", 
        ylab="Percentage", 
        ylim=c(0,100), 
        cex.names=0.6)
dev.off()







# at least one JAVA 2015
pdf(file = "rq1AtLeastOneJAVA2015", height = 4)
rq1AtLeastOneJava2015 <- finalResults2015[order(finalResults2015[al1jwl],
                                            decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOneJava2015[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq1AtLeastOneJava2015[[al1jwl]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOneJava2015[[category]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one JAVA (with libraries) protection 2015", 
        ylab="Percentage", 
        ylim=c(0,100), 
        cex.names=0.8)
dev.off()

# at least one JAVA 2019
pdf(file = "rq1AtLeastOneJAVA2019", height = 4)
rq1AtLeastOneJava2019 <- finalResults2019[order(finalResults2019[al1jwl],
                                            decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOneJava2019[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq1AtLeastOneJava2019[[al1jwl]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOneJava2019[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one JAVA (with libraries) protection 2019", 
        ylab="Percentage", 
        ylim=c(0,100), 
        cex.names=0.6)
dev.off()









# at least one JAVA WITHOUT LIBRARIES 2015
pdf(file = "rq1AtLeastOneJAVAWithoutLibraries2015", height = 4)
rq1AtLeastOneJavaWithoutLibraries2015 <- finalResults2015[order(finalResults2015[al1jwol],
                                                decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOneJavaWithoutLibraries2015[[category]] == allCategory)) + 1]
par(mar=c(7,5,2,0))
barplot(rq1AtLeastOneJavaWithoutLibraries2015[[al1jwol]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOneJavaWithoutLibraries2015[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one JAVA (without libraries) protection 2015", 
        ylab="Percentage", 
        ylim=c(0,70), 
        cex.names=0.8)
dev.off()

# at least one JAVA WITHOUT LIBRARIES 2019
pdf(file = "rq1AtLeastOneJAVAWithoutLibraries2019", height = 4)
rq1AtLeastOneJavaWithoutLibraries2019 <- finalResults2019[order(finalResults2019[al1jwol],
                                                decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOneJavaWithoutLibraries2019[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq1AtLeastOneJavaWithoutLibraries2019[[al1jwol]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOneJavaWithoutLibraries2019[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one JAVA (without libraries) protection 2019", 
        ylab="Percentage", 
        ylim=c(0,70), 
        cex.names=0.6)
dev.off()










# at least one NATIVE 2015
pdf(file = "rq1AtLeastOneNative2015", height = 4)
rq1AtLeastOneNative2015 <- finalResults2015[order(finalResults2015[al1n],
                                                                decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOneNative2015[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq1AtLeastOneNative2015[[al1n]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOneNative2015[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one NATIVE protection 2015", 
        ylab="Percentage", 
        ylim=c(0,10), 
        cex.names=0.8)
dev.off()

# at least one NATIVE 2019
pdf(file = "rq1AtLeastOneNative2019", height = 4)
rq1AtLeastOneNative2019 <- finalResults2019[order(finalResults2019[al1n],
                                                                decreasing=TRUE),]
cols <- c(blue, red)[((rq1AtLeastOneNative2019[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq1AtLeastOneNative2019[[al1n]], 
        names.arg=gsub("_", " ", sapply(rq1AtLeastOneNative2019[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Apps adopting at least one NATIVE protection 2019", 
        ylab="Percentage", 
        ylim=c(0,10), 
        cex.names=0.6)
dev.off()






# ===== RQ2 = how many protections does an application implement? =====



# JAVA WITH LIBRARIES 2015
pdf(file = "rq2NumberOfProtectionsJAVA2015", height = 4)
rq2JavaWithLibraries2015 <- finalResults2015[order(finalResults2015[anpwl],
                                                  decreasing=TRUE),]
cols <- c(blue, red)[((rq2JavaWithLibraries2015[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq2JavaWithLibraries2015[[anpwl]], 
        names.arg=gsub("_", " ", sapply(rq2JavaWithLibraries2015[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Number of JAVA protections (with libraries) per app 2015", 
        ylab="Average ", 
        ylim=c(0,5), 
        cex.names=0.8)
dev.off()

# JAVA WITH LIBRARIES 2019
pdf(file = "rq2NumberOfProtectionsJAVA2019", height = 4)
rq2JavaWithLibraries2019 <- finalResults2019[order(finalResults2019[anpwl],
                                                  decreasing=TRUE),]
cols <- c(blue, red)[((rq2JavaWithLibraries2019[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq2JavaWithLibraries2019[[anpwl]], 
        names.arg=gsub("_", " ", sapply(rq2JavaWithLibraries2019[[category]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        main="Number of JAVA protections (with libraries) per app 2019", 
        ylab="Average", 
        ylim=c(0,5), 
        cex.names=0.6)
dev.off()








# JAVA WITHOUT LIBRARIES 2015
pdf(file = "rq2NumberOfProtectionsJAVAWithoutLibraries2015", height = 4)
rq2JavaWithoutLibraries2015 <- finalResults2015[order(finalResults2015[anpwol],
                                                   decreasing=TRUE),]
cols <- c(blue, red)[((rq2JavaWithoutLibraries2015[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq2JavaWithoutLibraries2015[[anpwol]], 
        names.arg=gsub("_", " ", sapply(rq2JavaWithoutLibraries2015[[category]], tolower)), 
        las=2, 
        col=cols, 
        border=NA, 
        main="Number of JAVA protections (without libraries) per app 2015", 
        ylab="Average ", 
        ylim=c(0,5), 
        cex.names=0.8)
dev.off()

# JAVA WITHOUT LIBRARIES 2019
pdf(file = "rq2NumberOfProtectionsJAVAWithoutLibraries2019", height = 4)
rq2JavaWithoutLibraries2019 <- finalResults2019[order(finalResults2019[anpwol],
                                                   decreasing=TRUE),]
cols <- c(blue, red)[((rq2JavaWithoutLibraries2019[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq2JavaWithoutLibraries2019[[anpwol]], 
        names.arg=gsub("_", " ", sapply(rq2JavaWithoutLibraries2019[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Number of JAVA protections (without libraries) per app 2019", 
        ylab="Average", 
        ylim=c(0,5), 
        cex.names=0.6)
dev.off()





# NATIVE 2015
pdf(file = "rq2NumberOfProtectionsNATIVE2015", height = 4)
rq2Native2015 <- finalResults2015[order(finalResults2015[anpn],
                                                      decreasing=TRUE),]
cols <- c(blue, red)[((rq2Native2015[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq2Native2015[[anpn]], 
        names.arg=gsub("_", " ", sapply(rq2Native2015[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Number of Native protections per app 2015", 
        ylab="Average ",
        ylim=c(0,0.1), 
        cex.names=0.8)
dev.off()

# NATIVE 2019
pdf(file = "rq2NumberOfProtectionsNATIVE2019", height = 4)
rq2Native2019 <- finalResults2019[order(finalResults2019[anpn],
                                                      decreasing=TRUE),]
cols <- c(blue, red)[((rq2Native2019[[category]] == allCategory)) + 1]
par(mar=c(8,5,2,0))
barplot(rq2Native2019[[anpn]], 
        names.arg=gsub("_", " ", sapply(rq2Native2019[[category]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Number of Native protections per app 2019", 
        ylab="Average", 
        ylim=c(0,0.1), 
        cex.names=0.6)
dev.off()






# ===== RQ3 = which are the most implemented protections? =====



# 2015
pdf(file = "rq32015", height = 4)
rq32015 <- topProtections2015[order(topProtections2015[2],
                                                   decreasing=TRUE),]

cols <- c(blue, red)[((rq32015[[1]] == allCategory)) + 1]
par(mar=c(18,5,2,0))
rq32015[[2]] = rq32015[[2]]/rep(141.73, (length(rq32015[[2]])))
barplot(rq32015[[2]], 
        names.arg=gsub("_", " ", sapply(rq32015[[1]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Most implemented protections 2015",
        ylim=c(0,100), 
        ylab="Percentage", 
        
        cex.names=0.8)
dev.off()


# 2019
pdf(file = "rq32019", height = 4)
rq32019 <- topProtections2019[order(topProtections2019[2],
                                    decreasing=TRUE),]
cols <- c(blue, red)[((rq32019[[1]] == allCategory)) + 1]
par(mar=c(18,5,2,0))
rq32019[[2]] = rq32019[[2]]/rep(238.58, (length(rq32019[[2]])))
barplot(rq32019[[2]], 
        names.arg=gsub("_", " ", sapply(rq32019[[1]], tolower)),
        las=2, 
        col=cols, 
        border=NA, 
        main="Most implemented protections 2019",
        ylim=c(0,100), 
        ylab="Percentage", 
        
        cex.names=0.8)
dev.off()










# ===== RQ6 = what is the evolution if taking only apps both in 2015 and 2019? =====


# how many applications adopt at least one AT or AD protection?

# at least one Common
rq1AtLeastOne2015CommonAll <- tail(finalResults2015Common[1, al1], n=1)
rq1AtLeastOne2019CommonAll <- tail(finalResults2019Common[1, al1], n=1)

# at least one Java Common
rq1AtLeastOneJava2015CommonAll <- tail(finalResults2015Common[1, al1jwl], n=1)
rq1AtLeastOneJava2019CommonAll <- tail(finalResults2019Common[1, al1jwl], n=1)

# at least one Java wo Common
rq1AtLeastOneJavaWO2015CommonAll <- tail(finalResults2015Common[1, al1jwol], n=1)
rq1AtLeastOneJavaWO2019CommonAll <- tail(finalResults2019Common[1, al1jwol], n=1)


# at least one Naative Common
rq1AtLeastOneNATIVE2015CommonAll <- tail(finalResults2015Common[1, al1n], n=1)
rq1AtLeastOneNATIVE2019CommonAll <- tail(finalResults2019Common[1, al1n], n=1)



#pdf(file = "rq6al1", height = 4)
cols <- c(blue, red)
labelal1 <- c("At least one protection", 
              "At least one JAVA protection",
              "At least one JAVA without libraries",
              "At least one NATIVE protection")

final = rbind(c(rq1AtLeastOne2015CommonAll, 
                rq1AtLeastOneJava2015CommonAll, 
                rq1AtLeastOneJavaWO2015CommonAll, 
                rq1AtLeastOneNATIVE2015CommonAll), 
              
              c(rq1AtLeastOne2019CommonAll, 
                rq1AtLeastOneJava2019CommonAll, 
                rq1AtLeastOneJavaWO2019CommonAll,
                rq1AtLeastOneNATIVE2019CommonAll))
par(mar=c(3,4,2,4))
barplot(final, 
        names.arg=labelal1,
        beside=T,
        col=cols, 
        border=NA, 
        main="2015 and 2019 common app comparison",
        ylim=c(0,100), 
        ylab="Percentage", 
        cex.names=0.75)
legend('topright',fill=cols,legend=c("2015","2019"))


#dev.off()





