
from threading import Thread
import time

class mythread(Thread):
        def __init__(self,mid,sleep):
            Thread.__init__(self)
            self.mid = mid
	    self.sleep = sleep
	def run(self):
            for i in range(50):
            	print self.mid+ " run:"+str(time.ctime())
		time.sleep(self.sleep)


t1 = mythread("t1",1)
t2 = mythread("t2",3)
t3 = mythread("t3",4)
t1.start()
t2.start()
t3.start()
