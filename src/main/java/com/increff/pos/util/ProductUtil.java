package com.increff.pos.util;

import com.increff.pos.commons.ApiException;
import com.increff.pos.model.form.ProductForm;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


public class ProductUtil {

    public static List<ProductForm> parseTSV(MultipartFile file) throws ApiException {
        return TsvUtil.parseTSV(file, (tokens, rowNum) -> {
            final int EXPECTED_COLUMNS = 5;
            if (tokens.length != EXPECTED_COLUMNS) {
                throw new ApiException("Invalid columns in row " + rowNum + ". Expected " + EXPECTED_COLUMNS);
            }
            ProductForm form = new ProductForm();
            form.setBarcode(tokens[0].trim());
            form.setName(tokens[1].trim());
            try {
                form.setClientId(Integer.valueOf(tokens[2].trim()));
                form.setMrp(Double.valueOf(tokens[3].trim()));
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid number format in row " + rowNum + ". Check Client ID and MRP.");
            }
            form.setImgUrl(tokens[4].trim());
            return form;
        });
    }
}
