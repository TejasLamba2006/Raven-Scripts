String[] module = {"AntiKnockback", "KillAura", "AirVelo", "Bhop", "BedAura", "Scaffold", "Long Jump", "test", "NoFall"};
List<String> enabled = new ArrayList<>();

String[] colorModes = {"Solid", "Gradient", "Rainbow"};
String[] outlineModes = {"None", "Top", "Right", "Left", "Full"};

Color color, Gcolor, Alpha, rainbow, rainbow2;
boolean background;
boolean test;
double rbs, colorMode, outlineMode, bedauraMode, NoSlowMode, kbh, kbv, xz, yz, ScaffoldMode, BhopMode, NofallMode;

void onLoad() {
    modules.registerSlider("X-offset", 0, 0, 50, 1);
    modules.registerSlider("Y-offset", 0, 0, 50, 1);
    
    modules.registerSlider("Color Mode", 0, colorModes);
    
    modules.registerSlider("Outline", 0, outlineModes);
    modules.registerSlider("Rainbow-Speed", 1, 1, 10, 1);
    
    modules.registerSlider("Red", 170, 0, 255, 1);
    modules.registerSlider("Green", 50, 0, 255, 1);
    modules.registerSlider("Blue", 170, 0, 255, 1);
    
    modules.registerSlider("Gradient-Red", 170, 0, 255, 1);
    modules.registerSlider("Gradient-Green", 50, 0, 255, 1);
    modules.registerSlider("Gradient-Blue", 170, 0, 255, 1);
    
    
    modules.registerButton("Background", false);
    modules.registerSlider("Alpha", 1, 0, 255, 1);
}

void onPreUpdate() {
    int R = (int) modules.getSlider(scriptName, "Red");
    int G = (int) modules.getSlider(scriptName, "Green");
    int B = (int) modules.getSlider(scriptName, "Blue");
    int GR = (int) modules.getSlider(scriptName, "Gradient-Red");
    int GG = (int) modules.getSlider(scriptName, "Gradient-Green");
    int GB = (int) modules.getSlider(scriptName, "Gradient-Blue");
    int alpha = (int) modules.getSlider(scriptName, "Alpha");
    
    background = modules.getButton(scriptName, "Background");
    colorMode = modules.getSlider(scriptName, "Color Mode");
    outlineMode = modules.getSlider(scriptName, "Outline");
    rbs = modules.getSlider(scriptName, "Rainbow-Speed");
    bedauraMode = modules.getSlider("BedAura", "Break mode");
    NoSlowMode = modules.getSlider("NoSlow", "Mode");
    ScaffoldMode = modules.getSlider("Scaffold", "Rotation");
    BhopMode = modules.getSlider("Bhop", "Mode");
    NofallMode = modules.getSlider("NoFall", "Mode");
    kbh = (modules.getSlider("AntiKnockback", "Horizontal"));
    kbv = (modules.getSlider("AntiKnockback", "Vertical"));
    xz = modules.getSlider(scriptName, "X-offset");
    yz = modules.getSlider(scriptName, "Y-offset");
    
    color = new Color(R, G, B);
    Gcolor = new Color(GR, GG, GB);
    Alpha = new Color(0, 0, 0, alpha);
    rainbow = new Color(gc((long) rbs));
    rainbow2 = new Color(gc((long) rbs + 1));
    for (String name : module) {
        if (modules.isEnabled(name)) {
            if (!enabled.contains(name)) {
                enabled.add(name);
            } 
        } else if (enabled.contains(name)) {
            enabled.remove(name);
        }
    }
    
    enabled.sort((s1, s2) -> {
        String s1WithAdditionalText = addAdditionalText(s1);
        String s2WithAdditionalText = addAdditionalText(s2);
        return Integer.compare(client.getFontWidth(s2WithAdditionalText), client.getFontWidth(s1WithAdditionalText));
    });
    
    
    
}

