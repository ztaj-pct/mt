import { Component, OnInit } from '@angular/core';
import { Router, NavigationStart, NavigationEnd } from '@angular/router';
import { LoginService } from './login/login.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent implements OnInit {
  title = 'PCT';
  count = 0;

  constructor(public router: Router, private loginService: LoginService) { 
    // this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    // this.router.events.subscribe((event) => {
    //   if (event instanceof NavigationEnd) {
    //     if(event.url !== "/login")
    //     this.router.navigated = false;
    //   } 
    // }); 
  }
  ngOnInit() {
   
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        this.setLoginInfo(e);
        if(!e.url.includes("/reset-password")){
          this.loginService.beforeResetPasswordUrl = e.url;
        }
        if(!e.url.includes("/add-assets")){
          //this.loginService.beforeAssetCancelUrl = e.url;
          sessionStorage.setItem('beforeAssetCancelUrl',e.url);
        }
        if(!e.url.includes("/upload-assets")){
          sessionStorage.setItem('beforeUploadAssetUrl',e.url);
        }

        if (!e.url.includes("/forgot-password") && (sessionStorage.getItem("loggedInUser") === undefined || sessionStorage.getItem("loggedInUser") === null || sessionStorage.getItem('loggedInUser') === "null")) {
          this.router.navigate(['/login']);
          if(!e.url.includes("/login")){
            this.loginService.beforeLoginUrl = e.url;
          }
        }
      }
      window.scrollTo(0, 0);
    });
  }

  setLoginInfo(e) {
    if(e.url.includes("/login?token=")) {
      const split = e.url.split("?");
      if (split.length == 2) {
          const params = split[1];
          const splitParam = params.split("&");
          for (let index = 0; index < splitParam.length; index++) {
            const element = splitParam[index];
            const elementSplit = element.split("=");
            if(elementSplit.length == 2) {
              sessionStorage.setItem(elementSplit[0], elementSplit[1]);
            } 
          }
      }
    }
  }
}
