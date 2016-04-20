# -*- coding: utf-8 -*-

from __future__ import print_function

import time

from pprint import pprint
from zapv2 import ZAPv2

target = 'http://127.0.0.1'
# By default ZAP API client will connect to port 8080
zap = ZAPv2(proxies={'http': '127.0.0.1:9090', 'https': '127.0.0.1:9090'})

# Use the line below if ZAP is not listening on port 8080, for example, if listening on port 8090
# zap = ZAPv2(proxies={'http': 'http://127.0.0.1:8090', 'https': 'http://127.0.0.1:8090'})

# do stuff
print('Accessing target %s' % target)
# try have a unique enough session...
zap.urlopen(target)
# Give the sites tree a chance to get updated
time.sleep(2)

print('Spidering target %s' % target)
scanid = zap.spider.scan(target)
# Give the Spider a chance to start
time.sleep(2)
while int(zap.spider.status(scanid)) < 100:
	print('Spider progress %: ' + zap.spider.status(scanid))
	time.sleep(2)

print('Spider completed')
# Give the passive scanner a chance to finish
time.sleep(5)

print('Scanning target %s' % target)
scanid = zap.ascan.scan(target)
while int(zap.ascan.status(scanid)) < 100:
	print('Scan progress %: ' + zap.ascan.status(scanid))
	time.sleep(5)

print('Scan completed')

# Report the results

print('Hosts: ' + ', '.join(zap.core.hosts))
print('Alerts: ')
pprint((zap.core.alerts()))