void onRenderTick(float partialTicks) {
    if (!client.getScreen().isEmpty()) {
        return;
    }
    
    int count = 0;
    double offset = client.getDisplaySize()[0];
    float x = (float) (0.9 + offset - xz);
    float y = (float) (0.9 + yz);
    
    float line = client.getFontHeight() + 1;

    int totalModules = enabled.size();
    int previousWidth = 0;
    for (String name : enabled) {
        String prefix = addAdditionalText(name);
        if (background){
            client.render.rect((int) x, (int) y,(int) x - client.getFontWidth(prefix) - 3, (int) y + 10, Alpha.getRGB());
        }
        int currentWidth = client.getFontWidth(prefix); //thx canvas for the outline
            switch ((int) outlineMode) {
                case 1:
                    switch ((int) colorMode) {
                        case 0:
                            if (count == 0){
                                client.render.line2D(x - 1, y - 1, x - client.getFontWidth(prefix) - 5, y - 1, 2.5f, color.getRGB()); //up
                            }
                            break;
                        case 1:
                            if (count == 0){
                                renderGradientline(x - 1, y - 1, x - client.getFontWidth(prefix) - 5, y - 1, 2.5f, color, Gcolor); //up
                            }
                            break;
                        case 2:
                            if (count == 0){
                                client.render.line2D(x - 1, y - 1, x - client.getFontWidth(prefix) - 5, y - 1, 2.5f, gc((long) rbs)); //up
                            }
                            break; 
                    }
                    break;
                case 2:
                    switch ((int) colorMode) {
                        case 0:
                            client.render.line2D(x - 1, y - 1, x- 1,y + 9,2.5f,color.getRGB()); //right
                            break;
                        case 1:
                            renderGradientline(x - 1, y - 1, x- 1,y + 9,2.5f, color, Gcolor); //right
                            break;
                        case 2:
                            client.render.line2D(x - 1, y - 1, x- 1,y + 9,2.5f,gc((long) rbs));
                            break;
                    }
                    break;
                case 3:
                    switch ((int) colorMode) {
                        case 0:
                            client.render.line2D(x - client.getFontWidth(prefix) - 4, y - 1, (int) x - client.getFontWidth(prefix) - 3,y + 9,2.5f,color.getRGB()); //right
                            break;
                        case 1:
                            renderGradientline(x - client.getFontWidth(prefix) - 4, y - 1, (int) x - client.getFontWidth(prefix) - 3,y + 9,2.5f, color, Gcolor); //right
                            break;
                        case 2:
                            client.render.line2D(x - client.getFontWidth(prefix) - 4, y - 1, (int) x - client.getFontWidth(prefix) - 3,y + 9,2.5f,gc((long) rbs));
                            break;
                    }
                    break;
                case 4:
                    switch ((int) colorMode) {
                        case 0:
                            client.render.line2D(x - 0.3, y - 1.5, x - 0.3,y + 9.3,2.5f,color.getRGB()); //right
                            client.render.line2D(x - client.getFontWidth(prefix) - 5, y - 0.8, (int) x - client.getFontWidth(prefix) - 4,y + 9.5,2.5f,color.getRGB()); //left
                            if (count == 0){
                                client.render.line2D(x - 1, y - 1, x - client.getFontWidth(prefix) - 5.3, y - 1, 2.5f, color.getRGB()); //up
                            }
                            if (count == totalModules - 1){
                                client.render.line2D(x - 0.1, y + 9.7, x - client.getFontWidth(prefix) - 5.3, y + 9.7, 2.5f, color.getRGB()); //down
                            }
                            if (previousWidth != 0){
                                client.render.line2D(x - client.getFontWidth(prefix) - 5, y - 0.3, x - client.getFontWidth(prefix) - 5.3 - (previousWidth - currentWidth), y - 0.3, 2.5f, color.getRGB());
                            }
                            break;
                        case 1:
                            renderGradientline(x - 0.3, y - 1.5, x - 0.3,y + 9.3,2.5f,color, Gcolor); //right
                            renderGradientline(x - client.getFontWidth(prefix) - 5, y - 0.8, (int) x - client.getFontWidth(prefix) - 4,y + 9.5,2.5f,color, Gcolor); //left
                            if (count == 0){
                                renderGradientline(x - 1, y - 1, x - client.getFontWidth(prefix) - 5.3, y - 1, 2.5f, color, Gcolor); //up
                            }
                            if (count == totalModules - 1){
                                renderGradientline(x - 0.1, y + 9.7, x - client.getFontWidth(prefix) - 5.3, y + 9.7, 2.5f, color, Gcolor); //down
                            }
                            if (previousWidth != 0){
                                renderGradientline(x - client.getFontWidth(prefix) - 5, y - 0.3, x - client.getFontWidth(prefix) - 5.3 - (previousWidth - currentWidth), y - 0.3, 2.5f, color, Gcolor);
                            }
                            break;
                        case 3:
                            client.render.line2D(x - 0.3, y - 1.5, x - 0.3,y + 9.3,2.5f,color.getRGB()); //right
                            client.render.line2D(x - client.getFontWidth(prefix) - 5, y - 0.8, (int) x - client.getFontWidth(prefix) - 4,y + 9.5,2.5f,gc((long) rbs)); //left
                            if (count == 0){
                                client.render.line2D(x - 1, y - 1, x - client.getFontWidth(prefix) - 5.3, y - 1, 2.5f, gc((long) rbs)); //up
                            }
                            if (count == totalModules - 1){
                                client.render.line2D(x - 0.1, y + 9.7, x - client.getFontWidth(prefix) - 5.3, y + 9.7, 2.5f, gc((long) rbs)); //down
                            }
                            if (previousWidth != 0){
                                client.render.line2D(x - client.getFontWidth(prefix) - 5, y - 0.3, x - client.getFontWidth(prefix) - 5.3 - (previousWidth - currentWidth), y - 0.3, 2.5f, gc((long) rbs));
                            }
                            break;
                    }
                    break;
        }
        switch ((int) colorMode) {
            case 0:
                client.render.text(prefix, x - client.getFontWidth(prefix) - 2, y, 1, color.getRGB(), true);
                y += line;
                break;
            case 1:
                renderGradientText(prefix, x - client.getFontWidth(prefix) - 2, y, 1, color, Gcolor);
                y += line;
                break;
            case 2:
                renderGradientText(prefix, x - client.getFontWidth(prefix) - 2, y, 1, rainbow, rainbow2);
                y += line;
                break;
        }
        count++;
        previousWidth = currentWidth;
    }
}


