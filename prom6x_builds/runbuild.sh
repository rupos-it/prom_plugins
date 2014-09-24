#!/bin/bash

#ant -f build_animation.xml clean
#ant -f build_bpmnmeasures.xml clean
#ant -f build_bpmn.xml clean
#ant -f  build_logdialog.xml clean
#ant -f  build_PNRA.xml clean
#ant -f  build_log.xml clean
#ant -f  build_logmerge.xml Clean
#ant -f  build_interactiveVisuali.xml clean
#ant -f  build_OSService.xml clean
#ant -f  build_petrinetsreplay.xml clean
#ant -f  build_petrinets.xml clean
#ant -f  build_prom.xml clean
#ant -f  build_transitionsystem.xml clean
#ant -f  build_widgets.xml clean
#ant -f  build_simplelogop.xml clean

ant -f  build_prom.xml
ant -f  build_interactiveVisuali.xml
ant -f  build_OSService.xml
ant -f build_animation.xml
ant -f  build_log.xml
ant -f  build_widgets.xml
ant -f  build_simplelogop.xml
ant -f  build_logab.xml

ant -f  build_logmerge.xml
ant -f build_bpmn.xml
ant -f  build_logdialog.xml
ant -f  build_oppsupp.xml

ant -f  build_transitionsystem.xml

ant -f  build_petrinets.xml
ant -f  build_petrinetsreplay.xml
ant -f  build_PNRA.xml
ant -f build_bpmnmeasures.xml



