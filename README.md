# The fast-barcode-scanner plugin
Cordova plugin for fast, continuous, highly configurable scanning of barcodes

- ***Fast:*** scans and parses a realtime video feed at a rate of 30fps@2048x1536.
  
- ***Continuous:*** Just start it, and it will send you an event for each barcode it acquires, until you stop it.
- ***Configurable:***
    - *<u>Filtering:</u>* Don't waste time on barcodes you're not interested in 
      by filtering based on barcode type and contents (see xxx).
    - *<u>Conflation:</u>* When running in raw mode, fbs will send you an event 30 times per second - whether 
      anything significant changed or not.<br/> 
      With conflation, you can configure what you consider important (see xxx).

    - *<u>Debouncing:</u>* Errors happen in the real world, and any real-life
      sequence of scan events will contain random errors or false-blank readings.<br/>
      To avoid this, you can set up debouncing - i.e. require that an error or false-blank condtion persists 
      for a number of readings before being accepted (see xxx).
    - *<u>Optimistic tracking:</u>* When scanning at a high rate, a good place to look
      for a barcode is in the area you last found one - after all, how
      much will things usually change in 35ms?<br/>
      If this is a valid reasoning for your application, you will benefit from setting
      up *optimistic tracking* (see xxx)

## Getting it

    cordova plugin add cordova-plugin-fast-barcode-scanner

## Initializing

    fbs = window.navigator.fastbarcodescanner;
    fbs.init({...})

## Starting

    fbs.start(success, error);

## Processing events

    function success(bcinfo, image)
    {
       if (!bcinfo)
          console.log("fast-barcode-scanner: BLANK");
       else
          console.log(
             "fast-barcode-scanner: HIT:   " + 
             bcinfo.contents + 
             " at (" + 
             bcinfo.points[0].x + 
             ", " + 
             bcinfo.points[0].y + 
             ")"
          );
    }

    function error(reason)
    {
       console.error("fast-barcode-scanner: ERROR: " + reason);
    }

## Closing

    fbs.stop();
    fbs.close();
