#!/bin/bash

echo "inizio"

directory=prom61_origin/ProM/lib/

directory2=BPMNMeasures
directory3=PetriNetReplayAnalysis

mkdir BPMNMeasures/stdlib
mkdir PetriNetReplayAnalysis/stdlib
ln -s ../../$directory/images BPMNMeasures/stdlib/images
ln -s ../../$directory/images PetriNetReplayAnalysis/stdlib/images
 
ln -s ../../prom61_origin/ProM/dist/ProM-Contexts.jar  BPMNMeasures/stdlib/ProM-Contexts.jar
 ln -s ../../prom61_origin/ProM/dist/ProM-Framework.jar  BPMNMeasures/stdlib/ProM-Framework.jar
  ln -s ../../prom61_origin/ProM/dist/ProM-Models.jar  BPMNMeasures/stdlib/ProM-Models.jar
  ln -s ../../prom61_origin/ProM/dist/ProM-Plugins.jar  BPMNMeasures/stdlib/ProM-Plugins.jar

ln -s ../../prom61_origin/ProM/dist/ProM-Contexts.jar  PetriNetReplayAnalysis/stdlib/ProM-Contexts.jar
 ln -s ../../prom61_origin/ProM/dist/ProM-Framework.jar  PetriNetReplayAnalysis/stdlib/ProM-Framework.jar
  ln -s ../../prom61_origin/ProM/dist/ProM-Models.jar  PetriNetReplayAnalysis/stdlib/ProM-Models.jar
  ln -s ../../prom61_origin/ProM/dist/ProM-Plugins.jar  PetriNetReplayAnalysis/stdlib/ProM-Plugins.jar

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
