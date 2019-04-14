#!/usr/bin/env python3

import pika
import sys
import datetime

import common
from common import errprint


def on_receive(ch, method, properties, body):
    time = datetime.datetime.now()
    print('{}: {}'.format(time, {'body': body, 'timestamp': properties.timestamp}))


if __name__ == '__main__':
    # TODO send broadcast messages

    _, channel = common.connect()

    routing_key = '#'
    queue_name = ''

    errprint("binding queue")
    channel.queue_declare(queue_name)
    channel.queue_bind(exchange=common.EXCHANGE,
                        queue=queue_name, routing_key=routing_key)
    errprint("bound queue")
    channel.basic_consume(
        queue=queue_name, on_message_callback=on_receive, auto_ack=True)

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        pass
