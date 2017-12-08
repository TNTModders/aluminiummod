package com.tntmodders.tutorial;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Mod(modid = "aluminiummod", version = "1.0", name = "AluminiumMod",
        updateJSON = "https://raw.githubusercontent.com/TNTModders/aluminiummod/master/version/aluminiumVersionCheck" +
                ".json")
public class AluminiumMod {
    public static final Item ALUMINIUM = new ItemAluminium();
    public static final Block ALUMINIUM_BLOCK = new BlockAluminium();
    private static final AluminiumRecipeHolder HOLDER = new AluminiumRecipeHolder();
    
    @Mod.Instance("aluminiummod")
    public static AluminiumMod aluminiumInstance;
    
    @Mod.EventHandler
    //この関数でMODファイル自体をイベントの発火先にする。
    public void construct(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    //アイテムを登録するイベント。 旧preinitのタイミングで発火する。
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(ALUMINIUM);
        event.getRegistry().register(new ItemBlock(ALUMINIUM_BLOCK).setRegistryName("aluminiummod", "aluminium_block"));
    }
    
    //ブロックを登録するイベント。 旧preinitのタイミングで発火する。
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ALUMINIUM_BLOCK);
    }
    
    //モデルを登録するイベント。SideOnlyによってクライアント側のみ呼ばれる。旧preinitのタイミングで発火する。
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(ALUMINIUM, 0, new ModelResourceLocation(new ResourceLocation
                ("aluminiummod", "aluminium"), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ALUMINIUM_BLOCK), 0, new
                ModelResourceLocation(new ResourceLocation("aluminiummod", "aluminium_block"), "inventory"));
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        HOLDER.register();
    }
    
    //アイテムを拾ったときのイベント。
    @SubscribeEvent
    public void onPickupItem(EntityItemPickupEvent event) {
        this.aluminiumUnlockRecipes(event.getItem().getItem(), event.getEntityPlayer());
    }
    
    private void aluminiumUnlockRecipes(ItemStack stack, EntityPlayer player) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            Item item = stack.getItem();
            int meta = stack.getMetadata();
            ItemStack itemStack = new ItemStack(item, 1, meta);
            //もしレシピを保持するリストに合致すれば
            if (!AluminiumRecipeHolder.map.isEmpty() && AluminiumRecipeHolder.map.containsKey(itemStack)) {
                List<ResourceLocation> list = AluminiumRecipeHolder.map.get(itemStack);
                //player.unlockRecipes(ResourceLocation[] locations)でレシピブックに追加する。
                player.unlockRecipes(list.toArray(new ResourceLocation[list.size()]));
            }
        }
    }
    
    //コンテナを閉じたとき(チェストやプレイヤーインベントリなど)のイベント。
    @SubscribeEvent
    public void onCloseContainer(PlayerContainerEvent.Close event) {
        for (ItemStack itemStack : event.getEntityPlayer().inventoryContainer.getInventory()) {
            this.aluminiumUnlockRecipes(itemStack, event.getEntityPlayer());
        }
    }
}