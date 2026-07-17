package moe.karpador.model;

import processing.core.PImage;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Wallscroll {
    public static final Pattern PATTERN = Pattern.compile("(?<id>(?<rating>" + Rating.REGEX + ")\\d+)-(?<format>" + ScrollFormat.REGEX + ")(-(?<side>front|back))?\\.\\w+");

    public String id;
    public Path path;
    public PImage image;
    public Rating rating;
    public ScrollFormat format;
    public boolean isFront;
    public Wallscroll backside;

    public Wallscroll(Path path, PImage image) {
        var filename = path.getFileName().toString();
        var matcher = PATTERN.matcher(filename);
        if (!matcher.matches())
            throw new IllegalArgumentException("Illegal image name format: Expected '" + PATTERN + "'");
        this.id = matcher.group("id");
        this.path = path;
        this.image = image;
        this.rating = Rating.fromString(matcher.group("rating"));
        this.format = Wallscroll.ScrollFormat.byName(matcher.group("format") + ((image.height > image.width) ? "p" : "l"));
        this.isFront = matcher.group("side") == null || matcher.group("side").equals("front");
    }

    public enum Rating {
        SAFE("s"),
        EXPLICIT("e");
        static final String REGEX = "[se]";

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
        B0L("b0l", 1850, 1050),
        LONG("longp", 550, 1850);
        static final String REGEX = "long|b[012]";

        final String name;
        public final int width;
        public final int height;

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
