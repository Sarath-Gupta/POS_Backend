package com.increff.pos.util;

import com.increff.pos.commons.ApiException;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TsvUtil {

    private static final int MAX_ROWS = 5000;
    public static <T> List<T> parseTSV(MultipartFile file, TsvMapper<T> mapper) throws ApiException {
        List<T> forms = new ArrayList<>();
        int rowNumber = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (rowNumber == 1) continue;
                if (rowNumber > MAX_ROWS + 1) {
                    throw new ApiException("File upload limit exceeded. Maximum " + MAX_ROWS + " data rows allowed.");
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] tokens = line.split("\t", -1);
                T form = mapper.mapTokensToForm(tokens, rowNumber);
                forms.add(form);
            }
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Failed to parse TSV file near row " + rowNumber + ": " + e.getMessage());
        }
        return forms;
    }
}