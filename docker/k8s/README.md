an experiment on running ZAP as a k8s service

zap-k8s.py sets up zap on the specified values and launches a separate http server on 9090 which keeps zap running and will shut it down when it receives a single request on /trigger

e2e_tests.py is an example orchestration script

docker build -f Dockerfile-k8s . -t zap-k8s 
 docker run --network host zap-k8s:latest -d -P 8080 --triggerPort 9090
 
 kubectl port-forward svc/zap-svc 8080:8080 9090:9090 
 