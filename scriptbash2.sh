#!/bin/bash

echo "inizio"

directory1=prom61_origin/ProM/lib/

directory2=prom61_origin

#mkdir BPMNMeasures/stdlib
#mkdir PetriNetReplayAnalysis/stdlib
#ln -s ../../$directory1/images BPMNMeasures/stdlib/images
#ln -s ../../$directory1/images PetriNetReplayAnalysis/stdlib/images

DIR=`ls $directory2 `

for directory in $DIR
do 
  echo "$directory"
  mkdir prom61_origin/$directory/stdlib
  rm prom61_origin/$directory/images
  ln -s ../../../$directory1/images prom61_origin/$directory/stdlib/images
  for file in "$directory1"*.*
  do
  	MOVFile=`basename $file`
	#echo "$file"
	#echo "$MOVFile"
 	rm prom61_origin/$directory/stdlib/$MOVFile
 	ln -s ../../../$file prom61_origin/$directory/stdlib/$MOVFile
  done


  
  ln -s ../../ProM/dist/ProM-Contexts.jar  prom61_origin/$directory/stdlib/ProM-Contexts.jar
  ln -s ../../ProM/dist/ProM-Framework.jar  prom61_origin/$directory/stdlib/ProM-Framework.jar
  ln -s ../../ProM/dist/ProM-Models.jar  prom61_origin/$directory/stdlib/ProM-Models.jar
  ln -s ../../ProM/dist/ProM-Plugins.jar  prom61_origin/$directory/stdlib/ProM-Plugins.jar


done

echo; echo

exit 0