String addAdditionalText(String name) {
    boolean test = false;
    switch (name) {
        case "KillAura":
            return name + "" + client.colorSymbol + "7 Switch";
        case "NoFall":
            return name + "" + client.colorSymbol + "7 " + NofallMode();
        case "test":
            test = true;
            return "";
        case "Bhop":
            if (test) {
                return name + "" + client.colorSymbol + "7 Lowhop";
            } else {
                return name + "" + client.colorSymbol + "7 " + BhopMode(); 
            }
        case "AirVelo":
            return "Velocity" + client.colorSymbol + "7 0% 0%";
        case "AntiKnockback":
            if (test) {
                return "Velocity" + client.colorSymbol + "7 0% 0%";
            } else {
                return "Velocity" + client.colorSymbol + "7 0% 100%";
            }
        case "BedAura":
            return name + client.colorSymbol;
        case "Long Jump":
            return "FBfly" + client.colorSymbol + "7 Distance";
        case "Scaffold":
            return name + client.colorSymbol + "7 " + ScaffoldMode();
        default:
            return name;
    }
}



String NofallMode() {
    switch((int) NofallMode) {
        case 0:
            return "Spoof";
        case 1:
            return "Extra";
        case 2:
            return "No Ground";
        default:
            return "Unknown";
    }
}



String bedauraMode() {
    switch((int) bedauraMode) {
        case 0:
            return "Legit";
        case 1:
            return "Instant";
        case 2:
            return "Swap";
        default:
            return "Swap";
    }
}

String ScaffoldMode() {
    switch((int) ScaffoldMode) {
        case 0:
            return "None";
        case 1:
            return "Backwards";
        case 2:
            return "Strict";
        case 3:
            return "Raytrace";
        default:
            return "Strict";
    }
}

String BhopMode() {
    switch((int) BhopMode) {
        case 0:
            return "Strafe";
        case 1:
            return "Ground";
        default:
            return "Ground";
    }
}




int gc(long speed, long... delay) {
    long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0);
    float hue = (time % (15000L / speed)) / (15000.0f / speed);
    return Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();
}




long startTime = System.currentTimeMillis();

void renderGradientText(String text, float x, float y, double scale, Color startColor, Color endColor) {
    int textWidth = client.getFontWidth(text);
    
    long elapsed = System.currentTimeMillis() - startTime;
    double offset = 0.6 * (Math.sin(elapsed * 0.003 - y * 0.05) + 1);  // Inverted phase adjustment
    
    Color currentColor = blendColors(startColor, endColor, offset);
    client.render.text(text, x, y, scale, currentColor.getRGB(), true);   
}


void renderGradientline(double startX, double startY, double endX, double endY, float lineWidth,Color startColor, Color endColor) {
    
    long elapsed = System.currentTimeMillis() - startTime;
    double offset = 0.6 * (Math.sin(elapsed * 0.003 - startY * 0.05) + 1);  // Inverted phase adjustment
    
    Color currentColors = blendColors(startColor, endColor, offset);
    client.render.line2D(startX,startY,endX,endY,lineWidth,currentColors.getRGB());
}


Color blendColors(Color color1, Color color2, double ratio) {
    int r = clamp((int) (color1.getRed() * ratio + color2.getRed() * (1 - ratio)), 0, 255);
    int g = clamp((int) (color1.getGreen() * ratio + color2.getGreen() * (1 - ratio)), 0, 255);
    int b = clamp((int) (color1.getBlue() * ratio + color2.getBlue() * (1 - ratio)), 0, 255);
    return new Color(r, g, b);
}

int clamp(int val, int min, int max) {
    if (val < min) return min;
    if (val > max) return max;
    return val;
}
