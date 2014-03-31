package me.vaqxine.ShopMechanics;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.vaqxine.Main;
import me.vaqxine.AchievmentMechanics.AchievmentMechanics;
import me.vaqxine.ChatMechanics.ChatMechanics;
import me.vaqxine.CommunityMechanics.CommunityMechanics;
import me.vaqxine.DuelMechanics.DuelMechanics;
import me.vaqxine.HealthMechanics.HealthMechanics;
import me.vaqxine.Hive.Hive;
import me.vaqxine.InstanceMechanics.InstanceMechanics;
import me.vaqxine.ItemMechanics.ItemMechanics;
import me.vaqxine.KarmaMechanics.KarmaMechanics;
import me.vaqxine.LootMechanics.LootMechanics;
import me.vaqxine.MoneyMechanics.MoneyMechanics;
import me.vaqxine.MonsterMechanics.MonsterMechanics;
import me.vaqxine.PetMechanics.PetMechanics;
import me.vaqxine.ProfessionMechanics.ProfessionMechanics;
import me.vaqxine.RealmMechanics.RealmMechanics;
import me.vaqxine.RepairMechanics.RepairMechanics;
import me.vaqxine.ScoreboardMechanics.ScoreboardMechanics;
import me.vaqxine.TradeMechanics.TradeMechanics;
import me.vaqxine.TutorialMechanics.TutorialMechanics;
import me.vaqxine.database.ConnectionPool;
import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayOutWorldEvent;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;

import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.DespawnReason;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import de.kumpelblase2.remoteentities.entities.RemotePlayer;

public class ShopMechanics implements Listener {
	static Logger log = Logger.getLogger("Minecraft");
	
	private static final String ALPHA_NUM =  
			"123456789";  

	public static EntityManager npc_manager = null;
	
	static HashMap<Block, RemoteEntity> shop_nameplates = new HashMap<Block, RemoteEntity>();
	// NPC linked list that assigns an NPC to a given shop block.

	public static HashMap<String, Integer> shop_level = new HashMap<String, Integer>();
	// Locally stored shop level/tier.

	static HashMap<Block, String> shop_names = new HashMap<Block, String>();
	// Block-to-Shop name map for data reference.

	static HashMap<Block, String> shop_owners = new HashMap<Block, String>();
	// Owner of a given block shop.

	static HashMap<Block, Block> chest_partners = new HashMap<Block, Block>();
	// Allows us to get the owner w/o using List<Block>, the blocks will always have the inverse values of each other.

	static HashMap<String, Block> inverse_shop_owners = new HashMap<String, Block>();
	// Find block_1 of the shop owned by PLAYER_NAME

	public static HashMap<String, Inventory> shop_stock = new HashMap<String, Inventory>();
	// The current shop stock (inventory format) of PLAYER_NAME.

	public static HashMap<String, ItemStack> current_item_being_stocked = new HashMap<String, ItemStack>();
	// Async Chat event.

	public static HashMap<String, Integer> current_item_being_bought = new HashMap<String, Integer>();
	// Async Chat event.

	public static HashMap<String, Integer> shop_server = new HashMap<String, Integer>();
	// Locally stored data, Player, Shop Server

	static HashMap<String, String> shop_being_browsed = new HashMap<String, String>();
	// You get too far away, you can't purchase.

	static HashMap<String, String> shop_upgrade_codes = new HashMap<String, String>();
	// Unique upgrade codes.

	static HashMap<String, Long> last_shop_open = new HashMap<String, Long>();
	// Last time a shop was opened by a player (customer), prevents spamming up viewcount of a shop.

	// DEPRECIATED, used to be used when we had to spoof shop location with packets.
	//public static HashMap<String, List<Block>> shops_near = new HashMap<String, List<Block>>();

	public static volatile ConcurrentHashMap<String, Inventory> collection_bin = new ConcurrentHashMap<String, Inventory>();
	// Collection bin of users in inventory format. This is rendered at many different times depending on the situation.
	// Player Login, if they have items waiting -- Server stop, all shops converted to collection bins

