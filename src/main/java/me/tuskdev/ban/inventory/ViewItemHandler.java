package me.tuskdev.ban.inventory;

@FunctionalInterface
public interface ViewItemHandler {

    void handle(ViewSlotContext context);

}
