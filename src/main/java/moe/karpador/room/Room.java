package moe.karpador.room;

import moe.karpador.WallscrollSimulator;
import org.yaml.snakeyaml.Yaml;
import processing.core.PGraphics;
import processing.core.PVector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Room {
    PVector size;
    List<Furniture> furnitures;
    ArrayList<WallscrollPlacement> wallscrollPlacements;
    public boolean changed;

    public Room(PVector size, List<Furniture> furnitures) {
        this.size = size;
        this.furnitures = furnitures;
        wallscrollPlacements = new ArrayList<>();
    }

    public void draw(PGraphics g) {
        drawRoom(g);
        for (Furniture furniture : furnitures) {
            furniture.draw(g);
        }
        for (WallscrollPlacement wallscrollPlacement : wallscrollPlacements) {
            wallscrollPlacement.draw(g, getWallPos(wallscrollPlacement.side));
        }
    }

    public PVector getWallPos(WallSide side) {
        return switch (side) {
            case FRONT -> new PVector(0, size.y, size.z);
            case RIGHT -> new PVector(size.x, size.y, size.z);
            case LEFT -> new PVector(0, size.y, 0);
            case BACK -> new PVector(size.x, size.y, 0);
        };
    }

    public int getWidth(WallSide side) {
        return switch (side) {
            case FRONT, BACK -> (int) size.x;
            case LEFT, RIGHT -> (int) size.z;
        };
    }

    public int getHeight(WallSide side) {
        return (int) size.y;
    }

    public void drawRoom(PGraphics g) {
        g.fill(230);
        g.stroke(0);
        g.strokeWeight(2);
        g.pushMatrix();
        g.translate(size.x / 2, size.y / 2, size.z / 2);
        g.box(size.x, size.y, size.z);
        g.popMatrix();
    }

    public void loadWallscrollsConfig(Path path) throws ClassCastException {
        Yaml yaml = new Yaml();
        try {
            String config = Files.readString(path);
            Map<String, Object> root = yaml.load(config);
            List<WallscrollPlacement> wallscrollPlacements = ((List<Map<String, Object>>) root.get("wallscrolls")).stream()
                    .map(WallscrollPlacement::fromYaml)
                    .toList();
            clearWallscrolls();
            this.wallscrollPlacements.addAll(wallscrollPlacements);
            changed = false;
        } catch (IOException e) {
            System.err.println("Error reading file: '" + path + "'");
            e.printStackTrace();
        }
    }

    public void saveWallscrolls(Path dir) {
        String save = getWallscrollsConfig();
        SimpleDateFormat format = new SimpleDateFormat(WallscrollSimulator.getDateFormat());
        Path config = dir.resolve("wallscrolls-" + format.format(Date.from(Instant.now())) + ".yaml");
        try {
            Files.writeString(config, save);
        } catch (IOException e) {
            e.printStackTrace();
        }
        changed = false;
    }

    private String getWallscrollsConfig() {
        Yaml yaml = new Yaml();
        Map<String, Object> root = new HashMap<>();
        root.put("wallscrolls", wallscrollPlacements.stream().map(WallscrollPlacement::toYaml).toList());
        return yaml.dump(root);
    }

    public void addWallscroll(WallscrollPlacement wallscrollPlacement) {
        this.wallscrollPlacements.add(wallscrollPlacement);
        changed();
    }

    public void clearWallscrolls() {
        this.wallscrollPlacements.clear();
    }

    public void removeWallscroll(WallscrollPlacement wallscrollPlacement) {
        this.wallscrollPlacements.remove(wallscrollPlacement);
        changed();
    }

    public void changed() {
        this.changed = true;
    }

    public static Room fromYamlFile(Path path) {
        Yaml yaml = new Yaml();
        try {
            String config = Files.readString(path);
            Map<String, Object> root = yaml.load(config);
            return fromYaml(root);
        } catch (IOException e) {
            System.err.println("Error reading file: '" + path + "'");
            e.printStackTrace();
        } catch (ClassCastException e) {
            System.err.println("File has invalid root structure: '" + path + "'");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        System.exit(1);
        return null;
    }

    static Room fromYaml(Map<String, Object> root) throws IllegalArgumentException, ClassCastException {
        List<Integer> roomSizeList = (List<Integer>) root.get("size");
        List<Map<String, Object>> furnituresList = (List<Map<String, Object>>) root.get("furnitures");

        if (roomSizeList.size() != 3)
            throw new IllegalArgumentException("room size must contain three integer values [cm]");
        PVector roomSize = new PVector(roomSizeList.get(0), roomSizeList.get(1), roomSizeList.get(2));

        List<Furniture> furnitures = furnituresList.stream().map(Furniture::fromYaml).collect(Collectors.toList());

        return new Room(roomSize, furnitures);
    }

    public enum WallSide {
        FRONT("front"), BACK("back"), LEFT("left"), RIGHT("right");

        private final String name;

        WallSide(String name) {
            this.name = name;
        }

        public static WallSide fromName(String name) {
            return switch (name) {
                case "front" -> FRONT;
                case "back" -> BACK;
                case "right" -> RIGHT;
                case "left" -> LEFT;
                default -> throw new IllegalArgumentException("No such WallSide: '" + name + "'");
            };
        }

        public String toName() {
            return this.name;
        }

        WallSide toRight() {
            return switch (this) {
                case FRONT -> RIGHT;
                case RIGHT -> BACK;
                case BACK -> LEFT;
                case LEFT -> FRONT;
            };
        }

        WallSide toLeft() {
            return switch (this) {
                case FRONT -> LEFT;
                case LEFT -> BACK;
                case BACK -> RIGHT;
                case RIGHT -> FRONT;
            };
        }

        private final static float limit = 0.7071f;

        public static WallSide fromFacing(float x, float z) {
            return (x > limit) ? RIGHT
                    : (x < -limit) ? LEFT
                    : (z < -limit) ? BACK
                    : FRONT;
        }
    }
}
