// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  // baseUrl:'http://3.233.193.155:5555'
 baseUrl: 'http://localhost:5555',
  // baseUrl:'http://54.166.118.86:5555'//
 // baseUrl:'https://ms2-api.qa.phillips-connect.net',
  // baseUrl:'https://api.ms2.phillips-connect.net/',
  IabaseUrl: 'https://api.phillips-connect.com',
  IaUiUrl: 'http://ia.dev.phillips-connect.net',
  deviceSecuritySettingsUrl: 'https://device-signature.qa.phillips-connect.net' // OrderNow QA Env
 // deviceSecuritySettingsUrl: 'http://3.239.30.128:7073' // OrderNow QA Env

};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.