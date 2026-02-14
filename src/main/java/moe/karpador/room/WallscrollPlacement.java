package moe.karpador.room;

import moe.karpador.WallscrollSimulator;
import moe.karpador.model.Wallscroll;
import moe.karpador.room.Room.WallSide;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.*;

import static processing.core.PConstants.CLOSE;

public class WallscrollPlacement {
    Wallscroll wallscroll;
    PVector position;
    WallSide side;

    public WallscrollPlacement(Wallscroll wallscroll, WallSide side) {
        this.wallscroll = wallscroll;
        this.side = side;
        position = new PVector(100, 100);
    }

    public WallscrollPlacement(Wallscroll wallscroll) {
        this(wallscroll, WallSide.FRONT);
    }

    public static WallscrollPlacement fromYaml(Map<String, Object> root) throws ClassCastException {
        var id = (String) root.get("id");
        var side = WallSide.fromName((String) root.get("side"));
        var position = (List<Double>) root.get("position");
        if (position.size() != 2) throw new IllegalArgumentException("Position argument expected two integer values");
        var wallscrollPlacement = new WallscrollPlacement(Objects.requireNonNull(WallscrollSimulator.getWallscrolls().get(id)), side);
        wallscrollPlacement.position.set(position.get(0).intValue(), position.get(1).intValue());
        return wallscrollPlacement;
    }

    public Map<String, Object> toYaml() {
        var root = new HashMap<String, Object>();
        root.put("id", this.wallscroll.id);
        root.put("side", this.side.toName());
        root.put("position", Arrays.asList(this.position.x, this.position.y));
        return root;
    }

    public PVector[] getVertices(PVector wallPos) {
        var format = this.wallscroll.format;
        switch (this.side) {
            case FRONT -> {
                PVector topLeft = new PVector(wallPos.x + position.x, wallPos.y - position.y, wallPos.z - 1);
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
        g.texture(this.wallscroll.image);
        PVector[] vertices = getVertices(wallPos);
        g.vertex(vertices[0].x, vertices[0].y, vertices[0].z, 0, 0);
        g.vertex(vertices[1].x, vertices[1].y, vertices[1].z, 1, 0);
        g.vertex(vertices[2].x, vertices[2].y, vertices[2].z, 1, 1);
        g.vertex(vertices[3].x, vertices[3].y, vertices[3].z, 0, 1);
        g.endShape(CLOSE);
    }
}
