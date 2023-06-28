package com.business.unknow.enums;

public enum FormaPago {
  CHEQUE_NORMATIVO(1, "Cheque nominativo", "CHEQUE", "02"),
  TRANSFERENCIA_ELECTRONICA(2, "Transferencia electrónica de fondos", "TRANSFERENCIA", "03"),
  EFECTIVO(3, "Efectivo", "EFECTIVO", "01"),
  DEPOSITO(4, "Deposito Bancario", "DEPOSITO", "02"),
  CREDITO(5, "Credito Despacho", "CREDITO", "12"),
  NOT_VALID(6, "NOT_VALID", "NOT_VALID", "00");

  private Integer valor;
  private String descripcion;
  private String pagoValue;
  private String clave;

  FormaPago(Integer valor, String descripcion, String pagoValue, String clave) {
    this.valor = valor;
    this.descripcion = descripcion;
    this.pagoValue = pagoValue;
    this.clave = clave;
  }

  public static FormaPago findByDesc(String descripcion) {
    for (FormaPago v : values()) {
      if (v.getDescripcion().equals(descripcion)) {
        return v;
      }
    }
    return NOT_VALID;
  }

  public static FormaPago findByPagoValue(String pagoValue) {
    for (FormaPago v : values()) {
      if (v.getPagoValue().equals(pagoValue)) {
        return v;
      }
    }
    return NOT_VALID;
  }

  public static FormaPago findByPagoClave(String clave) {
    for (FormaPago v : values()) {
      if (v.getClave().equals(clave)) {
        return v;
      }
    }
    return NOT_VALID;
  }

  public Integer getValor() {
    return valor;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getPagoValue() {
    return pagoValue;
  }

  public String getClave() {
    return clave;
  }
}
