#!/bin/bash

echo "Compiling WasmCraft Examples..."
echo

# Function to compile all examples in a category
compile_category() {
    local category=$1
    local category_name=$2

    if [ -d "$category" ]; then
        echo "=== $category_name ==="
        for example_dir in "$category"/*; do
            if [ -d "$example_dir" ] && [ -f "$example_dir/go.mod" ]; then
                local example_name=$(basename "$example_dir")
                echo "Building $example_name..."
                (cd "$example_dir" && tinygo build -o "../../${example_name}.wasm" -target=wasi .)
                if [ $? -eq 0 ]; then
                    echo "  ✓ ${example_name}.wasm"
                else
                    echo "  ✗ Failed to compile $example_name"
                fi
            fi
        done
        echo
    fi
}

# Compile all categories
compile_category "basic" "Basic Examples"
compile_category "tui" "TUI Examples"
compile_category "advanced" "Advanced Examples"

echo "Done! Compiled examples:"
ls -lh *.wasm 2>/dev/null || echo "No WASM files found"
