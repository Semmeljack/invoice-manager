package com.business.unknow.services.util;

import static com.business.unknow.Constants.DATE_PRE_FOLIO_GENERIC_FORMAT;

import com.business.unknow.Constants;
import com.business.unknow.model.dto.FacturaPdf;
import com.business.unknow.model.error.InvoiceManagerException;
import com.google.common.io.Resources;
import com.unknown.error.PptUtilException;
import com.unknown.util.PdfGeneratorUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class FacturaUtils {

  public static String generatePreFolio(Integer amount) {
    return String.format(
        "%s-%s",
        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PRE_FOLIO_GENERIC_FORMAT)),
        String.format("%05d", amount + 1));
  }

  public static String generateFolio() {
    return LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern(Constants.DATE_FOLIO_GENERIC_FORMAT));
  }

  public static byte[] generateFacturaPdf(FacturaPdf facturaPdf, String type)
      throws InvoiceManagerException {
    try {
      String template =
          Resources.toString(
              FacturaUtils.class.getClassLoader().getResource(type), StandardCharsets.UTF_8);
      String pdf =
          PdfGeneratorUtil.generatePdf(
              Base64.getEncoder().encodeToString(template.getBytes()),
              "XSD",
              facturaPdf.toString());
      return Base64.getDecoder().decode(pdf);
    } catch (PptUtilException | IOException e) {
      log.error("Error en la generacion del PDF", e);
      throw new InvoiceManagerException(
          "Error en la generación del documento PDF", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
