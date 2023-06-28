import { Injectable } from '@angular/core';
import { Client } from '../../models/client';


@Injectable({
  providedIn: 'root',
})
export class ClientsValidatorService {

    camposObligatoriosCliente = [
        {
          field: 'porcentajePromotor',
          description: 'porcentaje promotor',
        },
        {
          field: 'porcentajeCliente',
          description: 'porcentaje cliente',
        },
        {
          field: 'porcentajeDespacho',
          description: 'porcentaje despacho',
        },
        {
          field: 'porcentajeContacto',
          description: 'porcentaje contacto',
        },
        {
          field: 'correoPromotor',
          description: 'correo promotor',
        },
        {
          field: 'rfc',
          description: 'RFC',
        },
        {
          field: 'razonSocial',
          description: 'Razón Social',
        },
        {
          field: 'cp',
          description: 'Código Postal',
        },
        {
          field: 'municipio',
          description: 'Municipio',
        },
        {
          field: 'estado',
          description: 'Estado',
        },
        {
          field: 'localidad',
          description: 'Localidad',
        },
        {
          field: 'calle',
          description: 'Calle',
        },
        {
          field: 'regimenFiscal',
          description: 'Regimen Fiscal',
        },
      ];
    
      camposObligatoriosInformacionFiscal = [
        
      ];

      public validarCliente(cliente: Client) {
        const messages: string[] = [];
        if (cliente == null) {
            throw 'El cliente a validar tiene un valor de null';
          }

        this.camposObligatoriosCliente.forEach(campo => {
          if (cliente[campo.field] === null || cliente[campo.field] === '*'
            || cliente[campo.field] === '') {
              messages.push(`El campo '${campo.description}' es obligatorio`);
            }
        });
        if (Math.abs(cliente.porcentajeCliente + cliente.porcentajeContacto
                + cliente.porcentajeDespacho + cliente.porcentajePromotor - 16) >= 0.01) {
            messages.push('La suma de los porcentajes asignados no debe ser mayor o menor a 16%');
        }
       if (cliente.porcentajeDespacho < 2) { 
            messages.push('El porcentaje asignado al despacho no debe ser menor al 2%');
        }
        return messages;
      }

}