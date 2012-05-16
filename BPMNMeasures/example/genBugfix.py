#!/usr/bin/python

from generator import Sequence, Choice, Entry, Empty, Recursion
from generator import generate
from generator import defaultActivityHook

#def task2_hook(startTime, attrs):
#    t0 = defaultActivityHook(startTime, attrs)
#    t0 += 15*60
#    return t0

#(scheduling, assignment, pause/resume, autoskip, manual skip)
notify = (1,0,1,0,0)
fix = (0,1,0,1,0)
check = (1,0,0,0,1)
closed = (0,0,0,0,0)

process = Sequence([
    Choice([
	    Entry("Notify",notify),
        Empty()
    ]),
    Recursion(
        Sequence([
        	Entry("Fix",fix),
        	Entry("Check",check)
        ])
    ),
    Entry("Closed",closed)
	])

generate(process, 100)
