import sys
import os
import fnmatch
from contextlib import contextmanager
import subprocess, threading

class Command(object):
    def __init__(self, cmd):
        self.cmd = cmd
        self.process = None

    def run(self, timeout):
        def target():
            print 'Thread started'
            self.process = subprocess.Popen(self.cmd, shell=True)
            self.process.communicate()
            print 'Thread finished'

        thread = threading.Thread(target=target)
        thread.start()

        thread.join(timeout)
        if thread.is_alive():
            print 'Thread timeout'
            self.process.terminate()
            thread.join()
        print self.process.returncode

matches = []
for root, dirnames, filenames in os.walk('benchmarks/'):
    for filename in fnmatch.filter(filenames, '*.json'):
        matches.append(os.path.join(root, filename))

with open('result.txt', 'w') as resultFile:
    for path in matches:
        print (path)
        resultFile.write(path + "\n")
        Dargs = ['"' + path + ' temp.txt -c"', 
                '"' + path + ' temp.txt"',
                '"' + path + ' temp.txt -c -e"',
                '"' + path + ' temp.txt -c -cp"',
                '"' + path + ' temp.txt -e -cp"']
        for Darg in Dargs:
            print ("settings:" + Darg)
            command = Command('ant symonster -Dargs=' + Darg + ' &>/dev/null')
            command.run(timeout=900)
            with open('temp.txt') as tempFile:
                resultFile.write(tempFile.read())
                resultFile.write("\n")
        print ("\n")
        resultFile.write("\n")




