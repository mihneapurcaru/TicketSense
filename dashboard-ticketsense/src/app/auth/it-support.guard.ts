import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { map } from 'rxjs';

export const itSupportGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const allowed = (role: string | undefined) =>
    role === 'IT_SUPPORT_MEMBER' || role === 'ADMIN';

  if (auth.isAuthenticated()) {
    return allowed(auth.user()?.role) ? true : router.createUrlTree(['/my-tickets']);
  }

  return auth.checkSession().pipe(
    map(valid => {
      if (!valid) return router.createUrlTree(['/login']);
      return allowed(auth.user()?.role) ? true : router.createUrlTree(['/my-tickets']);
    })
  );
};
