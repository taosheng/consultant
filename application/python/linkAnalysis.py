#!/usr/bin/env python
"""A more advanced Mapper, using Python iterators and generators."""

import sys
import random
import string
from urllib2 import Request, urlopen, URLError

def id_generator(size=6, chars=string.ascii_uppercase + string.digits):
    return ''.join(random.choice(chars) for x in range(size))

def read_input(file):
    for line in file:
        # split the line into words
        yield line.split()

def main(separator='\t'):
    # input comes from STDIN (standard input)
#    data = read_input(sys.stdin)
    for line in sys.stdin:
        line = line.strip()
        req = Request(line)
        response = urlopen(req)
        UID = id_generator()
        f = open("/tmp/show/"+UID,"a")
        f.write(response.read() )

#        for word in words:
#            print '%s%s%d' % (word, separator, 1)


UID = id_generator() 
if __name__ == "__main__":
    main()



