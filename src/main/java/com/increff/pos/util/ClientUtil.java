package com.increff.pos.util;

import com.increff.pos.model.form.ClientForm;
import com.increff.pos.commons.ApiException;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class ClientUtil {
    public static List<ClientForm> parseTSV(MultipartFile file) throws ApiException {
        return TsvUtil.parseTSV(file, (tokens, rowNum) -> {
            final int EXPECTED_COLUMNS = 1;
            if (tokens.length != EXPECTED_COLUMNS) {
                throw new ApiException("Invalid columns in row " + rowNum + ". Expected " + EXPECTED_COLUMNS);
            }
            ClientForm form = new ClientForm();
            form.setClientName(tokens[0].trim());
            return form;
        });
    }
}
