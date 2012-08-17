
import threading

def doSomeThing():
    print "do some thing..."


#print dir(threading)

t = threading.Timer(3,doSomeThing)
#print dir(t)
t.start()
