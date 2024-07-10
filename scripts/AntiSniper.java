final String hypixelKey = "";

// https://api.antisniper.net/
// https://discord.gg/antisniper
String asKey = "";



int fontHeight = client.getFontHeight();
int startX = 12;
int startY = 12;
int offsetY = 3;
int lineHeight = fontHeight + offsetY;
float borderWidth = 2.5f;
int background, borderColorRGB, columnTitles;
int endY;
int endX;

final String chromeUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

final String chatPrefix = "&7[&dL&7]&r ";

boolean firstEnable = false;
boolean dowho = true;
boolean didwho = false;
boolean ascending = false;
boolean showYourself = false;
boolean showTeamPrefix = false;
boolean showTeamColors = false;
String sortBy;
int status = getBedwarsStatus();
int overlayTicks = 5;

Map<String, Map<String, Object>> overlayPlayers = new ConcurrentHashMap<>();
Map<String, String> ignoredPlayers = new HashMap<>();
ArrayList<String> currentPlayers = new ArrayList<>();
String currentLobby = "";
String lastLobby = "";

List<Map<String, Object>> columns = new ArrayList<>();
List<String> tags = new ArrayList<>();
List<String> sortingOptions = new ArrayList<>();
Map<String, String> parseSortingMode = new HashMap<>();
Map<String, List<Object[]>> playerEncounters = new HashMap<>();
Map<String, String> teams = new HashMap<>();

        final static String playerKey = "player",
                encountersKey = "seen",
                tagsKey = "tags";

        final static String encountersValue = "seenvalue",
                joinValue = "joinvalue";

void registerDefaultButtons() {
    modules.registerButton("Teams", true);
    modules.registerButton("Team Prefix", false);
    modules.registerButton("Show Yourself", false);
    modules.registerSlider("Encounters Timeout (mins)", 30, 1, 60, 1);
    modules.registerSlider("Sort By", 2, getSortingOptions());
    modules.registerSlider("Sort Mode", 0, new String[] { "Ascending", "Descending" });
    modules.registerSlider("Background Opacity", 170, 0, 255, 5);
    modules.registerSlider("Background Color", 0, 0, 360, 4);
    modules.registerSlider("Header Color", 290, 0, 360, 4);
    modules.registerSlider("Border Color", 360, 0, 360, 4);
}

void defaultSettings() {
    showYourself = modules.getButton(scriptName, "Show Yourself");
    showTeamPrefix = modules.getButton(scriptName, "Team Prefix");
    showTeamColors = modules.getButton(scriptName, "Teams");
    if (overlayPlayers.size() > 1 && (modules.getSlider(scriptName, "Sort Mode") == 0 ? true : false) != ascending) {
        ascending = modules.getSlider(scriptName, "Sort Mode") == 0 ? true : false;
        sortOverlay();
    }
    ascending = modules.getSlider(scriptName, "Sort Mode") == 0 ? true : false;
    if (overlayPlayers.size() > 1 && !parseSortingMode.get(sortingOptions.get((int) modules.getSlider(scriptName, "Sort By"))).equals(sortBy)) {
        sortBy = parseSortingMode.get(sortingOptions.get((int) modules.getSlider(scriptName, "Sort By")));
        sortOverlay();
    }
    sortBy = parseSortingMode.get(sortingOptions.get((int) modules.getSlider(scriptName, "Sort By")));
    background = getHueRGB((float) modules.getSlider(scriptName, "Background Color"), (int) modules.getSlider(scriptName, "Background Opacity"));
    columnTitles = getHueRGB((float) modules.getSlider(scriptName, "Header Color"), 255);
    borderColorRGB = getHueRGB((float) modules.getSlider(scriptName, "Border Color"), 255);
}

boolean isInOverlay(String uuid) {
    return overlayPlayers.containsKey(uuid);
}

String getChatPrefix() {
    return chatPrefix;
}

String getLobbyId() {
    return currentLobby;
}

boolean hasChangedLobby(String inputLobby) {
    return !inputLobby.equals(getLobbyId());
}

boolean hasTeamColor(String uuid) {
    return teams.containsKey(uuid);
}

int bedwarsStatus() {
    return status;
}

void addPlaceholderStats(String player, String username, boolean doName) {
    Map<String, Object> placeholderStats = new ConcurrentHashMap<>();

    for (Map<String, Object> column : columns) {
        boolean enabled = Boolean.TRUE.equals(column.get("enabled"));
        if (!enabled) continue;
        String key = column.get("key").toString();

        if (key.equals(encountersKey)) {
            placeholderStats.put(key, getSeenColor(1));
            continue;
        } else if (key.equals(playerKey)) {
            if (doName) placeholderStats.put(key, client.colorSymbol + "7" + username);
            continue;
        }

        placeholderStats.put(key, client.colorSymbol + "7-");
    }

    if (doName) overlayPlayers.put(player, placeholderStats);
    else addToOverlay(player, placeholderStats);
}

void onEnable() {
    if (!firstEnable) {
        client.print(getChatPrefix() + "&eWelcome to &3Lazify&e! Please run &3/ov&e for commands.");
        firstEnable = true;
    }
    overlayTicks = 5;
    defaultSettings();
    updateStatus();
    doColumns(false);
}

void onPreUpdate() {

    updateStatus();

    if (overlayTicks++ % 5 != 0) {
        return;
    }

    defaultSettings();
    doColumns(true);

    if (status > 1) {
        HashSet<String> currentEntityUUIDs = new HashSet<>();
        final int threshold = (int) modules.getSlider(scriptName, "Encounters Timeout (mins)") * 60000;
        for (NetworkPlayer pla : client.getWorld().getNetworkPlayers()) {
            final long currentTime = client.time();
            final String uuid = pla.getUUID().replace("-", "");
            final String displayName = pla.getDisplayName();
            final String username = pla.getName();
            if (ignoredPlayers.containsKey(username.toLowerCase())) {
                if (isInOverlay(uuid)) {
                    overlayPlayers.remove(uuid);
                    currentPlayers.remove(uuid);
                }
                continue;
            }
            currentEntityUUIDs.add(uuid);
            if (isBot(pla)) continue;
            if (isInOverlay(uuid)) {
                if (showTeamColors && status == 3 && teams.size() != overlayPlayers.size()) {
                    if (!teams.containsKey(uuid) && displayName.contains(" ")) {
                        teams.put(uuid, displayName);
                        Map<String, Object> theTeamName = new HashMap<>();
                        String nameteam = showTeamPrefix == true ? displayName : displayName.split(" ")[1];
                        theTeamName.put(playerKey, nameteam);
                        addToOverlay(uuid, theTeamName);
                    }
                }
                continue;
            }

            List<Object[]> encounters = uuid.charAt(12) == '4' ? playerEncounters.getOrDefault(uuid, new ArrayList<>()) : playerEncounters.getOrDefault(username, new ArrayList<>());
            if (!encounters.isEmpty()) {
                encounters.removeIf(encounter -> currentTime - (long) encounter[1] > threshold);
            }
            if (encounters.isEmpty() || !encounters.get(encounters.size() - 1)[0].equals(getLobbyId())) {
                encounters.add(new Object[]{getLobbyId(), currentTime});
            }
            if (uuid.charAt(12) == '4') playerEncounters.put(uuid, encounters);
            else playerEncounters.put(username, encounters);
            final String formattedencounter = getSeenColor(encounters.size());

            Map<String, Object> placeholderStats = new ConcurrentHashMap<>();

            placeholderStats.put(joinValue, (int) (client.time() / 1000) * -1);
            placeholderStats.put(encountersKey, formattedencounter);
            placeholderStats.put(encountersValue, encounters.size());
            placeholderStats.put(playerKey, displayName);

            if (uuid.charAt(12) != '4') {
                placeholderStats.put("nicked", true);
                placeholderStats.put(playerKey, username);
                overlayPlayers.put(uuid, placeholderStats);
                sortOverlay();
                continue;
            }

            overlayPlayers.put(uuid, placeholderStats);

            addPlaceholderStats(uuid, displayName, false);
            addToPlayers(uuid);
            onPlayerAdd(uuid);
        }

        if (status == 2) {
            Iterator<String> overlayIterator = overlayPlayers.keySet().iterator();
            while (overlayIterator.hasNext()) {
                String overlayUUID = overlayIterator.next();
                if (currentEntityUUIDs.contains(overlayUUID)) continue;
                if (overlayPlayers.get(overlayUUID).containsKey("manual")) continue;
                overlayIterator.remove();
                doColumns(false);
            }
        }
    }

    synchronized(currentPlayers) {
        if (status != 3) {
            Iterator<String> iterator = currentPlayers.iterator();
            while (iterator.hasNext()) {
                String uuid = iterator.next();
                if (isInOverlay(uuid)) continue;
                iterator.remove();
                doColumns(false);
            }
        }

        for (String uuid : overlayPlayers.keySet()) {
            if (currentPlayers.contains(uuid)) continue;
            currentPlayers.add(uuid.charAt(12) == '4' ? (ascending ? currentPlayers.size() : 0) : (ascending ? 0 : currentPlayers.size()), uuid);
            doColumns(false);
        }
    }
}

