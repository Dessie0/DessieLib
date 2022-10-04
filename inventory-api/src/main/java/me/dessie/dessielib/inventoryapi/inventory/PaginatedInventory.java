package me.dessie.dessielib.inventoryapi.inventory;

import me.dessie.dessielib.inventoryapi.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Abstract class that implements generic functionality for paginated inventories.
 *
 * @param <T> A type that extends PaginatedInventory, this type will be returned for builder purposes.
 */
public abstract class PaginatedInventory<T extends PaginatedInventory<T>> extends CustomInventory<T> {

    private List<PaginatedInventory<?>> pages = new ArrayList<>();

    private ItemStack nextButton = ItemBuilder.buildItem(Material.ARROW, 1, "&cNext");
    private ItemStack backButton = ItemBuilder.buildItem(Material.ARROW, 1, "&cPrevious");
    private boolean hideInvalidButton = true;
    private int nextSlot;
    private int backSlot;

    private BiConsumer<Player, PaginatedInventory<?>> pageChange;

    /**
     * Creates a new PaginatedInventory with the provided size and name.
     *
     * The size must be a multiple of 9, no greater than 54 and no less than 9.
     * The name will automatically have any colors translated, including HEX colors.
     *
     * For example: "&amp;bInventory {@literal #};ffffffName" would result in "Inventory" being aqua, and "Name" being white.
     *
     * @param size The size of the inventory.
     * @param name The name of the inventory.
     */
    protected PaginatedInventory(int size, String name) {
        super(size, name);

        this.nextSlot = this.getSize() - 1;
        this.backSlot = this.getSize() - 9;

        this.pages.add(this);
    }

    /**
     * Returns the current {@link ItemStack} that is used for the next button.
     *
     * @see PaginatedInventory#getBackButton()
     *
     * @return The next button ItemStack.
     */
    public ItemStack getNextButton() {
        return nextButton;
    }

    /**
     * Returns the current {@link ItemStack} that is used for the back button.
     *
     * @see PaginatedInventory#getNextButton()
     *
     * @return The back button ItemStack.
     */
    public ItemStack getBackButton() {
        return backButton;
    }

    /**
     * Returns the current slot that the next button is placed in.
     *
     * @see PaginatedInventory#getBackSlot()
     *
     * @return The slot of the next button.
     */
    public int getNextSlot() {
        return nextSlot;
    }

    /**
     * Returns the current slot that the back button is placed in.
     *
     * @see PaginatedInventory#getNextSlot()
     *
     * @return The slot of the back button.
     */
    public int getBackSlot() {
        return backSlot;
    }

    /**
     * Returns if buttons will be hidden if they are "invalid".
     *
     * The next button will be invalid if there isn't a next page.
     * The back button will be invalid if the current page is the first page.
     *
     * @return If the buttons should be hidden.
     */
    public boolean isHideInvalidButton() {
        return hideInvalidButton;
    }

    /**
     * Sets the {@link ItemStack} that will be used for the next button.
     *
     * @see PaginatedInventory#setPageArrows()
     * @see PaginatedInventory#setBackButton(ItemStack)
     *
     * @param nextButton The new next button ItemStack
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T setNextButton(ItemStack nextButton) {
        this.nextButton = nextButton;
        return (T) this;
    }

    /**
     * Sets the {@link ItemStack} that will be used for the back button.
     *
     * @see PaginatedInventory#setPageArrows()
     * @see PaginatedInventory#setNextButton(ItemStack)
     *
     * @param backButton The new back button ItemStack
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T setBackButton(ItemStack backButton) {
        this.backButton = backButton;
        return (T) this;
    }

    /**
     * Sets the slot that will be used for the next button.
     *
     * @see PaginatedInventory#setPageArrows()
     * @see PaginatedInventory#setBackSlot(int)
     *
     * @param nextSlot The new next button slot to use
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T setNextSlot(int nextSlot) {
        this.nextSlot = nextSlot;
        return (T) this;
    }

    /**
     * Sets the slot that will be used for the back button.
     *
     * @see PaginatedInventory#setPageArrows()
     * @see PaginatedInventory#setNextSlot(int)
     *
     * @param backSlot The new back button slot to use
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T setBackSlot(int backSlot) {
        this.backSlot = backSlot;
        return (T) this;
    }

    /**
     * Sets if buttons will be hidden if they are "invalid".
     *
     * The next button will be invalid if there isn't a next page.
     * The back button will be invalid if the current page is the first page.
     *
     * @param hideInvalidButton If the buttons should be hidden when invalid.
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T setHideInvalidButton(boolean hideInvalidButton) {
        this.hideInvalidButton = hideInvalidButton;
        return (T) this;
    }

    /**
     * @return The list of pages this inventory is holding.
     */
    public List<PaginatedInventory<?>> getPages() {
        return pages;
    }

