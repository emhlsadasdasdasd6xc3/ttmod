package com.example.teamtracker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/** Drag the HUD to move it; scroll wheel or buttons to resize. Saved when closed. */
public class HudEditScreen extends Screen {
    private boolean dragging;
    private double offX, offY;

    public HudEditScreen() {
        super(Text.literal("Edit Team Tracker HUD"));
    }

    @Override
    protected void init() {
        int bw = 70, gap = 6;
        int startX = (width - (4 * bw + 3 * gap)) / 2;
        int by = height - 30;
        addDrawableChild(ButtonWidget.builder(Text.literal("Smaller"), b -> changeScale(-0.1f))
            .dimensions(startX, by, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Bigger"), b -> changeScale(0.1f))
            .dimensions(startX + (bw + gap), by, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
                HudConfig.reset();
                HudConfig.clamp(width, height);
            }).dimensions(startX + 2 * (bw + gap), by, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(startX + 3 * (bw + gap), by, bw, 20).build());
    }

    private void changeScale(float delta) {
        float s = Math.round((HudConfig.scale + delta) * 20f) / 20f;
        HudConfig.scale = MathHelper.clamp(s, HudConfig.MIN_SCALE, HudConfig.MAX_SCALE);
        HudConfig.clamp(width, height);
    }

    private boolean inBox(double mx, double my) {
        int w = (int) Math.ceil(HudConfig.boxW * HudConfig.scale);
        int h = (int) Math.ceil(HudConfig.boxH * HudConfig.scale);
        return mx >= HudConfig.x - 4 && mx <= HudConfig.x + w + 4
            && my >= HudConfig.y - 4 && my <= HudConfig.y + h + 4;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Skip the default blurred background so the preview stays crisp.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        TeamTrackerClient.drawHud(context, client, TeamTrackerClient.previewLines(client));
        HudConfig.clamp(width, height);

        int w = (int) Math.ceil(HudConfig.boxW * HudConfig.scale);
        int h = (int) Math.ceil(HudConfig.boxH * HudConfig.scale);
        int color = dragging || inBox(mouseX, mouseY) ? 0xFFFFFF55 : 0xFFFFFFFF;
        context.drawBorder(HudConfig.x - 3, HudConfig.y - 3, w + 6, h + 6, color);

        context.drawCenteredTextWithShadow(textRenderer,
            "Drag to move  -  Scroll wheel or buttons to resize  -  Esc to save", width / 2, 12, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            String.format("Size: %.2fx", HudConfig.scale), width / 2, 24, 0xFFAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0 && inBox(mouseX, mouseY)) {
            dragging = true;
            offX = mouseX - HudConfig.x;
            offY = mouseY - HudConfig.y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging) {
            HudConfig.x = (int) (mouseX - offX);
            HudConfig.y = (int) (mouseY - offY);
            HudConfig.clamp(width, height);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (vertical != 0) changeScale(vertical > 0 ? 0.1f : -0.1f);
        return true;
    }

    @Override
    public void close() {
        HudConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
