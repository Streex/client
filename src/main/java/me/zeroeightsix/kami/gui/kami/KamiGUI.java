package me.zeroeightsix.kami.gui.kami;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.gui.kami.component.*;
import me.zeroeightsix.kami.gui.kami.theme.kami.KamiTheme;
import me.zeroeightsix.kami.gui.rgui.GUI;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Scrollpane;
import me.zeroeightsix.kami.gui.rgui.component.listen.MouseListener;
import me.zeroeightsix.kami.gui.rgui.component.listen.TickListener;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.component.use.Label;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.bewwawho.gui.InfoOverlay;
import me.zeroeightsix.kami.util.bewwawho.InfoCalculator;
import me.zeroeightsix.kami.util.zeroeightysix.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by 086 on 25/06/2017.
 * Updated by S-B99 on 14/01/20
 */
public class KamiGUI extends GUI {

    public static final RootFontRenderer fontRenderer = new RootFontRenderer(1);
    public Theme theme;

    public static ColourHolder primaryColour = new ColourHolder(29, 29, 29);

    //public static OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public KamiGUI() {
        super(new KamiTheme());
        theme = getTheme();
    }

    @Override
    public void drawGUI() {
        super.drawGUI();
    }

    @Override
    public void initializeGUI() {
        HashMap<Module.Category, Pair<Scrollpane, SettingsPanel>> categoryScrollpaneHashMap = new HashMap<>();
        for (Module module : ModuleManager.getModules()) {
            if (module.getCategory().isHidden()) continue;
            Module.Category moduleCategory = module.getCategory();
            if (!categoryScrollpaneHashMap.containsKey(moduleCategory)) {
                Stretcherlayout stretcherlayout = new Stretcherlayout(1);
                stretcherlayout.setComponentOffsetWidth(0);
                Scrollpane scrollpane = new Scrollpane(getTheme(), stretcherlayout, 300, 260);
                scrollpane.setMaximumHeight(180);
                categoryScrollpaneHashMap.put(moduleCategory, new Pair<>(scrollpane, new SettingsPanel(getTheme(), null)));
            }

            Pair<Scrollpane, SettingsPanel> pair = categoryScrollpaneHashMap.get(moduleCategory);
            Scrollpane scrollpane = pair.getKey();
            CheckButton checkButton = new CheckButton(module.getName());
            checkButton.setToggled(module.isEnabled());

            checkButton.addTickListener(() -> { // dear god
                checkButton.setToggled(module.isEnabled());
                checkButton.setName(module.getName());
            });

            checkButton.addMouseListener(new MouseListener() {
                @Override
                public void onMouseDown(MouseButtonEvent event) {
                    if (event.getButton() == 1) { // Right click
                        pair.getValue().setModule(module);
                        pair.getValue().setX(event.getX() + checkButton.getX());
                        pair.getValue().setY(event.getY() + checkButton.getY());
                    }
                }

                @Override
                public void onMouseRelease(MouseButtonEvent event) {

                }

                @Override
                public void onMouseDrag(MouseButtonEvent event) {

                }

                @Override
                public void onMouseMove(MouseMoveEvent event) {

                }

                @Override
                public void onScroll(MouseScrollEvent event) {

                }
            });
            checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                @Override
                public void execute(CheckButton component, CheckButtonPoofInfo info) {
                    if (info.getAction().equals(CheckButton.CheckButtonPoof.CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE)) {
                        module.setEnabled(checkButton.isToggled());
                    }
                }
            });
            scrollpane.addChild(checkButton);
        }

        int x = 10;
        int y = 10;
        int nexty = y;
        for (Map.Entry<Module.Category, Pair<Scrollpane, SettingsPanel>> entry : categoryScrollpaneHashMap.entrySet()) {
            Stretcherlayout stretcherlayout = new Stretcherlayout(1);
            stretcherlayout.COMPONENT_OFFSET_Y = 1;
            Frame frame = new Frame(getTheme(), stretcherlayout, entry.getKey().getName());
            Scrollpane scrollpane = entry.getValue().getKey();
            frame.addChild(scrollpane);
            frame.addChild(entry.getValue().getValue());
            scrollpane.setOriginOffsetY(0);
            scrollpane.setOriginOffsetX(0);
            frame.setCloseable(false);

            frame.setX(x);
            frame.setY(y);

            addChild(frame);

            nexty = Math.max(y + frame.getHeight() + 10, nexty);
            x += frame.getWidth() + 10;
            if (x > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
            }
        }

        this.addMouseListener(new MouseListener() {
            private boolean isNotBetween(int min, int val, int max) {
                return val > max || val < min;
            }

            @Override
            public void onMouseDown(MouseButtonEvent event) {
                List<SettingsPanel> panels = ContainerHelper.getAllChildren(SettingsPanel.class, KamiGUI.this);
                for (SettingsPanel settingsPanel : panels) {
                    if (!settingsPanel.isVisible()) continue;
                    int[] real = GUI.calculateRealPosition(settingsPanel);
                    int pX = event.getX() - real[0];
                    int pY = event.getY() - real[1];
                    if (isNotBetween(0, pX, settingsPanel.getWidth()) || isNotBetween(0, pY, settingsPanel.getHeight()))
                        settingsPanel.setVisible(false);
                }
            }

            @Override public void onMouseRelease(MouseButtonEvent event) { }

            @Override public void onMouseDrag(MouseButtonEvent event) { }

            @Override public void onMouseMove(MouseMoveEvent event) { }

            @Override public void onScroll(MouseScrollEvent event) { }
        });

        ArrayList<Frame> frames = new ArrayList<>();

        /*
        Active modules
         */
        Frame frame = new Frame(getTheme(), new Stretcherlayout(1), "Active modules");
        frame.setCloseable(false);
        frame.addChild(new ActiveModules());
        frame.setPinnable(true);
        frames.add(frame);

        /*
        Testing
         */
        /*
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Info2");
        frame.setCloseable(false);
        frame.setPinneable(true);
//        Label information2 = new Label("");
        EnumButton theme = new EnumButton("Theme", new String[] {"Modern", "Modern2", "Kami", "Kami Blue", "Custom"});
        ColorizedCheckButton checkButton = new ColorizedCheckButton("Button");
//        checkButton.addTickListener(() -> {
//            if (checkButton.isFocused()) {
//                Command.sendChatMessage("focused");
//            }
//            else if (checkButton.isHovered()) {
//                Command.sendChatMessage("hovered");
//            }
//            else if (checkButton.isToggled()) {
//                Command.sendChatMessage("toggled");
//            }
//        });

//        information.setShadow(true);
//        information2.addTickListener(() -> {
//            information2.setText("");
//            information2.addLine("\u00A7b" + KamiMod.KAMI_KANJI + "\u00A73 " + KamiMod.MODVER);
//            information2.addLine("\u00A7b" + Math.round(LagCompensator.INSTANCE.getTickRate()) + Command.SECTION_SIGN + "3 tps");
//            information2.addLine("\u00A7b" + Minecraft.debugFPS + Command.SECTION_SIGN + "3 fps");
//        });
        frame.addChild(theme, checkButton);
//        information2.setFontRenderer(fontRenderer);
        frames.add(frame);
        */
        
        /*
        Information Overlay / InfoOverlay
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Info");
        frame.setCloseable(false);
        frame.setPinnable(true);
        Label information = new Label("");
        information.setShadow(true);
        information.addTickListener(() -> {
            InfoOverlay info = (InfoOverlay) ModuleManager.getModuleByName("InfoOverlay");

            if (info.isEnabled()) {
                information.setText("");
                information.addLine("\u00A7b" + KamiMod.KAMI_KANJI + "\u00A73 " + KamiMod.MODVER);
                if (info.globalInfoNam.getValue()) {
                    information.addLine("\u00A7bWelcome" + Command.SECTION_SIGN + "3 " + Minecraft.getMinecraft().player.getName() + "!");
                }
                if (info.globalInfoTps.getValue()) {
                    information.addLine("\u00A7b" + InfoCalculator.tps() + Command.SECTION_SIGN + "3 tps");
                }
                if (info.globalInfoFps.getValue()) {
                    information.addLine("\u00A7b" + Minecraft.debugFPS + Command.SECTION_SIGN + "3 fps");
                }
                if (info.globalInfoSpe.getValue()) {
                    information.addLine("\u00A7b" + InfoCalculator.speed() + Command.SECTION_SIGN + "3 " + info.unitType(info.speedUnitSetting.getValue()));
                }
                if (info.globalInfoPin.getValue()) {
                    information.addLine("\u00A7b" + InfoCalculator.ping() + Command.SECTION_SIGN + "3 ms");
                }
                if (info.globalInfoDur.getValue()) {
                    information.addLine("\u00A7b" + InfoCalculator.dura() + "\u00A73 dura");
                }
                if (info.globalInfoMem.getValue()) {
                    information.addLine("\u00A7b" + InfoCalculator.memory() + Command.SECTION_SIGN + "3mB free");
                }
            } else {
                information.setText("");
                information.addLine("\u00A7b" + KamiMod.KAMI_KANJI + "\u00A73 " + KamiMod.MODVER);
            }
        });
        frame.addChild(information);
        information.setFontRenderer(fontRenderer);
        frames.add(frame);

        /*
        Inventory Viewer
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Inventory Viewer");
        frame.setCloseable(false);
        frame.setMinimizeable(false);
        frame.setPinnable(false);
        Label inventory = new Label("");
        inventory.setShadow(true);
        frame.addChild(inventory);
        inventory.setFontRenderer(fontRenderer);
        frames.add(frame);

        /*
        Friends List
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Friends");
        frame.setCloseable(false);
        frame.setPinnable(true);
        Label friends = new Label("");
        friends.setShadow(true);
        Frame friendsFrame = frame;

        AtomicInteger friendsAmount = new AtomicInteger();
        friends.addTickListener(() -> {
            /* Don't load friends list if it's minimized */
            if (!friendsFrame.isMinimized()) {
                friends.setText("");

                Friends.friends.getValue().forEach(friend -> {
                    friendsAmount.getAndIncrement();
                    /* Cap loading friends list at 100 */
                    if (friendsAmount.get() <= 100) {
//                        Command.sendChatMessage("added");
                        friends.addLine(friend.getUsername());
                    }
                });
            }
            else {
                friends.setText("");
            }
        });
        frame.addChild(friends);
        friends.setFontRenderer(fontRenderer);
        frames.add(frame);

        /*
        Text Radar
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Text Radar");
        Label list = new Label("");
        DecimalFormat dfHealth = new DecimalFormat("#.#");
        dfHealth.setRoundingMode(RoundingMode.HALF_UP);
        StringBuilder healthSB = new StringBuilder();
        list.addTickListener(() -> {
            if (!list.isVisible()) return;
            list.setText("");

            Minecraft mc = Wrapper.getMinecraft();

            if (mc.player == null) return;
            List<EntityPlayer> entityList = mc.world.playerEntities;

            Map<String, Integer> players = new HashMap<>();
            for (Entity e : entityList) {
                if (e.getName().equals(mc.player.getName())) continue;
                String posString = (e.posY > mc.player.posY ? ChatFormatting.DARK_GREEN + "+" : (e.posY == mc.player.posY ? " " : ChatFormatting.DARK_RED + "-"));
                float hpRaw = ((EntityLivingBase) e).getHealth() + ((EntityLivingBase) e).getAbsorptionAmount();
                String hp = dfHealth.format(hpRaw);
                healthSB.append(Command.SECTION_SIGN);
                if (hpRaw >= 20) {
                    healthSB.append("a");
                } else if (hpRaw >= 10) {
                    healthSB.append("e");
                } else if (hpRaw >= 5) {
                    healthSB.append("6");
                } else {
                    healthSB.append("c");
                }
                healthSB.append(hp);
                players.put(ChatFormatting.GRAY + posString + " " + healthSB.toString() + " " + ChatFormatting.GRAY + e.getName(), (int) mc.player.getDistance(e));
                healthSB.setLength(0);
            }

            if (players.isEmpty()) {
                list.setText("");
                return;
            }

            players = sortByValue(players);

            for (Map.Entry<String, Integer> player : players.entrySet()) {
                list.addLine(Command.SECTION_SIGN + "7" + player.getKey() + " " + Command.SECTION_SIGN + "8" + player.getValue());
            }
        });
        frame.setCloseable(false);
        frame.setPinnable(true);
        frame.setMinimumWidth(75);
        list.setShadow(true);
        frame.addChild(list);
        list.setFontRenderer(fontRenderer);
        frames.add(frame);

        /*
        Entity List
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Entities");
        Label entityLabel = new Label("");
        frame.setCloseable(false);
        entityLabel.addTickListener(new TickListener() {
            Minecraft mc = Wrapper.getMinecraft();

            @Override
            public void onTick() {
                if (mc.player == null || !entityLabel.isVisible()) return;

                final List<Entity> entityList = new ArrayList<>(mc.world.loadedEntityList);
                if (entityList.size() <= 1) {
                    entityLabel.setText("");
                    return;
                }
                final Map<String, Integer> entityCounts = entityList.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> !(e instanceof EntityPlayer))
                        .collect(Collectors.groupingBy(KamiGUI::getEntityName,
                                Collectors.reducing(0, ent -> {
                                    if (ent instanceof EntityItem)
                                        return ((EntityItem) ent).getItem().getCount();
                                    return 1;
                                }, Integer::sum)
                        ));

                entityLabel.setText("");
                entityCounts.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .map(entry -> TextFormatting.GRAY + entry.getKey() + " " + TextFormatting.DARK_GRAY + "x" + entry.getValue())
                        .forEach(entityLabel::addLine);

                //entityLabel.getParent().setHeight(entityLabel.getLines().length * (entityLabel.getTheme().getFontRenderer().getFontHeight()+1) + 3);
            }
        });
        frame.addChild(entityLabel);
        frame.setPinnable(true);
        entityLabel.setShadow(true);
        entityLabel.setFontRenderer(fontRenderer);
        frames.add(frame);

        /*
        Coordinates
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Coordinates");
        frame.setCloseable(false);
        frame.setPinnable(true);
        Label coordsLabel = new Label("");
        coordsLabel.addTickListener(new TickListener() {
            Minecraft mc = Minecraft.getMinecraft();

            @Override
            public void onTick() {
                boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

                int posX = (int) mc.player.posX;
                int posY = (int) mc.player.posY;
                int posZ = (int) mc.player.posZ;

                float f = !inHell ? 0.125f : 8;
                int hposX = (int) (mc.player.posX * f);
                int hposZ = (int) (mc.player.posZ * f);

                coordsLabel.setText(String.format(" %sf%,d%s7, %sf%,d%s7, %sf%,d %s7(%sf%,d%s7, %sf%,d%s7, %sf%,d%s7)",
                        Command.SECTION_SIGN,
                        posX,
                        Command.SECTION_SIGN,
                        Command.SECTION_SIGN,
                        posY,
                        Command.SECTION_SIGN,
                        Command.SECTION_SIGN,
                        posZ,
                        Command.SECTION_SIGN,
                        Command.SECTION_SIGN,
                        hposX,
                        Command.SECTION_SIGN,
                        Command.SECTION_SIGN,
                        posY,
                        Command.SECTION_SIGN,
                        Command.SECTION_SIGN,
                        hposZ,
                        Command.SECTION_SIGN
                ));
            }
        });
        frame.addChild(coordsLabel);
        coordsLabel.setFontRenderer(fontRenderer);
        coordsLabel.setShadow(true);
        frame.setHeight(20);
        frames.add(frame);

        /*
        Radar
         */
        frame = new Frame(getTheme(), new Stretcherlayout(1), "Radar");
        frame.setCloseable(false);
        frame.setMinimizeable(true);
        frame.setPinnable(true);
        frame.addChild(new Radar());
        frame.setWidth(100);
        frame.setHeight(100);
        frames.add(frame);

        for (Frame frame1 : frames) {
            frame1.setX(x);
            frame1.setY(y);

            nexty = Math.max(y + frame1.getHeight() + 10, nexty);
            x += frame1.getWidth() + 10;
            if (x * DisplayGuiScreen.getScale() > Wrapper.getMinecraft().displayWidth / 1.2f) {
                y = nexty;
                nexty = y;
                x = 10;
            }

            addChild(frame1);
        }
    }

    private static String getEntityName(@Nonnull Entity entity) {
        if (entity instanceof EntityItem) {
            return TextFormatting.DARK_AQUA + ((EntityItem) entity).getItem().getItem().getItemStackDisplayName(((EntityItem) entity).getItem());
        }
        if (entity instanceof EntityWitherSkull) {
            return TextFormatting.DARK_GRAY + "Wither skull";
        }
        if (entity instanceof EntityEnderCrystal) {
            return TextFormatting.LIGHT_PURPLE + "End crystal";
        }
        if (entity instanceof EntityEnderPearl) {
            return "Thrown ender pearl";
        }
        if (entity instanceof EntityMinecart) {
            return "Minecart";
        }
        if (entity instanceof EntityItemFrame) {
            return "Item frame";
        }
        if (entity instanceof EntityEgg) {
            return "Thrown egg";
        }
        if (entity instanceof EntitySnowball) {
            return "Thrown snowball";
        }

        return entity.getName();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, Comparator.comparing(o -> (o.getValue())));

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override public void destroyGUI() { kill(); }

    private static final int DOCK_OFFSET = 0;

    public static void dock(Frame component) {
        Docking docking = component.getDocking();
        if (docking.isTop())
            component.setY(DOCK_OFFSET);
        if (docking.isBottom())
            component.setY((Wrapper.getMinecraft().displayHeight / DisplayGuiScreen.getScale()) - component.getHeight() - DOCK_OFFSET);
        if (docking.isLeft())
            component.setX(DOCK_OFFSET);
        if (docking.isRight())
            component.setX((Wrapper.getMinecraft().displayWidth / DisplayGuiScreen.getScale()) - component.getWidth() - DOCK_OFFSET);
        if (docking.isCenterHorizontal())
            component.setX((Wrapper.getMinecraft().displayWidth / (DisplayGuiScreen.getScale() * 2) - component.getWidth() / 2));
        if (docking.isCenterVertical())
            component.setY(Wrapper.getMinecraft().displayHeight / (DisplayGuiScreen.getScale() * 2) - component.getHeight() / 2);

    }
}
