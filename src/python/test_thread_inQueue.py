import Queue
from threading import Thread
import time

class mythread(Thread):
        def __init__(self,mid,sleep,q):
            Thread.__init__(self)
            self.mid = mid
	    self.sleep = sleep
	def run(self):
            for i in range(50):
                if q._qsize() > 0:
            	    print self.mid+ " run:"+q.get()+" ("+str(time.ctime())+")"
                else:
            	    print self.mid+ " nothing to run :"+" ("+str(time.ctime())+")"
		time.sleep(self.sleep)

##
## send a job to a given queue
##
class sendJobToQueue(Thread):
	def __init__(self,sleep,q):
	    Thread.__init__(self)
	    self.sleep = sleep
	    self.queue =  q


        def run(self):
            for i in range(30):
                time.sleep(self.sleep)
                q.put("additional cmd!")
   

q = Queue.Queue(110)

for i in range(110):
    q.put("cmd_"+str(i))

t1 = mythread("t1",1,q)
t2 = mythread("t2",1,q)
t3 = mythread("t3",2,q)

t1.start()
t2.start()
t3.start()

job = sendJobToQueue(20,q)
job.start()
