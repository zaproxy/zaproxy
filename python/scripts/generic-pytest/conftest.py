def pytest_addoption(parser):
    parser.addoption("--zapconfig", action="store", default="test_zap.config",
        help="ZAP client configuration file, default: test_zap.config")

def pytest_funcarg__zapconfig(request):
    return request.config.option.zapconfig

