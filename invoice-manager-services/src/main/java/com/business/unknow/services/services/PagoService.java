package com.business.unknow.services.services;

import com.business.unknow.enums.FacturaStatus;
import com.business.unknow.enums.FormaPago;
import com.business.unknow.enums.MetodosPago;
import com.business.unknow.enums.RevisionPagos;
import com.business.unknow.enums.TipoArchivo;
import com.business.unknow.enums.TipoDocumento;
import com.business.unknow.model.dto.FacturaCustom;
import com.business.unknow.model.dto.PagoComplemento;
import com.business.unknow.model.dto.files.ResourceFileDto;
import com.business.unknow.model.dto.pagos.PagoDto;
import com.business.unknow.model.dto.pagos.PagoFacturaDto;
import com.business.unknow.model.error.InvoiceManagerException;
import com.business.unknow.services.entities.Pago;
import com.business.unknow.services.entities.PagoFactura;
import com.business.unknow.services.mapper.PagoMapper;
import com.business.unknow.services.repositories.PagoFacturaRepository;
import com.business.unknow.services.repositories.PagoRepository;
import com.business.unknow.services.util.validators.PagoValidator;
import com.unknown.error.PptUtilException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class PagoService {
  @Autowired private FilesService filesService;

  @Autowired private FacturaService facturaService;

  @Autowired private CfdiService cfdiService;

  @Autowired private DownloaderService downloaderService;

  @Autowired private PagoRepository repository;

  @Autowired private PagoFacturaRepository facturaPagosRepository;

  @Autowired private PagoMapper mapper;

  private Page<Pago> paymentsSearch(
      Optional<String> solicitante,
      Optional<String> acredor,
      Optional<String> deudor,
      String formaPago,
      String status,
      String banco,
      Date since,
      Date to,
      int page,
      int size) {

    Date start = (since == null) ? new DateTime().minusYears(1).toDate() : since;
    Date end = (to == null) ? new Date() : to;
    Page<Pago> result = null;
    if (solicitante.isPresent()) {
      result =
          repository.findBySolicitanteIgnoreCaseContaining(
              solicitante.get(),
              PageRequest.of(page, size, Sort.by("fechaActualizacion").descending()));
    } else if (acredor.isPresent()) {
      result =
          repository.findPagosAcredorFilteredByParams(
              String.format("%%%s%%", acredor.get()),
              String.format("%%%s%%", status),
              String.format("%%%s%%", formaPago),
              String.format("%%%s%%", banco),
              start,
              end,
              PageRequest.of(page, size, Sort.by("fechaActualizacion").descending()));
    } else if (deudor.isPresent()) {
      result =
          repository.findPagosDeudorFilteredByParams(
              String.format("%%%s%%", deudor.get()),
              String.format("%%%s%%", status),
              String.format("%%%s%%", formaPago),
              String.format("%%%s%%", banco),
              start,
              end,
              PageRequest.of(page, size, Sort.by("fechaActualizacion").descending()));
    } else {
      result =
          repository.findPagosFilteredByParams(
              String.format("%%%s%%", status),
              String.format("%%%s%%", formaPago),
              String.format("%%%s%%", banco),
              start,
              end,
              PageRequest.of(page, size, Sort.by("fechaActualizacion").descending()));
    }
    return result;
  }

  public Page<PagoDto> getPaginatedPayments(
      Optional<String> solicitante,
      Optional<String> acredor,
      Optional<String> deudor,
      String formaPago,
      String status,
      String banco,
      Date since,
      Date to,
      int page,
      int size) {

    Page<Pago> result =
        paymentsSearch(
            solicitante, acredor, deudor, formaPago, status, banco, since, to, page, size);

    return new PageImpl<>(
        mapper.getPagosDtoFromEntities(result.getContent()),
        result.getPageable(),
        result.getTotalElements());
  }

  public ResourceFileDto getPaymentsReport(
      Optional<String> solicitante,
      Optional<String> acredor,
      Optional<String> deudor,
      String formaPago,
      String status,
      String banco,
      Date since,
      Date to,
      int page,
      int size)
      throws IOException {

    Page<Pago> result =
        paymentsSearch(
            solicitante, acredor, deudor, formaPago, status, banco, since, to, page, size);

    List<String> headers =
        Arrays.asList(
            "ID",
            "ACREDOR",
            "DEUDOR",
            "MONEDA",
            "BANCO",
            "CUENTA",
            "TIPO CAMBIO",
            "FORMA PAGO",
            "MONTO",
            "FECHA PAGO",
            "ESTATUS",
            "COMENTARIO",
            "SOLICITANTE",
            "REVISOR 1",
            "REVISOR 2",
            "CREACION",
            "ACTUALIZACION");

    List<Map<String, Object>> data =
        result.getContent().stream()
            .map(
                pago -> {
                  Map<String, Object> row = new HashMap<>();
                  row.put("ID", pago.getId());
                  row.put("ACREDOR", pago.getAcredor());
                  row.put("DEUDOR", pago.getDeudor());
                  row.put("MONEDA", pago.getMoneda());
                  row.put("BANCO", pago.getBanco());
                  row.put("CUENTA", pago.getCuenta());
                  row.put("TIPO CAMBIO", pago.getTipoDeCambio());
                  row.put("FORMA PAGO", pago.getFormaPago());
                  row.put("MONTO", pago.getMonto());
                  row.put("FECHA PAGO", pago.getFechaPago());
                  row.put("ESTATUS", pago.getStatusPago());
                  row.put("COMENTARIO", pago.getComentarioPago());
                  row.put("SOLICITANTE", pago.getSolicitante());
                  row.put("REVISOR 1", pago.getRevisor1());
                  row.put("REVISOR 2", pago.getRevisor2());
                  row.put("CREACION", pago.getFechaCreacion());
                  row.put("ACTUALIZACION", pago.getFechaActualizacion());
                  return row;
                })
            .collect(Collectors.toList());

    return downloaderService.generateBase64Report("Pagos", data, headers);
  }

  public List<PagoDto> findPagosByFolio(String folio) {
    return mapper.getPagosDtoFromEntities(repository.findPagosByFolio(folio));
  }

  public PagoDto getPaymentById(Integer id) throws InvoiceManagerException {
    Optional<Pago> payment = repository.findById(id);
    if (payment.isPresent()) {
      return mapper.getPagoDtoFromEntity(payment.get());
    } else {
      throw new InvoiceManagerException(
          "Pago no encontrado",
          String.format("El pago con id %d no fu encontrado.", id),
          HttpStatus.NOT_FOUND.value());
    }
  }

  @Transactional(
      rollbackOn = {InvoiceManagerException.class, DataAccessException.class, SQLException.class})
  public PagoDto insertNewPayment(PagoDto pagoDto)
      throws InvoiceManagerException, PptUtilException {
    PagoValidator.validate(pagoDto);
    log.info("Creating a new payment [{}]", pagoDto);
    List<FacturaCustom> facturas = new ArrayList<>();
    for (PagoFacturaDto pagoFact : pagoDto.getFacturas()) {
      FacturaCustom factura = facturaService.getFacturaByFolio(pagoFact.getFolio());
      facturas.add(factura);
      pagoFact.setRfcEmisor(factura.getRfcEmisor());
      pagoFact.setRfcReceptor(factura.getRfcRemitente());
      pagoFact.setSolicitante(factura.getSolicitante());
      pagoFact.setAcredor(factura.getRazonSocialEmisor());
      pagoFact.setDeudor(factura.getRazonSocialRemitente());
      pagoFact.setTotalFactura(factura.getTotal());
      pagoFact.setMetodoPago(factura.getMetodoPago());
      if (MetodosPago.PUE.name().equals(factura.getMetodoPago())) {
        pagoFact.setIdCfdi(factura.getIdCfdi());
      }
    }

    List<FacturaCustom> factPpd =
        facturas.stream()
            .filter(f -> MetodosPago.PPD.name().equals(f.getMetodoPago()))
            .collect(Collectors.toList());

    List<FacturaCustom> factPue =
        facturas.stream()
            .filter(f -> MetodosPago.PUE.name().equals(f.getMetodoPago()))
            .collect(Collectors.toList());

    if (!factPpd.isEmpty()) {
      log.info(
          "Generando complemento para : {}",
          factPpd.stream().map(f -> f.getFolio()).collect(Collectors.toList()));
      FacturaCustom facturaCustomComeplemento =
          facturaService.generateComplemento(facturas, pagoDto, FacturaStatus.VALIDACION_TESORERIA);
      pagoDto
          .getFacturas()
          .forEach(a -> a.setFolioReferencia(facturaCustomComeplemento.getFolio()));
    }
    if (!factPue.isEmpty()) {
      pagoDto.getFacturas().forEach(a -> a.setFolioReferencia(a.getFolio()));
    }
    for (FacturaCustom dto : facturas) {
      if (!FormaPago.CREDITO.getPagoValue().equals(pagoDto.getFormaPago())
          && dto.getTipoDocumento().equals(TipoDocumento.FACTURA.getDescripcion())
          && dto.getMetodoPago().equals(MetodosPago.PUE.name())) {

        PagoFacturaDto pagoFact =
            pagoDto.getFacturas().stream()
                .filter(p -> p.getFolio().equals(dto.getFolio()))
                .findAny()
                .orElseThrow(
                    () ->
                        new InvoiceManagerException(
                            String.format(
                                "La factura con folio {} no existe en el sistema, intente de nuevo mas tarde",
                                dto.getFolio()),
                            HttpStatus.CONTINUE.value()));
        BigDecimal paidAmount =
            findPagosByFolio(dto.getFolio()).stream()
                .filter(
                    p -> !"CREDITO".equals(p.getFormaPago())) // removes credito despacho payments
                .map(
                    p ->
                        dto.getCfdi().getMoneda().equals(p.getMoneda())
                            ? p.getMonto()
                            : p.getMonto().divide(p.getTipoDeCambio(), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, (p1, p2) -> p1.add(p2))
                .add(pagoFact.getMonto());
        log.info("Updating saldo pendiente factura {}", dto.getFolio());
        facturaService.updateSaldoFactura(dto, paidAmount);
      }
    }
    if (FormaPago.CREDITO.getPagoValue().equals(pagoDto.getFormaPago()) && facturas.size() == 1) {
      Optional<FacturaCustom> currentFactura = facturas.stream().findFirst();
      if (currentFactura.isPresent()
          && currentFactura.get().getMetodoPago().equals(MetodosPago.PUE.name())) {
        FacturaCustom fact = facturaService.getFacturaByFolio(currentFactura.get().getFolio());
        fact.setValidacionTeso(Boolean.TRUE);
        facturaService.updateFacturaCustom(currentFactura.get().getFolio(), fact);
        pagoDto.setStatusPago("ACEPTADO");
        pagoDto.setRevision1(true);
        pagoDto.setRevision2(true);
      }
    }
    Pago payment = repository.save(mapper.getEntityFromPagoDto(pagoDto));
    for (PagoFacturaDto fact : pagoDto.getFacturas()) {
      PagoFactura pagoFact = mapper.getEntityFromPagoFacturaDto(fact);
      pagoFact.setPago(payment);
      payment.addFactura(facturaPagosRepository.save(pagoFact));
    }
    return mapper.getPagoDtoFromEntity(payment);
  }

  @Transactional(
      rollbackOn = {InvoiceManagerException.class, DataAccessException.class, SQLException.class})
  public PagoDto updatePago(Integer idPago, PagoDto pago)
      throws InvoiceManagerException, PptUtilException {
    PagoDto entity = getPaymentById(idPago);
    PagoDto pagoDto =
        entity.toBuilder()
            .revision1(pago.getRevision1())
            .revision2(pago.getRevision2())
            .revisor1(pago.getRevisor1())
            .revisor2(pago.getRevisor2())
            .build();
    PagoValidator.validate(pago);

    List<FacturaCustom> facturas = new ArrayList<>();
    for (PagoFacturaDto pagoFact :
        pago.getFacturas().stream().distinct().collect(Collectors.toList())) {
      FacturaCustom factura = facturaService.getFacturaByFolio(pagoFact.getFolioReferencia());
      facturas.add(factura);
    }

    if (pago.getStatusPago().equals(RevisionPagos.RECHAZADO.name())) {
      pagoDto.setStatusPago(RevisionPagos.RECHAZADO.name());
      pagoDto.setComentarioPago(pago.getComentarioPago());
      for (FacturaCustom factura : facturas) {
        log.info("Rejecting payment of invoice: {}", factura.getFolio());
        if (MetodosPago.PUE.getClave().equals(factura.getMetodoPago())) {
          factura.setStatusFactura(FacturaStatus.RECHAZO_TESORERIA.getValor());
          factura.setStatusDetail(pago.getComentarioPago());
          facturaService.updateFacturaCustom(factura.getFolio(), factura);
        }
      }
    } else if (entity.getRevision1() && pago.getRevision2()) {
      pagoDto.setStatusPago(RevisionPagos.ACEPTADO.name());
      for (FacturaCustom fact : facturas) {
        log.info("Payment approved for invoice: {}", fact.getFolio());
        fact.setValidacionTeso(true);
        facturaService.updateFacturaCustom(fact.getFolio(), fact);
      }
    }
    return mapper.getPagoDtoFromEntity(repository.save(mapper.getEntityFromPagoDto(pagoDto)));
  }

  @Transactional(
      rollbackOn = {InvoiceManagerException.class, DataAccessException.class, SQLException.class})
  public void deletePago(Integer idPago) throws InvoiceManagerException, PptUtilException {
    PagoDto entity = getPaymentById(idPago);
    List<FacturaCustom> facturas = new ArrayList<>();
    List<String> mainFactFolios =
        entity.getFacturas().stream()
            .map(f -> f.getFolioReferencia())
            .distinct()
            .collect(Collectors.toList());
    for (String folio : mainFactFolios) {
      log.warn("Deleting payment of {} from invoice {}", entity.getMonto(), folio);
      if (Objects.nonNull(folio)) {
        FacturaCustom f = facturaService.getFacturaBaseByFolio(folio);
        facturas.add(f);
      } else {
        throw new InvoiceManagerException(
            "El pago tiene inconsistencias en las referencias del complemento de pago, contactar al area de soporte",
            HttpStatus.CONFLICT.value());
      }
    }
    for (FacturaCustom mainFact : facturas) {
      if (TipoDocumento.COMPLEMENTO.getDescripcion().equals(mainFact.getTipoDocumento())) {
        // removing complements data
        List<PagoFacturaDto> pagos =
            entity.getFacturas().stream()
                .filter(p -> p.getFolioReferencia().equals(mainFact.getFolio()))
                .collect(Collectors.toList());

        for (PagoFacturaDto pago : pagos) {
          FacturaCustom fact = facturaService.getFacturaByFolio(pago.getFolio());
          log.info(
              "Removing Complement references of {} from PPD parent {}",
              mainFact.getFolio(),
              pago.getFolio());
          fact.setSaldoPendiente(fact.getSaldoPendiente().add(entity.getMonto()));

          Optional<PagoComplemento> pagoComplemento =
              fact.getPagos().stream()
                  .filter(p -> p.getFolio().equals(mainFact.getFolio()))
                  .findAny();
          if (pagoComplemento.isPresent()) {
            fact.getPagos().remove(pagoComplemento.get());
            facturaService.updateFacturaCustom(fact.getFolio(), fact);
          } else {
            throw new InvoiceManagerException(
                "Incosistencias en los complementos de pago de la factura origen, no se elimino el complemento",
                HttpStatus.CONFLICT.value());
          }
        }
        // deleting all complement references
        facturaService.deleteFactura(mainFact.getFolio());
        filesService.deleteFacturaFile(mainFact.getFolio(), TipoArchivo.PDF.name());
        filesService.deleteFacturaFile(mainFact.getFolio(), TipoArchivo.XML.name());
      } else {
        Optional<PagoFacturaDto> pagoFactOpt =
            entity.getFacturas().stream()
                .filter(p -> p.getFolio().equals(mainFact.getFolio()))
                .findAny();
        if (pagoFactOpt.isPresent()
            && pagoFactOpt.get().getFolioReferencia().equals(mainFact.getFolio())) {

          List<PagoDto> pagosFact =
              findPagosByFolio(mainFact.getFolio()).stream()
                  .filter(p -> !p.getId().equals(idPago)) // removes pago to be deleted
                  .filter(
                      p -> !"CREDITO".equals(p.getFormaPago())) // removes credito despacho payments
                  .collect(Collectors.toList());
          FacturaCustom pueFact = facturaService.getFacturaByFolio(mainFact.getFolio());
          BigDecimal paidAmount = BigDecimal.ZERO;
          if (!pagosFact.isEmpty()) { // if pagosFact is empty means nothing was paid
            paidAmount =
                pagosFact.stream()
                    .map(
                        p ->
                            pueFact.getCfdi().getMoneda().equals(p.getMoneda())
                                ? p.getMonto()
                                : p.getMonto().divide(p.getTipoDeCambio(), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, (p1, p2) -> p1.add(p2));

            pueFact.setSaldoPendiente(pueFact.getCfdi().getTotal().subtract(paidAmount));
          }
          facturaService.updateSaldoFactura(pueFact, paidAmount);
        } else {
          throw new ResponseStatusException(
              HttpStatus.CONFLICT, "Existe alguna inconsistencia en las referencias de los pagos");
        }
      }
    }
    filesService.deleteResourceFileByResourceReferenceAndType(
        "PAGOS", idPago.toString(), TipoArchivo.IMAGEN.name());
    repository.delete(mapper.getEntityFromPagoDto(entity));
  }

  public void delePagoFacturas(int id) {
    for (PagoFactura pagoFactura : facturaPagosRepository.findByPagoId(id)) {
      facturaPagosRepository.deleteById(pagoFactura.getId());
    }
  }
}
