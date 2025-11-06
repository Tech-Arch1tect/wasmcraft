#!/bin/bash

echo "Compiling TinyGo programs to WASM..."

cd redstone
tinygo build -o ../redstone.wasm -target=wasi .
cd ..

echo "Done! Files compiled:"
ls -lh *.wasm

echo ""
echo "To test in Minecraft:"
echo "1. Start HTTP server: python3 -m http.server 8000"
echo "2. In game: download http://localhost:8000/redstone.wasm"
echo "3. In game: run redstone"
