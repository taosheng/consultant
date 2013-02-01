#!/usr/bin/env python

import sys
import re
import commands


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
    oneChange = {'id':'','action':'','time':'','submitter':'','reviewer':'','files':[],'desc':''}
    lineCnt = 0
    for line in sys.stdin:
        lineCnt +=1
        line = line.strip()
        if re.match('Change .*', line) is not None:
            #print "%s <= %s"%(oneChange['submitter'], oneChange['reviewer'])
            resetDict(oneChange)
            parts = line.split(" ")
            oneChange['id'] = parts[1]
            oneChange['action'] = parts[4]
            oneChange['time'] = parts[3]
            oneChange['submitter'] = parts[5].split("@")[0]
            cmd = "~/bin/p4 -c tsu -p 10.201.16.19:1667 -u Tao-Sheng_Chen describe -S " + oneChange['id']
            print commands.getoutput(cmd)
        elif re.match('.*Reviewed by.*',line) is not None:
            reviewerLineParts = line.split("Reviewed by: ")
            if len(reviewerLineParts) > 1:
                oneChange['reviewer'] = line.split("Reviewed by: ")[1][1:-1]
 
      #  elif re.match('\.\.\. \.\.\..*',line) is not None:
      #      filename = line.split(" ")[4].split("#")[0]
      #      oneChange['files'].append(filename)
        else:
            oneChange['desc'] =  oneChange['desc'] + line
            

if __name__ == '__main__':

    main()
