package moe.karpador;

import moe.karpador.room.Room;
import moe.karpador.room.RoomView;
import moe.karpador.room.Wallscroll;
import moe.karpador.view.View;
import moe.karpador.view.ViewConstraint;
import processing.core.*;
import processing.event.MouseEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private String configDateFormat;

    private PImage configIcon;

    public WallscrollSimulator(String[] args) {
        String tempRoom = "room.yaml";
        String tempImages = ".";
        String tempConfig = ".";
        String tempDateFormat = "yyyy-MM-dd";
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
                case "-c", "--config-dir" -> {
                    if (i == args.length - 1) {
                        printHelp();
                    }
                    tempConfig = args[i + 1];
                    i++;
                }
                case "-w", "--wallscroll-dir" -> {
                    if (i == args.length - 1) {
                        printHelp();
                    }
                    tempImages = args[i + 1];
                    i++;
                }
                case "-f", "--date-format" -> {
                    if (i == args.length - 1) {
                        printHelp();
                    }
                    tempDateFormat = args[i + 1];
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
        this.configDateFormat = tempDateFormat;
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
                    -h, --help                   show this help
                    -r, --room <config>          use room config <config> (default 'room.yaml')
                    -c, --config-dir <dir>       use wallscroll configs from directory <dir> (default './')
                    -w, --wallscroll-dir <dir>   use wallscroll images from directory <dir> recursively (default './')
                    -f, --date-format <format>   use date format for saved wallscroll configs (default 'yyyy-MM-dd')
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

    public static PImage getConfigIcon() {
        return wallscrollSimulator.configIcon;
    }
    public static String getDateFormat() {
        return wallscrollSimulator.configDateFormat;
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public static void loadImages() {
        try (Stream<Path> walk = Files.walk(wallscrollSimulator.wallscrollImagesPath.toAbsolutePath())) {
            wallscrollSimulator.wallscrolls = walk
                    .filter(pa -> Wallscroll.PATTERN.matcher(pa.getFileName().toString()).matches())
                    .collect(Collectors.toMap(Path::toString, pa -> {
                        PImage i = wallscrollSimulator.loadImage(pa.toString());
                        i.resize(i.width > i.height ? min(500, i.width) : 0, i.width > i.height ? 0 : min(500, i.height));
                        return i;
                    }));
            URL url = Thread.currentThread().getContextClassLoader().getResource("config_file_icon.png");
            BufferedImage image = ImageIO.read(url);
            wallscrollSimulator.configIcon = new PImage(image);
        } catch (IOException e) {
            e.printStackTrace();
            wallscrollSimulator.exit();
        }
    }

    public static void loadConfigs() {
        try (Stream<Path> walk = Files.walk(wallscrollSimulator.wallscrollConfigPath.toAbsolutePath(), 1)) {
            wallscrollSimulator.configs = walk
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
        View top = views.get(views.size() - 1);
        long now = System.nanoTime();
        boolean modified = top.update(now - nanos);
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
        top.mousePressed(mouseButton, new PVector(mouseX, mouseY));
    }

    @Override
    public void mouseReleased() {
        View top = views.get(views.size()-1);
        top.mouseReleased(mouseButton, new PVector(mouseX, mouseY));
    }

    @Override
    public void mouseDragged() {
        View top = views.get(views.size()-1);
        top.mouseDragged(mouseButton, new PVector(mouseX, mouseY), new PVector(pmouseX, pmouseY));
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        mouseWheel(event.getCount(), new PVector(mouseX, mouseY));
    }

    public void mouseWheel(int scrollCount, PVector mouse) {
        View top = views.get(views.size() - 1);
        top.mouseWheel(scrollCount, mouse);
    }

    @Override
    public void keyPressed() {
        View top = views.get(views.size()-1);
        top.keyPressed(key, keyCode, new PVector(mouseX, mouseY));
    }
}
