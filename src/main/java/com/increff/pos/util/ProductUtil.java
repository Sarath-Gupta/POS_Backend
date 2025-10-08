package com.increff.pos.util;

import com.increff.pos.commons.ApiException;
import com.increff.pos.model.form.ProductForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductUtil {
    public static List<ProductForm> parseTSV(MultipartFile file) throws ApiException {
        List<ProductForm> productForms = new ArrayList<>();

        // The maximum number of rows for file uploads must be limited to 5000.
        int rowCount = 0;
        final int MAX_ROWS = 5000;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                // Check for row limit immediately after reading the line
                if (rowCount++ > MAX_ROWS) {
                    throw new ApiException("File upload must be limited to " + MAX_ROWS + " rows maximum.");
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip the header row
                }

                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines

                // 1. Split the line by the tab character (\t)
                String[] tokens = line.split("\t");

                // 2. Validate token count (expecting 5 attributes)
                if (tokens.length != 5) {
                    throw new ApiException("Invalid number of columns in row " + rowCount + ". Expected 5 columns.");
                }

                // 3. Create the ProductForm and populate fields
                ProductForm productForm = new ProductForm();

                // --- Data Assignment ---
                // Note: Data is read as strings; validation and type conversion (for ClientId and MRP)
                // will happen later in the service layer using ClientUtil/ValidationUtil.

                productForm.setBarcode(tokens[0].trim());
                productForm.setName(tokens[1].trim());
                productForm.setClientId(Integer.valueOf(tokens[2].trim())); // Convert to Integer here for Form
                productForm.setMrp(Double.valueOf(tokens[3].trim()));       // Convert to Double here for Form
                productForm.setImgUrl(tokens[4].trim());

                productForms.add(productForm);
            }
        } catch (NumberFormatException e) {
            // Catch number conversion errors specifically
            throw new ApiException("Invalid number format in file: Check ClientId and MRP columns.");
        } catch (Exception e) {
            // Catch general IO/Parsing errors
            throw new ApiException("Failed to parse TSV file: " + e.getMessage());
        }

        return productForms;
    }
}
