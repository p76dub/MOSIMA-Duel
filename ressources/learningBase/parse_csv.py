# -*- coding: utf-8 -*-
import os
import sys

files = [os.path.abspath(file) for file in sys.argv[1:]]

for file in files:
    basename = os.path.basename(file)

    content = '@RELATION {}\n'.format(basename)
    content += '@ATTRIBUTE avgAlt REAL\n'
    content += '@ATTRIBUTE maxAlt REAL\n'
    content += '@ATTRIBUTE curAlt REAL\n'
    content += '@ATTRIBUTE fov REAL\n'
    content += '@ATTRIBUTE lastAction {explore, shoot, idle}\n'
    content += '@ATTRIBUTE life REAL\n'

    if basename in ["victory.csv", "defeat.csv"]:
        content += '@ATTRIBUTE result {VICTORY, DEFEAT}\n'
    else:
        content += '@ATTRIBUTE class {INSIGHT, NOTINSIGHT}\n'

    content += '\n@DATA\n'

    with open(file, 'r') as fd:
        for i, line in enumerate(fd):
            if i != 0:  # ignore header
                content += line.replace(';', ',').replace(',\n', '\n')

    with open('{}.arff'.format(file.split('.')[0]), 'w') as fd:
        fd.write(content)
