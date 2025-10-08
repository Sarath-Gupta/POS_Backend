package com.increff.pos.util;

import com.increff.pos.model.form.ClientForm;
import com.increff.pos.commons.ApiException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientUtil {

    public static List<ClientForm> parseTSV(MultipartFile file) throws ApiException {
        List<ClientForm> clientForms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                ClientForm clientForm = new ClientForm();
                clientForm.setClientName(line);
                clientForms.add(clientForm);
            }
        } catch (Exception e) {
            throw new ApiException("Failed to parse TSV file: " + e.getMessage());
        }
        return clientForms;
    }
}
