#!/usr/bin/python

from generator import Sequence, Choice, Entry, Empty, Recursion
from generator import generate
from generator import defaultActivityHook

#def task2_hook(startTime, attrs):
#    t0 = defaultActivityHook(startTime, attrs)
#    t0 += 15*60
#    return t0

process = Sequence([
    Choice([
	    Entry("Notify"),
        Empty()
    ]),
    Recursion(
        Sequence([
        	Entry("Fix"),
        	Entry("Check")
        ])
    ),
    Entry("Closed")
	])

generate(process, 1)
