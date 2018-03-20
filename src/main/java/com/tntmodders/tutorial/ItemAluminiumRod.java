package com.tntmodders.tutorial;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAluminiumRod extends Item {

    public ItemAluminiumRod() {
        super();
        //レジストリに保存する名称を登録する。大文字禁止。
        this.setRegistryName("aluminiummod", "aluminium_rod");
        //クリエイティブタブを設定する。
        this.setCreativeTab(CreativeTabs.MATERIALS);
        //翻訳名を登録する。大文字非推奨。
        this.setUnlocalizedName("aluminium_rod");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        worldIn.setBlockState(pos.offset(facing), Blocks.CHEST.getDefaultState());
        if (worldIn.getTileEntity(pos.offset(facing)) instanceof TileEntityChest) {
            ((TileEntityChest) worldIn.getTileEntity(pos.offset(facing))).setLootTable(
                    new ResourceLocation("aluminiummod:chests/aluminium_chest"), player.getRNG().nextLong());
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}