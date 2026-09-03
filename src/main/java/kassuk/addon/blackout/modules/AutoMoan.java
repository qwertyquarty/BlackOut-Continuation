package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.Player;
import java.util.Random;

/**
 * @author KassuK
 ** @author HYPE115
 */

 // Replacing gay (all of them :\) thing by straight thing

public class AutoMoan extends BlackOutModule {

    public AutoMoan() {
        super(BlackOut.BLACKOUT, "Auto Moan", "Moans sexual things to the closest person.");
    }

    //Where the fuck did I go so wrong in life to end up coding fucking AutoMoan

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ContentMode> contentMode = sgGeneral.add(new EnumSetting.Builder<ContentMode>()
        .name("Content Mode")
        .description("Choose between straight, gay, and universal message sets.")
        .defaultValue(ContentMode.Universal)
        .build()
    );

    private final Setting<MessageStyle> messageStyle = sgGeneral.add(new EnumSetting.Builder<MessageStyle>()
        .name("Message Style")
        .description("Choose between submissive and dominant wording.")
        .defaultValue(MessageStyle.Submissive)
        .build()
    );

    private final Setting<Boolean> iFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("Ignore Friends")
        .description("Doesn't send messages targeted to friends.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> targetUsername = sgGeneral.add(new StringSetting.Builder()
        .name("Target username")
        .description("Target a specific player by username. Leave empty to use the closest player.")
        .defaultValue("")
        .build()
    );

