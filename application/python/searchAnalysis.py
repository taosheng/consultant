#!/usr/bin/env python
# -*- coding: UTF-8 -*-

#Mapper to grap all result from search engine.
#rsz=8
#start from none to 2
#search jason api = http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=

import os, sys, urllib, urllib2
import json as m_json

reload(sys)
sys.setdefaultencoding('utf8')

URL = 'http://ajax.googleapis.com/ajax/services/search/web?rsz=8&v=1.0&'
PAGE_SIZE = 8
RUN_PAGES = 2

QUERY_STRING = '陳道陞'
def doQuery(queryString,start=0):
    query = urllib.urlencode ( { 'q' : QUERY_STRING } )
    response = urllib.urlopen ( URL + query ).read()
    json = m_json.loads ( response )
    res = json['responseData']
    results = json [ 'responseData' ] [ 'results' ]
    return results

    

def main(separator='\t'):
    # input comes from STDIN (standard input)
#    data = read_input(sys.stdin)
    for line in sys.stdin:
        line = line.strip()
        queryString = line
        for run in range(RUN_PAGES):
            resultDict = doQuery(queryString,start=run)
            for result in resultDict:
                title = result['title']
                url = result['url'] 
                print ( url+","+QUERY_STRING+","+title )

#        for word in words:
#            print '%s%s%d' % (word, separator, 1)


if __name__ == "__main__":
    main()

