/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager.sub;

import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager;
import net.ccbluex.liquidbounce.ui.elements.GuiPasswordField;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.login.MinecraftAccount;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class GuiDirectLogin extends GuiScreen {

    private final GuiScreen prevGui;

    private GuiButton loginButton;
    private GuiButton clipboardLoginButton;
    private GuiTextField username;
    private GuiPasswordField password;

    private String status = "§7%ui.alt.idle%";

    public GuiDirectLogin(final GuiAltManager gui) {
        this.prevGui = gui;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.add(loginButton = new GuiButton(1, width / 2 - 100, height / 4 + 72, "%ui.alt.login%"));
        buttonList.add(clipboardLoginButton = new GuiButton(2, width / 2 - 100, height / 4 + 96, "%ui.alt.clipBoardLogin%"));
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 120, "%ui.back%"));
        username = new GuiTextField(2, Fonts.font40, width / 2 - 100, 60, 200, 20);
        username.setFocused(true);
        username.setMaxStringLength(Integer.MAX_VALUE);
        password = new GuiPasswordField(3, Fonts.font40, width / 2 - 100, 85, 200, 20);
        password.setMaxStringLength(Integer.MAX_VALUE);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        drawCenteredString(Fonts.font40, "%ui.alt.directLogin%", width / 2, 34, 0xffffff);
        drawCenteredString(Fonts.font35, status == null ? "" : status, width / 2, height / 4 + 60, 0xffffff);

        username.drawTextBox();
        password.drawTextBox();

        if(username.getText().isEmpty() && !username.isFocused())
            drawCenteredString(Fonts.font40, "§7%ui.alt.loginUsername%", width / 2 - 55, 66, 0xffffff);

        if(password.getText().isEmpty() && !password.isFocused())
            drawCenteredString(Fonts.font40, "§7%ui.alt.loginPassword%", width / 2 - 74, 91, 0xffffff);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(!button.enabled) return;

        switch(button.id) {
            case 0:
                mc.displayGuiScreen(prevGui);
                break;
            case 1:
                if (username.getText().isEmpty()) {
                    status = "§c%ui.alt.fillBoth%";
                    return;
                }

                loginButton.enabled = clipboardLoginButton.enabled = false;

                new Thread(() -> {
                    status = "§a%ui.alt.loggingIn%";

                    if (password.getText().isEmpty())
                        status = GuiAltManager.login(new MinecraftAccount(ColorUtils.translateAlternateColorCodes(username.getText())));
                    else
                        status = GuiAltManager.login(new MinecraftAccount(username.getText(), password.getText()));

                    loginButton.enabled = clipboardLoginButton.enabled = true;
                }).start();
                break;
            case 2:
                try {
                    final String clipboardData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    final String[] args = clipboardData.split(":", 2);

                    if (!clipboardData.contains(":") || args.length != 2) {
                        status = "§c%ui.alt.invalidClipData%";
                        return;
                    }

                    loginButton.enabled = clipboardLoginButton.enabled = false;

                    new Thread(() -> {
                        status = "§a%ui.alt.loggingIn%";

                        status = GuiAltManager.login(new MinecraftAccount(args[0], args[1]));

                        loginButton.enabled = clipboardLoginButton.enabled = true;
                    }).start();
                } catch (final UnsupportedFlavorException e) {
                    status = "§c%ui.alt.readFailed%";
                    ClientUtils.INSTANCE.logError("Failed to read data from clipboard.", e);
                }
                break;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        switch (keyCode) {
            case Keyboard.KEY_ESCAPE:
                mc.displayGuiScreen(prevGui);
                return;
            case Keyboard.KEY_RETURN:
                actionPerformed(loginButton);
                return;
        }

        if(username.isFocused())
            username.textboxKeyTyped(typedChar, keyCode);

        if(password.isFocused())
            password.textboxKeyTyped(typedChar, keyCode);

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        username.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        username.updateCursorCounter();
        password.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }
}
