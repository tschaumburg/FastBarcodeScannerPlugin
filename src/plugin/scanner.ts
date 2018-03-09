import { IOptions } from "./ioptions";
import { IFastBarcodeScannerPlugin } from "./iscanner";

//*************************************************************************
//* 
//* 
//*************************************************************************
declare var cordova: any;

class FastBarcodeScannerPlugin implements IFastBarcodeScannerPlugin
{
    public startScanning(config: IOptions, successCallback: (barcode: string) => void, errorCallback: any)
    {
        cordova.exec(
            successCallback,
            errorCallback,
            'FastBarcodeScannerPlugin',
            'startScanning',
            [config]
        );
    }

    public showPreview(x: number, y: number, w: number, h: number)
    {
        var dpr = window.devicePixelRatio;
        console.error("DPR = " + dpr);
        cordova.exec(
            null,
            null,
            'FastBarcodeScannerPlugin',
            'showPreview',
            [{ 'x': x * dpr, 'y': y * dpr, 'w': w * dpr, 'h': h * dpr }]
        );
    }

    public hidePreview()
    {
        cordova.exec(
            null,
            null,
            'FastBarcodeScannerPlugin',
            'hidePreview',
            []
        );
    }

    public stopScanning(successCallback: () => void, errorCallback: any)
    {
        cordova.exec(
            successCallback,
            errorCallback,
            'FastBarcodeScannerPlugin',
            'stopScanning',
            []
        );
    }

    public simulate(barcode: string): void
    {
        cordova.exec(
            null,
            null, 
            'FastBarcodeScannerPlugin',
            'simulate',
            [{ barcode: barcode }]
        );
    }
};

var fbs = function () { return new FastBarcodeScannerPlugin();}

module.exports = {
    fastbarcodescanner: fbs()
}

//* 
//* +------------------------------------+    +----------------------------------+
//* |            MyPlugin.js             |    |          MyPlugin.d.ts           |
//* +------------------------------------+    +----------------------------------+
//* | var MyPlugin = (function() {...}); |    | export interface IMyPlugin {...} |
//* |                                    |    |                                  |
//* | exports.myplugin = new MyPlugin(); |    | declare global {                 |
//* |                                    |    |    interface Window {            |
//* |                                    |    |       myplugin: IMyPlugin        |
//* |                                    |    |    }                             |
//* |                                    |    | }                                |
//* +------------------------------------+    +----------------------------------+

