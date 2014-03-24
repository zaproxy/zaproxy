Example Code
============
::

   #!/usr/bin/env python
   import time
   target = 'http://127.0.0.1'
   zap = ZAP()

   # do stuff
   print 'Starting session, scan and spider against %s' % target
   # try have a unique enough session...
   zap.urlopen(target)
   zap.start_scan(target)
   zap.start_spider(target)
   print 'A little sleep...'
   time.sleep(5)

   # Check status
   print 'Hosts: ' + ', '.join(zap.hosts)
   print 'Sites: ' + ', '.join(zap.sites)
   print 'Urls: ' + ', '.join(zap.urls)
   print 'Scan %: ' + zap.scan_status[0]
   print 'Spider %: ' + zap.spider_status[0]
   
   
   import time
   from pprint import pprint
   from zap import ZAP

   target = 'http://127.0.0.1'
   zap = ZAP()
   # Use the line below if ZAP is not listening on 8090
   # zap = ZAP(proxies={'http': 'http://127.0.0.1:8090', 'https': 'http:127.0.0.1:8090'})

   # do stuff
   print 'Accessing target %s' % target
   # try have a unique enough session...
   zap.urlopen(target)
   # Give the sites tree a chance to get updated
   time.sleep(2)

   print 'Spidering target %s' % target
   zap.start_spider(target)
   # Give the Spider a chance to start
   time.sleep(2)
   while (int(zap.spider_status[0]) < 100):
       print 'Spider progress %: ' + zap.spider_status[0]
       time.sleep(5)

   print 'Spider completed'
   # Give the passive scanner a chance to finish
   time.sleep(5)

   print 'Scanning target %s' % target
   zap.start_scan(target)
   while (int(zap.scan_status[0]) < 100):
       print 'Scan progress %: ' + zap.scan_status[0]
       time.sleep(5)

   print 'Scan completed'

   # Report the results

   print 'Hosts: ' + ', '.join(zap.hosts)
   print 'Alerts: '
   pprint (zap.alerts)
