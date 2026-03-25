//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.Antiesp.AntiEsp.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class RaycastUtils {
    private static final List<Material> TRANSPARENT_MATERIALS = new ArrayList();
    private static boolean debugMode = false;

    public static boolean hasLineOfSight(Location from, Location to, double maxDistance) {
        if (from != null && to != null) {
            if (from.getWorld() != to.getWorld()) {
                return false;
            } else {
                double distance = from.distance(to);
                return distance > maxDistance ? false : strictRaycast(from, to, maxDistance, 0.8);
            }
        } else {
            return false;
        }
    }

    public static boolean hasLineOfSight(Location from, Location to, double maxDistance, double blockingThreshold) {
        if (from != null && to != null) {
            if (from.getWorld() != to.getWorld()) {
                return false;
            } else {
                double distance = from.distance(to);
                return distance > maxDistance ? false : strictRaycast(from, to, maxDistance, blockingThreshold);
            }
        } else {
            return false;
        }
    }

    public static boolean hasLineOfSightMultiAngle(Location from, Location to, double maxDistance, double blockingThreshold, double radius) {
        if (from != null && to != null) {
            if (from.getWorld() != to.getWorld()) {
                return false;
            } else {
                double distance = from.distance(to);
                if (distance > maxDistance) {
                    return false;
                } else {
                    int angles = 8;
                    int blockedAngles = 0;

                    for(int i = 0; i < angles; ++i) {
                        double angle = (Math.PI * 2D) * (double)i / (double)angles;
                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;
                        Location offsetFrom = from.clone().add(offsetX, (double)0.0F, offsetZ);
                        Location offsetTo = to.clone().add(offsetX, (double)0.0F, offsetZ);
                        if (!strictRaycast(offsetFrom, offsetTo, maxDistance, blockingThreshold)) {
                            ++blockedAngles;
                        }
                    }

                    double blockedRatio = (double)blockedAngles / (double)angles;
                    return blockedRatio < 0.7;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean hasLineOfSightSmart(Location from, Location to, double maxDistance, double blockingThreshold) {
        if (from != null && to != null) {
            if (from.getWorld() != to.getWorld()) {
                return false;
            } else {
                double distance = from.distance(to);
                if (distance > maxDistance) {
                    return false;
                } else {
                    double heightDiff = Math.abs(to.getY() - from.getY());
                    return heightDiff > (double)1.5F ? hasLineOfSightMultiHeight(from, to, maxDistance, blockingThreshold) : hasLineOfSightLenient(from, to, maxDistance, blockingThreshold);
                }
            }
        } else {
            return false;
        }
    }

    private static boolean hasLineOfSightLenient(Location from, Location to, double maxDistance, double blockingThreshold) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        if (distance > maxDistance) {
            distance = maxDistance;
        }

        double stepSize = 0.3;
        int steps = (int)Math.ceil(distance / stepSize);
        World world = from.getWorld();
        if (world == null) {
            return false;
        } else {
            int consecutiveBlocked = 0;
            int maxConsecutiveBlocked = 0;
            int totalBlocked = 0;
            int totalChecks = 0;

            for(int i = 0; i < steps; ++i) {
                double currentDistance = (double)i * stepSize;
                if (currentDistance >= distance) {
                    break;
                }

                Vector currentPos = from.toVector().add(direction.clone().multiply(currentDistance));
                Location checkLocation = currentPos.toLocation(world);
                ++totalChecks;
                if (isBlocked(checkLocation)) {
                    ++totalBlocked;
                    ++consecutiveBlocked;
                    maxConsecutiveBlocked = Math.max(maxConsecutiveBlocked, consecutiveBlocked);
                } else {
                    consecutiveBlocked = 0;
                }
            }

            double blockedRatio = (double)totalBlocked / (double)totalChecks;
            if (blockingThreshold >= (double)1.0F) {
                return maxConsecutiveBlocked < 4;
            } else if (blockingThreshold >= 0.8) {
                return blockedRatio < blockingThreshold;
            } else {
                return blockedRatio < blockingThreshold || maxConsecutiveBlocked < 2;
            }
        }
    }

    private static boolean hasLineOfSightMultiHeight(Location from, Location to, double maxDistance, double blockingThreshold) {
        double[] heights = new double[]{(double)1.0F, 1.2, 1.4, 1.6, 1.8, (double)2.0F};
        int clearPaths = 0;
        int totalPaths = heights.length;

        for(double height : heights) {
            Location fromHeight = from.clone().add((double)0.0F, height, (double)0.0F);
            Location toHeight = to.clone().add((double)0.0F, height, (double)0.0F);
            if (hasLineOfSightLenient(fromHeight, toHeight, maxDistance, blockingThreshold)) {
                ++clearPaths;
            }
        }

        double clearRatio = (double)clearPaths / (double)totalPaths;
        return clearRatio > 0.3;
    }

    public static boolean raycast(Location from, Location to, double maxDistance) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        if (distance > maxDistance) {
            distance = maxDistance;
        }

        double stepSize = 0.1;
        int steps = (int)Math.ceil(distance / stepSize);
        World world = from.getWorld();
        if (world == null) {
            return false;
        } else {
            for(int i = 0; i < steps; ++i) {
                double currentDistance = (double)i * stepSize;
                if (currentDistance >= distance) {
                    break;
                }

                Vector currentPos = from.toVector().add(direction.clone().multiply(currentDistance));
                Location checkLocation = currentPos.toLocation(world);
                if (isBlocked(checkLocation)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean strictRaycast(Location from, Location to, double maxDistance, double blockingThreshold) {
        long startTime = System.nanoTime();
        if (debugMode) {
            debugLog("=== 开始严格射线投射 ===");
            debugLog("起点: " + formatLocation(from));
            debugLog("终点: " + formatLocation(to));
            debugLog("最大距离: " + maxDistance);
            debugLog("阻挡阈值: " + blockingThreshold * (double)100.0F + "%");
        }

        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        if (distance > maxDistance) {
            distance = maxDistance;
            if (debugMode) {
                debugLog("距离超过最大距离，限制为: " + maxDistance);
            }
        }

        double stepSize = 0.1;
        int steps = (int)Math.ceil(distance / stepSize);
        if (debugMode) {
            Object[] var10001 = new Object[]{distance};
            debugLog("实际距离: " + String.format("%.2f", var10001));
            debugLog("步长: " + stepSize);
            debugLog("总步数: " + steps);
        }

        World world = from.getWorld();
        if (world == null) {
            if (debugMode) {
                debugLog("世界为null，返回false");
            }

            return false;
        } else {
            int consecutiveBlocked = 0;
            int maxConsecutiveBlocked = 0;
            int totalBlocked = 0;
            int totalChecks = 0;

            for(int i = 0; i < steps; ++i) {
                double currentDistance = (double)i * stepSize;
                if (currentDistance >= distance) {
                    break;
                }

                Vector currentPos = from.toVector().add(direction.clone().multiply(currentDistance));
                Location checkLocation = currentPos.toLocation(world);
                ++totalChecks;
                boolean isBlockedAtLocation = isBlocked(checkLocation);
                if (isBlockedAtLocation) {
                    ++totalBlocked;
                    ++consecutiveBlocked;
                    maxConsecutiveBlocked = Math.max(maxConsecutiveBlocked, consecutiveBlocked);
                    if (debugMode && i % 10 == 0) {
                        debugLog("步骤 " + i + ": 位置 " + formatLocation(checkLocation) + " 被阻挡 (连续: " + consecutiveBlocked + ")");
                    }
                } else {
                    consecutiveBlocked = 0;
                    if (debugMode && i % 10 == 0) {
                        debugLog("步骤 " + i + ": 位置 " + formatLocation(checkLocation) + " 畅通");
                    }
                }
            }

            double blockedRatio = (double)totalBlocked / (double)totalChecks;
            boolean result;
            String logicType;
            if (blockingThreshold >= (double)1.0F) {
                result = maxConsecutiveBlocked < 2;
                logicType = "100%阈值 (连续阻挡: " + maxConsecutiveBlocked + " < 2)";
            } else if (blockingThreshold >= 0.9) {
                result = blockedRatio < blockingThreshold;
                String var10000 = String.format("%.2f", blockedRatio * (double)100.0F);
                logicType = "90%+阈值 (阻挡比例: " + var10000 + "% < " + blockingThreshold * (double)100.0F + "%)";
            } else if (blockingThreshold >= 0.7) {
                result = blockedRatio < blockingThreshold && maxConsecutiveBlocked < 3;
                String var30 = String.format("%.2f", blockedRatio * (double)100.0F);
                logicType = "70%+阈值 (比例: " + var30 + "% < " + blockingThreshold * (double)100.0F + "% 且 连续: " + maxConsecutiveBlocked + " < 3)";
            } else {
                result = blockedRatio < blockingThreshold;
                String var31 = String.format("%.2f", blockedRatio * (double)100.0F);
                logicType = "低阈值 (阻挡比例: " + var31 + "% < " + blockingThreshold * (double)100.0F + "%)";
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000L;
            if (debugMode) {
                debugLog("=== 严格射线投射结果 ===");
                debugLog("总检查次数: " + totalChecks);
                debugLog("阻挡次数: " + totalBlocked);
                String var32 = String.format("%.2f", blockedRatio * (double)100.0F);
                debugLog("阻挡比例: " + var32 + "%");
                debugLog("最大连续阻挡: " + maxConsecutiveBlocked);
                debugLog("检测逻辑: " + logicType);
                debugLog("最终结果: " + (result ? "可见" : "不可见"));
                debugLog("检测耗时: " + duration + "μs");
            }

            return result;
        }
    }

    public static boolean isBlocked(Location location) {
        if (location != null && location.getWorld() != null) {
            Block block = location.getBlock();
            Material material = block.getType();
            if (TRANSPARENT_MATERIALS.contains(material)) {
                return false;
            } else if (material.isOccluding()) {
                return !material.name().contains("GLASS") && !material.name().contains("ICE") && !material.name().contains("SLIME") && !material.name().contains("HONEY");
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static Location getRaycastHit(Location from, Vector direction, double maxDistance) {
        if (from != null && direction != null) {
            World world = from.getWorld();
            if (world == null) {
                return null;
            } else {
                double stepSize = 0.1;
                int steps = (int)Math.ceil(maxDistance / stepSize);

                for(int i = 0; i < steps; ++i) {
                    double currentDistance = (double)i * stepSize;
                    if (currentDistance >= maxDistance) {
                        break;
                    }

                    Vector currentPos = from.toVector().add(direction.clone().multiply(currentDistance));
                    Location checkLocation = currentPos.toLocation(world);
                    if (isBlocked(checkLocation)) {
                        return checkLocation;
                    }
                }

                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean isTransparent(Material material) {
        return TRANSPARENT_MATERIALS.contains(material);
    }

    public static void addTransparentMaterial(Material material) {
        if (material != null && !TRANSPARENT_MATERIALS.contains(material)) {
            TRANSPARENT_MATERIALS.add(material);
        }

    }

    public static void removeTransparentMaterial(Material material) {
        TRANSPARENT_MATERIALS.remove(material);
    }

    public static List<Material> getTransparentMaterials() {
        return new ArrayList(TRANSPARENT_MATERIALS);
    }

    public static double calculateDistance(Location from, Location to) {
        if (from != null && to != null) {
            return from.getWorld() != to.getWorld() ? Double.MAX_VALUE : from.distance(to);
        } else {
            return Double.MAX_VALUE;
        }
    }

    public static double calculateHorizontalDistance(Location from, Location to) {
        if (from != null && to != null) {
            if (from.getWorld() != to.getWorld()) {
                return Double.MAX_VALUE;
            } else {
                double dx = to.getX() - from.getX();
                double dz = to.getZ() - from.getZ();
                return Math.sqrt(dx * dx + dz * dz);
            }
        } else {
            return Double.MAX_VALUE;
        }
    }

    public static void setDebugMode(boolean debug) {
        debugMode = debug;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    private static void debugLog(String message) {
        if (debugMode) {
            System.out.println("[RaycastUtils] " + message);
        }

    }

    private static String formatLocation(Location location) {
        return location == null ? "null" : String.format("(%s, %.2f, %.2f, %.2f)", location.getWorld() != null ? location.getWorld().getName() : "null", location.getX(), location.getY(), location.getZ());
    }

    static {
        TRANSPARENT_MATERIALS.add(Material.AIR);
        TRANSPARENT_MATERIALS.add(Material.CAVE_AIR);
        TRANSPARENT_MATERIALS.add(Material.VOID_AIR);
        TRANSPARENT_MATERIALS.add(Material.GLASS);
        TRANSPARENT_MATERIALS.add(Material.GLASS_PANE);
        TRANSPARENT_MATERIALS.add(Material.WHITE_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.ORANGE_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.MAGENTA_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.LIGHT_BLUE_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.YELLOW_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.LIME_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.PINK_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.GRAY_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.LIGHT_GRAY_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.CYAN_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.PURPLE_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.BLUE_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.BROWN_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.GREEN_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.RED_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.BLACK_STAINED_GLASS);
        TRANSPARENT_MATERIALS.add(Material.IRON_BARS);
        TRANSPARENT_MATERIALS.add(Material.CHAIN);
        TRANSPARENT_MATERIALS.add(Material.LADDER);
        TRANSPARENT_MATERIALS.add(Material.VINE);
        TRANSPARENT_MATERIALS.add(Material.WATER);
        TRANSPARENT_MATERIALS.add(Material.LAVA);
        TRANSPARENT_MATERIALS.add(Material.TORCH);
        TRANSPARENT_MATERIALS.add(Material.WALL_TORCH);
        TRANSPARENT_MATERIALS.add(Material.REDSTONE_TORCH);
        TRANSPARENT_MATERIALS.add(Material.REDSTONE_WALL_TORCH);
        TRANSPARENT_MATERIALS.add(Material.TRIPWIRE);
        TRANSPARENT_MATERIALS.add(Material.TRIPWIRE_HOOK);
        TRANSPARENT_MATERIALS.add(Material.COBWEB);
        TRANSPARENT_MATERIALS.add(Material.SCAFFOLDING);
        TRANSPARENT_MATERIALS.add(Material.BARRIER);
        TRANSPARENT_MATERIALS.add(Material.STRUCTURE_VOID);
        TRANSPARENT_MATERIALS.add(Material.LIGHT);
        TRANSPARENT_MATERIALS.add(Material.ICE);
        TRANSPARENT_MATERIALS.add(Material.PACKED_ICE);
        TRANSPARENT_MATERIALS.add(Material.BLUE_ICE);
        TRANSPARENT_MATERIALS.add(Material.FROSTED_ICE);
        TRANSPARENT_MATERIALS.add(Material.SLIME_BLOCK);
        TRANSPARENT_MATERIALS.add(Material.HONEY_BLOCK);
        TRANSPARENT_MATERIALS.add(Material.SNOW);
        TRANSPARENT_MATERIALS.add(Material.SNOW_BLOCK);
        TRANSPARENT_MATERIALS.add(Material.POWDER_SNOW);
        TRANSPARENT_MATERIALS.add(Material.OAK_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.SPRUCE_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.BIRCH_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.JUNGLE_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.ACACIA_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.DARK_OAK_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.MANGROVE_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.CHERRY_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.AZALEA_LEAVES);
        TRANSPARENT_MATERIALS.add(Material.FLOWERING_AZALEA_LEAVES);
    }
}