void onRenderTick(float partialTicks) {
    if ((!client.getScreen().isEmpty() && !client.getScreen().startsWith("GuiChat")) || overlayTicks < 5 || columns.size() == 0) return;

    client.render.rect(startX, startY, endX, endY, background);
    client.render.line2D(startX, startY, endX, startY, borderWidth, borderColorRGB);
    client.render.line2D(endX, startY, endX, endY, borderWidth, borderColorRGB);
    client.render.line2D(endX, endY, startX, startY + (endY - startY), borderWidth, borderColorRGB);
    client.render.line2D(startX, startY + (endY - startY), startX, startY, borderWidth, borderColorRGB);

    for (Map<String, Object> column : columns) {
        boolean enabled = Boolean.TRUE.equals(column.get("enabled"));
        if (!enabled) continue;

        String statKey = column.get("key").toString();
        String title = column.get("header").toString();
        int width = Integer.parseInt(column.get("width").toString());
        int maxWidth = Integer.parseInt(column.get("maxwidth").toString());
        int x = Integer.parseInt(column.get("position").toString());

        if (!statKey.equals(playerKey)) x += (maxWidth - width) / 2;
        client.render.text(title, x, startY + offsetY, 1, columnTitles, true);
    }

    int y = startY + lineHeight + 5;

    synchronized (currentPlayers) { for (String uuid : currentPlayers) {
        Map<String, Object> playerStats = overlayPlayers.get(uuid);
        if (playerStats == null) {
            overlayPlayers.remove(uuid);
            continue;
        }
        boolean isNicked = (Boolean) playerStats.getOrDefault("nicked", false);
        boolean isError = (Boolean) playerStats.getOrDefault("error", false);

        for (Map<String, Object> column : columns) {
            boolean enabled = Boolean.TRUE.equals(column.get("enabled"));
            if (!enabled) continue;

            String statKey = column.get("key").toString();
            int maxWidth = Integer.parseInt(column.get("maxwidth").toString());
            Object statValue = playerStats.get(statKey);
            String stringStatValue = String.valueOf(statValue);

            int x = Integer.parseInt(column.get("position").toString());

            if (isNicked) {
                if (!statKey.equals(playerKey) && !statKey.equals(encountersKey)) {
                    statValue = client.colorSymbol + "7-";
                } else if (!teams.containsKey(uuid) && statKey.equals(playerKey)) {
                    statValue = client.colorSymbol + 'e' + stringStatValue.replaceAll(client.colorSymbol + ".", "");
                }
            } else if (isError && (statValue == null || stringStatValue.isEmpty())) {
                statValue = client.colorSymbol + "4E";
            }

            switch (statKey) {
                case playerKey:
                    if (isNicked && !teams.containsKey(uuid)) {
                        statValue = client.colorSymbol + 'e' + stringStatValue.replaceAll(client.colorSymbol + ".", "");
                    }
                    if (isError && (statValue == null || stringStatValue.isEmpty() || stringStatValue.equals(client.colorSymbol + "7-"))) {
                        statValue = client.colorSymbol + "4E";
                    }
                    if (statValue == null || stringStatValue.isEmpty()) {
                        overlayPlayers.remove(uuid);
                        continue;
                    }
                    break;
                case tagsKey:
                    if (stringStatValue.isEmpty()) {
                        StringBuilder statValueBuilder = new StringBuilder();
                        for (String tag : tags) {
                            if (!playerStats.containsKey(tag)) continue;
                            String realTag = String.valueOf(playerStats.get(tag));
                            if (!realTag.startsWith(client.colorSymbol)) continue;
                            statValueBuilder.append(realTag);
                        }
                        statValue = statValueBuilder.length() > 0 ? statValueBuilder.toString() : isNicked ? client.colorSymbol + "7-" : null;
                    }
                    break;
                case encountersKey:
                    if (statValue == null || stringStatValue.isEmpty()) {
                        statValue = client.colorSymbol + "a1";
                    }
                    break;
            }

            String text = statValue != null ? statValue.toString() : "";
            int textWidth = client.getFontWidth(text);

            if (!statKey.equals(playerKey)) {
                x += (maxWidth - textWidth) / 2;
            }

            client.render.text(text, x, y, 1, -1, true);
        }
        y += lineHeight;
    }}
}

boolean onChat(String message) {
    String msg = client.util.strip(message);
    if (sortBy == joinValue) {
        if (dowho && ((msg.endsWith("!") && msg.contains("has joined")) || msg.startsWith("You will respawn in"))) {
            dowho = false;
            client.async(() -> {
                client.sleep(500);
                if (status > 1 && timeUntilStart() > 5) {
                    client.chat("/who");
                }
            });
            return true;
        } else if (msg.startsWith("ONLINE: ")) {
            String[] players = msg.replace("ONLINE: ", "").split(", ");
            Map<String, Integer> playerMap = new ConcurrentHashMap<>();
            int length = players.length - 1;
            for (String player : players) {
                playerMap.put(player, length--);
            }

            for (String uuid : overlayPlayers.keySet()) {
                Map<String, Object> overlayPlayer = overlayPlayers.get(uuid);
                Map<String, Object> tempMap = new ConcurrentHashMap<>();
                Object playerUsernameObj = overlayPlayer.get(playerKey);
                if (!(playerUsernameObj instanceof String)) continue;
                String playerUsername = client.util.strip((String) playerUsernameObj);
                if (!playerMap.containsKey(playerUsername)) continue;
                int join = playerMap.get(playerUsername);
                tempMap.put(joinValue, join);
                addToOverlay(uuid, tempMap);
            }

            if (!didwho) {
                didwho = true;
                client.log("[CHAT] " + msg);
                return false;
            }
        }
    }
    return true;
}

