#!/usr/bin/env python3

import pika
import sys

import common
from common import errprint


def make_skill_callback(skill):
    def callback(ch, method, properties, body):
        errprint("handling message {} regarding skill {}".format(body.decode('utf8'), skill))
    return callback


if __name__ == '__main__':
    # TODO listen for INFO messages
    # TODO send replies to requests
    channel = common.get_channel()

    for skill in sys.argv[1:3]:
        errprint("Binding skill {}".format(skill))

        routing_key = 'request.#.{}'.format(skill)
        queue_name = 'requests.{}'.format(skill)

        channel.queue_declare(queue_name)
        channel.queue_bind(exchange=common.EXCHANGE,
                           queue=queue_name, routing_key=routing_key)
        channel.basic_consume(
            queue=queue_name, on_message_callback=make_skill_callback(skill), auto_ack=True)

    channel.start_consuming()
