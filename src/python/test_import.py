

import sys
sys.path.append("/tmp/")

import moduleA.func as func
import moduleA
print dir(moduleA)
print dir(moduleA.func)

func.funcA()

a = func.Cluster()
a.run()
#func.Commander.runCommand()
