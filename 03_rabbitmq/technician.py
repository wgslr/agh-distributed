#!/usr/bin/env python3

import pika
import sys
import time
import random

import common
from common import errprint


def make_skill_callback(skill):
    def callback(ch, method, properties, body):
        injury, name = body.decode().split(maxsplit=1)

        if injury != skill:
            errprint("'{}' handler received unexpected message!".format(skill))

        errprint("performing {} test on {}".format(injury, name))
        time.sleep(random.uniform(3, 7))
        errprint("{} test on {} done".format(injury, name))

        reply_to = properties.reply_to
        response = "{} {} done".format(name, injury)
        ch.basic_ack(delivery_tag=method.delivery_tag)
        ch.basic_publish(exchange=common.EXCHANGE,
                         routing_key=reply_to,
                         body=response)

    return callback


def on_info(ch, method, properties, body):
    print("[INFO] {}".format(body.decode()))


if __name__ == '__main__':
    _connection, channel = common.connect()

    channel.queue_declare('', exclusive=True)
    channel.queue_bind(exchange=common.EXCHANGE, queue='', routing_key='info')
    channel.basic_consume(
        queue='', on_message_callback=on_info, auto_ack=True)
    for skill in sys.argv[1:3]:
        errprint("Binding skill {}".format(skill))

        routing_key = 'request.#.{}'.format(skill)
        queue_name = 'requests.{}'.format(skill)

        channel.queue_declare(queue_name)
        channel.queue_bind(exchange=common.EXCHANGE,
                           queue=queue_name, routing_key=routing_key)
        channel.basic_qos(prefetch_count=1)
        channel.basic_consume(
            queue=queue_name, on_message_callback=make_skill_callback(skill), auto_ack=False)

    try:
        channel.start_consuming()
    except KeyboardInterrupt:
        pass
