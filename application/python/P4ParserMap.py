#!/usr/bin/env python

import sys
import re


def resetDict(oneChange):
    oneChange['time'] =''
    oneChange['id'] =''
    oneChange['submitter'] = ''
    oneChange['reviewer'] = ''
    oneChange['files'] = []
    oneChange['desc'] = ''
    return 

def main(separator='\t'):
    # input comes from STDIN (standard input)
#    data = read_input(sys.stdin)
    
    oneChange = {'id':'','action':'','time':'','submitter':'','reviewer':'','files':[],'desc':''}
    lineCnt = 0
    for line in sys.stdin:
        lineCnt +=1
        line = line.strip()
        if re.match('.*#.* change .*', line) is not None:
            print str(oneChange)
            resetDict(oneChange)
            parts = line.split(" ")
            oneChange['id'] = parts[3]
            oneChange['action'] = parts[4]
            oneChange['time'] = parts[6]
            oneChange['submitter'] = parts[8].split("@")[0]
        elif re.match('.*Reviewed by.*',line) is not None:
            reviewerLineParts = line.split("Reviewed by: ")
            if len(reviewerLineParts) > 1:
                oneChange['reviewer'] = line.split("Reviewed by: ")[1][1:-1]
 
        elif re.match('\.\.\. \.\.\..*',line) is not None:
            filename = line.split(" ")[4].split("#")[0]
            oneChange['files'].append(filename)
        else:
            oneChange['desc'] =  oneChange['desc'] + line
            

if __name__ == '__main__':

    main()
