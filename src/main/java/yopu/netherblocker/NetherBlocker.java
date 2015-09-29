package yopu.netherblocker;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.BlockEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod(modid = NetherBlocker.MODID)
public class NetherBlocker {

    public static final String MODID = "netherblocker";

    @Instance
    public static NetherBlocker instance;

    private Map<Integer, Set<UniqueIdentifier>> dimensionWhitelist;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        String[] whiteListedBlocks = configuration.getStringList("block_whitelist", "main", new String[]{"-1:minecraft:obsidian"}, "Blocks allowed in the following format: 'dimension:blockid'");
        configuration.save();

        dimensionWhitelist = parseBlockWhitelist(whiteListedBlocks);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event) {
        if (event.player.capabilities.isCreativeMode)
            return;

        int playerDimension = event.player.dimension;
        if (dimensionWhitelist.keySet().contains(playerDimension)) {

            Set<UniqueIdentifier> uniqueIdentifierWhitelist = dimensionWhitelist.get(playerDimension);
            UniqueIdentifier placedBlockUniqueIdentifier = GameRegistry.findUniqueIdentifierFor(event.placedBlock);

            if (!uniqueIdentifierWhitelist.contains(placedBlockUniqueIdentifier)) {
                event.setCanceled(true);
            }
        }
    }

    private Map<Integer, Set<UniqueIdentifier>> parseBlockWhitelist(String[] rawBlockWhitelist) {
        HashMap<Integer, Set<UniqueIdentifier>> map = new HashMap<Integer, Set<UniqueIdentifier>>();

        for (String s : rawBlockWhitelist) {
            int i = s.indexOf(':');
            int dim = Integer.parseInt(s.substring(0, i));

            Set<UniqueIdentifier> whitelist;
            if (!map.containsKey(dim)) {
                whitelist = new HashSet<UniqueIdentifier>();
                map.put(dim, whitelist);
            } else {
                whitelist = map.get(dim);
            }

            String blockID = s.substring(i + 1);
            whitelist.add(new UniqueIdentifier(blockID));
        }

        return map;
    }
}
