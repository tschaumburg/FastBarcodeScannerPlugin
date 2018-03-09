//var options = require("./options");
import { IOptions } from "./ioptions";

export interface IFastBarcodeScannerPlugin
{
    startScanning(config: IOptions, successCallback: (barcode: string) => void, errorCallback: any): void;
    showPreview(x: number, y: number, w: number, h: number): void;
    hidePreview(): void;
    stopScanning(successCallback: () => void, errorCallback: any): void;

    simulate(barcode: string): void;
}

declare global
{
    interface Navigator // Window
    { 
        fastbarcodescanner: IFastBarcodeScannerPlugin;
    }
}