boolean onPacketSent(CPacket packet) {
    if (packet instanceof C01) {
        C01 c01 = (C01) packet;
        if (!c01.message.startsWith("/ov")) return true;
        String[] parts = c01.message.split(" ");
        if (parts.length <= 1) {
            String title = " &eLazify Overlay &7";
            String footerText = " &eMade by Pug &7";
            String[] messages = {
                    "&3/ov clearhidden&e: Clears the hidden player list.",
                    "&3/ov hide [username]&e: Hides player from the overlay.",
                    "&3/ov reload&e: Reloads the players displayed on the overlay.",
                    "&3/ov sc [username]&e: Manually adds a player to the overlay."
            };

            int maxPixelWidth = 0;
            String strippedTitle = title.replaceAll("&[0-9a-fk-or]", "");
            String strippedFooterText = footerText.replaceAll("&[0-9a-fk-or]", "");
            for (String message : messages) {
                String strippedMessage = message.replaceAll("&[0-9a-fk-or]", "");
                int messageWidth = client.getFontWidth(strippedMessage);
                if (messageWidth > maxPixelWidth) {
                    maxPixelWidth = messageWidth;
                }
            }
            int titleWidth = client.getFontWidth(strippedTitle);
            int footerTextWidth = client.getFontWidth(strippedFooterText);
            if (titleWidth > maxPixelWidth) maxPixelWidth = titleWidth;
            if (footerTextWidth > maxPixelWidth) maxPixelWidth = footerTextWidth;

            int headerFooterWidth = maxPixelWidth + client.getFontWidth("  ");
            int titlePaddingTotal = headerFooterWidth - titleWidth;
            int footerPaddingTotal = headerFooterWidth - footerTextWidth;
            int titlePaddingSides = titlePaddingTotal / 2;
            int footerPaddingSides = footerPaddingTotal / 2;

            String header = "&7" + generatePadding('-', titlePaddingSides) + title + generatePadding('-', titlePaddingSides);
            if (titlePaddingTotal % 2 != 0) header += "-";
            client.print(getChatPrefix() + header);

            for (String message : messages) {
                String strippedMessage = message.replaceAll("&[0-9a-fk-or]", "");
                int messagePixelWidth = client.getFontWidth(strippedMessage);
                int totalPaddingWidth = maxPixelWidth - messagePixelWidth;
                int paddingLeftWidth = totalPaddingWidth / 2;

                String paddedMessage = generatePadding(' ', paddingLeftWidth) + message;
                client.print(getChatPrefix() + paddedMessage);
            }

            String footer = "&7" + generatePadding('-', footerPaddingSides) + footerText + generatePadding('-', footerPaddingSides);
            if (footerPaddingTotal % 2 != 0) footer += "-";
            client.print(getChatPrefix() + footer);

            return false;
        }

        String command = parts[1];

        if (command.equalsIgnoreCase("sc")) {
            if (parts.length < 3) {
                client.print(getChatPrefix() + "&eInvalid syntax. Use &3/ov sc [username]&e.");
                return false;
            }

            String player = parts[2];

            client.async(() -> {
                String[] conversion = convertPlayer(player);
                String uuid = conversion[0];
                String username = conversion[1];

                if (uuid == null || uuid.isEmpty()) {
                    String[] conversion2 = convertPlayerPlayerdb(player);
                    uuid = conversion2[0];
                    username = conversion2[1];
                    if (uuid == null || uuid.isEmpty()) {
                        client.print(getChatPrefix() + "&eFailed to convert &3" + player);
                        return;
                    }
                }

                synchronized(currentPlayers) {
                    overlayPlayers.remove(uuid);
                    currentPlayers.remove(uuid);
                    addPlaceholderStats(uuid, username, true);
                    addToPlayers(uuid);

                    Map<String, Object> manual = new ConcurrentHashMap<>();
                    manual.put("manual", true);
                    addToOverlay(uuid, manual);

                    onManualPlayerAdd(uuid);
                    client.print(getChatPrefix() + "&eAdded &3" + username + " &eto the overlay.");
                }
            });
            return false;
        } else if (command.equalsIgnoreCase("hide")) {
            if (parts.length > 2) {
                String ign = parts[2].toLowerCase();
                ignoredPlayers.put(ign, "");
                client.print(getChatPrefix() + "&3" + parts[2] + "&e is now hidden.");
            } else {
                client.print(getChatPrefix() + "&eInvalid syntax. Use &3/ov hide [username]&e.");
            }
            return false;
        } else if (command.equalsIgnoreCase("clearhidden")) {
            String msg = getChatPrefix() + "&eCleared &3" + ignoredPlayers.size() + "&e player";
            msg += ignoredPlayers.size() != 1 ? "s." : ".";
            ignoredPlayers.clear();
            client.print(msg);
            return false;
        } else if (command.equalsIgnoreCase("reload")) {
            List<String> players = new ArrayList<>(overlayPlayers.keySet());
            clearMaps();
            for (String player : players) {
                addPlaceholderStats(player, client.colorSymbol + "7-", true);
                addToPlayers(player);
                onPlayerAdd(player);
            }
            overlayTicks = 5;
            String msg = getChatPrefix() + "&eReloaded &3" + players.size() + "&e player";
            msg += ignoredPlayers.size() != 1 ? "s." : ".";
            client.print(msg);
            return false;
        }
    }
    return true;
}

String generatePadding(char character, int pixelWidth) {
    StringBuilder builder = new StringBuilder();
    int charWidth = client.getFontWidth(String.valueOf(character));
    int numChars = pixelWidth / charWidth;
    for (int i = 0; i < numChars; i++) {
        builder.append(character);
    }
    return builder.toString();
}

void onWorldJoin(Entity entity) {
    if (client.getPlayer() == entity) {
        dowho = true;
        didwho = false;
        overlayTicks = 0;
        clearMaps();
        onWorldSwap();
    }
}

void addToOverlay(String uuid, Map<String, Object> newData) {
    try {

        Map<String, Object> existingData = overlayPlayers.get(uuid);
        if (existingData == null) {
            return;
        }

        existingData.putAll(newData);
        overlayPlayers.put(uuid, existingData);
        doColumns(false);
        sortOverlay();
    } catch (Exception e) {
        client.log("Error in addToOverlay: " + e);
        client.print(getChatPrefix() + "&eError detected. Please check &3latest.log&e.");
    }
}

