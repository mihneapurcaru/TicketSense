import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { map } from 'rxjs';

export const normalUserGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated()) {
    const role = auth.user()?.role;
    return role === 'NORMAL_USER' || role === 'ADMIN' || role === 'IT_SUPPORT_MEMBER' ? true : router.createUrlTree(['/dashboard']);
  }

  return auth.checkSession().pipe(
    map(valid => {
      if (!valid) return router.createUrlTree(['/login']);
      const role = auth.user()?.role;
    return role === 'NORMAL_USER' || role === 'ADMIN' || role === 'IT_SUPPORT_MEMBER' ? true : router.createUrlTree(['/dashboard']);
    })
  );
};
