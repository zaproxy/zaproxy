#!/usr/bin/python3


ENV = {
    "DSV_API" : {
        "url": "http://juice-shop.herokuapp.com/rest/user/login",
        "path_json": "./wrk/DSV_APIs",
        "result":"results",
        "base_dir_env":"/zap/wrk/DSV_APIs/",
    },
    "HML_API" : {
        "url": "http://juice-shop.herokuapp.com/rest/user/login",
        "path_json": "./wrk/HML_APIs",
        "result":"results",
        "base_dir_env":"/zap/wrk/HML_APIs/",
    },
    "PRD_API" : {
        "url": "http://juice-shop.herokuapp.com/rest/user/login",
        "path_json": "./wrk/PRD_APIs",
        "result":"results",
        "base_dir_env":"/zap/wrk/PRD_APIs/",
    },
}