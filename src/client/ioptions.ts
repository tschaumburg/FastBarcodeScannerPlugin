export
{ }
//*************************************************************************
//* 
//* 
//*************************************************************************
export enum Facing
{
    Back = 0,
    Front = 1,
    External = 2
}

export enum EventConflation
{
    None = 0,
    First = 1,
    Changes = 2,
    All = 3
}

export interface ICameraOptions
{
    enablePreview: boolean;
    facing: Facing;
    minPixels: number;
}

export interface IScanOptions
{
    emptyMarker: string;
    beginsWith: string;
}

export interface ITrackingOptions
{
    trackingMargin: number;
    trackingPatience: number;
}

export interface ICallBackOptions
{
    includeImage: boolean;

    conflateHits: EventConflation;
    conflateBlanks: EventConflation;
    conflateErrors: EventConflation;
    debounceBlanks: number;
    debounceErrors: number;
}

export interface IOptions
{
    readonly cameraOptions: ICameraOptions;
    readonly scanOptions: IScanOptions;
    readonly trackingOptions: ITrackingOptions;
    readonly callBackOptions: ICallBackOptions;
}

