#!/usr/bin/env python3

import sys
import pika

EXCHANGE = 'hospital'


def errprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


def get_channel():
    """Initializes rabbitmq connection and ensures the common exchange exists"""

    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()

    channel.exchange_declare(exchange=EXCHANGE, exchange_type='topic')
    return channel