Comparator<String> comparator = (uuid1, uuid2) -> {
    try {
        Map<String, Object> stats1 = overlayPlayers.get(uuid1);
        Map<String, Object> stats2 = overlayPlayers.get(uuid2);

        boolean isNicked1 = stats1 != null && Boolean.TRUE.equals(stats1.get("nicked"));
        boolean isNicked2 = stats2 != null && Boolean.TRUE.equals(stats2.get("nicked"));

        if (!sortBy.equals(joinValue)) {
            if (isNicked1 && !isNicked2) {
                return ascending ? -1 : 1;
            } else if (!isNicked1 && isNicked2) {
                return ascending ? 1 : -1;
            }
        }

        String val1 = (stats1 != null && stats1.get(sortBy) != null) ? stats1.get(sortBy).toString() : "-";
        String val2 = (stats2 != null && stats2.get(sortBy) != null) ? stats2.get(sortBy).toString() : "-";

        val1 = val1.replaceAll(client.colorSymbol + ".", "");
        val2 = val2.replaceAll(client.colorSymbol + ".", "");

        boolean isVal1Numeric = containsDigit(val1);
        boolean isVal2Numeric = containsDigit(val2);

        if (!isVal1Numeric && !isVal2Numeric) {
            return 0;
        } else if (!isVal1Numeric) {
            return ascending ? 1 : -1;
        } else if (!isVal2Numeric) {
            return ascending ? -1 : 1;
        }

        double num1 = Double.parseDouble(val1);
        double num2 = Double.parseDouble(val2);
        return ascending ? Double.compare(num2, num1) : Double.compare(num1, num2);
    } catch (NumberFormatException e) {
        client.log("NumberFormatException for " + uuid1 + " or " + uuid2);
        client.print(getChatPrefix() + "&eError detected. Please check &3latest.log&e.");
        return ascending ? -1 : 1;
    }
};

boolean containsDigit(String s) {
    for (char c : s.toCharArray()) { if (Character.isDigit(c)) return true; }
    return false;
}

String[] convertPlayer(String player) {
    boolean isUUID = player.length() < 37 && (player.length() == 32 && player.charAt(12) == '4') || (player.length() == 36 && player.charAt(14) == '4');
    String url = isUUID ? "https://sessionserver.mojang.com/session/minecraft/profile/" + player : "https://api.mojang.com/users/profiles/minecraft/" + player;
    try {
        Object[] conversionResponse = get(url, 3000);
        if ((int)conversionResponse[1] == 200) {
            Json jsonData = (Json)conversionResponse[0];
            String uuid = jsonData.get("id", "");
            String username2 = jsonData.get("name", "");
            return new String[] { uuid, username2 };
        } else {
            //client.print(getChatPrefix() + "&eHTTP Error &3" + conversionResponse[1] + " &ewhile getting uuid.");
            client.log("HTTP Error " + conversionResponse[1] + " getting uuid on " + player);
            return new String[] { "", "" };
        }
    } catch (Exception e) {
        client.print(getChatPrefix() + "&eRuntime error while getting uuid.");
        client.log("Runtime error getting uuid on " + player + ": " + e);
        return new String[] { "", "" };
    }
}

String[] convertPlayerPlayerdb(String player) {
    String url = "https://playerdb.co/api/player/minecraft/" + player;
    try {
        Object[] conversionResponse = get(url, 3000);
        if ((int)conversionResponse[1] == 200) {
            Json jsonData = (Json)conversionResponse[0];
            Json thing = jsonData.object().object("data").object("player");
            String uuid = thing.get("raw_id", "");
            String username2 = thing.get("username", "");
            return new String[] { uuid, username2 };
        } else {
            //client.print(getChatPrefix() + "&eHTTP Error &3" + conversionResponse[1] + " &ewhile getting uuid.");
            client.log("HTTP Error " + conversionResponse[1] + " getting uuid on " + player);
            return new String[] { "", "" };
        }
    } catch (Exception e) {
        client.print(getChatPrefix() + "&eRuntime error while getting uuid.");
        client.log("Runtime error getting uuid on " + player + ": " + e);
        return new String[] { "", "" };
    }
}

Object[] get(String url, int timeout) {
    Json jsonData = new Json("{}");
    try {
        Request request = new Request("GET", url);
        request.setConnectTimeout(timeout);
        request.setReadTimeout(timeout);
        request.setUserAgent(chromeUserAgent);
        Response response = request.fetch();

        int code = response != null ? response.code() : 404;

        if (code == 200) jsonData = response.json();
        return new Object[] { jsonData, code };
    } catch (Exception e) {
        client.log("Error in get function: " + e);
        client.print(getChatPrefix() + "&eError detected. Please check &3latest.log&e.");
        return new Object[] { jsonData, 500 };
    }
}

int getBedwarsStatus() {
    World world = client.getWorld();
    List<String> sidebar = world.getScoreboard();
    if (sidebar == null) {
        if (world.getDimension().equals("The End")) {
            return 0;
        }
        return -1;
    }

    int size = sidebar.size();
    if (size < 7) return -1;

    if (!client.util.strip(sidebar.get(0)).startsWith("BED WARS")) {
        return -1;
    }

    String lobbyId = client.util.strip(sidebar.get(1)).split("  ")[1];
    if (lobbyId.charAt(lobbyId.length() - 1) == ']') {
        lobbyId = lobbyId.split(" ")[0];
    }
    currentLobby = lobbyId;

    if (lobbyId.charAt(0) == 'L') {
        return 1;
    }

    if (client.util.strip(sidebar.get(5)).startsWith("R Red:") && client.util.strip(sidebar.get(6)).startsWith("B Blue:")) {
        return 3;
    }

    String six = client.util.strip(sidebar.get(6));
    if (six.equals("Waiting...") || six.startsWith("Starting in")) {
        return 2;
    }

    return -1;
}

