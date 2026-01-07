package com.shopjoy.service;

import com.shopjoy.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper representing a node in the category hierarchy.
 * Each node contains a `rootCategory` and zero or more children.
 */
public class CategoryTree {
    private final Category rootCategory;
    private final List<CategoryTree> children = new ArrayList<>();

    public CategoryTree(Category rootCategory) {
        this.rootCategory = rootCategory;
    }

    public Category getRootCategory() {
        return rootCategory;
    }

    public void addChild(CategoryTree child) {
        if (child != null) children.add(child);
    }

    public List<CategoryTree> getChildren() {
        return new ArrayList<>(children);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}