    private final Setting<Boolean> sendInPm = sgGeneral.add(new BoolSetting.Builder()
        .name("Send messages in PM")
        .description("Sends the generated messages as private messages instead of public chat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> privateMessageCommand = sgGeneral.add(new StringSetting.Builder()
        .name("Private message command")
        .description("Command used to send private messages. Use %s for the target player name.")
        .defaultValue("/message %s")
        .build()
    );

    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Tick delay between moans.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    //Most haram module in Blackout

    public enum ContentMode {
        Straight,
        Gay,
        Universal,
    }

    public enum MessageStyle {
        Dominant,
        Submissive,
    }

// Moddified by HYPE115 for making straight mode and messages option

    private int lastStraightSubmissiveIndex = -1;
    private int lastStraightDominantIndex = -1;
    private int lastGaySubmissiveIndex = -1;
    private int lastGayDominantIndex = -1;
    private int lastUniversalSubmissiveIndex = -1;
    private int lastUniversalDominantIndex = -1;
    private double timer = 0;
    private static final String[] StraightSubmissive = new String[]{
        "Can I cum %s",
        "I'm your slut",
        "I'm all yours %s",
        "Make me feel like I belong to you",
        "I can't wait to feel you %s",
        "Feed me mommy",
        "Can i lick your feet %s",
        "Punish me"
    };

    public static String[] getStraightsubmissive() {
        return StraightSubmissive;
    }

    public static String[] getStraightdominant() {
        return StraightDominant;
    }

    private static final String[] StraightDominant = new String[]{
        "Call me daddy",
        "Get on your knees",
        "Beg for it",
        "Suck my cock %s",
        "Good girl",
        "Your so wet for me %s",
        "Little slut"
    };

    private static final String[] GaySubmissive = new String[]{
        //please fucking end me
        "fuck me harder daddy",
        "deeper! daddy deeper!",
        "Fuck yes your so big!",
        "I love your cock %s!",
        //I want to deepthroat a fucking loaded 12 gauge shotgun and pull the trigger
        "Do not stop fucking my ass before i cum!",
        "Oh your so hard for me",
        "Want to widen my ass up %s?",
        "I love you daddy",
        //what the fuck am I doing with my fucking life holy shit
        "Make my bussy pop",
        //I like how you can see me losing my mind while making this
        "%s loves my bussy so much",
        "i made %s cum so hard with my tight bussy",
        "Your cock is so big and juicy daddy!",
        "Please fuck me as hard as you can",
        "im %s's personal femboy cumdumpster!",
        //The fact that someone has actually said all of these non ironically shows how weird the world is
        "Please shoot your hot load deep inside me daddy!",
        "I love how %s's dick feels inside of me!",
        "%s gets so hard when he sees my ass!",
        "%s really loves fucking my ass really hard!",
        //I just want to stop writing these, but I just feel like it's not finished yet
        "why wont u say the last message",
        "Keep me close and never let go %s"
    };

    private static final String[] GayDominant = new String[]{
        //Oh god, why the fuck am I making this
        "Be a good boy for daddy",
        //If heaven is real im not getting there
        "I love pounding your ass %s!",
        "Give your bussy to daddy!",
        //this is surprisingly hard to do I cant think of anything
        "I love how you drip pre-cum while i fuck your ass %s",
        //how do gay people think of shit to say they gotta be like Shakespeare
        "Slurp up and down my cock like a good boy",
        "Come and jump on daddy's cock %s",
        //Why do I even know what bussy is (its boy pussy aka a man's asshole)
        "I love how you look at me while you suck me off %s",
        "%s looks so cute when i fuck him",
        //if nothing else in life works I can at least write gay fanfic to pay for rent
        "%s's bussy is so incredibly tight!",
        //ill add more shit to this everytime I can think of something new
        "%s takes dick like the good boy he is",
        //this one was inspired by a gif of a black man twerking why the fuck does the cheating com find those funny
        "I love how you shake your ass on my dick",
        //the amount of braincells and self-respect I lost while making this is insane
        "%s moans so cutely when i fuck his ass",
        "%s is the best cumdupster there is!",
        //I'm waiting for this to become a regular module in every client
        "%s is always horny and ready for his daddy's dick",
        //Like the fuck am I doing with my life I should be outside with friends not making this
        "My dick gets rock hard every time i see %s",
        "why wont u say the last message",
        //if anyone asks what I did during my vacation I can proudly tell them I wasted multiple hours writing things gay people can say during sex
        //The fact that I have said a few of these frightens me : SigmaClientWasTaken 2023 wtf sigma
    };

    private static final String[] UniversalSubmissive = new String[]{
        "I’m ready for you %s",
        "Tell me what you want and I’ll give it to you",
        "You can take control and I’ll follow",
        "I’m all yours when you want me",
        "Keep talking, I like hearing your voice %s",
        "I’m ready whenever you are %s",
        "Come closer and let me know what you want %s",
        "I want to make you feel good %s",
        "I’m here for you, just say what you want %s",
        "Do whatever you want with me",
        "Use me as your toy",
        "Tell me what you want %s",
        "Please use me %s",
        "I need you to take control %s",
        "I want your hands all over me %s",
        "i'm you're little thing"
    };

    private static final String[] UniversalDominant = new String[]{
        "You know exactly what I want from you",
        "Look at me when you say it %s",
        "You belong to this moment with me",
        "I want you exactly where I need you",
        "You’re mine for tonight %s",
        "Say my name when you do that %s",
        "You don’t get to leave until I say so",
        "I want you right here with me",
        "You’re going to give me everything I want",
        "I want your full attention %s",
        "Say my name when you do that",
        "You know what I want, give it to me",
        "I want you exactly where I need you",
        "You don't get to leave until I say so",
        "Look at me when you say it",
        "You belong to me tonight",
        "I love how you look right before I touch you",
        "Take it off, all of it, now"
    };

    private final Random r = new Random();


    @EventHandler
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        timer++;
        //I fucking got perm banned on a really important server to test this out you all better fucking enjoy using this
        if (mc.player != null && mc.level != null && timer >= delay.get()) {
            MOAN();
            timer = 0;
        }
    }

    private void MOAN() {
        Player target = getClosest();
        if (target == null) {
            return;
        }

        String name = target.getName().getString();
        String message = getRandomMessage(contentMode.get(), messageStyle.get(), name);

        if (sendInPm.get()) {
            String command = privateMessageCommand.get().replace("%s", name).trim();
            if (!command.isEmpty() && mc.getConnection() != null) {
                String commandText = command.startsWith("/") ? command.substring(1) : command;
                mc.getConnection().sendCommand(commandText + " " + message);
            }
        } else {
            ChatUtils.sendPlayerMsg(message);
        }
    }

