const { withNativeFederation, shareAll } = require('@angular-architects/native-federation/config');

module.exports = withNativeFederation({
  name: 'mf-tps',
  exposes: {
    './Component': './src/app/tps-viewer/tps-viewer.component.ts',
  },
  shared: {
    ...shareAll({ singleton: true, strictVersion: true, requiredVersion: 'auto' }),
  },
  skip: [
    'rxjs/ajax',
    'rxjs/fetch',
    'rxjs/testing',
    'rxjs/webSocket',
    p => p.startsWith('@angular/animations'),
    p => p.startsWith('@angular/platform-browser/animations'),
  ],
});
