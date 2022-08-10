package moe.karpador.room;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.Container;
import moe.karpador.view.View;
import moe.karpador.menu.Menu;
import moe.karpador.view.ViewConstraint;
import processing.core.PGraphics;
import processing.core.PVector;

import java.nio.file.Path;
import java.util.Arrays;

import static processing.core.PApplet.*;

public class RoomView extends View {
    static final int MAX_WALL_SWITCH_ATTEMPTS = 15;

    public final Room room;

    private PGraphics g;

    PVector camPos;
    PVector camDir;
    float theta, phi;
    float fov = 1.6f;

    Wallscroll selected;
    PVector selectedUV;
    int numAttempts = 0;

    public RoomView(Room room) {
        super(P3D);
        this.room = room;
        this.theta = HALF_PI;
        this.phi = HALF_PI;
        this.camPos = new PVector(room.size.x / 2, room.size.y * 2 / 5, room.size.z / 2);
        this.camDir = new PVector(sin(theta) * cos(phi), cos(theta), sin(theta) * sin(phi));
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public PGraphics build(ViewConstraint constraint) {
        g = clearG(g, (int) constraint.maxSize.x, (int) constraint.maxSize.y);
        g.beginDraw();

        g.pushMatrix();
        g.background(255);
        setCamera();
        room.draw(g);
        g.popMatrix();

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, int mouseX, int mouseY) {
        if (mouseButton == LEFT) {
            selectWallscroll(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(int mouseButton, int mouseX, int mouseY) {
        if (mouseButton == LEFT) {
            this.selected = null;
        }
    }

    @Override
    public void mouseDragged(int mouseButton, int mouseX, int mouseY, int pmouseX, int pmouseY) {
        if (mouseButton == RIGHT) {
            moveCamera(mouseX, mouseY, pmouseX, pmouseY);
            modified();
        } else if (mouseButton == LEFT) {
            if (moveSelected(mouseX, mouseY, pmouseX, pmouseY)) {
                modified();
            }
        }
    }

    @Override
    public void keyPressed(int key, int keyCode) {
        switch (keyCode) {
            case ESC -> {
                WallscrollSimulator.pushView(new Container<>(new Menu(this)));
                WallscrollSimulator.preventEscape();
            }
            case 147 -> { //DELETE
                selectWallscroll(mouseX, mouseY);
                if (selected!=null) {
                    room.removeWallscroll(selected);
                    selected = null;
                    modified();
                }
            }
            default -> {

            }
        }
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    public Room.WallSide getFacingWall() {
        return Room.WallSide.fromFacing(camDir.x, camDir.z);
    }

    public void placeWallscroll(Wallscroll wallscroll) {
        wallscroll.side = getFacingWall();
        room.addWallscroll(wallscroll);
        modified();
    }

    public void selectWallscroll(int mouseX, int mouseY) {
        g.pushMatrix();
        setCamera();
        for (Wallscroll wallscroll : room.wallscrolls) {
            PVector uv = getWallscrollUV(wallscroll, mouseX, mouseY);
            if (uv!=null) {
                this.selected = wallscroll;
                this.selectedUV = uv;
                break;
            }
        }
        g.popMatrix();
    }

    private PVector getWallscrollUV(Wallscroll wallscroll, int mouseX, int mouseY) {
        PVector mouseV = new PVector(mouseX, mouseY);
        PVector[] vertices = wallscroll.getVertices(room.getWallPos(wallscroll.side));
        for (PVector vertex : vertices) {
            vertex.set(g.screenX(vertex.x, vertex.y, vertex.z), g.screenY(vertex.x, vertex.y, vertex.z));
        }
        if (Arrays.stream(vertices).filter(this::invisible).count() >= 2) return null;
        return uxFromQuad(mouseV, vertices[0], vertices[1], vertices[2], vertices[3]);
    }

    private boolean invisible(PVector screenPos) {
        return screenPos.x < 0 || screenPos.x > g.width || screenPos.y < 0 || screenPos.y > g.height;
    }

    //https://ch.mathworks.com/matlabcentral/answers/1767600-how-to-compute-uv-coordinates-for-an-arbitrary-quad?s_tid=srchtitle
    public PVector uxFromQuad(PVector p, PVector a, PVector b, PVector c, PVector d) {
        float u1 = uvDistance(p, d, a);
        float u2 = uvDistance(p, b, c);
        float u = u1/(u1+u2);
        if (u1 < 0 || u2 < 0)
            return null;

        float v2 = uvDistance(p, a, b);
        float v1 = uvDistance(p, c, d);
        float v = v1/(v1+v2);
        if (v1 < 0 || v2 < 0)
            return null;

        return new PVector(u, v);
    }

    private float uvDistance(PVector p, PVector a, PVector b) {
        return ((b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)) / dist(a.x, a.y, b.x, b.y);
    }

    private boolean moveSelected(int mouseX, int mouseY, int pmouseX, int pmouseY) {
        if (selected == null) return false;
        g.pushMatrix();
        setCamera();
        PVector uv = getWallscrollUV(selected, mouseX, mouseY);
        PVector newPos = uv == null ?
                new PVector(selected.position.x + mouseX - pmouseX, selected.position.y + mouseY - pmouseY) :
                new PVector(selected.position.x + (uv.x - selectedUV.x)*selected.format.width, selected.position.y - (uv.y - selectedUV.y)*selected.format.height);
        if (newPos.x < 0) {
            numAttempts++;
            if (numAttempts == MAX_WALL_SWITCH_ATTEMPTS) {
                selected.side = selected.side.toLeft();
                newPos.x = room.getWidth(selected.side) - selected.format.width;
            } else {
                newPos.x = 0;
            }
        } else if (newPos.x > room.getWidth(selected.side) - selected.format.width) {
            numAttempts++;
            if (numAttempts == MAX_WALL_SWITCH_ATTEMPTS) {
                selected.side = selected.side.toRight();
                newPos.x = 0;
            } else {
                newPos.x = room.getWidth(selected.side) - selected.format.width;
            }
        } else if (newPos.y < 0) {
            newPos.y = 0;
        } else if (newPos.y > room.getHeight(selected.side) - selected.format.height) {
            newPos.y = room.getHeight(selected.side) - selected.format.height;
        } else {
            numAttempts = 0;
        }
        selected.position = newPos;
        if (numAttempts == MAX_WALL_SWITCH_ATTEMPTS) {
            numAttempts = 0;
            selected = null;
        }
        g.popMatrix();
        room.changed();
        return true;
    }

    public void moveCamera(int mouseX, int mouseY, int pmouseX, int pmouseY) {
        float xdiff = (mouseX - pmouseX) / 35.f;
        float ydiff = (mouseY - pmouseY) / 35.f;
        phi = (phi - xdiff + TWO_PI) % TWO_PI;
        theta = theta + ydiff;
        if (theta < 0.01) theta = 0.01f;
        else if (theta > (PI - 0.01)) theta = PI - 0.01f;
    }

    public void loadWallscrolls(Path config) {
        room.loadWallscrollsConfig(config);
        modified();
    }

    void setCamera() {
        float cameraZ = (g.height/2.0f) / tan(fov/2.0f);
        g.perspective(fov, (float) g.width/(float) g.height, cameraZ/10, cameraZ*10);
        camDir = new PVector(sin(theta)*cos(phi), cos(theta), sin(theta)*sin(phi));
        g.camera(camPos.x, camPos.y, camPos.z, camPos.x+camDir.x, camPos.y+camDir.y, camPos.z+camDir.z, 0, -1, 0);
    }
}
