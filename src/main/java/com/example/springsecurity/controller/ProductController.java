package com.example.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.springsecurity.model.Product;
import com.example.springsecurity.service.ProductService;
import com.example.springsecurity.service.CategoryService;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(Model model) {
        List<Product> productList = productService.getAllProducts();
        model.addAttribute("products", productList);
        return "product/list"; 
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "product/add";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("category") Long categoryId) {
        product.setCategory(categoryService.getCategoryById(categoryId));
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @PostMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {

        Product product = productService.getProductById(id);

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "product/edit";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute("product") Product product,
                                @RequestParam("category") Long categoryId) {

        product.setId(id);
        product.setCategory(categoryService.getCategoryById(categoryId));

        productService.saveProduct(product);

        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {

        productService.deleteProduct(id);

        return "redirect:/products";
    }
}
