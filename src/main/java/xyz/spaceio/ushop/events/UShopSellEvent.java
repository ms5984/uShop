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
package xyz.spaceio.ushop.events;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.spaceio.customitem.CustomItem;

import java.util.Map;
import java.util.Set;

public class UShopSellEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    protected final Player player;
    protected final Map<CustomItem, Integer> items;

    public UShopSellEvent(Player player, Map<CustomItem, Integer> items) {
        this.player = player;
        this.items = items;
    }

    public Player getPlayer() {
        return player;
    }

    public Set<ItemStack> getItems() {
        final ImmutableSet.Builder<ItemStack> builder = ImmutableSet.builder();
        items.entrySet().parallelStream()
                .map(e -> e.getKey().getItemCopy(e.getValue()))
                .sequential()
                .forEach(builder::add);
        return builder.build();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
