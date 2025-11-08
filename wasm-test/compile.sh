#!/bin/bash

echo "Compiling TinyGo programs to WASM..."

cd redstone
tinygo build -o ../redstone.wasm -target=wasi .
cd ..

cd monitor_one
tinygo build -o ../monitor_one.wasm -target=wasi .
cd ..

cd infinite_loop
tinygo build -o ../infinite_loop.wasm -target=wasi .
cd ..

echo ""
echo "Done! Files compiled:"
ls -lh *.wasm

echo ""
echo "To test in Minecraft:"
echo "1. Start HTTP server: python3 -m http.server 8000"
echo "2. In game: download http://localhost:8000/redstone.wasm"
echo "3. In game: run redstone"
echo ""
echo "For monitor example:"
echo "1. Place a monitor block and set label to 'monitor_test'"
echo "2. In game: download http://localhost:8000/monitor_one.wasm"
echo "3. In game: run monitor_one"
