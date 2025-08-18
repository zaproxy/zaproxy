from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/')
def inicio():
    return "<h2>Bienvenido a la API del proyecto ZAPROXY </h2>"


