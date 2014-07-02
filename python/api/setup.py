#!/usr/bin/env python
"""
Standard build script.
"""

__docformat__ = 'restructuredtext'


try:
    from setuptools import setup, find_packages
except ImportError:
    print "You must have setuptools installed to use setup.py. Exiting..."
    raise SystemExit(1)


setup(
    name="python-owasp-zap-v2",
    version="0.0.9",
    description="OWASP ZAP API client",
    long_description="OWASP Zed Attack Proxy API v2 python client",
    author="ZAP development team",
    author_email='',
    url="https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project",
    download_url="https://sourceforge.net/projects/zaproxy/files/client-api/",
    platforms=['any'],

    license="ASL2.0",

    package_dir={
        '': 'src',
    },
    packages=find_packages('src'),

    classifiers=[
        'License :: OSI Approved :: Apache Software License',
        'Development Status :: 4 - Beta',
        'Topic :: Security',
        'Topic :: Software Development :: Libraries :: Python Modules',
        'Intended Audience :: Developers',
        'Intended Audience :: Information Technology',
        'Programming Language :: Python'],
)
