import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { PagoBase } from '../../models/pago-base';
import { Catalogo } from '../../models/catalogos/catalogo';
import { Role } from '../models/role';

@Injectable({
    providedIn: 'root',
})
export class PaymentsService {
    constructor(private httpClient: HttpClient) {}

    public getPaymentsByFolio(folio: string): Observable<any> {
        return this.httpClient.get(`../api/facturas/${folio}/pagos`);
    }

    public getPaymentById(id: number): Observable<any> {
        return this.httpClient.get(`../api/pagos/${id}`);
    }

    public insertNewPayment(payment: PagoBase): Observable<any> {
        return this.httpClient.post(`../api/pagos`, payment);
    }

    public deletePayment(paymentId: number): Observable<any> {
        return this.httpClient.delete(`../api/pagos/${paymentId}`);
    }

    public updatePaymentWithValidation(
        paymentId: number,
        payment: PagoBase
    ): Observable<any> {
        return this.httpClient.put(`../api/pagos/${paymentId}`, payment);
    }

    public getFormasPago(roles?: Role[]): Observable<any> {
        const payTypeCat = [
            new Catalogo('EFECTIVO', 'Efectivo'),
            new Catalogo('CHEQUE', 'Cheque nominativo'),
            new Catalogo(
                'TRANSFERENCIA',
                'Transferencia electrónica de fondos'
            ),
            new Catalogo('DEPOSITO', 'Deposito bancario'),
        ];
        // custom rule to allow set credito despacho on invoices
        if (
            roles !== undefined &&
            roles.length > 0 &&
            roles.some((r) => r.role.indexOf('OPERADOR') == 0)
        ) {
            payTypeCat.push(new Catalogo('CREDITO', 'Credito despacho'));
        }
        return of(payTypeCat);
    }

    public getAllPayments(
        page: number,
        size: number,
        filterParams?: any
    ): Observable<any> {
        let pageParams: HttpParams = new HttpParams()
            .append('page', page.toString())
            .append('size', size.toString());
        for (const key in filterParams) {
            if (
                filterParams[key] !== undefined &&
                filterParams[key].length > 0
            ) {
                if (filterParams[key] instanceof Date) {
                    const date: Date = filterParams[key] as Date;
                    pageParams = pageParams.append(
                        key,
                        `${date.getFullYear()}-${
                            date.getMonth() + 1
                        }-${date.getDate()}`
                    );
                } else {
                    pageParams = pageParams.append(
                        key,
                        filterParams[key] === '*' ? '' : filterParams[key]
                    );
                }
            }
        }
        return this.httpClient.get('../api/pagos', { params: pageParams });
    }

    public getPaymentsReport(
        page: number,
        size: number,
        filterParams?: any
    ): Observable<any> {
        let pageParams: HttpParams = new HttpParams()
            .append('page', page.toString())
            .append('size', size.toString());
        for (const key in filterParams) {
            if (
                filterParams[key] !== undefined &&
                filterParams[key].length > 0
            ) {
                if (filterParams[key] instanceof Date) {
                    const date: Date = filterParams[key] as Date;
                    pageParams = pageParams.append(
                        key,
                        `${date.getFullYear()}-${
                            date.getMonth() + 1
                        }-${date.getDate()}`
                    );
                } else {
                    pageParams = pageParams.append(
                        key,
                        filterParams[key] === '*' ? '' : filterParams[key]
                    );
                }
            }
        }
        return this.httpClient.get('../api/pagos/report', {
            params: pageParams,
        });
    }

    public getIncomes(
        page: number,
        size: number,
        filterParams?: any
    ): Observable<Object> {
        let pageParams: HttpParams = new HttpParams()
            .append('page', page.toString())
            .append('size', size.toString());
        for (const key in filterParams) {
            let value: string;
            if (filterParams[key] instanceof Date) {
                let date: Date = filterParams[key] as Date;
                value = `${date.getFullYear()}-${
                    date.getMonth() + 1
                }-${date.getDate()}`;
            } else {
                value = filterParams[key];
            }
            if (value.length > 0) {
                pageParams = pageParams.append(
                    key,
                    filterParams[key] === '*' ? '' : value
                );
            }
        }
        return this.httpClient.get('../api/pagos/ingresos', {
            params: pageParams,
        });
    }

    public getIncomesSum(filterParams?: any): Observable<Object> {
        let pageParams: HttpParams = new HttpParams();
        for (const key in filterParams) {
            let value: string;
            if (filterParams[key] instanceof Date) {
                let date: Date = filterParams[key] as Date;
                value = `${date.getFullYear()}-${
                    date.getMonth() + 1
                }-${date.getDate()}`;
            } else {
                value = filterParams[key];
            }
            if (value.length > 0) {
                pageParams = pageParams.append(
                    key,
                    filterParams[key] === '*' ? '' : value
                );
            }
        }
        return this.httpClient.get('../api/pagos/ingresos/total', {
            params: pageParams,
        });
    }

    public getExpenses(
        page: number,
        size: number,
        filterParams?: any
    ): Observable<Object> {
        let pageParams: HttpParams = new HttpParams()
            .append('page', page.toString())
            .append('size', size.toString());
        for (const key in filterParams) {
            let value: string;
            if (filterParams[key] instanceof Date) {
                let date: Date = filterParams[key] as Date;
                value = `${date.getFullYear()}-${
                    date.getMonth() + 1
                }-${date.getDate()}`;
            } else {
                value = filterParams[key];
            }
            if (value.length > 0) {
                pageParams = pageParams.append(
                    key,
                    filterParams[key] === '*' ? '' : value
                );
            }
        }
        return this.httpClient.get('../api/pagos/egresos', {
            params: pageParams,
        });
    }

    public getExpensesSum(filterParams?: any): Observable<Object> {
        let pageParams: HttpParams = new HttpParams();
        for (const key in filterParams) {
            let value: string;
            if (filterParams[key] instanceof Date) {
                let date: Date = filterParams[key] as Date;
                value = `${date.getFullYear()}-${
                    date.getMonth() + 1
                }-${date.getDate()}`;
            } else {
                value = filterParams[key];
            }
            if (value.length > 0) {
                pageParams = pageParams.append(
                    key,
                    filterParams[key] === '*' ? '' : value
                );
            }
        }
        return this.httpClient.get('../api/pagos/egresos/total', {
            params: pageParams,
        });
    }

    public updatePayment(payment: PagoBase): Observable<any> {
        return this.httpClient.put(`../api/pagos/${payment.id}`, payment);
    }
}
