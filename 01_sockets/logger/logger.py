#!/bin/env python3
import socket
import struct
import json
import datetime

ADDR = "224.0.0.128"
PORT = 8111
FILE = "tokens.log"

file = open(FILE, "a")

client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_IP)
client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

client.bind((ADDR, PORT))
mreq = struct.pack("4sl", socket.inet_aton(ADDR), socket.INADDR_ANY)
client.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

while True:
    msg = client.recv(1024)
    decoded = json.loads(msg)
    time = datetime.datetime.now()
    log = '{}: {}: {}\n'.format(time, decoded['node'], decoded['message'])
    file.write(log)
    file.flush()
    print(log, end='')

    
