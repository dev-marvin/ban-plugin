package me.tuskdev.ban.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ViewListener implements Listener {

	private final ViewFrame frame;

	public ViewListener(final ViewFrame frame) {
		this.frame = frame;
	}

	private View getView(final Inventory inventory, final Player player) {
		// check for Player#getTopInventory
		if (inventory == null)
			return null;

		final InventoryHolder holder = inventory.getHolder();
		if (!(holder instanceof View))
			return null;

		final View view = (View) holder;
		if (inventory.getType() != InventoryType.CHEST)
			throw new UnsupportedOperationException("Views is only supported on chest-type inventory.");

		return view;
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onViewPluginDisable(final PluginDisableEvent e) {
		if (!frame.getOwner().equals(e.getPlugin()))
			return;

		frame.unregister();
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onViewItemDrag(final InventoryDragEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		final Inventory inventory = e.getInventory();
		final View view = getView(inventory, (Player) e.getWhoClicked());
		if (view == null)
			return;

		if (!view.isCancelOnDrag())
			return;

		final int size = inventory.getSize();
		for (int slot : e.getRawSlots()) {
			if (!(slot < size))
				continue;

			e.setCancelled(true);
			break;
		}
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onViewClick(final InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		final Player player = (Player) e.getWhoClicked();

		final Inventory inventory = e.getInventory();
		final View view = getView(inventory, player);
		if (view == null)
			return;

		if (view.getFrame().isDebugEnabled()) {
			player.sendMessage("[IF DEBUG] interacting with view (already " +
				"cancelled = " + e.isCancelled() + ")");
			player.sendMessage("[IF DEBUG] tracked slot: " + e.getSlotType());
			player.sendMessage("[IF DEBUG] tracked click: " + e.getClick());
		}

		final ViewContext context;
		try {
			context = getContextOrThrow(view, player);
		} catch (final IllegalStateException ex) {
			e.setCancelled(true);

			if (view.getFrame().isDebugEnabled())
				player.sendMessage("[IF DEBUG] null view context detected");

			Bukkit.getScheduler().runTask(frame.getOwner(), player::closeInventory);
			ex.printStackTrace();
			return;
		}

		if (e.getSlotType() == InventoryType.SlotType.OUTSIDE) {
			e.setCancelled(true);

			if (view.isCloseOnOutsideClick())
				context.closeNow();

			view.onClickOutside(context);
			return;
		}

		final InventoryAction action = e.getAction();
		if (view.getFrame().isDebugEnabled())
			player.sendMessage("[IF DEBUG] tracked action: " + action);

		if (e.getClick() == ClickType.NUMBER_KEY)
			view.onHotbarInteract(context, e.getHotbarButton());

		if (e.getClick() != ClickType.NUMBER_KEY && (
			action == InventoryAction.UNKNOWN || action == InventoryAction.NOTHING
		)) {
			e.setCancelled(true);
			return;
		}

		final ItemStack cursor = e.getCursor();
		final int slot = e.getSlot();

		final boolean bottomInventoryClick = !(e.getRawSlot() < inventory.getSize());
		if (bottomInventoryClick && view.getFrame().isDebugEnabled())
			player.sendMessage("[IF DEBUG] bottom inventory click");

		if (!bottomInventoryClick && action == InventoryAction.CLONE_STACK && view.isCancelOnClone()) {
			e.setCancelled(true);
			return;
		}

		if (bottomInventoryClick) {
			e.setCancelled(view.isCancelOnClick());

			final ViewSlotContext click = new DelegatedViewContext(context, slot, e.getCurrentItem(), e);
			view.runCatching(click, () -> view.onClickBottomside(click));

			return;
		}

		e.setCancelled(view.isCancelOnClick());
		final ItemStack stack = e.getCurrentItem();

		final ClickType click = e.getClick();
		if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD ||
			click == ClickType.DROP ||
			click == ClickType.CONTROL_DROP) {
			ItemStack targetItem = null;
			final Inventory targetInventory = e.getView().getBottomInventory();
			if (action == InventoryAction.HOTBAR_MOVE_AND_READD)
				targetItem = targetInventory.getItem(e.getHotbarButton());

			final ViewSlotMoveContext moveOutContext = new ViewSlotMoveContext(context, slot, stack, targetInventory,
				targetItem, slot, false, false, e);
			view.runCatching(moveOutContext,
				() -> view.onMoveOut(moveOutContext));

			if (view.isCancelOnMoveOut() || moveOutContext.isCancelled())
				e.setCancelled(true);

			if (moveOutContext.isMarkedToClose())
				Bukkit.getScheduler().runTask(frame.getOwner(), moveOutContext::closeNow);
			return;
		}

		final ViewItem item = view.resolve(context, slot);

		// global click handling
		final ViewSlotContext globalClick = new DelegatedViewContext(context, slot, stack, e);
		view.runCatching(globalClick, () -> view.onClick(globalClick));

		if (globalClick.isCancelled())
			return;

		if (item == null) {
			final ViewItem holdingItem = resolveReleasableItem(view, context);
			if (holdingItem != null)
				releaseAt(new DelegatedViewContext(context,
						holdingItem.getSlot(), stack, e), slot, cursor,
					e.getClickedInventory());

			return;
		}

		final ViewSlotContext slotContext = new DelegatedViewContext(context,
			slot, stack, e);

		if (item.getClickHandler() != null) {
			view.runCatching(slotContext,
				() -> item.getClickHandler().handle(slotContext));
			e.setCancelled(e.isCancelled() || slotContext.isCancelled());
		}

		if (item.isOverrideCancelOnClick())
			e.setCancelled(item.isCancelOnClick());

		if (!e.isCancelled() && (view.isCancelOnShiftClick() || item.isOverrideCancelOnShiftClick()) && click.isShiftClick())
			e.setCancelled(view.isCancelOnShiftClick() || item.isCancelOnShiftClick());

		if (!e.isCancelled()) {
			if (action.name().startsWith("PICKUP") || action == InventoryAction.CLONE_STACK) {
				item.setState(ViewItem.State.HOLDING);
				view.runCatching(slotContext, () ->
					view.onItemHold(slotContext));
			} else if (item.getState() == ViewItem.State.HOLDING) {
				final ViewItem holdingItem = resolveReleasableItem(view, slotContext);
				if (holdingItem != null)
					releaseAt(slotContext, slot, cursor, e.getClickedInventory());
			}
		}

		if (item.isCloseOnClick() || slotContext.isMarkedToClose())
			Bukkit.getScheduler().runTask(frame.getOwner(), slotContext::closeNow);
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onViewClose(final InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player))
			return;

		final View view = getView(e.getInventory(), (Player) e.getPlayer());
		if (view == null)
			return;

		final Player player = (Player) e.getPlayer();
		final ViewContext context = view.getContext(player);
		if (context == null)
			return;

		final ViewContext close = new CloseViewContext(context);
		view.runCatching(context, () -> view.onClose(close));

		if (close.isCancelled()) {
			Bukkit.getScheduler().runTaskLater(
				frame.getOwner(),
				() -> player.openInventory(close.getInventory()),
				1L
			);

			// set the old cursor item
			final ItemStack cursor = player.getItemOnCursor();

			// cursor can be null in legacy versions
			//noinspection ConstantConditions
			if ((cursor != null) && cursor.getType() != Material.AIR)
				player.setItemOnCursor(cursor);
			return;
		}

		if (view.isClearCursorOnClose())
			player.setItemOnCursor(null);

		view.remove(context);
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onDropItemOnView(final PlayerDropItemEvent e) {
		final View view = getView(e.getPlayer().getOpenInventory().getTopInventory(), e.getPlayer());
		if (view == null)
			return;

		e.setCancelled(view.isCancelOnDrop());
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onPickupItemOnView(final PlayerPickupItemEvent e) {
		final View view = getView(e.getPlayer().getOpenInventory().getTopInventory(), e.getPlayer());
		if (view == null)
			return;

		e.setCancelled(view.isCancelOnPickup());
	}

	private ViewItem resolveReleasableItem(View view, ViewContext context) {
		// we can't use `view.getRows()` here because the inventory size can be set dynamically
		// the items array size is updated when view is dynamic, so we can use this safely
		for (int i = 0; i < view.getItems().length; i++) {
			// must use resolve to works with context-defined items (not only items defined on View constructor)
			final ViewItem holdingItem = view.resolve(context, i);

			if (holdingItem == null || holdingItem.getState() != ViewItem.State.HOLDING)
				continue;

			return holdingItem;
		}

		return null;
	}

	private void releaseAt(ViewSlotContext context, int slot, ItemStack cursor, Inventory inventory) {
		context.getView().onItemRelease(context, new ViewSlotContext(context.getView(), context.getPlayer(),
			inventory, slot, cursor));

		final int currentSlot = context.getSlot();
		final ViewItem currentItem = context.getView().resolve(context, context.getSlot());
		context.getItems()[currentSlot] = null;

		if (currentItem == null)
			return;

		currentItem.setState(ViewItem.State.UNDEFINED);

		// outside top inventory
		if (slot > context.getInventory().getSize())
			return;

		context.getItems()[slot] = currentItem;
	}


	private ViewContext getContextOrThrow(
		View view,
		Player player
	) {
		final ViewContext context = view.getContext(player);

		// for some reason I haven't figured out which one yet, it's possible
		// that the View's inventory is open and the context doesn't exist,
		// so we check to see if it's null
		if (context == null)
			throw new IllegalStateException("View context cannot be null to " + player.getName() + " in " + view.getClass().getName());

		return context;
	}

}
