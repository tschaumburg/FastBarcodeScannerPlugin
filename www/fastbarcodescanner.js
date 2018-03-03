var fastBarcodeScannerPlugin = {
    showToast: function(toastText) {
        cordova.exec(
            null,
            null,
            'FastBarcodeScannerPlugin',
            'showToast',
            [{'data': toastText}]
        );
    },
    startScanning: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            'FastBarcodeScannerPlugin',
            'startScanning',
            []
        );
    },
    showPreview: function (x, y, w, h)
    {
        dpr = window.devicePixelRatio;
        console.error("DPR = " + dpr);
        cordova.exec(
            null,
            null,
            'FastBarcodeScannerPlugin',
            'showPreview',
            [{ 'x': x * dpr, 'y': y * dpr, 'w': w * dpr, 'h': h * dpr }]
        );
    },
    hidePreview: function() {
        cordova.exec(
            null,
            null,
            'FastBarcodeScannerPlugin',
            'hidePreview',
            []
        );
    },
    stopScanning: function (successCallback, errorCallback)
    {
        cordova.exec(
            successCallback,
            errorCallback,
            'FastBarcodeScannerPlugin',
            'stopScanning',
            []
        );
    }
};

module.exports = fastBarcodeScannerPlugin;
