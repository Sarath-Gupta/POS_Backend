package com.increff.pos.dto;

import com.increff.pos.model.form.TsvRowResultProduct;
import com.increff.pos.service.ProductApi;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.commons.ApiException;
import com.increff.pos.model.data.ProductData;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.entity.Product;
import com.increff.pos.model.form.ProductUpdateForm;
import com.increff.pos.util.AbstractMapper;
import com.increff.pos.util.NormalizeUtil;
import com.increff.pos.util.ProductUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductDto {

    @Autowired
    ProductApi productApi;

    @Autowired
    ProductFlow productFlow;

    @Autowired
    AbstractMapper mapper;

    @Autowired
    private ValidationUtil validationUtil;

    public ProductData add(ProductForm productForm) throws ApiException {
        NormalizeUtil.normalize(productForm);
        validationUtil.validate(productForm);
        Product pojo = mapper.convert(productForm, Product.class);
        productFlow.add(pojo);
        return mapper.convert(pojo, ProductData.class);
    }

    public String processTsvWithRemarks(MultipartFile file) throws ApiException {
        List<ProductForm> productForms = ProductUtil.parseTSV(file);
        List<TsvRowResultProduct> results = new ArrayList<>();
        boolean allValid = true;
        for (ProductForm productForm : productForms) {
            TsvRowResultProduct result = new TsvRowResultProduct(productForm);
            try {
                NormalizeUtil.normalize(productForm);
                validationUtil.validate(productForm);
                Product product = mapper.convert(productForm, Product.class);
                productApi.add(product);
            } catch (ApiException e) {
                result.setRemarks(e.getMessage());
                allValid = false;
            }
            results.add(result);
        }

        if (allValid) {
            List<Product> productPojos = results.stream()
                    .map(r -> mapper.convert(r.form, Product.class))
                    .collect(Collectors.toList());
            try {
                for (Product productPojo : productPojos) {
                    productApi.add(productPojo);
                }
            } catch (ApiException e) {
                throw new ApiException("Critical Database/Service Error during insertion: " + e.getMessage());
            }
        }
        return convertResultsToTsvString(results);
    }

    private String convertResultsToTsvString(List<TsvRowResultProduct> results) {
        StringBuilder tsvContent = new StringBuilder();
        tsvContent.append("barcode\tclientId\tname\tmrp\tRemarks\n");
        for (TsvRowResultProduct result : results) {
            String outputRemarks = result.isValid() ? "" : result.remarks;
            tsvContent.append(result.form.getBarcode()).append("\t")
                    .append(result.form.getClientId()).append("\t")
                    .append(result.form.getName()).append("\t")
                    .append(result.form.getMrp()).append("\t")
                    .append(outputRemarks).append("\n");
        }
        return tsvContent.toString();
    }


    public ProductData findById(Integer id) throws ApiException {
        Product productPojo = productApi.findById(id);
        return mapper.convert(productPojo, ProductData.class);
    }

    public Page<ProductData> getAll(Pageable pageable) {
        Page<Product> productPage = productApi.getAll(pageable);
        return productPage.map(product -> mapper.convert(product, ProductData.class));
    }

    public ProductData update(Integer id, ProductUpdateForm productForm) throws ApiException {
        NormalizeUtil.normalize(productForm);
        validationUtil.validate(productForm);
        Product pojo = mapper.convert(productForm, Product.class);
        Product updatedPojo = productApi.update(id,pojo);
        return mapper.convert(updatedPojo, ProductData.class);
    }
}
