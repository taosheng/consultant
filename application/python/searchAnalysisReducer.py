#!/usr/bin/env python
# -*- coding: UTF-8 -*-

#Reducerr to grap all result from search engine.


from operator import itemgetter
import sys

current_url = None
current_count = 0
url = None

# input comes from STDIN
for line in sys.stdin:
    # remove leading and trailing whitespace
    line = line.strip()

    # parse the input we got from mapper.py
    url, queryString = line.split('\t', 1)


    # this IF-switch only works because Hadoop sorts map output
    # by key (here: word) before it is passed to the reducer
    if url == current_url:
        current_count += 1
    else:
        if current_count > 1:
            # write result to STDOUT
            print '%s\t%s' % (current_url, current_count)
        current_count = 1
        current_url = url

# TODO the last line?
#if current_url == url:
#    print '%s\t%s' % (current_url, current_count)
