import { IOptions, EventConflation, Facing} from "../ioptions";

export interface IImageSources
{
    backCamera(): ISessionBuilder;
    frontCamera(): ISessionBuilder;
    externalCamera(): ISessionBuilder;
}

export interface ISessionBuilder
{
    enablePreview(enabled: boolean): ISessionBuilder;
    resolution(minPixels: number): ISessionBuilder;
    emptyMarker(emptyMarkerContents: string): ISessionBuilder;
    beginsWith(prefix: string): ISessionBuilder;
    debounceBlanks(nSamples: number): ISessionBuilder;
    debounceErrors(nSamples: number): ISessionBuilder;
    conflateHits(hitConflation: EventConflation): ISessionBuilder;
    conflateBlanks(blankConflation: EventConflation): ISessionBuilder;
    conflateErrors(errorConflation: EventConflation): ISessionBuilder;
    track(relativeTrackingMargin: number, nRetries: number): ISessionBuilder;
    open(): ISession;
}

export interface ISession
{
    start(success: (barcode: string) => void, error: () => void): void;
    stop(): void;
    close(): void;
}

