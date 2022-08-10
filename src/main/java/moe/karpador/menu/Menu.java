package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.Container;
import moe.karpador.view.View;
import moe.karpador.room.Room;
import moe.karpador.room.RoomView;
import moe.karpador.view.ViewConstraint;
import moe.karpador.view.ViewInstance;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.HashMap;
import java.util.List;

import static moe.karpador.menu.FileBrowser.BrowserType.ConfigBrowser;
import static moe.karpador.menu.FileBrowser.BrowserType.WallscrollBrowser;
import static processing.core.PConstants.*;

public class Menu extends View {
    private final List<ViewInstance<TextButton>> buttons;

    public Menu(RoomView roomView) {
        Room room = roomView.room;
        int textSize = WallscrollSimulator.menuTextSize();
        this.buttons = List.of(
                new ViewInstance<>(new TextButton("Return to room",
                        () -> WallscrollSimulator.popView(),
                        textSize
                )),
                new ViewInstance<>(new TextButton("Add wallscroll",
                        () -> WallscrollSimulator.pushView(new Container<>(new FileBrowser(WallscrollBrowser, roomView))),
                        textSize
                )),
                new ViewInstance<>(new TextButton("Load wallscroll config",
                        () -> {
                            Runnable r = () -> WallscrollSimulator.pushView(new Container<>(new FileBrowser(ConfigBrowser, roomView)));
                            if (room.changed)
                                attemptTo("Are you sure you want to overwrite room setup? You have unsaved changes!", r);
                            else
                                r.run();
                        },
                        textSize
                )),
                new ViewInstance<>(new TextButton("Save wallscrolls config",
                        () -> {
                            room.saveWallscrolls(WallscrollSimulator.getWallscrollConfigPath());
                            WallscrollSimulator.popView();
                        },
                        textSize
                )),
                new ViewInstance<>(new TextButton("Quit",
                        () -> {
                            Runnable r = WallscrollSimulator::exitApp;
                            if (room.changed)
                                attemptTo("Are you sure you want to quit? You have unsaved changes!", r);
                            else
                                r.run();
                        },
                        textSize
                ))
        );
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------


    @Override
    protected PGraphics build(ViewConstraint constraint) {
        int maxWidth = (int) (1.3f*buttons.stream()
                        .map(b -> WallscrollSimulator.getTextWidth(b.view.text, b.view.textSize))
                        .max(Float::compareTo)
                        .orElse(0f));
        int padding = maxWidth/5;
        int gap = padding/2;
        buttons.forEach(b -> b.draw(ViewConstraint.with(
                        new PVector(maxWidth, 0),
                        new PVector(maxWidth, (constraint.maxSize.x - gap * (buttons.size()-1)) / buttons.size())
                )));
        int totalHeight = gap*(buttons.size()-1) + buttons.stream()
                .mapToInt(b -> b.g.height)
                .sum();

        g = clearG(g, maxWidth + 2*padding, totalHeight + 2*padding);
        g.beginDraw();

        g.fill(200, 255, 230);
        g.strokeWeight(4);
        g.rect(2, 2, g.width - 4, g.height - 4);
        float offsetY = padding;
        for (ViewInstance<TextButton> bi : buttons) {
            bi.position = new PVector(padding, offsetY);
            g.image(bi.g, bi.position.x, bi.position.y);
            offsetY += bi.g.height;
            offsetY += gap;
        }

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, int mouseX, int mouseY) {
        if (mouseButton == LEFT) {
            if (mouseX >= 0 && mouseX < g.width && mouseY >= 0 && mouseY < g.height) {
                for (ViewInstance<TextButton> bi : buttons) {
                    if (bi.hover(mouseX, mouseY)) {
                        PVector pos = bi.mousePos(mouseX, mouseY);
                        bi.view.mousePressed(mouseButton, (int) pos.x, (int) pos.y);
                        break;
                    }
                }
            } else {
                WallscrollSimulator.popView();
            }
        }
    }

    @Override
    public boolean update(long time, int mouseX, int mouseY) {
        for (ViewInstance<TextButton> bi : buttons) {
            PVector pos = bi.mousePos(mouseX, mouseY);
            if (bi.view.update(time, (int) pos.x, (int) pos.y)) {
                modified();
            }
        }
        return super.update(time, mouseX, mouseY);
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    private void attemptTo(String warning, Runnable r) {
        ConfirmMenu confirmMenu = new ConfirmMenu(warning, WallscrollSimulator.menuTextSize(), r);
        WallscrollSimulator.pushView(new Container<>(confirmMenu));
    }
}
