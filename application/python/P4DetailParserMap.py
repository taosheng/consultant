#!/usr/bin/env python

import sys
import re


def pathToDecimalString(path):
    tmp="0."
    for c in path:
        tmp = tmp + str( ord(c))
    return tmp


def resetDict(oneChange):
    oneChange['time'] =''
    oneChange['id'] =''
    oneChange['submitter'] = ''
    oneChange['reviewer'] = ''
    oneChange['files'] = []
    oneChange['diffs'] = []
    oneChange['desc'] = ''
    return 

def read_input(file):
    for line in file:
        # split the line into words
        yield line.split()

def main(separator='\t'):

    # input comes from STDIN (standard input)
    data = read_input(sys.stdin)
    
#    print data
    oneChange = {'id':'','action':'','time':'','submitter':'','reviewer':'','files':[],'desc':'','diffs':[]}
    lineCnt = 0
    inFiles = False
    for line in sys.stdin:
        lineCnt +=1
        line = line.strip()
        if re.match('Change .*', line) is not None:
            inFiles = False
            print "%s <= %s"%(oneChange['submitter'], oneChange['reviewer'])
            print len(oneChange['files'])
            resetDict(oneChange)
            parts = line.split(" ")
            oneChange['id'] = parts[1]
            oneChange['action'] = ""
            oneChange['time'] = parts[5]
            oneChange['submitter'] = parts[3].split("@")[0]
        elif re.match('.*Reviewed by.*',line) is not None:
            inFiles = False
            reviewerLineParts = line.split("Reviewed by: ")
            if len(reviewerLineParts) > 1:
                oneChange['reviewer'] = line.split("Reviewed by: ")[1][1:-1]
        elif re.match('Affected files.*',line) is not None:
            inFiles = True
            
        elif re.match('\.\.\. \/\/',line) is not None and inFiles == True:
            parts = line.split(" ")
            oneChange['files'].append(parts[1])
 
      #  elif re.match('\.\.\. \.\.\..*',line) is not None:
      #      filename = line.split(" ")[4].split("#")[0]
      #      oneChange['files'].append(filename)
        else:
            #oneChange['desc'] =  oneChange['desc'] + line
            """do nothing"""
            

if __name__ == '__main__':

    main()
