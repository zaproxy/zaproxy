an experiment on running ZAP as a k8s service

zap-k8s.py sets up zap on 0.0.0.0 with the port specified (8080 by default)

zap_remote_full_scan.py is an example orchestration script

you can launch it with
docker build -f Dockerfile-k8s . -t zap-k8s
kubectl apply -f k8s/zap_deployment.yaml

you can build and launch the orchestration script in kubernetes with:
docker build -f Dockerfile-fullscan . -t zap-fullscan
kubectl apply -f k8s/fullscan-test.yaml

the script will dump the findings on stdout, although you can pass arguments to get a report in several formats
