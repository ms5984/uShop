/*
 * MIT License
 *
 * Copyright (c) 2021 Matt (ms5984) <https://github.com/ms5984>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.spaceio.ushop;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.spaceio.customitem.CustomItem;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CommandOnSellHandler {
    private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());
    private final Player player;
    private final Map<CustomItem, Integer> soldItems;

    CommandOnSellHandler(Player player, Map<CustomItem, Integer> soldItems) {
        this.player = player;
        this.soldItems = soldItems;
    }

    void processCommand() {
        final String string = plugin.getConfig().getString("on-sell.run-command");
        if (string == null) return;
        final boolean replacePlayerName = string.contains("%player%");
        final boolean replaceItems = string.contains("%items%");
        final boolean replaceCost = string.contains("%cost%");
        final String lang = Optional.ofNullable(plugin.getConfig().getString("on-sell.cost-locale.lang"))
                .filter(String::isEmpty)
                .orElse("en");
        final String country = Optional.ofNullable(plugin.getConfig().getString("on-sell.cost-locale.country"))
                .filter(String::isEmpty)
                .orElse("US");
        final List<String> formatList = plugin.getConfig().getStringList("on-sell.items-format");
        final String itemsFormat = formatList.get(0);
        final String itemsFormatSeparator = formatList.get(1);
        final String itemsFormatJoin = formatList.get(2);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
            double cost = 0;
            final Locale costLocale = new Locale(lang, country);
            final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(costLocale);
            for (Map.Entry<CustomItem, Integer> entry : soldItems.entrySet()) {
                if (replaceItems || replaceCost) {
                    final CustomItem key = entry.getKey();
                    final double itemTotal = key.getPrice() * entry.getValue();
                    if (replaceItems && itemsFormat != null) {
                        String name = key.getDisplayname();
                        if (name == null) name = key.getMaterial().toLowerCase();
                        builder.add(itemsFormat
                                .replace("%quantity%", String.valueOf(entry.getValue()))
                                .replace("%name%", name)
                                .replace("%cost%", currencyInstance.format(itemTotal))
                        );
                    }
                    cost += itemTotal;
                }
            }
            final double finalCost = cost;
            final AtomicReference<String> finalCommand = new AtomicReference<>(string);
            final ImmutableList<String> items = builder.build();
            if (replaceItems) {
                if (!items.isEmpty()) {
                    final int size = items.size();
                    if (size == 1) {
                        finalCommand.updateAndGet(s -> s.replace("%items%", items.get(0)));
                    } else if (size == 2) {
                        finalCommand.updateAndGet(s -> s.replace(
                                "%items%",
                                items.get(0) + itemsFormatJoin + items.get(1)
                        ));
                    } else {
                        final StringBuilder sb = new StringBuilder(items.get(0));
                        for (int i = 1; i < size - 1; ++i) {
                            sb.append(itemsFormatSeparator);
                            sb.append(items.get(i));
                        }
                        sb.append(itemsFormatJoin);
                        sb.append(items.get(size - 1));
                        finalCommand.updateAndGet(s -> s.replace("%items%", sb.toString()));
                    }
                } else {
                    finalCommand.updateAndGet(s -> s.replace(" %items%", ""));
                }
            }
            if (replaceCost) {
                finalCommand.updateAndGet(s -> s.replace("%cost%", currencyInstance.format(finalCost)));
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                final String playerName = player.getName();
                if (replacePlayerName) finalCommand.updateAndGet(s -> s.replace("%player%", playerName));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand.get());
            });
        });
    }
}
