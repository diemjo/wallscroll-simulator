package moe.karpador.room;

import moe.karpador.WallscrollSimulator;
import moe.karpador.room.Room.WallSide;
import org.yaml.snakeyaml.Yaml;
import processing.core.*;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static processing.core.PConstants.CLOSE;

public class Wallscroll {
    public static final Pattern PATTERN = Pattern.compile(Rating.regex+"(\\d+)-"+ScrollFormat.regex+"\\.\\w+");
    PVector position;
    WallSide side;
    public final ScrollFormat format;
    public final Rating rating;

    public final Path path;
    private final String id;
    public final PImage image;

    public Wallscroll(Path path, PImage image, WallSide side) {
        this.path = path;
        this.side = side;
        this.image = image;
        if (image==null) {
            throw new IllegalArgumentException("Image must not be null ("+path+")");
        }

        String filename = path.getFileName().toString();
        Matcher matcher = PATTERN.matcher(filename);
        if (!matcher.matches())
            throw new IllegalArgumentException("Illegal image name format: Expected '(e|s)\\d+-\\w+\\.\\w+");
        this.rating = Rating.fromString(matcher.group(1));
        this.id = matcher.group(2);
        this.format = ScrollFormat.byName(matcher.group(3)+((image.height > image.width) ? "p" : "l"));
        position = new PVector(100, 100);
    }

    public Wallscroll(Path path, PImage image) {
        this(path, image, WallSide.FRONT);
    }

    public Wallscroll copy() {
        return new Wallscroll(this.path, this.image, this.side);
    }

    public static Wallscroll fromYaml(Map<String, Object> root) throws ClassCastException {
        Path path = Path.of((String) root.get("path")).toAbsolutePath();
        WallSide side = WallSide.fromName((String) root.get("side"));
        List<Double> position = (List<Double>) root.get("position");
        if (position.size()!=2) throw new IllegalArgumentException("Position argument expected two integer values");
        Wallscroll wallscroll = new Wallscroll(path, WallscrollSimulator.getWallscrolls().get(path.toString()), side);
        wallscroll.position.set(position.get(0).intValue(), position.get(1).intValue());
        return wallscroll;
    }

    public Map<String, Object> toYaml() {
        Yaml yaml = new Yaml();
        Map<String, Object> root = new HashMap<>();
        root.put("path", path.toString());
        root.put("side", side.toName());
        root.put("position", Arrays.asList(position.x, position.y));
        return root;
    }

    public PVector[] getVertices(PVector wallPos) {
        switch (side) {
            case FRONT -> {
                PVector topLeft = new PVector(wallPos.x + position.x, wallPos.y - position.y, wallPos.z -1);
                return new PVector[]{
                        new PVector(topLeft.x, topLeft.y, topLeft.z),
                        new PVector(topLeft.x + format.width, topLeft.y, topLeft.z),
                        new PVector(topLeft.x + format.width, topLeft.y - format.height, topLeft.z),
                        new PVector(topLeft.x, topLeft.y - format.height, topLeft.z)
                };
            }
            case RIGHT -> {
                PVector topLeft = new PVector(wallPos.x - 1, wallPos.y - position.y, wallPos.z - position.x);
                return new PVector[]{
                        new PVector(topLeft.x, topLeft.y, topLeft.z),
                        new PVector(topLeft.x, topLeft.y, topLeft.z - format.width),
                        new PVector(topLeft.x, topLeft.y - format.height, topLeft.z - format.width),
                        new PVector(topLeft.x, topLeft.y - format.height, topLeft.z)
                };
            }
            case BACK -> {
                PVector topLeft = new PVector(wallPos.x - position.x, wallPos.y - position.y, wallPos.z + 1);
                return new PVector[]{
                        new PVector(topLeft.x, topLeft.y, topLeft.z),
                        new PVector(topLeft.x - format.width, topLeft.y, topLeft.z),
                        new PVector(topLeft.x - format.width, topLeft.y - format.height, topLeft.z),
                        new PVector(topLeft.x, topLeft.y - format.height, topLeft.z)
                };
            }
            case LEFT -> {
                PVector topLeft = new PVector(wallPos.x + 1, wallPos.y - position.y, wallPos.z + position.x);
                return new PVector[]{
                        new PVector(topLeft.x, topLeft.y, topLeft.z),
                        new PVector(topLeft.x, topLeft.y, topLeft.z + format.width),
                        new PVector(topLeft.x, topLeft.y - format.height, topLeft.z + format.width),
                        new PVector(topLeft.x, topLeft.y - format.height, topLeft.z),
                };
            }
        }
        return null;
    }

    void draw(PGraphics g, PVector wallPos) {
        g.textureMode(PConstants.NORMAL);
        g.beginShape();
        g.texture(image);
        PVector[] vertices = getVertices(wallPos);
        g.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);
        g.vertex(vertices[1].x, vertices[1].y, vertices[1].z, 1, 0);
        g.vertex(vertices[2].x, vertices[2].y, vertices[2].z, 1, 1);
        g.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, 1);
        g.endShape(CLOSE);
    }

    public enum Rating {
        SAFE("s"),
        EXPLICIT("e");
        static final String regex = "([se])";

        final String name;
        Rating(String e) {
            this.name = e;
        }

        static Rating fromString(String name) {
            return Arrays.stream(Rating.values())
                    .filter(e -> e.name.equals(name))
                    .findAny()
                    .orElseThrow();
        }
    }

    public enum ScrollFormat {
        B2P("b2p", 550, 750),
        B2L("b2l", 750, 550),
        B1P("b1p", 750, 1050),
        B1L("b1l", 1050, 750),
        B0P("b0p", 1050, 1850),
        LONG("longp", 550, 1850);
        static final String regex = "(\\w+)";

        final String name;
        final int width;
        final int height;
        ScrollFormat(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        static ScrollFormat byName(String name) {
            return Arrays.stream(ScrollFormat.values())
                    .filter(e -> e.name.equals(name))
                    .findAny()
                    .orElseThrow();
        }
    }
}
