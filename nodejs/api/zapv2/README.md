node-zaproxy
============

ZAProxy Client API for Node.js. Generated using the ZAProxy API generator.

## Getting Started
Install with:

    npm install zaproxy

## Usage
Example:

```js
    var options = { proxy: 'http://localhost:8080' };
    var ZapClient = require('zaproxy');
    
    var zaproxy = new ZapClient(options);
    zaproxy.core.sites(function (err, resp) {
      resp.sites.forEach(function (site) {
        // do something with the site
      });  
    });
```

## API
For a full API list, see [https://github.com/zaproxy/zaproxy/wiki/ApiGen_Index](https://github.com/zaproxy/zaproxy/wiki/ApiGen_Index).

The Node.js API methods have the same signature as the API documentation, except that they all take a callback as their last parameter. The callback will be called with error and response arguments, with the response being the an object that corresponds to the JSON output of the API call.
