package kassuk.addon.blackout.modules;

import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import java.util.Random;

public class DamageMoan extends BlackOutModule {
    public DamageMoan() {
        super(BlackOut.BLACKOUT, "Damage Moan", "Send weird things when ur dom hurt u");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("Damage Threshold")
        .description("Minimum damage taken to trigger a message.")
        .defaultValue(4.0)
        .min(0)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Tick delay between sending messages.")
        .defaultValue(50)
        .min(0)
        .sliderRange(0, 200)
        .build()
    );

    private final Setting<Boolean> sendInPm = sgGeneral.add(new BoolSetting.Builder()
        .name("Send in PM")
        .description("Send the message using the private message command to the last attacker.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> cacheDuration = sgGeneral.add(new IntSetting.Builder()
        .name("Player Cache Duration")
        .description("How long to remember the last attacker before clearing the cache, in seconds.")
        .defaultValue(60)
        .min(0)
        .sliderRange(0, 300)
        .build()
    );

    private final Setting<String> pmCommand = sgGeneral.add(new StringSetting.Builder()
        .name("PM Command")
        .description("Command used to send a private message. Use %s as the target placeholder.")
        .defaultValue("/msg %s")
        .build()
    );

    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("Messages")
        .description("List of messages to send when damaged.")
        .defaultValue("I like that, please continue", "Yea.. Punish me~", "That hurt mommy ~", "I'm gonna cum if u continue", "I love it when you hit me like that")
        .build()
    );

    private final Setting<TriggerMode> triggerMode = sgGeneral.add(new EnumSetting.Builder<TriggerMode>()
        .name("Trigger Mode")
        .description("When to trigger messages: on damage, on totem pop, or both.")
        .defaultValue(TriggerMode.Damage)
        .build()
    );

    private final Random random = new Random();
    private double previousHealth = -1;
    private int delayTicks = 0;
    private int attackerCacheTicks = 0;
    private int lastIndex = -1;
    private String lastAttackerName = "";

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isActive() || mc.player == null) {
            if (mc.player == null) {
                previousHealth = -1;
                attackerCacheTicks = 0;
                lastAttackerName = "";
            }
            return;
        }

        if (delayTicks > 0) delayTicks--;
        if (attackerCacheTicks > 0) attackerCacheTicks--;
        if (attackerCacheTicks == 0) lastAttackerName = "";

        double currentHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        if (mc.player.isDeadOrDying() || currentHealth <= 0) {
            clearAttackerCache();
            previousHealth = currentHealth;
            return;
        }

        if (previousHealth < 0) {
            previousHealth = currentHealth;
            return;
        }

        double damageTaken = previousHealth - currentHealth;

        if ((triggerMode.get() == TriggerMode.Damage || triggerMode.get() == TriggerMode.Both) && damageTaken >= threshold.get() && delayTicks <= 0) {
            List<String> list = messages.get();
            if (list == null || list.isEmpty()) {
                previousHealth = currentHealth;
                return;
            }

            int idx = random.nextInt(list.size());
            if (list.size() > 1) {
                // avoid sending the same message twice in a row
                while (idx == lastIndex) {
                    idx = random.nextInt(list.size());
                }
            }

            String msg = list.get(idx);
            // Use last known attacker for damage-triggered messages
            sendMessageWithAttacker(msg);

            lastIndex = idx;
            delayTicks = tickDelay.get();
        }

        previousHealth = currentHealth;
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (!isActive()) return;

        if (event.packet instanceof ClientboundDamageEventPacket dmg) {
            if (!shouldTrackAttacker()) return;
            if (mc.level == null || mc.player == null || dmg.entityId() != mc.player.getId()) return;

            String attacker = getAttackerName(dmg);
            if (attacker != null) {
                lastAttackerName = attacker;
                attackerCacheTicks = cacheDuration.get() * 20;
            }
            return;
        }

        if (triggerMode.get() == TriggerMode.Totem || triggerMode.get() == TriggerMode.Both) {
            if (event.packet instanceof ClientboundEntityEventPacket packet) {
                if (packet.getEventId() == 35) { // totem pop status
                    Entity entity = packet.getEntity(mc.level);
                    if (entity == mc.player) {
                        if (delayTicks <= 0) {
                            List<String> list = messages.get();
                            if (list == null || list.isEmpty()) return;
                            int idx = random.nextInt(list.size());
                            if (list.size() > 1) {
                                while (idx == lastIndex) idx = random.nextInt(list.size());
                            }
                            String msg = list.get(idx);
                            sendMessageWithAttacker(msg);
                            lastIndex = idx;
                            delayTicks = tickDelay.get();
                        }
                    }
                }
            }
        }
    }

    private boolean shouldTrackAttacker() {
        return sendInPm.get() && !pmCommand.get().trim().isEmpty();
    }

    private void sendMessageWithAttacker(String msg) {
        String attacker = lastAttackerName.trim();
        if (sendInPm.get() && !attacker.isEmpty()) {
            String cmd = pmCommand.get().trim();
            if (!cmd.isEmpty() && mc.getConnection() != null) {
                String filled = cmd.replace("%s", attacker).trim();
                String commandText = filled.startsWith("/") ? filled.substring(1) : filled;
                mc.getConnection().sendCommand(commandText + " " + msg);
                return;
            }
        }
        ChatUtils.sendPlayerMsg(msg);
    }

    private String getAttackerName(ClientboundDamageEventPacket dmg) {
        if (mc.level == null) return null;

        int attackerId = dmg.sourceDirectId();
        if (attackerId == 0) attackerId = dmg.sourceCauseId();
        if (attackerId == 0) return null;

        Entity attackerEntity = mc.level.getEntity(attackerId);
        if (attackerEntity instanceof Player player) {
            return player.getName().getString();
        }

        return null;
    }

    private void clearAttackerCache() {
        lastAttackerName = "";
        attackerCacheTicks = 0;
    }

    public enum TriggerMode {
        Damage,
        Totem,
        Both
    }
}
