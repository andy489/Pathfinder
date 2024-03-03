#!/bin/bash

# set -x

if [ $# -lt 1 ]; then
	echo -e "Usage: ${0} <base_ele>"
	exit 1
fi

file=${1}
base=${2}
hh=1
ss=1

while IFS=', ', read -r field1 field2
do {
    ss=$(( (${ss} + 1) % 60))

    if [ "${ss}" -eq "0" ]; then
       hh=$(( ${hh} + 1))
    fi

    hh_formatted=$(printf "%02d" ${hh})
    ss_formatted=$(printf "%02d" ${ss})

    echo -e "<trkpt lat=\"$field1\" lon=\"$field2\">\n\t<ele>$((base + $((10 + $RANDOM % 10)))).$((10 + $RANDOM % 90))</ele>"
    echo -e "\t<time>2011-10-22T${hh_formatted}:${ss_formatted}:$((10 + $RANDOM % 50))Z</time>\n</trkpt>"

} done < ${file}
