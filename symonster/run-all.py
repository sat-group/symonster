import sys
import os
import fnmatch

matches = []
for root, dirnames, filenames in os.walk('benchmarks/'):
    for filename in fnmatch.filter(filenames, '*.json'):
        matches.append(os.path.join(root, filename))


matches = ['benchmarks/geometry/10/benchmark10.json']
with open('result.txt', 'w') as resultFile:
    for path in matches:
        Dargs = ['"' + path + ' temp.txt -c"', 
                '"' + path + ' temp.txt"',
                '"' + path + ' temp.txt -e"',
                '"' + path + ' temp.txt -cp"',
                '"' + path + ' temp.txt -e -cp"']

        for Darg in Dargs:
            os.system('ant symonster -Dargs=' + Darg)
            with open('temp.txt') as tempFile:
                resultFile.write(tempFile.read())
                resultFile.write("\n")




