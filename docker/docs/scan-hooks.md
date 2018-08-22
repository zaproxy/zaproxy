# Scan Hooks
To make it easy to make little tweaks here and there a hook system is in place to help you. 
It enables you to override or modify behaviour of the script components instead of having 
to write a new script.

## Use Cases
**Modfiying Args**  
For the AJAX crawler you may want to target a suburl with a specific 
hash (`http://example.com` vs `http://example.com/#/dashboard`). You can use the 
`zap_ajax_spider` hook to intercept the arguments and modify them.

**Running Scripts**  
There may be some standalone scripts that you want to run before crawling and scanning. 
You can use the hook `zap_started` to run some scripts before the rest of the scan runs.

**Changing Configs**  
For some applications, there are a number of active scan scanners that you may want to
disable that are not applicable for that application. Configuring your policy
before the active scan using `zap_active_scan` hook can ensure you only run the 
tests you want to run.


## Example
#### Define your hooks in a python file `my-hooks.py`
You define all the hooks you want to integrate with using python methods that
correspond with the name of the hook. By default, ZAP scans will load hooks defined in
`~/.zap_hooks.py` or you may specify the hooks location using a command line flag `--hook=my-hooks.py`. 

```python
# vim my-hooks.py
# Change the zap_ajax_spider target to hit dashboard hash 
# Change the crawl_depth to 2
def zap_ajax_spider(zap, target, max_time):
  zap.ajaxSpider.set_option_max_crawl_depth(2)
  return zap, target + '#/dashboard', max_time
```
#### Run scan with hook flag
```sh
# Run baseline directly
zap-baseline.py -t https://example.com --hook=my-hooks.py

# or using Docker 
docker run -v $(pwd):/zap/wrk/:rw -t owasp/zap2docker-stable zap-baseline.py \
    -t https://www.example.com -g gen.conf -r testreport.html --hook=/zap/wrk/my-hooks.py
```

## List of Hooks
- `cli_opts(opts)`
- `zap_started(zap, target)`
- `importing_openapi(target_url, target_file)`
- `importing_soap(target_url, target_file)`
- `load_config(config, config_dict, config_msg, out_of_scope_dict)`
- `print_rules_wrap(count, inprog_count)`
- `start_zap(port, extra_zap_params)`
- `start_docker_zap(docker_image, port, extra_zap_params, mount_dir)`
- `start_docker_zap_wrap(cid)`
- `zap_access_target(zap, target)`
- `zap_spider(zap, target)`
- `zap_spider_wrap()`
- `zap_ajax_spider(zap, target, max_time)`
- `zap_ajax_spider_wrap()`
- `zap_active_scan(zap, target, policy)`
- `zap_active_scan_wrap()`
- `zap_get_alerts(zap, baseurl, blacklist, out_of_scope_dict)`
- `zap_get_alerts_wrap(alert_dict)`
- `zap_import_context(zap, context_file)`
- `zap_import_context_wrap(context_id)`
- `zap_pre_shutdown(zap)`
- `pre_exit(fail_count, warn_count, pass_count)`