    /**
     * Adds a PaginatedInventory as a page to this PaginatedInventory
     * Page arrows will automatically be set on the bottom corner slots.
     *
     * @param builder The builder to add.
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T addPage(PaginatedInventory<T> builder) {
        this.getPages().add(builder);
        this.setPageArrows();

        //Update all pages with the new list of pages.
        for(PaginatedInventory<?> page : this.getPages()) {
            page.setPages(this.getPages());
            page.setPageArrows();
        }
        return (T) this;
    }

    /**
     * Adds ALL items into the CustomInventory.
     * If the current page is full, the CustomInventory will be copied
     * and items will begin to be placed there.
     *
     * @param items A list of {@link ItemBuilder}s to place into the inventory
     * @param supplier A supplier that should return a PaginatedInventory.
     *                 This Supplier will be used when a new Page needs to be added, and the supplied inventory will be added as a page.
     * @return The caller PaginatedInventory type instance, for chained builder purposes. 
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
     * @return The PaginatedInventory at that page index
     */
    public PaginatedInventory<?> getPage(int page) {
        try {
            return this.getPages().get(page);
        } catch (IndexOutOfBoundsException ignored) {
            throw new IndexOutOfBoundsException("Page does not exist!");
        }
    }

    /**
     * Forcefully places page arrows into an Inventory
     * @return The caller CustomInventory type instance, for chained builder purposes. 
     */
    @SuppressWarnings("unchecked")
    public T setPageArrows() {
        if(!this.isHideInvalidButton() || this.getPages().indexOf(this) != this.getPages().size() - 1) {
            this.setItem(this.getNextSlot(), new ItemBuilder(this.getNextButton())
                    .onClick((result) -> this.nextPage(result.getPlayer()))
                    .cancel());
        }

        if(!this.isHideInvalidButton() || this.getPages().indexOf(this) != 0) {
            this.setItem(this.getBackSlot(), new ItemBuilder(this.getBackButton())
                    .onClick((result) -> this.previousPage(result.getPlayer()))
                    .cancel());
        }

        return (T) this;
    }

    /**
     * Called when the Inventory's page changes.
     * @param consumer A BiFunction containing the Player that changed the page
     *                 and the new page.
     * @return The caller CustomInventory type instance, for chained builder purposes.
     */
    @SuppressWarnings("unchecked")
    public T onPageChange(BiConsumer<Player, PaginatedInventory<?>> consumer) {
        this.pageChange = consumer;
        return (T) this;
    }

    /**
     * Goes to the next page, if it exists, for this player.
     *
     * @param player The Player to advance the page for.
     */
    public void nextPage(Player player) {
        PaginatedInventory<?> newPage = this.getPages().indexOf(this) + 1 < this.getPages().size() ? this.getPages().get(this.getPages().indexOf(this) + 1) : null;

        if(newPage == null) return;

        this.executePageChange(player, newPage);
        this.removeHolder(player);
        newPage.open(player);
    }

    /**
     * Goes to the previous page, if it exists, for this player.
     *
     * @param player The Player to backtrack the page for.
     */
    public void previousPage(Player player) {
        PaginatedInventory<?> newPage = this.getPages().indexOf(this) - 1 >= 0 ? this.getPages().get(this.getPages().indexOf(this) - 1) : null;

        if(newPage == null) return;

        this.executePageChange(player, newPage);
        this.removeHolder(player);
        newPage.open(player);
    }

    /**
     * @return The {@link BiConsumer} that is consumed when the page is changed.
     */
    protected BiConsumer<Player, PaginatedInventory<?>> getPageChange() {
        return pageChange;
    }

    private void executePageChange(Player player, PaginatedInventory<?> newPage) {
        if (this.getPageChange() != null) {
            this.getPageChange().accept(player, newPage);
        }
    }

    private void setPages(List<PaginatedInventory<?>> pages) {
        this.pages = pages;
    }
}
