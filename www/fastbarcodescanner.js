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
    stopScanning: function(successCallback, errorCallback) {
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
