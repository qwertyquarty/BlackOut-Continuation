package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    private static final String RELEASES_URL = "https://github.com/HYPE115/BlackOut-Continuation/releases";
    private static final String API_URL = "https://api.github.com/repos/HYPE115/BlackOut-Continuation/releases/latest";
    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern URL_PATTERN = Pattern.compile("\"html_url\"\\s*:\\s*\"([^\"]+)\"");

    private static volatile boolean checking = false;
    private static volatile boolean checked = false;
    private static volatile boolean updateAvailable = false;
    private static volatile String latestVersion = null;
    private static volatile String latestUrl = RELEASES_URL;

    public static void start() {
        if (checking || checked) return;

        checking = true;

        Thread thread = new Thread(UpdateChecker::runCheck, "BlackOut-UpdateChecker");
        thread.setDaemon(true);
        thread.start();
    }

    private static void runCheck() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/vnd.github+json")
                .timeout(Duration.ofSeconds(5))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                checked = true;
                checking = false;
                return;
            }

            String body = response.body();
            Matcher tagMatcher = TAG_PATTERN.matcher(body);
            Matcher urlMatcher = URL_PATTERN.matcher(body);
            String latestTag = null;

            if (tagMatcher.find()) {
                latestTag = tagMatcher.group(1).replaceFirst("^v", "");
                if (isNewer(latestTag, BlackOut.BLACKOUT_VERSION)) {
                    latestVersion = latestTag;
                    if (urlMatcher.find()) latestUrl = urlMatcher.group(1);
                    updateAvailable = true;
                }
            }

            if (latestTag != null) {
                BlackOut.LOG.info("BlackOut update check: current version {} | latest release {}", BlackOut.BLACKOUT_VERSION, latestTag);
            }
        } catch (Exception ignored) {
            // Ignore network issues and keep the mod responsive.
        } finally {
            checked = true;
            checking = false;
            showPopupIfNeeded();
        }
    }

    private static class UpdateScreen extends Screen {
        private final String latestVersion;
        private final String latestUrl;

        private UpdateScreen(String latestVersion, String latestUrl) {
            super(Component.literal("BlackOut update"));
            this.latestVersion = latestVersion;
            this.latestUrl = latestUrl;
        }

        @Override
        protected void init() {
            super.init();
            int buttonWidth = 140;
            int buttonHeight = 20;
            int centerX = width / 2 - buttonWidth / 2;

            addRenderableWidget(Button.builder(Component.literal("Open releases"), button -> {
                try {
                    Desktop.getDesktop().browse(URI.create(latestUrl));
                } catch (Exception ignored) {
                    // Ignore if the environment cannot open the browser.
                }
                onClose();
            }).bounds(centerX, height - 70, buttonWidth, buttonHeight).build());

            addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(centerX, height - 40, buttonWidth, buttonHeight)
                .build());
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
            super.extractRenderState(graphics, mouseX, mouseY, tickDelta);

            graphics.fill(0, 0, width, height, 0xCC000000);

            Component title = Component.literal("BlackOut update available").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            Component body = Component.literal("A newer version " + latestVersion + " is available.").withStyle(ChatFormatting.WHITE);
            Component body2 = Component.literal("Open the release page to update.").withStyle(ChatFormatting.GRAY);

            graphics.centeredText(font, title, width / 2, 60, 0xFFFFFF);
            graphics.centeredText(font, body, width / 2, 95, 0xFFFFFF);
            graphics.centeredText(font, body2, width / 2, 115, 0xFFFFFF);
        }
    }

    private static void showPopupIfNeeded() {
        if (!checked || !updateAvailable || MeteorClient.mc == null) return;

        MeteorClient.mc.execute(() -> {
            if (MeteorClient.mc.level != null || MeteorClient.mc.gui.screen() != null) return;
            MeteorClient.mc.gui.setScreen(new UpdateScreen(latestVersion, latestUrl));
        });

        updateAvailable = false;
    }

    private static boolean isNewer(String candidate, String current) {
        String normalizedCandidate = normalizeVersion(candidate);
        String normalizedCurrent = normalizeVersion(current);

        if (normalizedCandidate.isEmpty() || normalizedCurrent.isEmpty()) return false;

        String[] candidateParts = normalizedCandidate.split("\\.");
        String[] currentParts = normalizedCurrent.split("\\.");
        int length = Math.max(candidateParts.length, currentParts.length);

        for (int i = 0; i < length; i++) {
            int candidateValue = i < candidateParts.length ? parsePart(candidateParts[i]) : 0;
            int currentValue = i < currentParts.length ? parsePart(currentParts[i]) : 0;
            if (candidateValue > currentValue) return true;
            if (candidateValue < currentValue) return false;
        }

        return false;
    }

    private static String normalizeVersion(String version) {
        if (version == null) return "";
        String cleaned = version.trim().replaceFirst("^[vV]", "");
        cleaned = cleaned.replaceAll("[^0-9.]", "");
        return cleaned.replaceAll("\\.+$", "");
    }

    private static int parsePart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
