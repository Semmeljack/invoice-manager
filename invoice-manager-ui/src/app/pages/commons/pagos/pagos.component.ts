import { Component, OnInit } from '@angular/core';

import { GenericPage } from '../../../models/generic-page';
import { PaymentsData } from '../../../@core/data/payments-data';
import { NbDialogService } from '@nebular/theme';
import { ValidacionPagoComponent } from './validacion-pago/validacion-pago.component';
import { PagoBase } from '../../../models/pago-base';
import { Router } from '@angular/router';
import { User } from '../../../@core/models/user';
import { DonwloadFileService } from '../../../@core/util-services/download-file-service';
import { NotificationsService } from '../../../@core/util-services/notifications.service';
import { NtError } from '../../../@core/models/nt-error';

@Component({
  selector: 'ngx-pagos',
  templateUrl: './pagos.component.html',
  styleUrls: ['./pagos.component.scss'],
})
export class PagosComponent implements OnInit {

  public user: User;
  public vista: string = 'validacion-pagos';
  public module: string = 'operaciones';
  public filterParams: any = { formaPago: '*', status: 'VALIDACION', acredor: '', deudor: '', since: '', to: '' };
  public page: GenericPage<any> = new GenericPage();
  public pageSize = '10';

  constructor(
    private paymentService: PaymentsData,
    private downloadService: DonwloadFileService,
    private router: Router,
    private dialogService: NbDialogService,
    private notificationService: NotificationsService
  ) { }

  ngOnInit() {

    this.user = JSON.parse(sessionStorage.getItem('user'));
    this.vista = this.router.url.split('/')[3];
    this.module = this.router.url.split('/')[2];
    if (this.vista === 'validacion-pagos') {
      this.filterParams.status = 'VALIDACION';
    }
    if (this.vista === 'historial-pagos') {
      this.filterParams.status = 'ACEPTADO';
    }
    this.updateDataTable();
  }

  public updateDataTable(currentPage?: number, pageSize?: number) {
    const pageValue = currentPage || 0;
    const sizeValue = pageSize || 10;
    this.paymentService.getAllPayments(pageValue, sizeValue, this.filterParams)
      .subscribe((result: GenericPage<any>) => { this.page = result; });
  }

  public onChangePageSize(pageSize: number) {
    this.updateDataTable(this.page.number, pageSize);
  }

  public downloadHandler() {
    this.paymentService.getPaymentsReport(0, 10000, this.filterParams).subscribe(result => {
      this.downloadService.downloadFile(result.data,'Pagos.xlsx','data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,');
    });
  }

  public validar1(pago: PagoBase) {
    if (pago.solicitante !== this.user.email) {
      pago.revision1 = true;
      pago.revisor1 = this.user.email;
      this.paymentService.updatePaymentWithValidation(pago.id, pago)
        .subscribe(updatedPayment => pago = updatedPayment,
          (error: NtError) => this.notificationService.sendNotification('danger', error?.message,'Error validando el pago'));
    } else {
      this.notificationService.sendNotification('warning', 'El solicitante del pago no puede validar el pago.','Error validando el pago');
    }
  }

  public validar2(pago: PagoBase) {
    if (pago.solicitante !== this.user.email && pago.revisor1 !== this.user.email) {
      pago.revision2 = true;
      pago.revisor2 = this.user.email;
      this.paymentService.updatePaymentWithValidation(pago.id, pago)
        .subscribe(updatedPayment => pago = updatedPayment,
          (error: NtError) =>this.notificationService.sendNotification('danger', error?.message,'Error validando el pago'));
    } else {
      this.notificationService.sendNotification('warning','El segundo revisor, no puede ser ni el solicitante ni el primer revisor.','Error validando el pago');
    }
  }


  openDialog(payment: PagoBase) {
    this.dialogService.open(ValidacionPagoComponent, {
      context: {
        pago: payment,
      },
    }).onClose.subscribe(pago => {
      if (pago !== undefined) {
        if (pago.revisor2 === undefined) {
          pago.revisor2 = this.user.email;
        } else {
          pago.revisor1 = this.user.email;
        }
        this.paymentService.updatePaymentWithValidation(pago.id, pago).toPromise()
          .then(success => console.log(success), (error: NtError) => this.notificationService.sendNotification('danger', error?.message,'Error validando el pago'))
          .then(() => this.updateDataTable(this.page.number, this.page.size))
      } else {
        this.updateDataTable(this.page.number, this.page.size);
      }
    });
  }

  public async deletePayment(payment: PagoBase) {

    try {
        await this.paymentService.deletePayment(payment.id).toPromise();
        this.updateDataTable();
    } catch (error) {
        this.notificationService.sendNotification(
            'danger',
            error?.message,
            'Error en el borrado del pago'
        );
    }
}

  public openComprobante(id:Number){
    window.open(`../api/pagos/${id}/comprobante`, "_blank");
  }

  public redirectToCfdi(folio: string) {
    this.router.navigate([`./pages/promotor/precfdi/${folio}`])
  }

  //validacion cuadro rojo por fila
  public cal(fila: any): any {

    const condition = (typeof fila.facturas !== 'undefined' && fila.facturas.length > 0);
    const totalFactura = condition ? fila.facturas.reduce((a, b) => a.totalFactura + b.totalFactura) : 0;
    const metodoPago = condition ? fila.facturas[0].metodoPago : 'PPD';

    if ((+(totalFactura.totalFactura) - (+fila.monto) !== 0 && metodoPago === 'PUE')) {

      return '#ff9494';
    }

    if (Math.abs(new Date(fila.fechaCreacion).getTime() - new Date(fila.fechaPago).getTime()) / 86400000 > 15) {

      return '#ffd982';
    }

    return 'null';
  }



}
