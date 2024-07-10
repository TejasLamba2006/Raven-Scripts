void onRenderTick(float partialTicks) {
    if (!client.getScreen().isEmpty()) {
        return;
    }
    String fpsText = "FPS: " + String.valueOf(client.getFPS());
    int fpsTextWidth = client.getFontWidth(fpsText);
    int fpsTextHeight = client.getFontHeight();
    int fpsTextX = 0;
    int fpsTextY = 0;

    client.render.text(fpsText, fpsTextX, fpsTextY, Color.white.getRGB(), true);
}