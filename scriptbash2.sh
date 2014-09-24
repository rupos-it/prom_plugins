#!/bin/bash

echo "inizio"

directory1=prom6x_origin/ProM/lib/
directory3=prom6x_origin/ProM/tests/libs-external/
directory4=prom6x_origin/ProM/tests/libs-external/lib-cobertura/
directory2=prom6x_origin

#mkdir BPMNMeasures/stdlib
#mkdir PetriNetReplayAnalysis/stdlib
#ln -s ../../$directory1/images BPMNMeasures/stdlib/images
#ln -s ../../$directory1/images PetriNetReplayAnalysis/stdlib/images

DIR=`ls $directory2 `

for directory in $DIR
do 
  echo "$directory"
  #rm -rf prom6x_origin/$directory/tests/libs-external/lib-cobertura
  mkdir prom6x_origin/$directory/stdlib
  mkdir prom6x_origin/$directory/doc
  #rm prom6x_origin/$directory/images
  #rm -rf prom6x_origin/$directory/stdlib/
  ln -s ../../../$directory1/images prom6x_origin/$directory/stdlib/images
  for file in "$directory1"*.*
  do
  	MOVFile=`basename $file`
	#echo "$file"
	#echo "$MOVFile"
 	#rm prom6x_origin/$directory/stdlib/$MOVFile
 	ln -s ../../../$file prom6x_origin/$directory/stdlib/$MOVFile 		
  done
  #echo "$directory3"
  for file in "$directory3"*.*
  do
  	MOVFile=`basename $file`
	#echo "$file"
	#echo "$MOVFile"
	mkdir prom6x_origin/$directory/tests/libs-external
 	#rm prom6x_origin/$directory/tests/libs-external/$MOVFile
 	#echo "$file"
 	ln -s ../../../../$file prom6x_origin/$directory/tests/libs-external/$MOVFile
 	 	done
	for file in "$directory4"*.*
  do
  	MOVFile=`basename $file`
	echo "$file"
	echo "$MOVFile"
	mkdir prom6x_origin/$directory/tests/libs-external/lib-cobertura
 	#rm prom6x_origin/$directory/tests/libs-external/$MOVFile
 	#echo "$file"
 	ln -s ../../../../../$file prom6x_origin/$directory/tests/libs-external/lib-cobertura/$MOVFile
 	done
  echo "here $directory "
  ln -s ../../ProM/dist/ProM-Contexts.jar  prom6x_origin/$directory/stdlib/ProM-Contexts.jar
  ln -s ../../ProM/dist/ProM-Framework.jar  prom6x_origin/$directory/stdlib/ProM-Framework.jar
  ln -s ../../ProM/dist/ProM-Models.jar  prom6x_origin/$directory/stdlib/ProM-Models.jar
  ln -s ../../ProM/dist/ProM-Plugins.jar  prom6x_origin/$directory/stdlib/ProM-Plugins.jar


done

ln -s ../../Widgets/dist/Widgets.jar  prom6x_origin/BPMN/packagelib/Widgets.jar
ln -s ../../Log/dist/Log.jar  prom6x_origin/BPMN/packagelib/Log.jar

ln -s ../../SimpleLogOperations/dist/SimpleLogOperations.jar  prom6x_origin/LogMerge/packagelib/SimpleLogOperations.jar

ln -s ../../Log/dist/Log.jar  prom6x_origin/PetriNets/packagelib/Log.jar
ln -s ../../Widgets/dist/Widgets.jar  prom6x_origin/PetriNets/packagelib/Widgets.jar
ln -s ../../TransitionSystems/dist/TransitionSystems.jar  prom6x_origin/PetriNets/packagelib/TransitionSystems.jar



echo; echo

exit 0
