import { IOptions, ICallBackOptions, ICameraOptions, IScanOptions, ITrackingOptions, EventConflation, Facing} from "../ioptions";
import { IFastBarcodeScannerPlugin } from "../iscanner";
import { ISessionBuilder, ISession, IImageSources } from "./ibuilder";

class SessionBuilder implements ISessionBuilder
{
    public constructor(private readonly plugin: IFastBarcodeScannerPlugin)
    {}

    protected readonly _options: Options = new Options();
    public camera(facing: Facing): ISessionBuilder
    {
        this._options.cameraOptions.facing = facing;
        return this;
    }

    public enablePreview(enabled: boolean): ISessionBuilder
    {
        this._options.cameraOptions.enablePreview = enabled;
        return this;
    }

    public resolution(minPixels: number): ISessionBuilder
    {
        this._options.cameraOptions.minPixels = minPixels;
        return this;
    }

    public emptyMarker(emptyMarkerContents: string): ISessionBuilder
    {
        this._options.scanOptions.emptyMarker = emptyMarkerContents;
        return this;
    }

    public beginsWith(prefix: string): ISessionBuilder
    {
        this._options.scanOptions.beginsWith = prefix;
        return this;
    }

    public debounceBlanks(nSamples: number): ISessionBuilder
    {
        this._options.callBackOptions.debounceBlanks = nSamples;
        return this;
    }

    public debounceErrors(nSamples: number): ISessionBuilder
    {
        this._options.callBackOptions.debounceErrors = nSamples;
        return this;

    }
    public conflateHits(hitConflation: EventConflation): ISessionBuilder
    {
        this._options.callBackOptions.conflateHits = hitConflation;
        return this;
    }

    public conflateBlanks(blankConflation: EventConflation): ISessionBuilder
    {
        this._options.callBackOptions.conflateBlanks = blankConflation;
        return this;
    }

    public conflateErrors(errorConflation: EventConflation): ISessionBuilder
    {
        this._options.callBackOptions.conflateErrors = errorConflation;
        return this;
    }

    public track(relativeTrackingMargin: number, nRetries: number): ISessionBuilder
    {
        this._options.trackingOptions.trackingMargin = relativeTrackingMargin;
        this._options.trackingOptions.trackingPatience = nRetries;
        return this;
    }

    public open(): ISession
    {
        return new Session(this.plugin, this._options);
    }
}

class Session implements ISession
{
    constructor(private readonly plugin: IFastBarcodeScannerPlugin, private readonly options: IOptions)
    {}

    start(success: (barcode: string) => void, error: () => void): void
    {
        this.plugin.startScanning(this.options, success, error);
    }

    stop(): void
    {
        this.plugin.stopScanning(null, null);
    }

    close(): void
    {
    }
}

export class Options implements IOptions
{
    public readonly cameraOptions: ICameraOptions = new CameraOptions();
    public readonly scanOptions: IScanOptions = new ScanOptions();
    public readonly trackingOptions: ITrackingOptions = new TrackingOptions();
    public readonly callBackOptions: ICallBackOptions = new CallBackOptions();
}

class CameraOptions implements ICameraOptions
{
    public enablePreview: boolean = true;
    public facing: Facing = Facing.Back;
    public minPixels: number = 1024 * 768;
}

class ScanOptions implements IScanOptions
{
    public emptyMarker: string = null;
    public beginsWith: string = null;
}

class TrackingOptions implements ITrackingOptions
{
    public trackingMargin: number = 0;
    public trackingPatience: number = 0;
}

class CallBackOptions implements ICallBackOptions 
{
    public includeImage: boolean = false;

    public conflateHits: EventConflation = EventConflation.Changes;
    public conflateBlanks: EventConflation = EventConflation.First;
    public conflateErrors: EventConflation = EventConflation.Changes;
    public debounceBlanks: number = 4;
    public debounceErrors: number = 3;
}

class ImageSources implements IImageSources
{
    backCamera(): ISessionBuilder
    {
        return new SessionBuilder(navigator.fastbarcodescanner).camera(Facing.Back);
    }
    frontCamera(): ISessionBuilder
    {
        return new SessionBuilder(navigator.fastbarcodescanner).camera(Facing.Front);
    }
    externalCamera(): ISessionBuilder
    {
        return new SessionBuilder(navigator.fastbarcodescanner).camera(Facing.External);
    }
}

export var imageSources: IImageSources = new ImageSources();

//module.exports =
//    {
//        imageSources: new ImageSources()
//    }