    private String getRandomMessage(ContentMode contentMode, MessageStyle messageStyle, String targetName) {
        String[] messages = getMessages(contentMode, messageStyle);
        int nextIndex = getNextIndex(messages, getLastIndex(contentMode, messageStyle));
        setLastIndex(contentMode, messageStyle, nextIndex);
        return messages[nextIndex].replace("%s", targetName);
    }

    private String[] getMessages(ContentMode contentMode, MessageStyle messageStyle) {
        return switch (contentMode) {
            case Straight -> switch (messageStyle) {
                case Submissive -> mergeMessages(StraightSubmissive, UniversalSubmissive);
                case Dominant -> mergeMessages(StraightDominant, UniversalDominant);
            };
            case Gay -> switch (messageStyle) {
                case Submissive -> mergeMessages(GaySubmissive, UniversalSubmissive);
                case Dominant -> mergeMessages(GayDominant, UniversalDominant);
            };
            case Universal -> switch (messageStyle) {
                case Submissive -> UniversalSubmissive;
                case Dominant -> UniversalDominant;
            };
        };
    }

    private String[] mergeMessages(String[] primaryMessages, String[] secondaryMessages) {
        String[] merged = new String[primaryMessages.length + secondaryMessages.length];
        System.arraycopy(primaryMessages, 0, merged, 0, primaryMessages.length);
        System.arraycopy(secondaryMessages, 0, merged, primaryMessages.length, secondaryMessages.length);
        return merged;
    }

    private int getNextIndex(String[] messages, int previousIndex) {
        if (messages.length <= 1) {
            return 0;
        }

        int nextIndex = r.nextInt(messages.length);
        if (nextIndex == previousIndex) {
            nextIndex = nextIndex < messages.length - 1 ? nextIndex + 1 : 0;
        }

        return nextIndex;
    }

    private int getLastIndex(ContentMode contentMode, MessageStyle messageStyle) {
        return switch (contentMode) {
            case Straight -> switch (messageStyle) {
                case Submissive -> lastStraightSubmissiveIndex;
                case Dominant -> lastStraightDominantIndex;
            };
            case Gay -> switch (messageStyle) {
                case Submissive -> lastGaySubmissiveIndex;
                case Dominant -> lastGayDominantIndex;
            };
            case Universal -> switch (messageStyle) {
                case Submissive -> lastUniversalSubmissiveIndex;
                case Dominant -> lastUniversalDominantIndex;
            };
        };
    }

    private void setLastIndex(ContentMode contentMode, MessageStyle messageStyle, int index) {
        switch (contentMode) {
            case Straight -> {
                switch (messageStyle) {
                    case Submissive -> lastStraightSubmissiveIndex = index;
                    case Dominant -> lastStraightDominantIndex = index;
                }
            }
            case Gay -> {
                switch (messageStyle) {
                    case Submissive -> lastGaySubmissiveIndex = index;
                    case Dominant -> lastGayDominantIndex = index;
                }
            }
            case Universal -> {
                switch (messageStyle) {
                    case Submissive -> lastUniversalSubmissiveIndex = index;
                    case Dominant -> lastUniversalDominantIndex = index;
                }
            }
        }
    }

    private Player getClosest() {
        assert mc.player != null && mc.level != null;

        String targetName = targetUsername.get().trim();
        if (!targetName.isEmpty()) {
            for (Player player : mc.level.players()) {
                if (player == mc.player) continue;
                if (iFriends.get() && Friends.get().isFriend(player)) continue;
                if (player.getName().getString().equalsIgnoreCase(targetName)) {
                    return player;
                }
            }
            return null;
        }

        Player closest = null;
        float distance = -1;
        if (!mc.level.players().isEmpty()) {
            for (Player player : mc.level.players()) {
                if (player != mc.player && (!iFriends.get() || !Friends.get().isFriend(player))) {
                    if (closest == null || mc.player.position().distanceTo(player.position()) < distance) {
                        closest = player;
                        distance = (float) mc.player.position().distanceTo(player.position());
                    }
                }
            }
        }
        return closest;
    }
}
