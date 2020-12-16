/*
In addition to allowing you to specify exactly what can be attacked in a request,
input vector scripts now also allow you to change how requests are represented in the sites tree.
This script decodes URL and post parameters that are represented by JSON.
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

Note that you should not actively scan www.example.com - use a site that you have permission to test instead.
*/
var target = 'http://www.example.com/'

function parseParameters(helper, msg) {
	var uri = msg.getRequestHeader().getURI()
	print("parseParameters " + uri + " path=" + uri.getPath())
	if (!uri.toString().startsWith(target)) {
		print("  not target, ignoring")
		return
	}
	var q = uri.getEscapedQuery()
	var d = decodeURI(q)
	try {
		var json = JSON.parse(d)
	
		for (var i=0; i < json.length; i++) {
			print('  json[' + i + '] ' + json[i])
			print('  key in json ' + ('key' in json[i]))
			if ('key' in json[i] && 'value' in json[i]) {
				print('  Adding ' + json[i]['key'] + '=' + json[i]['value'])
	            helper.addParamQuery(json[i]['key'], json[i]['value']);
			}
		}
	} catch (err) {
		// Not JSON, not interested
		print('  URL params not JSON')
	}
	if (msg.getRequestHeader().getMethod() == "POST") {
		var body = decodeURI(msg.getRequestBody().toString())
		print("  body = " + body)
		try {
			json = JSON.parse(body)
			print('  JSON length ' + json.length)
		
			for (var i=0; i < json.length; i++) {
				print('  json[' + i + '] ' + json[i])
				print('  key in json ' + ('key' in json[i]))
				if ('key' in json[i] && 'value' in json[i]) {
					print('Adding ' + json[i]['key'] + '=' + json[i]['value'])
		            helper.addParamPost(json[i]['key'], json[i]['value']);
				}
			}
		} catch (err) {
			// Not JSON, not interested
			print('  Body not JSON')
		}
	}
}

function setParameter(helper, msg, param, value, escaped) {
	var uri = msg.getRequestHeader().getURI()
	print("setParameter " + uri + " param=" + param + " value=" + value)
	if (!uri.toString().startsWith(target)) {
		print("uri    " + uri.toString())
		print("target " + target)
		print("  not target, ignoring")
		return null;
	}
    var cParam = helper.getCurrentParam();
	if (cParam.getType() == 1) {
		// Query param
		var q = uri.getEscapedQuery()
		var d = decodeURI(q)
		try {
			var json = JSON.parse(d)
			for (var i=0; i < json.length; i++) {
				if ('key' in json[i] && json[i]['key'] == param) {
					json[i]['value'] = value
					break
				}
			}
			print('  setting query to ' + JSON.stringify(json))
			msg.getRequestHeader().getURI().setQuery(JSON.stringify(json))			
		} catch (err) {
			// Not JSON, not interested
			print('  URL params not JSON, unexpectedly')
		}

	} else if (cParam.getType() == 2) {
		// Post param
		var body = decodeURI(msg.getRequestBody().toString())
		try {
			var json = JSON.parse(body)
			for (var i=0; i < json.length; i++) {
				if ('key' in json[i] && json[i]['key'] == param) {
					json[i]['value'] = value
					break
				}
			}
			var body = JSON.stringify(json)
			print('  setting body to ' + body)
			msg.getRequestBody().setBody(body)
			msg.getRequestHeader().setContentLength(msg.getRequestBody().length)			
		} catch (err) {
			// Not JSON, not interested
			print('  Body not JSON, unexpectedly')
		}
	}
}

function getLeafName(helper, nodeName, msg) {
	var uri = msg.getRequestHeader().getURI()
	print("getLeafName " + uri)
	if (!uri.toString().startsWith(target)) {
		print("  not target, ignoring")
		return null
	}
	parseParameters(helper, msg)
	return helper.getStandardLeafName(nodeName, msg, helper.getParamList())
}


function getTreePath(helper, msg) {
	var uri = msg.getRequestHeader().getURI()
	print("getTreePath " + uri)
	if (!uri.toString().startsWith(target)) {
		print("  not target, ignoring")
		return null
	}
	var path = uri.getPath().split('/')
	if (path.length >= 6) {
		var list = []
		// Ignore the first and last (empty) values
		for (var x=1; x < path.length; x++) {
			if (x % 2 == 0) {
				list.push('<ddn>')
			} else {
				list.push(path[x])
			}
		}
		return list
	}
	return null
}
