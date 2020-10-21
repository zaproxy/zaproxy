import re
import json
import os
import requests
import time
import sys
from zap_enviroment import *
from zap_Auth import *


#Read all swagger from the directory
def getFiles(directory):
    json_files = []
    for file in os.listdir(directory): 
        if 'swagger.json' in file:
            json_files.append(file)
        else:
            pass
    return json_files


#Do the authentication
def gen_token(url_env):
    if "juice-shop" in url_env:   #can be you prd, hml or dsv URL
        Final_token = auth_api(url_env)
        return Final_token



def main():
    
    filenames = getFiles(path_json)

    for file in filenames:
        Final_token = gen_token(url_env)
        
        #Creating the config file and adding new headers
        conf = open(path_json+"/"+file.split(".")[0]+".prop","w")
           
        conf.write("-config replacer.full_list(0).description=auth" + "\n")
        conf.write("-config replacer.full_list(0).enable=true" + "\n")
        conf.write("-config replacer.full_list(0).matchtype=REQ_HEADER" + "\n")
        conf.write("-config replacer.full_list(0).matchstr=Token" + "\n")
        conf.write("-config replacer.full_list(0).replacement='My unique token'" + "\n")
        conf.write("-config replacer.full_list(1).description=auth1" + "\n")
        conf.write("-config replacer.full_list(1).enable=true" + "\n")
        conf.write("-config replacer.full_list(1).matchtype=REQ_HEADER" + "\n")
        conf.write("-config replacer.full_list(1).matchstr=Authorization" + "\n")
        conf.write("-config replacer.full_list(1).replacement='Bearer " +Final_token+ "'\n") 
        
        conf.close()
        mass_scan(path_json,file,result_env, base_dir_env)

def mass_scan(path_json,swagger,result_env, base_dir_env):
        
    print(swagger)
    swagger_splited = swagger.split(".")[0]

    #Selecting the API's environment
    if "DSV_APIs" in base_dir_env:
        config_env = "dsv_api"
    elif "HML_APIs" in base_dir_env:
        config_env = "hml_api"
    else:
        config_env = "prd_api"
    
    #Starting the container using the zap-api params
    os.system(f'docker exec owasp_zap python3 zap-api-scan.py -e {config_env} \
       -t "{swagger_splited}.swagger.json" -f openapi -d -z {swagger_splited}.prop  -r {result_env}/{swagger_splited}.html -x {result_env}/{swagger_splited}.xml')
   
    print("finish him!")

if __name__ == "__main__":

    if len(sys.argv) != 2:
        print ("Usage: python3 zap-ConfigFile + environment")
        sys.exit(3)
    else:
        config = ENV[str(sys.argv[1]).upper()]  # Argument for the environment
    
    # Env Conf    
    url_env = config["url"]
    path_json = config["path_json"]
    result_env = config["result"]
    base_dir_env = config["base_dir_env"]


    print("Initializing...")
    main()
    print("END!!!")
    print("Starting Zap API Scan!")