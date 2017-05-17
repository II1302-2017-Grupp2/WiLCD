#!/bin/bash
set -eou pipefail
pushd Dokument
./konvertera.sh
popd
git add .
git commit -m "Travis: konverterade dokument [skip ci]" || true
git checkout -B master
git remote set-url origin git@github.com:II1302-2017-Grupp2/WiLCD.git
git push
