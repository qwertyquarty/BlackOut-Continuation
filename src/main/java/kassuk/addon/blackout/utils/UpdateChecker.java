package kassuk.addon.blackout.utils;

import kassuk.addon.blackout.BlackOut;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
            super(Text.literal("BlackOut update"));
            this.latestVersion = latestVersion;
            this.latestUrl = latestUrl;
        }

        @Override
        protected void init() {
            super.init();
            int buttonWidth = 140;
            int buttonHeight = 20;
            int centerX = width / 2 - buttonWidth / 2;

            addDrawableChild(ButtonWidget.builder(Text.literal("Open releases"), button -> {
                try {
                    Desktop.getDesktop().browse(URI.create(latestUrl));
                } catch (Exception ignored) {
                    // Ignore if the environment cannot open the browser.
                }
                close();
            }).dimensions(centerX, height - 70, buttonWidth, buttonHeight).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(centerX, height - 40, buttonWidth, buttonHeight)
                .build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            context.fill(0, 0, width, height, 0xCC000000);

            Text title = Text.literal("BlackOut update available").formatted(Formatting.GOLD, Formatting.BOLD);
            Text body = Text.literal("A newer version " + latestVersion + " is available.").formatted(Formatting.WHITE);
            Text body2 = Text.literal("Open the release page to update.").formatted(Formatting.GRAY);

            context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 60, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, body, width / 2, 95, 0xFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, body2, width / 2, 115, 0xFFFFFF);
        }
    }

    private static void showPopupIfNeeded() {
        if (!checked || !updateAvailable || MeteorClient.mc == null) return;

        MeteorClient.mc.execute(() -> {
            if (MeteorClient.mc.world != null || MeteorClient.mc.currentScreen != null) return;
            MeteorClient.mc.setScreen(new UpdateScreen(latestVersion, latestUrl));
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
