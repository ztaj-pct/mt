import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree , CanDeactivate} from "@angular/router";
import { Observable } from "rxjs";
import { DirtyComponent } from "../util/dirty-component";

@Injectable({
    providedIn: 'root'
  })
  export class DirtyCheckGuard implements CanDeactivate<DirtyComponent> {
  
    canDeactivate(
      component: DirtyComponent,
      next: ActivatedRouteSnapshot,
      state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
      if (component.canDeactivate()) {
        return confirm('You have done changes on this page. If you quit, you will lose your changes.');
      } else {
        return true;
      }
    }
  
  }