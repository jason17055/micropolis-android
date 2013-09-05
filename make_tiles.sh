#!/bin/sh

cp=../build
tmpdir=out.$$
(
cd ../micropolis-java/graphics
mkdir $tmpdir
for i in {0..15}
do
	ii=$(printf "%02d" $i)
	let j=64*i
	echo "$ii $i $j"
	
	java -cp $cp -Dtile_size=32 -Dtile_count=64 -Dskip_tiles=$j micropolisj.build_tool.MakeTiles tiles.rc out.$$/tiles$ii.png
done
)
mv ../micropolis-java/graphics/$tmpdir .
