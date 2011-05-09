
class Handler:

    def __init__(self):
        self.something = 'something'

    def run(self):
        print "run() in Handler"


class VMHandler(Handler):
    def run(self):
        print "run() in VMHandler"
        Handler.run(self)

    def wrong(self):
        try :
            a = 0
            b = 1
            c = b /a
        except ZeroDivisionError : 
            print "zero division error!! "
        except :
            print "something went wrong but we don't know!! "



a = Handler()
a.run()
b = VMHandler()
b.run()
b.wrong()
