#!/bin/sh

sz=${1-32}
echo "size $sz"

cp=../build
tmpdir=out.$$
(
cd ../micropolis-java/graphics
mkdir -v $tmpdir
for i in {0..15}
do
	ii=$(printf "%02d" $i)
	let j=64*i
	echo "$ii $i $j"
	
	java -cp $cp -Dtile_size=$sz -Dtile_count=64 -Dskip_tiles=$j micropolisj.build_tool.MakeTiles tiles.rc out.$$/tiles${sz}_$ii.png
done
)
mv ../micropolis-java/graphics/$tmpdir .
mv $tmpdir/* res/drawable-nodpi/
rmdir $tmpdir
