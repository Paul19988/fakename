package tschipp.fakename;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "fakename", name = "Fake Name", version = "1.2")
public class FakeName {

	@Instance(value = "fakename")
	private static FakeName instance;

	private SimpleNetworkWrapper network;
	
	public static final String CLIENT_PROXY = "tschipp.fakename.ClientProxy";
	public static final String COMMON_PROXY = "tschipp.fakename.CommonProxy";

	public static final String KEY = "fakename";

	@SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
	private static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);

		network = NetworkRegistry.INSTANCE.newSimpleChannel("FakeNameChannel");
		network.registerMessage(FakeNamePacketHandler.class, FakeNamePacket.class, 0, Side.CLIENT);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandFakeName());
	}

	public SimpleNetworkWrapper getNetwork() {
		return this.network;
	}

	public static FakeName getInstance() {
		return instance;
	}

	public CommonProxy getProxy() {
		return this.proxy;
	}
}
