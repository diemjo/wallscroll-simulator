package moe.karpador;

import moe.karpador.room.Room;
import moe.karpador.room.RoomView;
import moe.karpador.room.Wallscroll;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.*;
import processing.event.MouseEvent;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WallscrollSimulator extends PApplet {
    private static final Pattern WALLSCROLL_CONFIG_PATTERN = Pattern.compile("wallscrolls.*\\.yaml");
    private static WallscrollSimulator wallscrollSimulator;
    private long nanos = System.nanoTime();

    private ArrayList<View> views;
    private final Path roomConfigPath;
    private final Path wallscrollConfigPath;
    private final Path wallscrollImagesPath;
    private Map<String, PImage> wallscrolls;
    private List<Path> configs;

    public WallscrollSimulator(String[] args) {
        String tempRoom = "room.yaml";
        String tempImages = ".";
        String tempConfig = ".";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> {
                    printHelp();
                }
                case "-r", "--room" -> {
                    if (i == args.length - 1) {
                        printHelp();
                    }
                    tempRoom = args[i + 1];
                    i++;
                }
                case "-c", "--config" -> {
                    if (i == args.length - 1) {
                        printHelp();
                    }
                    tempConfig = args[i + 1];
                    i++;
                }
                case "-w", "--wallscrolls" -> {
                    if (i == args.length - 1) {
                        printHelp();
                    }
                    tempImages = args[i + 1];
                    i++;
                }
                default -> {
                    System.err.println("Unexpected argument: '" + args[i] + "'");
                    printHelp();
                }
            }
        }
        this.wallscrollConfigPath = Path.of(tempConfig);
        if (!wallscrollConfigPath.toFile().isDirectory()) {
            throw new IllegalArgumentException("Config directory does not exists");
        }
        this.roomConfigPath = Path.of(tempRoom);
        if (!roomConfigPath.toFile().isFile()) {
            throw new IllegalArgumentException("Room config does not exists");
        }
        this.wallscrollImagesPath = Path.of(tempImages);
        if (!wallscrollImagesPath.toFile().isDirectory()) {
            throw new IllegalArgumentException("Wallscroll image directory does not exists");
        }
        float displayScaling = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0f;
    }

    public static void main(String[] args) {
        wallscrollSimulator = new WallscrollSimulator(args);
        PApplet.runSketch(new String[]{"Wallscroll Simulator"}, wallscrollSimulator);
    }

    private static void printHelp() {
        System.out.println("""
                wallscroll-simulator
                Usage: wallscroll-simulator [Options]
                
                Options:
                    -h, --help                  show this help
                    -r, --room <config>         use room config <config> (default 'room.yaml')
                    -c, --config <dir>          use wallscroll configs from directory <dir> (default './')
                    -w, --wallscrolls <dir>     use wallscroll images from directory <dir> recursively (default './')
                """
        );
        System.exit(1);
    }

    // STATIC FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    public static PGraphics createPGraphics(int w, int h, String renderer) {
        return wallscrollSimulator.createGraphics(w, h, renderer);
    }

    public static Path getWallscrollConfigPath() {
        return wallscrollSimulator.wallscrollConfigPath;
    }

    public static Map<String, PImage> getWallscrolls() {
        return wallscrollSimulator.wallscrolls;
    }

    public static List<Path> getConfigs() {
        return wallscrollSimulator.configs;
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public static void loadImages() {
        try {
            wallscrollSimulator.wallscrolls = Files.walk(wallscrollSimulator.wallscrollImagesPath.toAbsolutePath())
                    .filter(pa -> Wallscroll.PATTERN.matcher(pa.getFileName().toString()).matches())
                    .collect(Collectors.toMap(Path::toString, pa -> wallscrollSimulator.loadImage(pa.toString())));
        } catch (IOException e) {
            e.printStackTrace();
            wallscrollSimulator.exit();
        }
    }

    public static void loadConfigs() {
        try {
            wallscrollSimulator.configs = Files.walk(wallscrollSimulator.wallscrollConfigPath.toAbsolutePath(), 1)
                    .filter(pa -> WALLSCROLL_CONFIG_PATTERN.matcher(pa.getFileName().toString()).matches())
                    .map(Path::toAbsolutePath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            wallscrollSimulator.exit();
        }
    }

    public static void pushView(View view) {
        wallscrollSimulator.views.add(view);
    }

    public static void popView() {
        wallscrollSimulator.views.remove(wallscrollSimulator.views.size()-1);
    }

    public static void preventEscape() {
        wallscrollSimulator.key = 0;
    }

    public static void exitApp() {
        wallscrollSimulator.exit();
    }

    public static float getTextWidth(String text, int textSize) {
        wallscrollSimulator.textSize(textSize);
        return wallscrollSimulator.textWidth(text);
    }

    public static int menuTextSize() {
        return (int) (wallscrollSimulator.width*0.015f);
    }
    public static int buttonDescTextSize() {
        return (int) (wallscrollSimulator.width*0.010f);
    }
    public static int viewTitleTextSize() {
        return (int) (wallscrollSimulator.width*0.020f);
    }
    public static int pathTextSize() {
        return (int) (wallscrollSimulator.width*0.008f);
    }

    // PROCESSING FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    public void settings() {
        fullScreen(P2D);
    }

    @Override
    protected PSurface initSurface() {
        PSurface surface = super.initSurface();
        com.jogamp.newt.opengl.GLWindow window = (com.jogamp.newt.opengl.GLWindow)(surface.getNative());
        window.setResizable(true);
        window.setMaximized(true, true);
        return surface;
    }

    public void setup() {
        loadImages();

        views = new ArrayList<>(3);
        RoomView view = new RoomView(Room.fromYamlFile(roomConfigPath));
        //view.loadWallscrolls("wallscrolls.yaml");
        views.add(view);
    }

    @Override
    public void draw() {
        View top = views.get(views.size()-1);

        long now = System.nanoTime();
        boolean modified = top.update(now-nanos, mouseX, mouseY);
        nanos = now;

        background(255);
        for (View v : views) {
            //System.out.print(v+" ");
            PGraphics p = v.draw(ViewConstraint.exact(new PVector(width, height)));
            image(p, 0, 0);
        }
        //System.out.println();
    }

    @Override
    public void mousePressed() {
        View top = views.get(views.size()-1);
        top.mousePressed(mouseButton, mouseX, mouseY);
    }

    @Override
    public void mouseReleased() {
        View top = views.get(views.size()-1);
        top.mouseReleased(mouseButton, mouseX, mouseY);
    }

    @Override
    public void mouseDragged() {
        View top = views.get(views.size()-1);
        top.mouseDragged(mouseButton, mouseX, mouseY, pmouseX, pmouseY);
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        View top = views.get(views.size()-1);
        top.mouseWheel(event);
    }

    @Override
    public void keyPressed() {
        View top = views.get(views.size()-1);
        top.keyPressed(key, keyCode);
    }
}
