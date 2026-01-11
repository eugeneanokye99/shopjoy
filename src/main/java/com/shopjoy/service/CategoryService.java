package com.shopjoy.service;

import com.shopjoy.dao.CategoryDAO;
import com.shopjoy.cache.CategoryCache;
import com.shopjoy.dao.ProductDAO;
import com.shopjoy.model.Category;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for category management and business rules.
 *
 * Category hierarchy logic: categories may reference a parent via
 * `parentCategoryId`.
 * Top-level categories have `parentCategoryId == null`. The service builds a
 * `CategoryTree` by retrieving top-level categories and recursively fetching
 * their subcategories via `CategoryDAO.findSubcategories()`.
 */
public class CategoryService {
    private final CategoryDAO categoryDAO;
    private final ProductDAO productDAO;
    private final CategoryCache categoryCache = CategoryCache.getInstance();

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.productDAO = new ProductDAO();
    }

    /**
     * Return all categories.
     */
    public List<Category> getAllCategories() {
        // Check cache first
        if (categoryCache.isCacheValid() && categoryCache.getCacheSize() > 0) {
            List<Category> cached = categoryCache.getAllCachedCategories();
            if (cached != null) {
                System.out.println("Returning categories from cache");
                return cached;
            }
        }

        try {
            List<Category> categories = categoryDAO.findAll();
            if (categories != null) {
                categoryCache.cacheCategoryList(categories);
                System.out.println("Categories loaded from database and cached");
            }
            return categories;
        } catch (SQLException e) {
            System.err.println("getAllCategories: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get category by id, or null if invalid/not found.
     */
    public Category getCategoryById(int categoryId) {
        if (categoryId <= 0)
            return null;
        // Check cache first
        Category cached = categoryCache.getCategory(categoryId);
        if (cached != null) {
            return cached;
        }

        try {
            Category category = categoryDAO.findById(categoryId);
            if (category != null) {
                categoryCache.cacheCategory(category);
                System.out.println("Category " + categoryId + " loaded from database and cached");
            }
            return category;
        } catch (SQLException e) {
            System.err.println("getCategoryById: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get category by exact name.
     */
    public Category getCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty())
            return null;
        try {
            return categoryDAO.findByName(categoryName.trim());
        } catch (SQLException e) {
            System.err.println("getCategoryByName: " + e.getMessage());
            return null;
        }
    }

    /**
     * Return top-level categories (no parent).
     */
    public List<Category> getTopLevelCategories() {
        // Check cache first
        List<Category> cached = categoryCache.getTopLevelCategories();
        if (cached != null) {
            System.out.println("Top-level categories retrieved from cache");
            return cached;
        }

        try {
            List<Category> categories = categoryDAO.findTopLevelCategories();
            if (categories != null) {
                categoryCache.cacheTopLevelCategories(categories);
                System.out.println("Top-level categories loaded from database and cached");
            }
            return categories;
        } catch (SQLException e) {
            System.err.println("getTopLevelCategories: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get direct subcategories of a parent category.
     */
    public List<Category> getSubcategories(int parentCategoryId) {
        if (parentCategoryId <= 0)
            return new ArrayList<>();
        // Check cache first
        List<Category> cached = categoryCache.getSubcategories(parentCategoryId);
        if (cached != null) {
            System.out.println("Subcategories for category " + parentCategoryId + " retrieved from cache");
            return cached;
        }

        try {
            List<Category> subs = categoryDAO.findSubcategories(parentCategoryId);
            if (subs != null) {
                categoryCache.cacheSubcategories(parentCategoryId, subs);
                System.out
                        .println("Subcategories for category " + parentCategoryId + " loaded from database and cached");
            }
            return subs;
        } catch (SQLException e) {
            System.err.println("getSubcategories: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Add a new category. Returns saved Category or null on validation/error.
     */
    public Category addCategory(String categoryName, String description, Integer parentCategoryId) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.err.println("addCategory: name required");
            return null;
        }
        String name = categoryName.trim();
        if (name.length() > 100) {
            System.err.println("addCategory: name too long");
            return null;
        }
        try {
            // name must be unique
            Category existing = categoryDAO.findByName(name);
            if (existing != null)
                return null;

            // if parent specified, validate it exists
            if (parentCategoryId != null) {
                Category parent = categoryDAO.findById(parentCategoryId);
                if (parent == null) {
                    System.err.println("addCategory: parent not found");
                    return null;
                }
            }

            Category c = new Category();
            c.setCategoryName(name);
            c.setDescription(description);
            c.setParentCategoryId(parentCategoryId);
            c.setCreatedAt(LocalDateTime.now());

            try {
                Category saved = categoryDAO.save(c);
                if (saved != null) {
                    categoryCache.invalidateCache();
                    System.out.println("Category cache invalidated after adding new category");
                }
                return saved;
            } catch (SQLException e) {
                System.err.println("addCategory: save error: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("addCategory: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update an existing category. Returns updated or null.
     */
    public Category updateCategory(Category category) {
        if (category == null || category.getCategoryId() <= 0)
            return null;
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty())
            return null;
        String name = category.getCategoryName().trim();
        if (name.length() > 100)
            return null;
        try {
            Category existing = categoryDAO.findById(category.getCategoryId());
            if (existing == null)
                return null;

            Category byName = categoryDAO.findByName(name);
            if (byName != null && byName.getCategoryId() != category.getCategoryId()) {
                // another category with same name exists
                return null;
            }

            try {
                category.setCategoryName(name);
                Category updated = categoryDAO.update(category);
                if (updated != null) {
                    categoryCache.invalidateCategory(category.getCategoryId());
                    System.out.println("Cache invalidated for category " + category.getCategoryId());
                }
                return updated;
            } catch (SQLException e) {
                System.err.println("updateCategory: update error: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("updateCategory: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete a category if it has no products or subcategories.
     */
    public boolean deleteCategory(int categoryId) {
        if (categoryId <= 0)
            return false;
        try {
            // cannot delete if products exist
            if (categoryDAO.hasProducts(categoryId))
                return false;

            // cannot delete if subcategories exist
            List<Category> subs = categoryDAO.findSubcategories(categoryId);
            if (subs != null && !subs.isEmpty())
                return false;

            try {
                boolean success = categoryDAO.delete(categoryId);
                if (success) {
                    categoryCache.invalidateCache();
                    System.out.println("Category cache invalidated after deletion");
                }
                return success;
            } catch (SQLException e) {
                System.err.println("deleteCategory: delete error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("deleteCategory: " + e.getMessage());
            return false;
        }
    }

    /**
     * Return count of products in a category.
     */
    public int getProductCount(int categoryId) {
        if (categoryId <= 0)
            return 0;
        try {
            return categoryDAO.getProductCount(categoryId);
        } catch (SQLException e) {
            System.err.println("getProductCount: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Return categories that have products.
     */
    public List<Category> getCategoriesWithProducts() {
        try {
            return categoryDAO.findCategoriesWithProducts();
        } catch (SQLException e) {
            System.err.println("getCategoriesWithProducts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check whether category has subcategories.
     */
    public boolean hasSubcategories(int categoryId) {
        if (categoryId <= 0)
            return false;
        try {
            List<Category> subs = categoryDAO.findSubcategories(categoryId);
            return subs != null && !subs.isEmpty();
        } catch (SQLException e) {
            System.err.println("hasSubcategories: " + e.getMessage());
            return false;
        }
    }

    /**
     * Build and return a tree of categories.
     */
    public List<CategoryTree> getCategoryTree() {
        List<CategoryTree> roots = new ArrayList<>();
        try {
            List<Category> top = categoryDAO.findTopLevelCategories();
            if (top == null)
                return roots;
            for (Category c : top) {
                CategoryTree node = buildTreeRecursive(c);
                roots.add(node);
            }
            return roots;
        } catch (SQLException e) {
            System.err.println("getCategoryTree: " + e.getMessage());
            return roots;
        }
    }

    /**
     * Recursively build CategoryTree for a category.
     */
    private CategoryTree buildTreeRecursive(Category category) {
        CategoryTree node = new CategoryTree(category);
        try {
            List<Category> children = getSubcategories(category.getCategoryId());
            if (children != null) {
                for (Category child : children) {
                    node.addChild(buildTreeRecursive(child));
                }
            }
        } catch (Exception e) {
            System.err.println("buildTreeRecursive: " + e.getMessage());
        }
        return node;
    }
}
