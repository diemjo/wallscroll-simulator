# Wallscroll Simulator

Decorate your room using wallscrolls without needing to unpack and hang them up irl.
You can save and load wallscroll configurations.
(filename is `<config-dir>/wallscrolls-yyyy-MM-dd.yaml` with <config-dir> as the argument from '-c', rename it if you don't want it overwritten)

This is written using the Processing drawing library.

# Usage
This project has only been tested using java version 17. Using that version is recommended.

    java -jar wallscroll-simulator [Options]

    Options:
        -h, --help                  show this help
        -r, --room <config>         use room config <config> (default 'room.yaml')
        -c, --config <dir>          use wallscroll configs from directory <dir> (default './')
        -w, --wallscrolls <dir>     use wallscroll images from directory <dir> recursively (default './')
        
- The menu can be opened using ESC.
- Hover over a wallscroll and press DEL to remove it.
- Drag a wallscroll around using the left mouse button.
- Move the camera by clicking and dragging the right mouse button.
- Drag a wallscroll into a side of a wall to move it to the next wall.
