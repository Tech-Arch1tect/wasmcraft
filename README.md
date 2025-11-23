# wasmcraft

An in-development mod inspired by computercraft allowing players to run WASM* on computers/drones/etc to automate in-game play.

* Although the name suggests WASM, it's technically WASI


# info

- Not ready
- Even when ready it will NEVER be "safe" to run on a public server due to it's nature. You should ONLY ever use this mod with servers/players that you trust to not abuse it.

# status

- basic computers implemented
- redstone control (both reading input and setting output)
- basic peripheral api implemented
- (peripheral) multi-block monitors implemented
- "drone" added (computer which can traverse and interact with the world + have inventory)
- basic internal (drone) and external inventory api's implemented
- basic block placing and breaking implemented
- basic movement (drone) implemented
- basic api's to view blocks surrounding computers (block scanning) implemented

# todo

- game mechanics (e.g. progression and how the mod should actually play out)
- todo - write this list (so much)

# documentation

- todo

# how do I write WASM (WASI)?

Many languages can compile into WASI however during the development of this mod I am maintaining bindings to be used with TinyGo. The TinyGo bindings were originally meant to simply access the WASI host functions which wasmcraft implementes without any 'magic', however the bindings do currently hide some invisible 'magic' like decoding and parsing json from some api's, making them imediately usable from programs.