int getChroma(long speed, int alpha) {
    float hue = client.time() % (15000L / speed) / (15000.0f / speed);
    Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
    return (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
}

void sortOverlay() {
    synchronized(currentPlayers) {
        currentPlayers.sort(comparator);
    }
}

int getHueRGB(float hue, int alpha) {
    if (hue == 0) {
        return new Color(0, 0, 0, alpha).getRGB();
    } else if (hue == 360) {
        return getChroma(1L, alpha);
    } else {
        Color color = Color.getHSBColor(hue / 360.0f, 1.0f, 1.0f);
        return (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }
}

boolean isBot(NetworkPlayer pla) {
    final String uuid = pla.getUUID();

    if (pla.getPing() > 1) {
        return true;
    }

    if (pla.getName().length() < 2) {
        return true;
    }

    if (uuid.charAt(14) != '4' && uuid.charAt(14) != '1') {
        return true;
    }

    if (overlayTicks < 80 && pla.getDisplayName().startsWith(client.colorSymbol + "c")) {
        return true;
    }

    if (!showYourself) {
        if (uuid.equals(client.getPlayer().getUUID())) {
            return true;
        }
    }

    if (status == 3 && !pla.getDisplayName().contains(" ")) {
        return true;
    }

    return false;
}

int timeUntilStart() {
    List<String> scoreboard = client.getWorld().getScoreboard();
    if (scoreboard == null || scoreboard.size() < 7) return -1;
    String line = client.util.strip(scoreboard.get(6));
    if (!line.startsWith("Starting in ")) {
        if (line.equals("Waiting...")) return 20;
        return -1;
    }
    String[] parts = line.split(" ");
    String lastPart = parts[parts.length - 1];
    if (!lastPart.endsWith("s")) return -1;
    return Integer.parseInt(lastPart.substring(0, lastPart.length() - 1));
}

void clearMaps() {
    teams.clear();
    overlayPlayers.clear();
    synchronized(currentPlayers) {
        currentPlayers.clear();
    }
}

void addColumn(String display, String header, String key) {
    Map<String, Object> columnData = new HashMap<>();
    columnData.put("display", display);
    columnData.put("header", header);
    columnData.put("key", key);
    columnData.put("width", client.getFontWidth(header));
    columnData.put("maxwidth", client.getFontWidth(header));
    columnData.put("position", 0);
    columnData.put("enabled", false);

    columns.add(columnData);

    modules.registerButton(display, true);
}

void addSortingOption(String display, String key) {
    sortingOptions.add(display);
    parseSortingMode.put(display, key);
}

String[] getSortingOptions() {
    if (sortingOptions.size() == 0) {
        sortingOptions.add("Empty");
        parseSortingMode.put("Empty", "Empty");
    }

    String[] array = new String[sortingOptions.size()];
    return sortingOptions.toArray(array);
}

boolean isColumnEnabled(String key) {
    for (Map<String, Object> column : columns) {
        if (key.equals(column.get("key"))) {
            return Boolean.TRUE.equals(column.get("enabled"));
        }
    }
    return false;
}

void addTag(String newTag) {
    tags.add(newTag);
}

void doColumns(boolean updateEnabled) {
    int currentX = startX + 5;

    for (Map<String, Object> column : columns) {
        int longest = Integer.parseInt(column.get("width").toString());
        String statKey = column.get("key").toString();

        boolean enabled;
        if (updateEnabled) {
            enabled = modules.getButton(scriptName, column.get("display").toString());
            column.put("enabled", enabled);
        } else {
            enabled = Boolean.TRUE.equals(column.get("enabled"));
        }

        if (!enabled) continue;

        synchronized (currentPlayers) { for (String uuid : currentPlayers) {
            if (uuid == null) continue;

            Map<String, Object> playerData = overlayPlayers.get(uuid);
            if (playerData == null) continue;

            Object statValueObj = playerData.get(statKey);
            if (statValueObj == null) continue;
            String statValue;

            if (statKey.equals(tagsKey)) {
                StringBuilder statValueBuilder = new StringBuilder();
                for (String tag : tags) {
                    Object tagObj = playerData.get(tag);
                    if (tagObj == null) continue;
                    statValueBuilder.append(tagObj.toString());
                }
                statValue = statValueBuilder.length() > 0 ? statValueBuilder.toString() : "";
            } else {
                statValue = statValueObj.toString();
            }

            int width = client.getFontWidth(statValue);
            if (width > longest) {
                longest = width;
            }
        }};

        column.put("maxwidth", longest);
        column.put("position", currentX);
        currentX += longest + 5;
    }

    endX = currentX;
    endY = startY + lineHeight + (currentPlayers.size() * lineHeight) + (currentPlayers.size() > 0 ? 6 : 1);
}

void updateStatus() {
    lastLobby = getLobbyId();
    status = getBedwarsStatus();
    if (!lastLobby.equals(getLobbyId())) {
        clearMaps();
    }
}

void addToPlayers(String uuid) { synchronized(currentPlayers) {
    if (ascending) {
        if (uuid.charAt(12) == '4') currentPlayers.add(uuid);
        else currentPlayers.add(0, uuid);
    } else {
        if (uuid.charAt(12) == '4') currentPlayers.add(0, uuid);
        else currentPlayers.add(uuid);
    }
    doColumns(false);
}}

String getSeenColor(int encounters) {
    if (encounters > 5) {
        return client.colorSymbol + 'c' + String.valueOf(encounters);
    } else if (encounters > 3) {
        return client.colorSymbol + '6' + String.valueOf(encounters);
    } else if (encounters > 1) {
        return client.colorSymbol + 'e' + String.valueOf(encounters);
    } else {
        return client.colorSymbol + 'a' + String.valueOf(encounters);
    }
}

final String useragent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

Map<String, Map<String, Object>> statsCache = new ConcurrentHashMap<>();
Map<String, Map<String, Object>> pingCache = new ConcurrentHashMap<>();

final static String starKey = "star",
                    fkdrKey = "fkdr",
                    winstreakKey = "winstreaks",
                    sessionKey = "session",
                    pingKey = "ping";

final static String starValue = "starvalue",
                    fkdrValue = "fkdrvalue",
                    indexValue = "indexvalue",
                    pingValue = "indexvalue",
                    sessionValue = "sessionvalue",
                    winstreakValue = "winstreakvalue";

void onLoad() {
    if (hypixelKey.isEmpty()) {
        client.print(getChatPrefix() + "&eNo hypixel api key declared.");
    }
    if (asKey.isEmpty()) {
        client.print(getChatPrefix() + "&eNo antisniper api key declared.");
    }

    /* 
    - add your columns here
    - addColumn(Button Display Name, Column Title, Stats Key); 
    */

    addColumn("Encounters", "[E]", encountersKey);
    addColumn("Username", "[PLAYER]", playerKey);
    modules.registerButton("Show Ranks", false);
    addColumn("Star", "[STAR]", starKey);
    addColumn("FKDR", "[FKDR]", fkdrKey);
    addColumn("Winstreaks", "[WS]", winstreakKey);
    modules.registerSlider("Winstreak Mode", 0, new String[] { "Overall", "Solos", "Doubles", "Threes", "Fours", "4v4" });
    addColumn("Tags", "[TAGS]", tagsKey);
    addColumn("Session", "[SESSION]", sessionKey);

    /*
    - add sorting options here
    - if you want to sort a custom value, create a value key to use for it and store it as a double/int under that key
    */

    addSortingOption("Encounters", encountersValue);
    addSortingOption("Star", starValue);
    addSortingOption("FKDR", fkdrValue);
    addSortingOption("Index", indexValue);
    addSortingOption("Winstreak", winstreakValue);
    addSortingOption("Join Time", joinValue);

    /* 
    - add tags in the order which they will be displayed 
    - store your tag string under the key you registered the tag with
    - addTag("example");
    - then, you would store your tag you want to render under "example" when you parse
    */

    addTag("nofinaldeaths");
    addTag("language");
    
    registerDefaultButtons(); // don't touch
}

void onPlayerAdd(String uuid) {
    handlePlayerStats(uuid, getLobbyId());
}

void onManualPlayerAdd(String uuid) {
    statsCache.remove(uuid);
    handlePlayerStats(uuid, getLobbyId());
}

void onWorldSwap() {

}

void handlePlayerStats(String uuid, String lobby) {
    Map<String, Object> cachedStats = statsCache.get(uuid);
    if (cachedStats == null || client.time() > (long) cachedStats.get("cachetime")) {
        if (cachedStats != null) statsCache.remove(uuid);
        client.async(() -> {
            Map<String, Object> playerStats = new ConcurrentHashMap<>();
            try {
                String url = "https://api.hypixel.net/v2/player?key=" + hypixelKey + "&uuid=" + uuid;
                Object[] playerStatsRequest = get(url, 3000);
                if ((int)playerStatsRequest[1] == 200) {
                    playerStats = parseStats((Json)playerStatsRequest[0], uuid);
                } else {
                    client.print(getChatPrefix() + "&eHTTP Error &3" + playerStatsRequest[1] + " &ewhile getting stats.");
                    client.log("HTTP Error " + playerStatsRequest[1] + " getting stats on " + uuid);
                    playerStats.put("error", true);
                }
            } catch (Exception e) {
                client.print(getChatPrefix() + "&eRuntime error while getting stats.");
                client.log("Runtime error getting stats on " + uuid + ": " + e);
                playerStats.put("error", true);
            }

            if (isInOverlay(uuid) && !hasChangedLobby(lobby)) addToOverlay(uuid, playerStats);
        });
    } else {
        addToOverlay(uuid, cachedStats);
    }
}

Map<String, Object> parseStats(Json jsonData, String uuid) {
    Map<String, Object> stats = new ConcurrentHashMap<>();
    
    try {
        Json data = jsonData.object();
        if (jsonData.string().equals("{\"success\":true,\"player\":null}") || !data.object("player").exists()) {
            stats.put("nicked", true);
            return stats;
        }

        Json playerObject = data.object("player");

        String username = playerObject.get("displayname", "");
        String rank = modules.getButton(scriptName, "Show Ranks") ? getFormattedRank(jsonData) : getRankColor(getRank(jsonData));
        String coloredUsername = rank.length() == 2 ? rank + username : rank + " " + username;
        if (bedwarsStatus() <= 2 && !hasTeamColor(uuid)) {
            stats.put(playerKey, coloredUsername);
        }
        
        boolean hasBedwarsStats = false;
        if (playerObject.object("stats").exists() && playerObject.object("stats").object("Bedwars").exists()) {
            hasBedwarsStats = true;
        }

        String language = playerObject.get("userLanguage", "ENGLISH");
        if (!language.equals("ENGLISH")) {
            stats.put("language", client.colorSymbol + "3L");
        }

        Json bedwarsObject = hasBedwarsStats ? playerObject.object("stats").object("Bedwars") : new Json("{}");

        int star = (int) Math.floor(expToStars((int)Double.parseDouble(bedwarsObject.get("Experience", "0"))));
        String coloredStar = getPrestigeColor(star);

        double finalKills = Double.parseDouble(bedwarsObject.get("final_kills_bedwars", "0"));
        double finalDeaths = Double.parseDouble(bedwarsObject.get("final_deaths_bedwars", "0"));
        if (finalDeaths == 0) stats.put("nofinaldeaths", client.colorSymbol + "5Z");

        double fkdr = finalDeaths == 0 ? finalKills : finalKills / finalDeaths < 10 ? client.util.round(finalKills / finalDeaths, 2) : client.util.round(finalKills / finalDeaths, 1);
        double index = star * Math.pow(fkdr, 2);
        String coloredFkdr = getFkdrColor(formatDoubleStr(fkdr));
    
        long lastLogin = Long.parseLong(playerObject.get("lastLogin", "0"));
        long lastLogout = Long.parseLong(playerObject.get("lastLogout", "0"));
        boolean statusOn = lastLogin != 0;
        String coloredSession = client.colorSymbol + "cAPI";
        if (statusOn) {
            if (lastLogin - lastLogout > -10000) {
                lastLogout = client.time();
                coloredSession = calculateRelativeTimestamp(lastLogin, lastLogout);
                coloredSession = getSessionColor(lastLogin, lastLogout, coloredSession);
            } else {
                coloredSession = client.colorSymbol + "cOFFLINE";
            }
        }

        String winstreakMode = parseWinstreakMode((int)modules.getSlider(scriptName, "Winstreak Mode"));
        boolean winstreaksDisabled = Integer.parseInt(bedwarsObject.get("games_played_bedwars_1", "0")) > 0 && bedwarsObject.get(winstreakMode + "winstreak", "").isEmpty();
        int winstreak = 0;
        if (winstreaksDisabled && asKey.length() == 36 && modules.getButton(scriptName, "Winstreaks")) {
            Object[] resp = get("https://api.antisniper.net/v2/player/winstreak?key=" + asKey + "&player=" + uuid, 3000);
            if ((int)resp[1] == 200) {
                Json wsResponse = (Json)resp[0];
                String mode = winstreakMode.isEmpty() ? "overall_winstreak" : winstreakMode + "winstreak";
                winstreak = Integer.parseInt(wsResponse.object().get(mode, "0"));
            } else if ((int)resp[1] == 403) {
                client.print(getChatPrefix() + "&eAntisniper key has been changed or is invalid.");
                asKey = "";
            }
        } else {
            winstreak = Integer.parseInt(bedwarsObject.get(winstreakMode + "winstreak", "0"));
        }
        String coloredWinstreak = getWinstreakColor(String.valueOf(winstreak));
        boolean highWinstreak = winstreak > 50;

        stats.put(starKey, coloredStar);
        stats.put(starValue, star);
        stats.put(fkdrKey, coloredFkdr);
        stats.put(fkdrValue, fkdr);
        stats.put(winstreakKey, coloredWinstreak);
        stats.put(winstreakValue, winstreak);
        stats.put(sessionKey, coloredSession);
        stats.put(sessionValue, lastLogin * -1);
        stats.put(indexValue, index);
        stats.put(tagsKey, "");
        long CACHE_DURATION = highWinstreak ? 600000 : Math.max(300, Math.min(86400, 60 * (60 * ((int) finalDeaths / 120)))) * 1000L;
        stats.put("cachetime", client.time() + CACHE_DURATION);
        statsCache.put(uuid, stats);
    } catch (Exception e) {
        client.log("Error in parseStats function on " + uuid + ": " + e);
        client.print(getChatPrefix() + "&eError detected. Please check &3latest.log&e.");
        stats.put("error", true);
    }

    return stats;
}

String parseWinstreakMode(int i) {
    switch (i) {
        case 0: return "";
        case 1: return "eight_one_";
        case 2: return "eight_two_";
        case 3: return "four_three_";
        case 4: return "four_four_";
        case 5: return "two_four_";
        default: return "";
    }
}

double expToStars(int exp) {
    int levelBase = (exp / 487000) * 100;
    int expMod = exp % 487000;
    int[][] levels = {
        {7000, 4, 5000},
        {3500, 3, 3500},
        {1500, 2, 2000},
        {500, 1, 1000},
        {0, 0, 500}
    };
    
    for (int[] lvl : levels) {
        if (expMod < lvl[0]) continue;
        double result = levelBase + lvl[1] + ((double) (expMod - lvl[0]) / lvl[2]);
        return result;
    }
    
    return 0;
}

String getRank(Json playerData) {
    if (playerData == null) return null;
    if (!playerData.object("player").exists()) {
        return client.colorSymbol + "7";
    }

    Json player = playerData.object("player");
    
    String prefix = player.get("prefix", null);
    if (prefix != null) {
        return client.util.strip(prefix.substring(4, prefix.length() - 1));
    }

    String rank = player.get("rank", null);
    if (rank != null) {
        if (rank.equals("GAME_MASTER")) return "GM";
        if (rank.equals("YOUTUBER")) return "YOUTUBE";
        if (!rank.equals("NORMAL")) return rank;
    }

    String packageRank = player.get("newPackageRank", player.get("packageRank", null));

    if (packageRank == null || packageRank.equals("NONE")) return client.colorSymbol + "7";
    if (packageRank.startsWith("MVP")) {
        return player.get("monthlyPackageRank", "").equals("SUPERSTAR") ? "MVP++" : packageRank.length() == 3 ? packageRank : "MVP+";
    }
    if (packageRank.startsWith("VIP")) {
        return packageRank.length() == 3 ? packageRank : "VIP+";
    }

    return client.colorSymbol + "7";
}

String getFormattedRank(Json playerData) {
    String rank = getRank(playerData);

    String colorCode = client.colorSymbol + "7";
    if (playerData == null || rank.equals("")) return colorCode;

    Json player = playerData.object("player");
    String plusColor = player.get("rankPlusColor", "RED");

    switch (plusColor) {
        case "BLACK": colorCode = client.colorSymbol + "0"; break;
        case "DARK_BLUE": colorCode = client.colorSymbol + "1"; break;
        case "DARK_GREEN": colorCode = client.colorSymbol + "2"; break;
        case "DARK_AQUA": colorCode = client.colorSymbol + "3"; break;
        case "DARK_RED": colorCode = client.colorSymbol + "4"; break;
        case "DARK_PURPLE": colorCode = client.colorSymbol + "5"; break;
        case "GOLD": colorCode = client.colorSymbol + "6"; break;
        case "GRAY": colorCode = client.colorSymbol + "7"; break;
        case "DARK_GRAY": colorCode = client.colorSymbol + "8"; break;
        case "BLUE": colorCode = client.colorSymbol + "9"; break;
        case "GREEN": colorCode = client.colorSymbol + "a"; break;
        case "AQUA": colorCode = client.colorSymbol + "b"; break;
        case "RED": colorCode = client.colorSymbol + "c"; break;
        case "LIGHT_PURPLE": colorCode = client.colorSymbol + "d"; break;
        case "YELLOW": colorCode = client.colorSymbol + "e"; break;
        case "WHITE": colorCode = client.colorSymbol + "f"; break;
    }

    switch (rank) {
        case "VIP":
            return client.colorSymbol + "a[VIP]";
        case "VIP+":
            return client.colorSymbol + "a[VIP" + client.colorSymbol + "6+" + client.colorSymbol + "a]";
        case "MVP":
            return client.colorSymbol + "b[MVP]";
        case "MVP+":
            return client.colorSymbol + "b[MVP" + colorCode + "+" + client.colorSymbol + "b]";
        case "MVP++":
            return client.colorSymbol + "6[MVP" + colorCode + "++" + client.colorSymbol + "6]";
        case "GM":
            return client.colorSymbol + "2[GM]";
        case "YOUTUBE":
            return client.colorSymbol + "c[" + client.colorSymbol + "fYOUTUBE" + client.colorSymbol + "c]";
        case "ADMIN":
            return client.colorSymbol + "c[ADMIN]";
        case "OWNER":
            return client.colorSymbol + "c[OWNER]";
        case "PIG+++":
            return client.colorSymbol + "d[PIG" + client.colorSymbol + "b+++" + client.colorSymbol + "d]";
        default:
            return client.colorSymbol + "7";
    }
}

String getWinstreakColor(String winstreak) {
    if (winstreak.isEmpty()) {
        return "";
    }

    int realwinstreak = Integer.parseInt(winstreak);
    if (realwinstreak == 0) {
        return "";
    }

    if (realwinstreak >= 1000) {
        return client.colorSymbol + '5' + realwinstreak;
    } else if (realwinstreak >= 500) {
        return client.colorSymbol + 'd' + realwinstreak;
    } else if (realwinstreak >= 300) {
        return client.colorSymbol + '4' + realwinstreak;
    } else if (realwinstreak >= 150) {
        return client.colorSymbol + 'c' + realwinstreak;
    } else if (realwinstreak >= 100) {
        return client.colorSymbol + '6' + realwinstreak;
    } else if (realwinstreak >= 75) {
        return client.colorSymbol + 'e' + realwinstreak;
    } else if (realwinstreak >= 50) {
        return client.colorSymbol + '2' + realwinstreak;
    } else if (realwinstreak >= 25) {
        return client.colorSymbol + 'a' + realwinstreak;
    } else {
        return client.colorSymbol + '7' + realwinstreak;
    }
}

String getSessionColor(long lastLogin, long lastLogout, String sessionFormatted) {
    long session = lastLogout - lastLogin;

    if (session > 21600000) {
        return client.colorSymbol + '4' + sessionFormatted;
    } else if (session > 14400000) {
        return client.colorSymbol + 'c' + sessionFormatted;
    } else if (session > 9000000) {
        return client.colorSymbol + '6' + sessionFormatted;
    } else if (session > 7200000) {
        return client.colorSymbol + 'e' + sessionFormatted;
    } else if (session > 1200000) {
        return client.colorSymbol + 'a' + sessionFormatted;
    } else if (session > 600000) {
        return client.colorSymbol + 'e' + sessionFormatted;
    } else if (session > 300000) {
        return client.colorSymbol + 'e' + sessionFormatted;
    } else if (session > 150000) {
        return client.colorSymbol + 'c' + sessionFormatted;
    } else {
        return client.colorSymbol + '4' + sessionFormatted;
    }
}

String getFkdrColor(String fkdr) {
    double realfkdr = Double.parseDouble(fkdr);
    if (realfkdr > 1000) {
        return client.colorSymbol + '5' + fkdr;
    } else if (realfkdr > 100) {
        return client.colorSymbol + '4' + fkdr;
    } else if (realfkdr > 10) {
        return client.colorSymbol + 'c' + fkdr;
    } else if (realfkdr > 5) {
        return client.colorSymbol + '6' + fkdr;
    } else if (realfkdr > 2.4) {
        return client.colorSymbol + 'e' + fkdr;
    } else if (realfkdr > 1.4) {
        return client.colorSymbol + 'f' + fkdr;
    } else {
        return client.colorSymbol + '7' + fkdr;
    }
}

String formatDoubleStr(double val) {
    String str;
    if (val % 1 == 0) {
        str = String.valueOf((int) val);
    } else {
        str = String.valueOf(val);
    }
    return str;
}

String calculateRelativeTimestamp(long lastLogin, long lastLogout) {
    long timeSince = (lastLogout - lastLogin) / 1000L;
    long remainingTime = timeSince;

    long years = remainingTime / 31557600L;
    remainingTime %= 31557600L;
    long months = remainingTime / 2629800L;
    remainingTime %= 2629800L;
    long days = remainingTime / 86400L;
    remainingTime %= 86400L;
    long hours = remainingTime / 3600L;
    remainingTime %= 3600L;
    long minutes = remainingTime / 60L;
    long seconds = remainingTime % 60L;

    StringBuilder msg = new StringBuilder();
    int componentsAdded = 0;

    if (years > 0 && componentsAdded < 2) {
        msg.append(years).append("y");
        componentsAdded++;
    }
    if (months > 0 && componentsAdded < 2) {
        msg.append(months).append("mo");
        componentsAdded++;
    }
    if (days > 0 && componentsAdded < 2) {
        msg.append(days).append("d");
        componentsAdded++;
    }
    if (hours > 0 && componentsAdded < 2) {
        msg.append(hours).append("h");
        componentsAdded++;
    }
    if (minutes > 0 && componentsAdded < 2) {
        msg.append(minutes).append("m");
        componentsAdded++;
    }
    if ((seconds > 0 && componentsAdded < 2) || timeSince == 0) {
        msg.append(seconds).append("s");
        componentsAdded++;
    }

    return msg.toString();
}

String getRankColor(String rank) {
    switch (rank) {
        case "VIP":
        case "VIP+":
            return client.colorSymbol + "a";
        case "MVP":
        case "MVP+":
            return client.colorSymbol + "b";
        case "MVP++":
            return client.colorSymbol + "6";
        case "YOUTUBE":
        case "ADMIN":
        case "OWNER":
            return client.colorSymbol + "c";
        case "GM":
            return client.colorSymbol + "2";
        case "PIG+++":
            return client.colorSymbol + "d";
        default:
            return client.colorSymbol + "7";
    }
}

String getPrestigeColor(int number) {
    int prestige = number - number % 100;
    String nums = String.format("%04d", number);

    switch (prestige) {
        case 5000:
            return client.colorSymbol + "4" + nums.charAt(0) + client.colorSymbol + "5" + nums.charAt(1) + client.colorSymbol + "9" + nums.charAt(2) + nums.charAt(3) + client.colorSymbol;
        case 4900:
            return client.colorSymbol + "a" + nums.charAt(0) + client.colorSymbol + "f" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "a" + nums.charAt(3);
        case 4800:
            return client.colorSymbol + "5" + nums.charAt(0) + client.colorSymbol + "c" + nums.charAt(1) + client.colorSymbol + "6" + nums.charAt(2) + client.colorSymbol + "e" + nums.charAt(3);
        case 4700:
            return client.colorSymbol + "f" + nums.charAt(0) + client.colorSymbol + "4" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "9" + nums.charAt(3);
        case 4600:
            return client.colorSymbol + "2" + nums.charAt(0) + client.colorSymbol + "b" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 4500:
            return client.colorSymbol + "f" + nums.charAt(0) + client.colorSymbol + "b" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "2" + nums.charAt(3);
        case 4400:
            return client.colorSymbol + "2" + nums.charAt(0) + client.colorSymbol + "a" + nums.charAt(1) + client.colorSymbol + "e" + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 4300:
            return client.colorSymbol + "0" + nums.charAt(0) + client.colorSymbol + "5" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "5" + nums.charAt(3);
        case 4200:
            return client.colorSymbol + "1" + nums.charAt(0) + client.colorSymbol + "9" + nums.charAt(1) + client.colorSymbol + "3" + nums.charAt(2) + client.colorSymbol + "f" + nums.charAt(3);
        case 4100:
            return client.colorSymbol + "e" + nums.charAt(0) + client.colorSymbol + "6" + nums.charAt(1) + client.colorSymbol + "c" + nums.charAt(2) + client.colorSymbol + "d" + nums.charAt(3);
        case 4000:
            return client.colorSymbol + "5" + nums.charAt(0) + client.colorSymbol + "c" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 3900:
            return client.colorSymbol + "c" + nums.charAt(0) + client.colorSymbol + "a" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "3" + nums.charAt(3);
        case 3800:
            return client.colorSymbol + "1" + nums.charAt(0) + client.colorSymbol + "9" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "5" + nums.charAt(3);
        case 3700:
            return client.colorSymbol + "4" + nums.charAt(0) + client.colorSymbol + "c" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "b" + nums.charAt(3);
        case 3600:
            return client.colorSymbol + "a" + nums.charAt(0) + nums.charAt(1) + client.colorSymbol + "b" + nums.charAt(2) + client.colorSymbol + "9" + nums.charAt(3);
        case 3500:
            return client.colorSymbol + "c" + nums.charAt(0) + client.colorSymbol + "4" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "2" + nums.charAt(3);
        case 3400:
            return client.colorSymbol + "2" + nums.charAt(0) + client.colorSymbol + "a" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "5" + nums.charAt(3);
        case 3300:
            return client.colorSymbol + "9" + nums.charAt(0) + nums.charAt(1) + client.colorSymbol + "d" + nums.charAt(2) + client.colorSymbol + "c" + nums.charAt(3);
        case 3200:
            return client.colorSymbol + "c" + nums.charAt(0) + client.colorSymbol + "7" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "4" + nums.charAt(3);
        case 3100:
            return client.colorSymbol + "9" + nums.charAt(0) + client.colorSymbol + "2" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 3000:
            return client.colorSymbol + "e" + nums.charAt(0) + client.colorSymbol + "6" + nums.charAt(1) + client.colorSymbol + "c" + nums.charAt(2) + client.colorSymbol + "4" + nums.charAt(3);
        case 2900:
            return client.colorSymbol + "b" + nums.charAt(0) + client.colorSymbol + "3" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "9" + nums.charAt(3);
        case 2800:
            return client.colorSymbol + "a" + nums.charAt(0) + client.colorSymbol + "2" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 2700:
            return client.colorSymbol + "e" + nums.charAt(0) + client.colorSymbol + "f" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "8" + nums.charAt(3);
        case 2600:
            return client.colorSymbol + "4" + nums.charAt(0) + client.colorSymbol + "c" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "d" + nums.charAt(3);
        case 2500:
            return client.colorSymbol + "f" + nums.charAt(0) + client.colorSymbol + "a" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "2" + nums.charAt(3);
        case 2400:
            return client.colorSymbol + "b" + nums.charAt(0) + client.colorSymbol + "f" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "7" + nums.charAt(3);
        case 2300:
            return client.colorSymbol + "5" + nums.charAt(0) + client.colorSymbol + "d" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 2200:
            return client.colorSymbol + "6" + nums.charAt(0) + client.colorSymbol + "f" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "b" + nums.charAt(3);
        case 2100:
            return client.colorSymbol + "f" + nums.charAt(0) + client.colorSymbol + "e" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "6" + nums.charAt(3);
        case 2000:
            return client.colorSymbol + "7" + nums.charAt(0) + client.colorSymbol + "f" + nums.charAt(1) + nums.charAt(2) + client.colorSymbol + "7" + nums.charAt(3);
        case 1000:
            return client.colorSymbol + "c" + nums.charAt(0) + client.colorSymbol + "6" + nums.charAt(1) + client.colorSymbol + "e" + nums.charAt(2) + client.colorSymbol + "a" + nums.charAt(3);
        case 1900:
        case 900:
            return client.colorSymbol + "5" + number;
        case 1800:
        case 800:
            return client.colorSymbol + "9" + number;
        case 1700:
        case 700:
            return client.colorSymbol + "d" + number;
        case 1600:
            return client.colorSymbol + "c" + number;
        case 600:
            return client.colorSymbol + "4" + number;
        case 1500:
        case 500:
            return client.colorSymbol + "3" + number;
        case 1400:
        case 400:
            return client.colorSymbol + "2" + number;
        case 1300:
        case 300:
            return client.colorSymbol + "b" + number;
        case 1200:
        case 200:
            return client.colorSymbol + "6" + number;
        case 1100:
        case 100:
            return client.colorSymbol + "f" + number;
        default:
            return client.colorSymbol + "7" + number;
    }
}