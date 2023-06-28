import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpErrorResponse,
} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { NtError } from './@core/models/nt-error';
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor(private router: Router) {}

    intercept(
        request: HttpRequest<any>,
        next: HttpHandler
    ): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            catchError((err: HttpErrorResponse) => {
                console.error('AuthInterceptor', err);
                if (
                    err.status === 302 ||
                    err.status === 0 ||
                    err.statusText.indexOf('Unknown') >= 0
                ) {
                    console.error('Sesion perdida');
                    this.router.navigateByUrl('/sesion-lost');
                }
                if (
                    err.status === 401 ||
                    err.url.indexOf('/oauth2/authorization/google') > 1
                ) {
                    console.error('Unauthorized request');
                    this.router.navigateByUrl('/unauthorized');
                }

                if (err.status == 200 && err.url.indexOf('login?logout') > 0) {
                    console.warn('Logout');
                    this.router.navigateByUrl('/unauthorized');
                }
                const msg =
                    err.error.message || `${err.statusText} : ${err.message}`;
                    
                return throwError(new NtError(msg, err.error));
            })
        );
    }
}
