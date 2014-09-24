#!/bin/bash

echo "inizio"

directory=prom6x_origin/ProM/lib/

directory2=BPMNMeasures
directory3=PetriNetReplayAnalysis

mkdir BPMNMeasures/stdlib
mkdir PetriNetReplayAnalysis/stdlib
mkdir PetriNetReplayAnalysis/lib
ln -s ../../prom6x_origin/CostBasedAnalysis/tests/libs-external/  BPMNMeasures/tests/libs-external

ln -s ../../prom6x_origin/CostBasedAnalysis/tests/libs-external/  PetriNetReplayAnalysis/tests/libs-external
 
ln -s ../../$directory/images BPMNMeasures/stdlib/images
ln -s ../../$directory/images PetriNetReplayAnalysis/stdlib/images

#link petrinetanalysis stdlib/prom and packagelib

ln -s ../../prom6x_origin/ProM/dist/ProM-Contexts.jar  PetriNetReplayAnalysis/stdlib/ProM-Contexts.jar
 ln -s ../../prom6x_origin/ProM/dist/ProM-Framework.jar  PetriNetReplayAnalysis/stdlib/ProM-Framework.jar
  ln -s ../../prom6x_origin/ProM/dist/ProM-Models.jar  PetriNetReplayAnalysis/stdlib/ProM-Models.jar
  ln -s ../../prom6x_origin/ProM/dist/ProM-Plugins.jar  PetriNetReplayAnalysis/stdlib/ProM-Plugins.jar
mkdir PetriNetReplayAnalysis/packagelib
ln -s ../../prom6x_origin/Log/dist/Log.jar  PetriNetReplayAnalysis/packagelib/Log.jar
ln -s ../../prom6x_origin/PetriNets/dist/PetriNets.jar  PetriNetReplayAnalysis/packagelib/PetriNets.jar
ln -s ../../prom6x_origin/InteractiveVisualization/dist/InteractiveVisualization.jar  PetriNetReplayAnalysis/packagelib/InteractiveVisualization.jar

ln -s ../../prom6x_origin/LogDialog/dist/LogDialog.jar  PetriNetReplayAnalysis/packagelib/LogDialog.jar
ln -s ../../prom6x_origin/LogMerge/dist/LogMerge.jar  PetriNetReplayAnalysis/packagelib/LogMerge.jar
ln -s ../../prom6x_origin/PetriNetReplayer/dist/PetriNetReplayer.jar  PetriNetReplayAnalysis/packagelib/PetriNetReplayer.jar

#link bmpnmeasures stdlib/prom and packagelib

ln -s ../../prom6x_origin/ProM/dist/ProM-Contexts.jar  BPMNMeasures/stdlib/ProM-Contexts.jar
 ln -s ../../prom6x_origin/ProM/dist/ProM-Framework.jar  BPMNMeasures/stdlib/ProM-Framework.jar
  ln -s ../../prom6x_origin/ProM/dist/ProM-Models.jar  BPMNMeasures/stdlib/ProM-Models.jar
  ln -s ../../prom6x_origin/ProM/dist/ProM-Plugins.jar  BPMNMeasures/stdlib/ProM-Plugins.jar
mkdir BPMNMeasures/packagelib

 ln -s ../../prom6x_origin/Log/dist/Log.jar  BPMNMeasures/packagelib/Log.jar
ln -s ../../prom6x_origin/PetriNets/dist/PetriNets.jar  BPMNMeasures/packagelib/PetriNets.jar
ln -s ../../prom6x_origin/InteractiveVisualization/dist/InteractiveVisualization.jar  BPMNMeasures/packagelib/InteractiveVisualization.jar

ln -s ../../prom6x_origin/LogDialog/dist/LogDialog.jar  BPMNMeasures/packagelib/LogDialog.jar
ln -s ../../prom6x_origin/LogMerge/dist/LogMerge.jar  BPMNMeasures/packagelib/LogMerge.jar
ln -s ../../prom6x_origin/PetriNetReplayer/dist/PetriNetReplayer.jar  BPMNMeasures/packagelib/PetriNetReplayer.jar
ln -s ../../prom6x_origin/BPMN/dist/BPMN.jar  BPMNMeasures/packagelib/BPMN.jar
ln -s ../../PetriNetReplayAnalysis/dist/PetriNetReplayAnalysis.jar  BPMNMeasures/packagelib/PetriNetReplayAnalysis.jar


for file in "$directory"*.*
do 
  #echo "$file"
  MOVFile=`basename $file`
  rm BPMNMeasures/stdlib/$MOVFile
  ln -s ../../$file BPMNMeasures/stdlib/$MOVFile
  rm PetriNetReplayAnalysis/stdlib/$MOVFile
  ln -s ../../$file PetriNetReplayAnalysis/stdlib/$MOVFile
  #echo "$MOVFile"
done

echo; echo

exit 0
