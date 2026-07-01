import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { map } from 'rxjs';

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated()) {
    return auth.user()?.role === 'ADMIN' ? true : router.createUrlTree(['/dashboard']);
  }

  return auth.checkSession().pipe(
    map(valid => {
      if (!valid) return router.createUrlTree(['/login']);
      return auth.user()?.role === 'ADMIN' ? true : router.createUrlTree(['/dashboard']);
    })
  );
};
