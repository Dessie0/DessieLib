package me.dessie.dessielib.inventoryapi.inventory;

import me.dessie.dessielib.inventoryapi.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class PaginatedInventory<T extends PaginatedInventory<T>> extends CustomInventory<T> {

    private List<PaginatedInventory<T>> pages = new ArrayList<>();

    private ItemStack nextButton = ItemBuilder.buildItem(Material.ARROW, 1, "&cNext");
    private ItemStack backButton = ItemBuilder.buildItem(Material.ARROW, 1, "&cPrevious");
    private boolean hideInvalidButton = true;
    private int nextSlot;
    private int backSlot;

    protected BiConsumer<Player, PaginatedInventory<T>> pageChange;

    public PaginatedInventory(int size, String name) {
        super(size, name);

        this.nextSlot = this.getSize() - 1;
        this.backSlot = this.getSize() - 9;

        this.pages.add(this);
    }

    public ItemStack getNextButton() {
        return nextButton;
    }

    public ItemStack getBackButton() {
        return backButton;
    }

    public int getNextSlot() {
        return nextSlot;
    }

    public int getBackSlot() {
        return backSlot;
    }

    public boolean isHideInvalidButton() {
        return hideInvalidButton;
    }

    @SuppressWarnings("unchecked")
    public T setNextButton(ItemStack nextButton) {
        this.nextButton = nextButton;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setBackButton(ItemStack backButton) {
        this.backButton = backButton;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setNextSlot(int nextSlot) {
        this.nextSlot = nextSlot;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setBackSlot(int backSlot) {
        this.backSlot = backSlot;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setHideInvalidButton(boolean hideInvalidButton) {
        this.hideInvalidButton = hideInvalidButton;
        return (T) this;
    }

    /**
     * @return The list of pages this inventory is holding.
     */
    public List<PaginatedInventory<T>> getPages() {
        return pages;
    }

    /**
     * Adds a PaginatedInventory as a page to this PaginatedInventory
     * Page arrows will automatically be set on the bottom corner slots.
     *
     * @param builder The builder to add.
     * @return The original InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T addPage(PaginatedInventory<T> builder) {
        this.getPages().add(builder);
        this.setPageArrows();

        //Update all pages with the new list of pages.
        for(PaginatedInventory<T> page : this.getPages()) {
            page.setPages(this.getPages());
            page.setPageArrows();
        }
        return (T) this;
    }

    /**
     * Adds ALL items into the InventoryBuilder.
     * If the current page is full, the InventoryBuilder will be copied
     * and items will begin to be placed there.
     *
     * @param items A list of {@link ItemBuilder}s to place into the inventory
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T addItemsAndPages(List<ItemBuilder> items, Supplier<PaginatedInventory<T>> supplier) {
        int currentPage = 0;
        for(ItemBuilder builder : items) {
            if(this.getPage(currentPage).addItem(builder) == null) {
                this.addPage(supplier.get()).addItem(builder);
            }
        }

        return (T) this;
    }

    /**
     * @param page The page to get
     * @return The InventoryBuilder at that page index
     */
    @SuppressWarnings("unchecked")
    public T getPage(int page) {
        try {
            return (T) this.getPages().get(page);
        } catch (IndexOutOfBoundsException ignored) {
            throw new IndexOutOfBoundsException("Page does not exist!");
        }
    }

    /**
     * Forcefully places page arrows into an Inventory
     * @return The InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T setPageArrows() {
        if(!this.isHideInvalidButton() || this.getPages().indexOf(this) != this.getPages().size() - 1) {
            this.setItem(this.getNextSlot(), this.getNextButton())
                    .onClick((result) -> this.nextPage(result.getPlayer())).cancel();
        }

        if(!this.isHideInvalidButton() || this.getPages().indexOf(this) != 0) {
            this.setItem(this.getBackSlot(), this.getBackButton())
                    .onClick((result) -> this.previousPage(result.getPlayer())).cancel();
        }

        return (T) this;
    }

    /**
     * Called when the Inventory's page changes.
     * @param consumer A BiFunction containing the Player that changed the page
     *                 and the new page.
     * @return The current InventoryBuilder
     */
    @SuppressWarnings("unchecked")
    public T onPageChange(BiConsumer<Player, PaginatedInventory<T>> consumer) {
        this.pageChange = consumer;
        return (T) this;
    }

    public void nextPage(Player player) {
        PaginatedInventory<T> newPage = this.getPages().indexOf(this) + 1 < this.getPages().size() ? this.getPages().get(this.getPages().indexOf(this) + 1) : null;

        if(newPage == null) return;

        this.executePageChange(player, newPage);
        this.removeHolder(player);
        newPage.open(player);
    }

    public void previousPage(Player player) {
        PaginatedInventory<T> newPage = this.getPages().indexOf(this) - 1 >= 0 ? this.getPages().get(this.getPages().indexOf(this) - 1) : null;

        if(newPage == null) return;

        this.executePageChange(player, newPage);
        this.removeHolder(player);
        newPage.open(player);
    }

    private void executePageChange(Player player, PaginatedInventory<T> newPage) {
        if (this.pageChange != null) {
            this.pageChange.accept(player, newPage);
        }
    }

    private void setPages(List<PaginatedInventory<T>> pages) {
        this.pages = pages;
    }
}
