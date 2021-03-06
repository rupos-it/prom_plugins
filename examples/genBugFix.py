#!/usr/bin/python
import random
import time

def defaultHook(startTime, attrs):
    t = startTime
    delta1 = random.randint(1, 30*60)
    delta2 = random.randint(delta1+1, 59*60)
    t0 = t+delta1
    t1 = t+delta2
    return t0, t1, ""
    
class Empty():
    def __init__(self):
        pass
    def gen(self, t, attrs):
        return ("", t)

class Entry:
    def __init__(self, name, hook=None):
        self.name = name
        self.hook = hook
        if self.hook is None:
            self.hook = defaultHook
    def gen(self, t, attrs):
        t0, t1, data = self.hook(t, attrs)
        tf0 = time.strftime("%Y-%m-%dT%H:%M:%S.000+01:00", time.gmtime(t0))
        tf1 = time.strftime("%Y-%m-%dT%H:%M:%S.000+01:00", time.gmtime(t1))

        res = """
      <AuditTrailEntry>
        <Data>
         %s
        </Data>
	<WorkflowModelElement>%s</WorkflowModelElement>
	<EventType >start</EventType>
	<Timestamp>%s</Timestamp>
	<Originator>Guancio</Originator>
      </AuditTrailEntry>
      <AuditTrailEntry>
	<WorkflowModelElement>%s</WorkflowModelElement>
	<EventType >complete</EventType>
	<Timestamp>%s</Timestamp>
	<Originator>Guancio</Originator>
      </AuditTrailEntry>
        """ % (data, self.name, tf0, self.name, tf1)
        return (res, t1)

class Recursion:
    def __init__(self, e):
        self.e = e
    def gen(self, t, attrs):
        res = ""
        p = 0.5
        max_rec = 2
        it = 1
        while random.random() < p:
            if max_rec is not None and it > max_rec:
                break
            (res1, t) = self.e.gen(t, attrs)
            res += res1
            it += 1
        return (res, t)

def defaultChoiceHook(l, attr):
    x = random.randint(0, len(l)-1)
    return l[x]
    
class Choice:
    def __init__(self, l, hook=None):
        self.l = l
        self.hook = hook
        if (self.hook is None):
            self.hook = defaultChoiceHook
    def gen(self, t, attrs):
        res = ""
        task = self.hook(self.l, attrs)
        (res1, t1) = task.gen(t, attrs)
        return (res1, t1)

class Par:
    def __init__(self, l):
        self.l = l
    def gen(self, t, attrs):
        l1 = self.l[:]
        res = ""
        t1 = t
        while len(l1) > 0:
            x = random.randint(0, len(l1)-1)
            x = l1.pop(x)
            (res1, t2) = x.gen(t, attrs)
            # Non vero parallelo
            # t = t1
            t1 = max(t1, t2)
            res += res1
        return (res, t1)

class Sequence:
    def __init__(self, l, p=0):
        self.l = l
        self.p = p
    def gen(self, t, attrs):
        res = ""
       # exceptions = [Entry("Draft")]#, Entry("Study")]
        for a in self.l:
            (res1, t) = a.gen(t, attrs)
            res += res1
          #  if random.random() < self.p:
           #     a1 = exceptions[random.randint(0, len(exceptions) - 1)]
            #    (res1, t) =a1.gen(t)
             #   res += res1
        return (res, t)
        


def requestHook(t, attrs):
    dests = ["Italy", "UK", "USA", "Spain"]
    dest = dests[random.randint(0, len(dests)-1)]
    attrs["dest"] = dest

    attrs["age"] = random.randint(18, 80)

    t0, t1, data = defaultHook(t, attrs)
    data = ""
    for attr in attrs.keys():
        data += """
    <Attribute name="%s">%s</Attribute>
    """%(attr, attrs[attr])
    return t0,t1,data

def carHook(t, attrs):
    t0, t1, data = defaultHook(t, attrs)
    if attrs["dest"] == "Italy":
        t1 += 0
    elif attrs["dest"] == "Spain" or attrs["dest"] == "USA":
        t1 += random.randint(1, 60 * 60)
    else:
        t1 += random.randint(0.5 * 60 * 60, 3 * 60 * 60)
    return t0,t1,data

def airHook(t, attrs):
    t0, t1, data = defaultHook(t, attrs)
    if attrs["dest"] == "Italy":
        t1 += 0
    elif attrs["dest"] == "Spain" or attrs["dest"] == "UK":
        t1 += random.randint(1, 60 * 60)
    else:
        t1 += random.randint(60 * 60, 5 * 60 * 60)
    return t0,t1,data

def driveHook(t, attrs):
    t0, t1, data = defaultHook(t, attrs)
    return t0,t1 + random.randint(1, 60 * 60),data

def choiceHook(l, attrs):
    if 30 < attrs["age"] < 60:
        return l[1]
    return l[0]

def notifyHook(t, attrs):
    Urgente = ["Yes", "No"]
    urg = Urgente[random.randint(0, len(Urgente)-1)]
    attrs["urg"] = urg


    t0, t1, data = defaultHook(t, attrs)
    data = ""
    for attr in attrs.keys():
        data += """
    <Attribute name="%s">%s</Attribute>
    """%(attr, attrs[attr])
    return t0,t1,data

def choiceHook(l, attrs):
    if  attrs["urg"] == "Yes":
        return l[1]
    return l[0]

	


#def gen_sequence(t):
 #   acts = Sequence([
 #           Entry("StartRequest", requestHook),
  #          Par([
   #                 Sequence([
   #                         Entry("BookHotel"),
   #                         Entry("BookAir", airHook)
    #                        ]),
     #               Sequence([
      #                      Choice([
       #                             Entry("TestDrive", driveHook),
        #                            Empty()
        #                           ], choiceHook),
         #                   Entry("BookCar", carHook)
          #                  ]),
           #         ]),
            #
            #Entry("Payment")
            #])

    #p = 0.00

   # return acts.gen(t, {})[0]

def gen_sequence(t):
	acts = Sequence([ Entry("NotifyBug", notifyHook),
			Choice([
		     		Sequence([ Entry("CheckBug"),
					   Entry("FixBug" )
			     ]),
		     	Entry("FixBug")
    		], sceltaHook)	     
	
	return acts.gen(t,{})[0]	     


def gen_log(i):
    t = time.time() + 3600 * i
    return """
    <ProcessInstance id="%i" description="PaperOne">
      %s
    </ProcessInstance>
""" % (i, gen_sequence(t))


print("""<?xml version="1.0" encoding="UTF-8" ?>
<WorkflowLog xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://is.tm.tue.nl/research/processmining/WorkflowLog.xsd" description="Sample Guancio Log File">
  <Source program="Example One from Guancio"/>
  <Process id="Paper" description="Paper Submission">
""")

for i in range(500):
    print(gen_log(i))

print("""
  </Process>
</WorkflowLog>
""")

