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

# for file in **/*.{org,tex}
# do
#     echo Converting $file to pdf
#     newSum="$(sha256sum "$file")"
#     oldSum=""
#     if [ -e "$file.hash" ]
#     then
#         oldSum="$(cat "$file.hash")"
#     fi
#     if [ "$newSum" = "$oldSum" ]
#     then
#         echo Unchanged, skipping
#         continue
#     fi
#     echo "$newSum" > "$file.hash"
#     pushd "$(dirname "$file")" > /dev/null
#     filebase="$(basename "$file")"
#     pandoc --to=latex "--output=${filebase%.*}.pdf" "$filebase"
#     popd > /dev/null
# done
