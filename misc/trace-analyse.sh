#!/bin/bash
FILES="run/traces/*.tr"
for f in $FILES
do
	echo ">> Trace: $f"
	normal=`tail -n 2 "$f" | head -n 1`

	if [[ $normal == *Numbers* ]]
	then
		echo $normal
	else
		lines=`wc -l "$f" | cut -f1 -d' '`
		numbers=$(($lines - 2))
		echo "Unterminated. Numbers: $numbers"
	fi
	echo "-----------------------------------------"
done
