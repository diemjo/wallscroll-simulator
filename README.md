# Wallscroll Simulator

Decorate your room using wallscrolls without needing to unpack and hang them up irl.
You can save and load wallscroll configurations.
(filename is `<config-dir>/wallscrolls-<dateformat>.yaml` with <config-dir> as the argument from '-c', rename it if you don't want it overwritten)

This is written using the Processing drawing library.

# Usage
This project has only been tested using java version 17. Using that version is recommended.

    java -jar wallscroll-simulator [Options]

    Options:
                    -h, --help                   show this help
                    -r, --room <config>          use room config <config> (default 'room.yaml')
                    -c, --config-dir <dir>       use wallscroll configs from directory <dir> (default './')
                    -w, --wallscroll-dir <dir>   use wallscroll images from directory <dir> recursively (default './')
                    -f, --date-format <format>   use date format for saved wallscroll configs (default 'yyyy-MM-dd')

- The menu can be opened and closed using ESC.
- Hover over a wallscroll and press DEL to remove it.
- Hover over a wallscroll and press ALT to show a tooltip with the image name.
- Drag a wallscroll around using the left mouse button.
- Move the camera by clicking and dragging the right mouse button.
- Drag a wallscroll into a side of a wall to move it to the next wall.
