#!/bin/bash
set -euo pipefail

for file in **/*.docx
do
    if [ -e "$file" ]
    then
        echo Converting $file to org
        pandoc --to=org "--output=${file%.*}.org" "$file"
        rm "$file"
    fi
done

for file in **/*.{org,tex}
do
    echo Converting $file to pdf
    pandoc --to=latex "--output=$file.pdf" "$file"
done
