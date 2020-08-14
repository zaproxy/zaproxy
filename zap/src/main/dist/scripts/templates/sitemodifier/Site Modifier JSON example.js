/*
Site modifier scripts allow you to change how requests are represented in the sites tree
This script decodes URL and post parameters that are represented by JSON
To demonstate this run the following curl commands from the command line with this script enabled:

export http_proxy=http://localhost:8090/
curl http://www.example.com/page/?%5B%7B%22key%22:%22aa%22,%22value%22:%22bb%22%7D%5D
curl http://www.example.com/page/?%5B%7B%22key%22:%22aa%22,%22value%22:%22bb%22%7D,%7B%22key%22:%22cc%22,%22value%22:%22dd%22%7D%5D
curl -d '[{"key":"aa","value":"ee"},{"key":"cc","value":"ff"}]' -X POST http://www.example.com/page/

It also changes the path of requests starting http://www.example.com/test/ so that every other path element after the initial 'test' is replaced by '<ddn>'
The built-in ZAP support for Data Driven Nodes (ie nodes where name is actually data rather than part of the application structure)
only supports one DDN per URL. This script shows that site modifier scripts can support an arbritrary number of them.
To demonstate this run the following curl commands from the command line with this script enabled:

export http_proxy=http://localhost:8090/
curl http://www.example.com/test/aaa/test2/bbb/
curl http://www.example.com/test/ccc/test2/ddd/
curl http://www.example.com/test/ccc/test2/ddd/eee/fff/ggg/hhh
curl http://www.example.com/test/ccc/test2/eee/fff/ggg/hhh/iii
*/
var ArrayList = Java.type('java.util.ArrayList')
var DefaultNameValuePair = Java.type('org.zaproxy.zap.model.DefaultNameValuePair')

function getParameters(msg, type, helper) {
	print("get params " + msg.getRequestHeader().getURI() + " " + type)
	var list = new ArrayList()
	switch (type.name()) {
		case 'url':
			var q = msg.getRequestHeader().getURI().getEscapedQuery()
			var d = decodeURI(q)
			try {
				var json = JSON.parse(d)
				print('JSON length ' + json.length)
			
				for (var i=0; i < json.length; i++) {
					print('json[' + i + '] ' + json[i])
					print('key in json ' + ('key' in json[i]))
					if ('key' in json[i] && 'value' in json[i]) {
						print('Adding ' + json[i]['key'] + '=' + json[i]['value'])
						list.add(new DefaultNameValuePair(json[i]['key'], json[i]['value']))
					}
				}
			} catch (err) {
				// Not JSON, not interested
				print('URL params not JSON')
			}
			break
		case 'form': 
			if (msg.getRequestHeader().getMethod() == "POST") {
				var body = decodeURI(msg.getRequestBody().toString())
				print("  body = " + body)
				try {
					json = JSON.parse(body)
					print('JSON length ' + json.length)
				
					for (i=0; i < json.length; i++) {
						print('json[' + i + '] ' + json[i])
						print('key in json ' + ('key' in json[i]))
						if ('key' in json[i] && 'value' in json[i]) {
							print('Adding ' + json[i]['key'] + '=' + json[i]['value'])
							list.add(new DefaultNameValuePair(json[i]['key'], json[i]['value']))
						}
					}
				} catch (err) {
					// Not JSON, not interested
					print('Body not JSON')
				}
			}
			break
		default: 
			break
	}
	if (list.size() == 0) {
		return null
	}
	return list
}

function getTreePath(msg, helper) {
	var uri = msg.getRequestHeader().getURI()
	print("getTreePath " + uri + " path=" + uri.getPath())
	if (uri.toString().startsWith('http://www.example.com/test/')) {
		var path = uri.getPath().split('/')
		if (path.length >= 6) {
			var list = new ArrayList()
			// Ignore the first and last (empty) values
			for (var x=1; x < path.length; x++) {
				if (x % 2 == 0) {
					list.add('<ddn>')
				} else {
					list.add(path[x])
				}
			}
			return list
		}
	}
	return null
}

