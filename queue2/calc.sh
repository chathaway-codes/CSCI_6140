#!/bin/sh

echo -e "N\tMPL\tCPU\tQ1\tQ2\tQe\tUtil"

SMPL="10 11 12 13 14 15 16 17 18 19 20"
SN="30 60 90"

for n in $SN
do
  for mpl in $SMPL
  do
    echo -n -e "$n\t$mpl\t"
    ./a.out $mpl $n 100000
  done
done
