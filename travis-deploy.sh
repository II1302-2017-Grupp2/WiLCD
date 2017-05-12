#!/bin/bash
set -eou pipefail
pushd docs
./konvertera.sh
popd
git add .
git commit -m "Travis: konverterade filer [skip ci]"
git remote set-url origin git@github.com:II1302-2017-Grupp2/WiLCD.git
git push