	ItemStack gray_button = setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 8), ChatColor.GREEN.toString() + "Click to OPEN Shop.", ChatColor.GRAY.toString() + "This will open your shop to the public.");
	ItemStack green_button = setIinfo(new ItemStack(Material.INK_SACK, 1, (short) 10), ChatColor.RED.toString() + "Click to CLOSE Shop.", ChatColor.GRAY.toString() + "This will allow you to edit your stock.");

	public static List<Player> price_update_needed = new ArrayList<Player>();
	// Used with an item is put in shop and needs to update price of other items. (right click reprice)

	public static List<String> openning_shop = new ArrayList<String>();
	// Pending shop open -- "PLEASE ENTER SHOP NAME!"

	public static List<String> shop_name_list = new ArrayList<String>();
	// List of all shop names. Prevents duplicate names.

	public static List<Block> open_shops = new ArrayList<Block>();
	// Determine if a given block is indeed a shop. isShop(b);

	public static CopyOnWriteArrayList<RemoteEntity> npc_to_remove = new CopyOnWriteArrayList<RemoteEntity>();
	// List of NPC's to kill on the main thread if concurrency issues arise.

	public static boolean shop_shutdown = false;

	public static volatile CopyOnWriteArrayList<String> need_sql_update = new CopyOnWriteArrayList<String>();
	// A list of all shop owner's that need their shop contents updated SQL side.

	public static boolean all_collection_bins_uploaded = false;
	
	public static CopyOnWriteArrayList<Integer> packet_resend = new CopyOnWriteArrayList<Integer>();
	
	Thread store_backup;

	static ShopMechanics sm = null;
	
	@SuppressWarnings("deprecation")
	public void onEnable() {
		Main.plugin.getServer().getPluginManager().registerEvents(this, Main.plugin);
		sm = this;
		
		store_backup = new BackupStoreData();
		store_backup.start();

		Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable(){
			public void run(){
				npc_manager = RemoteEntities.createManager(Main.plugin);
			}
		}, 2 * 20L);
		
		Main.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				if(npc_to_remove.size() <= 0){return;}
				for(RemoteEntity re : npc_to_remove){
					if(re == null){
						npc_to_remove.remove(re);
						continue;
					}
					
					re.despawn(DespawnReason.CUSTOM);
					npc_to_remove.remove(re);
				}
			}
		}, 10 * 20L, 10L);

		Main.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(Main.plugin, new Runnable() {
			public void run() {
				Iterator<net.citizensnpcs.api.npc.NPC> itr = CitizensAPI.getNPCRegistry().iterator();
				while(itr.hasNext()){
					net.citizensnpcs.api.npc.NPC cnpc = (net.citizensnpcs.api.npc.NPC) itr.next();

					int hp = 50;
					if(!(cnpc.getBukkitEntity() instanceof Player))continue;
					Player pl = (Player)cnpc.getBukkitEntity();
					if(pl == null){
						continue;
					}
					//if(HealthMechanics.objective.getScore(pl).getScore() > 0){
					//	continue;
					//}
					if(MonsterMechanics.getNPCTier(pl.getInventory().getArmorContents()) == 1){
						hp = 200;
					}
					if(MonsterMechanics.getNPCTier(pl.getInventory().getArmorContents()) == 2){
						hp = 1000;
					}
					if(MonsterMechanics.getNPCTier(pl.getInventory().getArmorContents()) == 3){
						hp = 5000;
					}
					if(MonsterMechanics.getNPCTier(pl.getInventory().getArmorContents()) == 4){
						hp = 10000;
					}
					if(MonsterMechanics.getNPCTier(pl.getInventory().getArmorContents()) == 5){
						hp = 20000;
					}
					HealthMechanics.setOverheadHP(pl, hp);
				}
			}
		}, 10 * 20L, 5 * 20L);
		
		// Cleanup all fake entities which are visual glitches
		/*new BukkitRunnable() {
			@Override
			public void run() {
				if(packet_resend.isEmpty()) return;
				int[] id = new int[packet_resend.size()];
				for(int x = 0; x < packet_resend.size(); x++){
					id[x] = packet_resend.get(x);
				}
				PacketPlayOutEntityDestroy packet = getDestroyPacket(id);
				for(Player p : Bukkit.getOnlinePlayers()){
					Main.d("Sent to " + p.getName() + " : " + id);
					((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
				}
				packet_resend.clear();
			}
		}.runTaskTimer(Main.plugin, 100L, 100L);*/

		log.info("[ShopMechanics] has been enabled.");
	}
	
	/*public static PacketPlayOutEntityDestroy getDestroyPacket(int[] entities) {
		PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy();
		
		try {
			Field a = getField(destroyPacket.getClass(), "a");
			a.setAccessible(true);
			a.set(destroyPacket, entities);
		} catch(IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		return destroyPacket;
	}*/
	
	public static Field getField(Class<?> cl, String field_name) {
		try {
			Field field = cl.getDeclaredField(field_name);
			return field;
		} catch(SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onDisable() {
		shop_shutdown = true;
		// So it doesn't think server is frozen.

		removeAllShops(); // Needed to upload data of offline players.
		uploadAllCollectionBinData(); // Uploads / sends sockets to all servers for new collection bin data.

		/*int timeout = 0;
		while((collection_bin.size() > 0) && timeout <= 200){
			// The timeout is for multithreaded player data upload events.
			timeout++;
			log.info("[ShopMechanics] (" + timeout + "/200" + ") COLLECTION BIN: " + collection_bin.size());
			for(String s : collection_bin.keySet()){
				log.info("CBIN: " + s);
			}
			//log.info("[ShopMechanics] SHOP OWNERS: " + shop_owners.size());
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}*/

		log.info("[ShopMechanics] has been disabled.");
	}

	public void setStockCount(Player shop_tag, int stock){
		for(Player p : Bukkit.getOnlinePlayers()){
			ScoreboardMechanics.getBoard(p).getObjective(DisplaySlot.BELOW_NAME).getScore(shop_tag).setScore(stock);
		}
	}

	public void incrementViewCount(Player shop_tag){
		for(Player p : Bukkit.getOnlinePlayers()){
			Score c = ScoreboardMechanics.getBoard(p).getObjective(DisplaySlot.BELOW_NAME).getScore(shop_tag);
			int new_score = c.getScore() + 1;
			c.setScore(new_score);
		}
	}

	public void cleanupNullNPC(){
		List<RemoteEntity> to_remove = new ArrayList<RemoteEntity>();
		for(RemoteEntity re : ((de.kumpelblase2.remoteentities.EntityManager) npc_manager).getAllEntities()){
			if(re.getBukkitEntity().getLocation().add(0, 1, 0).getBlock().getType() != Material.CHEST){
				to_remove.add(re);
			}
		}
		for(RemoteEntity re : to_remove){
		    re.despawn(DespawnReason.CUSTOM);
			Hive.npc_manager.removeEntity(re.getID(), true);
		}
	}

	public static boolean isOpenButton(ItemStack is){
		if(is.getType() == Material.INK_SACK && (is.getDurability() == (short)8 || is.getDurability() == (short)10)){
			if(is.hasItemMeta() && is.getItemMeta().hasDisplayName()){
				String item_name = is.getItemMeta().getDisplayName();
				if(item_name.contains("Shop")){
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void removeAllShops(){
		for (Block b1 : inverse_shop_owners.values()){

			if(b1.getType() != Material.CHEST){
				log.info("[ShopMechanics] Skipping a chest shop due to invalid block type.");
				continue;
			}

			String shop_owner_n = "";

			try{
				final Block b2 = chest_partners.get(b1);
				b1.setType(Material.AIR);
				b2.setType(Material.AIR);

				RemoteEntity n = shop_nameplates.get(b1);
				n.despawn(DespawnReason.CUSTOM);

				shop_owner_n = shop_owners.get(b1);

				if(!collection_bin.containsKey(shop_owner_n)){

					Inventory inv = shop_stock.get(shop_owner_n);
					inv.remove(inv.getSize() - 1);
					List<ItemStack> li = new ArrayList<ItemStack>();

					for(ItemStack is : inv.getContents()){
						if(is == null || is.getType() == Material.AIR || isOpenButton(is)){
							continue;
						}

						li.add(is);
					}

					if(li.size() > 0){
						Inventory cb = Bukkit.createInventory(null, 54, "Collection Bin");
						for(ItemStack is : li){
							cb.setItem(cb.firstEmpty(), is);
						}
						collection_bin.put(shop_owner_n, cb);
					}
					//else if(li.size() <= 0){
					asyncSetShopServerSQL(shop_owner_n, -1);
					//}

					/*if(!(need_sql_update.contains(shop_owner_n))){
					need_sql_update.add(shop_owner_n); // Update SQL after an item is sold from the shop.
					}*/

					shop_owners.remove(b1);

				}

			} catch(Exception err){
				/*if(!shop_owner_n.equalsIgnoreCase("")){
					asyncSetShopServerSQL(shop_owner_n, -1);
				}*/
				err.printStackTrace();
				continue;
			}
		}
		
		inverse_shop_owners.clear();
	}

	public void clearCollectionBinSQL(String p_name){
		Connection con = null;
		PreparedStatement pst = null;

		try {
			pst = ConnectionPool.getConnection().prepareStatement( 
					"INSERT INTO shop_database (p_name, collection_bin)"
							+ " VALUES"
							+ "('"+ p_name + "', '"+ "null" +"') ON DUPLICATE KEY UPDATE collection_bin = '" + "null" + "'");

			pst.executeUpdate();


		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);



		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException ex) {
				log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}

	public static String getItemName(ItemStack i){
		CraftItemStack css = (CraftItemStack)i;
		String name = CraftItemStack.asNMSCopy(css).getTag().getCompound("display").getString("Name");
		if(name.contains(ChatColor.WHITE.toString())){
			name.replaceAll(ChatColor.WHITE.toString(), "");
		}
		if(name.contains(ChatColor.GREEN.toString())){
			name.replaceAll(ChatColor.GREEN.toString(), "");
		}
		if(name.contains(ChatColor.AQUA.toString())){
			name.replaceAll(ChatColor.AQUA.toString(), "");
		}
		if(name.contains(ChatColor.LIGHT_PURPLE.toString())){
			name.replaceAll(ChatColor.LIGHT_PURPLE.toString(), "");
		}
		if(name.contains(ChatColor.YELLOW.toString())){
			name.replaceAll(ChatColor.YELLOW.toString(), "");
		}

		return name;
	}

	// Run on crash.
	public static void backupStoreData(String p_name){
		if(shop_stock.containsKey(p_name)){
			Inventory i = shop_stock.get(p_name);
			List<ItemStack> li = new ArrayList<ItemStack>();

			for(ItemStack is : i.getContents()){
				if(is != null && is.getType() != Material.AIR 
						&& !(is.getType() == Material.INK_SACK && (is.getDurability() == (short)8 || is.getDurability() == (short)10))){
					//li.add(removePrice(is));
					li.add(is);
				}
			}

			if(li.size() > 0){
				Inventory cb = Bukkit.createInventory(null, 54, "Collection Bin");
				for(ItemStack is : li){
					if(is == null){
						continue;
					}
					cb.setItem(cb.firstEmpty(), is);
				}
				collection_bin.put(p_name, cb);

				if(!(Hive.player_inventory.containsKey(p_name))){
					// If they're not online, upload the data here instead of uploadShopDatabaseData().
					uploadCollectionBinData(p_name); 
				}
			}
		}
	}

	public int getServerLocationOfShop(String p_name){
		if(shop_server.containsKey(p_name)){
			return shop_server.get(p_name);
		}

		shop_server.put(p_name, -1);
		return -1;
	}

	public static int getShopLevel(String p_name){

		if(shop_level.containsKey(p_name)){
			return shop_level.get(p_name);
		}

		return 0;
	}

	@SuppressWarnings("deprecation")
	public static Inventory legacyShopStringToInventory(final String p_name, String collection_bin_string){
		Inventory collection_bin_contents = Bukkit.createInventory(null, 63, "Collection Bin");
		String collection_bin_content_data = collection_bin_string;
		boolean has_nbt = false;
		int slot_x = -1;

		if(collection_bin_content_data.equalsIgnoreCase("empty")){
			return null; // Return empty list.
		}

		String partial_data = "";

		for(String s : collection_bin_content_data.split("_")){
			if(s.length() <= 0){continue;}
			has_nbt = false;

			slot_x++;

			if(s.equalsIgnoreCase("AIR")){
				slot_x--;
				/*try{
					collection_bin_contents.setItem(slot_x, new ItemStack(Material.AIR));
				} catch(ArrayIndexOutOfBoundsException err){

					err.printStackTrace();
					continue;
				}*/
				continue;
			}

			if(partial_data.length() > 0){
				s = partial_data + "_" + s;
				partial_data = "";
			}

			String[] s_sub = s.split("=");

			if(s.contains("@")){
				has_nbt = true;
			}

			int type_id = Integer.parseInt(s_sub[0]);
			int amount = Integer.parseInt(s_sub[1]);
			short meta_data = Short.parseShort(s_sub[2].split("@")[0]);

			ItemStack i = new ItemStack(type_id, amount, meta_data);

			if(has_nbt == true){
				String nbt_data = "";
				try{
					try{
						nbt_data = s.substring(s.indexOf("@") + 1, s.lastIndexOf("@"));
						String nbt_name = nbt_data.substring(nbt_data.indexOf("#") + 1, nbt_data.lastIndexOf("#"));
						nbt_data = nbt_data.replace("#" + nbt_name + "#", "");
						if(nbt_data.contains("#")){
							nbt_data = nbt_data.substring(nbt_data.lastIndexOf("#") + 1, nbt_data.lastIndexOf("@"));
						}

						if(nbt_name.contains("Harrison")){
							nbt_name = ChatColor.BOLD.toString() + "Teleport:" + ChatColor.WHITE.toString() + " Harrison Field";
						}

						if(nbt_name.equalsIgnoreCase("NOPE")){
							nbt_name = ChatColor.WHITE.toString() + RealmMechanics.getFormalMatName(Material.getMaterial(type_id), meta_data);
						}

						ItemStack custom_i = ItemMechanics.signCustomItem(Material.getMaterial(type_id), meta_data, nbt_name, nbt_data);

						if(s.contains("[larm1]")){
							// Set leather color.
							ItemMeta im = custom_i.getItemMeta();
							LeatherArmorMeta lam = (LeatherArmorMeta)im;
							int bgr_color = Integer.parseInt(s.substring((s.indexOf("[larm1]") + 7), s.indexOf("[larm2]")));
							lam.setColor(Color.fromBGR(bgr_color));
							custom_i.setItemMeta(lam);
						}

						if(custom_i.getType() == Material.POTION && custom_i.getDurability() > 0){
							// Renames potion to Instant Heal.
							custom_i = ItemMechanics.signNewCustomItem(Material.getMaterial(type_id), meta_data, nbt_name, nbt_data);
						}

						if(nbt_name.contains("Sheep") && nbt_name.contains("''")){
							// Sheep O' Luck
							custom_i = PetMechanics.generatePetEgg(EntityType.SHEEP, "green");
						}

						custom_i.setAmount(amount);
						collection_bin_contents.setItem(slot_x, custom_i);
						continue;

					} catch(StringIndexOutOfBoundsException e){
						//log.info(nbt_data);
						partial_data = s;
						slot_x--;
						continue;
					}
				} catch(ArrayIndexOutOfBoundsException err){
					continue;
				}
			}

			else{
				try{
					collection_bin_contents.setItem(slot_x, i);
				} catch(ArrayIndexOutOfBoundsException err){
					continue;
				}
			}
		}

		return collection_bin_contents;
	}

	public static boolean downloadShopDatabaseData(String p_name){
		PreparedStatement pst = null;

		collection_bin.remove(p_name);
		shop_level.remove(p_name);
		shop_server.remove(p_name);

		try {
			pst = ConnectionPool.getConnection().prepareStatement(
					"SELECT level, server_num, collection_bin FROM shop_database WHERE p_name = '" + p_name + "'");

			pst.execute();

			ResultSet rs = pst.getResultSet();
			if(!rs.next()){
				shop_level.put(p_name, 0);
				shop_server.put(p_name, -1);
				return true;
			}

			int level = 0;
			int server_num = -1;

			server_num = rs.getInt("server_num");
			level = rs.getInt("level");

			String collection_bin_s = rs.getString("collection_bin");

			shop_level.put(p_name, level);
			shop_server.put(p_name, server_num);

			if(collection_bin_s != null && !collection_bin_s.equalsIgnoreCase("null")){
				// They have a collection bin.
				Inventory inv = null;
				/*if(!collection_bin_s.contains("@item@") && collection_bin_s.length() > 0){
					// Convert to new format.
					log.info("[ShopMechanics] Legacy collection convert: " + collection_bin_s);
					inv = legacyShopStringToInventory(p_name, collection_bin_s);
				}*/
				if(collection_bin_s.length() > 0){
					if(level <= 6 && collection_bin_s.split("@item@").length <= 54){
						inv = Hive.convertStringToInventory(null, collection_bin_s, "Collection Bin", 54);
					}
				}

				if(inv != null){
					collection_bin.put(p_name, inv);
				}
			}

			return true;

		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);
			return false;

		} finally {
			try {
				if (pst != null) {
					pst.close();
				}

			} catch (SQLException ex) {
				log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}

	public static void uploadCollectionBinData(String p_name){
		// This function is used to upload data on serverStop for players who may not be on this server. Now, this presents a unique challenge because if players are on ANOTHER server
		// that perhaps restarts AFTER this one, their collection bin data could be overwritten. So, we could send a socket out to all servers to update collection_bin data, but still could
		// cause the occasional issue...

		// Send socket -> THEN update the SQL.

		if(!(collection_bin.containsKey(p_name))){
			return; // No one cares.
		}

		String collection_bin_s = Hive.convertInventoryToString(null, collection_bin.get(p_name), false);

		if(collection_bin_s.length() <= 0){
			collection_bin_s = "null";
		}
		
		if(!(Hive.local_ddos)){
			// Tell any server with the player online the new collection bin data in real time.
			//ConnectProtocol.sendResultCrossServer("*", "@collection_bin@" + p_name + collection_bin.get(p_name));
			List<Object> query = new ArrayList<Object>();
			query.add("@collection_bin@" + p_name + "&" + collection_bin.get(p_name));
			query.add(null);
			query.add(true);
			CommunityMechanics.social_query_list.put(p_name, query);
		}

		PreparedStatement pst = null;

		try {

			pst = ConnectionPool.getConnection().prepareStatement( 
					"INSERT INTO shop_database (p_name, collection_bin)"
							+ " VALUES"
							+ "('"+ p_name + "', '" + StringEscapeUtils.escapeSql(collection_bin_s) + "') ON DUPLICATE KEY UPDATE collection_bin='" + StringEscapeUtils.escapeSql(collection_bin_s) + "'");

			pst.executeUpdate();

		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);


		} finally {
			try {
				if (pst != null) {
					pst.close();
				}

			} catch (SQLException ex) {
				log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}

		log.info("[ShopMechanics] Uploaded collection bin data for " + p_name + "...");

		if(!shop_shutdown){
			// If we're shutting down, then the system won't remove the collection_bin string... why?
			collection_bin.remove(p_name);
		}

		asyncSetShopServerSQL(p_name, -1);
	}

	public static void uploadAllCollectionBinData(){
		for(String p_name : collection_bin.keySet()){
			//if(Bukkit.getPlayer(p_name) == null && !(Hive.pending_upload.contains(p_name))){			
			uploadCollectionBinData(p_name);
			//}
		}
		all_collection_bins_uploaded = true;
	}

	// When they take items out of collection bin, it's set to 'empty' on server reboot.
	// Then, when server starts again, the restore script sees they have a shop_backup and no collection_bin.
	// We need to clear the shop_backup when collection_bin becomes empty.
	public static void uploadShopDatabaseData(String p_name, boolean remove_when_done){
		String collection_bin_s = "null";
		int server_num = -1;
		if(collection_bin.containsKey(p_name)){
			collection_bin_s = Hive.convertInventoryToString(p_name, collection_bin.get(p_name), false);
			if(collection_bin_s.equalsIgnoreCase("")){
				collection_bin_s = "null"; // No items left in collection bin.
			}
		}
		if(shop_server.containsKey(p_name)){
			server_num = shop_server.get(p_name);
		}

		int lshop_level = -1;
		if(shop_level.containsKey(p_name)){
			lshop_level = shop_level.get(p_name);
		}

		if(lshop_level == -1){
			log.info("[ShopMechanics] Skipping shop_database upload for " + p_name + ", data does not exist.");
			return; // Do not upload, something is wrong.
		}

		PreparedStatement pst = null;

		try {
			pst = ConnectionPool.getConnection().prepareStatement( 
					"INSERT INTO shop_database (p_name, level, server_num, collection_bin)"
							+ " VALUES"
							+ "('"+ p_name + "', '"+ lshop_level +"', '" + server_num + "', '" + StringEscapeUtils.escapeSql(collection_bin_s) + "') ON DUPLICATE KEY UPDATE level = '" + lshop_level + "', server_num='" + server_num + "', collection_bin='" + StringEscapeUtils.escapeSql(collection_bin_s) + "'");

			pst.executeUpdate();


		} catch (SQLException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);


		} finally {
			try {
				if (pst != null) {
					pst.close();
				}

			} catch (SQLException ex) {
				log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}

		if(remove_when_done){
			collection_bin.remove(p_name); //- Causes corruption of collection bin data.
			//shop_level.remove(p_name); Shop level is needed if a shop exists on the server.
			shop_server.remove(p_name);
		}
	}

	public static void removeShop(Player p){
		if(inverse_shop_owners.containsKey(p.getName())){
			try{
				final Block b1 = inverse_shop_owners.get(p.getName());
				final Block b2 = chest_partners.get(b1);

				final String shop_owner_n = p.getName();
				b1.setType(Material.AIR);
				b2.setType(Material.AIR);

				RemoteEntity n = shop_nameplates.get(b1);
				Main.d("Removing shop of " + p.getName());
				packet_resend.add(n.getBukkitEntity().getEntityId());
				n.despawn(DespawnReason.CUSTOM);

				shop_nameplates.remove(b1);
				shop_name_list.remove(ChatColor.stripColor(shop_names.get(b1).substring(shop_names.get(b1).indexOf(" ") + 1, shop_names.get(b1).length())));
				shop_names.remove(b1);
				shop_owners.remove(b1);
				inverse_shop_owners.remove(shop_owner_n);
				shop_stock.remove(shop_owner_n);
				current_item_being_stocked.remove(shop_owner_n);

				openning_shop.remove(shop_owner_n);
				open_shops.remove(b1);
				//modifying_stock.remove(shop_owner_n);

				Block other_chest = b2;

				chest_partners.remove(b1);
				chest_partners.remove(other_chest);

				shop_nameplates.remove(other_chest);
				shop_names.remove(other_chest);
				shop_owners.remove(other_chest);
				open_shops.remove(other_chest);

				asyncSetShopServerSQL(shop_owner_n, -1);

				if(!(need_sql_update.contains(p.getName()))){
					need_sql_update.add(p.getName()); // Update SQL after an item is sold from the shop.
				}
			} catch(Exception err){
				err.printStackTrace();
				return; 
			}
		}
	}

	public void upgradeShop(final Player p, int new_level, boolean interval){
		int r_new_level = 0;

		if(interval == true){
			r_new_level = getShopLevel(p.getName()) + 1;
		}
		else if(interval == false){
			r_new_level = new_level;
		}

		//final int f_new_level = r_new_level;

		shop_level.put(p.getName(), r_new_level);

		Inventory stock = shop_stock.get(p.getName());
		Inventory i = Bukkit.createInventory(null, getShopSlots(r_new_level), stock.getName());
		for(ItemStack is : stock.getContents()){
			if(is != null && !(is.getType() == Material.AIR) && (is.getType() != Material.INK_SACK && is.getDurability() != 8)){ // If it's not the button, we'll move the button to new last slot.
				i.setItem(i.firstEmpty(), is);
			}
		}

		i.setItem((i.getSize() - 1), gray_button); // Set the last slot to the open/close button.

		shop_stock.put(p.getName(), i);
	}

	public static int getShopSlots(int level){
		if(level == 0){
			return 9;
		}
		if(level == 1){
			return 18;
		}
		if(level == 2){
			return 27;
		}
		if(level == 3){
			return 36;
		}
		if(level == 4){
			return 45;
		}
		if(level == 5){
			return 54;
		}
		if(level == 6){
			return 63;
		}
		if(level == 7){
			return 72;
		}
		if(level == 8){
			return 81;
		}
		if(level == 9){
			return 90;
		}
		return 0;
	}

	public boolean doesPlayerHaveShopSQL(String p_name){
		int server_num = getServerLocationOfShop(p_name);
		if(server_num == -1){
			return false;
		}
		return true;
	}

	public static void runSyncQuery(String query){
		Connection con = null;
		PreparedStatement pst = null;

		try {
			pst = ConnectionPool.getConnection().prepareStatement(query);
			pst.executeUpdate();

			Hive.log.info("[Hive] SYNC Executed query: " + query);

		} catch (SQLException ex) {
			Hive.log.log(Level.SEVERE, ex.getMessage(), ex);

		} finally {
			try {
				if (pst != null) {
					pst.close();
				}
				if (con != null) {
					con.close();
				}

			} catch (SQLException ex) {
				Hive.log.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}

	public static void asyncSetShopServerSQL(final String p_name, final int server_num){
		shop_server.put(p_name, server_num);

		if(!shop_shutdown){
			Hive.sql_query.add( 
					"INSERT INTO shop_database (p_name, server_num)"
							+ " VALUES"
							+ "('"+ p_name + "', '"+ server_num +"') ON DUPLICATE KEY UPDATE server_num = '" + server_num + "'");
			// The issue with this, is if the server crashes... backups are kind of GG'd
			/*if(server_num == -1){
				Hive.sql_query.add( 
						"INSERT INTO shop_database (p_name, shop_backup)"
								+ " VALUES"
								+ "('"+ p_name + "', '') ON DUPLICATE KEY UPDATE shop_backup=''");
			}*/
		}
		else{
			// In this case, we are shutting down, do work on main thread do not use query.
			runSyncQuery("INSERT INTO shop_database (p_name, server_num)"
					+ " VALUES"
					+ "('"+ p_name + "', '"+ server_num +"') ON DUPLICATE KEY UPDATE server_num = '" + server_num + "'");
			/*if(server_num == -1){
				Hive.runSyncQuery(
						"INSERT INTO shop_database (p_name, shop_backup)"
								+ " VALUES"
								+ "('"+ p_name + "', '') ON DUPLICATE KEY UPDATE shop_backup=''");
			}*/
		}

	}


	public ItemStack setIinfo(ItemStack orig_i, String name, String desc) {
		ItemMeta im = orig_i.getItemMeta();
		im.setDisplayName(name);
		List<String> lore_list = new ArrayList<String>(Arrays.asList(desc));
		im.setLore(lore_list);
		orig_i.setItemMeta(im);
		return orig_i;
	}

	public boolean hasLocalShop(String p_name) {
		if (shop_stock.containsKey(p_name)) {
			return true;
		}
		return false;
	}

	public boolean isShopOwner(Player p, Block b) {
		if (!shop_owners.containsKey(b)) {
			return false;
		}
		if(p.isOp()){
		    return true;
		}
		if (!shop_owners.get(b).equalsIgnoreCase(p.getName())) {
			return false;
		}
		return true;
	}

	public static boolean isShop(Block b) {
		if (b.getType() != Material.CHEST) {
			return false;
		}
		if (!(shop_owners.containsKey(b))) {
			return false;
		}
		// Player owner = getShopOwner(b);
		// if(!(open_shops.contains(b))){return false;}
		return true;
	}

	public static boolean isShopOpen(Block b) {
		if (!(open_shops.contains(b))) {
			return false;
		}
		return true;
	}

	public Player getShopOwner(Block b) {
		return Bukkit.getPlayer(shop_owners.get(b));
	}

	public Inventory getShopStock(Block b) {
		if (!(isShop(b))) {
			return null;
		}
		String p_name = shop_owners.get(b);
		Inventory i = shop_stock.get(p_name);
		return i;
	}

	public static boolean hasCustomName(ItemStack i) {
		try {
			try {
				String fake_var = CraftItemStack.asNMSCopy(i).getTag().getCompound("display").getString("Name");
				// log.info(fake_var);
				if (fake_var != null && fake_var.length() > 0) {
					return true;
				}
			} catch (NullPointerException npe) {
				return false;
			}

		} catch (ClassCastException cce) {
			return false;
		}

		return false;
	}

	public static ItemStack setPrice(ItemStack i, int price) {
		boolean rename = false;
		String o_name = "";
		try {
			try {
				o_name = CraftItemStack.asNMSCopy(i).getTag().getCompound("display").getString("Name");
				rename = true;
				// log.info(o_name);
			} catch (NullPointerException npe) {
				rename = false;
			}
		} catch (ClassCastException cce) {
			rename = false;
		}

		List<String> old_lore = new ArrayList<String>();
		ItemMeta im = i.getItemMeta();

		if(i != null && i.hasItemMeta() && i.getItemMeta().hasLore()){
			for(String s : im.getLore()){
				old_lore.add(s);
			}

			if(rename == true && o_name.length() > 0){
				im.setDisplayName(o_name);
			} 

			old_lore.add(ChatColor.GREEN.toString() + "Price: " + ChatColor.WHITE.toString() + price + "g");
			im.setLore(old_lore);
			i.setItemMeta(im);
		}

		if(i != null && !(i.hasItemMeta() && !(i.getType() == Material.AIR))){
			old_lore.add(ChatColor.GREEN.toString() + "Price: " + ChatColor.WHITE.toString() + price + "g");
			ItemStack is = new ItemStack(Material.WOOD_SWORD, 1);
			im = is.getItemMeta();
			im.setLore(old_lore);
			//im.setLore(old_lore);
			i.setItemMeta(im);
		}

		return i;
	}

	public static ItemStack removePrice(ItemStack i) {

		if(i == null || !(i.hasItemMeta())){
			return i;
		}

		ItemMeta im = i.getItemMeta();
		List<String> old_lore = new ArrayList<String>();
		if(im.hasLore()){
			for(String s : im.getLore()){
				old_lore.add(s);
			}
		}

		List<String> s_to_remove = new ArrayList<String>();
		for(String s : old_lore){
			if(s.contains("Price:")){
				s_to_remove.add(s);
			}
			if(ChatColor.stripColor(s).contains("E-CASH")){
				s_to_remove.add(s);
			}
		}

		for(String s : s_to_remove){
			old_lore.remove(s);
		}

		if(old_lore.size() <= 0){
			i.setItemMeta(null);
		}
		else if(old_lore.size() > 0){
			im.setLore(old_lore);
			i.setItemMeta(im);
		}

		return i;
	}

	public static int getPrice(ItemStack i) {
		
		if(i != null && i.hasItemMeta() && i.getItemMeta().hasLore()){
			List<String> lore = i.getItemMeta().getLore();
			for(String s : lore){
				if(s.contains("Price:")){
					return Integer.parseInt((s.substring(
									s.lastIndexOf(":") + 2,
									s.length() - 1)).replaceAll(
											ChatColor.WHITE.toString(), ""));
				}
			}
		}
		
		return -1;
		
		/*try {
			NBTTagList description = CraftItemStack.asNMSCopy(i).getTag().getCompound("display").getList("Lore", 0);
			int x = 0;
			while (description.size() > x) {
				if (description.get(x).toString().contains("Price:")) {
					String content = description.get(x).toString();

					return Integer
							.parseInt((content.substring(
									content.lastIndexOf(":") + 2,
									content.length() - 1)).replaceAll(
											ChatColor.WHITE.toString(), ""));
				}
				x++;
			}

		} catch (NullPointerException e) {
			return -1;
		}
		return -1;*/

	}

	public static void setStoreColor(Block b, ChatColor c) {
		// Player owner = getShopOwner(b);
		RemotePlayer re = (RemotePlayer) shop_nameplates.get(b);
		CraftPlayer p = (CraftPlayer) re.getBukkitEntity();
		CommunityMechanics.setColor(p, c);
	}


	public static boolean hasCollectionBinItems(String p_name){
		if(collection_bin.containsKey(p_name)){
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e){
		Player p = e.getPlayer(); // The person MOVING.

		if(current_item_being_stocked.containsKey(p.getName())){
			Block shop = inverse_shop_owners.get(p.getName());
			if(!(p.getWorld().getName().equalsIgnoreCase(shop.getWorld().getName())) || p.getLocation().distanceSquared(shop.getLocation()) > 16){ // They're too far to stock, let's cancel it for them.
				p.sendMessage(ChatColor.RED + "Pricing of item cancelled. >4 blocks from shop.");
				p.getInventory().setItem(p.getInventory().firstEmpty(),
						removePrice(current_item_being_stocked.get(p.getName())));
				current_item_being_stocked.remove(p.getName());
				p.updateInventory();
			}
		}

		if(current_item_being_bought.containsKey(p.getName())){
			if(!(shop_being_browsed.containsKey(p.getName()))){
				current_item_being_bought.remove(p.getName());
				return;
			}
			Block shop = inverse_shop_owners.get(shop_being_browsed.get(p.getName()));
			if(shop == null || !(p.getWorld().getName().equalsIgnoreCase(shop.getWorld().getName())) || p.getLocation().distanceSquared(shop.getLocation()) > 36){ // They're too far to buy, let's cancel it for them.
				p.sendMessage(ChatColor.RED + "Purchase of item cancelled. >6 blocks from shop.");
				current_item_being_bought.remove(p.getName());
				shop_being_browsed.remove(p.getName());
				p.updateInventory();
			}
		}

	}

	public String getUpgradeAuthenticationCode(Player p){
		if(shop_upgrade_codes.containsKey(p.getName())){
			return shop_upgrade_codes.get(p.getName());
		}
		else{
			return null;
		}
	}

	public int getShopUpgradeCost(int new_tier){
		if(new_tier == 1){return 200;}
		if(new_tier == 2){return 450;}
		if(new_tier == 3){return 800;}
		if(new_tier == 4){return 1200;}
		if(new_tier == 5){return 1500;}
		if(new_tier == 6){return 2000;}
		return 0;
	}

	public void generateUpgradeAuthenticationCode(Player p, String tier){
		StringBuffer sb = new StringBuffer(4);  
		for (int i=0;  i<4;  i++) {  
			int ndx = (int)(Math.random()*ALPHA_NUM.length());  
			sb.append(ALPHA_NUM.charAt(ndx));  
		}  

		shop_upgrade_codes.put(p.getName(), tier + sb.toString());
	}

	@SuppressWarnings("deprecation")
	public static boolean isThereAShopNear(Block b, int maxradius) {
		//if(b.getType() != Material.CAULDRON){
		//return false;
		//}
		BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
		BlockFace[][] orth = {{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.UP, BlockFace.EAST}, {BlockFace.NORTH, BlockFace.UP}};
		for (int r = 0; r <= maxradius; r++) {
			for (int s = 0; s < 6; s++) {
				BlockFace f = faces[s%3];
				BlockFace[] o = orth[s%3];
				if (s >= 3)
					f = f.getOppositeFace();
				if(!(b.getRelative(f, r) == null)){
					Block c = b.getRelative(f, r);

					for (int x = -r; x <= r; x++) {
						for (int y = -r; y <= r; y++) {
							Block a = c.getRelative(o[0], x).getRelative(o[1], y);
							if (a.getTypeId() == 54)
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean isThereALadderNear(Block b, int maxradius) {
		//if(b.getType() != Material.CAULDRON){
		//return false;
		//}
		BlockFace[] faces = {BlockFace.UP, BlockFace.NORTH, BlockFace.EAST};
		BlockFace[][] orth = {{BlockFace.NORTH, BlockFace.EAST}, {BlockFace.UP, BlockFace.EAST}, {BlockFace.NORTH, BlockFace.UP}};
		for (int r = 0; r <= maxradius; r++) {
			for (int s = 0; s < 6; s++) {
				BlockFace f = faces[s%3];
				BlockFace[] o = orth[s%3];
				if (s >= 3)
					f = f.getOppositeFace();
				if(!(b.getRelative(f, r) == null)){
					Block c = b.getRelative(f, r);

					for (int x = -r; x <= r; x++) {
						for (int y = -r; y <= r; y++) {
							Block a = c.getRelative(o[0], x).getRelative(o[1], y);
							if (a.getType() == Material.LADDER)
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean collectionBinHasPrices(String p_name){
		if(!(collection_bin.containsKey(p_name))){
			return false;
		}
		Inventory cb = collection_bin.get(p_name);
		if(getPrice(cb.getItem(0)) != -1){
			return true;
		}
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!(e.hasItem())) {
			return;
		}
		if (!(CommunityMechanics.isSocialBook(e.getItem()))) {
			return;
		}
		if (!(e.getAction() == Action.LEFT_CLICK_BLOCK)) {
			return;
		}
		final Player p = e.getPlayer();
		if (!(p.isSneaking())) {
			return;
		}

		if(openning_shop.contains(p.getName())){
			if(isShopOwner(p, e.getClickedBlock())){
				//TODO: Left click an opening shop = remove it instantly.
			}
			return;
		}

		e.setCancelled(true);

		if(TutorialMechanics.onTutorialIsland(p)){
			p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " place shops until you have completed Tutorial Island.");
			return;
		}

		if(HealthMechanics.in_combat.containsKey(p.getName())){
			p.sendMessage(ChatColor.RED + "You cannot place your shop while in combat.");
			p.sendMessage(ChatColor.GRAY + "Wait " + ChatColor.BOLD + "a few seconds" + ChatColor.GRAY + " and try again.");
			return;
		}

		if(hasCollectionBinItems(p.getName()) && (!collectionBinHasPrices(p.getName()) || !DuelMechanics.isDamageDisabled(p.getLocation()))){
			p.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
			p.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
			return;
		}

		if(!DuelMechanics.isPvPDisabled(e.getClickedBlock().getLocation())){
			// They're in chaotic zone.
			p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " place your shop in a chaotic zone.");
			return;
		}

		if(KarmaMechanics.getRawAlignment(p.getName()).equalsIgnoreCase("evil")){
			p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " place your shop while chaotic.");
			return;
		}

		if (hasLocalShop(p.getName()) && inverse_shop_owners.containsKey(p.getName())) {
			Block shop = inverse_shop_owners.get(p.getName());
			p.sendMessage(ChatColor.YELLOW + "You already have an open shop on " + ChatColor.UNDERLINE + "this" + ChatColor.YELLOW + " server.");
			p.sendMessage(ChatColor.GRAY + "Shop Location: " + (int)shop.getLocation().getX() + ", " + (int)shop.getLocation().getY() + ", " + (int)shop.getLocation().getZ());
			return;
		}

		if(!e.getClickedBlock().getWorld().getName().equalsIgnoreCase(Bukkit.getWorlds().get(0).getName())){
			p.sendMessage(ChatColor.RED + "You cannot setup your shop in a player owned realm.");
			return;
		}

		if(MoneyMechanics.isThereABankChestNear(e.getClickedBlock(), 15)){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}

		if(e.getClickedBlock().getType() == Material.FENCE || e.getClickedBlock().getType() == Material.CHEST || e.getClickedBlock().getType() == Material.LONG_GRASS || e.getClickedBlock().getType() == Material.THIN_GLASS || e.getClickedBlock().getType() == Material.WATER || e.getClickedBlock().getType() == Material.GLASS || e.getClickedBlock().getType() == Material.PORTAL){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}

		if(e.getClickedBlock().getType() == Material.STEP && e.getClickedBlock().getLocation().subtract(0, 1, 0).getBlock().getType() != Material.SMOOTH_BRICK){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}

		if(isThereALadderNear(e.getClickedBlock(), 3)){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}

		if (e.getClickedBlock().getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR
				|| e.getClickedBlock().getLocation().add(0, 2, 0).getBlock().getType() != Material.AIR
				|| e.getClickedBlock().getLocation().add(0, 3, 0).getBlock().getType() != Material.AIR
				|| (e.getClickedBlock().getLocation().add(1, 1, 0).getBlock().getType() != Material.AIR || e.getClickedBlock().getLocation().subtract(1, -1, 0).getBlock().getType() != Material.AIR)) {
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
			// p.sendMessage(ChatColor.GRAY + "REQ: 2 AIR Blocks above spot.");
		}

		if(doesPlayerHaveShopSQL(p.getName())){
			int server_num = getServerLocationOfShop(p.getName());
			String prefix = "US-";

			String motd = Bukkit.getMotd();
			int local_server_num = Integer.parseInt(motd.substring(motd.indexOf("-") + 1, motd.indexOf(" ")));

			if(motd.contains("EU-")){
				local_server_num += 1000;
			}

			if(motd.contains("BR-")){
				local_server_num += 2000;
			}

			if(server_num != local_server_num){
				if(server_num > 1000 && server_num < 2000){
					server_num -= 1000;
					prefix = "EU-";
				}
				if(server_num > 2000){
					server_num -= 2000;
					prefix = "BR-";
				}
				String server_name = prefix + server_num;
				p.sendMessage(ChatColor.YELLOW + "You already have an open shop on ANOTHER server.");
				p.sendMessage(ChatColor.GRAY + "Shop Location: " + server_name);
				return;
			}	
		}

		Location l1 = e.getClickedBlock().getLocation().add(0, 1, 0);
		Location l2 = e.getClickedBlock().getLocation().add(0, 1, 0);


		if(LootMechanics.loot_spawns.containsKey(l1) || LootMechanics.loot_spawns.containsKey(l2)){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}


		if (e.getClickedBlock().getLocation().add(1, 1, 0).getBlock().getType() == Material.AIR && !(e.getClickedBlock().getLocation().add(1, 0, 0).getBlock().getType() == Material.AIR)) {
			l2.add(1, 0, 0);

		} else {
			if(e.getClickedBlock().getLocation().subtract(1, 0, 0).getBlock().getType() == Material.AIR){
				p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
				return;
			}
			l2.subtract(1, 0, 0);
		}

		final Block b1 = p.getWorld().getBlockAt(l1);
		final Block b2 = p.getWorld().getBlockAt(l2);

		if(b1.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.STEP && b1.getLocation().subtract(0, 2, 0).getBlock().getType() != Material.SMOOTH_BRICK){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}

		if(b2.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.STEP && b2.getLocation().subtract(0, 2, 0).getBlock().getType() != Material.SMOOTH_BRICK){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}

		if(isThereAShopNear(b1, 2) || isThereAShopNear(b2, 2)){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			p.sendMessage(ChatColor.GRAY + "You're too close to an already open shop.");
			return;
		}

		List<Material> illegal = new ArrayList<Material>();
		illegal.add(Material.STEP);
		illegal.add(Material.FENCE);
		illegal.add(Material.GLASS);
		illegal.add(Material.WOOD_STAIRS);

		for(Material m : illegal){
			if(b1.getType() == m || b2.getType() == m){
				p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
				return;
			}
		}

		/*if(b1.getLocation().subtract(0, 0.25, 0).getBlock().getType() == Material.AIR
				|| b2.getLocation().subtract(0, 0.25, 0).getBlock().getType() == Material.AIR){
			p.sendMessage(ChatColor.RED + "You cannot place your shop here.");
			return;
		}*/

		b1.setType(Material.CHEST);
		b2.setType(Material.CHEST);
		shop_owners.put(b1, p.getName());
		shop_owners.put(b2, p.getName());
		inverse_shop_owners.put(p.getName(), b1);
		//inverse_shop_owners.put(p.getName(), b2);
		chest_partners.put(b1, b2);
		chest_partners.put(b2, b1);
		//modifying_stock.add(p.getName());
		//String default_shop_name = " Shop";

		openning_shop.add(p.getName());
		p.sendMessage(ChatColor.YELLOW + "Please enter a " + ChatColor.BOLD + "SHOP NAME." + ChatColor.YELLOW + " [max. 12 characters]");
		p.playSound(p.getLocation(), Sound.WOOD_CLICK, 1F, 0.8F);

		// TODO: Make sure this still works.
		new BukkitRunnable(){
			@Override
			public void run() {
				assignShopNameplate(p.getName(), b1.getLocation(), b2.getLocation());
				setStoreColor(b1, ChatColor.RED);
			}
		}.runTaskLater(Main.plugin, 1L);

	}


	@SuppressWarnings("deprecation")
	@EventHandler
	public void CollectionBinManager(InventoryClickEvent e){
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getWhoClicked();

		int slot = e.getRawSlot();
		//Inventory i = e.getInventory();

		if(!e.getInventory().getTitle().equalsIgnoreCase("Collection Bin")){
			return;
		}

		if(slot >= 54 && (e.getCursor() == null || e.getCursor().getType() == Material.AIR)){
			e.setCancelled(true); // Don't let them pick up items on cursor or shift click from their inventory.
			p.updateInventory();
			return;
		}

		int count = 0;
		for(ItemStack is : e.getInventory()){
			if(is != null && is.getType() != Material.AIR){
				count++;
			}
		}
		
		if(count > 0){
			collection_bin.put(p.getName(), p.getOpenInventory().getTopInventory());
		}
		else if(count <= 0){
			log.info("[ShopMechanics] Removing local collection_bin data for " + p.getName() + ", no items left in bin!");
			collection_bin.remove(p.getName()); // Nothing left in bin!
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // ignoreCancelled = true, prevents bug with shift left click.
	public void ShopManager(InventoryClickEvent e) { 
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}

		final Player p = (Player) e.getWhoClicked();

		int slot = e.getRawSlot();
		Inventory i = e.getInventory();

		if(!e.getInventory().getTitle().contains("@")){
			return;
		}

		final String owner_name = e.getInventory().getTitle().substring(e.getInventory().getTitle().lastIndexOf("@") + 1, e.getInventory().getTitle().length());
		int shop_slots = getShopSlots(getShopLevel(owner_name));

		if (slot >= shop_slots && (!(e.isShiftClick()) || (!shop_stock.containsKey(p.getName()) || !e.getInventory().getName().equalsIgnoreCase(shop_stock.get(p.getName()).getName())))){
			if(!shop_stock.containsKey(p.getName()) || !e.getInventory().getName().equalsIgnoreCase(shop_stock.get(p.getName()).getName())){
				e.setCancelled(true);
				p.updateInventory();
			}
			return;
		}


		if (slot == (shop_slots - 1) && e.getInventory().getTitle().contains("@")) { // Open / Close button.
			e.setCancelled(true);
			if(!(shop_stock.containsKey(p.getName())) || !e.getInventory().getName().equalsIgnoreCase(shop_stock.get(p.getName()).getName())){
				p.sendMessage(ChatColor.RED + "You can't do that.");
				return;
			}
			final Block p_shop = inverse_shop_owners.get(p.getName());
			if (i.getItem(slot) == null){
				i.setItem(slot, CraftItemStack.asCraftCopy(green_button));
				// Fix for corrupt shops.
			}
			if (i.getItem(slot).getDurability() == (short) 8) {
				// TODO: OPEN store.
				i.setItem(slot, green_button);

				List<ItemStack> to_remove = new ArrayList<ItemStack>();
				for(ItemStack is : i.getContents()){
					if(is == null){continue;}
					if(!(RealmMechanics.isItemTradeable(is)) || PetMechanics.isPermUntradeable(is) || CommunityMechanics.isSocialBook(is) || is.getType() == Material.NETHER_STAR || InstanceMechanics.isDungeonItem(is)){
						to_remove.add(is);
					}
				}
				if(to_remove.size() > 0){
					for(ItemStack is : to_remove){
						i.remove(is);
					}
				}

				open_shops.add(p_shop);
				open_shops.add(chest_partners.get(p_shop));
				//modifying_stock.remove(p.getName());
				setStoreColor(p_shop, ChatColor.GREEN);
				p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1.3F);
				return;
			}
			if (i.getItem(slot).getDurability() == (short) 10) {
				// TODO: CLOSE store.
				int button_slot = (i.getSize() - 1);
				i.setItem(button_slot, gray_button);
				open_shops.remove(p_shop);
				open_shops.remove(chest_partners.get(p_shop));
				//modifying_stock.add(p.getName());
				p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 0.70F);
				setStoreColor(p_shop, ChatColor.RED);

				List<Player> viewers = new ArrayList<Player>();

				if(i.getViewers() != null && i.getViewers().size() > 0){
					for(HumanEntity he : i.getViewers()){
						Player he_p = (Player) he;
						viewers.add(he_p);
					}
					for(Player he_p : viewers){
						if(!he_p.getName().equalsIgnoreCase(p.getName())){
							he_p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
							he_p.closeInventory();
							//he_p.sendMessage(ChatColor.RED + "Shop closed.");
							continue;
						}
					}
				}
				return;
			}
		}

		if (shop_stock.containsKey(p.getName()) && e.getInventory().getName().equalsIgnoreCase(shop_stock.get(p.getName()).getName()) && shop_stock.containsKey(p.getName()) && !(isShopOpen(inverse_shop_owners.get(p.getName())))){
			// STOCKING

			if(e.getCursor() != null && e.getCurrentItem() == null && e.getClick() == ClickType.RIGHT){
				// Prevent glitch, stocks item with no price.
				e.setCancelled(true);
				p.updateInventory();
				return;
			}

			if (e.getCurrentItem() != null
					&& e.getCurrentItem().getType() != Material.AIR
					&& e.getCursor() != null
					&& e.getCursor().getType() != Material.AIR) { 
				if (e.getCurrentItem().getType() != e.getCursor().getType()
						|| (hasCustomName(e.getCurrentItem()) || hasCustomName(e.getCursor()))) {
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "Remove current item first."); 
					return;
				}
			}

			if(e.getSlotType() != SlotType.OUTSIDE){
				if((e.isShiftClick() && (e.getCurrentItem().getType() == Material.PAPER || e.getCurrentItem().getType() == Material.EMERALD)) || e.getCursor().getType() == Material.EMERALD || e.getCursor().getType() == Material.PAPER){
					e.setCancelled(true);
					p.updateInventory();
					return;
				}
			}

			if (e.isRightClick()) {
				if(e.isShiftClick()){
					e.setCancelled(true);
					p.updateInventory();
					return;
				}
				if(e.getCursor() != null && e.getCursor().getType() != Material.AIR){
					e.setCancelled(true);
					p.updateInventory();
					return;
				}
				if (e.getCurrentItem() == null
						|| e.getCurrentItem().getType() == Material.AIR) {
					return;
				}

				current_item_being_stocked.put(p.getName(), e.getCurrentItem());
				price_update_needed.add(p);
				e.setCurrentItem(new ItemStack(Material.AIR));

				Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						//p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
						p.closeInventory();
						p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD
								+ "NEW GEM" + ChatColor.GREEN + " value of ["
								+ ChatColor.BOLD + "1x" + ChatColor.GREEN
								+ "] of this item.");
					}
				}, 2L);
				return;
			}


			if ((e.getCursor() != null && e.getCursor().getType() != Material.AIR) || e.isShiftClick()) {

				ItemStack in_stock = null;
				ItemStack i_added = null;

				if(e.isShiftClick()){
					i_added = e.getCurrentItem();
				}
				else if(!(e.isShiftClick())){
					i_added = e.getCursor();
				}

				int price = 0;

				if(i_added.getType() == Material.POTION){
					for(ItemStack is_pot : i.all(Material.POTION).values()){
						if(is_pot.getDurability() == i_added.getDurability()){ // The same type of potion is already in shop, we can copy the price!
							price = getPrice(is_pot);
							in_stock = is_pot;
							break;
						}
					}
				}

				if (price == 0 && (!i.contains(e.getCursor().getType()) || ((hasCustomName(e.getCursor())) && e.getCursor().getType() != Material.POTION)) && !(e.isShiftClick())) {

					if(!RealmMechanics.isItemTradeable(i_added) || InstanceMechanics.isDungeonItem(i_added)){
						//p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " perform this action with an " + ChatColor.ITALIC + "untradeable" + ChatColor.RED + " item.");
						e.setCancelled(true);
						p.updateInventory();
						return;
					}


					String item_type = "";
					if(!ItemMechanics.getDamageData(i_added).equalsIgnoreCase("no") || ProfessionMechanics.isSkillItem(i_added)){
						item_type = "wep";
					}
					if(ItemMechanics.isArmor(i_added)){
						item_type = "armor";
					}


					if(!item_type.equalsIgnoreCase("") && RepairMechanics.getPercentForDurabilityValue(i_added, item_type) < 50){
						p.sendMessage(ChatColor.YELLOW + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.YELLOW + " sell items with " + ChatColor.BOLD + "<50%" + ChatColor.YELLOW + " durability in your player-owned shop.");
						p.sendMessage(ChatColor.GRAY + "Either repair the item or use the player-to-player trade menu.");
						e.setCancelled(true);
						Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
							public void run() {
								p.closeInventory();
								p.updateInventory();
							}
						}, 2L);
						return;
					}

					// i.setItem(i.firstEmpty(), i_added);
					e.setCursor(new ItemStack(Material.AIR));
					e.setCurrentItem(new ItemStack(Material.AIR)); // Added to fix the weird dupe item.
					e.setCancelled(true);
					shop_stock.put(p.getName(), i);
					current_item_being_stocked.put(p.getName(), i_added);

					Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
						public void run() {
							p.closeInventory();
							p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD
									+ "GEM" + ChatColor.GREEN + " value of ["
									+ ChatColor.BOLD + "1x" + ChatColor.GREEN
									+ "] of this item.");
						}
					}, 2L);


				} else {

					if(price == 0){
						if (i.contains(e.getCursor().getType()) && e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
							try{
								in_stock = i.getItem(i.first(e.getCursor().getType()));
							} catch(ArrayIndexOutOfBoundsException aiobe){
								e.setCancelled(true);
								p.updateInventory();
								return;
							}
						} else {
							in_stock = e.getCurrentItem();
						}

						price = getPrice(in_stock);
					}

					ItemStack to_stock = null;

					if(!(e.isShiftClick())){

						to_stock = e.getCursor();

						if(!RealmMechanics.isItemTradeable(to_stock) || InstanceMechanics.isDungeonItem(to_stock)){
							//p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " perform this action with an " + ChatColor.ITALIC + "untradeable" + ChatColor.RED + " item.");
							e.setCancelled(true);
							p.updateInventory();
							return;
						}

						String item_type = "";
						if(!ItemMechanics.getDamageData(to_stock).equalsIgnoreCase("no") || ProfessionMechanics.isSkillItem(to_stock)){
							item_type = "wep";
						}
						if(ItemMechanics.isArmor(to_stock)){
							item_type = "armor";
						}


						if(!item_type.equalsIgnoreCase("") && RepairMechanics.getPercentForDurabilityValue(to_stock, item_type) < 50){
							p.sendMessage(ChatColor.YELLOW + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.YELLOW + " sell items with " + ChatColor.BOLD + "<50%" + ChatColor.YELLOW + " durability in your player-owned shop.");
							p.sendMessage(ChatColor.GRAY + "Either repair the item or use the player-to-player trade menu.");
							e.setCancelled(true);
							Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
								public void run() {
									p.closeInventory();
									p.updateInventory();
								}
							}, 2L);
							return;
						}


						to_stock = setPrice(e.getCursor(), price);
					}

					if (e.isShiftClick()) {
						if(e.isRightClick()){
							e.setCancelled(true);
							return;
						}
						if(i.firstEmpty() == -1){
							//p.sendMessage(ChatColor.RED + "No room.");
							e.setCancelled(true);
							return;
						}


						if(slot >= (i.getSize() - 1)){
							ItemStack cur_item = e.getCurrentItem();

							if(!RealmMechanics.isItemTradeable(cur_item) || CommunityMechanics.isSocialBook(cur_item) || cur_item.getType() == Material.NETHER_STAR || InstanceMechanics.isDungeonItem(cur_item)){
								//p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " perform this action with an " + ChatColor.ITALIC + "untradeable" + ChatColor.RED + " item.");
								e.setCancelled(true);
								p.updateInventory();
								return;
							}

							String item_type = "";
							if(!ItemMechanics.getDamageData(cur_item).equalsIgnoreCase("no") || ProfessionMechanics.isSkillItem(cur_item)){
								item_type = "wep";
							}
							if(ItemMechanics.isArmor(cur_item)){
								item_type = "armor";
							}


							if(!item_type.equalsIgnoreCase("") && RepairMechanics.getPercentForDurabilityValue(cur_item, item_type) < 50){
								p.sendMessage(ChatColor.YELLOW + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.YELLOW + " sell items with " + ChatColor.BOLD + "<50%" + ChatColor.YELLOW + " durability in your player-owned shop.");
								p.sendMessage(ChatColor.GRAY + "Either repair the item or use the player-to-player trade menu.");
								e.setCancelled(true);
								Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
									public void run() {
										p.closeInventory();
										p.updateInventory();
									}
								}, 2L);
								return;
							}


							e.setCurrentItem(new ItemStack(Material.AIR));
							e.setCancelled(true);
							to_stock = setPrice(cur_item, price);
							if(!i.contains(cur_item.getType()) || hasCustomName(cur_item)){
								Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
									public void run() {
										p.closeInventory();
										//p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
										p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD
												+ "GEM" + ChatColor.GREEN + " value of ["
												+ ChatColor.BOLD + "1x" + ChatColor.GREEN
												+ "] of this item.");
									}
								}, 2L);

								current_item_being_stocked.put(p.getName(), to_stock);
							}
							else{
								in_stock = i.getItem(i.first(cur_item.getType()));
								price = getPrice(in_stock);

								ItemStack i_format = setPrice(removePrice(to_stock), price);
								Inventory shop_i = shop_stock.get(p.getName());

								shop_i.setItem(shop_i.firstEmpty(), i_format);
							}

							//e.setCurrentItem(to_stock);
							//i.setItem(i.firstEmpty(), to_stock);
							shop_stock.put(p.getName(), i);
						}
						if(slot < (i.getSize() - 1)){
							e.setCurrentItem(removePrice(e.getCurrentItem()));
						}
						// in_stock = i.getItem(i.first(e.getCursor().getType()));
					}
					if(!e.isShiftClick()){
						e.setCancelled(true);
						if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
							// in_stock = i.getItem(i.first(e.getCursor().getType()));
							e.setCurrentItem(to_stock);
							e.setCursor(new ItemStack(Material.AIR));
						} else if (e.getCurrentItem().getType() == e.getCursor()
								.getType()) {
							if (in_stock.getAmount() + to_stock.getAmount() <= 64) {
								// e.getCurrentItem().setAmount(in_stock.getAmount() +
								// to_stock.getAmount());
								e.setCurrentItem(setPrice(
										new ItemStack(in_stock.getType(), in_stock
												.getAmount() + to_stock.getAmount(),
												in_stock.getDurability()), price));
								e.setCursor(new ItemStack(Material.AIR));
							} else {
								int room_left = (64 - in_stock.getAmount());
								if (room_left <= 0) {
									return;
								}
								e.setCurrentItem(setPrice(
										new ItemStack(in_stock.getType(), 64, in_stock
												.getDurability()), price));
								e.setCursor(new ItemStack(to_stock.getType(), to_stock
										.getAmount() - room_left, to_stock
										.getDurability()));
							}
							// else{
							// i.setItem(i.firstEmpty(), to_stock);
							// }
						}
					}
				}
				p.updateInventory();
				return;
			}

			if ((e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR)
					&& (e.getCursor() == null || e.getCursor().getType() == Material.AIR)
					&& (e.getRawSlot() != (i.getSize() - 1))) {
				// Withdraw the item and strip the price.
				ItemStack old_i = e.getCurrentItem();
				e.setCurrentItem(removePrice(old_i));
			}
		}


		else if(e.getInventory().getName().contains("@")){
			// CUSTOMER BUYING
			//String inv_name = e.getInventory().getName();
			Block shop_b1 = inverse_shop_owners.get(owner_name);

			if(e.getRawSlot() > (e.getInventory().getSize() - 1)){ // They're touching an item that's in their inventory, don't let em even move it TBH.
				e.setCancelled(true);
				return;
			}

			if(e.getSlotType() != SlotType.OUTSIDE){
				if((e.isShiftClick() && (e.getCurrentItem().getType() == Material.PAPER || e.getCurrentItem().getType() == Material.EMERALD)) || e.getCursor().getType() == Material.EMERALD || e.getCursor().getType() == Material.PAPER){
					e.setCancelled(true);
					p.updateInventory();
					return;
				}
			}

			if(owner_name.equalsIgnoreCase(p.getName())){
				e.setCancelled(true);
				e.setResult(Result.DENY);
				p.sendMessage(ChatColor.RED + "Please " + ChatColor.BOLD + "CLOSE" + ChatColor.RED + " your shop before modifying its stock.");
				p.updateInventory();
				return;
			}

			if(!(isShopOpen(shop_b1))){
				Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						p.closeInventory();
						p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
						p.sendMessage(ChatColor.RED + "This shop has closed.");
					}
				}, 2L);
			}

			if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
				e.setCancelled(true);
				p.updateInventory();
				return;
			}

			if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
				e.setCancelled(true);
				p.updateInventory();
				return;
			}

			e.setCancelled(true);

			final ItemStack being_bought = e.getCurrentItem();
			//Inventory inv = e.getInventory();
			int price = getPrice(being_bought);

			/*		if (!RealmMechanics.doTheyHaveEnoughMoney(p, price)) {
				p.sendMessage(ChatColor.RED + "You don't have enough GEM(s) for even 1x of this item.");
				e.setCancelled(true);
				return;
			} */

			if(p.getInventory().firstEmpty() == -1){ // Make sure they have enough room to fit the item so it doesn't overwrite anything.
				p.sendMessage(ChatColor.RED + "No room in inventory to purchase item.");
				Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						p.closeInventory();
					}
				}, 2L);
				return;
			}

			if(!e.isShiftClick()){
				current_item_being_bought.put(p.getName(), e.getRawSlot());
				shop_being_browsed.put(p.getName(), owner_name);
				int total_price = price * being_bought.getAmount();
				Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						p.closeInventory();
					}
				}, 2L);

				p.sendMessage(ChatColor.GREEN + "Enter the " + ChatColor.BOLD + "QUANTITY" + ChatColor.GREEN + " you'd like to purchase.");
				p.sendMessage(ChatColor.GRAY + "MAX: " + being_bought.getAmount() + "X (" + total_price + "g), OR " + price + "g/each.");
			}
			else if(e.isShiftClick()){
				// INSTA-BUY NIG.
				if(being_bought == null){return;}
				final int total_price = price * being_bought.getAmount();
				if (!RealmMechanics.doTheyHaveEnoughMoney(p, total_price)) {
					p.sendMessage(ChatColor.RED + "You don't have enough GEM(s) for " + being_bought.getAmount() + "x of this item.");
					p.sendMessage(ChatColor.RED + "COST: " + total_price);
					e.setCancelled(true);
					return;
				}

				if(Bukkit.getPlayer(owner_name) != null && Bukkit.getPlayer(owner_name).isOnline()){
					int old_net = MoneyMechanics.bank_map.get(owner_name);
					int new_net = old_net + total_price;
					MoneyMechanics.bank_map.put(owner_name, new_net);

					Player p_owner_name = Bukkit.getPlayer(owner_name);
					if(being_bought != null && hasCustomName(being_bought)){
						CraftItemStack css = (CraftItemStack) being_bought;
						String i_name = CraftItemStack.asNMSCopy(css).getTag().getCompound("display").getString("Name");
						p_owner_name.sendMessage(ChatColor.GREEN + "SOLD " + being_bought.getAmount() + "x '" + i_name + ChatColor.GREEN + "' for " + ChatColor.BOLD + total_price + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + p.getName());
					}
					else if(being_bought != null){
						p_owner_name.sendMessage(ChatColor.GREEN + "SOLD " + being_bought.getAmount() + "x '" + ChatColor.WHITE + being_bought.getType().toString().toLowerCase() + ChatColor.GREEN + "' for " + ChatColor.BOLD + total_price + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + p.getName());
					}
				}
				else if(Bukkit.getPlayer(owner_name) == null || !(Bukkit.getPlayer(owner_name).isOnline())){
					// They're not online locally.
					String i_name = being_bought.getType().toString().toLowerCase();
					if(being_bought != null && hasCustomName(being_bought)){
						CraftItemStack css = (CraftItemStack) being_bought;
						i_name = CraftItemStack.asNMSCopy(css).getTag().getCompound("display").getString("Name");
					}

					final String f_i_name = i_name;

					Main.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Main.plugin, new Runnable() {
						public void run() {
							boolean online_global = Hive.isPlayerOnline(owner_name);
							if(online_global == true){
								// They're online SOME server, so this is gonna be a pain in the ass.
								// We need to notify them of their sold item AND update their information. We'll need a socket!
								//int server_num = Hive.getPlayerServer(owner_name, false);
								//String server_ip = CommunityMechanics.server_list.get(server_num);
								//  // @money@vaquxine,50.1:availer#epic sword#
								List<Object> query = new ArrayList<Object>();
								query.add("@money@" + owner_name + "," + total_price + "." + being_bought.getAmount() + ":" + p.getName() + "#" + f_i_name + "#");
								query.add(owner_name);
								query.add(false);
								CommunityMechanics.social_query_list.put(owner_name, query);
								//ConnectProtocol.sendResultCrossServer(server_ip, "@money@" + owner_name + "," + total_price + "." + being_bought.getAmount() + ":" + p.getName() + "#" + f_i_name + "#");
							}
							else if(online_global == false){
								// They're not even online, just update the SQL directly.
								MoneyMechanics.addMoneyToOfflinePlayerBank(owner_name, total_price);
							}
						}
					}, 1L);
				}

				//i.setAmount(amount_to_buy);

				RealmMechanics.subtractMoney(p, total_price);
				p.getInventory().setItem(p.getInventory().firstEmpty(), removePrice(being_bought));
				e.setCurrentItem(new ItemStack(Material.AIR));

				p.sendMessage(ChatColor.GREEN + "Transaction successful.");
				p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
				//p.getWorld().spawnParticle(p.getLocation().add(0, 2.5, 0), Particle.HAPPY_VILLAGER, 0.5F, 20);
				p.updateInventory();

				AchievmentMechanics.addAchievment(owner_name, "A Merchant");

				int items_left = 0;
				for(ItemStack is : i.getContents()){
					if(is != null && is.getType() != Material.AIR && !(isOpenButton(is))){
						items_left++;
					}
				}

				if(items_left <= 0){
					// CLOSE shop, it's EMPTY.
					//int button_slot = (shop_inv.getSize() - 1);
					//shop_inv.setItem(button_slot, gray_button);
					final String shop_owner = owner_name;
					Inventory shop_inv = shop_stock.get(shop_owner);

					final Block chest = inverse_shop_owners.get(shop_owner);
					final Block other_chest = chest_partners.get(chest);

					open_shops.remove(chest);
					open_shops.remove(chest_partners.get(chest));

					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 0.70F);

					List<Player> viewers = new ArrayList<Player>();

					for(HumanEntity he : shop_inv.getViewers()){
						if(!(he instanceof Player)){
							continue;
						}
						Player he_p = (Player) he;
						viewers.add(he_p);
					}

					for(Player pl : viewers){
						if(!pl.getName().equalsIgnoreCase(p.getName())){
							pl.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
							pl.closeInventory();
							continue;
						}
					}

					if(shop_nameplates.containsKey(chest)){
						RemoteEntity re = shop_nameplates.get(chest);
						re.despawn(DespawnReason.CUSTOM);
						Hive.npc_manager.removeEntity(re.getID(), true);
						/*NPC n = shop_nameplates.get(chest);
						n.removeFromWorld();*/
					}

					if(Bukkit.getPlayer(shop_owner) != null){
						Player owner = Bukkit.getPlayer(shop_owner);
						price_update_needed.remove(owner);
					}

					shop_nameplates.remove(chest);
					shop_name_list.remove(ChatColor.stripColor(shop_names.get(chest).substring(shop_names.get(chest).indexOf(" ") + 1, shop_names.get(chest).length())));
					shop_names.remove(chest);
					shop_owners.remove(chest);
					inverse_shop_owners.remove(shop_owner);
					shop_stock.remove(shop_owner);
					current_item_being_stocked.remove(shop_owner);
					openning_shop.remove(shop_owner);
					open_shops.remove(chest);
					//modifying_stock.remove(shop_owner);

					chest_partners.remove(chest);
					chest_partners.remove(other_chest);
					shop_names.remove(other_chest);
					shop_nameplates.remove(other_chest);
					shop_owners.remove(other_chest);
					open_shops.remove(other_chest);


					Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
						public void run() {
							chest.setType(Material.AIR);
							other_chest.setType(Material.AIR);

							Packet b_particles = new PacketPlayOutWorldEvent(2001, (int)Math.round(chest.getLocation().getX()), (int)Math.round(chest.getLocation().getY()), (int)Math.round(chest.getLocation().getZ()), 54, false);
							((CraftServer) Main.plugin.getServer()).getServer().getPlayerList().sendPacketNearby(chest.getLocation().getX(), chest.getLocation().getY(), chest.getLocation().getZ(), 24, ((CraftWorld) chest.getWorld()).getHandle().dimension, b_particles);

							Packet other_chest_particles = new PacketPlayOutWorldEvent(2001, (int)Math.round(other_chest.getLocation().getX()), (int)Math.round(other_chest.getLocation().getY()), (int)Math.round(other_chest.getLocation().getZ()), 54, false);
							((CraftServer) Main.plugin.getServer()).getServer().getPlayerList().sendPacketNearby(other_chest.getLocation().getX(), other_chest.getLocation().getY(), other_chest.getLocation().getZ(), 24, ((CraftWorld) other_chest.getWorld()).getHandle().dimension, other_chest_particles);

							asyncSetShopServerSQL(shop_owner, -1);
						}
					}, 5L);


					if(!(need_sql_update.contains(p.getName()))){
						need_sql_update.add(p.getName()); // Update SQL after an item is sold from the shop.
					}
					return;
				}

				if(!(need_sql_update.contains(p.getName()))){
					need_sql_update.add(p.getName()); // Update SQL after an item is sold from the shop.
				}
			}

		}
	}

	@EventHandler
	public void onPlayerJoinPriceFix(PlayerJoinEvent e){
		Player p = e.getPlayer();
		Inventory inv = p.getInventory();
		if(inv.contains(Material.EMPTY_MAP)){
			for(Entry<Integer, ? extends ItemStack> data : inv.all(Material.EMPTY_MAP).entrySet()){
				try{
					ItemStack is = data.getValue();
					int slot = data.getKey();
					if((is.getItemMeta().getDisplayName().toLowerCase().contains("teleport"))){
						is.setType(Material.BOOK);
						inv.setItem(slot, is);
					}
				} catch(NullPointerException npe){
					continue;
				}
			}
		}
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamagEvent(EntityDamageEvent e) {
		if (e.getEntity() != null && e.getEntity() instanceof LivingEntity && npc_manager.isRemoteEntity((LivingEntity)e.getEntity())) {
			e.setCancelled(true);
			e.setDamage(0);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
		final Player p = e.getPlayer();

		if(openning_shop.contains(p.getName())){
			e.setCancelled(true);

			String msg = e.getMessage();
			if(msg.length() > 12){
				p.sendMessage(ChatColor.RED + "Shop name '" + ChatColor.BOLD + msg + ChatColor.RED + "' exceeds the MAX character limit of 12.");
				return;
			}

			msg = ChatMechanics.censorMessage(msg);

			if(msg.contains("@")){
				p.sendMessage(ChatColor.RED + "Invalid character '@' in name.");
				return;
			}
			msg = "[S] " + msg;
			if(shop_name_list.contains(msg)){
				// Already exists.
				p.sendMessage(ChatColor.RED + "A shop already exists on this server with the name '" + ChatColor.GRAY + msg + ChatColor.RED + "'.");
				p.sendMessage(ChatColor.GRAY + "Please choose another name.");
				return;
			}

			Block b = inverse_shop_owners.get(p.getName());

			RemotePlayer re = (RemotePlayer) shop_nameplates.get(b);
			try{
				re.setName(msg);
			} catch(Exception err){
				err.printStackTrace();
				npc_to_remove.add(re);
			}

			//Location l1 = b.getLocation();
			//Location l2 = chest_partners.get(b).getLocation();
			shop_name_list.add(msg);
			//assignShopNameplate(msg, l1, l2);
			setStoreColor(b, ChatColor.RED);

			Inventory new_shop_inv = Bukkit.createInventory(null, getShopSlots(getShopLevel(p.getName())), msg + " @" + p.getName());
			new_shop_inv.setItem((new_shop_inv.getSize() - 1), gray_button); // "OPEN" button.

			p.sendMessage(ChatColor.YELLOW + "Shop name assigned.");
			openning_shop.remove(p.getName());
			p.sendMessage("");
			p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "YOU'VE CREATED A SHOP!");
			p.sendMessage(ChatColor.YELLOW
					+ "To stock your shop, simply drag items into your shop's inventory.");


			if(hasCollectionBinItems(p.getName()) && DuelMechanics.isDamageDisabled(p.getLocation())){
				Inventory cb = collection_bin.get(p.getName());
				if(getPrice(cb.getItem(0)) != -1){
					for(ItemStack is : cb){
						if(is == null || is.getType() == Material.AIR){
							continue;
						}
						if(getPrice(is) == -1){
							continue; // They have no price data.
						}
						new_shop_inv.setItem(new_shop_inv.firstEmpty(), is);				  }
					cb.clear();
					collection_bin.remove(p.getName());
					p.sendMessage("");
					p.sendMessage(ChatColor.GREEN + "Previous shop stock " + ChatColor.BOLD + "LOADED.");
					p.sendMessage("");
				}
			}
			else if(hasCollectionBinItems(p.getName()) && !DuelMechanics.isDamageDisabled(p.getLocation())){
				Inventory cb = collection_bin.get(p.getName());
				List<ItemStack> new_contents = new ArrayList<ItemStack>();
				for(ItemStack is : cb.getContents()){
					if(is == null || is.getType() == Material.AIR){
						continue;
					}
					new_contents.add(ShopMechanics.removePrice(is));
				}

				cb.clear();

				for(ItemStack is : new_contents){
					cb.setItem(cb.firstEmpty(), is);
				}

				collection_bin.put(p.getName(), cb);
			}

			shop_stock.put(p.getName(), new_shop_inv);
			p.playSound(p.getLocation(), Sound.CHEST_OPEN, 1F, 1F);

			String motd = Bukkit.getMotd();
			int server_num = Integer.parseInt(motd.substring(motd.indexOf("-") + 1, motd.indexOf(" ")));
			if(motd.contains("EU-")){
				server_num += 1000;
			}
			if(motd.contains("BR-")){
				server_num += 2000;
			}
			if(motd.contains("US-YT")){
				server_num += 3000;
			}
			asyncSetShopServerSQL(p.getName(), server_num);
			p.openInventory(new_shop_inv);
		}

		if(shop_upgrade_codes.containsKey(p.getName())){
			String auth_code = getUpgradeAuthenticationCode(p);
			int new_tier = 0;
			if(auth_code == null){return;}
			e.setCancelled(true);

			if(e.getMessage().contains(auth_code)){
				new_tier = Integer.parseInt(auth_code.substring(0, 1));

				if(new_tier > 6){
					p.sendMessage(ChatColor.RED + "You cannot upgrade your shop; already at highest available tier.");
					shop_upgrade_codes.remove(p.getName());
					return;
				}

				int cost = getShopUpgradeCost(new_tier);
				if(!(RealmMechanics.doTheyHaveEnoughMoney(p, cost))){
					p.sendMessage(ChatColor.RED + "You do not have enough gems to purchase this upgrade. Upgrade cancelled.");
					p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "COST: " + ChatColor.RED + cost + ChatColor.BOLD + "G");
					shop_upgrade_codes.remove(p.getName());
					return;
				}

				RealmMechanics.subtractMoney(p, cost);
				upgradeShop(p, new_tier, true);
				shop_upgrade_codes.remove(p.getName());
				p.sendMessage("");
				p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "*** SHOP UPGRADE TO LEVEL " + new_tier + " COMPLETE ***");
				p.sendMessage(ChatColor.GRAY + "You now have " + getShopSlots(new_tier) + " shop slots available.");
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1F, 1.25F);
			}
			else{
				p.sendMessage(ChatColor.RED + "Invalid authentication code entered. Bank upgrade cancelled.");
				shop_upgrade_codes.remove(p.getName());
			}

		}

		if (current_item_being_bought.containsKey(p.getName())){
			e.setCancelled(true);

			if (e.getMessage().equalsIgnoreCase("cancel")) {
				p.sendMessage(ChatColor.RED + "Purchase of item " + ChatColor.BOLD + "CANCELLED");
				current_item_being_bought.remove(p.getName());
				shop_being_browsed.remove(p.getName());
				p.updateInventory();
				return;
			}

			if(p.getInventory().firstEmpty() == -1){
				p.sendMessage(ChatColor.RED + "No space available in inventory. Type 'cancel' or clear some room.");
				return;
			}

			final String shop_owner = shop_being_browsed.get(p.getName());

			if(!(isShopOpen(inverse_shop_owners.get(shop_owner)))){
				p.sendMessage(ChatColor.RED + "The shop is no longer available.");
				current_item_being_bought.remove(p.getName());
				shop_being_browsed.remove(p.getName());
				return;
			}

			Inventory shop_inv = shop_stock.get(shop_owner);
			Block b = inverse_shop_owners.get(shop_owner);
			int slot = current_item_being_bought.get(p.getName());
			if(shop_inv.getItem(slot) == null){
				p.sendMessage(ChatColor.RED + "Attempted to purchase null item, aborted.");
				return;
			}
			ItemStack i = shop_inv.getItem(slot);

			if(p.getLocation().distanceSquared(b.getLocation()) > 16){
				p.sendMessage(ChatColor.RED + "You are too far away from the shop [>4 blocks], purchase of item CANCELLED.");
				current_item_being_bought.remove(p.getName());
				shop_being_browsed.remove(p.getName());
				return;
			}

			if(i == null || i.getType() == Material.AIR){
				e.setCancelled(true);
				p.sendMessage(ChatColor.RED + "This item is no longer available.");
				current_item_being_bought.remove(p.getName());
				shop_being_browsed.remove(p.getName());
				return;
			}

			if(p.getInventory().firstEmpty() == -1){ // Make sure they have enough room to fit the item so it doesn't overwrite anything.
				p.sendMessage(ChatColor.RED + "No room in inventory to purchase item.");
				current_item_being_bought.remove(p.getName());
				shop_being_browsed.remove(p.getName());
				Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						p.closeInventory();
					}
				}, 2L);
				return;
			}

			int price_per = getPrice(i);
			int amount_to_buy = 0; 
			int i_amount = i.getAmount();


			try {
				amount_to_buy = Integer.parseInt(e.getMessage());
			} catch (NumberFormatException ex) {
				p.sendMessage(ChatColor.RED
						+ "Please enter a valid integer, or type 'cancel' to void this item purchase.");
				return;
			}

			if(amount_to_buy <= 0){
				p.sendMessage(ChatColor.RED + "You cannot purchase a NON-POSITIVE number.");
				return;
			}	

			final int total_price = amount_to_buy * price_per;

			if(amount_to_buy > i_amount){
				p.sendMessage(ChatColor.RED + "There are only [" + ChatColor.BOLD + i_amount + ChatColor.RED + "] available.");
				return;
			}

			if(total_price > 0 && !RealmMechanics.doTheyHaveEnoughMoney(p, total_price)){
				p.sendMessage(ChatColor.RED + "You do not have enough GEM(s) to complete this purchase.");
				p.sendMessage(ChatColor.GRAY + "" + amount_to_buy + " X " + price_per + " gem(s)/ea = " + total_price + " gem(s).");
				return;	
			}

			RealmMechanics.subtractMoney(p, total_price);

			int remainder = i_amount - amount_to_buy;
			if(remainder > 0){
				shop_inv.getItem(slot).setAmount(remainder);
			}
			if(remainder <= 0){
				shop_inv.setItem(slot, new ItemStack(Material.AIR));
			}

			if(Bukkit.getPlayer(shop_owner) != null && Bukkit.getPlayer(shop_owner).isOnline()){
				int old_net = MoneyMechanics.bank_map.get(shop_owner);
				int new_net = old_net + total_price;
				MoneyMechanics.bank_map.put(shop_owner, new_net);

				Player p_shop_owner = Bukkit.getPlayer(shop_owner);
				if(i != null && hasCustomName(i)){
					CraftItemStack css = (CraftItemStack) i;
					String i_name = CraftItemStack.asNMSCopy(css).getTag().getCompound("display").getString("Name");
					p_shop_owner.sendMessage(ChatColor.GREEN + "SOLD " + amount_to_buy + "x '" + i_name + ChatColor.GREEN + "' for " + ChatColor.BOLD + total_price + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + p.getName());
				}
				else if(i != null){
					p_shop_owner.sendMessage(ChatColor.GREEN + "SOLD " + amount_to_buy + "x '" + ChatColor.WHITE + i.getType().toString().toLowerCase() + ChatColor.GREEN + "' for " + ChatColor.BOLD + total_price + "g" + ChatColor.GREEN + " to " + ChatColor.WHITE + "" + ChatColor.BOLD + p.getName());
				}
			}
			else if(Bukkit.getPlayer(shop_owner) == null || !(Bukkit.getPlayer(shop_owner).isOnline())){
				// They're not online locally.
				final ItemStack f_i = i;
				final int f_amount_to_buy = amount_to_buy;

				Main.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						String i_name = f_i.getType().toString().toLowerCase();
						if(f_i != null && hasCustomName(f_i)){
							CraftItemStack css = (CraftItemStack) f_i;
							i_name = CraftItemStack.asNMSCopy(css).getTag().getCompound("display").getString("Name");
						}

						boolean online_global = Hive.isPlayerOnline(shop_owner);
						if(online_global){
							// They're online SOME server, so this is gonna be a pain in the ass.
							// We need to notify them of their sold item AND update their information. We'll need a socket!
							// TODO: Socket
							//int server_num = Hive.getPlayerServer(shop_owner, false);
							//String server_ip = CommunityMechanics.server_list.get(server_num);

							List<Object> query = new ArrayList<Object>();
							query.add("@money@" + shop_owner + "," + total_price + "." + f_amount_to_buy + ":" + p.getName() + "#" + i_name + "#");
							query.add(shop_owner);
							query.add(false);
							CommunityMechanics.social_query_list.put(shop_owner, query);
						}
						else if(!online_global){
							// They're not even online, just update the SQL directly.
							MoneyMechanics.addMoneyToOfflinePlayerBank(shop_owner, total_price);
						}
					}
				}, 1L);
			}

			if(i != null){
				ItemStack bought_stack = CraftItemStack.asCraftCopy(i);
				bought_stack.setAmount(amount_to_buy);
				p.getInventory().setItem(p.getInventory().firstEmpty(), removePrice(bought_stack));
			}

			current_item_being_bought.remove(p.getName());
			shop_being_browsed.remove(p.getName());

			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-" + ChatColor.RED + total_price + ChatColor.BOLD + "G");
			p.sendMessage(ChatColor.GREEN + "Transaction successful.");
			p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
			//p.getWorld().spawnParticle(p.getLocation().add(0, 2.5, 0), Particle.HAPPY_VILLAGER, 0.5F, 20);
			p.updateInventory();

			int items_left = 0;
			for(ItemStack is : shop_inv.getContents()){
				if(is != null && is.getType() != Material.AIR && !(isOpenButton(is))){
					items_left++;
				}
			}

			if(items_left <= 0){
				// CLOSE shop, it's EMPTY.
				final Block chest = inverse_shop_owners.get(shop_owner);
				final Block other_chest = chest_partners.get(chest);
				open_shops.remove(chest);
				open_shops.remove(chest_partners.get(chest));

				p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 0.70F);

				List<Player> viewers = new ArrayList<Player>();

				for(HumanEntity he : shop_inv.getViewers()){
					viewers.add((Player)he);
				}

				for(Player he_p : viewers){
					if(!he_p.getName().equalsIgnoreCase(p.getName())){
						he_p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
						he_p.closeInventory();
						continue;
					}
				}

				if(shop_nameplates.containsKey(chest)){
					RemoteEntity re = shop_nameplates.get(chest);
					re.despawn(DespawnReason.CUSTOM);
					Hive.npc_manager.removeEntity(re.getID(), true);
					/*NPC n = shop_nameplates.get(chest);
					n.removeFromWorld();*/
				}

				if(Bukkit.getPlayer(shop_owner) != null){
					Player owner = Bukkit.getPlayer(shop_owner);
					price_update_needed.remove(owner);
				}

				shop_nameplates.remove(chest);
				shop_name_list.remove(ChatColor.stripColor(shop_names.get(chest).substring(shop_names.get(chest).indexOf(" ") + 1, shop_names.get(chest).length())));
				shop_names.remove(chest);
				shop_owners.remove(chest);
				inverse_shop_owners.remove(shop_owner);
				shop_stock.remove(shop_owner);
				current_item_being_stocked.remove(shop_owner);
				openning_shop.remove(shop_owner);
				open_shops.remove(chest);
				//modifying_stock.remove(shop_owner);

				chest_partners.remove(chest);
				chest_partners.remove(other_chest);
				shop_names.remove(other_chest);
				shop_nameplates.remove(other_chest);
				shop_owners.remove(other_chest);
				open_shops.remove(other_chest);


				Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
					public void run() {
						chest.setType(Material.AIR);
						other_chest.setType(Material.AIR);

						Packet b_particles = new PacketPlayOutWorldEvent(2001, (int)Math.round(chest.getLocation().getX()), (int)Math.round(chest.getLocation().getY()), (int)Math.round(chest.getLocation().getZ()), 54, false);
						((CraftServer) Main.plugin.getServer()).getServer().getPlayerList().sendPacketNearby(chest.getLocation().getX(), chest.getLocation().getY(), chest.getLocation().getZ(), 24, ((CraftWorld) chest.getWorld()).getHandle().dimension, b_particles);

						Packet other_chest_particles = new PacketPlayOutWorldEvent(2001, (int)Math.round(other_chest.getLocation().getX()), (int)Math.round(other_chest.getLocation().getY()), (int)Math.round(other_chest.getLocation().getZ()), 54, false);
						((CraftServer) Main.plugin.getServer()).getServer().getPlayerList().sendPacketNearby(other_chest.getLocation().getX(), other_chest.getLocation().getY(), other_chest.getLocation().getZ(), 24, ((CraftWorld) other_chest.getWorld()).getHandle().dimension, other_chest_particles);

						asyncSetShopServerSQL(shop_owner, -1);
					}
				}, 5L);

				if(!(need_sql_update.contains(p.getName()))){
					need_sql_update.add(p.getName()); // Update SQL after a new item is added to stock.
				}				
				return;
			}

			if(!(need_sql_update.contains(p.getName()))){
				need_sql_update.add(p.getName()); // Update SQL after a new item is added to stock.
			}
		}

		if (current_item_being_stocked.containsKey(p.getName())) {
			e.setCancelled(true);
			ItemStack i = current_item_being_stocked.get(p.getName());
			int price_per = 0;
			if (e.getMessage().equalsIgnoreCase("cancel")) {
				p.sendMessage(ChatColor.RED + "Pricing of item - " + ChatColor.BOLD + "CANCELLED");
				p.getInventory().setItem(p.getInventory().firstEmpty(), removePrice(current_item_being_stocked.get(p.getName())));
				current_item_being_stocked.remove(p.getName());
				p.updateInventory();
				return;
			}
			try {
				price_per = Integer.parseInt(e.getMessage());
			} catch (NumberFormatException ex) {
				p.sendMessage(ChatColor.RED
						+ "Please enter a valid integer, or type 'cancel' to void this price set request.");
				return;
			}

			if(price_per < 0){
				p.sendMessage(ChatColor.RED + "You cannot purchase a NON-POSITIVE number.");
				return;
			}	

			ItemStack i_format = setPrice(removePrice(i), price_per);
			Inventory shop_i = shop_stock.get(p.getName());
			current_item_being_stocked.remove(p.getName());

			shop_i.setItem(shop_i.firstEmpty(), i_format);
			if (price_update_needed.contains(p)) {
				// Update all of the same items in shop.
				HashMap<Integer, ? extends ItemStack> invItems = shop_i
						.all(i_format.getType());
				for (Map.Entry<Integer, ? extends ItemStack> entry : invItems
						.entrySet()) {
					ItemStack is = entry.getValue();
					int s = entry.getKey();
					if (hasCustomName(is)) {
						continue;
					}
					shop_i.setItem(s, setPrice(removePrice(is), price_per));
					//log.info(String.valueOf(s));
				}
				p.sendMessage(ChatColor.YELLOW + "All prices updated.");
			} else {
				p.sendMessage(ChatColor.YELLOW + "Price set. Right-Click item to edit."); 
			}
			price_update_needed.remove(p);
			p.openInventory(shop_i);
			p.playSound(p.getLocation(), Sound.CLICK, 1F, 1.25F);
			if(!(need_sql_update.contains(p.getName()))){
				need_sql_update.add(p.getName()); // Update SQL after a new item is added to stock.
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (!(shop_stock.containsKey(p.getName()))) {
			return;
		}
		if (e.getInventory().getName().equalsIgnoreCase(shop_stock.get(p.getName()).getName())) {
			shop_stock.put(p.getName(), e.getInventory());
			if(e.getInventory().firstEmpty() == -1 && e.getInventory().getSize() <= 54){
				p.sendMessage(ChatColor.GRAY + "Merchant: " + ChatColor.WHITE + "To purchase more shop slots, " + ChatColor.GREEN + ChatColor.BOLD + "SNEAK + RIGHT-CLICK" + ChatColor.WHITE + " your shop chest.");
			}
			//log.info("[ShopMechanics] Updated saved shop stock for "+ p.getName());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onShopInteraction(InventoryClickEvent e){
		Player p = (Player) e.getWhoClicked();
		if (!(shop_stock.containsKey(p.getName()))) {
			return;
		}
		if (e.getInventory().getName().equalsIgnoreCase(shop_stock.get(p.getName()).getName())) {
			shop_stock.put(p.getName(), e.getInventory());
			if(!(need_sql_update.contains(p.getName()))){
				need_sql_update.add(p.getName()); // Update SQL after owner closes the shop.
			}
			//log.info("[ShopMechanics] Updated saved shop stock for "+ p.getName());
		}
	}

	@EventHandler
	public void ShopCloseEvent(InventoryCloseEvent e){
		final Player p = (Player)e.getPlayer();
		if(e.getInventory().getName().contains("@")){
			p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
			Main.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					p.updateInventory();
				}
			}, 5L);

			if(e.getInventory().getName().contains(p.getName())){
				if(!(need_sql_update.contains(p.getName()))){
					need_sql_update.add(p.getName()); // Update SQL after owner closes the shop.
				}
			}
		}

		if(e.getInventory().getName().equalsIgnoreCase("Collection Bin")){
			if(!(need_sql_update.contains(p.getName()))){
				need_sql_update.add(p.getName()); // Update SQL after owner closes the shop.
			}
		}
	}

	@EventHandler
	public void onPlayerBreakChest(PlayerInteractEvent e){
		final Player p = e.getPlayer();

		if(e.getAction() == Action.LEFT_CLICK_BLOCK && e.hasBlock() && e.getClickedBlock().getType() == Material.CHEST){
			final Block b = e.getClickedBlock();
			final Block other_chest = chest_partners.get(b);

			if(!(isShop(b))){return;}
			if (!(isShopOwner(p, b))){
				e.setCancelled(true);
				return;
			}

			if(isShopOpen(b)){
				p.sendMessage(ChatColor.RED + "Please " + ChatColor.BOLD + "CLOSE" + ChatColor.RED + " your shop before destroying it.");
				e.setCancelled(true);
				return;
			}
			try{
				if(shop_stock.containsKey(p.getName())){
					Inventory i = shop_stock.get(p.getName());
					List<ItemStack> li = new ArrayList<ItemStack>();

					for(ItemStack is : i.getContents()){
						if(is != null && is.getType() != Material.AIR 
								&& !(is.getType() == Material.INK_SACK && (is.getDurability() == (short)8 || is.getDurability() == (short)10))){
							//li.add(removePrice(is));
							li.add(is);
						}
					}

					if(li.size() > 0){
						Inventory cb = Bukkit.createInventory(null, 54, "Collection Bin");
						for(ItemStack is : li){
							cb.setItem(cb.firstEmpty(), is);
						}
						collection_bin.put(p.getName(), cb);
					}
				}

				if(current_item_being_stocked.containsKey(p.getName())){
					if(p.getInventory().firstEmpty() != -1){
						p.getInventory().setItem(p.getInventory().firstEmpty(), removePrice(current_item_being_stocked.get(p.getName())));
					}
					else{
						p.getWorld().dropItem(p.getLocation(), removePrice(current_item_being_stocked.get(p.getName())));
					}
				}

				if(shop_nameplates.containsKey(b)){
					RemotePlayer re = (RemotePlayer) shop_nameplates.get(b);
					re.despawn(DespawnReason.CUSTOM);
					Hive.npc_manager.removeEntity(re.getID(), true);
					/*NPC n = shop_nameplates.get(chest);
					n.removeFromWorld();*/
				}

				shop_nameplates.remove(b);
				shop_name_list.remove(ChatColor.stripColor(shop_names.get(b).substring(shop_names.get(b).indexOf(" ") + 1, shop_names.get(b).length())));
				shop_names.remove(b);
				shop_owners.remove(b);
				inverse_shop_owners.remove(p.getName());
				shop_stock.remove(p.getName());
				current_item_being_stocked.remove(p.getName());
				price_update_needed.remove(p);
				openning_shop.remove(p.getName());
				open_shops.remove(b);
				//modifying_stock.remove(p.getName());

				chest_partners.remove(b);
				chest_partners.remove(other_chest);
				shop_names.remove(other_chest);
				shop_nameplates.remove(other_chest);
				shop_owners.remove(other_chest);
				open_shops.remove(other_chest);
			} catch(Exception ex){
				shop_nameplates.remove(b);
				shop_names.remove(b);
				shop_owners.remove(b);
				inverse_shop_owners.remove(p.getName());
				shop_stock.remove(p.getName());
				current_item_being_stocked.remove(p.getName());
				price_update_needed.remove(p);
				openning_shop.remove(p.getName());
				open_shops.remove(b);
				//modifying_stock.remove(p.getName());

				chest_partners.remove(b);
				chest_partners.remove(other_chest);
				shop_names.remove(other_chest);
				shop_nameplates.remove(other_chest);
				shop_owners.remove(other_chest);
				open_shops.remove(other_chest);
				ex.printStackTrace();
			}
			e.setCancelled(true);

			Main.plugin.getServer().getScheduler()
			.scheduleSyncDelayedTask(Main.plugin, new Runnable() {
				public void run() {
					p.playSound(p.getLocation(), Sound.ITEM_PICKUP, 1F, 0.75F);
					b.setType(Material.AIR);
					other_chest.setType(Material.AIR);

					Packet b_particles = new PacketPlayOutWorldEvent(2001, (int)Math.round(b.getLocation().getX()), (int)Math.round(b.getLocation().getY()), (int)Math.round(b.getLocation().getZ()), 54, false);
					((CraftServer) Main.plugin.getServer()).getServer().getPlayerList().sendPacketNearby(b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 24, ((CraftWorld) b.getWorld()).getHandle().dimension, b_particles);

					Packet other_chest_particles = new PacketPlayOutWorldEvent(2001, (int)Math.round(other_chest.getLocation().getX()), (int)Math.round(other_chest.getLocation().getY()), (int)Math.round(other_chest.getLocation().getZ()), 54, false);
					((CraftServer) Main.plugin.getServer()).getServer().getPlayerList().sendPacketNearby(other_chest.getLocation().getX(), other_chest.getLocation().getY(), other_chest.getLocation().getZ(), 24, ((CraftWorld) other_chest.getWorld()).getHandle().dimension, other_chest_particles);

					// TODO: At this point, they are breaking their shop block. Their items
					// need to be handled, database updated, etc, etc.
					//String motd = Bukkit.getMotd();
					//int server_num = Integer.parseInt(motd.substring(motd.indexOf("US-") + 3, motd.indexOf(" ")));
					asyncSetShopServerSQL(p.getName(), -1);
					p.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.BOLD + "CLOSED" + ChatColor.YELLOW + " your shop.");

					if(collection_bin.containsKey(p.getName())){
						p.sendMessage(ChatColor.YELLOW + "Your shop's contents have been moved to your bank chest.");
					}

					if(!(need_sql_update.contains(p.getName()))){
						need_sql_update.add(p.getName()); // Update SQL after an item is sold from the shop.
					}

				}
			}, 5L);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() != Material.CHEST) {
			return;
		}
		final Player p = e.getPlayer();
		final Block b = e.getBlock();

		if (!(isShop(b))){
			return;
		}

		if (!(isShopOwner(p, e.getBlock()))){
			e.setCancelled(true);
			return;
		}

		if(isShopOpen(e.getBlock())){
			p.sendMessage(ChatColor.RED + "Please " + ChatColor.BOLD + "CLOSE" + ChatColor.RED + " your shop before destroying it.");
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerOpeningChest(PlayerInteractEvent e) {
		if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block b = e.getClickedBlock();
		Player p = e.getPlayer();
		if(openning_shop.contains(p.getName())){
			p.sendMessage(ChatColor.RED + "You must NAME your shop first. Simply enter any name you desire into chat.");
			e.setCancelled(true);
			return;
		}
		if (!(isShop(b)) && !(isShopOwner(p, b))) {
			return;
		}
		    if(!isShopOpen(b)  && !(isShopOwner(p, b))){
		   
			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + "This shop is currently closed.");
			return;
		    }

		e.setCancelled(true);
		Player owner = getShopOwner(b);
		if (getShopStock(b) == null) {
			p.sendMessage(ChatColor.RED + owner.getName()
					+ "'s Shop is not currently available.");
			return;
		}

		if (current_item_being_stocked.containsKey(p.getName())) {
			p.sendMessage(ChatColor.RED
					+ "Please finish pricing your current item before accessing your shop. Type 'cancel' to stop pricing.");
			e.setCancelled(true);
			//p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
			p.closeInventory();
			e.setUseInteractedBlock(Result.DENY);
			return;
		}

		if(current_item_being_bought.containsKey(p.getName())){
			p.sendMessage(ChatColor.RED
					+ "Please finish your pending purchase before accessing this shop. Type 'cancel' to void your purchase.");
			e.setCancelled(true);
			//p.playSound(p.getLocation(), Sound.CHEST_CLOSE, 1F, 1F);
			p.closeInventory();
			e.setUseInteractedBlock(Result.DENY);
			return;
		}

		if(TradeMechanics.trade_map.containsKey(p)){
			e.setCancelled(true);
			return;
		}

		if(DuelMechanics.duel_request.containsKey(p.getName())){
			e.setCancelled(true);
			p.sendMessage(ChatColor.RED + "You cannot access shops with a pending duel request.");
			return;
		}

		if (isShopOwner(p, b) && p.isSneaking()){
			if(isShopOpen(b)){
				p.sendMessage(ChatColor.RED + "Please " + ChatColor.BOLD + "CLOSE" + ChatColor.RED + " your shop before attempting to upgrade it.");
				return;
			}
			int shop_tier = getShopLevel(p.getName());
			int next_shop_tier = shop_tier + 1;
			if(next_shop_tier >= 6){
				p.sendMessage(ChatColor.RED + "Your shop is already at it's maximum size. (54 slots)");
				return;
			}
			int upgrade_cost = getShopUpgradeCost(next_shop_tier);
			generateUpgradeAuthenticationCode(p, String.valueOf(next_shop_tier));

			p.sendMessage("");
			p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Shop Upgrade Confirmation" + ChatColor.DARK_GRAY + " ***");
			p.sendMessage(ChatColor.DARK_GRAY + "           CURRENT Slots: " + ChatColor.GREEN + getShopSlots(shop_tier) + ChatColor.DARK_GRAY + "          NEW Slots: " + ChatColor.GREEN + getShopSlots(next_shop_tier));
			//p.sendMessage(ChatColor.DARK_GRAY + "FROM Tier " + ChatColor.GREEN + shop_tier + ChatColor.DARK_GRAY + " TO " + ChatColor.GREEN + next_shop_tier);
			p.sendMessage(ChatColor.DARK_GRAY + "                  Upgrade Cost: " + ChatColor.GREEN + "" + upgrade_cost + " Gem(s)");
			p.sendMessage("");
			p.sendMessage(ChatColor.GREEN + "Enter the code '" + ChatColor.BOLD + getUpgradeAuthenticationCode(p) + ChatColor.GREEN + "' to confirm your upgrade.");
			p.sendMessage("");
			p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Player owned shop upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
			p.sendMessage("");
			return;
		}

		Inventory shop_i = getShopStock(b);

		p.openInventory(shop_i);
		p.playSound(p.getLocation(), Sound.CHEST_OPEN, 1F, 1F);


		if(!(last_shop_open.containsKey(p.getName())) || System.currentTimeMillis() - last_shop_open.get(p.getName()) >= 1000){
			if(owner != null && inverse_shop_owners.containsKey(owner.getName())){
				if(p.getName().equalsIgnoreCase(owner.getName())){
					return; // Owner cannot up own views.
				}
				Block shop_block = inverse_shop_owners.get(owner.getName());

				//NPC nshop_tag = shop_nameplates.get(shop_block);
				RemoteEntity nshop_tag = shop_nameplates.get(shop_block);
				Player pshop_tag = (Player)nshop_tag.getBukkitEntity();
				incrementViewCount(pshop_tag);

				last_shop_open.put(p.getName(), System.currentTimeMillis());
			}
		}
		// This will hijack the inventory open event on a chest and instead show
		// them the shop inventory with all its modifications.
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		final Player p = e.getPlayer();

		if(current_item_being_stocked.containsKey(p.getName())){
			ItemStack i = current_item_being_stocked.get(p.getName());
			p.getInventory().setItem(p.getInventory().firstEmpty(), i);
			current_item_being_stocked.remove(p.getName());
		}

		if(openning_shop.contains(p.getName())){
			final Block b1 = inverse_shop_owners.get(p.getName());
			final Block b2 = chest_partners.get(b1);

			final String shop_owner_n = p.getName();
			b1.setType(Material.AIR);
			b2.setType(Material.AIR);

			/*NPC n = shop_nameplates.get(b1);
			n.removeFromWorld();*/
			RemoteEntity re = shop_nameplates.get(b1);
			re.despawn(DespawnReason.CUSTOM);
			Hive.npc_manager.removeEntity(re.getID(), true);
			
			shop_nameplates.remove(b1);
			shop_name_list.remove(ChatColor.stripColor(shop_names.get(b1).substring(shop_names.get(b1).indexOf(" ") + 1, shop_names.get(b1).length())));
			shop_names.remove(b1);
			shop_owners.remove(b1);
			inverse_shop_owners.remove(shop_owner_n);
			shop_stock.remove(shop_owner_n);
			current_item_being_stocked.remove(shop_owner_n);

			openning_shop.remove(shop_owner_n);
			open_shops.remove(b1);
			//modifying_stock.remove(shop_owner_n);

			Block other_chest = b2;

			chest_partners.remove(b1);
			chest_partners.remove(other_chest);

			shop_nameplates.remove(other_chest);
			shop_names.remove(other_chest);
			shop_owners.remove(other_chest);
			open_shops.remove(other_chest);

			asyncSetShopServerSQL(shop_owner_n, -1);
		}

		shop_being_browsed.remove(p.getName());
		current_item_being_bought.remove(p.getName());	 

	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent e){
		if(e.getTarget() instanceof Player && ((Player)e.getTarget()).getGameMode() == GameMode.CREATIVE){
			e.setCancelled(true);
		}
	}

	public static void assignShopNameplate(String p_name, Location chest_loc1, Location chest_loc2) {
		String name = "[S] " + p_name;

		if (name.length() > 16) {
			name = "[S] " + p_name.substring(0, 12);
		}
		// TODO See if we can say "fuck you" to the 16 char limit.

		Block chest1 = chest_loc1.getBlock();
		Block chest2 = chest_loc2.getBlock();
		Location loc;
		if(chest1.getLocation().getX() > chest2.getLocation().getX()){
			loc = chest_loc1.subtract(0.0, 1.1, -0.5);
		}
		else{
			loc = chest_loc1.subtract(-1.0, 1.1, -0.5);
		}
		
		RemotePlayer re = (RemotePlayer) ((de.kumpelblase2.remoteentities.EntityManager) npc_manager).createNamedEntity(RemoteEntityType.Human, loc, name);
		//NPC n = m.spawnHumanNPC(name, loc);
		re.setPushable(false);
		re.setStationary(true, true);
		re.getMind().clearMovementDesires();
		re.getMind().clearBehaviours();
		CraftPlayer p = (CraftPlayer) re.getBukkitEntity();
		p.setGameMode(GameMode.CREATIVE);
		p.setPlayerListName("");
		shop_nameplates.put(chest1, re);
		shop_nameplates.put(chest2, re);
		shop_names.put(chest1, name);
		shop_names.put(chest2, name);
		// p.teleport(loc);
	}
}