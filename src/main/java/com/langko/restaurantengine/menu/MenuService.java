package com.langko.restaurantengine.menu;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.menu.dto.MenuCategoryRequest;
import com.langko.restaurantengine.menu.dto.MenuItemRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    public MenuService(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public MenuCategory createCategory(MenuCategoryRequest request) {
        MenuCategory category = new MenuCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        MenuCategory saved = categoryRepository.save(category);
        log.info("Created menu category: {}", saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<MenuItem> getAllItems(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return itemRepository.findByCategoryId(categoryId, pageable);
        }
        return itemRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public MenuItem getItemById(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    @Transactional
    public MenuItem createItem(MenuItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setAvailable(request.getAvailable());
        item.setCategory(category);
        MenuItem saved = itemRepository.save(item);
        log.info("Created menu item: {}", saved.getName());
        return saved;
    }

    @Transactional
    public MenuItem updateItem(Long id, MenuItemRequest request) {
        MenuItem item = getItemById(id);
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setAvailable(request.getAvailable());
        item.setCategory(category);
        return itemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem item = getItemById(id);
        itemRepository.delete(item);
        log.info("Deleted menu item: {}", id);
    }
}
