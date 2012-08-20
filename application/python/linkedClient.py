#!/usr/bin/env python
import oauth2 as oauth
import httplib2
import time, os, simplejson
 
# Fill the keys and secrets you retrieved after registering your app
consumer_key      =   'so5j3fqc12bx'
consumer_secret  =   'OtH49Qk1Z3q12KkO'
user_token           =   'bf61202e-7390-4e21-a286-fdb52f3cb1be'
user_secret          =   '5ee8795c-cb7c-487d-a39b-1998cb5203f4'



 
# Use your API key and secret to instantiate consumer object
consumer = oauth.Consumer(consumer_key, consumer_secret)
 
# Use your developer token and secret to instantiate access token object
access_token = oauth.Token(
            key=user_token,
            secret=user_secret)
 

client = oauth.Client(consumer, access_token)
 
# Make call to LinkedIn to retrieve your own profile
resp,content = client.request("http://api.linkedin.com/v1/people/~", "GET", "")
print content
print resp
 
# By default, the LinkedIn API responses are in XML format. If you prefer JSON, simply specify the format in your call
# resp,content = client.request(""http://api.linkedin.com/v1/people/~?format=JSON", "GET", {})

