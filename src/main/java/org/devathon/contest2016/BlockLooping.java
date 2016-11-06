package org.devathon.contest2016;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;

/**
 * Created by zacha on 11/6/2016.
 */
public class BlockLooping {

    public static ArrayList<Block> loopCube(final Location loc1, final Location loc2){
        ArrayList<Block> blocks = new ArrayList<Block>();

        int topBlockX = (loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());
        int bottomBlockX = (loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX());

        int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());
        int bottomBlockY = (loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY());

        int topBlockZ = (loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());
        int bottomBlockZ = (loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ());

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public static ArrayList<Block> loopSphere(final Location center, final int radius){
        ArrayList<Block> sphere = new ArrayList<Block>();
        for (int Y = -radius; Y < radius; Y++){
            for (int X = -radius; X < radius; X++){
                for (int Z = -radius; Z < radius; Z++){
                    if (Math.sqrt((X*X)+(Y*Y)+(Z*Z)) <= radius){
                        sphere.add(center.getWorld().getBlockAt(X + center.getBlockX(), Y+center.getBlockY(),Z+center.getBlockZ()));
                    }
                }
            }
        }
        return sphere;
    }
    public static ArrayList<Block> loopSphere(final Location center, final int radius, boolean ignoreAir){
        if (!ignoreAir){
            return loopSphere(center, radius);
        }
        ArrayList<Block> sphere = new ArrayList<Block>();
        for (int Y = -radius; Y < radius; Y++){
            for (int X = -radius; X < radius; X++){
                for (int Z = -radius; Z < radius; Z++){
                    if (Math.sqrt((X*X)+(Y*Y)+(Z*Z)) <= radius){
                        Block b = center.getWorld().getBlockAt(X + center.getBlockX(), Y+center.getBlockY(),Z+center.getBlockZ());
                        if (b.getType() != Material.AIR) {
                            sphere.add(b);
                        }
                    }
                }
            }
        }
        return sphere;
    }

    public static ArrayList<Location> loopHelix(final Location center, final int radius, final int height, final double increment, final boolean reverseSinCos){
        ArrayList<Location> helix = new ArrayList<>();
        for(double y = 0; y <= height; y+=increment) {
            if (!reverseSinCos) {
                double x = radius * Math.cos(y);
                double z = radius * Math.sin(y);
                helix.add(center.clone().add(x, y, z));
            }else{
                double x = radius * Math.sin(y);
                double z = radius * Math.cos(y);
                helix.add(center.clone().add(x, y, z));
            }
        }
        return helix;
    }

    public static ArrayList<Location> loopHelixBoth(final Location center, final int radius, final int height, final double increment){
        ArrayList<Location> h1 = loopHelix(center, radius, height, increment, false);
        for (Location l : loopHelix(center, radius, height, increment, true)){
            h1.add(l);
        }
        return h1;
    }

    public static ArrayList<Location> loopCylinder(Location center, int radius){
        ArrayList<Location> cyl = new ArrayList<>();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int rSquared = radius * radius;
        for (int x = cx - radius; x <= cx +radius; x++) {
            for (int z = cz - radius; z <= cz +radius; z++) {
                if ((cx - x) * (cx - x) + (cz - z) * (cz - z) <= rSquared) {
                    cyl.add(center.getWorld().getBlockAt(x, center.getBlockY(), z).getLocation());
                }
            }
        }
        return cyl;
    }

    public static Block getBlockAtDistance(LivingEntity entity, int range, boolean stop_at_solid, boolean return_previous) {
        BlockIterator iterator = new BlockIterator(entity, range);
        Block prev = null;
        Block b = null;
        while (iterator.hasNext()) {
            if (b != null){
                prev = b;
            }
            b = iterator.next();

            if (iterator.hasNext()) {
                if (stop_at_solid) {
                    if (b.getType() == Material.AIR
                            || b.getType() == Material.VINE
                            || b.getType() == Material.TORCH
                            || b.getType() == Material.DEAD_BUSH
                            || b.getType() == Material.SIGN_POST
                            || b.getType() == Material.WALL_SIGN) {
                        continue;
                    } else {
                        if (return_previous) {
                            return prev;
                        }else{
                            return b;
                        }
                    }
                } else {
                    continue;
                }
            } else {
                if (return_previous) {
                    return prev;
                }else{
                    return b;
                }
            }
        }

        return null;
    }
}
