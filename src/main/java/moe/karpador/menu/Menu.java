package moe.karpador.menu;

import moe.karpador.WallscrollSimulator;
import moe.karpador.view.*;
import moe.karpador.room.Room;
import moe.karpador.room.RoomView;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.List;

import static moe.karpador.menu.FileBrowser.BrowserType.ConfigBrowser;
import static moe.karpador.menu.FileBrowser.BrowserType.WallscrollBrowser;
import static processing.core.PConstants.*;

public class Menu extends View {
    private final List<ViewInstance<Button<TextView>>> buttons;

    public Menu(RoomView roomView) {
        Room room = roomView.room;
        int textSize = WallscrollSimulator.menuTextSize();
        this.buttons = List.of(
                new ViewInstance<>(new Button<>(new TextView("Return to room", textSize),
                        () -> WallscrollSimulator.popView()

                )),
                new ViewInstance<>(new Button<>(new TextView("Add wallscroll", textSize),
                        () -> WallscrollSimulator.pushView(new Container<>(new FileBrowser(WallscrollBrowser, roomView), true))

                )),
                new ViewInstance<>(new Button<>(new TextView("Load wallscroll config", textSize),
                        () -> {
                            Runnable r = () -> WallscrollSimulator.pushView(new Container<>(new FileBrowser(ConfigBrowser, roomView), true));
                            if (room.changed)
                                attemptTo("Are you sure you want to overwrite room setup? You have unsaved changes!", r);
                            else
                                r.run();
                        }

                )),
                new ViewInstance<>(new Button<>(new TextView("Save wallscrolls config", textSize),
                        () -> {
                            room.saveWallscrolls(WallscrollSimulator.getWallscrollConfigPath());
                            WallscrollSimulator.popView();
                        }

                )),
                new ViewInstance<>(new Button<>(new TextView("Quit", textSize),
                        () -> {
                            Runnable r = WallscrollSimulator::exitApp;
                            if (room.changed)
                                attemptTo("Are you sure you want to quit? You have unsaved changes!", r);
                            else
                                r.run();
                        }

                ))
        );
    }

    // VIEW FUNCTIONS
    // -----------------------------------------------------------------------------------------------------------------


    @Override
    protected PGraphics build(ViewConstraint constraint) {
        int maxWidth = (int) (1.3f*buttons.stream()
                        .map(b -> WallscrollSimulator.getTextWidth(b.v.view().text, b.v.view().textSize))
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
        for (ViewInstance<Button<TextView>> bi : buttons) {
            bi.position = new PVector(padding, offsetY);
            g.image(bi.g, bi.position.x, bi.position.y);
            offsetY += bi.g.height;
            offsetY += gap;
        }

        g.endDraw();
        return g;
    }

    @Override
    public void mousePressed(int mouseButton, PVector mouse) {
        if (mouseButton == LEFT) {
            for (ViewInstance<Button<TextView>> bi : buttons) {
                if (bi.hover(mouse)) {
                    PVector pos = bi.mousePos(mouse);
                    bi.v.mousePressed(mouseButton, pos);
                    break;
                }
            }
        }
    }

    @Override
    public boolean update(long time) {
        for (ViewInstance<Button<TextView>> bi : buttons) {
            if (bi.v.update(time)) {
                modified();
            }
        }
        return super.update(time);
    }

    // HELPERS
    // -----------------------------------------------------------------------------------------------------------------

    private void attemptTo(String warning, Runnable r) {
        ConfirmMenu confirmMenu = new ConfirmMenu(warning, WallscrollSimulator.menuTextSize(), r);
        WallscrollSimulator.pushView(new Container<>(confirmMenu, true));
    }
}
