package com.business.unknow.services.util.validators;

import com.business.unknow.Constants;
import com.business.unknow.model.error.InvoiceManagerException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

  protected static final String ATTRIBUTE_REQUIRED = "Attribute required.";
  protected static final String ATTRIBUTE_REQUIRED_MESSAGE = "Error, el campo [%s] no fue enviado.";
  protected static final String ATRIBUTE_NEGATIVE = "Error, el campo [%s] no puede ser negativo.";
  private static final String EMAIL_REGEX =
      "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
  private static final String CHAR_REGEX = "^[A-Za-z0-9ÁÉÍÓÚÑáéíóúñ.,:;!$%@#£€*+/°?&-_\"\\-\\s]+";

  public static void checkNotNull(Object var, String attribute) throws InvoiceManagerException {
    if (var == null) {
      throw new InvoiceManagerException(
          String.format(ATTRIBUTE_REQUIRED_MESSAGE, attribute),
          ATTRIBUTE_REQUIRED,
          Constants.BAD_REQUEST);
    }
  }

  public static void checkNotNegative(BigDecimal value, String attribute)
      throws InvoiceManagerException {
    if (BigDecimal.ZERO.compareTo(value) > 0) {
      throw new InvoiceManagerException(
          String.format(ATRIBUTE_NEGATIVE, attribute), ATTRIBUTE_REQUIRED, Constants.BAD_REQUEST);
    }
  }

  public static void checkNotEmpty(String var, String attribute) throws InvoiceManagerException {
    if (var.isEmpty()) {
      throw new InvoiceManagerException(
          String.format(ATTRIBUTE_REQUIRED_MESSAGE, attribute),
          ATTRIBUTE_REQUIRED,
          Constants.BAD_REQUEST);
    }
  }

  public static void checkNotEquals(String var, String comp) throws InvoiceManagerException {
    if (var.equals(comp)) {
      throw new InvoiceManagerException(
          String.format("La cadena %s no debe ser igual a %s", var, comp),
          ATTRIBUTE_REQUIRED,
          Constants.BAD_REQUEST);
    }
  }

  public static void checkValidEmail(String email) throws InvoiceManagerException {
    Pattern pattern = Pattern.compile(EMAIL_REGEX);
    Matcher matcher = pattern.matcher(email);
    if (!matcher.matches()) {
      throw new InvoiceManagerException(
          String.format("El correo %s: es incorrecto", email),
          String.format("El correo %s: es incorrecto", email),
          Constants.BAD_REQUEST);
    }
  }

  public static void checkValidString(String cadena) throws InvoiceManagerException {
    Pattern pattern = Pattern.compile(CHAR_REGEX);
    Matcher matcher = pattern.matcher(cadena);
    if (!matcher.matches()) {
      throw new InvoiceManagerException(
          String.format("La cadena %s: es incorrecta", cadena),
          String.format("La cadena %s: es incorrecta", cadena),
          Constants.BAD_REQUEST);
    }
  }
}
